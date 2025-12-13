package groupfour.trafficsim.ui;

import groupfour.trafficsim.sim.Simulation;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * The main application window.
 * It handles the connection process to SUMO and holds all UI elements.
 *
 * @author 8wf92323f
 */
public class MainWindow extends Application {
    private final BorderPane root;
    private final MenuItem connectMenuItem;
    private final MenuItem disconnectMenuItem;
    private final SimulationControls simulationControls;
    private Stage stage;
    private Simulation simulation;

    /**
     * Creates the window and its UI elements.
     */
    public MainWindow() {
        // Menu bar
        Menu sumoMenu = new Menu("SUMO");
        this.connectMenuItem = new MenuItem("Connect");
        this.connectMenuItem.setOnAction(this::onPressConnect);
        this.disconnectMenuItem = new MenuItem("Disconnect");
        this.disconnectMenuItem.setOnAction(this::onPressDisconnect);
        sumoMenu.getItems().addAll(this.connectMenuItem, this.disconnectMenuItem);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(sumoMenu);

        this.setSumoMenuButtonsEnabled(true, false);

        // Window Elements
        HBox controlBar = new HBox();
        controlBar.setAlignment(Pos.CENTER);
        controlBar.setSpacing(16.0);

        StackPane mapPane = new StackPane();

        TabPane dashboardPane = new TabPane();

        this.simulationControls = new SimulationControls(controlBar, mapPane, dashboardPane);

        // Menu bar and Control bar
        VBox vbox = new VBox();
        vbox.getChildren().addAll(menuBar, controlBar);

        // SplitPane of Canvas and Dashboard
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(mapPane, dashboardPane);
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.5);

        // UI root node
        this.root = new BorderPane();
        this.root.setTop(vbox);
        this.root.setCenter(splitPane);
    }

    /**
     * Called when the application starts and the window gets opened.
     *
     * @param stage the main window stage
     */
    @Override
    public void start(Stage stage) {
        this.stage = stage;

        this.stage.setOnHidden(this::onStageHidden);

        Scene scene = new Scene(this.root, 800, 500);
        stage.setTitle("SUMO Simulation");
        stage.setScene(scene);
        stage.show();
    }

    private void onPressConnect(ActionEvent event) {
        assert this.simulation == null; // UI error: button should be disabled

        ConnectWindow connectWindow = new ConnectWindow(this);
        connectWindow.open(this.stage);
    }

    private void onPressDisconnect(ActionEvent event) {
        this.disconnectSimulation();
    }

    private void onStageHidden(WindowEvent event) {
        if (this.simulation != null) {
            this.disconnectSimulation();
        }
    }

    /**
     * Initializes controls to a new simulation connection.
     *
     * @param simulation the simulation to initialize for
     */
    public void setSimulation(Simulation simulation) {
        assert this.simulation == null; // UI error or function was called wrong

        this.setSumoMenuButtonsEnabled(false, true);
        this.simulation = simulation;
        this.simulationControls.setSimulation(simulation);
    }

    /**
     * Resets simulation controls.
     */
    public void disconnectSimulation() {
        assert this.simulation != null; // UI error or function was called wrong

        if (!this.simulation.isClosed()) {
            this.simulation.close();
        }

        this.simulationControls.disconnectSimulation();
        this.simulation = null;
        this.setSumoMenuButtonsEnabled(true, false);
    }

    private void setSumoMenuButtonsEnabled(boolean enableConnect, boolean enableDisconnect) {
        this.connectMenuItem.setDisable(!enableConnect);
        this.disconnectMenuItem.setDisable(!enableDisconnect);
    }
}
