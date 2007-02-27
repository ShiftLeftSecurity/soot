/*
 * Created on 24-Sep-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

import soot.util.IdentityHashSet;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.VariableSMEdgeFactory.SMVariableEdge;

/**
 * ComponentStateMachineConstructor
 *
 * @author Eric Bodden
 */
public class ComponentStateMachineConstructor {
	
	protected final IdentityHashMap globalStateToComponentState;
	
	protected TMStateMachine componentSM;
	
	/**
	 * @param component
	 */
	public ComponentStateMachineConstructor(final Set component, TMStateMachine sm) {

		globalStateToComponentState = new IdentityHashMap();

		componentSM = new TMStateMachine();
		
		final Set found = new HashSet();
		
		SMNode.SMEdgePredicate componentPredicate = new SMNode.SMEdgePredicate() {
			
			public boolean accept(SMEdge e) {
				if(e instanceof SMVariableEdge && e.getLabel()!=UGStateMachine.EPSILON) {
					SMVariableEdge variableEdge = (SMVariableEdge) e;
					if(component.contains(variableEdge.getShadow())) {
						found.add(variableEdge.getShadow());
					} else {
						return false;
					}
				}
				return true;
			}
			
		};
		
		Set added = new IdentityHashSet();
		
		for (Iterator stateIter = sm.getStateIterator(); stateIter.hasNext();) {
			SMNode originalNode = (SMNode) stateIter.next();			
			
			Set closure = new HashSet();
			originalNode.fillInClosure(closure, true, componentPredicate);
			
			closure.remove(originalNode);
			
            Iterator closureIt = closure.iterator();
            boolean isInitial = originalNode.isInitialNode();

            // .. for every node in the closure
            while(closureIt.hasNext()) {

        		SMNode next = (SMNode)closureIt.next();
        		// .. for every edge coming into the original node
        		Iterator edgeIt = originalNode.getInEdgeIterator();
        		while(edgeIt.hasNext()) {
        			SMEdge edge = (SMEdge)edgeIt.next();
        			// .. copy that edge onto the node from the closure if it is a transition we want to keep
        			if(componentPredicate.accept(edge) && !edge.getSource().hasEqualEdgeTo((SMNode) stateFor(next), edge)) {
        				componentSM.newTransitionFromClone(stateFor(edge.getSource()), stateFor(next), edge.getLabel(), edge);
        				added.add(edge);
        			}
        		}
        		// Any node in the closure of an initial node is initial.
        		if(isInitial) stateFor(next).setInitial(true);            	
            }			
		}

		if(found.size()<component.size()) {
			componentSM = null;			
		} else {
			//copy all edges that were not added so far 
			for (Iterator edgeIter = sm.getEdgeIterator(); edgeIter.hasNext();) {
				SMEdge edge = (SMEdge) edgeIter.next();
				if(!added.contains(edge) && componentPredicate.accept(edge)) {
					componentSM.newTransitionFromClone(stateFor(edge.getSource()), stateFor(edge.getTarget()), edge.getLabel(), edge);
				}			
			}
			
			componentSM.cleanup();
		}
		
	}
	
	
	protected State stateFor(State orig) {
		State s = (State) globalStateToComponentState.get(orig);
		
		if(s==null) {
			s = componentSM.newState();
			s.setInitial(orig.isInitialNode());
			s.setFinal(orig.isFinalNode());
			globalStateToComponentState.put(orig, s);
		}
		
		return s;
	}


	/**
	 * @return the componentSM
	 */
	public TMStateMachine getComponentStateMachine() {
		return componentSM;
	}	

}
