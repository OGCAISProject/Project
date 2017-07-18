/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.du.ogc.ais.function;

/**
 *
 * @author Jing
 */
import java.awt.Dimension;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.Group;

public class LineChartGenerator extends JFXPanel {
        //TODO:need to disable actions
    String ncfile = "test.nc";
    String wfsfile = "3565.xml";
    String charttile = "";
    VerticalProfiling vp;

    public  LineChartGenerator(String ncfile, String wfsfile, Dimension preferredSize ) {
        super();
        this.setPreferredSize(preferredSize);
        this.revalidate();;
        if (!"".equals(ncfile)) {
            this.ncfile = ncfile;
        }
        if (!"".equals(wfsfile)) {
            this.wfsfile = wfsfile;
        }
        this.vp = new VerticalProfiling(this.ncfile, this.wfsfile);

        this.charttile = "Vertical Profile" + vp.GetVariableName();
        //defining the axes
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
        Group root = new Group();
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Distance");
        //creating the chart
        final LineChart<Number, Number> lineChart
                = new LineChart<Number, Number>(xAxis, yAxis);

//          lineChart.setTitle("");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("Vertical Profile" + vp.GetVariableName());
        //populating the series with data
        for (int i = 0; i < vp.GetZValues().size(); i++) {
            float datavalue = vp.GetZValues().get(i);
            if (!Float.isNaN(datavalue)) {
                series.getData().add(new XYChart.Data(i, datavalue));
            }
        }
       

        lineChart.getData().add(series);
        
    
        Scene scene = new Scene(root);
        
        
//        File sytle =  new File("chart.css");
//        try {
//            scene.getStylesheets().add(sytle.toURI().toURL().toExternalForm());
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(LineChartGenerator.class.getName()).log(Level.SEVERE, null, ex);
//        }

        root.getChildren().add(lineChart);
        this.setScene(scene);
 
//        WritableImage snapShot = scene.snapshot(null);
//
//        try {
//            ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null), "png", new File("chart.png"));
//        } catch (IOException ex) {
//            Logger.getLogger(LineChartGenerator.class.getName()).log(Level.SEVERE, null, ex);
//        }
        return scene;

    }
}
