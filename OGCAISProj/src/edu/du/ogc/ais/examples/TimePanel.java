package edu.du.ogc.ais.examples;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TimePanel extends JPanel{
	private JSlider picSpeed = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
	private JComboBox jcBeginning = new JComboBox();
	private JComboBox jcEnd = new JComboBox();
	private JToggleButton jStart;
	private javax.swing.Timer times;
	private int time;
	
	 private WorldWindow wwd;
		public TimePanel(WorldWindow wwd, int time)
		{
			  super(new BorderLayout());
			  this.wwd = wwd;
			  this.time = time;
			  this.makepanel();
			  times = new Timer(200, new changeTime());
			
		}
		
		
		
		private JPanel makepanel() {
			// add time control panel
			for(int i=0; i<time; i++)
			{
				if(i<10)
				{
					jcBeginning.insertItemAt("2008-01-07_0"+i+":00:00", i);
					jcEnd.insertItemAt("2008-01-07_0"+i+":00:00", i);
				}
				else
				{
					jcBeginning.insertItemAt("2008-01-07_"+i+":00:00", i);
					jcEnd.insertItemAt("2008-01-07_"+i+":00:00", i);
				}
					
			}
			
		JPanel timeSeries = new JPanel(new GridLayout(0, 2, 5, 5));
		timeSeries.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		timeSeries.add(new JLabel("BeginTime"));
		timeSeries.add(jcBeginning);
		timeSeries.add(new JLabel("EndTime"));
		timeSeries.add(jcEnd);
		JPanel timeControl = new JPanel(new BorderLayout());
		
		// Create the slider
		picSpeed.setValue(50);
		picSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				if(times!=null)
				{
					times.setDelay(picSpeed.getValue() * 10);
					if(jStart.isSelected())
						times.start();
				}
				
			}
		});
		picSpeed.setMajorTickSpacing(10);
		picSpeed.setPaintTicks(true);
		picSpeed.setPaintLabels(false);
		timeControl.add(picSpeed,BorderLayout.CENTER);
		
		jStart = new JToggleButton();
		jStart.setText("Start");
		jStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(times!=null)
				{
					if(jStart.isSelected())
					{
						times.start();
						jStart.setText("Stop");
					}
						
					else
					{
						times.stop();
						//layer.setTime(1);
						wwd.getModel().getLayers().setTime((String) jcBeginning.getItemAt(0));
						jStart.setText("Start");
						picSpeed.setValue(50);
					}
				}
			}
			
			});
		
		timeControl.add(jStart,BorderLayout.EAST);
		JPanel timePanel  = this;//new JPanel(new BorderLayout());
		timePanel.add(timeSeries,BorderLayout.NORTH);
		timePanel.add(timeControl,BorderLayout.SOUTH);
		timePanel.setBorder(new CompoundBorder(BorderFactory
				.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Time Control")));
		return timePanel;
		}

	
		
		private class changeTime extends AbstractAction{
			int i=0;
			int currentIndex;
			String dustLayerName;
			public void actionPerformed(ActionEvent e) {
				//always disable the first layer
				dustLayerName = "Dust0";
				getLayerByName(dustLayerName).setEnabled(false);
				// TODO Auto-generated method stub
				int interval = jcEnd.getSelectedIndex()-jcBeginning.getSelectedIndex();
				if(i<interval)
				{
					i++;
				}
				else
				{
					i=0;
				}
				
				if (i!=0)
				dustLayerName = "Dust"+(i-1);
				else 
					dustLayerName ="Dust"+interval;
			
				currentIndex = i+jcBeginning.getSelectedIndex();
				getLayerByName(dustLayerName).setEnabled(false);
				//set up the selected index
				wwd.getModel().getLayers().get(11+currentIndex).setEnabled(true);
				wwd.getModel().getLayers().setTime((String) jcBeginning.getItemAt(currentIndex));
				wwd.redraw();
			}
			
			public Layer getLayerByName(String layerName)
		    {
		        for (Layer layer : wwd.getModel().getLayers())
		        {
		       
		            if (layer.getName().indexOf(layerName) != -1)
		                return layer;
		        }
		        return null;
		    }
		}
		
		   
		
		
		private class LayerStart extends AbstractAction{
		
			public LayerStart()
			{
				
			}
			
			public void actionPerformed(final ActionEvent actionEvent) {
		
			}
			
		}
	   
}
