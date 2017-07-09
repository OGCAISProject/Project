/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.xlab.gaea.core.layers.wfs;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Track;
import gov.nasa.worldwind.layers.SelectableIconLayer;
import gov.nasa.worldwind.ogc.kml.KMLStyle;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static si.xlab.gaea.core.layers.wfs.AbstractWFSLayer.logger;
import static si.xlab.gaea.core.layers.wfs.WFSGenericLayer.findFirstLinkedImage;
import si.xlab.gaea.core.ogc.gml.GMLFeature;
import si.xlab.gaea.core.ogc.gml.GMLGeometry;
import si.xlab.gaea.core.ogc.gml.GMLParser;
import si.xlab.gaea.core.ogc.gml.GMLPoint;
import si.xlab.gaea.core.render.DefaultLook;
import si.xlab.gaea.core.render.SelectableIcon;

/**
 *
 * @author Jing
 */
public class WFSServiceSimple {

    private final String service;
    private final String dataset;
    private final String urlBase; //full URL of a data ending with "&BBOX=", such that only the coordinates need to be appended
    private final String fileCachePath;
    private final String queryfield;
    private final String queryvalue;
    private final String resultstype;

    public WFSServiceSimple(String service, String dataset, String queryfield, String queryvalue, String resultstype) {

        this.service = service;
        this.dataset = dataset;
        this.queryfield = queryfield;
        this.queryvalue = queryvalue;
        StringBuilder urlBase = new StringBuilder(this.service);
        if (!this.service.endsWith("?") && !this.service.endsWith("&")) {
            if (this.service.contains("?")) {
                urlBase.append("&");
            } else {
                urlBase.append("?");
            }
        }
        urlBase.append("Service=WFS");
        this.urlBase = urlBase.toString();

        URI mapRequestURI = null;
        try {
            mapRequestURI = new URI(service);
        } catch (URISyntaxException e) {
            String message = Logging.getMessage(
                    "WFSService.URISyntaxException: ", e);

            Logging.logger().severe(message);
        }

        if (mapRequestURI != null) {
            this.fileCachePath = WWIO.formPath(mapRequestURI.getAuthority(),
                    mapRequestURI.getPath(), dataset, queryfield, queryvalue);
        } else {
            this.fileCachePath = WWIO.formPath(service, dataset, queryfield, queryvalue);
        }

        this.resultstype = resultstype;
    }

    public String getQueryField() {
        return this.queryfield;
    }

    public String getQueryValue() {
        return this.queryvalue;
    }

    public String geturlBase() {
        return this.urlBase;
    }

    public String downloadFeatures() throws IOException {
        URL url;

        url = new URL(this.geturlBase());
        URLConnection con = url.openConnection();
        // specify that we will send output and accept input
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setConnectTimeout(20000);  // long timeout, but not infinite
        con.setReadTimeout(20000);
        con.setUseCaches(false);
        con.setDefaultUseCaches(false);
        // tell the web server what we are sending
        con.setRequestProperty("Content-Type", "text/xml");
        OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
        String queryxmlString;

        queryxmlString = "<wfs:GetFeature "
                + "xmlns:wfs=\"http://www.opengis.net/wfs/2.0\" xmlns:fes=\"http://www.opengis.net/fes/2.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" "
                + " service=\"WFS\" version=\"2.0.0\"";
        if ("hit".equals(this.resultstype)) //number of features
        {
            queryxmlString = queryxmlString + "\n resultType=\"hits\"  >\n";
        } else { //features
            queryxmlString = queryxmlString + ">\n";
        }
        //need to check if 0 records returned.
        //attribute query
        queryxmlString = queryxmlString
                + "<wfs:Query typeNames=\"AIS_US\">\n";
        //selection

        if (this.queryvalue.contains(",")) {
            queryxmlString = queryxmlString
                    + "<fes:Filter>\n"
                    + "<fes:And>\n"
                    + "<fes:PropertyIsGreaterThanOrEqualTo>\n"
                    + "<fes:ValueReference>" + this.queryfield + "</fes:ValueReference>\n"
                    + "<fes:Literal>" + this.queryvalue.split(",")[0] + "</fes:Literal>\n"
                    + "</fes:PropertyIsGreaterThanOrEqualTo>\n"
                    + "<fes:PropertyIsLessThanOrEqualTo>\n"
                    + "<fes:ValueReference>" + this.queryfield + "</fes:ValueReference>\n"
                    + "<fes:Literal>" + this.queryvalue.split(",")[1] + "</fes:Literal>\n"
                    + "</fes:PropertyIsLessThanOrEqualTo>\n"
                    + "</fes:And>\n"
                    + "</fes:Filter>\n";

        } else {
            //filter      
            queryxmlString = queryxmlString
                    + "<fes:Filter>\n"
                    + "<fes:PropertyIsEqualTo>\n"
                    + "<fes:ValueReference>" + this.getQueryField() + "</fes:ValueReference>\n"
                    + "<fes:Literal>" + this.getQueryValue() + "</fes:Literal>\n"
                    + "</fes:PropertyIsEqualTo>\n"
                    + "</fes:Filter>\n";
        }

        //  sorting              
        queryxmlString = queryxmlString
                + "<fes:SortBy>\n"
                + "<fes:SortProperty>\n"
                + "<fes:ValueReference>timeConv</fes:ValueReference>\n" //does not work for time? --24 hour format!
                + "<fes:SortOrder>DESC</fes:SortOrder>\n"
                + "</fes:SortProperty>\n"
                + "</fes:SortBy>\n"
                + "</wfs:Query>\n"
                + "</wfs:GetFeature>";
        writer.write(queryxmlString);
        writer.flush();
        writer.close();
        // reading the response
        InputStreamReader reader = new InputStreamReader(con.getInputStream());
        StringBuilder buf = new StringBuilder();
        char[] cbuf = new char[2048];
        int num;
        while (-1 != (num = reader.read(cbuf))) {
            buf.append(cbuf, 0, num);
            System.err.println(num);
        }

        String result = buf.toString();
        System.err.println("\nResponse from server after POST:\n" + result);
        File cacheFileURL = WorldWind.getDataFileStore().newFile(this.fileCachePath + ".xml");
        String fileCachePath = cacheFileURL.toURI().getPath();
        PrintWriter out = new PrintWriter(fileCachePath);
        out.print(result);
        out.close();
        return fileCachePath;
    }

