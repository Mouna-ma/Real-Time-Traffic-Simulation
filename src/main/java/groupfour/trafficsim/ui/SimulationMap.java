package groupfour.trafficsim.ui;

import groupfour.trafficsim.sim.Simulation;
import groupfour.trafficsim.sim.SumoLane;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polyline;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.List;

/**
 * A Map class that visualizes the Simulation
 *
 * @author 8wf92323f
 */
public class SimulationMap {
    private final Group root = new Group();
    private SimulationMap.Camera camera;
    private boolean active = false;
    private double x1;
    private double y1;

    private final Translate cameraTranslateTransform = new Translate();
    private final Scale cameraScaleTransform = new Scale();

    public SimulationMap(StackPane mapPane) {
        mapPane.setAlignment(Pos.TOP_LEFT); // set (x=0, y=0) to top left
        mapPane.setMinSize(0.0, 0.0);
        mapPane.getChildren().add(this.root);

        mapPane.setOnMouseMoved(this::onMouseMove);
        mapPane.setOnMouseDragged(this::onMouseDrag);
        mapPane.setOnScroll(this::onScroll);

        // dynamic transforms
        // (convert java fx's x-right, y-down system to SUMO's x-right, y-up system)

        // flip y-axis
        Scale scaleTransform = new Scale(1.0, -1.0);
        scaleTransform.setPivotX(0.0);
        scaleTransform.pivotYProperty().bind(mapPane.heightProperty().divide(2.0));

        // translate (0, 0) to center of screen
        Translate translateTransform = new Translate();
        translateTransform.xProperty().bind(mapPane.widthProperty().divide(2.0));
        translateTransform.yProperty().bind(mapPane.heightProperty().divide(2.0));

        this.root.getTransforms().addAll(
                scaleTransform,
                translateTransform,
                this.cameraTranslateTransform,
                this.cameraScaleTransform
        );
    }

    /**
     * Invoked when the mouse hovers over the UI element.
     */
    private void onMouseMove(MouseEvent event) {
        this.x1 = event.getX();
        this.y1 = -event.getY();
    }

    /**
     * Invoked when the mouse drags (holds the mouse down) and moves over the UI element.
     */
    private void onMouseDrag(MouseEvent event) {
        double x0 = event.getX();
        double y0 = -event.getY();

        if (this.active) {
            double dx = x0 - this.x1;
            double dy = y0 - this.y1;

            this.camera.processDrag(dx, dy);
            this.updateTransforms();
        }

        this.x1 = x0;
        this.y1 = y0;
    }

    /**
     * Invoked when the mouse wheel is scrolled.
     */
    private void onScroll(ScrollEvent event) {
        if (!this.active || event.getDeltaY() == 0) {
            return;
        }

        if (event.isControlDown()) {
            this.camera.processRotationScroll(event.getDeltaY());
        } else {
            this.camera.processZoomScroll(event.getDeltaY());
        }

        this.updateTransforms();
    }

    /**
     * Called when the user starts a simulation
     *
     * @param simulation the simulation started
     */
    public void init(Simulation simulation) {
        this.active = true;
        this.camera = new Camera();

        /*
        // Debug
        Rectangle r1 = new Rectangle(0, 0, 100, 5);
        r1.setFill(Color.RED);
        Rectangle r2 = new Rectangle(0, 0, 5, 100);
        r2.setFill(Color.GREEN);
        this.root.getChildren().addAll(r, r2);
         */

        // load road network

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        List<SumoLane> lanes = simulation.getLanes();

        if (lanes.isEmpty()) {
            minX = 0;
            maxX = 0;
            minY = 0;
            maxY = 0;
        } else {
            Polyline[] polylines = new Polyline[lanes.size()];

            for (int i = 0; i < lanes.size(); i++) {
                SumoLane lane = lanes.get(i);
                polylines[i] = new Polyline();
                polylines[i].setStrokeWidth(0.9 * lane.getLaneWidth());

                double[][] geometry = lane.getGeometry();

                for (double[] point : geometry) {
                    double x = point[0];
                    double y = point[1];

                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);

                    polylines[i].getPoints().addAll(x, y);
                }
            }

            this.root.getChildren().addAll(polylines);
        }

        this.camera.x = (maxX - minX) / 2.0;
        this.camera.y = (maxY - minY) / 2.0;
        this.updateTransforms();
    }


    /**
     * Called when a simulation step occurred and visuals have to be updated.
     *
     * @param simulation the simulation in question
     */
    public void update(Simulation simulation) {

    }

    /**
     * Resets the visualization map.
     */
    public void reset() {
        this.active = false;
        this.root.getChildren().clear();
    }

    /**
     * Updates the visual transformations.
     */
    private void updateTransforms() {
        this.cameraTranslateTransform.setX(-this.camera.x);
        this.cameraTranslateTransform.setY(-this.camera.y);

        this.cameraScaleTransform.setX(this.camera.zoom);
        this.cameraScaleTransform.setY(this.camera.zoom);
        this.cameraScaleTransform.setPivotX(this.camera.x);
        this.cameraScaleTransform.setPivotY(this.camera.y);
    }

    /**
     * A Camera class containing all viewing related variables.
     */
    private static class Camera {
        public double x = 0.0;
        public double y = 0.0;
        public double zoom = 1.0;
        public double rotation = 0.0;

        /**
         * Calculates new camera parameters when the user drags his mouse.
         *
         * @param dx number of horizontal pixels dragged
         * @param dy number of vertical pixels dragged
         */
        public void processDrag(double dx, double dy) {
            dx /= this.zoom;
            dy /= this.zoom;

            this.x -= dx;
            this.y -= dy;
        }

        /**
         * Calculates new camera parameters when the user scrolls his mouse wheel.
         *
         * @param dy amount of wheel change
         */
        public void processZoomScroll(double dy) {
            if (dy > 0) {
                this.zoom *= 1.125;
            } else {
                this.zoom /= 1.125;
            }
        }

        /**
         * Calculates new camera parameters when the user scrolls his mouse wheel.
         *
         * @param dy amount of wheel change
         */
        public void processRotationScroll(double dy) {
            this.rotation += dy / 2.0;
        }
    }
}
