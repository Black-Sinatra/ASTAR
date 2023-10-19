

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rgu.algorithms.NearestFinder;
import rgu.algorithms.TargetUnreachableException;
import rgu.algorithms.collections.BinaryMinHeap;
import rgu.algorithms.collections.Explorable;
import rgu.algorithms.collections.Graph;
import rgu.algorithms.collections.MinHeap;
import rgu.transport.geospatial.Distance;
import rgu.transport.geospatial.GeoLocation;
import rgu.transport.geospatial.osm.RoadEdge;
import rgu.transport.util.DataIO;

public class AStar2 {

    private static Graph<GeoLocation, Distance> getDistanceGraph(Graph<GeoLocation, RoadEdge> graph) {
        return new Graph<>() {
            @Override
            public boolean containsVertex(GeoLocation vertex) {
                return graph.containsVertex(vertex);
            }
            @Override
            public boolean containsEdge(GeoLocation from, GeoLocation to) {
                return graph.containsEdge(from, to);
            }
            @Override
            public Set<GeoLocation> neighbours(GeoLocation vertex) {
                return graph.neighbours(vertex);
            }
            @Override
            public Distance edge(GeoLocation from, GeoLocation to) {
                return graph.edge(from, to).distance();
            }
            @Override
            public Set<GeoLocation> vertices() {
                return graph.vertices();
            }
            @Override
            public Graph<GeoLocation, Distance> reverse() {
                return getDistanceGraph(graph.reverse());
            }
        };
    }

    private static List<GeoLocation> reConstruct(Map<GeoLocation, GeoLocation> previous, GeoLocation current) {
        List<GeoLocation> path = new ArrayList<>();
        GeoLocation currentNode = current;
            
        while (currentNode != null) {
            path.add(currentNode);
                
            currentNode = previous.get(currentNode);
        }
            
        Collections.reverse(path);
        return path;
    }

    private static List<GeoLocation> getShortestPath(Explorable<GeoLocation, Distance> graphstar, GeoLocation source, GeoLocation target) {
        
        MinHeap<GeoLocation, Double> open = new BinaryMinHeap<>((a, b) -> { return a.compareTo(b) < 0; });
        Set<GeoLocation> closed = new HashSet<>();
        Map<GeoLocation, GeoLocation> previous = new HashMap<>();
        Map<GeoLocation, Double> gScore = new HashMap<>();
        Map<GeoLocation, Double> fScore = new HashMap<>();

        previous.put(source, null);
        gScore.put(source, 0.0);
        fScore.put(source, GeoLocation.HAVERSINE.distance(source, target).asMeters());
        open.addElement(source, 0.0);


        while (!open.isEmpty()) {
            GeoLocation current = open.deleteMinimum();

            if (current.equals(target)) {
                return reConstruct(previous, current);
            }

            closed.add(current);

            Set<GeoLocation> neighbor = graphstar.neighbours(current);

            for (GeoLocation child : neighbor) {

                double newGScore = gScore.get(current) + graphstar.edge(current, child).asMeters();

                if (closed.contains(child)) {
                    if (newGScore >= gScore.get(child)) {
                        continue;
                    }
                }

                if (!fScore.containsKey(child) || newGScore < gScore.get(child)) {
                    previous.put(child, current);
                    gScore.put(child, newGScore);

                    Double newFScore = newGScore + GeoLocation.HAVERSINE.distance(child, target).asMeters();
                    if (!fScore.containsKey(child)) {
                        open.addElement(child, newFScore);
                        fScore.put(child, newFScore);
                    }
                }
            }
        }
        throw new IllegalStateException("Path Not Found");
    }
    public static void main(String[] args) throws InterruptedException, TargetUnreachableException {
        
        File file = new File("highway-maxspeed.roads");
        Graph<GeoLocation, RoadEdge> roadGraph;

        try {
            System.out.println("loading graph . . .");
            roadGraph = DataIO.readRoads(file);
        } catch (IOException ex) {
            System.err.println("Unable to load data.");
            ex.printStackTrace();
            return;
        }

        Graph<GeoLocation, Distance> graph1 = getDistanceGraph(roadGraph);
        
        NearestFinder<GeoLocation> find = GeoLocation.nearestHaversine(graph1.vertices());
        
        GeoLocation firstpoint = GeoLocation.parseLocation("57.43", "-4.36");
        
        GeoLocation endpoint = GeoLocation.parseLocation("57.5953", "-4.4284");
        
        GeoLocation start = find.nearest(firstpoint);
        System.out.println("Start: " + firstpoint);
        
        GeoLocation end = find.nearest(endpoint);
        System.out.println("End: " + end);
        
        long now = System.nanoTime();

        List<GeoLocation> travel; 

        travel = getShortestPath(graph1, start, end);
//            System.out.println(travel1);
        
        Duration runtime = Duration.ofNanos(System.nanoTime() - now);

        // check how long the path is
        System.out.println("found path from start to end using "
                + travel.size() + " vertices (" + (travel.size()-1) + " edges)");
        System.out.println("how long the computation took: " + (runtime.toNanos() / 1000000000.0) + " seconds");

        // we can print out the path
        int i = 0;
        for (GeoLocation v : travel) {
            System.out.println("path[" + i++ + "]: " + v);
        }

    }
}
