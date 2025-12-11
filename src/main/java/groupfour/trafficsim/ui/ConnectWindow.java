package groupfour.trafficsim.ui;

import groupfour.trafficsim.sim.Simulation;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * A Window that allows the user to configure SUMO launch settings.
 *
 * @author 8wf92323f
 */
public class ConnectWindow {
    private static final Logger LOGGER = LogManager.getLogger(ConnectWindow.class.getName());
    private final MainWindow parent;
    private final ComboBox<String> comboBox;
    private final TextField textField;
    private final VBox layout;
    private Stage stage;

    /**
     * Creates the window and its UI elements.
     *
     * @param parent the parent window
     */
    public ConnectWindow(MainWindow parent) {
        this.parent = parent;

        // Launch Configuration Buttons

        this.comboBox = new ComboBox<>();
        this.comboBox.getItems().addAll("sumo", "sumo-gui");
        this.comboBox.getSelectionModel().select(0);

        this.textField = new TextField();
        this.textField.setPromptText(".sumocfg path");

        Button searchFilesButton = new Button("Search Files");
        searchFilesButton.setOnAction(this::onPressSearchFilesButton);

        GridPane configLayout = new GridPane();
        configLayout.setAlignment(Pos.CENTER);
        configLayout.setHgap(10.0);
        configLayout.setVgap(6.0);
        configLayout.add(new Label("Binary"), 0, 0);
        configLayout.add(this.comboBox, 1, 0);
        configLayout.add(new Label("Config File"), 0, 1);
        configLayout.add(this.textField, 1, 1);
        configLayout.add(searchFilesButton, 2, 1);

        // Window Buttons

        Button startButton = new Button("Start");
        startButton.setOnAction(this::onPressStartButton);
        Button closeButton = new Button("Close");
        closeButton.setOnAction(this::onPressCloseButton);

        HBox buttonLayout = new HBox(startButton, closeButton);
        buttonLayout.setAlignment(Pos.CENTER);
        buttonLayout.setSpacing(10.0);

        // Window creation

        this.layout = new VBox();
        this.layout.setAlignment(Pos.CENTER);
        this.layout.setSpacing(32.0);
        this.layout.getChildren().addAll(
                new Label("Launch SUMO"),
                configLayout,
                buttonLayout
        );
    }

    /**
     * Visually opens/starts the window.
     *
     * @param parentStage the stage of the parent window
     */
    public void open(Stage parentStage) {
        assert this.stage == null;

        this.stage = new Stage();
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.stage.initOwner(parentStage);
        this.stage.setTitle("Connect SUMO");
        this.stage.setScene(new Scene(this.layout, 400, 300));
        this.stage.showAndWait();
    }

    private void onPressSearchFilesButton(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Sumo Config");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".sumocfg", "*.sumocfg"));
        File selectedFile = fileChooser.showOpenDialog(this.stage);

        if (selectedFile != null) {
            this.textField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void onPressStartButton(ActionEvent event) {
        String binary = this.comboBox.getSelectionModel().getSelectedItem();
        String configFile = this.textField.getText();

        Simulation simulation;

        try {
            simulation = new Simulation(binary, configFile);
        } catch (Exception exception) {
            LOGGER.error("Exception whilst trying to connect to Sumo", exception);

            String message = String.join("\n",
                    "An exception occurred whilst trying to connect to Sumo.",
                    "Ensure the file path is valid.",
                    "See the full log for more information.",
                    "",
                    exception.getMessage()
            );

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(message);
            alert.showAndWait();

            return;
        }

        this.parent.setSimulation(simulation);
        this.stage.close();
    }

    private void onPressCloseButton(ActionEvent event) {
        this.stage.close();
    }
}
