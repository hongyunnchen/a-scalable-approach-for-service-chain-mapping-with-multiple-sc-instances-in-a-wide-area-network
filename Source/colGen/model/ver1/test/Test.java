package colGen.model.ver1.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Given.InputConstants;
import ILP.FuncPt;
import ILP.NodePair;
import ILP.ServiceChain;
import ILP.TrafficNodes;
import colGen.model.data.TrafficGroups;
import colGen.model.heuristic.BaseHeuristic2;
import colGen.model.heuristic.ClusterHeuristic;
import colGen.model.heuristic.BaseHeuristic2.SdDetails;
import colGen.model.heuristic.ClusterHeuristic.ClusterTrafficPairs;
import colGen.model.heuristic.HuerVarZ;
import colGen.model.preprocess.PreProcVer1;
import colGen.model.preprocess.placeNFVI;
import colGen.model.preprocess.preProcFunctions;
import colGen.model.trafficGen.TrafficGenerator;
import colGen.model.ver1.CG;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class Test {
	
	public static void runTest() throws Exception{
		//generate the graph object
	    Graph g = PreProcVer1.makeGraphObject();
	    //set all nodes to type switches
	    preProcFunctions.makeAllVrtSwitches(g);
	    //populate ChainSet details
	    InputConstants.populateServices();
	    //print the graph object
	    preProcFunctions.printGraph(g);  
	    
	    //generate the routes for the traffic pairs
	    HashMap<NodePair,List<Path>> sdpaths = preProcFunctions.findRoutesForSDpairs(g);
	    
	    //get the set of service chains
	  	Map<Integer,ServiceChain> ChainSet = PreProcVer1.populateChainSetBasedOnScenario1();
		// print out the Set of Service Chains
		preProcFunctions.printServiceChains(ChainSet);
		
		//total list of VNF available
		List<FuncPt> vnf_list = PreProcVer1.totalListOfVNFs();	
	    
	    //store the generated traffic groups
	    TrafficGroups dataGen = new TrafficGroups();	    
	    //generate the number of clusters
	    for(int numOfClusters=1;numOfClusters<=ClusterHeuristic.formClustersWhenAllSdPairs(g).size();numOfClusters++){	    	  	
		    //form clusters
		    List<ClusterTrafficPairs> clusterGroups = ClusterHeuristic.formClusters(g, numOfClusters);
		    //store in the data object
		    dataGen.addGroup(numOfClusters,clusterGroups);
		    //print clusters
		    ClusterHeuristic.printClusters(clusterGroups);
	    }
	     /*try {
	    	 String dataFileName = InputConstants.NETWORK_FILE_NAME + ".ser";
	         FileOutputStream fileOut = new FileOutputStream(dataFileName);
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(dataGen);
	         out.close();
	         fileOut.close();
	         System.out.printf("Serialized data is saved in " + dataFileName);
	      }catch(IOException i) {
	         i.printStackTrace();
	      }*/
	    
	    //check for only selected values of K and configurations
	    ArrayList<Integer> kValues = new ArrayList<Integer>();
	    int maxKNodeCount = g._vertex_list.size();
	    kValues.add(maxKNodeCount);
	    for(int kCount = (int)Math.ceil(maxKNodeCount/3);kCount>0;kCount--){
	    	kValues.add(kCount);
	    }
	    
	    ArrayList<Integer> selectedClusterCounts = new ArrayList<Integer>();
	    selectedClusterCounts.add(1); selectedClusterCounts.add(2); selectedClusterCounts.add(3); selectedClusterCounts.add(4); selectedClusterCounts.add(5);
	    selectedClusterCounts.add(6); selectedClusterCounts.add(7); selectedClusterCounts.add(8); selectedClusterCounts.add(9); selectedClusterCounts.add(10);
	    for(int clusterValue=11;clusterValue<=ClusterHeuristic.formClustersWhenAllSdPairs(g).size();clusterValue++){
	    	if(clusterValue%5==0){
	    		selectedClusterCounts.add(clusterValue);
	    	}
	    }
	    

//	    for(int kNodeCount : kValues){
	    int kNodeCount = kValues.get(0);
    		String filename = "PlotData_" + kNodeCount + ".txt";
    	    BufferedWriter bw = new BufferedWriter(new FileWriter(filename));    
		    //40 for NSFNET //41
		    //86 for us24 network //87
		    //176 for germany //177
//		    for(int numOfClusters : selectedClusterCounts){	 
    	    	int numOfClusters = selectedClusterCounts.get(0);
		    	//NFV nodes 
				//All nodes are NFV capable
				int numOfNfvNodes = g._vertex_list.size();    	
		    	
			    //form clusters
			    List<ClusterTrafficPairs> clusterGroups = ClusterHeuristic.formClustersV2(g, numOfClusters);
			    //print clusters
			    ClusterHeuristic.printClusters(clusterGroups);			 	
						
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
				
				//print the total bandwidth consumed
				System.out.println("Total bandwidth consumed= " + preProcFunctions.totalBwUsedOnShortestPaths(sdpaths, pair_list));
				
				
				//List of the service chains to be deployed
//				List<Integer> scUsed = preProcFunctions.serviceChainsUsed(pair_list);		
//				//print out the pair lists
//				preProcFunctions.printSDpairs(pair_list, scUsed);	    
//				
//				//VNFs used across the service chains deployed
//				List<FuncPt> func_list = preProcFunctions.listOfVNFsUsed(vnf_list, ChainSet, scUsed);		
//				//print out the function list
//				preProcFunctions.printListOfVNFsUsed(func_list);
//				
//				//traffic pairs for each service chain deployed	
//				Map<Integer,ArrayList<TrafficNodes>> serviceChainTN = preProcFunctions.sdPairsforServiceChain(scUsed, pair_list);
//			    //print out the traffic nodes available for each service chain
//			    preProcFunctions.printTrafficNodesForServiceChains(scUsed, serviceChainTN);	
//			    
//			    //split a single service chain into multiple service chains
//			  	Map<Integer,ArrayList<Integer>> scCopies = new HashMap<Integer,ArrayList<Integer>>();		
//			  	//new service to old service
//			  	Map<Integer,Integer> scCopyToSC = new HashMap<Integer,Integer>();
//			  	//create list of service chains
//			  	ArrayList<Integer> scCopyUsed = new ArrayList<Integer>();
//			  	//iterate through the service chains
//			  	//fill the various IDs
//			  	for(int scID: scUsed){
//			  		ArrayList<Integer> scCopyList = new ArrayList<Integer>();
//				  	//add to map and list's
//				  	for(ClusterTrafficPairs ctpr : clusterGroups){
//				  		//get a sc copy Id
//				    	int scCopyId = 1000*scID + ctpr.sdGroupNum;
//				    	scCopyToSC.put(scCopyId,scID);
//				    	scCopyUsed.add(scCopyId);
//				    	scCopyList.add(scCopyId);
//				  	}
//				  	//add all scCopyId's in map
//				    scCopies.put(scID, scCopyList);
//			  	}
//			  	
//			  	//iterate through scIDs
//			  	for(int scID : scUsed){
//			  		//iterate through (s,d) pairs
//				    for(TrafficNodes tn : serviceChainTN.get(scID)){
//				    	NodePair tnPr = new NodePair(tn.v1,tn.v2);
//				    	//iterate through the traffic groups
//				    	for(ClusterTrafficPairs ctpr : clusterGroups){
//				    		//chech if the (s,d) cluster contains the node pair
//				    		if(ctpr.clusterPairs.contains(tnPr)){
//					    		//get a sc copy Id
//						    	int scCopyId = 1000*scID + ctpr.sdGroupNum;
//						    	//update chain index for (s,d)
//						    	tn.updateChainIndex(scCopyId); 
//				    		}
//				    	}
//				    }		   
//			    }
//				    
//			    //max number of VNFs
//			    Map<Integer, ArrayList<Integer>> funcInSC = preProcFunctions.vnfInSCs(scUsed, func_list, ChainSet);
//			    Map<Integer,Integer> CountMaxVNF = preProcFunctions.countMaxVnfBasedOnSdPairs(ChainSet, funcInSC, serviceChainTN);
//			    
//			    //replica constraint per VNF
//			    Map<Integer,Integer> replicaPerVNF = new HashMap<Integer,Integer>(CountMaxVNF);
//			    
//			    //enforce constraint on maximum number of VNFs
//			    for(Map.Entry<Integer,Integer> entry : replicaPerVNF.entrySet()){
//			    	replicaPerVNF.put(entry.getKey(),1);
//			    }
//				
//			    //DC node placement		  
//				ArrayList<Integer> dcNodes = new ArrayList<Integer>();  
//				  
//				//place the DC nodes
//				placeNFVI.placeDC(g, dcNodes);
//				//place the NFV nodes
//				placeNFVI.allNFV(g);
//				//create the list of NFV-capable nodes
//				ArrayList<BaseVertex> nfv_nodes = new ArrayList<BaseVertex>();
//				placeNFVI.makeNFVList(g, nfv_nodes);
//				
//				//create the list of NFVI nodes
//				//add the set of DC nodes to the set of nfv nodes
//				ArrayList<BaseVertex> nodesNFVI = new ArrayList<BaseVertex>();
//				//add the set of NFV nodes
//				nodesNFVI.addAll(nfv_nodes);
//				//print the nodes with NFV capability
//				placeNFVI.printNFVINodes(nodesNFVI);
//				
//				//list of vertices without the NFV nodes
//				ArrayList<BaseVertex> vertex_list_without_nfvi_nodes = new ArrayList<BaseVertex>(g._vertex_list);
//				//assuming that the NFV and DC node sets are exclusive				 
//				vertex_list_without_nfvi_nodes.removeAll(nodesNFVI);		    
//				
//			  	  
//			  	//valid configurations for each service chain //each (s,d) selects a valid configuration
//				Map<Integer,ArrayList<HuerVarZ>> configsPerSC = new HashMap<Integer,ArrayList<HuerVarZ>>();
//				Map<TrafficNodes,SdDetails> configPerSD = new HashMap<TrafficNodes,SdDetails>();
//				
//				//cluster traffic pairs according to service chains
//				serviceChainTN = preProcFunctions.sdPairsforServiceChain(scCopyUsed, pair_list);
//				 //print out the traffic nodes available for each service chain
//			    preProcFunctions.printTrafficNodesForServiceChains(scCopyUsed, serviceChainTN);
//				//get configuration per SC
//				configsPerSC = BaseHeuristic2.singleConfigBasedOnAdj(scUsed, ChainSet, nodesNFVI, scCopyUsed, scCopyToSC, sdpaths, serviceChainTN, scCopies, configPerSD);
//				
//				//print the configurations for each SC
//			  	preProcFunctions.printConfigsPerSCforBH2(scUsed, configsPerSC);					  
//			  	//print the configuration for each (s,d)
//			  	preProcFunctions.printConfigForSD(configPerSD);  	  
//			  
//				//calculate the core and link constraints
//				boolean coreCstr = false;
//				boolean capCstr = false;
//				Map<BaseVertex,Double> cpuCoreCount = new HashMap<BaseVertex,Double>();
//				Map<NodePair,Double> linkCapacity = new HashMap<NodePair,Double>();
//				CG.runCG(coreCstr,capCstr,cpuCoreCount,linkCapacity,g, ChainSet, pair_list, scUsed, vnf_list, func_list, serviceChainTN, nfv_nodes, 
//						  nodesNFVI, vertex_list_without_nfvi_nodes, scCopies, scCopyToSC, configsPerSC, configPerSD, CountMaxVNF, replicaPerVNF,kNodeCount);
//				
//				//write to a file
//				bw.write(numOfClusters + "\t" + CG.rmpIlpValue + "\t" + CG.vnfPlacementNodes.size() + "\t" + CG.vnfPlacementNodes.toString() + "\t" + CG.eoptimality + "\t" + 
//				CG.cgRunTime + "\t" + CG.ilpRunTime + "\t" + CG.totalTime + "\n");
//		    }
		   
		    //close the file writer
		    bw.close();
		    
//		}
		  
		  
	}

}
