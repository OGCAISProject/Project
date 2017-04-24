/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package edu.du.ogc.ais.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.formats.geojson.GeoJSONPoint;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.view.orbit.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.GeoJSONLoader;

import javax.media.opengl.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.DoubleBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.concurrent.ThreadLocalRandom;

/**

 */
public class TrackViewer extends ApplicationTemplate
{
    // See the USGS GeoJSON feed documentation for information on this earthquake data feed:
    // https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php
    protected static final String GEO_JSON_FILE
        = "D:\\OneDrive\\OGCWorkspace\\OGCAIS\\src\\edu\\du\\ogc\\ais\\GeoJSONs\\random.json";
    //attribute names
    protected static final String USGS_EARTHQUAKE_MAGNITUDE = "mag";
    protected static final String USGS_EARTHQUAKE_PLACE = "place";
    protected static final String USGS_EARTHQUAKE_TIME = "time";

    @SuppressWarnings("unchecked")
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private RenderableLayer eqLayer;
        private EqAnnotation mouseEq, latestEq;
        private GlobeAnnotation tooltipAnnotation;
        private JButton downloadButton;
        private JLabel statusLabel, latestLabel;
        private long updateTime;
        //for animation
        private Timer updater;

        public AppFrame()
        {
            super(true, true, false);

            // Init tooltip annotation
            this.tooltipAnnotation = new GlobeAnnotation("", Position.fromDegrees(0, 0, 0));
            Font font = Font.decode("Arial-Plain-16");
            this.tooltipAnnotation.getAttributes().setFont(font);
            this.tooltipAnnotation.getAttributes().setSize(new Dimension(400, 0));
            this.tooltipAnnotation.getAttributes().setDistanceMinScale(1);
            this.tooltipAnnotation.getAttributes().setDistanceMaxScale(1);
            this.tooltipAnnotation.getAttributes().setVisible(false);
            this.tooltipAnnotation.setPickEnabled(false);
            this.tooltipAnnotation.setAlwaysOnTop(true);

            // Add control panels
            JPanel controls = new JPanel();
            controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
            // Add earthquakes view control panel

            // Add select listener for earthquake picking
            this.getWwd().addSelectListener(new SelectListener()
            {
                public void selected(SelectEvent event)
                {
                    if (event.getEventAction().equals(SelectEvent.ROLLOVER))
                        highlight(event.getTopObject());
                }
            });
            
            
            // Add updater timer
            startDataDownload();
 
        }

        private void highlight(Object o)
        {
            if (this.mouseEq == o)
                return; // same thing selected

            if (this.mouseEq != null)
            {
                this.mouseEq.getAttributes().setHighlighted(false);
                this.mouseEq = null;
                this.tooltipAnnotation.getAttributes().setVisible(false);
            }

            if (o != null && o instanceof EqAnnotation)
            {
                this.mouseEq = (EqAnnotation) o;
                this.mouseEq.getAttributes().setHighlighted(true);
                this.tooltipAnnotation.setText(this.composeAnnotationText(this.mouseEq));
                this.tooltipAnnotation.setPosition(this.mouseEq.getPosition());
                this.tooltipAnnotation.getAttributes().setVisible(true);
                this.getWwd().redraw();
            }
        }

       

        private String composeAnnotationText(EqAnnotation eqAnnotation)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");

            Number magnitude = (Number) eqAnnotation.getValue(USGS_EARTHQUAKE_MAGNITUDE);
            String place = (String) eqAnnotation.getValue(USGS_EARTHQUAKE_PLACE);
            if (magnitude != null || !WWUtil.isEmpty(place))
            {
                sb.append("<b>");

                if (magnitude != null)
                    sb.append("M ").append(magnitude).append(" - ");

                if (place != null)
                    sb.append(place);

                sb.append("</b>");
                sb.append("<br/>");
            }

            Number time = (Number) eqAnnotation.getValue(USGS_EARTHQUAKE_TIME);
           

            sb.append(String.format("%.2f", eqAnnotation.getPosition().elevation)).append(" km deep");

            sb.append("</html>");

