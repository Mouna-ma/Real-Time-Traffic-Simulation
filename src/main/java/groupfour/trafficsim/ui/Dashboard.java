package groupfour.trafficsim.ui;

import groupfour.trafficsim.sim.Simulation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * A class used for displaying simulation data
 *
 * @author dila-ylz, 8wf92323f
 */
public class Dashboard {
    private final Label avgSpeedLabel = new Label("Average Speed: -");      // displays average speed
    private final Label hotspotLabel = new Label("Congestion Hotspots: -"); // displays congestion hotpots
    private final BarChart<String, Number> vehicleEdgeDensityChart;           // shows vehicle density per edge
    private final LineChart<Number, Number> vehicleCountChart;                // shows vehicle count over time

    public Dashboard(TabPane dashboardPane) {
        dashboardPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        dashboardPane.setTabDragPolicy(TabPane.TabDragPolicy.FIXED);

        Tab statisticsTab = new Tab("Statistics");
        Tab trafficLightSystemsTab = new Tab("Traffic Lights");

        // set font size for the labels
        this.avgSpeedLabel.setFont(Font.font(18));
        this.hotspotLabel.setFont(Font.font(18));

        // setup barchart
        CategoryAxis xAxis = new CategoryAxis(); // x-axis for edges
        NumberAxis yAxis = new NumberAxis();     // y-axis for number of vehicles
        xAxis.setLabel("Edges");
        yAxis.setLabel("Vehicles");
        this.vehicleEdgeDensityChart = new BarChart<>(xAxis, yAxis);
        this.vehicleEdgeDensityChart.setTitle("Vehicle Density per Edge");
        this.vehicleEdgeDensityChart.setPrefHeight(250);
        this.vehicleEdgeDensityChart.setPrefWidth(380);
        VBox.setVgrow(this.vehicleEdgeDensityChart, Priority.NEVER); // do not grow vertically

        // setup line chart
        NumberAxis timeAxis = new NumberAxis();    // x-axis for time
        NumberAxis vehicleAxis = new NumberAxis(); // y-axis for number of vehicles
        timeAxis.setLabel("Time (s)");
        vehicleAxis.setLabel("Vehicles");
        this.vehicleCountChart = new LineChart<>(timeAxis, vehicleAxis);
        this.vehicleCountChart.setTitle("Vehicles Over Time");
        this.vehicleCountChart.setPrefHeight(250);
        this.vehicleCountChart.setPrefWidth(380);
        VBox.setVgrow(this.vehicleCountChart, Priority.ALWAYS); // allow this chart to grow vertically

        // setup layout
        VBox statisticsRoot = new VBox(15);
        statisticsRoot.setPadding(new Insets(20)); // padding around the vbox
        statisticsRoot.setAlignment(Pos.TOP_LEFT);
        statisticsRoot.setPrefWidth(380);
        statisticsRoot.setMaxWidth(400);
        statisticsRoot.setStyle("-fx-padding: 20; -fx-background-color: #d3d3d3;"); // background style

        // qdd all UI elements to the root layout
        statisticsRoot.getChildren().addAll(
                this.avgSpeedLabel,
                this.hotspotLabel,
                this.vehicleEdgeDensityChart,
                this.vehicleCountChart
        );

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(statisticsRoot);
        scrollPane.setFitToWidth(true);                                 // make the ScrollPane resize its content to fit the width
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);     // disable horizontal scrolling
        scrollPane.setPrefWidth(400);
        scrollPane.setPrefHeight(600);
        StackPane.setAlignment(scrollPane, Pos.TOP_LEFT);

        statisticsTab.setContent(scrollPane);

        trafficLightSystemsTab.setContent(new Label("Traffic Light Systems"));

        dashboardPane.getTabs().addAll(statisticsTab, trafficLightSystemsTab);
    }

    /**
     * Called when the user starts a simulation
     *
     * @param simulation the simulation started
     */
    public void init(Simulation simulation) {

    }
    /**
     * Called when a simulation step occurred and statistics have to be updated.
     *
     * @param simulation the simulation in question
     */
    public void update(Simulation simulation) {

    }

    /**
     * Resets the dashboard.
     */
    public void reset() {

    }
}