/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package edu.du.ogc.ais.function;

import edu.du.ogc.netcdf.NetCDFReader2D;
import edu.du.ogc.wcs.WCSService;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import static gov.nasa.worldwindx.examples.ApplicationTemplate.insertBeforeCompass;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

public class WCSExample extends ApplicationTemplate {

    protected static final String[] servers = new String[]{
        "http://sdf.ndbc.noaa.gov/thredds/wcs/hfradar_usegc_1km"};

    ArrayList<String> variables = new ArrayList<String>();

    protected static final ArrayList<String> images = new ArrayList<String>();

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        public AppFrame() {
            super(true, true, false);

            try {
                //access wcs file 
                WCSService wcsservice = new WCSService(servers[0]);
                wcsservice.parseCapablities();
                String varname = wcsservice.GetCoverageVariable().get(0);
                wcsservice.BuildCoverageURL(varname,  "2017-06-11T00:00:00Z", "-60,21,-57,47");
                String path = wcsservice.downloadNetCDF();
                System.out.println(path);

                //add as a surface image
                NetCDFReader2D nctest = new NetCDFReader2D(path);
                nctest.ReadNetCDF();
                nctest.CreateImage(path.split(".nc")[0] + ".png", path.split(".nc")[0] + "legend.png");
                nctest.CloseNetCDF();

                SurfaceImage si1 = new SurfaceImage(path.split(".nc")[0] + ".png", new ArrayList<LatLon>(Arrays.asList(
                        LatLon.fromDegrees(nctest.GetBottom(), nctest.GetRight()),
                        LatLon.fromDegrees(nctest.GetBottom(), nctest.GetLeft()),
                        LatLon.fromDegrees(nctest.GetUp(), nctest.GetLeft()),
                        LatLon.fromDegrees(nctest.GetUp(), nctest.GetRight())
                )));
                RenderableLayer layer = new RenderableLayer();
                layer.setName(nctest.GetVariableName());
                layer.setPickEnabled(false);
                layer.addRenderable(si1);

                insertBeforeCompass(this.getWwd(), layer);

                this.getLayerPanel().update(this.getWwd());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public static void main(String[] args) {
        ApplicationTemplate.start("World Wind WCS Layers", AppFrame.class);
    }
}
