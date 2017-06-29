package edu.du.ogc.ais.examples.GUI;

import edu.du.ogc.ais.function.BarChartGenerator;
import edu.du.ogc.ais.function.LineChartGenerator;
import edu.du.ogc.ais.function.PieChartGenerator;
import edu.du.ogc.netcdf.NetCDFReader2D;
import edu.du.ogc.wcs.WCSService;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.SelectableIconLayer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.ogc.kml.KMLStyle;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.layertree.LayerTree;
import static gov.nasa.worldwindx.examples.ApplicationTemplate.insertBeforeCompass;
import static gov.nasa.worldwindx.examples.ApplicationTemplate.insertBeforePlacenames;
import gov.nasa.worldwindx.examples.ClickAndGoSelectListener;
import gov.nasa.worldwindx.examples.util.HotSpotController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
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
public class AISMainFrame extends javax.swing.JFrame {

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

    public AISMainFrame() {
        initComponents();
        ((Component) this.wwd).setPreferredSize(new java.awt.Dimension(700, 700));
//            wwd.setPreferredSize();
        this.jPanel1.setLayout(new BorderLayout());
        this.wwd.addSelectListener(new ClickAndGoSelectListener(this.wwd, WorldMapLayer.class));
        this.jPanel1.add((Component) wwd, java.awt.BorderLayout.CENTER);

        this.statusBar = new StatusBar();
        this.jPanel1.add(statusBar, BorderLayout.PAGE_END);
        this.statusBar.setEventSource(wwd);

        wwd.setModel(new BasicModel());

        this.layerTree = new LayerTree();

        // Set up a layer to display the on-screen layer tree in the WorldWindow.
        this.hiddenLayer = new RenderableLayer();
        this.hiddenLayer.addRenderable(this.layerTree);
        this.wwd.getModel().getLayers().add(this.hiddenLayer);

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
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jLabel3 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
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
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
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
        jMenu4 = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();
        Classification = new javax.swing.JMenuItem();
        DensityMap = new javax.swing.JMenuItem();
        Profile = new javax.swing.JMenuItem();
        TimeSeries = new javax.swing.JMenuItem();
        jMenu8 = new javax.swing.JMenu();
        jMenu7 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jToolBar1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setBorderPainted(false);
        jToolBar1.add(jSeparator3);

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/folder2.png"))); // NOI18N
        jToolBar1.add(jLabel3);

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/window2.png"))); // NOI18N
        jToolBar1.add(jLabel9);

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/save.png"))); // NOI18N
        jToolBar1.add(jLabel15);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/data.png"))); // NOI18N
        jToolBar1.add(jLabel1);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/draw.png"))); // NOI18N
        jToolBar1.add(jLabel4);

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/zoom in.png"))); // NOI18N
        jToolBar1.add(jLabel11);

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/zoom out.png"))); // NOI18N
        jToolBar1.add(jLabel13);

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/move.png"))); // NOI18N
        jToolBar1.add(jLabel12);

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/setting.png"))); // NOI18N
        jToolBar1.add(jLabel8);

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/search.png"))); // NOI18N
        jToolBar1.add(jLabel14);
        jToolBar1.add(jSeparator1);

        WFS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/feature.png"))); // NOI18N
        jToolBar1.add(WFS);

        WMS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/map2.png"))); // NOI18N
        jToolBar1.add(WMS);

        WCS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/coverage.png"))); // NOI18N
        jToolBar1.add(WCS);
        jToolBar1.add(jSeparator5);

        Profiler.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/pie.png"))); // NOI18N
        jToolBar1.add(Profiler);

        Classify.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/classify2.png"))); // NOI18N
        jToolBar1.add(Classify);

        Density.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/density.png"))); // NOI18N
        jToolBar1.add(Density);

        Time.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/time.png"))); // NOI18N
        jToolBar1.add(Time);
        jToolBar1.add(jSeparator6);

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/locate.png"))); // NOI18N
        jToolBar1.add(jLabel6);

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/map.png"))); // NOI18N
        jToolBar1.add(jLabel7);

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/sync.png"))); // NOI18N
        jToolBar1.add(jLabel5);

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/info.png"))); // NOI18N
        jToolBar1.add(jLabel10);

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
            .addGap(0, 1297, Short.MAX_VALUE)
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

        jMenu4.setText("Layer");
        jMenu4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu4ActionPerformed(evt);
            }
        });
        jMenuBar1.add(jMenu4);

        jMenu5.setText("Analysis");

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

        Profile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/pie.png"))); // NOI18N
        Profile.setText("Profiler");
        Profile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProfileActionPerformed(evt);
            }
        });
        jMenu5.add(Profile);

        TimeSeries.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/du/ogc/ais/examples/GUI/icons/time.png"))); // NOI18N
        TimeSeries.setText("Time Series");
        TimeSeries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TimeSeriesActionPerformed(evt);
            }
        });
        jMenu5.add(TimeSeries);

        jMenuBar1.add(jMenu5);

        jMenu8.setText("Security");
        jMenuBar1.add(jMenu8);

        jMenu7.setText("Help");
        jMenuBar1.add(jMenu7);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 1309, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
            String time = classpanel.getTimeStart(); //simplify this to get the start time only.
            String typename = "AIS_US";
            attribute = classpanel.getAttribute();
            String uniquevalues = classpanel.getAttributevalues();
            values = new ArrayList<>(Arrays.asList(uniquevalues.split(",")));
            try {
                counts = getWFSCount(url, typename, attribute, values);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        }

        dialog.dispose();

        //show charts from the paenl
        JDialog dialogchart = new JDialog(this, "Show Chart", true);
        Dimension dimensionchart = new Dimension(500, 500);
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
    }//GEN-LAST:event_DensityMapActionPerformed

    private void TimeSeriesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TimeSeriesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TimeSeriesActionPerformed

    private void ProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ProfileActionPerformed
        // TODO add your handling code here:

        String ncfile = "";
        String wfsfile = "";

        JDialog dialog = new JDialog(this, "Create Vertical Profile", true);
        ProfilerPanel profilerpanel = new ProfilerPanel();
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
        JDialog dialogchart = new JDialog(this, "Show Weather Along Routes", true);
        LineChartGenerator lineGenerator = new LineChartGenerator(ncfile, wfsfile);
        this.add(lineGenerator);
        Dimension dimensionchart = new Dimension(500, 400);
        dimensionchart.setSize(dimensionchart.getWidth() + 10, dimensionchart.getHeight() + 25);
        dialogchart.getContentPane().add(lineGenerator);
        dialogchart.setSize(dimensionchart);
        dialogchart.setModal(true);
        dialogchart.setVisible(true);
        dialogchart.pack();
    }//GEN-LAST:event_ProfileActionPerformed

    private void jMenu3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenu3ActionPerformed

    private void jMenuItemWMSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemWMSActionPerformed
        // TODO add your handling code here:
        JDialog dialog = new JDialog(this, "Import WCS layer", true);
        WMSPanel wmsPanel = new WMSPanel(this.wwd);
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
            iconlayer.setName(featureTypeName);
            iconlayer.setPickEnabled(true);

            AISMainFrame.insertBeforePlacenames(this.wwd, iconlayer);

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
        RenderableLayer layer = new RenderableLayer();
        layer.setName(nctest.GetVariableName());
        layer.setPickEnabled(false);
        layer.addRenderable(si1);

        this.insertBeforePlacenames(this.wwd, layer);

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
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenu jMenu8;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItemWCS;
    private javax.swing.JMenuItem jMenuItemWFS;
    private javax.swing.JMenuItem jMenuItemWMS;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator3;
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
}
