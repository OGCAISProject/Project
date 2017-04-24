/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.du.ogc.netcdf;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author jingli
 */
public class TimeParser {

    String timeStr;

    public TimeParser(String dimension) {
        timeStr = dimension;
    }

    public ArrayList<String> ParseTimeStr() {
        ArrayList<String> retVal = new ArrayList<String>();
        
        String[] strPeriod = timeStr.split(","); // get time series for each period
        for (int i = 0; i < strPeriod.length; i++) {
            String[] strArray = strPeriod[i].split("/");
            String strRule = strArray[strArray.length - 1];
            if(strRule.length()>11)//.charAt(10)).equals("T")||String.valueOf(strRule.charAt(10)).equals(""))
            {
            	System.out.println("strRule"+strRule);
            	String beginDate = this.CorrectDate(strArray[0]);
                String endDate = this.CorrectDate(strArray[1]);
                beginDate =beginDate.substring(0,10);
                endDate =endDate.substring(0,10);
	    		String afterAdd = this.AddDay(beginDate, -1);
    		 if(String.valueOf(strRule.charAt(strRule.length()-1)).equals("Z")){
    			 while (!afterAdd.equals(endDate)) {
                     afterAdd = this.AddDay(afterAdd, 1);    
                     for (int j = 0; j<24; j++)
                     {
                    	 String newDateString = null;
                    	 if (j<10){
                    		 newDateString =afterAdd+"T"+"0"+Integer.toString(j)+":00:00Z";
                        	 retVal.add(newDateString);
                        	
                    	 }
                    	 else if(j>=10){
                    		 newDateString =afterAdd+"T"+Integer.toString(j)+":00:00Z";
                        	 retVal.add(newDateString);
                    	 }
                     }
                    
                    
                    
    		 }
    			
    	
            }
               
    		 else{
    			 while (!afterAdd.equals(endDate)) {
                     afterAdd = this.AddDay(afterAdd, 1);
                    String newDateString =afterAdd+" 00:00:00";
                     retVal.add(newDateString);
    		 }
            }
            }

            else{
            	if (String.valueOf(strRule.charAt(0)).equals("P")) {
                    char c = strRule.charAt(1);
                    if (!String.valueOf(c).equals("T")) ////if it is not time,just date
                    {
                        String DMY = this.GetDMYFromRule(strRule);
                        int number = this.GetNumFromRule(strRule);

                        String beginDate = this.CorrectDate(strArray[0]);
                        String endDate = this.CorrectDate(strArray[1]);
                        if (DMY.equals("D")) {

                            String afterAdd = this.AddDay(beginDate, -number);
                            // String endDate = this.CorrectDate(strArray[1]);
                            while (!afterAdd.equals(endDate)) {
                                afterAdd = this.AddDay(afterAdd, number);
                                retVal.add(afterAdd);
                            }

                        }
                        if (DMY.equals("M")) {

                            String afterAdd = this.AddMonth(beginDate, -number);
                            // String endDate = strArray[1];
                            while (!afterAdd.equals(endDate)) {
                                afterAdd = this.AddMonth(afterAdd, number);
                                retVal.add(afterAdd);
                            }

                        }
                        if (DMY.equals("Y")) {

                            String afterAdd = this.AddYear(beginDate, -number);
                            // String endDate = strArray[1];
                            //if(afterAdd!=null)
                            //{
                            while (!afterAdd.equals(endDate)) {
                                afterAdd = this.AddYear(afterAdd, number);
                                retVal.add(afterAdd);
                            }
                        // }

                        }

                    } //end of !=T
                    else //if =T , need to modify.
                    {
                        retVal.add(strArray[0]);
                        retVal.add(strArray[1]);
                    }

                } //end of ==P
                else {
                    if (!strPeriod[i].contains("/")) {
                        retVal.add(strPeriod[i]);
                    } else {
                        for (int a = 0; a < strArray.length; a++) {
                            retVal.add(strArray[a]);
                        }
                    }

                }
            }

        }
      
       
        return retVal;

    }

    //add days to an appoint date,then return the new date
    public String AddDay(String s, int n) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cd = Calendar.getInstance();
            cd.setTime(sdf.parse(s));
            cd.add(Calendar.DATE, n);
            return sdf.format(cd.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    //add months to an appoint date,then return the new date
    public String AddMonth(String s, int n) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cd = Calendar.getInstance();
            cd.setTime(sdf.parse(s));
            cd.add(Calendar.MONTH, n);
            return sdf.format(cd.getTime());
        } catch (Exception e) {
            return null;
        }
    }
    //add years to an appoint date,then return the new date
    public String AddYear(String s, int n) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cd = Calendar.getInstance();
            cd.setTime(sdf.parse(s));
            cd.add(Calendar.YEAR, n);
            return sdf.format(cd.getTime());
        } catch (Exception e) {
            return null;
        }
    }
    //add hours to an appoint date,then return the new date
    public String AddHour(String s, int n) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
            Calendar cd = Calendar.getInstance();
            cd.setTime(sdf.parse(s));
            cd.add(Calendar.HOUR_OF_DAY, n);
            return sdf.format(cd.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    private int GetNumFromRule(String strRule) // Suppose that the number is smaller than 1000
    {
        if (strRule.length() == 3) {
            String strNum = String.valueOf(strRule.charAt(1));
            return Integer.parseInt(strNum);
        } else {
            char c = strRule.charAt(1);
            if (String.valueOf(c).equals("T")) {
                if (strRule.length() == 4) {
                    String strNum = String.valueOf(strRule.charAt(2));
                    return Integer.parseInt(strNum);
                } else if (strRule.length() == 5) //=5
                {
                    String strNum = String.valueOf(strRule.charAt(2)) + String.valueOf(strRule.charAt(3));
                    return Integer.parseInt(strNum);
                } else {
                    String strNum = String.valueOf(strRule.charAt(2)) + String.valueOf(strRule.charAt(3)) + String.valueOf(strRule.charAt(4));
                    return Integer.parseInt(strNum);
                }


            }//end of T
            else {
                if (strRule.length() == 4) {
                    String strNum = String.valueOf(strRule.charAt(1)) + String.valueOf(strRule.charAt(2));
                    return Integer.parseInt(strNum);
                } else //=5
                {
                    String strNum = String.valueOf(strRule.charAt(1)) + String.valueOf(strRule.charAt(2)) + String.valueOf(strRule.charAt(3));
                    return Integer.parseInt(strNum);
                }
            }

        }


    }

    private String GetDMYFromRule(String strRule) {
        return String.valueOf(strRule.charAt(strRule.length() - 1));
    }

    //if mouth>12 then =12.Now,it just support mouth check.
    private String CorrectDate(String dateStr) {
        String newDateStr = dateStr;
        String[] tem = dateStr.split("-");
        if (tem.length == 1) {
            return newDateStr;
        }
        int month = Integer.valueOf(tem[1]);
        if (month > 12) {
            if (tem.length == 3) {
                newDateStr = tem[0] + "-" + "12" + "-" + tem[2];
            } else {
                newDateStr = tem[0] + "-" + "12";
            }
        }
        return newDateStr;
    }
}
