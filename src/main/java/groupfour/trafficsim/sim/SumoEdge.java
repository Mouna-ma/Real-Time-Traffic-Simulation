package groupfour.trafficsim.sim;

import de.tudresden.sumo.cmd.Edge;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for a sumo edge
 *
 * @author 8wf92323f
 */
public class SumoEdge {
    private final String edgeId;
    private final List<SumoLane> lanes = new ArrayList<>();

    public SumoEdge(String edgeId, SumoTraciConnection connection) throws Exception {
        this.edgeId = edgeId;

        int laneCount = (int)connection.do_job_get(Edge.getLaneNumber(this.edgeId));

        for (int i = 0; i < laneCount; ++i) {
            SumoLane lane = new SumoLane(this.edgeId + "_" + i, this, connection);
            this.lanes.add(lane);
        }
    }

    /**
     * @return the id of the edge
     */
    public String getEdgeId() {
        return this.edgeId;
    }

    /**
     * @return a list of all lanes on the edge
     */
    public List<SumoLane> getLanes() {
        return this.lanes;
    }
}
