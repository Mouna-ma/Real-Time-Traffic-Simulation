package groupfour.trafficsim.sim;

import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.cmd.Trafficlight;
import it.polito.appeal.traci.SumoTraciConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A wrapper for the connection to a SUMO instance.
 * All simulation control tasks are managed by this class.
 *
 * @author 8wf92323f
 */
public class Simulation {
    private static final Logger LOGGER = LogManager.getLogger(Simulation.class.getName());
    private final SumoTraciConnection connection;
    private Thread thread;
    private Runnable updateListener;
    private volatile boolean shouldStopSimulation = true;
    private double time = 0.0;
    private final List<SumoEdge> edges = new ArrayList<>();
    private final List<SumoLane> lanes = new ArrayList<>();
    private final Map<String, SumoTrafficLight> trafficLights = new ConcurrentHashMap<>();
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

        while (!this.shouldStopSimulation) {
            try {
                long stepStartNanoTime = System.nanoTime();

                this.connection.do_timestep();
                this.update();

                long stepEndNanoTime = System.nanoTime();

                long stepNanoDuration = stepEndNanoTime - stepStartNanoTime;
                long sleepNanoTime = expectedStepNanoDuration - stepNanoDuration;

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

        for(SumoTrafficLight tls : this.trafficLights.values()) {
            tls.updateState();
        }
        // update checks here

        if (this.updateListener != null) {
            // ping listener to notify that the
            // simulation state has changed
            this.updateListener.run();
        }
    }

    //returns the complete TrafficLight Ids of the simulation
    public List<String> getTrafficLightIds() throws Exception{
        return(List<String>) connection.do_job_get(Trafficlight.getIDList());
    }

    public SumoTrafficLight getTrafficLight(String tlsId) {
        return trafficLights.computeIfAbsent(tlsId, id -> {
            try {
                SumoTrafficLight t1 = new SumoTrafficLight(id, this.connection);
                return t1;
            } catch (Exception e) {
                System.err.println("Trafficlight failed");
                return null;
            }
        });
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

    public SumoTraciConnection getConnection() {
        return this.connection;
    }

    /**
     * @return the current simulation timestamp in seconds
     */
    public double getTime() {
        return this.time;
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
}
