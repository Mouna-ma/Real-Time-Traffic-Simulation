package groupfour.trafficsim.ui;

import groupfour.trafficsim.sim.Simulation;
import groupfour.trafficsim.sim.SumoVehicle;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class used for displaying simulation data
 *
 * @author dila-ylz, 8wf92323f
 */
public class Dashboard {
    private static final Logger LOGGER = LogManager.getLogger(Dashboard.class.getName());
    private final Label vehicleCountLabel = new Label("Vehicle count: -");
    private final Label stepDurationLabel = new Label("Step Duration (ms): -");
    private final Label updateDurationLabel = new Label("Update duration (ms): -");
    private final Label totalStepLengthLabel = new Label("Step length (ms): -");
    private final Label avgSpeedLabel = new Label("Average speed: -");      // displays average speed
    private final Label hotspotLabel = new Label("Congestion hotspots: -"); // displays congestion hotpots
    private final BarChart<String, Number> vehicleEdgeDensityChart;           // shows vehicle density per edge
    private final LineChart<Number, Number> vehicleCountChart;                // shows vehicle count over time
    private final Button vehicleInjectionButton;
    private final Button stressTestButton;
    private final Spinner<Integer> batchSizeSpinner;
    private Simulation simulation;

    public Dashboard(TabPane dashboardPane) {
        dashboardPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        dashboardPane.setTabDragPolicy(TabPane.TabDragPolicy.FIXED);

        Tab statisticsTab = new Tab("Statistics");
        Tab trafficLightSystemsTab = new Tab("Traffic Lights");
        Tab vehicleInjectionTab = new Tab("Vehicle Injection");

        // STATISTICS

        // set font size for the labels
        this.avgSpeedLabel.setFont(Font.font(18));
        this.hotspotLabel.setFont(Font.font(18));

        // setup barchart
        CategoryAxis xAxis = new CategoryAxis(); // x-axis for edges
        NumberAxis yAxis = new NumberAxis();     // y-axis for number of vehicles
        xAxis.setLabel("Edges");
        yAxis.setLabel("Vehicles");
        this.vehicleEdgeDensityChart = new BarChart<>(xAxis, yAxis);
        this.vehicleEdgeDensityChart.setTitle("Vehicle Density per Edge (TODO)");
        this.vehicleEdgeDensityChart.setMinWidth(380);
        this.vehicleEdgeDensityChart.setMinHeight(250);
        VBox.setVgrow(this.vehicleEdgeDensityChart, Priority.NEVER); // do not grow vertically

        // setup line chart
        NumberAxis timeAxis = new NumberAxis();    // x-axis for time
        timeAxis.setForceZeroInRange(false);
        NumberAxis vehicleAxis = new NumberAxis(); // y-axis for number of vehicles
        timeAxis.setLabel("Time (s)");
        vehicleAxis.setLabel("Vehicles");
        this.vehicleCountChart = new LineChart<>(timeAxis, vehicleAxis);
        this.vehicleCountChart.setTitle("Vehicles Over Time");
        this.vehicleCountChart.setMinWidth(380);
        this.vehicleCountChart.setMinHeight(250);
        this.vehicleCountChart.getData().add(new XYChart.Series<>());
        this.vehicleCountChart.setLegendVisible(false);
        this.vehicleCountChart.setCreateSymbols(false);
        VBox.setVgrow(this.vehicleCountChart, Priority.ALWAYS); // allow this chart to grow vertically

        // setup layout - add all UI elements to the root layout
        ScrollPane statisticsScrollPane = Dashboard.createVerticalScrollPane(
                this.avgSpeedLabel,
                this.hotspotLabel,
                this.vehicleCountLabel,
                this.vehicleEdgeDensityChart,
                this.vehicleCountChart,
                this.stepDurationLabel,
                this.updateDurationLabel,
                this.totalStepLengthLabel
        );

        // TRAFFIC LIGHT SYSTEMS

        ScrollPane tlsScrollPane = Dashboard.createVerticalScrollPane(
                new Label("TLS")
        );


        // VEHICLE INJECTION

        this.vehicleInjectionButton = new Button("Inject vehicle");
        this.vehicleInjectionButton.setOnAction(this::onPressVehicleInjectionButton);
        this.stressTestButton = new Button("Stress test");
        this.stressTestButton.setOnAction(this::onPressStressTestButton);
        this.batchSizeSpinner = new Spinner<>();
        this.batchSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 20, 1));

        ScrollPane vehicleInjectionScrollPane = Dashboard.createVerticalScrollPane(
                this.vehicleInjectionButton,
                new Label("Batch size:"),
                this.batchSizeSpinner,
                this.stressTestButton
        );

        statisticsTab.setContent(statisticsScrollPane);
        trafficLightSystemsTab.setContent(tlsScrollPane);
        vehicleInjectionTab.setContent(vehicleInjectionScrollPane);
        dashboardPane.getTabs().addAll(statisticsTab, trafficLightSystemsTab, vehicleInjectionTab);

        this.reset();
    }

    /**
     * Helper method for creating a scroll pane
     * with elements aligned vertically
     */
    private static ScrollPane createVerticalScrollPane(Node... nodes) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20)); // padding around the vbox
        content.setAlignment(Pos.TOP_LEFT);
        content.setBackground(Background.fill(Color.LIGHTGRAY));
        content.getChildren().addAll(nodes);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true); // make the ScrollPane resize its content to fit the width
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // disable horizontal scrolling
        return scrollPane;
    }

    /**
     * Called when the user starts a simulation
     *
     * @param simulation the simulation started
     */
    public void init(Simulation simulation) {
        this.simulation = simulation;
        this.setButtonsEnabled(true);

        // reset them to disabled, they get enabled once simulation starts
        this.vehicleInjectionButton.setDisable(true);
        this.stressTestButton.setDisable(true);
    }
    /**
     * Called when a simulation step occurred and statistics have to be updated.
     *
     * @param simulation the simulation in question
     */
    public void update(Simulation simulation) {
        int vehicleCount = simulation.getVehicles().size();
        this.vehicleCountLabel.setText("Vehicle count: " + vehicleCount);

        double d1 = (double)simulation.getSimulationStepDuration() / 1_000_000.0;
        double d2 = (double)simulation.getUpdateStepDuration() / 1_000_000.0;
        this.stepDurationLabel.setText("Step duration (ms): " + d1);
        this.updateDurationLabel.setText("Update duration (ms): " + d2);
        this.totalStepLengthLabel.setText("Total length (ms): " + (d1 + d2));

        double averageSpeed = 0.0;

        for (SumoVehicle vehicle : simulation.getVehicles()) {
            averageSpeed += vehicle.get_speed();
        }

        averageSpeed /= (double)vehicleCount;
        this.avgSpeedLabel.setText("Average speed: " + Math.round(100.0 * averageSpeed) / 100.0);


        var seriesData = this.vehicleCountChart.getData().getFirst().getData();

        // if chart is too large, remove first 30 data points
        if (seriesData.size() > 90) {
            seriesData.remove(0, 30);
            double x = seriesData.getFirst().getXValue().doubleValue();
            ((NumberAxis)this.vehicleCountChart.getXAxis()).setLowerBound(x);
        }

        // only add points after a second has passed
        if (seriesData.isEmpty() || seriesData.getLast().getXValue().doubleValue() <= simulation.getTime() - 1.0) {
            seriesData.add(new XYChart.Data<>(simulation.getTime(), simulation.getVehicles().size()));
        }
    }

    /**
     * Resets the dashboard.
     */
    public void reset() {
        this.vehicleCountLabel.setText("Vehicle count: -");
        this.stepDurationLabel.setText("Step Duration (ms): -");
        this.updateDurationLabel.setText("Update duration (ms): -");
        this.totalStepLengthLabel.setText("Step length (ms): -");
        this.avgSpeedLabel.setText("Average speed: -");
        this.hotspotLabel.setText("Congestion hotspots: -");

        this.vehicleCountChart.getData().getFirst().getData().clear();

        this.setButtonsEnabled(false);
        this.simulation = null;
    }

    private void setButtonsEnabled(boolean enabled) {
        this.vehicleInjectionButton.setDisable(!enabled);
        this.stressTestButton.setDisable(!enabled);
        this.batchSizeSpinner.setEditable(!enabled);
    }

    private void onPressVehicleInjectionButton(ActionEvent event) {
        try {
            this.simulation.injectVehicle();
        } catch (Exception exception) {
            LOGGER.error("An exception occurred whilst trying to inject a vehicle", exception);
        }
    }

    private void onPressStressTestButton(ActionEvent event) {
        try {
            this.simulation.batchInjection(this.batchSizeSpinner.getValue());
        } catch (Exception exception) {
            LOGGER.error("An exception occurred whilst trying to perform a batch injection", exception);
        }
    }

    /**
     * Enables vehicle injection
     */
    public void allowInjection() {
        this.vehicleInjectionButton.setDisable(false);
        this.stressTestButton.setDisable(false);
    }
}