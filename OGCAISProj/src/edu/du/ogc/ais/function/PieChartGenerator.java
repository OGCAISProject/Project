/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.du.ogc.ais.function;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.Group;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;

/**
 *
 * @author Jing
 */
public class PieChartGenerator extends JFXPanel {

 ArrayList<String> aistypes;
    ArrayList<Integer> aisvalues;
    String charttitle="";
//  wfs:  resultype=hits
    //read info from file or stream?
    //need to join data from different tables
    //example 1: different types of tracks
    //example 2: # of within a query window (e.g,. time series)


    public PieChartGenerator(ArrayList<String> aistypes,ArrayList<Integer> aisvalues, String title ) {

        super();
        this.aistypes= aistypes;
        this.aisvalues =  aisvalues;
        charttitle=title;
        
        // create JavaFX scene
        Platform.setImplicitExit(false);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                createScene();

            }
        });
    }
    
    public Scene createScene() {
//        stage.setTitle("Bar Chart");
        Group root = new Group();
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList();
    
        for (int i = 0; i < this.aistypes.size(); i++) {
            pieChartData.add(new PieChart.Data(this.aistypes.get(i), this.aisvalues.get(i)));
        }
        
        final PieChart chart
                = new PieChart(pieChartData);
        chart.setTitle(" Summary of "+ charttitle);
        
       root.getChildren().add(chart);
        Scene scene = new Scene(root);
//        root.getChildren().add(bc);
        this.setScene(scene);
        this.validate();
        WritableImage snapShot = scene.snapshot(null);

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null), "png", new File("chart.png"));
        } catch (IOException ex) {
            Logger.getLogger(LineChartGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return scene;
    }

}
