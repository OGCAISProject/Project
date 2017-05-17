/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * WfsPanel.java
 *
 * Created on Jun 21, 2013, 2:14:11 PM
 */
package edu.du.ogc.ais.examples;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import java.awt.Color;
import javax.swing.JColorChooser;
import javax.swing.JDialog;

/**
 *
 * @author Marko
 */
public class WCSLayerPanel extends javax.swing.JPanel
{
    private Color color = new Color(19, 158, 254);
	private String lineLabelTag = null;
    private JDialog dialog;
    private boolean confirmed = false;

    /** Creates new form WfsPanel */
    public WCSLayerPanel()
    {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sectorGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        serviceUrl = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        sectorGlobal = new javax.swing.JRadioButton();
        sectorCustom = new javax.swing.JRadioButton();
        sectorLbl1 = new javax.swing.JLabel();
        sectorLatFrom = new javax.swing.JTextField();
        sectorLbl2 = new javax.swing.JLabel();
        sectorLatTo = new javax.swing.JTextField();
        sectorLbl3 = new javax.swing.JLabel();
        sectorLonFrom = new javax.swing.JTextField();
        sectorLbl4 = new javax.swing.JLabel();
        sectorLonTo = new javax.swing.JTextField();
        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jRadioButton1 = new javax.swing.JRadioButton();

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Import WCS features"); // NOI18N

        jLabel2.setText("Service URL:"); // NOI18N

        serviceUrl.setText("http://demo.luciad.com:8080/OgcAisServices/wfs?"); // NOI18N
        serviceUrl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serviceUrlActionPerformed(evt);
            }
        });

        jLabel4.setText("Sector:"); // NOI18N

        sectorGroup.add(sectorGlobal);
        sectorGlobal.setText("Global"); // NOI18N
        sectorGlobal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sectorGlobalActionPerformed(evt);
            }
        });

        sectorGroup.add(sectorCustom);
        sectorCustom.setSelected(true);
        sectorCustom.setText("Custom:"); // NOI18N
        sectorCustom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sectorCustomActionPerformed(evt);
            }
        });

        sectorLbl1.setText("Latitude  ->  From:"); // NOI18N

        sectorLatFrom.setText("39"); // NOI18N

        sectorLbl2.setText("To:"); // NOI18N

        sectorLatTo.setText("42"); // NOI18N
        sectorLatTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sectorLatToActionPerformed(evt);
            }
        });

        sectorLbl3.setText("Longitude  ->  From:"); // NOI18N

        sectorLonFrom.setText("-74"); // NOI18N

        sectorLbl4.setText("To:"); // NOI18N

        sectorLonTo.setText("-70"); // NOI18N

        btnOk.setText("OK"); // NOI18N
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel"); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("edu/du/ogc/ais/examples/Bundle"); // NOI18N
        jLabel7.setText(bundle.getString("WCSLayerPanel.jLabel7.text")); // NOI18N

        jTextField1.setText(bundle.getString("WCSLayerPanel.jTextField1.text")); // NOI18N

        jLabel8.setText(bundle.getString("WCSLayerPanel.jLabel8.text")); // NOI18N

        jLabel10.setText(bundle.getString("WCSLayerPanel.jLabel10.text")); // NOI18N

        jTextField2.setText(bundle.getString("WCSLayerPanel.jTextField2.text")); // NOI18N

        jCheckBox1.setText(bundle.getString("WCSLayerPanel.jCheckBox1.text")); // NOI18N

        jCheckBox2.setText(bundle.getString("WCSLayerPanel.jCheckBox2.text")); // NOI18N

        jRadioButton1.setText(bundle.getString("WCSLayerPanel.jRadioButton1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(serviceUrl))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnOk, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(35, 35, 35)
                        .addComponent(jTextField2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(sectorLbl1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(sectorLatFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(sectorLbl2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sectorLatTo, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel8)
                            .addGap(29, 29, 29)
                            .addComponent(jTextField1))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel4)
                                .addComponent(sectorCustom)
                                .addComponent(jLabel1)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel7)
                                    .addGap(18, 18, 18)
                                    .addComponent(jCheckBox1)
                                    .addGap(18, 18, 18)
                                    .addComponent(jCheckBox2))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(sectorGlobal)
                                    .addGap(26, 26, 26)
                                    .addComponent(jRadioButton1))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(sectorLbl3)
                                    .addGap(57, 57, 57)
                                    .addComponent(sectorLonFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(sectorLbl4)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(sectorLonTo, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(0, 0, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(serviceUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sectorGlobal)
                    .addComponent(jRadioButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sectorCustom)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sectorLbl1)
                    .addComponent(sectorLatFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sectorLbl2)
                    .addComponent(sectorLatTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sectorLbl3)
                    .addComponent(sectorLonFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sectorLbl4)
                    .addComponent(sectorLonTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jCheckBox1)
                    .addComponent(jCheckBox2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(btnCancel))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sectorCustomActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sectorCustomActionPerformed
    {//GEN-HEADEREND:event_sectorCustomActionPerformed
        if (sectorCustom.isSelected()) {
            sectorLatFrom.setEnabled(true);
            sectorLatTo.setEnabled(true);
            sectorLonFrom.setEnabled(true);
            sectorLonTo.setEnabled(true);
            sectorLbl1.setEnabled(true);
            sectorLbl2.setEnabled(true);
            sectorLbl3.setEnabled(true);
            sectorLbl4.setEnabled(true);
        }
    }//GEN-LAST:event_sectorCustomActionPerformed

    private void sectorGlobalActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sectorGlobalActionPerformed
    {//GEN-HEADEREND:event_sectorGlobalActionPerformed
        if (sectorGlobal.isSelected()) {
            sectorLatFrom.setEnabled(false);
            sectorLatTo.setEnabled(false);
            sectorLonFrom.setEnabled(false);
            sectorLonTo.setEnabled(false);
            sectorLbl1.setEnabled(false);
            sectorLbl2.setEnabled(false);
            sectorLbl3.setEnabled(false);
            sectorLbl4.setEnabled(false);
        }
    }//GEN-LAST:event_sectorGlobalActionPerformed

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnOkActionPerformed
    {//GEN-HEADEREND:event_btnOkActionPerformed
        if (dialog != null) {
            confirmed = true;
            dialog.setVisible(false);
        }
    }//GEN-LAST:event_btnOkActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnCancelActionPerformed
    {//GEN-HEADEREND:event_btnCancelActionPerformed
        if (dialog != null) {
            confirmed = false;
            dialog.setVisible(false);
        }
    }//GEN-LAST:event_btnCancelActionPerformed

    private void sectorLatToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sectorLatToActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sectorLatToActionPerformed

    private void serviceUrlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serviceUrlActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_serviceUrlActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JRadioButton sectorCustom;
    private javax.swing.JRadioButton sectorGlobal;
    private javax.swing.ButtonGroup sectorGroup;
    private javax.swing.JTextField sectorLatFrom;
    private javax.swing.JTextField sectorLatTo;
    private javax.swing.JLabel sectorLbl1;
    private javax.swing.JLabel sectorLbl2;
    private javax.swing.JLabel sectorLbl3;
    private javax.swing.JLabel sectorLbl4;
    private javax.swing.JTextField sectorLonFrom;
    private javax.swing.JTextField sectorLonTo;
    private javax.swing.JTextField serviceUrl;
    // End of variables declaration//GEN-END:variables

    public void setDialog(JDialog dialog)
    {
        this.dialog = dialog;
    }
    
    public boolean isConfirmed()
    {
        return confirmed;
    }
    
    public String getUrl()
    {
        return serviceUrl.getText();
    }
    
  
    
    public Sector getSector()
    {
        Sector rv = null;
        if (sectorCustom.isSelected()) {
            float minLat = Float.parseFloat(sectorLatFrom.getText());
            float maxLat = Float.parseFloat(sectorLatTo.getText());
            float minLon = Float.parseFloat(sectorLonFrom.getText());
            float maxLon = Float.parseFloat(sectorLonTo.getText());
            rv = new Sector(Angle.fromDegreesLatitude(minLat), 
                    Angle.fromDegreesLatitude(maxLat), 
                    Angle.fromDegreesLongitude(minLon), 
                    Angle.fromDegreesLongitude(maxLon));
        } else {
            rv = Sector.FULL_SPHERE;
        }
        
        return rv;
    }
    
 
    public Color getColor()
    {
        return color;
    }
}
