/*
 * Created on 6-Mar-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.PointsToSet;
import soot.jimple.Stmt;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;

import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.TMStateMachine;

/**
 * InitialShadowFinder
 *
 * @author Eric Bodden
 */
public class InitialShadowFinder {
	
	
	public Set<Shadow> findInitialShadows() {

		TMGlobalAspectInfo globalAspectInfo = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
		
		//build a mapping from tracematch name to the initial labels of the tracematch state machine
		Map<TraceMatch,Set<String>> tmNameToInitialSymbolNames = new HashMap<TraceMatch, Set<String>>();
		for (TraceMatch tm : (Collection<TraceMatch>)globalAspectInfo.getTraceMatches()) {
			for (Iterator stateIter = tm.getStateMachine().getStateIterator(); stateIter.hasNext();) {
				SMNode state = (SMNode) stateIter.next();
				if(state.isInitialNode()) {
					for (Iterator outEdgeIter = state.getOutEdgeIterator(); outEdgeIter.hasNext();) {
						SMEdge outEdge = (SMEdge) outEdgeIter.next();
						String label = outEdge.getLabel();						
						Set<String> initLabels = tmNameToInitialSymbolNames.get(tm);
						if(initLabels==null) {
							initLabels = new HashSet<String>();
							tmNameToInitialSymbolNames.put(tm, initLabels);
						}
						initLabels.add(label);
					}
				}
			}
		}		
		
		Set<Shadow> result = new HashSet<Shadow>();
		Set<ShadowGroup> shadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
		//for all shadow groups
		for (ShadowGroup group : shadowGroups) {
			Set<Shadow> allShadows = group.getAllShadows();
			//for each shadow in the group, add the shadow if its symbol name is one of its tracematch's initial symbol names
			for (Shadow shadow : allShadows) {
				assert ShadowRegistry.v().enabledShadows().contains(shadow.getUniqueShadowId());
				String symbolName = Naming.getSymbolShortName(shadow.getUniqueShadowId());
				if(tmNameToInitialSymbolNames.get(group.getTraceMatch()).contains(symbolName)) {
					result.add(shadow);
				}
			}
		}
		
		return result;
	}
	
	
	public Set<Stmt> findInitialStatements() {
		
		Set<Stmt> result = new HashSet<Stmt>();
		
		Set<Shadow> findInitialShadows = findInitialShadows();
		for (Shadow shadow : findInitialShadows) {
			for (String var : (Collection<String>)shadow.getBoundVariables()) {
				PointsToSet pts = shadow.getPointsToSet(var);
				if(pts instanceof PointsToSetInternal) {
					//SPARK points-to set
					PointsToSetInternal sparkPts = (PointsToSetInternal) pts;
					//collect all nodes
			        sparkPts.forall( new P2SetVisitor() {
				        public final void visit( Node n ) {
				        	//TODO Ondrej, here we need to find the new-statement that corresponds to that node
				        	//and then we need to add this statement to "result"
				        }
				    });
				} else {
					throw new RuntimeException("currently can only handle SPARK points-to sets");
				}
			}
		}
		
		return result;
	}
	
	
	//singleton pattern
	
	protected static InitialShadowFinder instance;
	
	public static InitialShadowFinder v() {
		if(instance==null) {
			instance = new InitialShadowFinder();
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
