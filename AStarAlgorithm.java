/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ASTAR;

import java.util.*;
import java.util.function.Predicate;
import rgu.algorithms.*;
import rgu.algorithms.TargetUnreachableException;
import rgu.algorithms.collections.BinaryMinHeap;
import rgu.algorithms.collections.Explorable;
import rgu.algorithms.collections.MinHeap;
import rgu.transport.geospatial.Distance;
import rgu.transport.geospatial.GeoLocation;

/**
 *
 * @author HARFOE
 */
public class AStarAlgorithm implements RoutingAlgorithm<Distance> {
    
    public AStarAlgorithm(){
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <V> List<V> path(Explorable<V, Distance> graph, V source, V target) throws TargetUnreachableException, InterruptedException {
//        if (target instanceof GeoLocation) {
            List<GeoLocation> rv = geoLocationPath((Explorable<GeoLocation, Distance>) graph, (GeoLocation) source, (GeoLocation) target);
            return (List<V>) rv;
//        } else {
//            throw new IllegalArgumentException();
//        }
    }
    
    private List<GeoLocation> geoLocationPath(Explorable<GeoLocation, Distance> graphstar, GeoLocation source, GeoLocation target) throws TargetUnreachableException, InterruptedException{
        
        MinHeap<GeoLocation, Distance> OPEN = new BinaryMinHeap<>((a, b) -> { return a.compareTo(b) < 0; });        
        Set<GeoLocation> CLOSED = new HashSet<>();       
        Map<GeoLocation, Distance> DISTANCE = new HashMap<>();
        Map<GeoLocation, GeoLocation> PARENTS = new HashMap<>();
        
        
        OPEN.addElement(source, Distance.ofMeters(0.0));
        DISTANCE.put(source, Distance.ofMeters(0.0));
        PARENTS.put(source, null);

        
        
        while (!OPEN.isEmpty()) {
            GeoLocation currentNode = OPEN.deleteMinimum();
//            System.out.println("Current Node is: " + currentNode);
            
            if (currentNode.equals(target)) {
                return tracebackPath(currentNode, PARENTS); 
            }
            
            if (CLOSED.contains(currentNode)) {
                continue;
            }
            
            CLOSED.add(currentNode);
            
            Set<GeoLocation> neighbors = graphstar.neighbours(currentNode);
            
            for (GeoLocation childNode : neighbors) {
//                System.out.println("Neighbors: " + childNode);
                if (CLOSED.contains(childNode)) {
                    continue;
                } 
               
                
                Distance tentativeDistance = Distance.ofMeters(DISTANCE.get(currentNode).asMeters() + 
                        graphstar.edge(currentNode, childNode).asMeters());
          
                if (!DISTANCE.containsKey(childNode) || DISTANCE.get(childNode).asMeters() > 
                        tentativeDistance.asMeters()) {
                    DISTANCE.put(childNode, tentativeDistance);
                    PARENTS.put(childNode, currentNode);
                    OPEN.addElement(childNode, Distance.ofMeters(tentativeDistance.asMeters() + 
                            GeoLocation.HAVERSINE.distance(childNode, target).asMeters()));
                }
            }
        }
        return new ArrayList<>();
    }
    
    private static List<GeoLocation> tracebackPath(GeoLocation target, Map<GeoLocation, GeoLocation> PARENTS) {
            List<GeoLocation> path = new ArrayList<>();
            GeoLocation currentNode = target;
            
            while (currentNode != null) {
                path.add(currentNode);
                currentNode = PARENTS.get(currentNode);
                System.out.println("Traceback path: " + currentNode);
            }
            
            Collections.reverse(path);
            System.out.println("Traceback path: " + path);
            return path;
        }

    @Override
    public <V> List<V> path(Explorable<V, Distance> e, V v, V v1, Distance e1) throws TargetUnreachableException, InterruptedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <V> List<V> path(Explorable<V, Distance> e, V v, Predicate<V> prdct) throws TargetUnreachableException, InterruptedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <V> List<V> path(Explorable<V, Distance> e, V v, Predicate<V> prdct, Distance e1) throws TargetUnreachableException, InterruptedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
