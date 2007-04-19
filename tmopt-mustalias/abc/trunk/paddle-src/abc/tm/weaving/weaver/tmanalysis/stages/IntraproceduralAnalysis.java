/*
 * Created on 12-Feb-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.stages;

import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.toolkits.graph.UnitGraph;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.ExceptionalUnitGraph;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.weaver.tmanalysis.mustalias.StatePropagatorFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.query.ReachableShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowsPerTMSplitter;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowMatchTag;

/**
 * IntraproceduralAnalysis: This analysis is the core of the analysis
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
public class IntraproceduralAnalysis extends AbstractAnalysisStage {
	protected static TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();

	/**
	 * {@inheritDoc}
	 */
	protected void doAnalysis() {
        for (TraceMatch tm : (Collection<TraceMatch>)gai.getTraceMatches()) {
			// Now propagate p through the procedure.

            //split reachable shadows by tracematch
            Set reachableShadows = ReachableShadowFinder.v().reachableShadows
                (CallGraphAbstraction.v().abstractedCallGraph());
            Map tmNameToShadows = ShadowsPerTMSplitter.v().splitShadows
                (reachableShadows);
			Set<Shadow> thisTMsShadows = (Set<Shadow>) tmNameToShadows.get(tm.getName());
            Collection<SootMethod> thisTMsContainers = new HashSet();
            for (Shadow s : thisTMsShadows) 
                thisTMsContainers.add(s.getContainer());

            for (SootMethod m : thisTMsContainers) {
                System.err.println("analyzing method: "+m);
                UnitGraph g = new ExceptionalUnitGraph(m.retrieveActiveBody());
                StatePropagatorFlowAnalysis a = 
                    new StatePropagatorFlowAnalysis(tm,
                                                    g,
                                                    CallGraphAbstraction.v().abstractedCallGraph());

                for (Unit u : (Collection<Unit>)g.getBody().getUnits()) {
                    if (!u.hasTag(SymbolShadowMatchTag.NAME))
                        continue;

                    System.out.println("after stmt "+u+" have "+a.getFlowAfter(u));
                }

                for (Unit u : (Collection<Unit>)g.getTails()) {
                    System.out.println(" for tail "+u+" got "+a.getFlowAfter(u));
                }
                // now read off the results: if we know single
                // non-final state for each tail, then set the state
                // to the known state.
                // Also if we know that a given shadow can only hit a skip shadow,
                // we can eliminate it.
            }
		}
	}
	
	//singleton pattern

	protected static IntraproceduralAnalysis instance;

	private IntraproceduralAnalysis() {}
	
	public static IntraproceduralAnalysis v() {
		if(instance==null) {
			instance = new IntraproceduralAnalysis();
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