    public final String getFileCachePath() {
        return this.fileCachePath;
    }

    //create tracks based on flitered data sorted by vayoge ID
    public void CreateTracks(List<GMLFeature> gmlfeatures) {
        ArrayList<Track> paths = new ArrayList<Track>();

        for (GMLFeature gmlfeature : gmlfeatures) {
            GMLGeometry geometry;

            geometry = gmlfeature.getDefaultGeometry();
            GMLPoint gmlpoint = (GMLPoint) geometry;
            boolean pointadd = false;
            if (geometry instanceof GMLPoint) {

                int vogageid = 0; //from gml point

                for (Track path : paths) {
                    if (path.getID() == vogageid) {
                        path.addPosition(new Position(geometry.getCentroid(), 0));
                        pointadd = true;
                    }
                }
                if (pointadd != true) {
                    Track path = new Track(vogageid);
                    path.addPosition(new Position(geometry.getCentroid(), 0));
                    paths.add(path);
                }

            }
        }
    }

    public List<GMLFeature> readGMLData(String path) {
        java.io.InputStream is = null;

        try {

            path = path.replaceAll("%20", " "); // TODO: find a better way to get a path usable by FileInputStream
//            System.out.println(path);
            java.io.FileInputStream fis = new java.io.FileInputStream(path);
            java.io.BufferedInputStream buf = new java.io.BufferedInputStream(
                    fis);

            try {
                is = new java.util.zip.GZIPInputStream(buf);
            } catch (IOException e) {
                if (e.getMessage().contains("Not in GZIP format")) {
                    buf.close();
                    is = new java.io.BufferedInputStream(new java.io.FileInputStream(path));
                } else {
                    Logger.getLogger(AbstractWFSLayer.class.getName()).log(Level.SEVERE, "Failed to read tile data : " + e.getMessage());
                    return null;
                }
            }

            return GMLParser.parse(is);

        } catch (Exception e) {

        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (java.io.IOException e) {
                logger.log(Level.INFO,
                        "WFSLayer.ExceptionAttemptingToReadFile: ", e);
            }
        }

        return null;
    }

    public SelectableIconLayer GetIcons(List<GMLFeature> gmlfeatures) {
        KMLStyle style = new KMLStyle(DefaultLook.DEFAULT_FEATURE_STYLE);
        SelectableIconLayer iconlayer = new SelectableIconLayer();

        for (GMLFeature gmlfeature : gmlfeatures) {
            GMLGeometry geometry;
            geometry = gmlfeature.getDefaultGeometry();
            if (geometry instanceof GMLPoint) {
                GMLPoint gmlpoint = (GMLPoint) geometry;
                LatLon loc = gmlpoint.getCentroid();
                String desc = gmlfeature.buildDescription(null);
                String imageURL = findFirstLinkedImage(desc);
                SelectableIcon icon = new SelectableIcon(style,
                        new Position(geometry.getCentroid(), 0),
                        gmlfeature.getName(), desc, imageURL,
                        gmlfeature.getRelativeImportance(), true);

                iconlayer.addIcon(icon);

//                finalloc = loc;
            }
        }
        return iconlayer;
    }

    public ArrayList<Position> GetPositions(List<GMLFeature> gmlfeatures) {
        ArrayList<Position> pathPositions = new ArrayList<Position>();
        for (GMLFeature gmlfeature : gmlfeatures) {
            GMLGeometry geometry;
            geometry = gmlfeature.getDefaultGeometry();
            if (geometry instanceof GMLPoint) {
                GMLPoint gmlpoint = (GMLPoint) geometry;
                LatLon loc = gmlpoint.getCentroid();
                String desc = gmlfeature.buildDescription(null);
                String imageURL = findFirstLinkedImage(desc);
                pathPositions.add(new Position(geometry.getCentroid(), 100));
//                finalloc = loc;
            }
        }
        return pathPositions;
    }

    public static int readGMLCount(String path) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String sCurrentLine = br.readLine();
            while (sCurrentLine != null) {
                String[] parts = sCurrentLine.split(" ");
                for (String part : parts) {
                    if (part.contains("numberOfFeatures=")) {

                        return Integer.valueOf(part.split("\"")[1]);
                    }
                }
                sCurrentLine = br.readLine();
            }

        } catch (Exception ex) {
            Logger.getLogger(WFSServiceSimple.class.getName()).log(Level.SEVERE, null, ex);

        }
        return 0;
    }
}
