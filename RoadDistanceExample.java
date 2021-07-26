package ASTAR;

// rgu-algorithms imports
import rgu.algorithms.*;
import rgu.algorithms.collections.*;

// rgu-transport imports
import rgu.transport.geospatial.*;
import rgu.transport.geospatial.osm.*;
import rgu.transport.util.*;

// standard library imports
import java.io.*;
import java.time.*;
import java.util.*;

public class RoadDistanceExample {

    // adapter from road graph to road distance graph
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

    public static void main(String[] args) throws IOException {

        // load the pre-constructed sample graph (only needs to be done once)
        File file = new File("C:\\Users\\HARFOE\\Documents\\CMM512\\RoadExample\\highway-maxspeed.roads");
        System.out.println(file.getAbsoluteFile().toString());
        Graph<GeoLocation, RoadEdge> roadGraph;
        try {
            System.out.println("loading graph . . .");
            roadGraph = DataIO.readRoads(file);
        } catch (IOException ex) {
            System.err.println("Unable to load data.");
            ex.printStackTrace();
            return;
        }
        Graph<GeoLocation, Distance> graph = getDistanceGraph(roadGraph);

        // this is some random point somewhere near Inverness
        // we can't search from here thought, because it's not on the network
        GeoLocation examplePoint = GeoLocation.parseLocation("57.43", "-4.36");
        System.out.println("example point: " + examplePoint);

        // setup used to find the nearest point on the network (only needs to be done once)
        NearestFinder<GeoLocation> finder = GeoLocation.nearestHaversine(graph.vertices());

        // find a point near to out example point, but actually on the network
        // we should find a point quite close to our example point
        GeoLocation startPoint = finder.nearest(examplePoint);
        System.out.println("start point: " + startPoint);

        // we can get the neighbours of our start point
        Set<GeoLocation> neighbours = graph.neighbours(startPoint);

        // here are the neighbours we found
        int i = 0;
        for (GeoLocation n : neighbours) {
            System.out.println("neighbour " + i++ + ": " + n);
        }

        // let's take the first one as an example
        GeoLocation neighbour = new ArrayList<>(neighbours).get(0);

        // we can get the edge cost between the two
        Distance cost = graph.edge(startPoint, neighbour);
        System.out.println("cost for start point to neighbour 0: " + cost);

        // let's say we want to get to another point (this one is near Dingwall)
        // we need to use the finder again to find a point on the graph
        GeoLocation examplePoint2 = GeoLocation.parseLocation("57.5953", "-4.4284");
        GeoLocation endPoint = finder.nearest(examplePoint2);
        System.out.println("end point: " + endPoint);

        // we have Dijkstra's shortest path algorithm implemented in our code base
        // the interface is called RoutingAlgorithm
        RoutingAlgorithm<Distance> algorithm = Dijkstra.of(
                Distance.ZERO, (a, b) -> Distance.ofMeters(a.asMeters() + b.asMeters()),
                (a, b) -> a.asMeters() < b.asMeters());

        // the method we're going to use in RoutingAlgorithm is path(Explorable explorable, V source, V target)
        // Explorable is an interface implemented by the Graph object, which contains "neighbours" and "edge" methods
        long now = System.nanoTime(); // checking how long this takes
        List<GeoLocation> path;
        try {
            path = algorithm.path(graph, startPoint, endPoint);
        } catch (TargetUnreachableException ex) {
            System.err.println("Cannot reach the target!");
            ex.printStackTrace();
            return;
        } catch (InterruptedException ex) {
            System.err.println("Dijkstra thread was cancelled by an interrupt, aborting.");
            return;
        }
        Duration runtime = Duration.ofNanos(System.nanoTime() - now);

        // check how long the path is
        System.out.println("found path from start to end using "
                + path.size() + " vertices (" + (path.size()-1) + " edges)");
        System.out.println("how long the computation took: " + (runtime.toNanos() / 1000000000.0) + " seconds");

        // we can print out the path
        i = 0;
        for (GeoLocation v : path) {
            System.out.println("path[" + i++ + "]: " + v);
        }

        // and check the total path length (distance)
        // this method just added up the total edge cost for all the adjacent vertices in the graph
        Distance totalCost = ((Dijkstra<Distance>) algorithm).cost(graph, path);
        System.out.println("travel distance: " + totalCost);

    }

}
