/*
 * Created on 26-Feb-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ShadowGroupStatistics
 *
 * @author Eric Bodden
 */
public class ShadowGroupStatistics {
	
	protected class Cluster {
		
		private Set members;
		
		private Map clusterToDistance;
		
		private Cluster() {
			members = new HashSet();
			clusterToDistance = new HashMap();
		}

		protected Cluster(ShadowGroup group) {
			this();
			members.add(group);
		}
		
		public Cluster(Cluster c1, Cluster c2) {
			this();
			members.addAll(c1.members);
			members.addAll(c2.members);
		}

		public double distance(Cluster other) {
			if(other==this) {
				return 0;
			}
			
			if(clusterToDistance.containsKey(other)) {
				return ((Double) clusterToDistance.get(other)).doubleValue();
			}
			
			double distance = 0;
			for (Iterator memIter = members.iterator(); memIter.hasNext();) {
				ShadowGroup g1 = (ShadowGroup) memIter.next();
				Set shadows1 = g1.getAllShadows();
				
				for (Iterator memIter2 = other.members.iterator(); memIter2.hasNext();) {
					ShadowGroup g2 = (ShadowGroup) memIter2.next();
					Set shadows2 = g2.getAllShadows();
					
					Set union = new HashSet(shadows1);
					union.addAll(shadows2);
					
					distance += (union.size() / (double)(Math.max(shadows1.size(), shadows2.size()))) - 1; 
				}
			}
			
			clusterToDistance.put(other, new Double(distance));
			
			return distance;
		}		
		
	}
	
	protected Set clusters;
	
	public void computeAndDumpStatistics() {
		shadowGroupDump();
		//computeClusters();
    	probeStatistics();    	
	}
	
	/**
	 * 
	 */
	private void probeStatistics() {
		Set probes = Probe.generateAllSoundProbes();
		
		System.err.println("---------------------------------------------");
		System.err.println("Probe statistics");
		System.err.println();
		
    	for (Iterator iterator = probes.iterator(); iterator.hasNext();) {
			Probe probe = (Probe) iterator.next();			
			System.err.println("\n\nProbe number "+probe.getNumber()+":\n");

			List shadowIDs = new ArrayList(Shadow.uniqueShadowIDsOf(probe.getShadows()));
			Collections.sort(shadowIDs);
			for (Iterator shadowIDIter = shadowIDs.iterator(); shadowIDIter.hasNext();) {
				System.err.println("    "+shadowIDIter.next());
			}
    	}
		
		//get all shadows
		Set allShadows = new HashSet();		
		for (Iterator groupIter = probes.iterator(); groupIter.hasNext();) {
			Probe probe = (Probe) groupIter.next();
			allShadows.addAll(probe.getShadows());
		}
		
		//for each shadow
		for (Iterator shadowIter = allShadows.iterator(); shadowIter.hasNext();) {
			Shadow shadow = (Shadow) shadowIter.next();
			
			int groupsWithThisShadow = 0;
			double averageSizeOfGroupsWithThisShadows = 0;
			int maximalSizeOfGroupsWithThisShadows = 0;
			
			Set setsInducedByProbesWithThisShadows = new HashSet();
			
			for (Iterator groupIter = probes.iterator(); groupIter.hasNext();) {
				Probe probe = (Probe) groupIter.next();
			
				Set shadows = probe.getShadows();
				if(shadows.contains(shadow)) {
					groupsWithThisShadow++;
					averageSizeOfGroupsWithThisShadows += shadows.size();
					
					if(shadows.size()>maximalSizeOfGroupsWithThisShadows) {
						maximalSizeOfGroupsWithThisShadows = shadows.size();
					}
					
					setsInducedByProbesWithThisShadows.add(shadows);
				}
			}			
			averageSizeOfGroupsWithThisShadows /= (double)groupsWithThisShadow;
			
			System.err.println("shadow "+shadow.getUniqueShadowId());
			System.err.println("  contained in probes:     "+groupsWithThisShadow);
			System.err.println("  average probes size:      "+averageSizeOfGroupsWithThisShadows);
			System.err.println("  maximal probes size:      "+maximalSizeOfGroupsWithThisShadows);
			System.err.println();
			System.err.println("coupling:");
			for (Iterator shadowIter2 = allShadows.iterator(); shadowIter2.hasNext();) {
				Shadow shadow2 = (Shadow) shadowIter2.next();

				int commonSets = 0;
				for (Iterator thisShadowsGroupsShadows = setsInducedByProbesWithThisShadows.iterator(); thisShadowsGroupsShadows.hasNext();) {
					Set shadows = (Set) thisShadowsGroupsShadows.next();
					assert shadows.contains(shadow);
					
					if(shadows.contains(shadow2)) {
						commonSets++;
					}
				}
				double coupling = commonSets/ (double)setsInducedByProbesWithThisShadows.size();

				System.err.println("coupling["+shadow.getUniqueShadowId()+","+shadow2.getUniqueShadowId()+"] = "+coupling+" (common probes: "+commonSets+")");
			}	
				
			System.err.println();
			System.err.println();
		}

		System.err.println("---------------------------------------------");
	}
//
//	/**
//	 * 
//	 */
//	private void computeClusters() {
//		//initialize clusters
//		clusters = new LinkedHashSet();
//    	Set allConsistentShadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
//    	for (Iterator iterator = allConsistentShadowGroups.iterator(); iterator.hasNext();) {
//			ShadowGroup group = (ShadowGroup) iterator.next();			
//			clusters.add(new Cluster(group));
//    	}
//    	
//    	//cluster together
//    	boolean goodEnough = false;
//    	do {
//    		
//    		//find the two clusters with the smallest distance
//    		double smallestDistance = Double.POSITIVE_INFINITY;    		
//    		Cluster smallestDistanceCluster1 = null;
//    		Cluster smallestDistanceCluster2 = null;
//    		
//    		for (Iterator clusterIter = clusters.iterator(); clusterIter.hasNext();) {
//				Cluster c1 = (Cluster) clusterIter.next();
//
//				for (Iterator clusterIter2 = clusters.iterator(); clusterIter2.hasNext();) {
//					Cluster c2 = (Cluster) clusterIter2.next();
//					
//					if(c2==c1) break;	//shortcut; only need to traverse halfway, because the distance is symmetric
//					if(smallestDistance==0) break;
//					
//					double distance = c1.distance(c2);
//					if(distance<smallestDistance) {
//						smallestDistance = distance;
//						smallestDistanceCluster1 = c1;
//						smallestDistanceCluster2 = c2;
//					}					
//				}				
//			}
//    		
//    		//merge clusters
//    		Cluster join = new Cluster(smallestDistanceCluster1, smallestDistanceCluster2);
//    		clusters.remove(smallestDistanceCluster1);
//    		clusters.remove(smallestDistanceCluster2);
//    		clusters.add(join);
//    		
//    		System.out.println(smallestDistance);
//    		
//    		goodEnough = clusters.size()==1;
//    		
//    	} while(!goodEnough);
//    	
//    	clusters = null;
//	}

