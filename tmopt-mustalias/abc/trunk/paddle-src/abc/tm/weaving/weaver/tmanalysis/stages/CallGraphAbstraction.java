/*
 * Created on 12-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.stages;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.berkeley.pa.csdemand.DemandCSPointsTo;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.PointsToAnalysis;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import abc.main.Debug;
import abc.main.Main;
import abc.tm.weaving.weaver.tmanalysis.Timer;
import abc.tm.weaving.weaver.tmanalysis.callgraph.NodePredicate;
import abc.tm.weaving.weaver.tmanalysis.callgraph.SomewhatAbstractedCallGraph;
import abc.tm.weaving.weaver.tmanalysis.query.Naming;
import abc.tm.weaving.weaver.tmanalysis.query.ReachableShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.TaggedHosts;
import abc.weaving.weaver.Weaver;

/**
 * This stage does not actually perform any real analysis. It merely applies the <i>cg</i> phase, constructing a call graph
 * and points-to sets and afterwards abstracts the call graph, pruning subgraphs which contain no methods of interest. 
 * However, afterwards it disables all unrechable shadows, which might lead to the fact that there are actually no enabled
 * shadows remaining (in the case where all active shadows were unreachable).
 *
 * @author Eric Bodden
 */
public class CallGraphAbstraction extends AbstractAnalysisStage {

    /** 
     * predicate used for call graph  abstraction
     * @see TaggedMethods
     */
    protected final NodePredicate ONLY_METHODS_WITH_MATCHED_UNITS = new TaggedMethods();
    
	/** the abstracted call graph */
	protected CallGraph abstractedCallGraph;
	
	/** set of reachable shadows */
	protected Set reachableShadowIDs;
	
	/** timer for cg-phase */
	protected Timer cgTimer = new Timer("cg-phase"); 

	/** timer for call graph abstraction */
	protected Timer cgAbstrTimer = new Timer("cg-abstraction"); 

	/**
	 * {@inheritDoc}
	 */
	public void doAnalysis() {
        //set a main class from the options if none is set yet
        Scene.v().setMainClassFromOptions();
        
        cgTimer.startOrResume();
        
    	//build call graph
    	PackManager.v().getPack("cg").apply();

        cgTimer.stop();
		logToStatistics("cg-phase-time", cgTimer);
		
		System.err.println("cg done");
		
		if(Debug.v().onDemand) {
			PointsToAnalysis onDemandAnalysis = DemandCSPointsTo.makeDefault();
			Scene.v().setPointsToAnalysis(onDemandAnalysis);
		}

		System.err.println("on demand analysis created");
		
		CallGraph callGraph = Scene.v().getCallGraph();

		logToStatistics("cg-size-original", callGraph.size());

		cgAbstrTimer.startOrResume();
        
        //abstract the call-graph, i.e. only retain nodes which contain tagged units
        abstractedCallGraph = new SomewhatAbstractedCallGraph(callGraph, ONLY_METHODS_WITH_MATCHED_UNITS);

        cgAbstrTimer.stop();
		logToStatistics("cg-abstraction-time", cgAbstrTimer);

		logToStatistics("cg-size-abstracted", abstractedCallGraph.size());

    	reachableShadowIDs = ReachableShadowFinder.v().reachableShadowIDs(abstractedCallGraph);
		logToStatistics("reachable-shadow-count", reachableShadowIDs.size());
		
    	//determine the unreachable ones by subtracting the reachable ones from all enabled ones
    	Set unreachableShadowsIDs = ShadowRegistry.v().enabledShadows();
    	unreachableShadowsIDs.removeAll(reachableShadowIDs);

    	//disable all unreachable shadows
    	disableAll(unreachableShadowsIDs);
	}
	
	/**
	 * Returns the abstracted call graph. This graph holds no edges for methods which are not of interest to the analysis. 
	 * @return the abstracted call graph
	 */
	public CallGraph abstractedCallGraph() {
		return abstractedCallGraph;
	}
	
	/**
	 * @return the set of shadow IDs reachable via the abstracted call graph
	 * @see Naming#uniqueShadowID(String, int)
	 * @see Naming#uniqueShadowID(String, String, int)
	 */
	public Set getReachableShadowIDs() {
		return new HashSet(reachableShadowIDs);
	}
	
    /**
     * This is a predicate over call graph nodes which accepts
     * only methods which contain at least one unit which is tagged
     * with matching tracematch symbols.
     */
	private class TaggedMethods implements NodePredicate {
		
		protected Weaver weaver; 
		
		/** 
	     * @return <code>true</code> if the method is tagged with matching symbols or it 
	     * contains a unit that is tagged with matching symbols.
	     */
	    public boolean want(MethodOrMethodContext curr) {
	    	if(weaver==null) weaver = Main.v().getAbcExtension().getWeaver();    	
	    	
	    	SootMethod method = curr.method();

	    	if(TaggedHosts.v().hasTag(method)) {
	    		return true;
	    	}
	    	
	        if(method.hasActiveBody()) {
	            Body body = method.getActiveBody();
	            
	            for (Iterator iter = body.getUnits().iterator(); iter.hasNext();) {
	                Unit u = (Unit) iter.next();
	                if(TaggedHosts.v().hasTag(u)) {
	                    return true;
	                }
	            }
	        }
	        
	        return false;
	    }
	}
	
	//singleton pattern
	
	protected static CallGraphAbstraction instance;

	private CallGraphAbstraction() {}
	
	public static CallGraphAbstraction v() {
		if(instance==null) {
			instance = new CallGraphAbstraction();
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
