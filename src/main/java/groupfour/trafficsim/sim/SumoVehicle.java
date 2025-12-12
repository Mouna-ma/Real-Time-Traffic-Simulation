package groupfour.trafficsim.sim;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import it.polito.appeal.traci.SumoTraciConnection;

public class SumoVehicle {

    private final String vehId;
    private SumoPosition2D position;
    private SumoColor color;
    private double speed;
    private double maxSpeed;

    public SumoVehicle(String vehId, SumoPosition2D position, SumoColor color, double speed, double maxSpeed){
        this.vehId = vehId;
        this.position =  position;
        this.color = color;
        this.speed = speed;
        this.maxSpeed = maxSpeed;
    }

    public String get_vehId(){
        return this.vehId;
    }
    public SumoPosition2D get_position(){
        return this.position;
    }
    public SumoColor get_color(){
        return this.color;
    }
    public double get_speed(){
        return this.speed;
    }
    public double get_maxSpeed(){
        return this.maxSpeed;
    }

    public void update(SumoTraciConnection connection) throws Exception {
        this.position = (SumoPosition2D)connection.do_job_get(Vehicle.getPosition(this.vehId));
        this.speed = (double)connection.do_job_get(Vehicle.getSpeed(this.vehId));
        this.maxSpeed = (double)connection.do_job_get(Vehicle.getMaxSpeed(this.vehId));
        this.color = (SumoColor)connection.do_job_get(Vehicle.getColor(this.vehId));
    }
}
