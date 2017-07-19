package edu.du.ogc.ais.examples.GUI;

import com.jogamp.opengl.util.FPSAnimator;
import edu.du.ogc.ais.function.BarChartGenerator;
import edu.du.ogc.ais.function.LineChartGenerator;
import edu.du.ogc.ais.function.PieChartGenerator;
import edu.du.ogc.ais.function.TrackDensityGenerator;
import edu.du.ogc.gml.GMLPointReader;
import edu.du.ogc.netcdf.NetCDFReader2D;
import edu.du.ogc.wcs.WCSService;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.SelectableIconLayer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.ogc.kml.KMLStyle;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ScreenImage;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.util.BasicDragger;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.layertree.LayerTree;
import gov.nasa.worldwindx.examples.ClickAndGoSelectListener;
import gov.nasa.worldwindx.examples.layermanager.LayerAndElevationManagerPanel;
import gov.nasa.worldwindx.examples.util.HotSpotController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import javax.imageio.ImageIO;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.opengl.GLAnimatorControl;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import si.xlab.gaea.core.event.FeatureSelectListener;
import si.xlab.gaea.core.layers.wfs.WFSGenericLayer;
import static si.xlab.gaea.core.layers.wfs.WFSGenericLayer.findFirstLinkedImage;
import si.xlab.gaea.core.layers.wfs.WFSService;
import si.xlab.gaea.core.layers.wfs.WFSServiceSimple;
import si.xlab.gaea.core.ogc.gml.GMLFeature;
import si.xlab.gaea.core.ogc.gml.GMLGeometry;
import si.xlab.gaea.core.ogc.gml.GMLPoint;
import si.xlab.gaea.core.ogc.kml.KMLStyleFactory;
import si.xlab.gaea.core.render.DefaultLook;
import si.xlab.gaea.core.render.SelectableIcon;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author xuantongwang
 */
public class AISMainFrame extends javax.swing.JFrame implements RenderingListener {

    /**
     * Creates new form Test2design
     */
    WorldWindow wwd = new WorldWindowGLCanvas();
    StatusBar statusBar;
    protected LayerTree layerTree;
    protected RenderableLayer hiddenLayer;
    protected HotSpotController controller;
    //maintain a list of wfs/wcs layers and the local path
    ArrayList<String> wfslayername = new ArrayList<String>();
    ArrayList<String> wfslayerfilepath = new ArrayList<String>();
    ArrayList<String> wcslayername = new ArrayList<String>();
    ArrayList<String> wcslayerfilepath = new ArrayList<String>();

    static RenderableLayer tracklayer = new RenderableLayer();
    static IconLayer iconsimplelayer = new IconLayer();
    static protected FPSAnimator animator;
    static protected Path trackpath;
    static protected int currentPos = 0;

    static ArrayList<Position> pathPositions = new ArrayList<Position>();
    static ArrayList<Position> pathPositionsAnimation = new ArrayList<Position>();

