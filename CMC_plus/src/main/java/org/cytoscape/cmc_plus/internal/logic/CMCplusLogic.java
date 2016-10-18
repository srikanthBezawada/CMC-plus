package org.cytoscape.cmc_plus.internal.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cytoscape.cmc_plus.internal.view.CMCplusUI;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.graph.SimpleGraph;

public class CMCplusLogic extends Thread{
    
    private boolean stop;
    private CMCplusUI panel;
    private CyNetwork network;
    private CyNetworkView networkView;
    private Double mergeThreshold;
    
    public CMCplusLogic(CMCplusUI panel, CyNetwork network, CyNetworkView networkview, Double mergeThreshold) {
        this.panel = panel;
        this.network = network;
        this.networkView = networkView;
        this.mergeThreshold = mergeThreshold;
    }
    
    public void run() {
        stop = false;
        panel.startComputation();
        long startTime = System.currentTimeMillis();
        
        
        Set<Complex> cmcComplexes = new HashSet<Complex>();
        CyRootNetwork root = ((CySubNetwork)network).getRootNetwork();
        
        UndirectedGraph<CyNode, CyEdge> g = new SimpleGraph<CyNode, CyEdge>(CyEdge.class);
        List<CyNode> nodeList = network.getNodeList();
        List<CyEdge> edgeList = network.getEdgeList();
        
        if(stop) {
            return;
        }
        
        for(CyNode n : nodeList){
            g.addVertex(n);
        }
        for(CyEdge e : edgeList){
            if(e.getSource().equals(e.getTarget())){
                continue; // removing self-loops
            }
            g.addEdge(e.getSource(), e.getTarget(),e);
        }
        BronKerboschCliqueFinder bcfinder = new BronKerboschCliqueFinder(g);
        
        if(stop) {
            return;
        }
        
        List<Set<CyNode>> requiredNodeSetsList = (List<Set<CyNode>>) bcfinder.getAllMaximalCliques();
        
        for(Set<CyNode> requiredNodes : requiredNodeSetsList) {
            List<CyEdge> requiredEdges = new ArrayList<CyEdge>();
            for(CyEdge e : edgeList){
                if(requiredNodes.contains(e.getSource()) && requiredNodes.contains(e.getTarget())){
                    requiredEdges.add(e);
                }
            }
            // Removing very small complexes
            if(requiredEdges.size() >= 3) {
                CyNetwork subNet = root.addSubNetwork(requiredNodes, requiredEdges);
                cmcComplexes.add(new Complex(subNet));
            }
        }
        if(stop) {
            return;
        }
        
        if(cmcComplexes == null) {
            return;
            // TODO : notify user ?
        }
        
        Set<Complex> cmcComplexesMerged = Merger.merge(cmcComplexes, network, mergeThreshold);
        
        
        panel.resultsCalculated(cmcComplexesMerged, network);
        
        if(stop) {
            return;
        }
        
        
        long endTime = System.currentTimeMillis();
        long difference = endTime - startTime;
        System.out.println("Execution time for CMC " + difference +" milli seconds");
        panel.endComputation();
    }
    
    public void end() {
        stop = true;
    }
}
