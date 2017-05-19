package edu.du.ogc.ais.examples;

import static edu.du.ogc.ais.examples.WFSExampleSimple.pathPositions;
import edu.du.ogc.netcdf.NetCDFReader2D;
import gov.nasa.worldwind.geom.LatLon;
import java.util.ArrayList;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import static gov.nasa.worldwind.util.Logging.logger;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//import for javafx to create charts
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.BarChart;



import javax.imageio.ImageIO;
import si.xlab.gaea.core.layers.wfs.AbstractWFSLayer;
import static si.xlab.gaea.core.layers.wfs.WFSGenericLayer.findFirstLinkedImage;
import si.xlab.gaea.core.layers.wfs.WFSServiceSimple;
import si.xlab.gaea.core.ogc.gml.GMLFeature;
import si.xlab.gaea.core.ogc.gml.GMLGeometry;
import si.xlab.gaea.core.ogc.gml.GMLParser;
import si.xlab.gaea.core.ogc.gml.GMLPoint;

public class VerticalProfiling  {

    private NetCDFReader2D nctest; //store a list of climate web services within the time frame
    static ArrayList<Position> pathPositions = new ArrayList<Position>();  //store a list of tracks without temporal information 
    private String variablename;
    ArrayList<Float> zvalueall = new ArrayList<Float>();

//	protected ArrayList<String> timesteps; //store a list of time steps 
    //https://ioos.noaa.gov/project/hf-radar/ wcs 1.0.0 http://sdf.ndbc.noaa.gov/wcs/
    public VerticalProfiling(String climatefilepath, String trackfilepath) {

        nctest = new NetCDFReader2D(climatefilepath);
        nctest.ReadNetCDF();
        GetPositions(readGMLData(trackfilepath));
        variablename = nctest.GetVariableName();
        this.GetProfling();

    }

    private static void GetPositions(List<GMLFeature> gmlfeatures) {

        for (GMLFeature gmlfeature : gmlfeatures) {
            GMLGeometry geometry;
            geometry = gmlfeature.getDefaultGeometry();
            if (geometry instanceof GMLPoint) {
                GMLPoint gmlpoint = (GMLPoint) geometry;
                LatLon loc = gmlpoint.getCentroid();
                String desc = gmlfeature.buildDescription(null);
                pathPositions.add(new Position(geometry.getCentroid(), 0));

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

            }
        }

        return null;
    }

    public ArrayList<Float> GetProfling() {

        for (int i = 0; i < pathPositions.size() - 1; i++) {
            Position pos1 = pathPositions.get(i);
            Position pos2 = pathPositions.get(i + 1);
            int[] pos1_int = nctest.ConvertPosToIndex(pos1.longitude.degrees, pos1.latitude.degrees);
            int[] pos2_int = nctest.ConvertPosToIndex(pos2.longitude.degrees, pos2.latitude.degrees);
            zvalueall.addAll(FindRasterCellOnLine(pos1_int[0], pos1_int[1], pos2_int[0], pos2_int[1]));
        }
        return zvalueall;
    }

    public String GetVariableName()
    {
        return nctest.GetVariableName();
    }
    
    public ArrayList<Float> GetZValues()
    {
        return this.zvalueall;
    }
    
//	Bresenham Algorithm
//	https://rosettacode.org/wiki/Bitmap/Bresenham%27s_line_algorithm#Java
    //x1, y1, x2, y2 : two end points of a line segement
    private ArrayList<Float> FindRasterCellOnLine(int x1, int y1, int x2, int y2) {
        // delta of exact value and rounded value of the dependant variable
        ArrayList<Float> zvalue = new ArrayList<Float>();
        int d = 0;

        int dy = Math.abs(y2 - y1);
        int dx = Math.abs(x2 - x1);

        int dy2 = (dy << 1); // slope scaling factors to avoid floating
        int dx2 = (dx << 1); // point
        int ix = x1 < x2 ? 1 : -1; // increment direction
        int iy = y1 < y2 ? 1 : -1;

        if (dy <= dx) {
            for (;;) {
                //get a new pair of x1, y1
                zvalue.add(nctest.GetAttribute(x1, y1));
                if (x1 >= x2) {
                    break;
                }
                x1 += ix;
                d += dy2;
                if (d > dx) {
                    y1 += iy;
                    d -= dx2;
                }
            }
        } else {
            for (;;) {
                //get a new pair of x1, y1
                zvalue.add(nctest.GetAttribute(x1, y1));
                if (y1 >= y2) {
                    break;
                }
                y1 += iy;
                d += dx2;
                if (d > dy) {
                    x1 += ix;
                    d -= dy2;
                }
            }
        }

        return zvalue;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        VerticalProfiling vp = new VerticalProfiling("test.nc", "3565.xml");
        
    }

}
