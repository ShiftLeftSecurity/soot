/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Eric Bodden
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.tm.weaving.weaver.tmanalysis.ds;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.jimple.Stmt;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.StateMachine;
import abc.tm.weaving.weaver.tmanalysis.TMFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.VariableSMEdgeFactory.SMVariableEdge;
import abc.tm.weaving.weaver.tmanalysis.query.Naming;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;

/**
 * An abstract state machine configuration. It holds a mapping from states to
 * Constraints.
 *
 * @author Eric Bodden
 */
public class Configuration implements Cloneable {

	/**
	 * a most-recently used cache to cache equal configurations; the idea is that equality checks
	 * are faster if performed on "interned" instances
	 * @see #intern()
	 * @see String#intern()
	 */
	protected static Map configToUniqueConfig = new HashMap();//new MemoryStableMRUCache("config-intern",10*1024*1024,false);
	
	public static void reset() {
		configToUniqueConfig.clear();
	}


	/** The mapping from states to constraints. */
	protected HashMap stateToConstraint;
	
	/** Edges that may be triggered by a thread any time. */
	protected HashSet edgesMaybeTriggeredByThread;
	
	/** The associated analysis, as a callback to register active shadows. */
	protected final TMFlowAnalysis analysis;

	/** Statistical iteration counter. */
	public static int iterationCount;
	
	/**
	 * Creates a new configuration holding a mapping for the given states and registering active
	 * shadows with the given analysis.
	 * @param stateIter an iterator over {@link SMNode}s of the associated tracematch state machine
	 * @param callback the analysis to call back in the case an active shadow is found
	 */
	public Configuration(Iterator stateIter, TMFlowAnalysis callback) {
		analysis = callback;
		stateToConstraint = new HashMap();
		edgesMaybeTriggeredByThread = new HashSet();
		iterationCount = 0;

		//associate each initial state with a TRUE constraint and all other states with a FALSE constraint
		while(stateIter.hasNext()) {
			SMNode state = (SMNode) stateIter.next();
			Constraint constraint = state.isInitialNode() ? Constraint.TRUE : Constraint.FALSE; 
			stateToConstraint.put(state, constraint);
		}
	}
	
//	/**
//	 * Returns the successor configuration of this configuration under edge.
//	 * Processes all currently active threads which are registered.
//	 * @param shadow and {@link SMVariableEdge} of the program graph
//	 * @return the successor configuration under edge
//	 */
//	public Configuration doTransition(Shadow shadow) {
//		Configuration result = doTransitionInternal(shadow);
//		return result;//.processActiveThreads();
//	}