	protected void shadowGroupDump() {
    	Set allConsistentShadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
    	
    	System.err.println("=====================================================================");
    	System.err.println("================               SHADOW GROUPS               ==========");
    	System.err.println("=====================================================================");
		System.err.println("There are "+allConsistentShadowGroups.size()+" complete and consistent shadow groups");
    	System.err.println("containing "+ShadowRegistry.v().enabledShadows().size() +" shadows.\n");
		System.err.println("Those groups form "+Probe.generateAllSoundProbes().size()+" different probes.");
    	
    	for (Iterator iterator = allConsistentShadowGroups.iterator(); iterator.hasNext();) {
			ShadowGroup group = (ShadowGroup) iterator.next();			
			System.err.println("Group number "+group.getNumber()+":");
			System.err.println("Label shadows:");
			List labelShadowIDs = new ArrayList(Shadow.uniqueShadowIDsOf(group.getLabelShadows()));
			Collections.sort(labelShadowIDs);
			for (Iterator shadowIDIter = labelShadowIDs.iterator(); shadowIDIter.hasNext();) {
				System.err.println("    "+shadowIDIter.next());
			}
			System.err.println("Skip shadows:");
			List skipShadowIDs = new ArrayList(Shadow.uniqueShadowIDsOf(group.getSkipShadows()));
			Collections.sort(skipShadowIDs);
			for (Iterator shadowIDIter = skipShadowIDs.iterator(); shadowIDIter.hasNext();) {
				System.err.println("    "+shadowIDIter.next());
			}
			System.err.println();
		}
	}
	
	//singleton pattern
	
	public static void initialize() {
		v();
	}

	protected static ShadowGroupStatistics instance;
	
	public static ShadowGroupStatistics v() {
		if(instance==null) {
			instance = new ShadowGroupStatistics();
		}
		return instance;		
	}
	
	/**
	 * Frees the singleton object. 
	 */
	public static void reset() {
		instance = null;
	}

}
