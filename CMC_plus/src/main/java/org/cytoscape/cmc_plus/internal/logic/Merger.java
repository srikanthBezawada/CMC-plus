package org.cytoscape.cmc_plus.internal.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class Merger {
    
    public static Set<Complex> merge(Set<Complex> clusters, CyNetwork network, double overlapValue) {
        Set<Complex> newClusters = new HashSet<Complex>();
        List<Complex> unmergedLists = new ArrayList<Complex>();
        unmergedLists.addAll(clusters);
        
        UndirectedGraph<Complex, DefaultEdge> g = new SimpleGraph<Complex, DefaultEdge>(DefaultEdge.class);
        for(Complex cluster : clusters) {
            g.addVertex(cluster);
        }
        
        Iterator<Complex> outer = unmergedLists.iterator();
        
        while(outer.hasNext()) {
            Complex C1 = outer.next();
            Iterator<Complex> inner = unmergedLists.iterator();
            
            while(inner.hasNext()) {
                Complex C2 = inner.next();
                
                if(C1.equals(C2)){
                    continue;
                }
                
                if(jaccardSimilarity(C1, C2) >= overlapValue) {
                    g.addEdge(C1, C2);
                }
            }
        }
        
        
        
        ConnectivityInspector<Complex, DefaultEdge> inspector = new ConnectivityInspector<Complex, DefaultEdge>(g);
        List<Set<Complex>> connComponents = inspector.connectedSets();
        
        System.out.println("Connected components" + connComponents.size());
        
        for(Set<Complex> component : connComponents) {
            if(component.isEmpty() == false){
                newClusters.add(mergeComponent(component, network));
            }
        }
        
        return newClusters;
    }
        
    
    public static double matchCoefficient(Complex C1, Complex C2) {
        double inter = intersection(C1.getNodes(), C2.getNodes()).size();
        double matchCoeff = (inter * inter)/(C1.getNodes().size() * C2.getNodes().size());
    
        return matchCoeff;
    }
    
    public static double simpsonCoefficient(Complex C1, Complex C2) {
        double inter = intersection(C1.getNodes(), C2.getNodes()).size();
        double smallerComplexsize;
        if(C1.getNodes().size() > C2.getNodes().size()) {
            smallerComplexsize = C2.getNodes().size();
        } else{
            smallerComplexsize = C1.getNodes().size();
        }
        
        return inter/smallerComplexsize ;
    }
    
    // Using this for merging
    public static double jaccardSimilarity(Complex C1, Complex C2) {
        double inter = intersection(C1.getNodes(), C2.getNodes()).size();
        double union = union(C1.getNodes(), C2.getNodes()).size();
        
        return inter/union;
    }
    
    public static double diceSimilarity(Complex C1, Complex C2) {
        double inter = intersection(C1.getNodes(), C2.getNodes()).size();
        return (2*inter)/(C1.getNodes().size() + C2.getNodes().size());
    }
    
    
    public static Complex mergeComponent(Set<Complex> component, CyNetwork network) {
        Set<CyNode> nodesUnion = new HashSet<CyNode>();
        Set<CyEdge> edgesUnion = new HashSet<CyEdge>();
        CyRootNetwork root = ((CySubNetwork)network).getRootNetwork();
        if(component.isEmpty()) {
            return null;
        }
        for(Complex C : component) {
            if(component.size() == 1) {// handled already
                return C;
            } else{
                nodesUnion.addAll(C.getNodes());
                edgesUnion.addAll(C.getEdges());
                CyNetwork mergedNetwork = root.addSubNetwork(nodesUnion, edgesUnion);
                Complex mergedCluster = new Complex(mergedNetwork);
                return mergedCluster;
            }
        }
        return null;
    }
    
    public static Set<CyNode> intersection(List<CyNode> setA, List<CyNode> setB) {
        Set<CyNode> tmp = new HashSet<CyNode>();
        for (CyNode x : setA) {
            if (setB.contains(x)) {
                tmp.add(x);
            }
        }
        return tmp;    
    }
    
    public static Set<CyNode> union(List<CyNode> setA, List<CyNode> setB) {
        Set<CyNode> tmp = new HashSet<CyNode>();
        for (CyNode x : setA) {
            tmp.add(x);
        }
        for (CyNode x : setB) {
            tmp.add(x);
        }
        return tmp;    
    }
}
