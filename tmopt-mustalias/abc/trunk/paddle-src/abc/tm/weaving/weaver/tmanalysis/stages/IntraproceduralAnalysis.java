/*
 * Created on 12-Feb-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.stages;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.toolkits.graph.BriefUnitGraph;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.weaver.tmanalysis.mustalias.StatePropagatorFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.query.InitialShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;

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
		Set<Shadow> initialShadowMap
			= InitialShadowFinder.v().findInitialShadows();
		
		//for each initial shadow
        for (Shadow initialShadow : initialShadowMap) {
            System.err.println("analyzing this shadow: ");
			System.err.println(initialShadow);
			// Now propagate p through the procedure.
			//TODO is it safe enough to use a BriefUnitGraph or do we want an ExceptionalUnitGraph?
			StatePropagatorFlowAnalysis a = new StatePropagatorFlowAnalysis(
					new BriefUnitGraph(initialShadow.getContainer().retrieveActiveBody()),
					initialShadow,
					CallGraphAbstraction.v().abstractedCallGraph());

			System.err.println(initialShadow.getContainer());
			System.err.println(a.isSafelyInvariant());
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
		InitialShadowFinder.reset();
	}

}
