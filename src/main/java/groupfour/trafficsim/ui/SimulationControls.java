package groupfour.trafficsim.ui;

import groupfour.trafficsim.sim.Simulation;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * A class that holds all simulation control specific UI elements
 * that allow the user to interact with the simulation.
 *
 * @author 8wf92323f
 */
public class SimulationControls {
    private final MainWindow parent;
    private final Label timeLabel;
    private final Button stepButton;
    private final Button startButton;
    private final Button stopButton;
    private final SimulationMap simulationMap;
    private final AnimationTimer animationTimer;
    private final Dashboard dashboard;
    private Simulation simulation;
    private boolean simulationUpdatesAvailable = false;

    public SimulationControls(MainWindow parent, HBox controlBar, StackPane mapPane, StackPane dashboardPane) {
        this.parent = parent;

        this.timeLabel = new Label("00:00.000");
        this.stepButton = new Button("Step");
        this.stepButton.setOnAction(this::onPressStepButton);
        this.startButton = new Button("Start");
        this.startButton.setOnAction(this::onPressStartButton);
        this.stopButton = new Button("Stop");
        this.stopButton.setOnAction(this::onPressStopButton);
        controlBar.getChildren().addAll(this.timeLabel, this.stepButton, this.startButton, this.stopButton);

        this.simulationMap = new SimulationMap(mapPane);

        //Create a new Dashboard instance 
        this.dashboard = new Dashboard();

        ScrollPane scroll = new ScrollPane(dashboard.getView());    //Wrap the dashboard in a ScrollPane to allow scrolling
        scroll.setFitToWidth(true); //Make the ScrollPane resize its content to fit the width
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Disable horizontal scrolling
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); //Enable vertical scrolling as needed
        //Set preferred size for the ScrollPane
        scroll.setPrefWidth(400);
        scroll.setPrefHeight(600);


        dashboardPane.getChildren().clear();    //Clear any previous content from the dashboard to the container
        dashboardPane.getChildren().add(scroll);    //Add the ScrollPane containing the dashboard to the container
        StackPane.setAlignment(scroll, Pos.TOP_LEFT);   //Align to the top left

        this.animationTimer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                SimulationControls.this.update();
            }
        };

        this.setUIComponentsEnabled(false);
    }

    /**
     * Initializes UI components when simulation connection is established.
     *
     * @param simulation the simulation to initialize for
     */
    public void setSimulation(Simulation simulation) {
        if (this.simulation != null) {
            throw new RuntimeException("Called startSimulation whilst another simulation is connected");
        }

        this.simulation = simulation;
        this.setUIComponentsEnabled(true);
        this.simulation.setUpdateListener(() -> {
            this.simulationUpdatesAvailable = true;
        });
        this.simulationUpdatesAvailable = true; // initial simulation state should be fetched as soon as possible
        this.simulationMap.init(simulation);
        this.animationTimer.start();

    }

    /**
     * Resets UI components when simulation connection perishes.
     */
    public void disconnectSimulation() {
        if (this.simulation == null) {
            throw new RuntimeException("Called disconnectSimulation whilst no simulation is connected");
        }

        this.animationTimer.stop();
        this.simulationMap.reset();
        this.simulation.setUpdateListener(null);
        this.simulationUpdatesAvailable = false; // disable simulation state update
        this.setUIComponentsEnabled(false);
        this.simulation = null;
    }

    private void setUIComponentsEnabled(boolean enabled) {
        this.setStepControlsEnabled(enabled, false);
    }

    private void setStepControlsEnabled(boolean stepButtonsEnabled, boolean stopButtonEnabled) {
        this.stepButton.setDisable(!stepButtonsEnabled);
        this.startButton.setDisable(!stepButtonsEnabled);
        this.stopButton.setDisable(!stopButtonEnabled);
    }

    private void onPressStepButton(ActionEvent event) {
        assert this.simulation != null; // UI Error: button should be disabled

        this.setStepControlsEnabled(false, false);

        this.simulation.step(() -> {
            this.setStepControlsEnabled(true, false);
        });
    }

    private void onPressStartButton(ActionEvent event) {
        assert this.simulation != null; // UI Error: button should be disabled

        this.setStepControlsEnabled(false, true);

        this.simulation.startContinuous(() -> {
            this.setStepControlsEnabled(true, false);
        });
    }

    private void onPressStopButton(ActionEvent event) {
        assert this.simulation != null; // UI Error: button should be disabled

        this.simulation.stopContinuous();
    }

    /**
     * UI update function that runs on the JavaFX thread.
     * Called each time JavaFX rerenders.
     * Checks whether a new simulation state is available and updates UI components.
     */
    private void update() {
        if (!this.simulationUpdatesAvailable) return;

        double time = this.simulation.getTime();

        this.simulationMap.update(this.simulation);

        int millis = ((int)(time * 1000.0)) % 1000;
        int seconds = ((int)time) % 60;
        int minutes = ((int)time) / 60;
        this.timeLabel.setText(String.format("%02d:%02d:%03d", minutes, seconds, millis));
    }
}
