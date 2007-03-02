/*
 * Created on 13-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.stages;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.DominatorEdgeLabels;
import abc.tm.weaving.weaver.tmanalysis.Timer;
import abc.tm.weaving.weaver.tmanalysis.query.ConsistentShadowGroupFinder;
import abc.tm.weaving.weaver.tmanalysis.query.ReachableShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowsPerTMSplitter;
import abc.tm.weaving.weaver.tmanalysis.query.TraceMatchByName;

/**
 * This stage applies a flow-insensitive analysis to all shadows still remaining at this stage.
 *
 * @author Eric Bodden
 */
public class FlowInsensitiveAnalysis extends AbstractAnalysisStage {

	protected Timer domEdgesTimer = new Timer("dominating-edges");

	protected Timer groupShadowsTimer = new Timer("group-shadows");
		
	/**
	 * {@inheritDoc}
	 */
	protected void doAnalysis() {
		//fetch all shadows reachable over the abstracted call graph
        Set reachableShadows = ReachableShadowFinder.v().reachableShadows(CallGraphAbstraction.v().abstractedCallGraph());
        //remove and disable all shadows that have an empty variable mapping
        int emptyMappingCount=0;
        for (Iterator shadowIter = reachableShadows.iterator(); shadowIter.hasNext();) {
			Shadow shadow = (Shadow) shadowIter.next();
			if(shadow.hasEmptyMapping()) {
				shadowIter.remove();
				//such a shadow can safely be disabled due to the tracematch semantics which say
				//that the advice would not execute for an empty binding anyway
				disableShadow(shadow.getUniqueShadowId());
				
				emptyMappingCount++;
			}
		}
        logToStatistics("shadows-removed-due-to-empty-variable-mappings", emptyMappingCount);
        
        //split all remaining shadows by tracematch
        Map tmNameToShadows = ShadowsPerTMSplitter.v().splitShadows(reachableShadows);
        
        Set allConsistentShadowGroups = new LinkedHashSet();
        
        //for each "tracematch-name to shadows" mapping 
        for (Iterator entryIter = tmNameToShadows.entrySet().iterator(); entryIter.hasNext();) {
			Entry entry = (Entry) entryIter.next();
			String tmName = (String) entry.getKey();
			TraceMatch traceMatch = TraceMatchByName.v().get(tmName);
			
			//find the sets of labels that dominate final states along each path
			domEdgesTimer.startOrResume();
			DominatorEdgeLabels del = new DominatorEdgeLabels((TMStateMachine) traceMatch.getStateMachine());
			Set pathInfos = del.getPathInfos();
			domEdgesTimer.stop();
			
			Set thisTMsShadows = (Set) tmNameToShadows.get(tmName);
			assert thisTMsShadows!=null;
			
			groupShadowsTimer.startOrResume();
			Set shadowsGroups = ConsistentShadowGroupFinder.v().consistentShadowGroups(traceMatch,thisTMsShadows, pathInfos);
			groupShadowsTimer.stop();
			
			//store shadow groups for later reuse
			allConsistentShadowGroups.addAll(shadowsGroups);
			
			//disable all shadows which are not in a group
			Set shadowsToDisable = new HashSet();
			shadowsToDisable.addAll(thisTMsShadows);
			for (Iterator groupIter = shadowsGroups.iterator(); groupIter.hasNext();) {
				ShadowGroup group = (ShadowGroup) groupIter.next();
				shadowsToDisable.removeAll(group.getAllShadows());
			}
			
			disableShadows(shadowsToDisable);			
		}
        
        ShadowGroupRegistry.v().registerShadowGroups(allConsistentShadowGroups);
        
        logToStatistics("cum-dominating-edges-time", domEdgesTimer);
        logToStatistics("cum-group-shadows-time", groupShadowsTimer);
        
	}
	
	/**
	 * Disables all given shadows.
	 * @param shadows a set os {@link Shadow}s
	 */
	protected void disableShadows(Set shadows) {
		Set shadowIDsToDisable = new HashSet();
		for (Iterator shadowIter = shadows.iterator(); shadowIter.hasNext();) {
			Shadow shadow = (Shadow) shadowIter.next();
			shadowIDsToDisable.add(shadow.getUniqueShadowId());
		}
		disableAll(shadowIDsToDisable);
	}
	
//	/**
//	 * @return the allConsistentShadowGroups
//	 */
//	public Set getAllConsistentShadowGroups() {
//		if(allConsistentShadowGroups==null) {
//			throw new IllegalStateException("Stage not yet run!");
//		}
//		return allConsistentShadowGroups;
//	}
	
	//singleton pattern

	protected static FlowInsensitiveAnalysis instance;

	private FlowInsensitiveAnalysis() {}
	
	public static FlowInsensitiveAnalysis v() {
		if(instance==null) {
			instance = new FlowInsensitiveAnalysis();
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