	/**
	 * Returns the successor configuration of this configuration under edge.
	 * @param edge and {@link SMVariableEdge} of the program graph
	 * @param stmt the statement to which this shadow applies
	 * @return the successor configuration under edge
	 */
	public Configuration doTransition(Shadow shadow, Stmt stmt) {
		//the skip-copy has to be initialized as a copy of this configuration
		Configuration skip = (Configuration) clone();
		//the tmp-copy needs to be initialized to false on all states,
		//(we initialize it to true for initial states but that does not matter
		//because they are all the time true anyway)
		Configuration tmp = getCopyResetToInitial();
		
		//get the current symbol name
		final String symbolName = Naming.getSymbolShortName(shadow.getUniqueShadowId());//Naming.getSymbolShortName(edge.getLabel());
		//and thee variable binding
		final Map bindings = shadow.getVariableMapping();//edge.getVariableMapping();
		//the shadow id
		final String shadowId = shadow.getUniqueShadowId();//edge.getQualifiedShadowId();
		//all variables of the state machine
		final StateMachine sm = analysis.getTracematch().getStateMachine();
		final Collection allVariables =
			Collections.unmodifiableCollection(analysis.getTracematch().getVariableOrder(symbolName));

		
		//for all transitions in the state machine
		for (Iterator transIter = sm.getEdgeIterator(); transIter.hasNext();) {
			SMEdge transition = (SMEdge) transIter.next();
			
			//if the labels coincide
			if(transition.getLabel().equals(symbolName)) {

				//statistics
				iterationCount++;
				
				
				if(transition.isSkipEdge()) {
					//if we have a skip transition
					assert transition.getSource()==transition.getTarget(); //must be a loop
					
					//get the state of this skip loop
					SMNode skipState = transition.getTarget();
					assert !skipState.isFinalNode(); 		   //only nonfinal nodes should have skip edges
					assert getStates().contains(skipState);    //assert consistency
					
					//get the old constraint at the state
					Constraint oldConstraint = skip.getConstraintFor(skipState);
					
					//add negative bindings
					Constraint newConstraint = oldConstraint.addNegativeBindingsForSymbol(
							allVariables,
							skipState,
							bindings,
							shadowId,
							analysis,
							stmt
					);
					
					//store the result at the original (=target) state
					skip.stateToConstraint.put(skipState, newConstraint);
				} else {
					//a "normal" transition					 
					
					//get constraint at source state
					Constraint oldConstraint = getConstraintFor(transition.getSource());
					
					//add bindings
					Constraint newConstraint = oldConstraint.addBindingsForSymbol(
							allVariables, 
							transition.getTarget(), 
							bindings, 
							shadowId,
							analysis
					); 

					//put the new constraint on the target state
					//via a disjoint update
					tmp.disjointUpdateFor(transition.getTarget(), newConstraint);
				}
			}
		}

		//disjointly merge the constraints of tmp and skip
		tmp = tmp.getJoinWith(skip);
		//cleanup the resulting configuration
		tmp.cleanup();
		//return an interned version of the result
		return tmp.intern();
	}
	
//	/**
//	 * Registers an active thread with a clone of this configuration which is returned.
//	 * It is immediately processed and will be processed on each subsequent call of {@link #doTransition(SMVariableEdge)}
//	 * on this clone. 
//	 * @param threadSpawnEdge any {@link SMThreadSpawnEdge} of the associated program graph
//	 * @return a clone with all edges in the thread summary of threadSpawnEdge registered;
//	 * <code>this</code> is returned if all edges are already registered
//	 */
//	public Configuration registerActiveThread(SMThreadSpawnEdge threadSpawnEdge) {
//		
//		//if all edges are already registered, do nothing
//		Set threadEdges = threadSpawnEdge.getThreadSummary().edgesMaybeTriggeredByThread();
//		if(this.edgesMaybeTriggeredByThread.containsAll(threadEdges)) {
//			return this;
//		}
//		
//		//clone
//		Configuration copy = (Configuration) clone();
//		
//		//for all edges in the threadsummary
//		for (Iterator edgeIter = threadEdges.iterator(); edgeIter.hasNext();) {
//			SMEdge edge = (SMEdge) edgeIter.next();
//			//if the edge belongs to the tracematch we are analyzing
//			if(Naming.getTracematchName(edge.getLabel()).equals(analysis.getTracematch().getName())) {
//				copy.edgesMaybeTriggeredByThread.add(edge);
//			}
//		}
//		
//		/* we have to do at least as many iterations as the TM state machine has edges to be on the safe side;
//		 * this is for the following reason:
//		 * assume you have a pattern "a a" over an alphabet {a,b} and your thread summary edges are {a,b};
//		 * further assume the thread spawn edge leads directly to a final state:
//		 * q_0 ---ThreadSummary({a,b})---> q_f with q_f has not successors;
//		 * if we now only iterated once, "a" would not reach the final state - we need to iterate at least twice  
//		 */
//		
//		StateMachine sm = analysis.getTracematch().getStateMachine();		
//		for(int i=0;i<sm.size();i++) {
//			copy = copy.processActiveThreads();
//		}
//		
//		//return an interned version of the result
//		return copy.intern();
//	}
	
//	/**
//	 * Processes all registered active threads, i.e. propagates the current state
//	 * along all edges that may be triggered by active threads.
//	 * @return the resulting configuration
//	 */
//	protected Configuration processActiveThreads() {
//		if(edgesMaybeTriggeredByThread.size()==0) {
//			return this;
//		}
//		
//		//make a copy of this configuration
//		Configuration result = (Configuration) clone();
//		//for all edges that may be triggered by a thread
//		for (Iterator edgeIter = edgesMaybeTriggeredByThread.iterator(); edgeIter.hasNext();) {
//			SMVariableEdge edge = (SMVariableEdge) edgeIter.next();
//			//propagate along this edge
//			Configuration tmp = doTransitionInternal(edge);
//			
//			//statistics
//			//Bench.THR_THREAD_ITERATIONS++;
//			
//			//merge the resulting constraints (by disjunction) with the current ones
//			result = result.getJoinWith(tmp);			
//		}		
//		//return an interned version of the result
//		return result.intern();
//	}
	
	/**
	 * Merges the constraint disjoiuntly with the one currently associated with the state,
	 * updating this constraint of state.
	 * @param state any state in {@link #getStates()}
	 * @param constraint the constraint to merge
	 */
	public void disjointUpdateFor(SMNode state, Constraint constraint) {
		assert getStates().contains(state);		
		Constraint currConstraint = (Constraint) stateToConstraint.get(state);		
		stateToConstraint.put(state, currConstraint.or(constraint));
	}

