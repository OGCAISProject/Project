package edu.du.ogc.ais.examples;

import java.util.ArrayList;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;

//import for javafx to create charts
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class VerticalProfiling {
	protected ArrayList<String> climatefilepath = new ArrayList<String>(); //store a list of climate web services within the time frame
	protected ArrayList<Position> tracks = new ArrayList<Position>();  //store a list of tracks without temporal information 
	protected ArrayList<String> timesteps; //store a list of time steps 
	//https://ioos.noaa.gov/project/hf-radar/ wcs 1.0.0 http://sdf.ndbc.noaa.gov/wcs/
	
	//initalization
	public VerticalProfiling(ArrayList<String> inclimatefilepath, ArrayList<Position> intracks,ArrayList<String> intimesteps )
	{
		
		for (int i = 0; i < inclimatefilepath.size(); i ++)
		{
			climatefilepath.add(inclimatefilepath.get(i));
		}
		
		for (int i = 0; i < intracks.size(); i ++)
		{
			tracks.add(intracks.get(i));
		}
		
		
		for (int i = 0; i < intimesteps.size(); i ++)
		{
			timesteps.add(intimesteps.get(i));
		}
	}
	
	//1 hour HF observation; tracks within one hour
	public void LineBoxIntersection(String imgfile)
	{
		
		
	}
	
	
	//use the spatiotemporal bounds to perform queries and get wcs data
	public Sector  GetSpatialBound()
	{	
		double minLat =  Double.MAX_VALUE, minLon = Double.MAX_VALUE, maxLat = Double.MIN_VALUE, maxLon= Double.MIN_VALUE;
		for (int i =0; i <tracks.size(); i++)
		{
			Position track = tracks.get(i); 
			if (track.latitude.degrees >  maxLat)
			{
				maxLat = track.latitude.degrees;
			}
			
			if (track.longitude.degrees >  maxLon)
			{
				maxLon = track.longitude.degrees;
			}
			
			if (track.latitude.degrees <  minLat)
			{
				minLat = track.latitude.degrees;
			}
			
			if (track.longitude.degrees <  minLon)
			{
				minLon = track.longitude.degrees;
			}
		}
		Sector bound = Sector.fromDegrees(new double[]{minLat, maxLat, minLon, maxLon});
		return bound;
	}
	
	public String[] GetTemporalBound()
	{
		//assuming the first and the last time steps consist of the boundary
		String[] timeperiod = new String[]{timesteps.get(0), timesteps.get(timesteps.size()-1)}; 
		return timeperiod;
	}
	
	
//	Bresenham Algorithm
//	https://rosettacode.org/wiki/Bitmap/Bresenham%27s_line_algorithm#Java
	//x1, y1, x2, y2 : two end points of a line segement
    private void FindRasterCellOnLine(int x1, int y1, int x2, int y2) {
        // delta of exact value and rounded value of the dependant variable
    	ArrayList<Float> zvalue = new ArrayList<Float>();
        int d = 0;
 
        int dy = Math.abs(y2 - y1);
        int dx = Math.abs(x2 - x1);
 
        int dy2 = (dy << 1); // slope scaling factors to avoid floating
        int dx2 = (dx << 1); // point
 
        int ix = x1 < x2 ? 1 : -1; // increment direction
        int iy = y1 < y2 ? 1 : -1;
 
        if (dy <= dx) {
            for (;;) {
                //get a new pair of x1, y1
            	zvalue.add(GetAttribute(x1, y1));
                if (x1 == x2)
                    break;
                x1 += ix;
                d += dy2;
                if (d > dx) {
                    y1 += iy;
                    d -= dx2;
                }
            }
        } else {
            for (;;) {
                //get a new pair of x1, y1
            	zvalue.add(GetAttribute(x1, y1));
                if (y1 == y2)
                    break;
                y1 += iy;
                d += dx2;
                if (d > dy) {
                    x1 += ix;
                    d -= dy2;
                }
            }
        }
    }
    
  //get  z value from the netcdf file 
    private float GetAttribute(int x, int y)
    {
    	
    	return 0.f; 
    }
	
    //create a vertical plot for a variable 
    private void CreateLineCharts(Stage stage, String variablename, ArrayList<Float> zvalue)
    {
    	  stage.setTitle("Vertical Profile"+ variablename);
          //defining the axes
          final NumberAxis xAxis = new NumberAxis();
          final NumberAxis yAxis = new NumberAxis();
          xAxis.setLabel("Distance");
          //creating the chart
          final LineChart<Number,Number> lineChart = 
                  new LineChart<Number,Number>(xAxis,yAxis);
                  
//          lineChart.setTitle("");
          //defining a series
          XYChart.Series series = new XYChart.Series();
          series.setName("My portfolio");
          //populating the series with data
          for (int i =0 ; i < zvalue.size(); i++)
          {
        	  series.getData().add(new XYChart.Data(i, zvalue));
          }
          
          Scene scene  = new Scene(lineChart,800,600);
          lineChart.getData().add(series);
         
          stage.setScene(scene);
          stage.show();
          
    }
    
    
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
