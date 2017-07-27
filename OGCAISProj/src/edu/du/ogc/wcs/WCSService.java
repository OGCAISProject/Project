/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.du.ogc.wcs;

import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.Logging;
import static gov.nasa.worldwind.util.Logging.logger;
import gov.nasa.worldwind.util.WWIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import si.xlab.gaea.core.layers.wfs.AbstractWFSLayer;
import gov.nasa.worldwind.ogc.wcs.wcs100.*;
import java.util.ArrayList;

/**
 *
 * @author Jing
 */
public class WCSService extends WWObjectImpl {

    private  String service="";
    private  String dataset ="";
    private  String urlBase=""; //full URL of a data ending with "&BBOX=", such that only the coordinates need to be appended
    private  String fileCachePath="";
    private  String urlService="";
    private String localfilepath="";
    private ArrayList<String> variablelist = new ArrayList<String>();
    private ArrayList<String> varidesclist=new ArrayList<String>();

    public WCSService(String url, Sector sector) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    protected static class CoverageInfo {

        protected WCS100Capabilities caps;
        protected AVListImpl params = new AVListImpl();

        protected String getTitle() {
            return params.getStringValue(AVKey.DISPLAY_NAME);
        }

        protected String getName() {
            return params.getStringValue(AVKey.COVERAGE_IDENTIFIERS);
        }
    }

    public WCSService(String service)
    {
         this.service = service;
       


    }
    
    public void BuildCoverageURL( String dataset, String date, String bounds) {
          StringBuilder urlBase = new StringBuilder(this.service);
        if (!this.service.endsWith("?") && !this.service.endsWith("&")) {
            if (this.service.contains("?")) {
                urlBase.append("&");
            } else {
                urlBase.append("?");
            }
        }
        urlBase.append("Service=WCS");
        this.urlBase = urlBase.toString();

        URI mapRequestURI = null;
        try {
            mapRequestURI = new URI(service);
        } catch (URISyntaxException e) {
            String message = Logging.getMessage(
                    "WFSService.URISyntaxException: ", e);

            Logging.logger().severe(message);
        }
        
         if (mapRequestURI != null) {
            this.fileCachePath = WWIO.formPath(mapRequestURI.getAuthority(),
                    mapRequestURI.getPath(), dataset, date);
        } else {
            this.fileCachePath = WWIO.formPath(service, dataset, date);
        }

//        http://sdf.ndbc.noaa.gov/thredds/wcs/hfradar_usegc_1km
//        this.service = service;
//        this.dataset = dataset;

        
//        http://sdf.ndbc.noaa.gov/thredds/wcs/hfradar_usegc_1km?service=WCS&request=GetCoverage&version=1.0.0\
//&coverage=u&bbox=-98,21,-57,47&time=2017-05-08T00:00:00Z&format=NetCDF3
     

        urlBase.append("&request=GetCoverage&version=1.0.0");
        urlBase.append("&coverage=");
        urlBase.append(dataset);

        urlBase.append("&bbox=");
        urlBase.append(bounds);
        urlBase.append("&time=");
        urlBase.append(date);
        urlBase.append("&format=NetCDF3");
        this.urlService = urlBase.toString();
        System.out.println(this.urlService);
    }

    protected CoverageInfo createCoverageInfo(WCS100Capabilities caps, WCS100CoverageOfferingBrief coverage) {
        // Create the layer info specified by the coverage capabilities.

        CoverageInfo info = new CoverageInfo();
        info.caps = caps;
        info.params = new AVListImpl();
        info.params.setValue(AVKey.COVERAGE_IDENTIFIERS, coverage.getName());
        info.params.setValue(AVKey.DISPLAY_NAME, coverage.getLabel());

        return info;
    }

