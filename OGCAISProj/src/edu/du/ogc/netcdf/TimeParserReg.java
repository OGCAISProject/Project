/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.du.ogc.netcdf;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import ucar.units.ParseException;

/**
 * 
 * @author jingli
 */
public class TimeParserReg {
	public static final String[] PATTERNS = { 
		"yyyy", 
		"yyyy-MM", 
		"yyyy-MM-dd",
		"yyyy-MM-dd HH:mm", 
		"yyyy-MM-dd HH:mm ZZZZ", 
		"yyyy-MM-dd HH:mm:ss",
		"yyyy-MM-dd HH:mm:ss ZZZZ",
		"yyyy-MM-dd HH",
		"yyyy-MM-dd HH:mm:ss.ff"};
	private static final String[] ISO_PATTERNS = {
			"yyyy-MM-dd'T'HH:mm:ss'Z'", 
			"yyyy-MM-dd'T'HH:mm:ssZ",
			"yyyy-MM-dd'T'HH:mm:ssZZZZ", 
			"yyyy-MM-dd'T'HH:mm:ss Z",
			"yyyy-MM-dd'T'HH:mm:ss ZZZZ", 
			"yyyy-MM-dd'T'HH:mm:ssz",
			"yyyy-MM-dd'T'HH:mm:ss z", 
			"yyyy-MM-dd'T'HH:mm:ss zzz",
			"yyyy-MM-dd'T'HH:mm:ss zzzz", 
			"yyyy-MM-dd'T'HH:mm:ss",
			"yyyy-MM-dd'T'HH:mm", 
			"yyyy-MM-dd HH:mm:ss'Z'",
			"yyyy-MM-dd HH:mm:ssZ", 
			"yyyy-MM-dd HH:mm:ssZZZZ",
			"yyyy-MM-dd HH:mm:ss Z", 
			"yyyy-MM-dd HH:mm:ss ZZZZ",
			"yyyy-MM-dd HH:mm:ssz", 
			"yyyy-MM-dd HH:mm:ss z",
			"yyyy-MM-dd HH:mm:ss zzz", 
			"yyyy-MM-dd HH:mm:ss zzzz",
			"yyyy-MM-dd HH:mm:ss", 
			"yyyy-MM-dd HH:mm", 
			"yyyy-MM-dd", 
			"yyyy-MM",
			"yyyy"
			};
	
	private static final String[] CALENDER={"360_day","365_day","366_day","noleap","no_leap","all_leap","gregorian","proleptic_gregorian","julian"};
	//for more types go to http://download.oracle.com/javase/1,5.0/docs/api/java/text/SimpleDateFormat.html
	//Era(G) AM/PM(a) TimeZone(Z)
	//calendarName = dataset.findGlobalAttribute("calendar").getStringValue().toLowerCase();
	String time;
	String increments;
	String[] timeString; 
	String dataPattern;
	String appendTime=""; //used to append additional info
	boolean reverseTime=false;
	boolean relative=false;
	public TimeParserReg(String timeDes) {
		time = timeDes;
		if (time.contains("since")) { //relative date
			this.ParseTimeRelative(time);
			this.relative = true;
		}
		
		 // if the time is absolute one read the data from the time array
		 //System.err.println("create");
	}

	public boolean isRelative()
	{
		return this.relative;
	}
	
	/*
	 * if it's availiable in the format of ...
	 * since... e.g. hours since 2008-1-6 0
	 */
	public void ParseTimeRelative(String time) {

		timeString = time.split(" ");
		this.createIncrements();
		this.createDateFormat();
		if(this.appendTime.length()<1)
		{
			
			if(this.time.split(this.GetTime(0)).length>1)
			this.appendTime=this.time.split(this.GetTime(0))[1];
			//System.out.println(this.time+","+this.GetTime(0)+"appendTime:"+appendTime);
		}
		if(this.reverseTime)
		{
			this.time="";
			for(int i=0 ;i < timeString.length; i++)
			{
				this.time = this.time +timeString[i]+" ";
				
			}
		}
	}

	public String GetTime(int incre)
	{
	   
		if(this.increments.equals("AddDay"))
		{
			return this.AddDay(incre)+appendTime;
		}
		else if(this.increments.equals("AddYear"))
		{
			return this.AddYear(incre)+appendTime;
		}
		else if(this.increments.equals("AddMonth"))
		{
			return this.AddMonth(incre)+appendTime;
		}
		else if(this.increments.equals("AddHour"))
		{
			return this.AddHour(incre)+appendTime;
		}
		else if(this.increments.equals("AddSecond"))
		{
			return this.AddSecond(incre)+appendTime;
		}
		else if(this.increments.equals("AddMiliSec"))
		{
			return this.AddMiliSec(incre)+appendTime;
		}
		else if(this.increments.equals("AddMinute"))
		{
			return this.AddMinute(incre)+appendTime;
		}
		return null;
		
	}
	
	/*
	 * 
	 */
	
	private void createIncrements()
	{
		if (timeString[0].startsWith("year")) {
			this.increments = "AddYear";
		} else if (timeString[0].startsWith("month")) {
			this.increments = "AddMonth";
		} else if (timeString[0].startsWith("day")) {
			this.increments = "AddDay";
		} else if (timeString[0].startsWith("hour")) {
			this.increments = "AddHour";
		} else if (timeString[0].startsWith("minute")) {
			this.increments = "AddMinute";
		} else if (timeString[0].startsWith("sec")) {
			this.increments = "AddSecond";
		} else if (timeString[0].startsWith("millisec")) {
			this.increments = "AddMiliSec";
		} else {
			System.out.println("Not supported");
		}
	}
	
