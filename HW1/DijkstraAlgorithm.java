//package Dijkstra;
import java.util.*;
public class DijkstraAlgorithm {
    private Set<Vertex> visited;
    private Set<Vertex> unvisited;
    private Map<Vertex, Vertex> pre;
    private Map<Vertex, Integer> distance;
    private final List<Vertex> nodes;
    private final List<Edge> edges;
    
    public DijkstraAlgorithm(Graph graph) {
        this.nodes = new ArrayList<Vertex>(graph.getVertexes());
        this.edges = new ArrayList<Edge>(graph.getEdges());
    }

    public void run_dij(Vertex start) {
        visited = new HashSet<Vertex>();
        unvisited = new HashSet<Vertex>();
        distance = new HashMap<Vertex, Integer>();
        pre = new HashMap<Vertex, Vertex>();
        distance.put(start, 0);
        unvisited.add(start);
        while (unvisited.size() > 0) {
            Vertex node = get_min(unvisited);
            visited.add(node);
            unvisited.remove(node);
            findMinimalDistances(node);
        }
    }
    private Vertex get_min(Set<Vertex> vertexes) {
        Vertex minimum = null;
        for (Vertex vertex : vertexes) {
            if (minimum == null) {
                minimum = vertex;
            } else {
                if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                    minimum = vertex;
                }
            }
        }
        return minimum;
    }
    private void findMinimalDistances(Vertex node) {
        List<Vertex> adjacentNodes = getNeighbors(node);
        for (Vertex target : adjacentNodes) {
            if (getShortestDistance(target) > getShortestDistance(node)
                    + getDistance(node, target)) {
                distance.put(target, getShortestDistance(node)
                        + getDistance(node, target));
                pre.put(target, node);
                unvisited.add(target);
            }
        }

    }

    private int getDistance(Vertex node, Vertex target) {
        for (Edge edge : edges) {
            if (edge.getSource().equals(node)
                    && edge.getDestination().equals(target)) {
                return edge.getWeight();
            }
        }
        throw new RuntimeException("Should not happen");
    }

    private List<Vertex> getNeighbors(Vertex node) {
        List<Vertex> neighbors = new ArrayList<Vertex>();
        for (Edge edge : edges) {
            if (edge.getSource().equals(node)
                    && !isSettled(edge.getDestination())) {
                neighbors.add(edge.getDestination());
            }
        }
        return neighbors;
    }

    

    private boolean isSettled(Vertex vertex) {
        return visited.contains(vertex);
    }

    private int getShortestDistance(Vertex destination) {
        Integer d = distance.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }

    public LinkedList<Vertex> getPath(Vertex target) {
        LinkedList<Vertex> path = new LinkedList<Vertex>();
        Vertex step = target;
        if (pre.get(step) == null) {
            return null;
        }
        path.add(step);
        while (pre.get(step) != null) {
            step = pre.get(step);
            path.add(step);
        }
        Collections.reverse(path);
        return path;
    }

}
