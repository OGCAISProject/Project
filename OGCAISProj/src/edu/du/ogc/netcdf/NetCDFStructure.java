package edu.du.ogc.netcdf;

public class NetCDFStructure {
	double lat;
	double lon;
	double elevation;
	float value;
	
	public NetCDFStructure(double lat, double lon, double elevation, float value)
	{
		this.lat = lat;
		this.lon = lon;
		this.elevation = elevation;
		this.value =value;
	}

	public double getLat()
	{
		return this.lat;
	}
	
	public double getLon()
	{
		return this.lon;
	}
	
	
	public double getElevation()
	{
		return this.elevation;
	}
	
	public float getValue()
	{
		return this.value;
	}
}
