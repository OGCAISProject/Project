package edu.du.ogc.netcdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;

public class NetCDFReaderReg {
	/*
	 * :units = "m s-1"; :long_name =
	 * "V-component_of_wind @ altitude_above_msl"; :missing_value = NaNf;
	 */
	private NetcdfDataset ncDataSet;
	private NetcdfFile ncfile;
	public Array data;
	public Index index;
	private Variable variableSel;
	private Variable latVar, lonVar, timeVar, zVar;
	public float min, max;
	private float missing_value = 9.999E20f;
	public ArrayList<String> variableArray = new ArrayList<String>();
	private ArrayList<CoordinateAxis> coordinates = new ArrayList<CoordinateAxis>();
	public ArrayList<String> timeArray = new ArrayList<String>();
	private boolean timeIncluded = false;
	public int dLat, dLon, dEle;
	private TimeParserReg timeParserReg;
	public double latMin, latMax, lonMin, lonMax, eleMin, eleMax;//can be meters but use getlatlon to get original value
	public double latMinDeg, latMaxDeg, lonMinDeg, lonMaxDeg;
	private GridDataset gridDataset;
	private GeoGrid cfield;
	public GridCoordSystem gcs;
	private boolean isGrid = false;
	
	private HashMap<String, Integer> sequence = new HashMap<String, Integer>();
	//sequence: time, lat,long, ele;
	public NetCDFReaderReg(String fileUrl) {
		try {
			ncfile = NetcdfDataset.open(fileUrl);
			ncDataSet = new NetcdfDataset(ncfile);
			gridDataset = new GridDataset(ncDataSet);
			//System.out.println(ncDataSet);
			for  ( int i=0; i<gridDataset.getGrids().size() ; i++) {
				cfield = (GeoGrid) gridDataset.getGrids().get(i);
				this.isGrid = true;
				if(cfield.getShape().length>2)
				{
					gcs = cfield.getCoordinateSystem();
					latMinDeg = gcs.getBoundingBox().getMinY();
					latMaxDeg = gcs.getBoundingBox().getMaxY();
					lonMinDeg = gcs.getBoundingBox().getMinX();
					lonMaxDeg = gcs.getBoundingBox().getMaxY();
					System.out.println(gcs+","+	gcs.getBoundingBox().getMinX()+gcs.getLatLon(1, 1));
					break;
				}
			}
			// dimensions but also variables
			this.setupBoundary();
			this.setup3Dvariable();
			this.createTimeArray();
			//System.out.println(this.isGrid); --if it is not grid, get lat lon from the variables
			//System.out.println(latMin+","+ latMax+","+lonMin+","+lonMax+","+ eleMin+","+ eleMax);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * determine the axis of the datasets only some of the axes will be used for
	 * a variable
	 */
	public void setupBoundary() {
		if (ncDataSet.getCoordinateAxes().size() > 0) {
			for (int i = 0; i < ncDataSet.getCoordinateAxes().size(); i++) {
				CoordinateAxis ca = ncDataSet.getCoordinateAxes().get(i);
				//System.out.println(ca.getName());
				if (ca.getName().toLowerCase().startsWith("tim")) {
					this.timeIncluded = true;
					this.timeVar = this.ncDataSet.findVariable(ca.getName());
					this.timeParserReg = new TimeParserReg(this.timeVar
							.getUnitsString());
				}

				else if (ca.getName().toLowerCase().startsWith("lat")
						|| ca.getName().toLowerCase().contains("north")
						|| ca.getName().toLowerCase().contains("west")
						|| ca.getName().toLowerCase().contains("y")) {
					this.dLat = (int) ca.getSize();
					this.latMin = ca.getMinValue();
					this.latMax = ca.getMaxValue();
					this.latVar = this.ncDataSet
					.findVariable(ca.getName());
				} else if (ca.getName().toLowerCase().startsWith("lon")
						|| ca.getName().toLowerCase().contains("east")
						|| ca.getName().toLowerCase().contains("west")
						|| ca.getName().toLowerCase().contains("x")) {
					this.dLon = (int) ca.getSize();
					this.lonMin = ca.getMinValue();
					this.lonMax = ca.getMaxValue();
					this.lonVar = this.ncDataSet
					.findVariable(ca.getName());
				} else if (ca.getName().toLowerCase().startsWith("ele")
						|| (ca.getName().toLowerCase().contains("pres"))
						|| (ca.getName().toLowerCase().contains("bottom"))
						|| ca.getName().toLowerCase().contains("z")
						|| ca.getName().toLowerCase().startsWith("lev")||
						ca.getName().toLowerCase().startsWith("niv")||
						ca.getName().toLowerCase().startsWith("altitude")) {
					// revise when pressure and elevation are available
					this.dEle = (int) ca.getSize();
					this.eleMin = ca.getMinValue();
					this.eleMax = ca.getMaxValue();
					this.zVar = this.ncDataSet
					.findVariable(ca.getName());
				}
				this.coordinates.add(ca);
			}
		} else {
			// if the coordinateAxes is not availiable find axes from dimensions
			try {
				for (int i = 0; i < ncDataSet.getDimensions().size(); i++) {
					Dimension dim = ncDataSet.getDimensions().get(i);
					//System.out.println(dim.getName());
					// if(dim.getName().equals(anObject))
					if (dim.getName().toLowerCase().startsWith("lat")
							|| dim.getName().toLowerCase().contains("north")
							|| dim.getName().toLowerCase().contains("south")
							|| dim.getName().toLowerCase().contains("y")) {
						this.latVar = this.ncDataSet
								.findVariable(dim.getName());

						double lat1 = this.latVar.read().getDouble(
								(int) (this.latVar.getSize() - 1));
						double lat2 = this.latVar.read().getDouble(
								(int) (this.latVar.getSize() - 1));
						this.latMax = Math.max(lat1, lat2);
						this.latMin = Math.min(lat1, lat2);
					} else if (dim.getName().toLowerCase().startsWith("lon")
							|| dim.getName().toLowerCase().contains("east")
							|| dim.getName().toLowerCase().contains("west")
							|| dim.getName().toLowerCase().contains("x")) {
						this.lonVar = this.ncDataSet
								.findVariable(dim.getName());
						double lon1 = this.lonVar.read().getDouble(
								(int) (this.lonVar.getSize() - 1));
						double lon2 = this.lonVar.read().getDouble(
								(int) (this.lonVar.getSize() - 1));
						this.lonMax = Math.max(lon1, lon2);
						this.lonMin = Math.min(lon1, lon2);
					} else if (dim.getName().toLowerCase().startsWith("tim")) {
						this.timeIncluded = true;
						this.timeVar = this.ncDataSet.findVariable(dim
								.getName());
						this.timeParserReg = new TimeParserReg(this.timeVar
								.getUnitsString());
					} else if (dim.getName().toLowerCase().startsWith("ele")
							|| dim.getName().toLowerCase().contains("pres")
							||dim.getName().toLowerCase().contains("bottom")||
							dim.getName().toLowerCase().contains("up")
							|| dim.getName().toLowerCase().startsWith("lev")||
							dim.getName().toLowerCase().startsWith("niv")) {
						this.zVar = this.ncDataSet.findVariable(dim.getName());
						double z1 = this.zVar.read().getDouble(
								(int) (this.zVar.getSize() - 1));
						double z2 = this.zVar.read().getDouble(
								(int) (this.zVar.getSize() - 1));
						this.eleMax = Math.max(z1, z2);
						this.eleMin = Math.min(z1, z2);
					}
					//System.out.println(dim.getName());

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//check the variables...

		}
	}

	public void createTimeArray() {
		try {
			if (this.timeIncluded) {
				Array data;
				data = this.timeVar.read();
				for (int i = 0; i < data.getSize(); i++) {
					//System.out.println(this.timeParserReg.GetTime(data
							//.getInt(i)));
					this.timeArray.add(this.timeParserReg.GetTime(data
							.getInt(i)));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setup3Dvariable() {
		for (int i = 0; i < ncDataSet.getVariables().size(); i++) {
			Variable var = ncDataSet.getVariables().get(i);
			//
			String dimString = var.getDimensionsString();
			if(this.zVar!=null){
				if(dimString.contains(this.latVar.getName())&&dimString.contains(this.lonVar.getName())||dimString.contains(this.zVar.getName()))
				{
					//System.out.println(var);
					this.variableArray.add(var.getName());
				}
			}
			
		}

	}
	
	public void setLatVariable(String latName)
	{
		this.latVar = ncfile.findVariable(latName);
		
	}
	
	public void setLonVariable(String lonName)
	{
		this.lonVar = ncfile.findVariable(lonName);
	}
	
	public void setZVariable(String zName)
	{
		this.zVar = ncfile.findVariable(zName);
	}

	/*
	 * find the parameters: lat,long by checking the range of variable
	 */
	public double[] getLatBoundary() {
		Array data;
		try {
			data = this.latVar.read();
			int length =(int) (this.latVar.getSize()-1);
			return new double[]{data.getDouble(0), data.getDouble(length)};
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	/*
	 * find the parameters: lat,long by checking the range of variable
	 */
	public double[] getLonBoundary() {

		Array data;
		try {
			data = this.lonVar.read();
			int length =(int) (this.lonVar.getSize()-1);
			return new double[]{data.getDouble(0), data.getDouble(length)};
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;

	}


	public ArrayList<String> getTimeString() {
		return timeArray;
	}

	public double[] getZBoundary()
	{
		//pay attention to the units of Z
		Array data;
		try {
			data = this.zVar.read();
			int length =(int) (this.zVar.getSize()-1);
			String unitString = this.zVar.getUnitsString().toLowerCase();
			double min, max;
			min = data.getDouble(0);
			max =  data.getDouble(length);
			//convert to meters
			if(unitString.contains("kilom")||unitString.contains("km"))
			{
				return new double[]{min*1000,max*1000};
			}
			else if(unitString.contains("hPa"))
			{
				min = 0.3048*(1- Math.pow(min/1013.25, 0.190284))*145366.45;
				max = 0.3048*(1- Math.pow(max/1013.25, 0.190284))*145366.45;
				return new double[]{min, max};
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	

	public void readData(String variableName) {
		this.variableSel = this.ncDataSet.findVariable(variableName);
		this.findExtreme(this.variableSel);
		this.sequence.clear();
		for(int i =0 ; i < this.variableSel.getDimensions().size(); i++)
		{
			this.sequence.put( this.variableSel.getDimension(i).getName(), i);
		}
	}

	/*
	 * need to check if the sequence of input is the same as the dimensions
	 */
	public float getValue(int timeSeq, int latSeq, int lonSeq, int eleSeq) {
		int timeRank =(int)(this.sequence.get(this.timeVar.getName()));
		int latRank =(this.sequence.get(this.latVar.getName()));
		int lonRank =( this.sequence.get(this.lonVar.getName()));
		int eleRank =( this.sequence.get(this.zVar.getName()));
		ArrayList<Integer> arrayRank = new ArrayList<Integer>();
		arrayRank.add(0);
		arrayRank.add(0);
		arrayRank.add(0);
		arrayRank.add(0);
		arrayRank.set(timeRank, timeSeq);
		arrayRank.set(latRank, latSeq);
		arrayRank.set(lonRank, lonSeq);
		arrayRank.set(eleRank, eleSeq);
		index = data.getIndex();
		return data.getFloat(index.set(arrayRank.get(0),arrayRank.get(1),arrayRank.get(2),arrayRank.get(3)));
	}
	
	/*
	 * if the variable is 3D
	 */
	public float getValue3D(int latSeq, int lonSeq, int eleSeq)
	{
		int latRank =(this.sequence.get(this.latVar.getName()));
		int lonRank =( this.sequence.get(this.lonVar.getName()));
		int eleRank =( this.sequence.get(this.zVar.getName()));
		ArrayList<Integer> arrayRank = new ArrayList<Integer>();
		arrayRank.add(0);
		arrayRank.add(0);
		arrayRank.add(0);
		arrayRank.add(0);
		
		arrayRank.set(latRank, latSeq);
		arrayRank.set(lonRank, lonSeq);
		arrayRank.set(eleRank, eleSeq);
		index = data.getIndex();
		return data.getFloat(index.set(arrayRank.get(0),arrayRank.get(1),arrayRank.get(2)));
	}

	// find the extreme value for a variable
	private void findExtreme(Variable var) {
		int d1, d2, d3;
		// for 3D data
		float dataAtLocation;
		boolean flag = true;
		try {
			data = var.read();
			index = data.getIndex();
			for (int i = 0; i < var.getAttributes().size(); i++) {
				if (var.getAttributes().get(i).equals("missing_value")) {
					this.missing_value = var.getAttributes().get(i)
							.getNumericValue().floatValue();
					break;
				}

			}

			for (int i = 0; i < data.getSize(); i++) {

				if (data.getFloat(i) != this.missing_value
						&& !Float.isNaN(data.getFloat(i))) {
					dataAtLocation = data.getFloat(i);
					if (flag) {
						this.max = dataAtLocation;
						this.min = dataAtLocation;
						flag = false;
					}
					if (this.max < dataAtLocation)
						this.max = dataAtLocation;
					else if (this.min > dataAtLocation)
						this.min = dataAtLocation;
				}

			}
			//System.out.println("min" + this.min + ",max" + this.max);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void setVariable(String variable) {
		this.variableSel = this.ncDataSet.findVariable(variable); 
	}

	/*
	 * temporal information is included
	 */
	public boolean isTemporalIncluded() {
		return this.timeIncluded;
	}

	public static void main(String[] args) {
		// F:\workspace\test.nc
		NetCDFReaderReg ncReg = new NetCDFReaderReg(
				"F:\\workspace\\ncdata\\slim_100897_198.nc");
		//ncReg.readData("T");
		//ncReg.getValue(0, 10, 10, 1);

	}

}
