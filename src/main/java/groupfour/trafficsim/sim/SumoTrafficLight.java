package groupfour.trafficsim.sim;

import de.tudresden.sumo.cmd.Trafficlight;
import de.tudresden.sumo.objects.SumoTLSController;
import de.tudresden.sumo.objects.SumoTLSProgram;
import it.polito.appeal.traci.SumoTraciConnection;

public class SumoTrafficLight {
    private final SumoTraciConnection connection;
    private final String tlsId;
    private int PhaseIndex;
    private String StateString;
    private double Duration;
    private int totalPhases;

    public SumoTrafficLight(String tlsId, SumoTraciConnection connection){
        this.tlsId = tlsId;
        this.connection = connection;
        loadPhaseCount();
    }
    //gets the current State of the phase, StateString and Duration
    public void updateState() throws Exception {
        this.PhaseIndex = (Integer) connection.do_job_get(Trafficlight.getPhase(this.tlsId));
        this.StateString = (String) connection.do_job_get(Trafficlight.getRedYellowGreenState(this.tlsId));
        this.Duration = (Double) connection.do_job_get(Trafficlight.getPhaseDuration(this.tlsId));
    }
    //gets the total amount of phases in a trafficlight
    private void loadPhaseCount() {
        try {
            Object defObj = connection.do_job_get(Trafficlight.getCompleteRedYellowGreenDefinition(tlsId));
            if (defObj instanceof SumoTLSController controller) {
                SumoTLSProgram prog = controller.programs.values().iterator().next();
                this.totalPhases = prog.phases.size();
            } else totalPhases = 4;
        } catch (Exception e) {
            totalPhases = 4;
        }
    }
    //sets the new state for the phase
    public void setPhase(int phase) throws Exception {
        connection.do_job_set(Trafficlight.setPhase(tlsId, phase));
    }

    public int getPhaseIndex() { return PhaseIndex; } //returns the PhaseIndex
    public String getStateString() { return StateString; } //returns the current state string
    public double getDuration() { return Duration; } //returns the duration for the current phase
    public int getTotalPhases() { return totalPhases; } //return the total amount of Phases in a TLS
    public String getId() { return tlsId; } //returns the TLS ID
}
