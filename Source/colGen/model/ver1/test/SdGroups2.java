package colGen.model.ver1.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.heuristic.BaseHeuristic2;
import colGen.model.heuristic.HuerVarZ;
import colGen.model.heuristic.BaseHeuristic2.SdDetails;
import colGen.model.preprocess.PreProcVer1;
import colGen.model.preprocess.placeNFVI;
import colGen.model.preprocess.preProcFunctions;
import colGen.model.ver1.CG;
import colGen.model.ver1.VertexRank;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class SdGroups2 {
	public static void runTest() throws Exception{
		//generate the graph object
	    Graph g = PreProcVer1.makeGraphObject();
	    //set all nodes to type switches
	    preProcFunctions.makeAllVrtSwitches(g);
	    //populate ChainSet details
	    InputConstants.populateServices();
	    //print the graph object
	    preProcFunctions.printGraph(g);
	    
	    //calculate betweenness centrality
	    Map<BaseVertex,Double> bcOfVertex = preProcFunctions.calculateBetweenessCentrality(g);
	    
	    //calculate ranking of vertices based on 
	    //product of betweeness-centrality and degree centrality
	    Map<BaseVertex,Double> vertexRank = preProcFunctions.calProductOfBCandDeg(g,bcOfVertex);
	    //list of vertex ranks
	    List<VertexRank> rankList = new ArrayList<VertexRank>();
	    //make list
	    for(Map.Entry<BaseVertex, Double> entry : vertexRank.entrySet()){
	    	VertexRank obj = new VertexRank(entry.getKey(),entry.getValue());
	    	rankList.add(obj);
	    }	  
	    //sort list in descending order
	    Collections.sort(rankList);
	    //print out the vertex Ranking
	    preProcFunctions.printVertexRanking(rankList);
	    
	    //generate the routes for the traffic pairs
	    HashMap<NodePair,List<Path>> sdpaths = preProcFunctions.findRoutesForSDpairs(g);
	    
	    //get the set of service chains
	  	Map<Integer,ServiceChain> ChainSet = PreProcVer1.populateChainSetBasedOnScenario1();
		// print out the Set of Service Chains
		preProcFunctions.printServiceChains(ChainSet);
		
		//total list of VNF available
		List<FuncPt> vnf_list = PreProcVer1.totalListOfVNFs();
		
		//NFV nodes 
		//All nodes are NFV capable
		int numOfNfvNodes = 24;
				
		// SD pairs between which we desire traffic to be
		// Store each s-d pair
		List<TrafficNodes> pair_list = new ArrayList<TrafficNodes>();
		//generate all (s,d) pairs
		for(BaseVertex srcVrt: g.get_vertex_list()){
			for(BaseVertex destVrt: g.get_vertex_list()){
				if(srcVrt.get_id() != destVrt.get_id()){					
					pair_list.add(new TrafficNodes(srcVrt,destVrt,0,1000));					
				}
			}			
		}
		//for(TrafficNodes tn : pair_list){ tn.updateTraffic(1000); }
	    //List<TrafficNodes> pair_list = PreProcVer1.setOfSDpairs(sdpaths);		
		//Using Traffic Generator for generating sd pairs
		//List<TrafficNodes> pair_list = TrafficGenerator.getTrafficPairs(InputConstants.trafficLoad, 1, g);
		// Generate traffic pairs for a single service chain
		//"web","voip","videostream","cloudgame"	
		//List<TrafficNodes> pair_list = TrafficGenerator.generateDistinctPairsAndAllocateTraffic(InputConstants.trafficLoad,noOfSdPairsPerService,"web",1,g._vertex_list);
		
		
		//List of the service chains to be deployed
		List<Integer> scUsed = preProcFunctions.serviceChainsUsed(pair_list);		
		//print out the pair lists
		preProcFunctions.printSDpairs(pair_list, scUsed);	    
		
		//VNFs used across the service chains deployed
		List<FuncPt> func_list = preProcFunctions.listOfVNFsUsed(vnf_list, ChainSet, scUsed);		
		//print out the function list
		preProcFunctions.printListOfVNFsUsed(func_list);
		
		//traffic pairs for each service chain deployed	
		Map<Integer,ArrayList<TrafficNodes>> serviceChainTN = preProcFunctions.sdPairsforServiceChain(scUsed, pair_list);
	    //print out the traffic nodes available for each service chain
	    preProcFunctions.printTrafficNodesForServiceChains(scUsed, serviceChainTN);	
	    
	    
	    //split a single service chain into multiple service chains
	  	Map<Integer,ArrayList<Integer>> scCopies = new HashMap<Integer,ArrayList<Integer>>();		
	  	//new service to old service
	  	Map<Integer,Integer> scCopyToSC = new HashMap<Integer,Integer>();
	  	//create list of service chains
	  	ArrayList<Integer> scCopyUsed = new ArrayList<Integer>();
	    
	    //cluster each (s,d) pair as a separate service chain
	    for(int scID : scUsed){
		    int clustNum = 0;
		    ArrayList<Integer> scCopyList = new ArrayList<Integer>();
		    int sdPairCount = 0;
		    //get a sc copy Id
	    	int scCopyId = 1000*scID + clustNum;
	    	scCopyToSC.put(scCopyId,scID);
	    	scCopyUsed.add(scCopyId);
	    	scCopyList.add(scCopyId);
	    	//iterate through traffic pairs
		    for(TrafficNodes tn : serviceChainTN.get(scID)){
		    	//increment sd PairCount
		    	sdPairCount++;		    	
		    	//update chain index for (s,d)
		    	tn.updateChainIndex(scCopyId);		    	
		    	//increment the cluster count
		    	//if the sdPairCount is a multiple of 2
		    	if( (sdPairCount%4==0) && (sdPairCount<552) ){
		    		clustNum++;
		    		scCopyId = 1000*scID + clustNum;
		    		//add to map and list's
			    	scCopyToSC.put(scCopyId,scID);
			    	scCopyUsed.add(scCopyId);
			    	scCopyList.add(scCopyId);
		    	}
		    }
		    //add all scCopyId's in map
		    scCopies.put(scID, scCopyList);
	    }
	    
	    //max number of VNFs
	    Map<Integer, ArrayList<Integer>> funcInSC = preProcFunctions.vnfInSCs(scUsed, func_list, ChainSet);
	    Map<Integer,Integer> CountMaxVNF = preProcFunctions.countMaxVnfBasedOnSdPairs(ChainSet, funcInSC, serviceChainTN);
	    
	    //replica constraint per VNF
	    Map<Integer,Integer> replicaPerVNF = new HashMap<Integer,Integer>(CountMaxVNF);
	    
	    //enforce constraint on maximum number of VNFs
	    /*for(Map.Entry<Integer,Integer> entry : replicaPerVNF.entrySet()){
	    	replicaPerVNF.put(entry.getKey(),1);
	    }*/
		
	    //DC node placement		  
		ArrayList<Integer> dcNodes = new ArrayList<Integer>();  
		  
		//place the DC nodes
		placeNFVI.placeDC(g, dcNodes);
		//place the NFV nodes
		placeNFVI.placeNFVPoP(g, rankList, numOfNfvNodes);
		//create the list of NFV-capable nodes
		ArrayList<BaseVertex> nfv_nodes = new ArrayList<BaseVertex>();
		placeNFVI.makeNFVList(g, nfv_nodes);
		
		//create the list of NFVI nodes
		//add the set of DC nodes to the set of nfv nodes
		ArrayList<BaseVertex> nodesNFVI = new ArrayList<BaseVertex>();
		//add the set of NFV nodes
		nodesNFVI.addAll(nfv_nodes);
		//print the nodes with NFV capability
		placeNFVI.printNFVINodes(nodesNFVI);
		
		//list of vertices without the NFV nodes
		ArrayList<BaseVertex> vertex_list_without_nfvi_nodes = new ArrayList<BaseVertex>(g._vertex_list);
		//assuming that the NFV and DC node sets are exclusive				 
		vertex_list_without_nfvi_nodes.removeAll(nodesNFVI);	
	  	  
	  	//valid configurations for each service chain //each (s,d) selects a valid configuration
		Map<Integer,ArrayList<HuerVarZ>> configsPerSC = new HashMap<Integer,ArrayList<HuerVarZ>>();
		Map<TrafficNodes,SdDetails> configPerSD = new HashMap<TrafficNodes,SdDetails>();
		
		//cluster traffic pairs according to service chains
		serviceChainTN = preProcFunctions.sdPairsforServiceChain(scCopyUsed, pair_list);
		 //print out the traffic nodes available for each service chain
	    preProcFunctions.printTrafficNodesForServiceChains(scCopyUsed, serviceChainTN);
		//get configuration per SC
		configsPerSC = BaseHeuristic2.singleConfigBasedOnAdj(scUsed, ChainSet, nodesNFVI, scCopyUsed, scCopyToSC, sdpaths, serviceChainTN, scCopies, configPerSD);
		
		//print the configurations for each SC
	  	preProcFunctions.printConfigsPerSCforBH2(scUsed, configsPerSC);					  
	  	//print the configuration for each (s,d)
	  	preProcFunctions.printConfigForSD(configPerSD);  	  
	  
		//calculate the core and link constraints
		boolean coreCstr = false;
		boolean capCstr = false;
		Map<BaseVertex,Double> cpuCoreCount = new HashMap<BaseVertex,Double>();
		Map<NodePair,Double> linkCapacity = new HashMap<NodePair,Double>();
		CG.runCG(coreCstr,capCstr,cpuCoreCount,linkCapacity,g, ChainSet, pair_list, scUsed, vnf_list, func_list, serviceChainTN, nfv_nodes, 
				  nodesNFVI, vertex_list_without_nfvi_nodes, scCopies, scCopyToSC, configsPerSC, configPerSD, CountMaxVNF, replicaPerVNF, numOfNfvNodes);
	}
}
