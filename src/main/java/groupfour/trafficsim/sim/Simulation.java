package groupfour.trafficsim.sim;

import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.cmd.Vehicle;
import it.polito.appeal.traci.SumoTraciConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * A wrapper for the connection to a SUMO instance.
 * All simulation control tasks are managed by this class.
 *
 * @author 8wf92323f, mikey7303
 */
public class Simulation {
    private static final Logger LOGGER = LogManager.getLogger(Simulation.class.getName());
    private static final Random RANDOM = new Random();
    private final SumoTraciConnection connection;
    private Thread thread;
    private volatile boolean shouldStopSimulation = true;
    private Runnable updateListener;
    private final List<SumoEdge> edges = new ArrayList<>();
    private final List<SumoLane> lanes = new ArrayList<>();
    private final List<SumoRoute> routes;
    private final Map<String, SumoVehicle> vehicles = new HashMap<>();
    private double time = 0.0;
    private long simulationStepDuration = 0L;
    private long updateStepDuration = 0L;

    /**
     * Creates a simulation instance by launching SUMO.
     *
     * @param binary a string representing the SUMO executable name
     * @param configFile the file path of the .sumocfg file
     */
    public Simulation(String binary, String configFile) {
        this.connection = new SumoTraciConnection(binary, configFile);

        double stepFrequency = 8.0; // steps/sec
        double simulationSpeed = 1.0;
        double stepLength = simulationSpeed / stepFrequency;
        double delay = 1000.0 / simulationSpeed;

        this.connection.addOption("start", "true");
        this.connection.addOption("step-length", Double.toString(stepLength));
        this.connection.addOption("delay", Double.toString(delay));

        try {
            this.connection.runServer();
        } catch (IOException exception) {
            throw new RuntimeException("Could not start server", exception);
        }

        try {
            // load constant data

            this.routes = RouteParser.parseRoutes(configFile, this);

            List<String> edgeIds = (List<String>)this.connection.do_job_get(Edge.getIDList());

            for (String edgeId : edgeIds) {
                SumoEdge edge = new SumoEdge(edgeId, this.connection);
                this.edges.add(edge);
                this.lanes.addAll(edge.getLanes());
            }

            // run once to ensure the default state is loaded
            this.update();
        } catch (Exception exception) {
            throw new RuntimeException("An error occurred whilst trying to initialize", exception);
        }
    }

    /**
     * Performs a singular singulation step.
     *
     * @param callback a callback that is called once the simulation has finished
     */
    public void step(Runnable callback) {
        if (this.isClosed()) {
            throw new IllegalStateException("Connection is closed");
        }

        if (this.thread != null && this.thread.isAlive()) {
            throw new IllegalStateException("Simulation is already running");
        }

        this.thread = new Thread(() -> {
            try {
                this.connection.do_timestep();
                this.update();
            } catch (InterruptedException interruptedException) {
                LOGGER.error("Simulation thread was interrupted", interruptedException);
            } catch (Exception exception) {
                LOGGER.error("Exception during Simulation", exception);
            }

            callback.run();
        });

        this.thread.start();
    }

    /**
     * Starts a continuous simulation.
     * The continuous simulation can be stopped using the function stopContinuous.
     *
     * @param callback a callback that is called once the simulation has finished
     */
    public void startContinuous(Runnable callback) {
        if (this.isClosed()) {
            throw new IllegalStateException("Connection is closed");
        }

        if (this.thread != null && this.thread.isAlive()) {
            throw new IllegalStateException("Simulation is already running");
        }

        this.shouldStopSimulation = false;

        this.thread = new Thread(() -> this.runContinuous(callback));
        this.thread.start();
    }

