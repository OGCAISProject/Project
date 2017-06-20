/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.du.ogc.ais.examples;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.chart.*;
import javafx.scene.Group;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Jing
 */
public class PieChartGenerator extends Application {

    String chartfile = "xx.csv";
    ArrayList<String> aistypes;
    ArrayList<Integer> aisvalues;
//  wfs:  resultype=hits
    //read info from file or stream?
    //need to join data from different tables
    //example 1: different types of tracks
    //example 2: # of within a query window (e.g,. time series)

    private void ReadData() {
        this.aistypes = new ArrayList<String>();
        this.aisvalues = new ArrayList<Integer>();
        BufferedReader br = null;
        try {

            try {
                br = new BufferedReader(new FileReader(this.chartfile));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine(); //header
                line = br.readLine();
                while (line != null) {
//                    sb.append(line);
//                    sb.append(System.lineSeparator());
                    this.aistypes.add(line.split(",")[0]);
                    this.aisvalues.add(Integer.valueOf(line.split(",")[1]));
                    line = br.readLine();

                }
//                String everything = sb.toString();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PieChartGenerator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PieChartGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }

        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(PieChartGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private ObservableList<Data> getChartData() {
        ReadData();
        ObservableList<Data> piechartitems = FXCollections.observableArrayList();
        for (int i = 0; i < this.aistypes.size(); i++) {
            piechartitems.add(new PieChart.Data(this.aistypes.get(i), this.aisvalues.get(i)));
        }
        return piechartitems;
    }

    @Override
    public void start(Stage stage) {

        Scene scene = new Scene(new Group());
        stage.setTitle("AIS Information");
        stage.setWidth(500);
        stage.setHeight(500);

        PieChart chart = new PieChart();
        chart.setData(getChartData());
        chart.setTitle("AIS Information");
        chart.setLabelLineLength(10);
        chart.setLegendSide(Side.LEFT);
        ((Group) scene.getRoot()).getChildren().add(chart);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {

        launch(args);
    }

}
