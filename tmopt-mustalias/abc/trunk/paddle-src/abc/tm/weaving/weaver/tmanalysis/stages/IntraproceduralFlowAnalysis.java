/*
 * Created on 12-Feb-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.stages;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;

import soot.SootMethod;
import soot.Local;
import soot.toolkits.graph.BriefUnitGraph;

import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.ShadowSMEdgeFactory.SMShadowEdge;
import abc.tm.weaving.weaver.tmanalysis.mustalias.StatePropagatorFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.query.Naming;
import abc.tm.weaving.weaver.tmanalysis.query.TraceMatchByName;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.InitialShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.ReachableShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowsPerTMSplitter;

/**
 * IntraproceduralFlowAnalysis: This analysis is the core of the analysis
 * described in the OOPSLA paper.  In particular, for each shadow group SG:
 *
 *  a) For each initial shadow, give must-names to the binding objects.
 *  b) Propagate state information when we hit additional shadows.
 *  c) If we can show that a method is safely invariant with respect to SG,
 *       remove all of that method's shadows.
 *  c') Dead objects.
 *
 * @author Eric Bodden
 */
// TODO rename to IntraproceduralAnalysis
public class IntraproceduralFlowAnalysis extends AbstractAnalysisStage {
	protected static TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();

	/**
	 * {@inheritDoc}
	 */
	protected void doAnalysis() {
        Set reachableShadows = ReachableShadowFinder.v().reachableShadows(CallGraphAbstraction.v().abstractedCallGraph());
        // split shadows by tracematch
        Map<String, Set<Shadow>> tmNameToShadows = ShadowsPerTMSplitter.v().splitShadows(reachableShadows);

//		Set shadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
		Map<ShadowGroup, Shadow> initialShadowMap
			= InitialShadowFinder.v().findInitialShadows();
		Set<Shadow> initialShadows = new HashSet(initialShadowMap.values());
//		for (Entry<ShadowGroup, Shadow> e : initialShadowMap.entrySet()) 
//			initialShadows.add(e.getValue());

		//for each initial shadow
        for (Shadow initialShadow: initialShadows) {
        	String tmName = Naming.getTracematchName(initialShadow.getUniqueShadowId());
			TraceMatch traceMatch = TraceMatchByName.v().get(tmName);

			// Each ShadowSMEdge knows what Shadow it's carrying.
			// We want to map a Shadow to its ShadowSMEdge.
			// Shadows change the current SMNode in the state...
			Map<String, SMEdge> shadowIdsToSMEdges = new HashMap<String, SMEdge>();

//FIXME there can be multiple edges with the same label			
			Iterator<SMEdge> it = traceMatch.getStateMachine().getEdgeIterator();
			while (it.hasNext()) {
				SMEdge e = it.next();
				shadowIdsToSMEdges.put(((SMShadowEdge)e).getQualifiedShadowId(), e);
			}

			// locate initial shadow for that tracematch.
			Set<Shadow> thisTMsShadows = tmNameToShadows.get(tmName);
			for (Shadow s : thisTMsShadows) {
				if (initialShadows.contains(s)) {
					SootMethod m = s.getContainer();
//FIXME here we want the unique (?) state after reading the initial symbol, I guess
					SMEdge e = shadowIdsToSMEdges.get(s.getUniqueShadowId());
					SMNode initialState = e.getTarget();

					StatePropagatorFlowAnalysis.SmMaPair p = StatePropagatorFlowAnalysis.newSmMaPair(initialState, s.getBoundLocals());

					// Now propagate p through the procedure.
					//TODO is it safe enough to use a BriefUnitGraph or do we want an ExceptionalUnitGraph?
					StatePropagatorFlowAnalysis a = new StatePropagatorFlowAnalysis(m, new BriefUnitGraph(m.retrieveActiveBody()), 
																					s, p, shadowIdsToSMEdges);

					// a will tell you if the method is safely-invariant or what.
					// XXX use the results
				}
			}
		}
	}
	
	//singleton pattern

	protected static IntraproceduralFlowAnalysis instance;

	private IntraproceduralFlowAnalysis() {}
	
	public static IntraproceduralFlowAnalysis v() {
		if(instance==null) {
			instance = new IntraproceduralFlowAnalysis();
		}
		return instance;		
	}
	
	/**
	 * Frees the singleton object. 
	 */
	public static void reset() {
		instance = null;
		InitialShadowFinder.reset();
	}

}