    /**
     * The actual continuous simulation run by startContinuous.
     * This function should never be called from the calling thread.
     *
     * @param callback a callback that is called once the simulation has finished
     */
    private void runContinuous(Runnable callback) {
        long expectedStepNanoDuration;

        try {
            double deltaT = (double)this.connection.do_job_get(de.tudresden.sumo.cmd.Simulation.getDeltaT());
            expectedStepNanoDuration = (long)(deltaT * 1_000_000_000L);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        while (!this.shouldStopSimulation && !this.isClosed()) {
            try {
                long t0 = System.nanoTime();
                this.connection.do_timestep();
                long t1 = System.nanoTime();
                this.update();
                long t2 = System.nanoTime();

                long stepNanoDuration = t2 - t1;
                long sleepNanoTime = expectedStepNanoDuration - stepNanoDuration;

                // store statistics
                this.simulationStepDuration = t1 - t0;
                this.updateStepDuration = t2 - t1;

                if (sleepNanoTime > 0) {
                    long millis = sleepNanoTime / 1_000_000L;
                    int nanos = (int)(sleepNanoTime % 1_000_000L);
                    Thread.sleep(millis, nanos);
                }
            } catch (InterruptedException interruptedException) {
                this.shouldStopSimulation = true;
                LOGGER.error("Simulation thread was interrupted", interruptedException);
                break;
            } catch (Exception exception) {
                this.shouldStopSimulation = true;
                LOGGER.error("Exception during timestep", exception);
                break;
            }
        }

        callback.run();
    }

    /**
     * Notifies the continuous simulation that it should stop.
     */
    public void stopContinuous() {
        this.shouldStopSimulation = true;
    }

    /**
     * Called after each simulation step to ensure simulation objects are up to date.
     *
     * @throws Exception any error that may occur whilst receiving or updating data
     */
    private void update() throws Exception {
        this.time = (double)this.connection.do_job_get(de.tudresden.sumo.cmd.Simulation.getTime());

        // VEHICLE UPDATES

        Set<String> vehicleIds = new HashSet<>((List<String>)this.connection.do_job_get(Vehicle.getIDList()));
        Set<String> storedIds = new HashSet<>(this.vehicles.keySet()); // copy a set of all tracked vehicles

        for (String vehicleId : vehicleIds) {
            if (!storedIds.contains(vehicleId)) {
                // if the vehicle is not present,
                // it was just added to the simulation
                this.vehicles.put(vehicleId, new SumoVehicle(vehicleId));
            }

            // we remove all processed ids from storedIds
            storedIds.remove(vehicleId);
        }

        // storedIds now only contains ids from vehicles
        // that are no longer present in the simulation
        for (String removedVehicleId : storedIds) {
            this.vehicles.remove(removedVehicleId);
        }

        // update all vehicles in simulation
        for (SumoVehicle vehicle : this.vehicles.values()) {
            vehicle.update(this.connection);
        }







        if (this.updateListener != null) {
            // ping listener to notify that the
            // simulation state has changed
            this.updateListener.run();
        }
    }

    /**
     * Closes and thereby ends the connection to the SUMO instance.
     */
    public void close() {
        if (this.thread != null && this.thread.isAlive()) {
            this.thread.interrupt();
        }

        if (!this.isClosed()) {
            this.connection.close();
        }
    }

    /**
     * @return whether the connection is closed
     */
    public boolean isClosed() {
        return this.connection.isClosed();
    }

    /**
     * Sets the update listener.
     * Whenever the simulation gets updated,
     * a listener object receives a ping to indicate
     * that new simulation data is available.
     *
     * @param updateListener the update listener
     */
    public void setUpdateListener(Runnable updateListener) {
        this.updateListener = updateListener;
    }

    /**
     * @return the current simulation timestamp in seconds
     */
    public double getTime() {
        return this.time;
    }

    /**
     * @return the duration it took to perform one sumo simulation step
     */
    public long getSimulationStepDuration() {
        return this.simulationStepDuration;
    }

    /**
     * @return the duration it took to sync the java application with sumo
     */
    public long getUpdateStepDuration() {
        return this.updateStepDuration;
    }

    /**
     * @return a list of all edges in the simulation
     */
    public List<SumoEdge> getEdges() {
        return this.edges;
    }

    /**
     * @return a list of all lanes in the simulation
     */
    public List<SumoLane> getLanes() {
        return this.lanes;
    }

    /**
     * @return a collection of all vehicles in the simulation
     */
    public Collection<SumoVehicle> getVehicles() {
        return this.vehicles.values();
    }

    /**
     * Performs a vehicle injection on a random route.
     */
    public void injectVehicle() throws RuntimeException {
        int count = this.routes.size();
        int index = RANDOM.nextInt(0, count);
        SumoRoute randomRoute = this.routes.get(index);

        try {
            this.connection.do_job_set(Vehicle.add(
                    "injectedVeh_" + System.nanoTime() + "_" + RANDOM.nextInt(),
                    "DEFAULT_VEHTYPE",
                    randomRoute.getRouteId(),
                    (int)Math.ceil(this.time),
                    0.0,
                    10.0,
                    (byte)0)
            );
        } catch (Exception exception) {
            throw new RuntimeException("Failed to inject vehicle", exception);
        }
    }

    /**
     * Performs a batch vehicle injection
     *
     * @param batchSize the vehicle count to be inserted
     */
    public void batchInjection(int batchSize) throws RuntimeException {
        for (int i = 0; i < batchSize; ++i){
            this.injectVehicle();
        }
    }
}
