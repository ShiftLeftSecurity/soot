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

import polyglot.util.ErrorInfo;
import soot.EntryPoints;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import abc.main.Debug;
import abc.main.Main;
import abc.tm.weaving.weaver.tmanalysis.UGStateMachine;
import abc.tm.weaving.weaver.tmanalysis.UGStateMachineTag;
import abc.tm.weaving.weaver.tmanalysis.query.Naming;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;

/**
 * This analysis is not an actual analysis but rather a preparation phase.
 * It builds for each method reachable in the asbtracted call graph a {@link UGStateMachine}. 
 *
 * @author Eric Bodden
 */
public class PerMethodStateMachines extends AbstractAnalysisStage {
	
	/** the abstracted call graph */
	protected CallGraph abstractedCallGraph;
	
	/** shadow IDs that were found reachable */
	protected Set reachableShadowIDs;
	
	/** the number of state machines built */
	protected int smCount;

	/**
	 * {@inheritDoc}
	 */
	public void doAnalysis() {
        abstractedCallGraph = CallGraphAbstraction.v().abstractedCallGraph();
        reachableShadowIDs = new HashSet();
        smCount = 0;

        //find all rechable methods
    	ReachableMethods rm = new ReachableMethods(abstractedCallGraph,EntryPoints.v().application());
    	rm.update();
    	
    	//build a state machine for each
    	for (Iterator methIter = rm.listener(); methIter.hasNext();) {
			SootMethod method = (SootMethod) methIter.next();
			buildPerMethodStateMachine(method);
		}
    	
    	logToStatistics("sm-count", smCount);
    	
    	//all shadows enabled at this stage should have been found reachable
    	assert reachableShadowIDs.equals(ShadowRegistry.v().enabledShadows());
	}

	/**
     * For a given method,
     * builds a state machine reflecting the transition structure
     * of the graph. This state machine is then attached
     * to the method with a tag.
     * @param method the method to process
     */
    protected void buildPerMethodStateMachine(SootMethod method) {
    	//if no state machine is associated yet
        if(!method.hasTag(UGStateMachineTag.NAME)) {
        	UGStateMachine sm;

        	if(method.hasActiveBody()) {
            	//if the method has a body, build a state machine that models
            	//the abstract transition structure of that method

        		//build an initial unit graph
	            UnitGraph eg = new ExceptionalUnitGraph(method.getActiveBody());
	            
	            //build a state machine reflecting its transition structure
	            sm = new UGStateMachine(eg,abstractedCallGraph,this);
        	} else {
        		//else, we probably have a native method
        		
        		if(!method.isNative()) {
        			//a non-native method without body? trouble!
        			Main.v().error_queue.enqueue(ErrorInfo.SEMANTIC_ERROR,"Have a non-native method '"+
        					method.getName()+"' with no body!");
        		}

        		if(Debug.v().debugTmAnalysis) {
        			Main.v().error_queue.enqueue(ErrorInfo.WARNING,"Encountered method '"+
        					method +"' with no body. Modelling with empty FSM.");
        		}
        		
    			sm = new UGStateMachine(method,abstractedCallGraph,this);
        	}
            
            //UnitGraph ag = new AbstractedUnitGraph(eg, pred);
            method.addTag(new UGStateMachineTag(sm));
            
            smCount++;
        }
    }

	/**
	 * Callback method which will be called during construction of a {@link UGStateMachine}.
	 * It registers the shadow that is passed in as reachable.
	 * @param uniqueShadowId the unique shadow ID of a shadow found
	 * @see Naming#uniqueShadowID(String, int)
	 * @see Naming#uniqueShadowID(String, String, int)
	 */
	public void registerReachableShadow(String uniqueShadowId) {
		reachableShadowIDs.add(uniqueShadowId);
	}

	//singleton pattern
	
	protected static PerMethodStateMachines instance;

	private PerMethodStateMachines() {}
	
	public static PerMethodStateMachines v() {
		if(instance==null) {
			instance = new PerMethodStateMachines();
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
