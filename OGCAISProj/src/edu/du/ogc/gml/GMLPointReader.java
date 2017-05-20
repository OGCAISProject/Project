/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.du.ogc.gml;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import si.xlab.gaea.core.layers.wfs.AbstractWFSLayer;
import si.xlab.gaea.core.ogc.gml.GMLFeature;
import si.xlab.gaea.core.ogc.gml.GMLGeometry;
import si.xlab.gaea.core.ogc.gml.GMLParser;
import si.xlab.gaea.core.ogc.gml.GMLPoint;

/**
 *
 * @author jing.li145
 */
public class GMLPointReader {

    static ArrayList<Position> pathPositions = new ArrayList<Position>();
    String path;
    double left = 180, right = -180, up = -90, bottom = 90;

    public GMLPointReader(String path) {
        path = path;

    }

    public ArrayList<Position> GetPositions(List<GMLFeature> gmlfeatures) {

        for (GMLFeature gmlfeature : gmlfeatures) {
            GMLGeometry geometry;
            geometry = gmlfeature.getDefaultGeometry();
            if (geometry instanceof GMLPoint) {
                GMLPoint gmlpoint = (GMLPoint) geometry;
                LatLon loc = gmlpoint.getCentroid();
                String desc = gmlfeature.buildDescription(null);
                LatLon pt = geometry.getCentroid();
                if (pt.longitude.degrees >right)
                        {
                            right = pt.longitude.degrees;
                        }
                                if (pt.longitude.degrees <left)
                        {
                            left = pt.longitude.degrees;
                        }
                                
                                                if (pt.latitude.degrees >up)
                        {
                            up = pt.latitude.degrees;
                        }
                                                
                                                                if (pt.latitude.degrees <bottom)
                        {
                            bottom = pt.latitude.degrees;
                        }
                
                        pathPositions.add(new Position(pt, 0));

            }
        }
        return pathPositions;
    }

    public double[] GetBounds() {
        double bounds[] = new double[4];
        bounds[0]= left; 
        bounds[1]=right; 
        bounds[2]=bottom;
        bounds[3] = up;
        return bounds; 
    }

    public List<GMLFeature> readGMLData() {
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

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        GMLPointReader gp = new GMLPointReader("3565.xml");

    }
}
