package edu.du.ogc.ais.function;

import com.jogamp.opengl.util.FPSAnimator;
import javax.media.opengl.GLAnimatorControl;
import com.jogamp.opengl.util.*;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.SelectableIconLayer;
import gov.nasa.worldwind.ogc.kml.KMLStyle;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.util.BasicDragger;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import static gov.nasa.worldwindx.examples.ApplicationTemplate.insertBeforeCompass;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GLAnimatorControl;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import si.xlab.gaea.avlist.AvKeyExt;
import si.xlab.gaea.core.event.FeatureSelectListener;
import si.xlab.gaea.core.layers.RenderToTextureLayer;
import si.xlab.gaea.core.layers.elev.ElevationLayer;
import si.xlab.gaea.core.layers.elev.SlopeLayer;
import si.xlab.gaea.core.layers.wfs.WFSGenericLayer;
import static si.xlab.gaea.core.layers.wfs.WFSGenericLayer.findFirstLinkedImage;
import si.xlab.gaea.core.layers.wfs.WFSService;
import si.xlab.gaea.core.layers.wfs.WFSServiceSimple;
import si.xlab.gaea.core.ogc.gml.GMLFeature;
import si.xlab.gaea.core.ogc.gml.GMLGeometry;
import si.xlab.gaea.core.ogc.gml.GMLPoint;
import si.xlab.gaea.core.ogc.kml.KMLParserException;
import si.xlab.gaea.core.ogc.kml.KMLStyleFactory;
import si.xlab.gaea.core.render.DefaultLook;
import si.xlab.gaea.core.render.SelectableIcon;

/**
 *
 * @author marjan
 */
public class WFSExampleSimple extends ApplicationTemplate {

    static SelectableIconLayer iconlayer = new SelectableIconLayer();
    static IconLayer iconsimplelayer = new IconLayer();
    static LatLon finalloc;
    static RenderableLayer tracklayer = new RenderableLayer();

    static protected long lastTime;
    static protected GLAnimatorControl animator;
    static protected Path trackpath;
    static ArrayList<Position> pathPositions = new ArrayList<Position>();
    static ArrayList<Position> pathPositionsAnimation = new ArrayList<Position>();
    static protected int currentPos = 0;
    

