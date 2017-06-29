package edu.du.ogc.ais.function;


import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.kml.KMLStyle;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
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
import si.xlab.gaea.core.layers.wfs.WFSService;
import si.xlab.gaea.core.ogc.kml.KMLStyleFactory;

/**
 * 
 * @author marjan
 */
public class WFSExample extends ApplicationTemplate
{

    
    protected static void makeMenu(final AppFrame appFrame)
    {
        JMenuBar menuBar = new JMenuBar();
        appFrame.setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem openWfsItem = new JMenuItem(new AbstractAction("Add WFS layer...")
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                JDialog dialog = new JDialog(appFrame, "Import WFS layer", true);
                
                WFSLayerPanel wfsPanel = new WFSLayerPanel();
                wfsPanel.setDialog(dialog);
                Dimension dimension = wfsPanel.getPreferredSize();
                dimension.setSize(dimension.getWidth()+10, dimension.getHeight()+25);
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

                    try
                    {
                        addWfsLayer(url, name, sector, tile, queryfield, queryvalue, dist*1000, color, lineLabelTag);
                    }
                    catch (Exception e)
                    {
                        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
                    }
                }

                dialog.dispose();
            }
        });

        fileMenu.add(openWfsItem);
        
        JMenuItem quitItem = new JMenuItem(new AbstractAction("Quit")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });
        fileMenu.add(quitItem);
        

        
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        String licenseMsg = "This application, together with the NASA World Wind Java SDK and the modifications to the SDK done by XLAB d.o.o.,"
                + "are distributed under the terms of NASA Open Source Agreement license.\n"
                + "You should have received this license together with this application. If not, please contact info@geaplus.si or visit http://www.gaeaplus.eu.\n\n"
                + "The data layers included in this application are either licensed for use in World Wind, owned by XLAB d.o.o., or available for free from servers intended for public use.";
        helpMenu.add(new MessageItem(licenseMsg, "Terms of use"));
        String aboutMsg = "This is a demonstration of features that Gaea+ Open Source adds to NASA World Wind Java SDK.\n"
                + "For more information, visit http://www.gaeaplus.eu/en/, https://github.com/gaeaplus/gaeaplus, and http://worldwind.arc.nasa.gov/java/.";
        helpMenu.add(new MessageItem(aboutMsg, "About..."));
    }    
    

    private static class MessageItem extends JMenuItem
    {
        private final String message;
        
        public MessageItem(String message, String caption)
        {
            this.message = message;
            setAction(new AbstractAction(caption) {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    showMessage();
                }
            });
        }
        
        public void showMessage()
        {
            JOptionPane.showMessageDialog(null, message);            
        }
    }
    

    
    protected static void addWfsLayer(String url, String featureTypeName, Sector sector, Angle tileDelta, String queryField, String queryValue, double maxVisibleDistance, Color color,
			String lineLabelTag)
    {
        
      
        WFSService service = new WFSService(url, featureTypeName, sector, tileDelta);
        WFSGenericLayer layer = new WFSGenericLayer(service, "WFS: "+ featureTypeName + " (from "
                + url.replaceAll("^.+://", "").replaceAll("/.*$", "") + ")");
        layer.setMaxActiveAltitude(maxVisibleDistance);
		if(lineLabelTag != null && !lineLabelTag.isEmpty()){
			layer.setLineLabelTag(lineLabelTag);
		}

        KMLStyle style = layer.getDefaultStyle();
        style.getLineStyle().setField("color", KMLStyleFactory.encodeColorToHex(color));
        style.getPolyStyle().setField("color", KMLStyleFactory.encodeColorToHex(color).replaceFirst("^ff", "80")); //semi-transparent fill
        layer.setDefaultStyle(style);
        insertBeforePlacenames(appFrame.getWwd(), layer);       
        layer.setEnabled(true);
        appFrame.updateLayerPanel();
        appFrame.getWwd().getView().goTo(Position.fromDegrees(sector.getCentroid().latitude.degrees, sector.getCentroid().longitude.degrees), maxVisibleDistance/2);
        
        }
    
    public static class GaeaAppFrame extends AppFrame
    {
        protected void updateLayerPanel()
        {
            //remove RTT layer, update layer panel, re-insert RTT; otherwise it will appear in the layer list
            int rttIndex = getWwd().getModel().getLayers().indexOf(RenderToTextureLayer.getInstance());
            if (rttIndex != -1)
                getWwd().getModel().getLayers().remove(rttIndex);
            this.layerPanel.update(getWwd());
            getWwd().getModel().getLayers().add(rttIndex, RenderToTextureLayer.getInstance());
        }        
    }
    
    private static GaeaAppFrame appFrame = null;
    
    public static void main(String[] args)
    {
		//MeasureRenderTime.enable(true);
		//MeasureRenderTime.setMesureGpu(true);
		
        Configuration.insertConfigurationDocument("si/xlab/gaea/examples/gaea-example-config.xml");
        appFrame = (GaeaAppFrame)ApplicationTemplate.start("OGC AIS Viewer", GaeaAppFrame.class);
        insertBeforeCompass(appFrame.getWwd(), RenderToTextureLayer.getInstance());
        appFrame.getWwd().addSelectListener(new FeatureSelectListener(appFrame.getWwd()));
        makeMenu(appFrame);
    }
}
