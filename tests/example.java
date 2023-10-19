/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ASTAR;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import rgu.algorithms.NearestFinder;
import rgu.algorithms.RoutingAlgorithm;
import rgu.algorithms.TargetUnreachableException;
import rgu.algorithms.collections.Graph;
import rgu.transport.geospatial.Distance;
import rgu.transport.geospatial.GeoLocation;
import rgu.transport.geospatial.osm.RoadEdge;
import rgu.transport.util.DataIO;

/**
 *
 * @author HARFOE
 */
public class example {
    
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
    
    public static void main(String[] args) {
        File file = new File("C:\\Users\\HARFOE\\Documents\\NetBeansProjects\\RoadExample\\highway-maxspeed.roads");
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
        Graph<GeoLocation, Distance> graph1 = getDistanceGraph(roadGraph);
        
        NearestFinder<GeoLocation> find = GeoLocation.nearestEuclidean(graph1.vertices());
        
        GeoLocation firstpoint = GeoLocation.parseLocation("57.43", "-4.36");
        
        GeoLocation endpoint = GeoLocation.parseLocation("57.5953", "-4.4284");
        
        GeoLocation start = find.nearest(firstpoint);
        System.out.println("Start: " + firstpoint);
        
        GeoLocation end = find.nearest(endpoint);
        System.out.println("End: " + end);
        
        long now = System.nanoTime();
        
//        astarex algorithm = new astarex();
        RoutingAlgorithm<Distance> algorithm = new astarex();
        
        List<GeoLocation> travel1;
        
        try {
            travel1 = algorithm.path(graph1, start, end);
            System.out.println(travel1);
        } catch (TargetUnreachableException ex) {
            System.err.println("Cannot reach the target!");
            ex.printStackTrace();
            return;
        } catch (InterruptedException ex) {
            System.err.println("Aborting. Interrupted.");
            return;
        }
        
        Duration runtime = Duration.ofNanos(System.nanoTime() - now);

        // check how long the path is
        System.out.println("found path from start to end using "
                + travel1.size() + " vertices (" + (travel1.size()-1) + " edges)");
        System.out.println("how long the computation took: " + (runtime.toNanos() / 1000000000.0) + " seconds");

        // we can print out the path
        int i = 0;
        for (GeoLocation v : travel1) {
            System.out.println("path[" + i++ + "]: " + v);
        }


    }
}
