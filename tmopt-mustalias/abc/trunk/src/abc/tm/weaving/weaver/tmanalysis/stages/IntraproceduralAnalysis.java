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
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.weaver.tmanalysis.mustalias.StatePropagatorFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.query.ReachableShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowMatchTag;
import abc.tm.weaving.weaver.tmanalysis.util.ShadowsPerTMSplitter;

/**
 * IntraproceduralAnalysis: This analysis is the core of the analysis
 * described in the POPL paper.  In particular, for each shadow group SG:
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
            //split reachable shadows by tracematch
			CallGraph cg = CallGraphAbstraction.v().abstractedCallGraph();

            Set reachableShadows = ReachableShadowFinder.v().reachableShadows(cg);
            Map tmNameToShadows = ShadowsPerTMSplitter.splitShadows(reachableShadows);
			Set<Shadow> thisTMsShadows = (Set<Shadow>) tmNameToShadows.get(tm.getName());
            for (Shadow s : thisTMsShadows) {
                SootMethod m = s.getContainer();

                System.err.println("analyzing method: "+m);
                System.err.println(" and shadow "+s.getUniqueShadowId());

                UnitGraph g = new ExceptionalUnitGraph(m.retrieveActiveBody());
                StatePropagatorFlowAnalysis a = 
                    new StatePropagatorFlowAnalysis(tm, s, g, cg);

                for (Unit u : (Collection<Unit>)g.getBody().getUnits()) {
                    if (!u.hasTag(SymbolShadowMatchTag.NAME))
                        continue;

                    System.out.println("new unit");
                    for (abc.tm.weaving.weaver.tmanalysis.util.SymbolFinder.SymbolShadowMatch match : ((SymbolShadowMatchTag)u.getTag(SymbolShadowMatchTag.NAME)).getMatchesForTracematch(tm)) 
                        System.out.println("after tag "+match.getUniqueShadowId()+" have "+a.getFlowAfter(u));
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
