/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.du.ogc.ais.function;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;

import edu.du.ogc.gml.GMLPointReader;
import edu.du.ogc.netcdf.PngColor;
import edu.du.ogc.netcdf.PngWriter;
import gov.nasa.worldwind.geom.Position;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.coverage.grid.io.AbstractGridFormat;

import org.geotools.process.vector.HeatmapProcess;

//import org.geotools.gce.geotiff.GeoTiffWriteParams;
//import org.geotools.gce.geotiff.GeoTiffWriter;
//import org.geotools.gce.geotiff.GeoTiffFormat;
//import org.opengis.parameter.GeneralParameterValue;
//import org.opengis.parameter.ParameterValueGroup;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.util.ProgressListener;

/**
 *
 * @author jing.li145
 */
public class TrackDensityGenerator {

    static ArrayList<Position> pathPositions = new ArrayList<Position>();
    double geobounds[]; //left, right, bottom, up

    //read track points
    public TrackDensityGenerator(String trackfilepath) {
        GMLPointReader gmlptreader = new GMLPointReader(trackfilepath);
        pathPositions = gmlptreader.GetPositions(gmlptreader.readGMLData());
        geobounds = gmlptreader.GetBounds();
    }
    
    public double[] getBounds()
    {
        return geobounds; 
    }

    public void CreateDensityMap(String imgpath, String legendpath) {
        int outputWidth = 200;
        int outputHeight = 200;
        int searchRadius = 20;
        ReferencedEnvelope bounds = new ReferencedEnvelope(geobounds[0], geobounds[1], geobounds[2], geobounds[3], DefaultGeographicCRS.WGS84);
        SimpleFeatureCollection fc = createPoints(bounds);

        ProgressListener monitor = null;
        HeatmapProcess process = new HeatmapProcess();
        GridCoverage2D coverage = process.execute(fc, // data
                searchRadius, //radius
                null, // weightAttr
                1, // pixelsPerCell
                bounds, // outputEnv
                outputWidth, // outputWidth
                outputHeight, // outputHeight
                monitor // monitor)
        );
        Raster raster = coverage.getRenderedImage().getData();
        
        float min = 10000000;
        float max = -10000000;
        int[][] rgb = new int[outputWidth][outputHeight];
        for (int i = 0; i < outputWidth; i++) {
            for (int j = 0; j < outputHeight; j++) {
                float cellvalue = raster.getSampleFloat(i, j, 0);
                if (cellvalue > max) {
                    max = cellvalue;
                }
                if (cellvalue < min) {
                    min = cellvalue;
                }

            }
        }

        PngColor pc = new PngColor(min, max, "Density Map");

        File f = new File(legendpath);
        if (!f.exists()) {
            f.mkdirs();
            pc.createLegend(legendpath);
        }
        for (int i = 0; i < outputWidth; i++) {
            for (int j = 0; j < outputHeight; j++) {
                float cellvalue = raster.getSampleFloat(i, j, 0);

                rgb[i][j] = pc.getColorRGB(cellvalue);

            }
        }

        PngWriter png = new PngWriter();
        // put the output location here
        File fo = new File(imgpath);
        if (!fo.exists()) {
            fo.mkdirs();
        }
        png.createImage(rgb, fo);

//        //save the output as a tif file
//        GeoTiffWriteParams wp = new GeoTiffWriteParams();
//        wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
//        wp.setCompressionType("LZW");
//        ParameterValueGroup params = new GeoTiffFormat().getWriteParameters();
//        params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);
//        GeoTiffWriter writer;
//        try {
//            writer = new GeoTiffWriter(new File(fileName));
//            writer.write(coverage, (GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[1]));
//        } catch (IOException ex) {
//            Logger.getLogger(TrackDensityViewer.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private SimpleFeatureCollection createPoints(ReferencedEnvelope bounds) {

        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("data");
        tb.setCRS(bounds.getCoordinateReferenceSystem());
        tb.add("shape", MultiPoint.class);
        tb.add("value", Double.class);

        SimpleFeatureType type = tb.buildFeatureType();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(type);
        DefaultFeatureCollection fc = new DefaultFeatureCollection();

        GeometryFactory factory = new GeometryFactory(new PackedCoordinateSequenceFactory());

        for (Position p : pathPositions) {
            Geometry point = factory.createPoint(new Coordinate(p.getLongitude().degrees, p.getLatitude().degrees, p.elevation));
            fb.add(point);
            fb.add(p.elevation);
            fc.add(fb.buildFeature(null));
        }

        return fc;
    }

//    public static void main(String[] args) {
//        // TODO Auto-generated method stub
//        TrackDensityViewer tdv = new TrackDensityViewer("9_9.xml");
//        tdv.CreateDensityMap("kdvv.png", "kdelegend.png");
//
//    }
}