    public AISMainFrame() {
        initComponents();
        ((Component) this.wwd).setPreferredSize(new java.awt.Dimension(700, 600));
//            wwd.setPreferredSize();
        this.jPanel1.setLayout(new BorderLayout());
        this.wwd.addSelectListener(new ClickAndGoSelectListener(this.wwd, WorldMapLayer.class));
        this.wwd.addSelectListener(new FeatureSelectListener(this.wwd));

        this.jPanel1.add((Component) wwd, java.awt.BorderLayout.CENTER);

        this.statusBar = new StatusBar();
        this.jPanel1.add(statusBar, BorderLayout.PAGE_END);
        this.statusBar.setEventSource(wwd);

        wwd.setModel(new BasicModel());

        this.layerTree = new LayerTree();

        // Set up a layer to display the on-screen layer tree in the WorldWindow.
        this.hiddenLayer = new RenderableLayer();
        this.hiddenLayer.addRenderable(this.layerTree);
        this.hiddenLayer.setName("Layer Control");
        this.wwd.getModel().getLayers().add(this.hiddenLayer);

        //remove some of the layers that are not useful 
        // Mark the layer as hidden to prevent it being included in the layer tree's model. Including the layer in
        // the tree would enable the user to hide the layer tree display with no way of bringing it back.
        this.hiddenLayer.setValue(AVKey.HIDDEN, true);

        // Refresh the tree model with the WorldWindow's current layer list.
        this.layerTree.getModel().refresh(this.wwd.getModel().getLayers());

        // Add a controller to handle input events on the layer tree.
        this.controller = new HotSpotController(this.wwd);

        // Size the World Window to take up the space typically used by the layer panel. This illustrates the
        // screen space gained by using the on-screen layer tree.
//            Dimension size = new Dimension(1000, 600);
//            this.setPreferredSize(size);
        WWUtil.alignComponent(null, this, AVKey.CENTER);
       
        this.pack();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        OGCServiceMenuItem = new javax.swing.ButtonGroup();
        jSeparator4 = new javax.swing.JSeparator();
        jToolBar1 = new javax.swing.JToolBar();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        WFS = new javax.swing.JLabel();
        WMS = new javax.swing.JLabel();
        WCS = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        Profiler = new javax.swing.JLabel();
        Classify = new javax.swing.JLabel();
        Density = new javax.swing.JLabel();
        Time = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jLabel1 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jLabelSlow = new javax.swing.JLabel();
        jLabelPause = new javax.swing.JLabel();
        jLabelStart = new javax.swing.JLabel();
        jLabelStop = new javax.swing.JLabel();
        jLabelFast = new javax.swing.JLabel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton3 = new javax.swing.JToggleButton();
        jButton1 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        jMenuItemWFS = new javax.swing.JMenuItem();
        jMenuItemWCS = new javax.swing.JMenuItem();
        jMenuItemWMS = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        Classification = new javax.swing.JMenuItem();
        DensityMap = new javax.swing.JMenuItem();
        TimeSeries = new javax.swing.JMenuItem();
        Profile = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItemOrder = new javax.swing.JMenuItem();
        jMenuItemDelete = new javax.swing.JMenuItem();
        jMenu8 = new javax.swing.JMenu();
        jMenu7 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jToolBar1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setBorderPainted(false);
        jToolBar1.add(jSeparator1);

        WFS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/feature.png"))); // NOI18N
        WFS.setText(" ");
        WFS.setToolTipText("WFS");
        jToolBar1.add(WFS);

        WMS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/map2.png"))); // NOI18N
        WMS.setText(" ");
        WMS.setToolTipText("WMS");
        jToolBar1.add(WMS);

        WCS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/coverage.png"))); // NOI18N
        WCS.setText(" ");
        WCS.setToolTipText("WCS");
        jToolBar1.add(WCS);
        jToolBar1.add(jSeparator5);

        Profiler.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/pie.png"))); // NOI18N
        Profiler.setText(" ");
        Profiler.setToolTipText("Profiler");
        jToolBar1.add(Profiler);

        Classify.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/classify2.png"))); // NOI18N
        Classify.setText(" ");
        Classify.setToolTipText("Classification");
        jToolBar1.add(Classify);

        Density.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/density.png"))); // NOI18N
        Density.setText(" ");
        Density.setToolTipText("Density");
        jToolBar1.add(Density);

        Time.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/time.png"))); // NOI18N
        Time.setText(" ");
        Time.setToolTipText("Time Series");
        jToolBar1.add(Time);
        jToolBar1.add(jSeparator6);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/security.png"))); // NOI18N
        jLabel1.setToolTipText("Security");
        jToolBar1.add(jLabel1);
        jToolBar1.add(jSeparator2);

        jLabelSlow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/fast-rewind2.png"))); // NOI18N
        jLabelSlow.setText(" ");
        jLabelSlow.setEnabled(false);
        jLabelSlow.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelSlowMouseClicked(evt);
            }
        });
        jToolBar1.add(jLabelSlow);

        jLabelPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/pause2.png"))); // NOI18N
        jLabelPause.setText(" ");
        jLabelPause.setEnabled(false);
        jToolBar1.add(jLabelPause);

        jLabelStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/play2.png"))); // NOI18N
        jLabelStart.setText(" ");
        jLabelStart.setEnabled(false);
        jLabelStart.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelStartMouseClicked(evt);
            }
        });
        jToolBar1.add(jLabelStart);

        jLabelStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/Stop_1.png"))); // NOI18N
        jLabelStop.setText(" ");
        jLabelStop.setEnabled(false);
        jLabelStop.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelStopMouseClicked(evt);
            }
        });
        jToolBar1.add(jLabelStop);

        jLabelFast.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/fast-forward2.png"))); // NOI18N
        jLabelFast.setText(" ");
        jLabelFast.setEnabled(false);
        jLabelFast.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelFastMouseClicked(evt);
            }
        });
        jToolBar1.add(jLabelFast);

        jToggleButton1.setText("jToggleButton1");

        jToggleButton2.setText("jToggleButton2");

        jToggleButton3.setText("jToggleButton3");

        jButton1.setText("jButton1");

        jCheckBox1.setText("jCheckBox1");

        jCheckBox2.setText("jCheckBox2");

        jCheckBox3.setText("jCheckBox3");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1146, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 506, Short.MAX_VALUE)
        );

        jMenuBar1.setPreferredSize(new java.awt.Dimension(455, 32));

        jMenu3.setText("Service");
        jMenu3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu3ActionPerformed(evt);
            }
        });

        jMenuItemWFS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/feature.png"))); // NOI18N
        jMenuItemWFS.setText("WFS");
        jMenuItemWFS.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenuItemWFSMouseClicked(evt);
            }
        });
        jMenuItemWFS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemWFSActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemWFS);

        jMenuItemWCS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/coverage.png"))); // NOI18N
        jMenuItemWCS.setText("WCS");
        jMenuItemWCS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemWCSActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemWCS);

        jMenuItemWMS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/map2.png"))); // NOI18N
        jMenuItemWMS.setText("WMS");
        jMenuItemWMS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemWMSActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemWMS);

        jMenuBar1.add(jMenu3);

        jMenu5.setText("Analysis");
        jMenu5.setToolTipText("");

        Classification.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/classify2.png"))); // NOI18N
        Classification.setText("Classification");
        Classification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClassificationActionPerformed(evt);
            }
        });
        jMenu5.add(Classification);

        DensityMap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/density.png"))); // NOI18N
        DensityMap.setText("Density Map");
        DensityMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DensityMapActionPerformed(evt);
            }
        });
        jMenu5.add(DensityMap);

        TimeSeries.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/time.png"))); // NOI18N
        TimeSeries.setText("Tracking");
        TimeSeries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TimeSeriesActionPerformed(evt);
            }
        });
        jMenu5.add(TimeSeries);

        Profile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/pie.png"))); // NOI18N
        Profile.setText("Profiler");
        Profile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProfileActionPerformed(evt);
            }
        });
        jMenu5.add(Profile);

        jMenuBar1.add(jMenu5);

        jMenu4.setText("Layer");
        jMenu4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu4ActionPerformed(evt);
            }
        });

        jMenuItemOrder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/order_1.png"))); // NOI18N
        jMenuItemOrder.setText("Order");
        jMenuItemOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOrderActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItemOrder);

        jMenuItemDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/Remove_Delete_1.png"))); // NOI18N
        jMenuItemDelete.setText("Delete");
        jMenuItemDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDeleteActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItemDelete);

        jMenuBar1.add(jMenu4);

        jMenu8.setText("Security");
        jMenu8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenu8MouseClicked(evt);
            }
        });
        jMenu8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu8ActionPerformed(evt);
            }
        });
        jMenuBar1.add(jMenu8);

        jMenu7.setText("Help");
        jMenuBar1.add(jMenu7);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 1158, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private ArrayList<Integer> getWFSCount(String url, String featureTypeName, String queryField, ArrayList<String> queryvalues) {
        ArrayList<Integer> counts = new ArrayList<Integer>();

        for (String queryvalue : queryvalues) {
            WFSServiceSimple service = new WFSServiceSimple(url, featureTypeName, queryField, queryvalue, "hit");
            String filepath;
            try {
                filepath = service.downloadFeatures();
                int count = service.readGMLCount(filepath);
                counts.add(count);
            } catch (IOException ex) {
                Logger.getLogger(AISMainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        return counts;
    }

    public String AddHour(String s, int n) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cd = Calendar.getInstance();
            cd.setTime(sdf.parse(s));
            cd.add(Calendar.HOUR_OF_DAY, n);
            return sdf.format(cd.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    private void ClassificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClassificationActionPerformed
        // TODO add your handling code here:
        //create a dialog for the classfication panel 
        ArrayList<Integer> counts = new ArrayList<Integer>();
        ArrayList<String> values = new ArrayList<String>();
        String attribute = "";

        JDialog dialog = new JDialog(this, "Classify WFS Features", true);
        ClassificationPanel classpanel = new ClassificationPanel();
        classpanel.setDialog(dialog);
        Dimension dimension = classpanel.getPreferredSize();
        dimension.setSize(dimension.getWidth() + 10, dimension.getHeight() + 25);
        dialog.getContentPane().add(classpanel);
        dialog.setSize(dimension);
        dialog.setModal(true);
        dialog.setVisible(true);

        if (classpanel.isConfirmed()) {
            //to build a wcs request, url, variable, sector and time is needed
            String url = classpanel.getUrl();

            String typename = "AIS_US";
            if (classpanel.isBarChart()) {
                String starttime = classpanel.getTimeStart(); //simplify this to get the start time only.
                String endtime = classpanel.getTimeEnd();
                attribute = "timeConv";
                if (classpanel.isHourly()) {

                    int interval = (Integer.valueOf(endtime.substring(8, 10)) - Integer.valueOf(starttime.substring(8, 10))) * 24
                            + (Integer.valueOf(endtime.substring(11, 13)) - Integer.valueOf(starttime.substring(11, 13)));
                    for (int i = 0; i < interval - 1; i++) {
                        String starttimenew = AddHour(starttime, i);
                        String endtimenew = AddHour(starttime, i + 1);
                        values.add(starttimenew + "," + endtimenew);
                    }
                }
            } else {

                attribute = classpanel.getAttribute();
                String uniquevalues = classpanel.getAttributevalues();
                values = new ArrayList<>(Arrays.asList(uniquevalues.split(",")));

            }
            try {
                counts = getWFSCount(url, typename, attribute, values);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        }

        dialog.dispose();

        //show charts from the paenl
        JDialog dialogchart = new JDialog(this, "Show Chart", true);
        Dimension dimensionchart = new Dimension(500, 400);
        dimensionchart.setSize(dimensionchart.getWidth() + 10, dimensionchart.getHeight() + 25);
        if (classpanel.isBarChart()) {
            BarChartGenerator bcGenerator = new BarChartGenerator(values, counts, attribute);
            this.add(bcGenerator);
            dialogchart.getContentPane().add(bcGenerator);
        } else {

            PieChartGenerator pcGenerator = new PieChartGenerator(values, counts, attribute);
            this.add(pcGenerator);
            dialogchart.getContentPane().add(pcGenerator);
        }

        dialogchart.setSize(dimensionchart);
        dialogchart.setModal(true);
        dialogchart.setVisible(true);
        dialogchart.pack();


    }//GEN-LAST:event_ClassificationActionPerformed

    private void DensityMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DensityMapActionPerformed
        // TODO add your handling code here:
        JDialog dialog = new JDialog(this, "Show Track Density", true);
        DensityMapPanel densitymappanel = new DensityMapPanel();
        densitymappanel.setLayers(this.wfslayername);
        densitymappanel.setDialog(dialog);
        Dimension dimension = densitymappanel.getPreferredSize();
        dimension.setSize(dimension.getWidth() + 10, dimension.getHeight() + 25);
        dialog.getContentPane().add(densitymappanel);
        dialog.setSize(dimension);
        dialog.setModal(true);
        dialog.setVisible(true);

        if (densitymappanel.isConfirmed()) {
            //read gml points 
            int layeridx = densitymappanel.getLayerIndex();
            String wfspath = this.wfslayerfilepath.get(layeridx);
            TrackDensityGenerator tdviewer = new TrackDensityGenerator(wfspath);
            tdviewer.CreateDensityMap(wfspath.split(".xml")[0] + ".png", wfspath.split(".xml")[0] + "legend.png");
            //surfaceimage
            double[] bounds = tdviewer.getBounds(); //left, right, bottom, up
            SurfaceImage si1 = new SurfaceImage(wfspath.split(".xml")[0] + ".png", new ArrayList<LatLon>(Arrays.asList(
                    LatLon.fromDegrees(bounds[2], bounds[1]),
                    LatLon.fromDegrees(bounds[2], bounds[0]),
                    LatLon.fromDegrees(bounds[3], bounds[0]),
                    LatLon.fromDegrees(bounds[3], bounds[1])
            )));
            RenderableLayer layer = new RenderableLayer();
            layer.setName(this.wfslayername.get(layeridx) + " Animation");
            layer.setPickEnabled(false);
            layer.addRenderable(si1);
            layer.setOpacity(0.5f);
            this.insertBeforePlacenames(this.wwd, layer);
            this.wwd.getView().goTo(Position.fromDegrees(bounds[2], bounds[1]), 100000000);

        }

    }//GEN-LAST:event_DensityMapActionPerformed

    public void CreateAnimationLayer(String featureTypeName) {

        insertBeforePlacenames(this.wwd, iconsimplelayer);
        iconsimplelayer.setEnabled(true);
        iconsimplelayer.setName(featureTypeName);

        this.wwd.addSelectListener(new BasicDragger(this.wwd));

        // Create and set an attribute bundle.
        ShapeAttributes attrs = new BasicShapeAttributes();
        attrs.setOutlineMaterial(new Material(Color.RED));
        attrs.setOutlineWidth(2d);

        // Create a path, set some of its properties and set its attributes.
        trackpath = new Path(pathPositionsAnimation);
        trackpath.setAttributes(attrs);
        trackpath.setVisible(true);
        trackpath.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        trackpath.setPathType(AVKey.GREAT_CIRCLE);

        tracklayer.addRenderable(trackpath);

        // Add the layer to the model.
        this.insertBeforePlacenames(this.wwd, tracklayer);
        this.wwd.addRenderingListener(this);

    }

    private void TimeSeriesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TimeSeriesActionPerformed
        // TODO add your handling code here:
        JDialog dialog = new JDialog(this, "Show Ship Tracks", true);
        TimeSeriesPanel timeseriespanel = new TimeSeriesPanel(this.wwd);
        timeseriespanel.setLayers(this.wfslayername);
        timeseriespanel.setDialog(dialog);
        Dimension dimension = timeseriespanel.getPreferredSize();
        dimension.setSize(dimension.getWidth() + 10, dimension.getHeight() + 25);
        dialog.getContentPane().add(timeseriespanel);
        dialog.setSize(dimension);
        dialog.setModal(true);
        dialog.setVisible(true);

        if (timeseriespanel.isConfirmed()) {
            //read gml points 
            int layeridx = timeseriespanel.getLayerIndex();
            GMLPointReader gp = new GMLPointReader(this.wfslayerfilepath.get(layeridx));
            this.pathPositions = gp.GetPositions(gp.readGMLData());
            this.CreateAnimationLayer(this.wfslayername.get(layeridx) + " Tracking");
            this.jLabelFast.setEnabled(true);
            this.jLabelSlow.setEnabled(true);
            this.jLabelStop.setEnabled(true);
            this.jLabelStart.setEnabled(true);
            this.jLabelPause.setEnabled(true);
            animator = new FPSAnimator((WorldWindowGLCanvas) this.wwd, 15/*frames per second*/);

            animator.stop();
        }

    }//GEN-LAST:event_TimeSeriesActionPerformed

    private void ProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ProfileActionPerformed
        // TODO add your handling code here:

        String ncfile = "";
        String wfsfile = "";

        JDialog dialog = new JDialog(this, "Create Vertical Profile", true);
        ProfilerPanel profilerpanel = new ProfilerPanel();
        profilerpanel.setWFSLayer(this.wfslayername);
        profilerpanel.setWCSLayer(this.wcslayername);
        profilerpanel.setDialog(dialog);
        Dimension dimension = profilerpanel.getPreferredSize();
        dimension.setSize(dimension.getWidth() + 10, dimension.getHeight() + 25);
        dialog.getContentPane().add(profilerpanel);
        dialog.setSize(dimension);
        dialog.setModal(true);
        dialog.setVisible(true);

        if (profilerpanel.isConfirmed()) {
            //prefilter the wfs layer with route only
            wfsfile = this.wfslayerfilepath.get(profilerpanel.getWFSLayerIndex());
            ncfile = this.wcslayerfilepath.get(profilerpanel.getWCSLayerIndex());

        }

        dialog.dispose();

        //show charts from the paenl
        Dimension dimensionchart = new Dimension(520, 420);
        JDialog dialogchart = new JDialog(this, "Show Weather Along Routes", true);
        LineChartGenerator lineGenerator = new LineChartGenerator(ncfile, wfsfile, dimensionchart);
        lineGenerator.setPreferredSize(dimensionchart);

        this.add(lineGenerator);
        dimensionchart.setSize(dimensionchart.getWidth() + 10, dimensionchart.getHeight() + 25);
        dialogchart.getContentPane().add(lineGenerator);
        dialogchart.setSize(dimensionchart);
        dialogchart.setModal(true);
        dialogchart.setVisible(true);
        lineGenerator.revalidate();
        dialogchart.pack();
    }//GEN-LAST:event_ProfileActionPerformed

    private void jMenu3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenu3ActionPerformed

    private void jMenuItemWMSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemWMSActionPerformed
        // TODO add your handling code here:
        JDialog dialog = new JDialog(this, "Import WCS layer", true);
        WMSPanel wmsPanel = new WMSPanel(this.wwd, this.layerTree);
        wmsPanel.setDialog(dialog);
        Dimension dimension = wmsPanel.getPreferredSize();
        dimension.setSize(dimension.getWidth() + 10, dimension.getHeight() + 25);
        dialog.getContentPane().add(wmsPanel);
        dialog.setSize(dimension);
        dialog.setModal(true);
        dialog.setVisible(true);

        dialog.dispose();
    }//GEN-LAST:event_jMenuItemWMSActionPerformed

    private void jMenuItemWCSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemWCSActionPerformed
        // TODO add your handling code here:
        JDialog dialog = new JDialog(this, "Import WCS layer", true);
        WCSPanel wcsPanel = new WCSPanel();
        wcsPanel.setDialog(dialog);
        Dimension dimension = wcsPanel.getPreferredSize();
        dimension.setSize(dimension.getWidth() + 10, dimension.getHeight() + 25);
        dialog.getContentPane().add(wcsPanel);
        dialog.setSize(dimension);
        dialog.setModal(true);
        dialog.setVisible(true);

        if (wcsPanel.isConfirmed()) {
            //to build a wcs request, url, variable, sector and time is needed
            String url = wcsPanel.getUrl();
            String sector = wcsPanel.getSectorString();
            String time = wcsPanel.getTimeStart(); //simplify this to get the start time only.
            String var = wcsPanel.getVariable();
            try {
                addWcsLayer(url, sector, time, var);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        }

        dialog.dispose();
    }//GEN-LAST:event_jMenuItemWCSActionPerformed

    private void jMenuItemWFSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemWFSActionPerformed
        // TODO add your handling code here:
        JDialog dialog = new JDialog(this, "Import WFS layer", true);
        WFSPanel wfsPanel = new WFSPanel();
        wfsPanel.setDialog(dialog);
        Dimension dimension = wfsPanel.getPreferredSize();
        dimension.setSize(dimension.getWidth() + 10, dimension.getHeight() + 25);
        dialog.getContentPane().add(wfsPanel);
        dialog.setSize(dimension);
        dialog.setModal(true);
        dialog.setVisible(true);

        if (wfsPanel.isConfirmed()) {
            String url = wfsPanel.getUrl();
            String name = wfsPanel.getFeatureName();
            Sector sector = wfsPanel.getSector();
            double dist = wfsPanel.getVisibleDistance();
            Angle tile = wfsPanel.getTileDelta();

            String queryfield = wfsPanel.getQueryField();
            String queryvalue = wfsPanel.getQueryValue();
            if (wfsPanel.isShowAll()) {
                queryvalue = "#";
            }
            try {
                addWfsLayer(url, name, sector, tile, queryfield, queryvalue, dist * 1000);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        }

        dialog.dispose();
    }//GEN-LAST:event_jMenuItemWFSActionPerformed

    private void jMenuItemWFSMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItemWFSMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItemWFSMouseClicked

    private void jMenu4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu4ActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_jMenu4ActionPerformed

    private void jMenuItemOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOrderActionPerformed
        // TODO add your handling code here:

        JDialog dialog = new JDialog(this, "Layer Manger", true);
        LayerAndElevationManagerPanel layerManagerPanel = new LayerAndElevationManagerPanel(this.wwd);
        layerManagerPanel.setDialog(dialog);
        Dimension dimension = layerManagerPanel.getPreferredSize();
        dimension.setSize(dimension.getWidth() + 10, dimension.getHeight() + 25);
        dialog.getContentPane().add(layerManagerPanel);
        dialog.setSize(dimension);
        dialog.setModal(true);
        dialog.setVisible(true);
        this.pack();
        this.layerTree.getModel().refresh(this.wwd.getModel().getLayers());

    }//GEN-LAST:event_jMenuItemOrderActionPerformed

    private void jMenuItemDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDeleteActionPerformed
        // TODO add your handling code here:
        JDialog dialog = new JDialog(this, "Layer Delete", true);
        LayerDeletePanel layerDeletePanel = new LayerDeletePanel(this.wwd, this.layerTree);

        layerDeletePanel.setDialog(dialog);
        Dimension dimension = layerDeletePanel.getPreferredSize();
        dimension.setSize(dimension.getWidth() + 10, dimension.getHeight() + 25);
        dialog.getContentPane().add(layerDeletePanel);
        dialog.setSize(dimension);
        dialog.setModal(true);
        dialog.setVisible(true);
        if (layerDeletePanel.isConfirmed()) {
            ArrayList<String> layernames = layerDeletePanel.GetDeleteLayers();
            //remove layer
            LayerList layerlist = this.wwd.getModel().getLayers();
            for (Layer layer : layerlist) {
                if (layernames.contains(layer.getName())) {
                    this.wwd.getModel().getLayers().remove(layer);
                }
            }

        }
        dialog.dispose();
        this.layerTree.getModel().refresh(this.wwd.getModel().getLayers());

    }//GEN-LAST:event_jMenuItemDeleteActionPerformed

    private void jLabelStartMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelStartMouseClicked
        // TODO add your handling code here:

        this.animator.start();
    }//GEN-LAST:event_jLabelStartMouseClicked

    private void jLabelStopMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelStopMouseClicked
        // TODO add your handling code here:
        this.animator.stop();
        this.jLabelFast.setEnabled(false);
        this.jLabelSlow.setEnabled(false);
        this.jLabelStop.setEnabled(false);
        this.jLabelStart.setEnabled(false);
        this.jLabelPause.setEnabled(false);
        //TODO:clean everything and remove layer
    }//GEN-LAST:event_jLabelStopMouseClicked

    private void jLabelFastMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelFastMouseClicked
        // TODO add your handling code here:
