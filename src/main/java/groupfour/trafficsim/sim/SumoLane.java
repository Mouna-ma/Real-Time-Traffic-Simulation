package groupfour.trafficsim.sim;

import de.tudresden.sumo.cmd.Lane;
import de.tudresden.sumo.objects.SumoGeometry;
import de.tudresden.sumo.objects.SumoPosition2D;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Wrapper class for a sumo lane
 *
 * @author 8wf92323f
 */
public class SumoLane {
    private final String laneId;
    private final double[][] geometry;
    private final double laneWidth;
    private final SumoEdge edge;

    public SumoLane(String laneId, SumoEdge edge, SumoTraciConnection connection) throws Exception {
        this.laneId = laneId;
        this.edge = edge;

        this.laneWidth = (double)connection.do_job_get(Lane.getWidth(this.laneId));

        SumoGeometry geometry = (SumoGeometry)connection.do_job_get(Lane.getShape(laneId));
        this.geometry = new double[geometry.coords.size()][2];

        for (int i = 0; i < geometry.coords.size(); ++i) {
            SumoPosition2D pos = geometry.coords.get(i);
            this.geometry[i][0] = pos.x;
            this.geometry[i][1] = pos.y;
        }
    }

    /**
     * @return the id of the lane
     */
    public String getLaneId() {
        return this.laneId;
    }

    /**
     * @return the lanes geometry given as an array
     *         of 2d coordinates
     *         [[x0, y0], [x1, y1], ...]
     */
    public double[][] getGeometry() {
        return this.geometry;
    }

    /**
     * @return the width of the lane
     */
    public double getLaneWidth() {
        return this.laneWidth;
    }

    /**
     * @return the edge on which the lane is located
     */
    public SumoEdge getEdge() {
        return this.edge;
    }
}
