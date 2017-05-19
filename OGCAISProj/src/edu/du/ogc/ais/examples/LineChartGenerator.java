/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.du.ogc.ais.examples;

/**
 *
 * @author Jing
 */
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class LineChartGenerator extends Application {

    @Override
    public void start(Stage stage) {
        VerticalProfiling vp = new VerticalProfiling("test.nc", "test.xml");

        stage.setTitle("Vertical Profile" + vp.GetVariableName());
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Distance");
        //creating the chart
        final LineChart<Number, Number> lineChart
                = new LineChart<Number, Number>(xAxis, yAxis);

//          lineChart.setTitle("");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("");
        //populating the series with data
        for (int i = 0; i < vp.GetZValues().size(); i++) {
            float datavalue = vp.GetZValues().get(i);
            if (!Float.isNaN(datavalue)) {
                series.getData().add(new XYChart.Data(i, datavalue));
            }
        }

        lineChart.getData().add(series);
        lineChart.setAnimated(false);

        Scene scene = new Scene(lineChart, 800, 600);

        stage.setScene(scene);

        stage.show();

        WritableImage snapShot = scene.snapshot(null);

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null), "png", new File("chart.png"));
        } catch (IOException ex) {
            Logger.getLogger(LineChartGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}