	/*
	 * based on the time string guess the time format
	 * length+increments
	 */
	private void createDateFormat() {
		if(timeString.length==3)
		{
				if(!timeString[2].contains("T"))//with 'T'
				{
					String[] timeRestYMD = timeString[2].split("-");
					//check if it's reverse
					
					//	//seconds since 1-1-1970
					//07/05/100 23:51:03.850
					if(timeRestYMD.length==1)
					{
						//format: yyyy
						dataPattern = PATTERNS[0];
					}
					else if(timeRestYMD.length==2)
					{
						//format: yyyy-mm
						dataPattern = PATTERNS[1];
					}
					else if(timeRestYMD.length==3)
					{
						//format: yyyy-mm-dd
						if(timeRestYMD[2].length()==4)
						{
							this.reverseTime = true;
							timeString[2]=timeRestYMD[2]+ "-"+timeRestYMD[1]+"-"+timeRestYMD[0];
						}
					
						dataPattern = PATTERNS[2];
					}	
				}
				else 
				{
					String[] timeRest = timeString[2].split(":");
					if(timeRest.length==2)
					{
						//format: yyyy-MM-dd'T'HH:mm
						dataPattern = ISO_PATTERNS[10];
					}
					else if(timeRest.length==3)
					{
						//format: yyyy-MM-dd'T'HH:mm:ss
						dataPattern = ISO_PATTERNS[9];
					}
					//should consider Z but test if z is include or not the "addfunction" works 
				}
				
			
		}
		else if (timeString.length==4)
		{
			if(!timeString[2].contains("T"))//with 'T'
			{
				if(timeString[3].contains("."))
				{
				this.appendTime="."+timeString[3].split("\\.")[1];
				 this.dataPattern= PATTERNS[5];	
				}
				else {
					String[] timeRestHMS = timeString[3].split(":");
					if(timeRestHMS.length==1)
					{
						//format: HH
						dataPattern = PATTERNS[7];
					}
					else if(timeRestHMS.length==2)
					{
						//format: HH
						dataPattern = PATTERNS[3];
					}
					else if(timeRestHMS.length==3)
					{
						//format: HH
						dataPattern = PATTERNS[5];
					}
				}
				
				
				
			}
			else 
			{
				String[] timeRest = timeString[2].split(":");
				if(timeRest.length==2)
				{
					//format: yyyy-MM-dd'T'HH:mm
					dataPattern = ISO_PATTERNS[10];
				}
				else if(timeRest.length==3)
				{
					//format: yyyy-MM-dd'T'HH:mm:ss
					dataPattern = ISO_PATTERNS[9];
				}
			}
		}
		else if (timeString.length==5)
		{
			dataPattern = ISO_PATTERNS[20];
		}
	}
	


	// add days to an appoint date,then return the new date
	public String AddDay(int n) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(this.dataPattern);
			Calendar cd = Calendar.getInstance();
			cd.setTime(sdf.parse(this.time.split("since ")[1]));
			cd.add(Calendar.DATE, n);
			return sdf.format(cd.getTime());
		} catch (Exception e) {
			return null;
		}
	}

	// add months to an appoint date,then return the new date
	public String AddMonth(String s, int n) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(this.dataPattern);
			Calendar cd = Calendar.getInstance();
			cd.add(Calendar.MONTH, n);
			return sdf.format(cd.getTime());
		} catch (Exception e) {
			return null;
		}
	}

	// add years to an appoint date,then return the new date
	public String AddYear(int n) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(this.dataPattern);
			Calendar cd = Calendar.getInstance();
			cd.setTime(sdf.parse(this.time.split("since ")[1]));
			cd.add(Calendar.YEAR, n);
			return sdf.format(cd.getTime());
		} catch (Exception e) {
			return null;
		}
	}

	// add hours to an appoint date,then return the new date
	public String AddHour(int n) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(this.dataPattern);
			Calendar cd = Calendar.getInstance();
			cd.setTime(sdf.parse(this.time.split("since ")[1]));
			cd.add(Calendar.HOUR_OF_DAY, n);
			return sdf.format(cd.getTime());
		} catch (Exception e) {
			return null;
		}
	}
	
	// add hours to an appoint date,then return the new date
	public String AddMonth(int n) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(this.dataPattern);
			Calendar cd = Calendar.getInstance();
			cd.setTime(sdf.parse(this.time.split("since ")[1]));
			cd.add(Calendar.MONTH, n);
			return sdf.format(cd.getTime());
		} catch (Exception e) {
			return null;
		}
	}
	
	public String AddMinute(int n) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(this.dataPattern);
			Calendar cd = Calendar.getInstance();
			cd.setTime(sdf.parse(this.time.split("since ")[1]));
			cd.add(Calendar.MINUTE, n);
			return sdf.format(cd.getTime());
		} catch (Exception e) {
			return null;
		}
	}

	public String AddSecond(int n) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(this.dataPattern);
			Calendar cd = Calendar.getInstance();
			cd.setTime(sdf.parse(this.time.split("since ")[1]));
			cd.add(Calendar.SECOND, n);
			return sdf.format(cd.getTime());
		} catch (Exception e) {
			return null;
		}
	}
	
	public String AddMiliSec(int n) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(this.dataPattern);
			Calendar cd = Calendar.getInstance();
			cd.setTime(sdf.parse(this.time.split("since ")[1]));
			cd.add(Calendar.MILLISECOND, n);
			return sdf.format(cd.getTime());
		} catch (Exception e) {
			return null;
		}
	}
	
	
	public static void main(String[] args) {
		//hours since 1900-01-01 00:00:0.0--not supported
		//07/05/100 23:51:03.850
		TimeParserReg timeParser = new TimeParserReg("seconds since 1970-1-1");
		System.out.println(timeParser.GetTime(360000));
	}
}
