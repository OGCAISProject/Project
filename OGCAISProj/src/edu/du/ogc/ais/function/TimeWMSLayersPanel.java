/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
 */
package edu.du.ogc.ais.function;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.ogc.wms.*;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.wms.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;


import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.util.List;

/**
 * @author tag
 * @version $Id: WMSLayersPanel.java 3601 2007-11-21 03:23:23Z tgaskins $
 */
public class TimeWMSLayersPanel extends JPanel {
	protected static class LayerInfo {
		protected WMSCapabilities caps;
		protected AVListImpl params = new AVListImpl();
	

		protected String getTitle() {
			return params.getStringValue(AVKey.DISPLAY_NAME);
		}
		
		
        protected String getName()
        {
            return params.getStringValue(AVKey.LAYER_NAMES);
        }

		 protected String getAbstract()
	        {
	            return params.getStringValue(AVKey.LAYER_ABSTRACT);
	        }

	}

	protected JComboBox jcBeginning = new JComboBox();
	protected JComboBox jcEnd = new JComboBox();
	protected LayerInfo jcSelectLayer = new LayerInfo();
	protected JToggleButton jStart;
	protected JSlider picSpeed = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
	javax.swing.Timer timerwms;
	protected ArrayList<BasicTiledImageLayer> layersByTime = new ArrayList<BasicTiledImageLayer>();
	
	protected TimeParser timescale; 
	protected String t[];
	
	
	protected final WorldWindow wwd;
	protected final URI serverURI;
	protected final Dimension size;
	protected final Thread loadingThread;
	

	protected final TreeSet<LayerInfo> layerInfos = new TreeSet<LayerInfo>(new Comparator<LayerInfo>() {
				public int compare(LayerInfo infoA, LayerInfo infoB) {
					String nameA = infoA.getName();
					String nameB = infoB.getName();
					return nameA.compareTo(nameB);
				}
			});

	public TimeWMSLayersPanel(WorldWindow wwd, String server, Dimension size) throws URISyntaxException {
		super(new BorderLayout());

		// See if the server name is a valid URI. Throw an exception if not.
		this.serverURI = new URI(server.trim()); // throws an exception if
		// server name is not a
		// valid uri.
		
		this.wwd = wwd;
		this.size = size;
		this.setPreferredSize(this.size);

		this.makeProgressPanel();

		// Thread off a retrieval of the server's capabilities document and
		// update of this panel.
		this.loadingThread = new Thread(new Runnable() {
			public void run() {
				load();
			}
		});
		this.loadingThread.setPriority(Thread.MIN_PRIORITY);
		this.loadingThread.start();
	}

    protected void load()
    {
        WMSCapabilities caps;

        try
        {
            caps = WMSCapabilities.retrieve(this.serverURI);
            
            caps.parse();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }

        // Gather up all the named layers and make a world wind layer for each.
        final List<WMSLayerCapabilities> namedLayerCaps = caps.getNamedLayers();
        if (namedLayerCaps == null)
            return;

        try
        {
            for (WMSLayerCapabilities lc : namedLayerCaps)
            {
                Set<WMSLayerStyle> styles = lc.getStyles();
                if (styles == null || styles.size() == 0)
                {
                    LayerInfo layerInfo = createLayerInfo(caps, lc, null);
                    TimeWMSLayersPanel.this.layerInfos.add(layerInfo);
                }
                else
                {
                    for (WMSLayerStyle style : styles)
                    {
                        LayerInfo layerInfo = createLayerInfo(caps, lc, style);
                        TimeWMSLayersPanel.this.layerInfos.add(layerInfo);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }

        // Fill the panel with the layer titles.
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                TimeWMSLayersPanel.this.removeAll();
                makeLayerInfosPanel(layerInfos);
            }
        });
    }

	public String getServerDisplayString() {
		return this.serverURI.getHost();
	}

	protected LayerInfo createLayerInfo(WMSCapabilities caps, WMSLayerCapabilities layerCaps, WMSLayerStyle style) {
		// Create the layer info specified by the layer's capabilities entry and
		// the selected style.

        // Create the layer info specified by the layer's capabilities entry and the selected style.

        LayerInfo linfo = new LayerInfo();
        linfo.caps = caps;
        linfo.params = new AVListImpl();
        linfo.params.setValue(AVKey.LAYER_NAMES, layerCaps.getName());
        if (style != null)
            linfo.params.setValue(AVKey.STYLE_NAMES, style.getName());
        String abs = layerCaps.getLayerAbstract();
        if (!WWUtil.isEmpty(abs))
            linfo.params.setValue(AVKey.LAYER_ABSTRACT, abs);

        linfo.params.setValue(AVKey.DISPLAY_NAME, makeTitle(caps, linfo));
        //if time is not avaliable?
        linfo.params.setValue(AVKey.WMS_TIME_DIMENSION, makeTime(caps, linfo));
        
        return linfo;
	}

