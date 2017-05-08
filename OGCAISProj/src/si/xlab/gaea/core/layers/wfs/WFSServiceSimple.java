/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.xlab.gaea.core.layers.wfs;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static si.xlab.gaea.core.layers.wfs.AbstractWFSLayer.logger;
import si.xlab.gaea.core.ogc.gml.GMLFeature;
import si.xlab.gaea.core.ogc.gml.GMLParser;

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

    public WFSServiceSimple(String service, String dataset, String queryfield, String queryvalue) {

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

    public String downloadGML() throws IOException {
//        String queryfield = "VoyageID";
//    String queryvalue = "134385";
//        String argUrl = "http://demo.luciad.com:8080/OgcAisServices/wfs?SERVICE=WFS"; 
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

        String xmlString = "<wfs:GetFeature xmlns:wfs=\"http://www.opengis.net/wfs/2.0\" xmlns:fes=\"http://www.opengis.net/fes/2.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" count=\"50000\" service=\"WFS\" version=\"2.0.0\">\n"
                + "<wfs:Query typeNames=\"AIS_US\">\n"
                + "<fes:Filter>\n"
                + "<fes:PropertyIsEqualTo>\n"
                + "<fes:ValueReference>" + this.getQueryField() + "</fes:ValueReference>\n"
                + "<fes:Literal>" + this.getQueryValue() + "</fes:Literal>\n"
                + "</fes:PropertyIsEqualTo>\n"
                + "</fes:Filter>\n"
                + "</wfs:Query>\n"
                + "</wfs:GetFeature>";
        writer.write(xmlString);
        writer.flush();
        writer.close();
        // reading the response
        InputStreamReader reader = new InputStreamReader(con.getInputStream());
        StringBuilder buf = new StringBuilder();
        char[] cbuf = new char[2048];
        int num;
        while (-1 != (num = reader.read(cbuf))) {
            buf.append(cbuf, 0, num);
        }

        String result = buf.toString();
//    System.err.println( "\nResponse from server after POST:\n" + result );
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

}
