package edu.du.ogc.ais.examples;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.jogamp.opengl.util.FPSAnimator;
import javax.media.opengl.GLAnimatorControl;
import com.jogamp.opengl.util.*;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.formats.geojson.GeoJSONDoc;
import gov.nasa.worldwind.formats.geojson.GeoJSONFeature;
import gov.nasa.worldwind.formats.geojson.GeoJSONFeatureCollection;
import gov.nasa.worldwind.formats.geojson.GeoJSONObject;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.util.BasicDragger;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.GeoJSONLoader;
import gov.nasa.worldwindx.examples.Paths.AppFrame;

public class TrajectoryViewer extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame implements RenderingListener
    {
    	
    	protected long lastTime;
    	protected GLAnimatorControl animator;
    	protected Path path;
    	ArrayList<Position> pathPositions = new ArrayList<Position>();
    	ArrayList<Position> pathPositionsRender = new ArrayList<Position>();
    	protected int currentPos = 0; 
    	protected String geojsonfile = "./edu/du/ogc/ais/examples/GeoJSONs/track_geojson.json";
    	
        public AppFrame()
        {
            super(true, true, false);
            //replace this with features from GML files ---Jing Li
            readGeoJSONFile(geojsonfile);
            
            // Add a dragger to enable shape dragging
            this.getWwd().addSelectListener(new BasicDragger(this.getWwd()));

            RenderableLayer layer = new RenderableLayer();

            // Create and set an attribute bundle.
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(new Material(Color.RED));
            attrs.setOutlineWidth(5d);
           

            // Create a path, set some of its properties and set its attributes.
            
            path = new Path(pathPositionsRender);
            path.setAttributes(attrs);
            path.setVisible(true);
//            path.setShowPositions(true);
            path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            path.setPathType(AVKey.GREAT_CIRCLE);
           
            layer.addRenderable(path);


            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);
            getWwd().addRenderingListener(this);

            
            lastTime = System.currentTimeMillis();
            animator = new FPSAnimator((WorldWindowGLCanvas) getWwd(), 10 /*frames per second*/);
            animator.start();

        }
        
        
        public void readGeoJSONFile(Object docSource)
        {
            if (WWUtil.isEmpty(docSource))
            {
                String message = Logging.getMessage("nullValue.SourceIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            GeoJSONDoc doc = null;
            try
            {
                doc = new GeoJSONDoc(docSource);
                doc.parse();

                if (doc.getRootObject() instanceof GeoJSONObject)
                {
                    this.addGeoJSONGeometryToLayer((GeoJSONObject) doc.getRootObject());
                }
                else if (doc.getRootObject() instanceof Object[])
                {
                    for (Object o : (Object[]) doc.getRootObject())
                    {
                        if (o instanceof GeoJSONObject)
                        {
                            this.addGeoJSONGeometryToLayer((GeoJSONObject) o);
                        }
      
                    }
                }
  
            }
            catch (IOException e)
            {
                String message = Logging.getMessage("generic.ExceptionAttemptingToReadGeoJSON", docSource);
                Logging.logger().log(Level.SEVERE, message, e);
                throw new WWRuntimeException(message, e);
            }
            finally
            {
                WWIO.closeStream(doc, docSource.toString());
            }
        }
        
        public void addGeoJSONGeometryToLayer(GeoJSONObject object)
        {
            if (object == null)
            {
                String message = Logging.getMessage("nullValue.ObjectIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (object.isGeometry())
            	pathPositions.add(object.asGeometry().asPoint().getPosition());
            

            else if (object.isFeature())
            	pathPositions.add(object.asFeature().getGeometry().asPoint().getPosition());

            else if (object.isFeatureCollection())
            {
            	GeoJSONFeatureCollection c = object.asFeatureCollection();
                if (c.getFeatures() != null && c.getFeatures().length == 0)
                    return;

                for (GeoJSONFeature feat : c.getFeatures())
                {
                	pathPositions.add(feat.asFeature().getGeometry().asPoint().getPosition());
                }
            }

        }
        
        //show movement
        public void stageChanged(RenderingEvent event)
        {
            if (event.getStage().equals(RenderingEvent.BEFORE_RENDERING))
            {
            	
                // The globe may not be instantiated the first time the listener is called.
                if (getWwd().getView().getGlobe() == null)
                    return;
               
                if (currentPos<pathPositions.size()-1)
                	currentPos = currentPos+1;
                else
                {
	                currentPos = 0;
	                pathPositionsRender.removeAll(pathPositionsRender);
	                double distance = getWwd().getView().getCenterPoint().distanceTo3(getWwd().getView().getEyePoint());
	                getWwd().getView().goTo(pathPositions.get(currentPos), distance);
                }
               
               pathPositionsRender.add(pathPositions.get(currentPos));
               path.setPositions(pathPositionsRender);
               

            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Paths", AppFrame.class);
    }
}
