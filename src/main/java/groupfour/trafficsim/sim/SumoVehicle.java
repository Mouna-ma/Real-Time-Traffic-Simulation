package groupfour.trafficsim.sim;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * A wrapper class for SUMO vehicles
 *
 * @author mikey7303, 8wf92323f
 */
public class SumoVehicle {
    private final String vehId;
    private SumoPosition2D position;
    private SumoColor color;
    private double speed;
    private double maxSpeed;

    public SumoVehicle(String vehId) {
        this.vehId = vehId;
        this.position = new SumoPosition2D();
        this.color = new SumoColor();
        this.speed = 0.0;
        this.maxSpeed = 0.0;
    }

    public String get_vehId() {
        return this.vehId;
    }

    public SumoPosition2D get_position() {
        return this.position;
    }

    public SumoColor get_color() {
        return this.color;
    }

    public double get_speed() {
        return this.speed;
    }

    public double get_maxSpeed() {
        return this.maxSpeed;
    }

    /**
     * Updates the vehicles parameters by fetching them from the SUMO connection
     * @param connection the SUMO connection
     * @throws Exception if an api error occurs
     */
    public void update(SumoTraciConnection connection) throws Exception {
        this.position = (SumoPosition2D)connection.do_job_get(Vehicle.getPosition(this.vehId));
        this.speed = (double)connection.do_job_get(Vehicle.getSpeed(this.vehId));
        this.maxSpeed = (double)connection.do_job_get(Vehicle.getMaxSpeed(this.vehId));
    }
}
