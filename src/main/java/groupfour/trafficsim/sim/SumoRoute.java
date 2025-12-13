package groupfour.trafficsim.sim;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper class for sumo vehicle routes
 *
 * @author 8wf92323f
 */
public class SumoRoute {
    private final String routeId;
    private final List<SumoEdge> edges = new ArrayList<>();

    public SumoRoute(String routeId, String[] edgeIds, Simulation simulation) {
        this.routeId = routeId;

        for (String edgeId : edgeIds) {
            for (SumoEdge edge : simulation.getEdges()) {
                if (edge.getEdgeId().equals(edgeId)) {
                    this.edges.add(edge);
                }
            }
        }
    }

    /**
     * @return the routes id
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * @return a list of ordered edges contained on the route
     */
    public List<SumoEdge> getEdges() {
        return this.edges;
    }
}
