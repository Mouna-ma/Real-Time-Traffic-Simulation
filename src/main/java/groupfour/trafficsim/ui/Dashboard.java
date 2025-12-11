package groupfour.trafficsim.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.text.Font;


public class Dashboard {

    //Root Layout for the dashboard
    private VBox root = new VBox(15);

    //UI elements
    private Label avgSpeedLabel = new Label("Avgerage Speed: -");   //Displays average speed
    private BarChart<String, Number> densityChart;  //Shows vehicle density per edge
    private final LineChart<Number, Number> vehiclesOverTimeChart;  //Shows vehicle count over time
    private final Label hotspotLabel = new Label("Congestion Hotspots: -");   //Displays congestion hotpots



    public Dashboard() {
        //Set font size for the Labels
        avgSpeedLabel.setFont(Font.font(18));
        hotspotLabel.setFont(Font.font(18));

        //BarChart Setup
        CategoryAxis xAxis = new CategoryAxis();    //X-axis with categories (edges)
        xAxis.setLabel("Edges");    //Label for X-axis
        NumberAxis yAxis = new NumberAxis();    //Y-axis with numeric (vehicles)
        yAxis.setLabel("Vehicles"); //Label for Y-axis

        densityChart = new BarChart<>(xAxis, yAxis);    //Create the bar chart
        densityChart.setTitle("Vehicle Density per Edge");  //Set chart title
        densityChart.setPrefHeight(250);    //Preferred height
        densityChart.setPrefWidth(380);     //Preferred width
        VBox.setVgrow(densityChart, Priority.NEVER);    //Do not grow vertically

        //LineChart Setup
        NumberAxis timeAxis = new NumberAxis(); //X-axis for time
        timeAxis.setLabel("Time (s)");  //Label
        NumberAxis vehicleAxis = new NumberAxis();  //Y-axis for number of vehicles
        vehicleAxis.setLabel("Vehicles");   //Label

        vehiclesOverTimeChart = new LineChart<>(timeAxis, vehicleAxis);   //Create the line chart
        vehiclesOverTimeChart.setTitle("Vehicles Over Time");   //Set chart title
        vehiclesOverTimeChart.setPrefHeight(250);   //Preferred height
        vehiclesOverTimeChart.setPrefWidth(380);    //Preferred width
        VBox.setVgrow(vehiclesOverTimeChart, Priority.ALWAYS);  //Allow this chart to gro vertically

        //Layout Setup
        root.setPadding(new Insets(20));    //Padding around the VBox
        root.setAlignment(Pos.TOP_LEFT);    //Align to the top left
        root.setPrefWidth(380);    //Preferred width of VBox
        root.setMaxWidth(400);     //Maximum width of VBox
        root.setStyle("-fx-padding: 20; -fx-background-color: #d3d3d3;"); //Background style

        //Add all UI elements to the root layout
        root.getChildren().addAll(
                avgSpeedLabel,
                hotspotLabel,
                densityChart,
                vehiclesOverTimeChart

        );

    }
    //Returns thr root Layout to be added to a scene
    public VBox getView() {
        return root;
    }

    // public void update(SimulationStats stats) {
    //    speedLabel.setText("Avg Speed: " + stats.avgSpeed);
    // usw…
    //
}