            return sb.toString();
        }


    

        // Earthquake layer ------------------------------------------------------------------

        private void startDataDownload()
        {
            WorldWind.getScheduledTaskService().addTask(new Runnable()
            {
                public void run()
                {
                    downloadGeoJSON(GEO_JSON_FILE);
                }
            });
        }

        private void downloadGeoJSON(String geojsonfile)
        {
            // Disable download button and update status label
            if (this.downloadButton != null)
                this.downloadButton.setEnabled(false);
            if (this.statusLabel != null)
                this.statusLabel.setText("Updating GeoJSON...");

            RenderableLayer newLayer = (RenderableLayer) buildGeoJSONLayer(geojsonfile);
            if (newLayer.getNumRenderables() > 0)
            {
                LayerList layers = this.getWwd().getModel().getLayers();
                if (this.eqLayer != null)
                    layers.remove(this.eqLayer);
                this.eqLayer = newLayer;
                this.eqLayer.addRenderable(this.tooltipAnnotation);
                insertBeforePlacenames(this.getWwd(), this.eqLayer);
               

                if (this.statusLabel != null)
                    this.statusLabel.setText("Updated " + new SimpleDateFormat("EEE h:mm aa").format(new Date())); // now
            }
            else
            {
                if (this.statusLabel != null)
                    this.statusLabel.setText("No earthquakes");
            }

            if (this.downloadButton != null)
                this.downloadButton.setEnabled(true);
        }

        private Layer buildGeoJSONLayer(String geojsonfile)
        {
            GeoJSONLoader loader = new GeoJSONLoader()
            {
                @Override
                protected void addRenderableForPoint(GeoJSONPoint geom, RenderableLayer layer, AVList properties)
                {
                    try
                    {
                        addGeometry(geom, layer, properties);
                    }
                    catch (Exception e)
                    {
                        Logging.logger().log(Level.WARNING, "Exception adding earthquake", e);
                    }
                }
            };

            RenderableLayer layer = new RenderableLayer();
            layer.setName("AIS Track");
            loader.addSourceGeometryToLayer(geojsonfile, layer);

            return layer;
        }

        private AnnotationAttributes eqAttributes;
        private Color eqColors[] =
            {
                Color.RED,
                Color.ORANGE,
                Color.YELLOW,
                Color.GREEN,
                Color.BLUE,
                Color.GRAY,
                Color.BLACK,
            };

        private void addGeometry(GeoJSONPoint geom, RenderableLayer layer, AVList properties)
        {
            if (eqAttributes == null)
            {
                // Init default attributes for all eq
                eqAttributes = new AnnotationAttributes();
                eqAttributes.setLeader(AVKey.SHAPE_NONE);
                eqAttributes.setDrawOffset(new Point(0, -16));
                eqAttributes.setSize(new Dimension(128, 128));
                eqAttributes.setBorderWidth(0);
                eqAttributes.setCornerRadius(0);
                eqAttributes.setBackgroundColor(new Color(0, 0, 0, 0));
            }

            EqAnnotation eq = new EqAnnotation(geom.getPosition(), eqAttributes);
            eq.setAltitudeMode(WorldWind.CLAMP_TO_GROUND); // GeoJON point's 3rd coordinate indicates depth
            eq.setValues(properties);

            //show color and size based on attributes
            int randomNum = ThreadLocalRandom.current().nextInt(0, 7 );
            eq.getAttributes().setTextColor(eqColors[randomNum]);
            eq.getAttributes().setScale(1.0/ 10);
            
            layer.addRenderable(eq);
        }

        private class EqAnnotation extends GlobeAnnotation
        {
            public EqAnnotation(Position position, AnnotationAttributes defaults)
            {
                super("", position, defaults);
            }

            protected void applyScreenTransform(DrawContext dc, int x, int y, int width, int height, double scale)
            {
                double finalScale = scale * this.computeScale(dc);

                GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
                gl.glTranslated(x, y, 0);
                gl.glScaled(finalScale, finalScale, 1);
            }

            // Override annotation drawing for a simple circle
            private DoubleBuffer shapeBuffer;

            protected void doDraw(DrawContext dc, int width, int height, double opacity, Position pickPosition)
            {
                // Draw colored circle around screen point - use annotation's text color
                if (dc.isPickingMode())
                {
                    this.bindPickableObject(dc, pickPosition);
                }

                this.applyColor(dc, this.getAttributes().getTextColor(), 0.6 * opacity, true);

                // Draw 32x32 shape from its bottom left corner
                int size = 64;
                if (this.shapeBuffer == null)
                    this.shapeBuffer = FrameFactory.createShapeBuffer(AVKey.SHAPE_ELLIPSE, size, size, 0, null);
                GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
                gl.glTranslated(-size / 2, -size / 2, 0);
                FrameFactory.drawBuffer(dc, GL.GL_TRIANGLE_FAN, this.shapeBuffer);
            }
        }


    } // End AppFrame

    // --- Main -------------------------------------------------------------------------
    public static void main(String[] args)
    {
        // Adjust configuration values before instantiation
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 0);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, 0);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 50e6);
//        Configuration.setValue(AVKey.GLOBE_CLASS_NAME, EarthFlat.class.getName());
        ApplicationTemplate.start("AIS Tracker", AppFrame.class);
    }
}
