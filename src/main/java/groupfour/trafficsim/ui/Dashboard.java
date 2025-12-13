package groupfour.trafficsim.ui;
import groupfour.trafficsim.sim.SumoTrafficLight;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import java.util.List;
import javafx.scene.layout.HBox;
import javafx.scene.control.Separator;
import javafx.application.Platform;
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
    private Simulation simulation;
    private ListView<String> tlsList = new ListView<>();
    private ComboBox<Integer> phaseCombo = new ComboBox<>();
    private Label tlsLabel = new Label("TLS: ");
    private Label phaseLabel = new Label("Phase: ");
    private Label durationLabel = new Label("Duration: ");
    private Label statestringLabel = new Label("String: ");
    private SumoTrafficLight currentTLS;
    private VBox tlsDetailsPanel = new VBox(5);


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

        trafficLightSystemsTab.setContent(createTrafficLightSelection());

        dashboardPane.getTabs().addAll(statisticsTab, trafficLightSystemsTab);
    }

    //creates the interface for the traffic lights
    private VBox createTrafficLightSelection() {
        tlsList.setPrefHeight(200);
        tlsList.setPrefWidth(150);
        HBox tlsLayout = new HBox(10);
        VBox tlsleft = new VBox(5, new Label("Traffic Lights"), tlsList);
        VBox.setVgrow(tlsList, Priority.ALWAYS);
        tlsleft.setPrefWidth(180);
        tlsDetailsPanel.setPrefWidth(250);
        tlsDetailsPanel.getChildren().addAll(tlsLabel, statestringLabel, durationLabel, phaseLabel, phaseCombo);
        tlsLayout.getChildren().addAll(tlsleft, tlsDetailsPanel);
        return new VBox(10, new Separator(), tlsLayout, new Separator());
    }

    /**
     * Called when the user starts a simulation
     *
     * @param simulation the simulation started
     */
    public void init(Simulation simulation) {
        this.simulation = simulation;
        loadTrafficLights();

        simulation.setUpdateListener(() -> {
            Platform.runLater(() -> {
                refreshSelectedTLS();
            });
        });

        phaseCombo.setOnAction(event ->{
            Integer selectedPhase = phaseCombo.getSelectionModel().getSelectedItem();
            if(selectedPhase != null && currentTLS != null) {
                try{
                    currentTLS.setPhase(selectedPhase);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        tlsList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldV, newV) -> {
                    if (newV != null) {
                        showTLSDetails(newV);
                    }
                }
        );
    }
    /**
     * Called when a simulation step occurred and statistics have to be updated.
     *
     * @param simulation the simulation in question
     */
    public void update(Simulation simulation) {

    }

    private void showTLSDetails(String tlsId) {
        currentTLS = simulation.getTrafficLight(tlsId);

        if (currentTLS == null) return;

        tlsLabel.setText("TLS: " + tlsId);

        phaseCombo.getItems().clear();
        int max = currentTLS.getTotalPhases();
        for (int i = 0; i < max; i++) phaseCombo.getItems().add(i);
        phaseCombo.getSelectionModel().select(currentTLS.getPhaseIndex());
        refreshSelectedTLS();
    }

    //refreshes the display of the data
    public void refreshSelectedTLS() {
        if (currentTLS == null) return;

        //phaseLabel.setText("Phase: " + currentTLS.getPhaseIndex());
        statestringLabel.setText("State String: " + currentTLS.getStateString());
        durationLabel.setText("Duration: " + currentTLS.getDuration() + "s");
    }

    //loads all the trafficlights Ids at the start of the simulation
    public void loadTrafficLights() {
        if (this.simulation == null) {
            return;
        }
        try {
            List<String> tlsIds = this.simulation.getTrafficLightIds();
            this.tlsList.getItems().clear();
            this.tlsList.getItems().addAll(tlsIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Resets the dashboard.
     */
    public void reset() {

    }
}