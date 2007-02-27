/*
 * Created on 13-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import soot.EntryPoints;
import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.util.IdentityHashSet;
import abc.main.Debug;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.SMThreadCallEdge;
import abc.tm.weaving.weaver.tmanalysis.UGStateMachine;
import abc.tm.weaving.weaver.tmanalysis.UGStateMachineTag;
import abc.tm.weaving.weaver.tmanalysis.ds.ThreadContext;

/**
 * Folds all existing {@link UGStateMachine}s together, returning one single {@link TMStateMachine}
 * that represents the whole program. 
 *
 * @author Eric Bodden
 */
public class StateMachineFolder {
	
	/** edges not affecting the main thread */
	protected Set edgesWithSharedVariables;

	/**
	 * Computes and returns the complete state machine modeling all methods reachable in cg.
	 * @param cg a (possibly abstracted) call graph
	 * @return the state machine modeling the whole program
	 */
	public TMStateMachine getCompleteProgramStateMachine(CallGraph cg) {
		TMStateMachine completeStateMachine = new TMStateMachine();

		//create an initial state; this reflects the initial
		//program state
		State initialState = completeStateMachine.newState();
		initialState.setInitial(true);
		
		//and a final state; this reflects the final program state
		State finalState = completeStateMachine.newState();
		finalState.setFinal(true);		
		
		Collection foldedStateMachines = new ArrayList();
		
		//for all application entry points, fold state machines building up thread contexts
		for (Iterator entryPointIter = EntryPoints.v().application().iterator(); entryPointIter.hasNext();) {
			MethodOrMethodContext momc = (MethodOrMethodContext) entryPointIter.next();
			SootMethod entryPoint = momc.method();
			
			//get the state machine for this entry point
			UGStateMachine stateMachine = getUGStateMachine(entryPoint);
			//fold it
			UGStateMachine foldedStateMachine = stateMachine.fold();			

			foldedStateMachines.add(foldedStateMachine);
		}
		
		edgesWithSharedVariables = new IdentityHashSet();
		//for all folded state machines, clean them up and build thread summaries
		for (Iterator tmIter = foldedStateMachines.iterator(); tmIter.hasNext();) {
			UGStateMachine foldedStateMachine = (UGStateMachine) tmIter.next();
			
			//clean up and minimize
			foldedStateMachine.cleanup();
			foldedStateMachine.minimizeIfSmaller();
			
			
//			edgesWithSharedVariables.addAll(foldedStateMachine.removeEdgesWithSharedVariables());
//			//build thread summaries
//			//this has to be done here (and not at a later stage) because otherwise we would lose information about the entry point
//			nonMainThreadEdges.addAll(foldedStateMachine.buildThreadSummaries(ThreadContext.contextOf(ThreadContext.MAIN)));
//			SMThreadCallEdge.reset(); //TODO should be managed over a context or so

			//insert the state machine into the one for the complete program
			completeStateMachine.insertStateMachine(initialState, foldedStateMachine, finalState);
			assert completeStateMachine.isConsistent();
		}
		
		final int sizeBeforeCleanup = completeStateMachine.size();
		final long timeBeforeCleanup = System.currentTimeMillis();
		completeStateMachine.cleanup();
		final long duration = System.currentTimeMillis() - timeBeforeCleanup;
    	completeStateMachine.minimizeIfSmaller();
        
		if(Debug.v().debugTmAnalysis) {
			System.err.println("============================================================");
			System.err.println("Generated full-program state machine.");
			System.err.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
			System.err.println("Size of state machine: ");
			System.err.println("Before cleanup: "+sizeBeforeCleanup);
			System.err.println("After cleanup:  "+completeStateMachine.size());
			System.err.println("Cleanup took "+duration+"ms");
			System.err.println("============================================================");
		}		
		
		return completeStateMachine;
	}

	/**
	 * @param method
	 * @return
	 */
	protected UGStateMachine getUGStateMachine(SootMethod method) {
		//at this point every method should have an associated state machine
		assert method.method().hasTag(UGStateMachineTag.NAME);
		//get the state machine
		UGStateMachineTag smTag = (UGStateMachineTag) method.method().getTag(UGStateMachineTag.NAME);
		UGStateMachine stateMachine = smTag.getStateMachine();
		assert stateMachine != null;
		return stateMachine;
	}
	
	/**
	 * Returns all edges not affecting the main thread.
	 */
	public Set getEdgesWithSharedVariables() {
		return edgesWithSharedVariables;
	}

	//singleton pattern
	
	protected static StateMachineFolder instance;

	private StateMachineFolder() {}
	
	public static StateMachineFolder v() {
		if(instance==null) {
			instance = new StateMachineFolder();
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
