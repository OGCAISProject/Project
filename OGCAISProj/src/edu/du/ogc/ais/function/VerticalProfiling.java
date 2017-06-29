package edu.du.ogc.ais.function;

import edu.du.ogc.gml.GMLPointReader;
import edu.du.ogc.netcdf.NetCDFReader2D;
import gov.nasa.worldwind.geom.LatLon;
import java.util.ArrayList;

import gov.nasa.worldwind.geom.Position;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



public final class VerticalProfiling  {

    private final NetCDFReader2D nctest; //store a list of climate web services within the time frame
     //store a list of tracks without temporal information 
    private final String variablename;
    ArrayList<Float> zvalueall = new ArrayList<Float>();
static ArrayList<Position> pathPositions = new ArrayList<Position>();
//	protected ArrayList<String> timesteps; //store a list of time steps 
    //https://ioos.noaa.gov/project/hf-radar/ wcs 1.0.0 http://sdf.ndbc.noaa.gov/wcs/
    public VerticalProfiling(String climatefilepath, String trackfilepath) {

        nctest = new NetCDFReader2D(climatefilepath);
        nctest.ReadNetCDF();
        GMLPointReader gmlptreader = new GMLPointReader(trackfilepath);
       pathPositions =  gmlptreader.GetPositions(gmlptreader.readGMLData());
        variablename = nctest.GetVariableName();
        this.GetProfling();

    }

  
    public ArrayList<Float> GetProfling() {

        for (int i = 0; i < pathPositions.size() - 1; i++) {
            Position pos1 = pathPositions.get(i);
            Position pos2 = pathPositions.get(i + 1);
            int[] pos1_int = nctest.ConvertPosToIndex(pos1.longitude.degrees, pos1.latitude.degrees);
            int[] pos2_int = nctest.ConvertPosToIndex(pos2.longitude.degrees, pos2.latitude.degrees);
            zvalueall.addAll(FindRasterCellOnLine(pos1_int[0], pos1_int[1], pos2_int[0], pos2_int[1]));
        }
        return zvalueall;
    }

    public String GetVariableName()
    {
        return nctest.GetVariableName();
    }
    
    public ArrayList<Float> GetZValues()
    {
        return this.zvalueall;
    }
    
//	Bresenham Algorithm
//	https://rosettacode.org/wiki/Bitmap/Bresenham%27s_line_algorithm#Java
    //x1, y1, x2, y2 : two end points of a line segement
    private ArrayList<Float> FindRasterCellOnLine(int x1, int y1, int x2, int y2) {
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
                zvalue.add(nctest.GetAttribute(x1, y1));
                if (x1 >= x2) {
                    break;
                }
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
                zvalue.add(nctest.GetAttribute(x1, y1));
                if (y1 >= y2) {
                    break;
                }
                y1 += iy;
                d += dx2;
                if (d > dy) {
                    x1 += ix;
                    d -= dy2;
                }
            }
        }

        return zvalue;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        VerticalProfiling vp = new VerticalProfiling("test.nc", "3565.xml");
        
    }

}