//         this.animator.stop();
//         animator = new FPSAnimator((WorldWindowGLCanvas) this.wwd, 50/*frames per second*/);
//         this.animator.start();
        animator.setFPS(animator.getFPS() + 5);
    }//GEN-LAST:event_jLabelFastMouseClicked

    private void jLabelSlowMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelSlowMouseClicked
        // TODO add your handling code here:
        if (animator.getFPS() - 5 > 0) {
            animator.setFPS(animator.getFPS() - 5);
        }
    }//GEN-LAST:event_jLabelSlowMouseClicked

    private void jMenu8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu8ActionPerformed
        // TODO add your handling code here:

       
    }//GEN-LAST:event_jMenu8ActionPerformed

    private void jMenu8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenu8MouseClicked
        // TODO add your handling code here:
                JDialog dialog = new JDialog(this, "Security", true);
        SecurityPanel securityPanel = new SecurityPanel();

        securityPanel.setDialog(dialog);
        Dimension dimension = securityPanel.getPreferredSize();
        dimension.setSize(dimension.getWidth() + 10, dimension.getHeight() + 25);
        dialog.getContentPane().add(securityPanel);
        dialog.setSize(dimension);
        dialog.setModal(true);
        dialog.setVisible(true);
        dialog.dispose();
    }//GEN-LAST:event_jMenu8MouseClicked

    protected void addWfsLayer(String url, String featureTypeName, Sector sector, Angle tileDelta, String queryField, String queryValue, double maxVisibleDistance) {

        try {
            WFSServiceSimple service = new WFSServiceSimple(url, featureTypeName, queryField, queryValue, "");
            String filepath = service.downloadFeatures();

//            int cout = readGMLCount(filepath);
            KMLStyle style = new KMLStyle(DefaultLook.DEFAULT_FEATURE_STYLE);
            style.getLineStyle().setField("color", KMLStyleFactory.encodeColorToHex(Color.blue));
            style.getPolyStyle().setField("color", KMLStyleFactory.encodeColorToHex(Color.blue).replaceFirst("^ff", "80")); //semi-transparent fill
            SelectableIconLayer iconlayer = new SelectableIconLayer();
            iconlayer.setMaxActiveAltitude(maxVisibleDistance);

            List<GMLFeature> gmlfeatures = service.readGMLData(filepath);
            LatLon finalloc = null;
            for (GMLFeature gmlfeature : gmlfeatures) {
                GMLGeometry geometry;
                geometry = gmlfeature.getDefaultGeometry();
                if (geometry instanceof GMLPoint) {
                    GMLPoint gmlpoint = (GMLPoint) geometry;
                    LatLon loc = gmlpoint.getCentroid();
                    String desc = gmlfeature.buildDescription(null);
                    String imageURL = findFirstLinkedImage(desc);
                    SelectableIcon icon = new SelectableIcon(style,
                            new Position(geometry.getCentroid(), 0),
                            gmlfeature.getName(), desc, imageURL,
                            gmlfeature.getRelativeImportance(), true);

                    iconlayer.addIcon(icon);
                    finalloc = loc;

                }
            }
            //did not capture actions
            iconlayer.setEnabled(true);
            iconlayer.setName(featureTypeName + " " + queryField + ": " + queryValue);
            iconlayer.setPickEnabled(true);
            this.wfslayerfilepath.add(filepath);
            this.wfslayername.add(featureTypeName + " " + queryField + ": " + queryValue);

            AISMainFrame.insertBeforePlacenames(this.wwd, iconlayer);
            this.layerTree.getModel().refresh(this.wwd.getModel().getLayers());
            this.wwd.getView().goTo(Position.fromDegrees(finalloc.latitude.degrees, finalloc.longitude.degrees), maxVisibleDistance / 2);
        } catch (IOException ex) {
            Logger.getLogger(AISMainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected void addWcsLayer(String url, String sector, String time, String varname) {
        WCSService wcsservice = new WCSService(url);
        wcsservice.BuildCoverageURL(varname, time, sector);
        String path = wcsservice.downloadNetCDF();

        //add as a surface image
        NetCDFReader2D nctest = new NetCDFReader2D(path);

        nctest.ReadNetCDF();
        nctest.CreateImage(path.split(".nc")[0] + ".png", path.split(".nc")[0] + "legend.png");
        nctest.CloseNetCDF();

        SurfaceImage si1 = new SurfaceImage(path.split(".nc")[0] + ".png", new ArrayList<LatLon>(Arrays.asList(
                LatLon.fromDegrees(nctest.GetBottom(), nctest.GetRight()),
                LatLon.fromDegrees(nctest.GetBottom(), nctest.GetLeft()),
                LatLon.fromDegrees(nctest.GetUp(), nctest.GetLeft()),
                LatLon.fromDegrees(nctest.GetUp(), nctest.GetRight())
        )));

        ScreenImage screenImage = new ScreenImage();

        try {
            screenImage.setImageSource(ImageIO.read(new File(path.split(".nc")[0] + "legend.png")));
        } catch (IOException ex) {
            Logger.getLogger(AISMainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        RenderableLayer layer = new RenderableLayer();
        layer.setName(nctest.GetVariableName() + " " + time);
        this.wcslayerfilepath.add(path);
        this.wcslayername.add(nctest.GetVariableName() + " " + time);
        layer.setPickEnabled(false);
        layer.addRenderable(si1);
        layer.addRenderable(screenImage);
        screenImage.setScreenLocation(new Point(220, this.getHeight() - 150));
        this.wwd.getView().goTo(Position.fromDegrees(nctest.GetBottom(), nctest.GetRight()), 1000000);
        this.insertBeforePlacenames(this.wwd, layer);
        this.layerTree.getModel().refresh(this.wwd.getModel().getLayers());
    }

    public static void insertBeforePlacenames(WorldWindow wwd, Layer layer) {
        // Insert the layer into the layer list just before the placenames.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l instanceof PlaceNameLayer) {
                compassPosition = layers.indexOf(l);
            }
        }
        layers.add(compassPosition, layer);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AISMainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AISMainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AISMainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AISMainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AISMainFrame().setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem Classification;
    private javax.swing.JLabel Classify;
    private javax.swing.JLabel Density;
    private javax.swing.JMenuItem DensityMap;
    private javax.swing.ButtonGroup OGCServiceMenuItem;
    private javax.swing.JMenuItem Profile;
    private javax.swing.JLabel Profiler;
    private javax.swing.JLabel Time;
    private javax.swing.JMenuItem TimeSeries;
    private javax.swing.JLabel WCS;
    private javax.swing.JLabel WFS;
    private javax.swing.JLabel WMS;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelFast;
    private javax.swing.JLabel jLabelPause;
    private javax.swing.JLabel jLabelSlow;
    private javax.swing.JLabel jLabelStart;
    private javax.swing.JLabel jLabelStop;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenu jMenu8;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItemDelete;
    private javax.swing.JMenuItem jMenuItemOrder;
    private javax.swing.JMenuItem jMenuItemWCS;
    private javax.swing.JMenuItem jMenuItemWFS;
    private javax.swing.JMenuItem jMenuItemWMS;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

    private void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stageChanged(RenderingEvent event) {

        if (event.getStage().equals(RenderingEvent.BEFORE_RENDERING)) {
            if (this.animator.isAnimating()) {
                // The globe may not be instantiated the first time the listener is called.
                if (this.wwd.getView().getGlobe() == null) {
                    return;
                }

                if (currentPos < pathPositions.size() - 1) {
                    currentPos = currentPos + 1;
                } else {
                    currentPos = 0;
                    pathPositionsAnimation.removeAll(pathPositionsAnimation);

                    double distance = this.wwd.getView().getCenterPoint().distanceTo3(this.wwd.getView().getEyePoint());
                    this.wwd.getView().goTo(pathPositions.get(currentPos), distance);
                }

                iconsimplelayer.removeAllIcons();

                UserFacingIcon icon = new UserFacingIcon("src/images/pushpins/simple32.png", pathPositions.get(currentPos));
                icon.setSize(new Dimension(32, 32));
                iconsimplelayer.addIcon(icon);

                pathPositionsAnimation.add(pathPositions.get(currentPos));
                trackpath.setPositions(pathPositionsAnimation);
//                System.err.println(pathPositionsAnimation.size());
            }
        }
    }
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

}
