/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.du.ogc.netcdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;

/**
 *
 * @author jing.li145
 */
public class NetCDFReader2D {

    String filename;
    List<Variable> varlist;
    private float min, max;
    NetcdfFile ncfile;
    private Array data;
    private Index index;
    Variable var;
    GeoGrid cfield;
    double increX, increY;
    int d1, d2;
    GridCoordSystem gcs;
    double left, right, up, bottom;

    public NetCDFReader2D(String filepath) {
        this.filename = filepath;

    }

    public void ReadNetCDF() {

//        String filename = "C:/data/my/file.nc";
        try {
            ncfile = NetcdfFile.open(filename);

            NetcdfDataset ncDataSet = new NetcdfDataset(ncfile);
            GridDataset ncGridDataset = new GridDataset(ncDataSet);
            if (ncGridDataset.getGrids().size() > 0) {
                System.out.print("Grid");
                cfield = (GeoGrid) ncGridDataset.getGrids().get(0);
            }

//            Variable lat = ncfile.findVariable("lat");
////            long nlats = lat.getSize();
//
//            Variable lon = ncfile.findVariable("lon");
////            long nlons = lon.getSize();
            List<Variable> varlist = ncfile.getVariables();
            for (int i = 0; i < varlist.size(); i++) {
                var = varlist.get(i);
                if (var.getDimensions().size() > 2) {

                    data = var.read();
                    //create an image
                    index = data.getIndex();
                    break;
                }
            }

            left = cfield.getProjection().getDefaultMapAreaLL()
                    .getUpperLeftPoint().getLongitude();
            right = cfield.getProjection().getDefaultMapAreaLL()
                    .getLowerRightPoint().getLongitude();
            up = cfield.getProjection().getDefaultMapAreaLL()
                    .getUpperLeftPoint().getLatitude();
            bottom = cfield.getProjection().getDefaultMapAreaLL()
                    .getLowerRightPoint().getLatitude();
            gcs = cfield.getCoordinateSystem();

            d1 = var.getDimension(1).getLength();
            d2 = var.getDimension(2).getLength();
            increX = (Math.abs((left - right))) / (d2);
            increY = (Math.abs((up - bottom))) / (d1);

        } catch (IOException ioe) {
            System.err.println("trying to open " + filename);
        }

    }

    public void CloseNetCDF() {
        try {
            ncfile.close();
        } catch (IOException ex) {
            Logger.getLogger(NetCDFReader2D.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void CreateImage(String imgpath, String legendpath) {

        String para = var.getDescription();
        this.findExtreme(var);
        PngColor pc = new PngColor(this.min, this.max, para);

        File f = new File(legendpath);
        if (!f.exists()) {
            f.mkdirs();
            pc.createLegend(legendpath);
        }

        PngWriter png = new PngWriter();
        // put the output location here
        File fo = new File(imgpath);
        if (!fo.exists()) {
            fo.mkdirs();
        }
        png.createImage(this.getRGBArray(var, pc), fo);
    }

    private void findExtreme(Variable var) {
        //for 2 dimensions
        float dataAtLocation = data.getFloat(index.set(0, 0));

        int d1 = var.getDimension(1).getLength();
        int d2 = var.getDimension(2).getLength();
        for (int i = 0; i < d1; i++) {
            for (int j = 0; j < d2; j++) {
                dataAtLocation = data.getFloat(index.set(0, i, j));

                if (this.max < dataAtLocation) {
                    this.max = dataAtLocation;
                } else if (this.min > dataAtLocation) {
                    this.min = dataAtLocation;
                }
            }
        }

        if (this.max > 0) {
            this.max = this.max * 1.0001f;
        } else {
            this.max = this.max * 0.9999f;
        }
        if (this.min > 0) {
            this.min = this.min * 0.9999f;
        } else {
            this.min = this.min * 1.0001f;
        }
        System.out.println(this.min + "," + this.max);
    }

    private int[][] getRGBArray(Variable var, PngColor pc) {

        int[][] rgb = new int[1 + d2][1 + d1];
        // System.out.println(d1+","+d2);

        int xLoc = 0, yLoc = 0;

        for (int i = 0; i < d1; i++) {
            for (int j = 0; j < d2; j++) {
                xLoc = (int) (((gcs.getLatLon(j, i).getLongitude() - left) / increX)); //xLoc ==j
                yLoc = (int) (((up - gcs.getLatLon(j, i).getLatitude()) / increY)); //yLoc == d1-i-1
                float datavalue = data.getFloat(index.set(0, i, j));
//                if (yLoc!=d1-i-1)
//                        {
//                System.out.println(String.valueOf(xLoc)+","+String.valueOf(yLoc)+"; "+String.valueOf(j)+","+String.valueOf(i));
//                        }
                if (!Float.isNaN(datavalue)) {
                    rgb[xLoc][yLoc] = pc.getColorRGB(data.getFloat(index.set(0, i, j)));
                } else {
                    rgb[xLoc][yLoc] = -1;
                }

            }
        }
        return rgb;
    }

    //convert a value to an index
    public int[] ConvertPosToIndex(double locX, double locY) {
        int xLoc = (int) (((locX) - left) / increX);
        int yLoc = (int) (((locY) - bottom) / increY);
        int[] locs = new int[2];
        if (xLoc < 0) {
            xLoc = 0;
        } else if (xLoc > d2 - 1) {
            xLoc = d2;
        }

        if (yLoc < 0) {
            yLoc = 0;
        } else if (yLoc > d1 - 1) {
            yLoc = d1;
        }

        locs[0] = xLoc;
        locs[1] = yLoc;
        return locs;
    }
    
    public double GetLeft()
    {
        return this.left;
    }
    
       public double GetRight()
    {
        return this.right;
    }
          public double GetBottom()
    {
        return this.bottom;
    }
             public double GetUp()
    {
        return this.up;
    }
    

    //get  z value from the netcdf file, lat lon values
    public float GetAttribute(int x, int y) {

        float datavalue= data.getFloat((index.set(0, y, x)));
          if (!Float.isNaN(datavalue)) {
        System.out.println(datavalue);
          }
          return datavalue;
    }
    
    public String GetVariableName()
    {
        return this.var.getDescription();
    }

//    public static void main(String[] args) {
//        NetCDFReader2D nctest = new NetCDFReader2D("test.nc");
//        nctest.ReadNetCDF();
//        nctest.CreateImage("testimg.png", "testlegend.png");
//        nctest.CloseNetCDF();
//    }

}
