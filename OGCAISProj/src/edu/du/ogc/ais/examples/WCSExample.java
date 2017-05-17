/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package edu.du.ogc.ais.examples;

import gov.nasa.worldwindx.examples.ApplicationTemplate;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.net.URISyntaxException;


public class WCSExample extends ApplicationTemplate
{
    protected static final String[] servers = new String[]
        {
            "http://sdf.ndbc.noaa.gov/thredds/wcs/hfradar_usegc_1km",
        };

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected final Dimension wcsPanelSize = new Dimension(400, 600);
        protected JTabbedPane tabbedPane;
        protected int previousTabIndex;

        public AppFrame()
        {
          
        }


    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind WCS Layers", AppFrame.class);
    }
}