    public void parseCapablities() {
        WCS100Capabilities caps;

        try {
            caps = WCS100Capabilities.retrieve(new URI(service));
            caps.parse();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        final java.util.List<WCS100CoverageOfferingBrief> coverages = caps.getContentMetadata().getCoverageOfferings();
        if (coverages == null) {
            return;
        }

        try {
            for (WCS100CoverageOfferingBrief coverage : coverages) {

                CoverageInfo coverageInfo = this.createCoverageInfo(caps, coverage);

                AVList configParams = coverageInfo.params.copy();
//                  String describeCoverageUrlString = caps.getCapability().getGetOperationAddress("DescribeCoverage");
//            URI uri = new URI(describeCoverageUrlString);
//            WCS100DescribeCoverage coverageDescription = WCS100DescribeCoverage.retrieve(uri, coverageInfo.getName());
//            coverageDescription.parse();
//            configParams.setValue(AVKey.DOCUMENT, coverageDescription);
                String varname = String.valueOf(coverageInfo.params.getValue(AVKey.DISPLAY_NAME)); //variable name
               
                if (varname.contains("lat") || varname.contains("lon")) {
                    continue;
                } else {
                    this.variablelist.add(String.valueOf(coverageInfo.params.getValue(AVKey.COVERAGE_IDENTIFIERS)));
                    this.varidesclist.add(varname);
                }
//            coverageInfo.params.getValue(AVKey.DISPLAY_NAME); //variable description

            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Fill the panel with the coverage titles.
    }

    public ArrayList<String> GetCoverageVariable() {
        return this.variablelist;
    }
    
     public ArrayList<String> GetCoverageVariableDesc() {
        return this.varidesclist;
    }
     

    public String downloadNetCDF() {

        try {
            URL url = new URL(this.urlService);
            if (!WorldWind.getRetrievalService().isAvailable()) {
                return null;
            }

            Retriever retriever;
            DownloadPostProcessor postProcessor;
            if ("http".equalsIgnoreCase(url.getProtocol()) || "https".equalsIgnoreCase(url.getProtocol())) {
                postProcessor = new DownloadPostProcessor(this);
                retriever = new HTTPRetriever(url, postProcessor);
            } else {

                return null;
            }

            // Apply any overridden timeouts.
            Integer cto = AVListImpl.getIntegerValue(this, AVKey.URL_CONNECT_TIMEOUT);

            if (cto != null && cto > 0) {
                retriever.setConnectTimeout(cto);
            }
            Integer cro = AVListImpl.getIntegerValue(this, AVKey.URL_READ_TIMEOUT);

            if (cro != null && cro > 0) {
                retriever.setReadTimeout(cro);
            }
            Integer srl = AVListImpl.getIntegerValue(this,
                    AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);

            if (srl != null && srl > 0) {
                retriever.setStaleRequestLimit(srl);
            }
            retriever.call();

            java.net.URL filepath;
            filepath = WorldWind.getDataFileStore().findFile(this.fileCachePath + ".nc", false);

            return filepath.getPath();

        } catch (MalformedURLException ex) {
            Logger.getLogger(WCSService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(WCSService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    private static class DownloadPostProcessor implements RetrievalPostProcessor {

        final WCSService wcsservice;
        protected boolean success = false;
        protected String errorMessage = "Post-processor not yet called";

        private DownloadPostProcessor(WCSService wcsservice) {
            this.wcsservice = wcsservice;
        }

        private void saveBuffer(java.nio.ByteBuffer buffer, java.io.File outFile) throws java.io.IOException {

            {
                WWIO.saveBuffer(buffer, outFile);
            }
        }

        @Override
        public ByteBuffer run(Retriever retriever) {

            if (retriever == null) {
                String msg = Logging.getMessage("nullValue.RetrieverIsNull");

                throw new IllegalArgumentException(msg);
            }

            try {
                if (!retriever.getState().equals(
                        Retriever.RETRIEVER_STATE_SUCCESSFUL)) {
                    return null;
                }

                URLRetriever r = (URLRetriever) retriever;
                ByteBuffer buffer = r.getBuffer();

                if (retriever instanceof HTTPRetriever) {
                    HTTPRetriever htr = (HTTPRetriever) retriever;

                    if (htr.getResponseCode()
                            == java.net.HttpURLConnection.HTTP_NO_CONTENT) {
                        // Mark tile as missing to avoid further attempts

                        return null;
                    } else if (htr.getResponseCode()
                            != java.net.HttpURLConnection.HTTP_OK) {
                        // Also mark tile as missing, but for an unknown reason.

                        return null;
                    }
                }

                final java.io.File outFile = WorldWind.getDataFileStore().newFile(this.wcsservice.fileCachePath + ".nc");

                if (outFile == null) {
                    return null;
                }

                if (outFile.exists()) {
                    return buffer;
                } // info is already here; don't need to do anything

                if (buffer != null) {
                    String contentType = retriever.getContentType();

                    // System.out.println("placenamelayer content type: "+contentType);
                    if (contentType == null) {
                        // TODO: logger message
                        return null;
                    }

                    this.saveBuffer(buffer, outFile);
                    success = true;
                    return buffer;
                }
            } catch (java.io.IOException e) {

            }

            return null;
        }

    }
//
    public static void main(String[] args) {
        WCSService wcsservice = new WCSService("http://sdf.ndbc.noaa.gov/thredds/wcs/hfradar_usegc_1km");
       
        wcsservice.parseCapablities();
       String varname =  wcsservice.GetCoverageVariable().get(0);
         wcsservice.BuildCoverageURL("u", "2017-05-08T00:00:00Z", "-60,21,-57,47");
        String path = wcsservice.downloadNetCDF();
        System.out.println(path);
    }

}
