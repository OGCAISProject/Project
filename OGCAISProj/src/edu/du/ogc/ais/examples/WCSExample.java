/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package edu.du.ogc.ais.examples;

import edu.du.ogc.netcdf.NetCDFReader2D;
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


public class WCSExample extends ApplicationTemplate
{
    protected static final String[] servers = new String[]
        {
            "http://sdf.ndbc.noaa.gov/thredds/wcs/hfradar_usegc_1km",
        };
    
     protected static final String TEST_PATTERN = "test.png";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {

        public AppFrame()
        {
           super(true, true, false);

            try
            {
                //access nc file 
                
                 NetCDFReader2D nctest = new NetCDFReader2D("test.nc");
        nctest.ReadNetCDF();
        nctest.CreateImage(TEST_PATTERN, "testlegend.png");
        nctest.CloseNetCDF();
        
                SurfaceImage si1 = new SurfaceImage(TEST_PATTERN, new ArrayList<LatLon>(Arrays.asList(
                    LatLon.fromDegrees(nctest.GetBottom(), nctest.GetRight()),
                    LatLon.fromDegrees(nctest.GetBottom(), nctest.GetLeft()),
                    LatLon.fromDegrees(nctest.GetUp(), nctest.GetLeft()),
                    LatLon.fromDegrees(nctest.GetUp(), nctest.GetRight())
                )));
               RenderableLayer layer = new RenderableLayer();
                layer.setName("Surface Images");
                layer.setPickEnabled(false);
                layer.addRenderable(si1);
  
                insertBeforeCompass(this.getWwd(), layer);

                this.getLayerPanel().update(this.getWwd());
            }
             catch (Exception e)
            {
                e.printStackTrace();
            }
                
        }


    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind WCS Layers", AppFrame.class);
    }
}