    protected static void makeMenu(final TrackAppFrame appFrame) {
        JMenuBar menuBar = new JMenuBar();
        appFrame.setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem openWfsItem = new JMenuItem(new AbstractAction("Add WFS layer...") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JDialog dialog = new JDialog(appFrame, "Import WFS layer", true);
                WFSLayerPanel wfsPanel = new WFSLayerPanel();
                wfsPanel.setDialog(dialog);
                Dimension dimension = wfsPanel.getPreferredSize();
                dimension.setSize(dimension.getWidth() + 10, dimension.getHeight() + 25);
                dialog.getContentPane().add(wfsPanel);
                dialog.setSize(dimension);
                dialog.setModal(true);
                dialog.setVisible(true);

                if (wfsPanel.isConfirmed()) {
                    String url = wfsPanel.getUrl();
                    String name = wfsPanel.getFeatureName();
                    Sector sector = wfsPanel.getSector();
                    double dist = wfsPanel.getVisibleDistance();
                    Angle tile = wfsPanel.getTileDelta();
                    Color color = wfsPanel.getColor();
                    String lineLabelTag = wfsPanel.getFeatureLableTypeName();
                    String queryfield = wfsPanel.getQueryField();
                    String queryvalue = wfsPanel.getQueryValue();
                    boolean showTracks = wfsPanel.showTracks();

                    try {
                        addWfsLayer(url, name, sector, tile, queryfield, queryvalue, dist * 1000, color, lineLabelTag, showTracks, appFrame);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "WFS Panel Error: " + e.getMessage());
                    }
                }

                dialog.dispose();
            }
        });

        fileMenu.add(openWfsItem);

        JMenuItem quitItem = new JMenuItem(new AbstractAction("Quit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(quitItem);

    }

    private static class MessageItem extends JMenuItem {

        private final String message;

        public MessageItem(String message, String caption) {
            this.message = message;
            setAction(new AbstractAction(caption) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showMessage();
                }
            });
        }

        public void showMessage() {
            JOptionPane.showMessageDialog(null, message);
        }
    }

    private static void GetIcons(List<GMLFeature> gmlfeatures, KMLStyle style) {

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
                
                finalloc = loc;
            }
        }
    }

    private static void GetPositions(List<GMLFeature> gmlfeatures) {

        for (GMLFeature gmlfeature : gmlfeatures) {
            GMLGeometry geometry;
            geometry = gmlfeature.getDefaultGeometry();
            if (geometry instanceof GMLPoint) {
                GMLPoint gmlpoint = (GMLPoint) geometry;
                LatLon loc = gmlpoint.getCentroid();
                String desc = gmlfeature.buildDescription(null);
                String imageURL = findFirstLinkedImage(desc);
                pathPositions.add(new Position(geometry.getCentroid(), 100));
                finalloc = loc;
            }
        }
    }
    
      public static int readGMLCount(String path) {
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String sCurrentLine = br.readLine(); 
            while (sCurrentLine!=null)
            {
                String[] parts = sCurrentLine.split(" ");
                for(String part: parts)
                {
                    if (part.contains("numberOfFeatures="))
                    {
                        
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

    protected static void addWfsLayer(String url, String featureTypeName, Sector sector,
            Angle tileDelta, String queryField, String queryValue, double maxVisibleDistance, Color color,
            String lineLabelTag, boolean showTracks, TrackAppFrame appFrame) {

        try {
            WFSServiceSimple service = new WFSServiceSimple(url, featureTypeName, queryField, queryValue, "");
            String filepath = service.downloadFeatures();
//            int cout = readGMLCount(filepath);
            KMLStyle style = new KMLStyle(DefaultLook.DEFAULT_FEATURE_STYLE);
            style.getLineStyle().setField("color", KMLStyleFactory.encodeColorToHex(color));
            style.getPolyStyle().setField("color", KMLStyleFactory.encodeColorToHex(color).replaceFirst("^ff", "80")); //semi-transparent fill
            if (!showTracks) {
                iconlayer.setMaxActiveAltitude(maxVisibleDistance);
                GetIcons(service.readGMLData(filepath), style);
                insertBeforePlacenames(appFrame.getWwd(), iconlayer);
                iconlayer.setEnabled(true);
                iconlayer.setName(featureTypeName);
                appFrame.updateLayerPanel();
            } else {
                // Add a dragger to enable shape dragging
                
                
                 iconsimplelayer.setMaxActiveAltitude(maxVisibleDistance);
//                GetIcons(service.readGMLData(filepath), style);
                insertBeforePlacenames(appFrame.getWwd(), iconsimplelayer);
                iconsimplelayer.setEnabled(true);
                iconsimplelayer.setName(featureTypeName);
                        
                GetPositions(service.readGMLData(filepath));
                appFrame.getWwd().addSelectListener(new BasicDragger(appFrame.getWwd()));

                // Create and set an attribute bundle.
                ShapeAttributes attrs = new BasicShapeAttributes();
                attrs.setOutlineMaterial(new Material(Color.RED));
                attrs.setOutlineWidth(2d);

                // Create a path, set some of its properties and set its attributes.
                trackpath = new Path(pathPositionsAnimation);
                trackpath.setAttributes(attrs);
                trackpath.setVisible(true);
//            path.setShowPositions(true);
                trackpath.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                trackpath.setPathType(AVKey.GREAT_CIRCLE);
                
                tracklayer.addRenderable(trackpath);
                
                
                // Add the layer to the model.
                insertBeforeCompass(appFrame.getWwd(), tracklayer);
                appFrame.getWwd().addRenderingListener(appFrame);

                appFrame.updateLayerPanel();
                lastTime = System.currentTimeMillis();
                animator = new FPSAnimator((WorldWindowGLCanvas) appFrame.getWwd(), 10/*frames per second*/);
                animator.start();

            }

            appFrame.getWwd().getView().goTo(Position.fromDegrees(finalloc.latitude.degrees, finalloc.longitude.degrees), maxVisibleDistance / 2);
        } catch (IOException ex) {
            Logger.getLogger(WFSExampleSimple.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static class TrackAppFrame extends ApplicationTemplate.AppFrame implements RenderingListener {

        protected void updateLayerPanel() {
            //remove RTT layer, update layer panel, re-insert RTT; otherwise it will appear in the layer list
            int rttIndex = getWwd().getModel().getLayers().indexOf(RenderToTextureLayer.getInstance());
            if (rttIndex != -1) {
                getWwd().getModel().getLayers().remove(rttIndex);
            }
            this.layerPanel.update(getWwd());
            getWwd().getModel().getLayers().add(rttIndex, RenderToTextureLayer.getInstance());
        }

        //show movement
        public void stageChanged(RenderingEvent event) {
            if (event.getStage().equals(RenderingEvent.BEFORE_RENDERING)) {

                // The globe may not be instantiated the first time the listener is called.
                if (getWwd().getView().getGlobe() == null) {
                    return;
                }

                if (currentPos < pathPositions.size() - 1) {
                    currentPos = currentPos + 1;
                } else {
                    currentPos = 0;
                    pathPositionsAnimation.removeAll(pathPositionsAnimation);
                    double distance = getWwd().getView().getCenterPoint().distanceTo3(getWwd().getView().getEyePoint());
                    getWwd().getView().goTo(pathPositions.get(currentPos), distance);
                }
                
                iconsimplelayer.removeAllIcons();
                
                UserFacingIcon icon = new UserFacingIcon("src/images/pushpins/simple32.png",pathPositions.get(currentPos));
            icon.setSize(new Dimension(32, 32));
            iconsimplelayer.addIcon(icon);
            
                pathPositionsAnimation.add(pathPositions.get(currentPos));
                trackpath.setPositions(pathPositionsAnimation);
//                System.err.println(pathPositionsAnimation.size());
            }
        }
    }

    private static TrackAppFrame appFrame = null;

    public static void main(String[] args) {
        //MeasureRenderTime.enable(true);
        //MeasureRenderTime.setMesureGpu(true);

        Configuration.insertConfigurationDocument("si/xlab/gaea/examples/gaea-example-config.xml");
        appFrame = (TrackAppFrame) ApplicationTemplate.start("OGC AIS Viewer", TrackAppFrame.class);
        insertBeforeCompass(appFrame.getWwd(), RenderToTextureLayer.getInstance());
        appFrame.getWwd().addSelectListener(new FeatureSelectListener(appFrame.getWwd()));
        makeMenu(appFrame);
    }
}
