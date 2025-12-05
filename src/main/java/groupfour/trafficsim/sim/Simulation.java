package groupfour.trafficsim.sim;

import it.polito.appeal.traci.SumoTraciConnection;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Simulation {
    private static final Logger LOGGER = Logger.getLogger(Simulation.class.getName());
    private final SumoTraciConnection connection;
    private Thread thread;

    public Simulation(String binary, String configFile) {
        this.connection = new SumoTraciConnection(binary, configFile);

        double stepFrequency = 2.0; // steps/sec
        double simulationSpeed = 1.0;
        double stepLength = simulationSpeed / stepFrequency;
        double delay = 1000.0 / simulationSpeed;

        this.connection.addOption("start", "true");
        this.connection.addOption("step-length", Double.toString(stepLength));
        this.connection.addOption("delay", Double.toString(delay));

        try {
            this.connection.runServer();
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Could not start Server", exception);
            throw new RuntimeException(exception);
        }
    }

    public void step() {
        if (this.isClosed()) {
            throw new IllegalStateException("Connection is closed!");
        }

        // discard if previous thread is still running
        if (this.thread != null && this.thread.isAlive()) return;

        this.thread = new Thread(() -> {
            try {
                this.connection.do_timestep();
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "Exception during timestep", exception);
            }
        });

        this.thread.start();
    }

    public void close() {
        if (this.thread != null && this.thread.isAlive()) return; // discard

        if (!this.isClosed()) {
            this.connection.close();
        }
    }

    public boolean isClosed() {
        return this.connection.isClosed();
    }
}
