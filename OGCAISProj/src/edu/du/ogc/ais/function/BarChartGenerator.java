/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//https://platform.netbeans.org/tutorials/nbm-javafx.html
package edu.du.ogc.ais.function;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class BarChartGenerator extends JFXPanel {

   
    ArrayList<String> aistypes;
    ArrayList<Integer> aisvalues;
    String charttitle="";

    public BarChartGenerator(ArrayList<String> aistypes,ArrayList<Integer> aisvalues, String title ) {
        
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

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        

        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Count");

       

        for (int i = 0; i < this.aistypes.size(); i++) {
            series1.getData().add(new XYChart.Data(this.aistypes.get(i).split(",")[0], this.aisvalues.get(i)));
            
        }
        
        final BarChart<String, Number> bc
                = new BarChart<String, Number>(xAxis, yAxis);
        bc.setTitle(" Summary of "+ charttitle);
        
        xAxis.setLabel(charttitle);
        yAxis.setLabel("Count");
        bc.getData().addAll(series1);;
        Scene scene = new Scene(root);
        root.getChildren().add(bc);
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
