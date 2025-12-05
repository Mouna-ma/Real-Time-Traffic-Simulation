package groupfour.trafficsim.ui;

import groupfour.trafficsim.sim.Simulation;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class SimulationControls {
    private final MainWindow parent;
    private final Button stepButton;
    private Simulation simulation;

    public SimulationControls(MainWindow parent) {
        this.parent = parent;

        this.stepButton = new Button("Step");
        this.stepButton.setOnAction(this::onPressStepButton);

        this.setUIComponentsEnabled(false);
    }

    public void addUIElements(HBox controlBar, StackPane canvasPane, StackPane dashboardPane) {
        controlBar.getChildren().addAll(this.stepButton);

        canvasPane.getChildren().add(new Label("Canvas"));

        dashboardPane.getChildren().add(new Label("Dashboard"));
    }

    public void setSimulation(Simulation simulation) {
        if (this.simulation != null) {
            throw new RuntimeException("Should not call startSimulation when another simulation is connected.");
        }

        this.simulation = simulation;
        this.setUIComponentsEnabled(true);
    }

    public void disconnectSimulation() {
        if (this.simulation == null) {
            throw new RuntimeException("Should not call disconnectSimulation when no simulation is connected.");
        }

        this.setUIComponentsEnabled(false);
        this.simulation = null;
    }

    private void setUIComponentsEnabled(boolean enabled) {
        this.stepButton.setDisable(!enabled);
    }

    private void onPressStepButton(ActionEvent event) {
        assert this.simulation != null; // UI Error: button should be disabled

        this.simulation.step();
    }
}