	/**
	 * Joins this configuration with the other one and returns the result.
	 * This implies a disjoint update of all associated constraints and a merge
	 * of the associated thread edges. 
	 * @param other another configuration 
	 * @return the joined configuration
	 */
	public Configuration getJoinWith(Configuration other) {
		assert other.getStates().equals(getStates());
		assert analysis.equals(other.analysis);
		
		Configuration clone = (Configuration) clone();
		for (Iterator stateIter = getStates().iterator(); stateIter.hasNext();) {
			SMNode state = (SMNode) stateIter.next();
			clone.disjointUpdateFor(state, other.getConstraintFor(state));
		}
		clone.edgesMaybeTriggeredByThread.addAll(other.edgesMaybeTriggeredByThread);
		return clone;
	}

	/**
	 * Returns a copy of this configuration but with all constraints reset
	 * to the ones of the initial configuration.
	 * @return a configuration where each state <i>s</i> is mapped to <code>{@link Constraint#TRUE}</code>
	 * if it is initial and {@link Constraint#FALSE} otherwise.
	 */
	public Configuration getCopyResetToInitial() {
		Configuration copy = (Configuration) clone();
		for (Iterator iter = copy.stateToConstraint.entrySet().iterator(); iter.hasNext();) {
			Entry entry = (Entry) iter.next();
			SMNode state = (SMNode) entry.getKey();			
			entry.setValue(state.isInitialNode() ? Constraint.TRUE : Constraint.FALSE);
		}		
		return copy;
	}
	
	/**
	 * Cleans up this configuration.
	 * @see Constraint#cleanup()
	 */
	public void cleanup() {
		for (Iterator constIter = stateToConstraint.values().iterator(); constIter.hasNext();) {
			Constraint c = (Constraint) constIter.next();
			c.cleanup();
		}
	}
	
	/**
	 * Interns the configuration, i.e. returns a (usually) unique equal instance for it.
	 * @return a unique instance that is equal to this 
	 */
	protected Configuration intern() {
		Configuration cached = (Configuration) configToUniqueConfig.get(this);
		if(cached==null) {
			cached = this;
			configToUniqueConfig.put(this, this);
		}
		return cached;
	}

	/**
	 * Returns the state set of this configuration.
	 * @return
	 */
	public Set getStates() {
		return new HashSet(stateToConstraint.keySet()); 
	}
	
	/**
	 * Returns the constraint currently assosiated with the state. 
	 * @param state any state from {@link #getStates()}
	 * @return the constraint currently associated with this state
	 */
	public Constraint getConstraintFor(SMNode state) {
		assert getStates().contains(state);
		return (Constraint) stateToConstraint.get(state);
	}
	
	/**
	 * @return the number of disjuncts in this configuration
	 */
	public int size() {
		int res = 0;
		for (Iterator constIter = stateToConstraint.values().iterator(); constIter.hasNext();) {
			Constraint constr = (Constraint) constIter.next();
			res += constr.size();
		}
		return res;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		SMNode[] sorted = new SMNode[stateToConstraint.size()];
		//sort all states
		for (Iterator stateIter = stateToConstraint.keySet().iterator(); stateIter.hasNext();) {
			SMNode state = (SMNode) stateIter.next();
			sorted[state.getNumber()] = state;
		}

		String res = "[\n";
		for (int i = 0; i < sorted.length; i++) {
			SMNode state = sorted[i];
			res += "\t" + state.getNumber() + " -> " + stateToConstraint.get(state) + "\n";			
		}
		res += "]\n";

		return res;
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected Object clone() {
		Configuration clone;
		try {
			clone = (Configuration) super.clone();
			clone.stateToConstraint = (HashMap) stateToConstraint.clone();
			clone.edgesMaybeTriggeredByThread = (HashSet) edgesMaybeTriggeredByThread.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((analysis == null) ? 0 : analysis.hashCode());
		result = prime
				* result
				+ ((edgesMaybeTriggeredByThread == null) ? 0
						: edgesMaybeTriggeredByThread.hashCode());
		result = prime
				* result
				+ ((stateToConstraint == null) ? 0 : stateToConstraint
						.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		final Configuration other = (Configuration) obj;
		if (analysis == null) {
			if (other.analysis != null)
				return false;
		} else if (!analysis.equals(other.analysis))
			return false;
		if (edgesMaybeTriggeredByThread == null) {
			if (other.edgesMaybeTriggeredByThread != null)
				return false;
		} else if (!edgesMaybeTriggeredByThread
				.equals(other.edgesMaybeTriggeredByThread))
			return false;
		if (stateToConstraint == null) {
			if (other.stateToConstraint != null)
				return false;
		} else if (!stateToConstraint.equals(other.stateToConstraint))
			return false;
		return true;
	}
	
}