	protected void makeLayerInfosPanel(Collection<LayerInfo> layerInfos) {
		// Create the panel holding the layer names.
		JPanel layersPanel = new JPanel(new GridLayout(0, 1, 0, 15));
		layersPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// add time control panel
		
		
		JPanel timeControl = new JPanel(new GridLayout(0, 2, 5, 5));
		timeControl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		timeControl.add(new JLabel("BeginTime"));
		timeControl.add(jcBeginning);
		timeControl.add(new JLabel("EndTime"));
		timeControl.add(jcEnd);
		LayerInfoStartAction actionS = new LayerInfoStartAction(jcSelectLayer, TimeWMSLayersPanel.this.wwd);
		jStart = new JToggleButton(actionS);
		jStart.setText("Start");
		timeControl.add(jStart);
		// Create the slider
		picSpeed.setValue(50);
		picSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				timerwms.setDelay(picSpeed.getValue() * 10);
				if(jStart.isSelected())
					timerwms.start();
			}
		});
		picSpeed.setMajorTickSpacing(10);
		picSpeed.setPaintTicks(true);
		picSpeed.setPaintLabels(false);
		timeControl.add(picSpeed);

		// Add the server's layers to the panel.
		for (LayerInfo layerInfo : layerInfos) {
			addLayerInfoPanel(layersPanel, TimeWMSLayersPanel.this.wwd, layerInfo);
		}

		// Put the name panel in a scroll bar.
		JScrollPane scrollPane = new JScrollPane(layersPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		scrollPane.setPreferredSize(size);

		// Add the scroll bar and name panel to a titled panel that will resize
		// with the main window.
		JPanel westPanel = new JPanel(new BorderLayout());

		westPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(
				9, 9, 9, 9), new TitledBorder("Layers")));

		westPanel.add(timeControl, BorderLayout.NORTH);
		westPanel.add(scrollPane, BorderLayout.CENTER);
		this.add(westPanel, BorderLayout.CENTER);
		this.revalidate();
	}

	protected void addLayerInfoPanel(JPanel layersPanel, WorldWindow wwd, LayerInfo linfo) {
		// Give a layer a button and label and add it to the layer names panel.
		LayerInfoAction action = new LayerInfoAction(linfo, wwd);
		
		if (linfo.getAbstract() != null)
            action.putValue(Action.SHORT_DESCRIPTION, linfo.getAbstract());
        JCheckBox jcb = new JCheckBox(action);
		jcb.setSelected(false);
		layersPanel.add(jcb);

	}

	
	//add by jing li
	private class drawLayerByTime extends AbstractAction {
		int j = 0;
		int tBegin;
		int tEnd;
		private BasicTiledImageLayer layer;

		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			tBegin = jcBeginning.getSelectedIndex();
			tEnd = jcEnd.getSelectedIndex();
			if(layer!=null){
				wwd.getModel().getLayers().remove(layer);
				layer = null;
				}
			if (j < (tEnd-tBegin)+1) {
				jcSelectLayer.params.setValue(AVKey.DATA_CACHE_NAME, null);
				layer =new WMSTiledImageLayer(jcSelectLayer.caps, jcSelectLayer.params, t[j+tBegin]);
				layer.setEnabled(false);
				layer.setEnabled(true);
				wwd.getModel().getLayers().setTime(jcBeginning.getItemAt(tBegin+j).toString());
				if(j>0)
				{
					layersByTime.get(j-1).setEnabled(false);
				}
				layersByTime.get(j).setEnabled(true);
				wwd.getModel().getLayers().add(layersByTime.get(j));
				j++;
				wwd.redraw();
			} else {
				// If the layer is selected, add it to the world window's
				// current model, else remove it from the model.
				while(j>0)
				{
					j--;
					wwd.getModel().getLayers().remove(layersByTime.get(j));
					
				}
				jcSelectLayer.params.setValue(AVKey.DATA_CACHE_NAME, null);
				layer =new WMSTiledImageLayer(jcSelectLayer.caps, jcSelectLayer.params, t[j+tBegin]);
				layer.setEnabled(false);
				layer.setEnabled(true);
				wwd.getModel().getLayers().setTime(jcBeginning.getItemAt(tBegin+j).toString());
				layersByTime.get(j).setEnabled(true);
				wwd.getModel().getLayers().add(layersByTime.get(j));
				
				j++;	

				wwd.redraw();
			}
		}
	}

	
	//--add by jingli start button action for animation
	protected class LayerInfoStartAction extends AbstractAction {

		protected LayerInfo layerInfo;
		protected WorldWindow wwd;
		
		public LayerInfoStartAction(LayerInfo linfo, WorldWindow wwd) {
			super(linfo.getTitle());
			// Capture info we'll need later to control the layer.
			this.layerInfo = linfo;
			this.wwd =  wwd;
		}

		// ///////////////////////////////////
		// /Create Layer
		// /////////////////////////////////
		public void actionPerformed(final ActionEvent actionEvent) {
			layerInfo = jcSelectLayer;
			// parse the time dimension to get the time series
			if (((JToggleButton) actionEvent.getSource()).isSelected()) {
				
				((JToggleButton) actionEvent.getSource()).setText("stop");
				timescale = new TimeParser(this.layerInfo.params
						.getStringValue(AVKey.WMS_TIME_DIMENSION));
				t = timescale.ParseTimeStr().toArray(
						new String[timescale.ParseTimeStr().size()]);
				int tBegin = jcBeginning.getSelectedIndex();
				int tEnd = jcEnd.getSelectedIndex();
				for (int tSeries = tBegin; tSeries <= tEnd; tSeries++) {
					//add all layers 
					layerInfo.params.setValue(AVKey.DATA_CACHE_NAME, null);
					layersByTime.add(new WMSTiledImageLayer(layerInfo.caps, layerInfo.params,
									t[tSeries]));
				}
				timerwms = new Timer(picSpeed.getValue() * 10,
						new drawLayerByTime());
				
				Layer layer = layersByTime.get(0);
				layer.setEnabled(true);
				LayerList layers = this.wwd.getModel().getLayers();
				if (!layers.contains(layer))
                {
                    ApplicationTemplate.insertBeforePlacenames(this.wwd, layer);
                    this.firePropertyChange("LayersPanelUpdated", null, layer);
                }
				
				timerwms.start();
			        
			} else {

				Layer layer = layersByTime.get(0);
				layer.setEnabled(enabled);
				wwd.getModel().getLayers().setTime(null);
				TimeWMSLayersPanel.this.firePropertyChange("LayersPanelUpdated",
						layer, null);
				timerwms.stop();
				picSpeed.setValue(50);	
				
				((JToggleButton) actionEvent.getSource()).setText("start");
				int layersLength = layersByTime.size();
				
				while (layersLength > 0) {
					layersByTime.remove(--layersLength);
				}
			

			}
			wwd.redraw();
		}
	}


	
	private class LayerInfoAction extends AbstractAction {
		private WorldWindow wwd;
		private LayerInfo layerInfo;
		private BasicTiledImageLayer layer;
		private TimeParser timescale;
		
		protected Object component;
	

		public LayerInfoAction(LayerInfo linfo, WorldWindow wwd) {
			super(linfo.getTitle());
			// Capture info we'll need later to control the layer.
			this.wwd = wwd;
			this.layerInfo = linfo;
			
		}

		// ///////////////////////////////////
		// /Create Layer
		// /////////////////////////////////
		public void actionPerformed(ActionEvent actionEvent) {
		
			if (((JCheckBox) actionEvent.getSource()).isSelected()) {
				
				
				jStart.setSelected(false);
				if(timerwms!=null)
				{
				timerwms.stop();
				}
			
				//check if the time information is avaliables
				if(this.layerInfo.params.getStringValue(AVKey.WMS_TIME_DIMENSION)!=null){
					
					timescale = new TimeParser(this.layerInfo.params
							.getStringValue(AVKey.WMS_TIME_DIMENSION));
					ArrayList<String> timelabels= timescale.ParseTimeStr();
					jcBeginning.removeAllItems();
					jcEnd.removeAllItems();
					int index = 0;
					for (String timelabel : timelabels) {
						jcBeginning.insertItemAt(timelabel, index);
						jcEnd.insertItemAt(timelabel, index);
						index++;
					}
					jcBeginning.setSelectedIndex(0);
					jcEnd.setSelectedIndex(0);
					timelabels.clear();
					
					jcSelectLayer = this.layerInfo;
				}
				else{
					jcSelectLayer=null;
				}
				

				
				 if (this.component == null)
	                    this.component = createComponent(layerInfo.caps, layerInfo.params);

	                updateComponent(this.component, true);
			} else {
				
				picSpeed.setValue(50);
				jcBeginning.removeAllItems();
				jcEnd.removeAllItems();
			
				jcSelectLayer = null;
				
				  if (this.component != null)
	                    updateComponent(this.component, false);
			}

			// Tell the world window to update.
			wwd.redraw();

		}
	}

	
    protected void updateComponent(Object component, boolean enable)
    {
        if (component instanceof Layer)
        {
            Layer layer = (Layer) component;
            LayerList layers = this.wwd.getModel().getLayers();

            layer.setEnabled(enable);

            if (enable)
            {
                if (!layers.contains(layer))
                {
                    ApplicationTemplate.insertBeforePlacenames(this.wwd, layer);
                    this.firePropertyChange("LayersPanelUpdated", null, layer);
                }
            }
            else
            {
                layers.remove(layer);
                this.firePropertyChange("LayersPanelUpdated", layer, null);
            }
        }
        else if (component instanceof ElevationModel)
        {
            ElevationModel model = (ElevationModel) component;
            CompoundElevationModel compoundModel =
                (CompoundElevationModel) this.wwd.getModel().getGlobe().getElevationModel();

            if (enable)
            {
                if (!compoundModel.getElevationModels().contains(model))
                    compoundModel.addElevationModel(model);
            }
        }
    }

    protected static Object createComponent(WMSCapabilities caps, AVList params)
    {
        AVList configParams = params.copy(); // Copy to insulate changes from the caller.

        // Some wms servers are slow, so increase the timeouts and limits used by world wind's retrievers.
        configParams.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
        configParams.setValue(AVKey.URL_READ_TIMEOUT, 30000);
        configParams.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);

        try
        {
            String factoryKey = getFactoryKeyForCapabilities(caps);
            Factory factory = (Factory) WorldWind.createConfigurationComponent(factoryKey);
            return factory.createFromConfigSource(caps, configParams);
        }
        catch (Exception e)
        {
            // Ignore the exception, and just return null.
        }

        return null;
    }

    protected static String getFactoryKeyForCapabilities(WMSCapabilities caps)
    {
        boolean hasApplicationBilFormat = false;

        Set<String> formats = caps.getImageFormats();
        for (String s : formats)
        {
            if (s.contains("application/bil"))
            {
                hasApplicationBilFormat = true;
                break;
            }
        }

        return hasApplicationBilFormat ? AVKey.ELEVATION_MODEL_FACTORY : AVKey.LAYER_FACTORY;
    }
	
	//added by jing li 
	//return the 
	protected static String makeTime(WMSCapabilities caps, LayerInfo layerInfo) {
	    String layerNames = layerInfo.params.getStringValue(AVKey.LAYER_NAMES);
        String styleNames = layerInfo.params.getStringValue(AVKey.STYLE_NAMES);
        String[] lNames = layerNames.split(",");
        String[] sNames = styleNames != null ? styleNames.split(",") : null;

        StringBuilder sb = new StringBuilder();
        
		for (int i = 0; i < lNames.length; i++) {
			if (sb.length() > 0)
				sb.append(", ");

			String layerName = lNames[i];
			
			WMSLayerCapabilities lc = caps.getLayerByName(layerName);
			
			
			String layerDimension = null;
			
			Set<WMSLayerDimension> dimension = lc.getDimensions();
			
			if (dimension == null)
				return null;

				for (WMSLayerDimension es : dimension) {
					if( es.getName().equals("time")){
							layerDimension = es.getDimension();

					}	
				}
			
			sb.append(layerDimension != null ? layerDimension : null);
		}

		return sb.toString();
	}

    protected static String makeTitle(WMSCapabilities caps, LayerInfo layerInfo)
    {
        String layerNames = layerInfo.params.getStringValue(AVKey.LAYER_NAMES);
        String styleNames = layerInfo.params.getStringValue(AVKey.STYLE_NAMES);
        String[] lNames = layerNames.split(",");
        String[] sNames = styleNames != null ? styleNames.split(",") : null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lNames.length; i++)
        {
            if (sb.length() > 0)
                sb.append(", ");

            String layerName = lNames[i];
            WMSLayerCapabilities lc = caps.getLayerByName(layerName);
            String layerTitle = lc.getTitle();
            sb.append(layerTitle != null ? layerTitle : layerName);

            if (sNames == null || sNames.length <= i)
                continue;

            String styleName = sNames[i];
            WMSLayerStyle style = lc.getStyleByName(styleName);
            if (style == null)
                continue;

            sb.append(" : ");
            String styleTitle = style.getTitle();
            sb.append(styleTitle != null ? styleTitle : styleName);
        }

        return sb.toString();
    }


	protected void makeProgressPanel() {
		// Create the panel holding the progress bar during loading.

		JPanel outerPanel = new JPanel(new BorderLayout());
		outerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		outerPanel.setPreferredSize(this.size);

		JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		innerPanel.add(progressBar, BorderLayout.CENTER);

		JButton cancelButton = new JButton("Cancel");
		innerPanel.add(cancelButton, BorderLayout.EAST);
		cancelButton.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent actionEvent) {
				if (loadingThread.isAlive())
					loadingThread.interrupt();

				Container c = TimeWMSLayersPanel.this.getParent();
				c.remove(TimeWMSLayersPanel.this);
			}
		});

		outerPanel.add(innerPanel, BorderLayout.NORTH);
		this.add(outerPanel, BorderLayout.CENTER);
		this.revalidate();
	}
}
