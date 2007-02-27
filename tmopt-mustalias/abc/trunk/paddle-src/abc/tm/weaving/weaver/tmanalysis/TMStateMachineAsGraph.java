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
package abc.tm.weaving.weaver.tmanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.toolkits.graph.DirectedGraph;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.TMStateMachine;

/**
 * This is an adaptor (object version) that interprets a tracematch state machine as a directed graph,
 * so that it can be used in Soot's flow analysis framework.
 * <i>Note that the <b>edges</b> of the state machine are interpreted as
 * <b>nodes</b> in the directed graph and vice versa!</i> 
 * @author Eric Bodden
 */
public class TMStateMachineAsGraph implements DirectedGraph {

	protected final TMStateMachine delegate;
	protected final List ALL;
	protected final Map INITIAL_STATE_TO_ENTRY_EDGE, FINAL_STATE_TO_EXIT_EDGE;
	protected final Map nodeToSuccs;
	protected final Map nodeToPreds;
	protected final int size;
	
	/**
	 * Constructs a new adapter.
	 * @param delegate the state machine to adapt; the adapter uses caching, so delegate may not change after it has been adapted!
	 */
	public TMStateMachineAsGraph(TMStateMachine delegate) {
		this.delegate = delegate;
		//for each initial state construct an associated entry-edge
		Map initialStateToEntryEdge = new IdentityHashMap();
		for (Iterator iter = delegate.getStateIterator(); iter.hasNext();) {
			SMNode state = (SMNode) iter.next();
			if(state.isInitialNode()) {
				SMEdge startEdge = new SMEdge(null,state,null);
				initialStateToEntryEdge.put(state,startEdge);
			}
		}
		INITIAL_STATE_TO_ENTRY_EDGE = Collections.unmodifiableMap(initialStateToEntryEdge);
		//for each final state construct an associated exit-edge
		Map finalStateToExitEdge = new IdentityHashMap();
		for (Iterator iter = delegate.getStateIterator(); iter.hasNext();) {
			SMNode state = (SMNode) iter.next();
			if(state.isFinalNode()) {
				SMEdge endEdge = new SMEdge(state,null,null);
				finalStateToExitEdge.put(state,endEdge);
			}
		}		
		FINAL_STATE_TO_EXIT_EDGE = Collections.unmodifiableMap(finalStateToExitEdge);
		//construct a reference to all edges
		List all = new ArrayList();
		all.addAll(getHeads());
		all.addAll(getTails());
		for (Iterator iter = delegate.getEdgeIterator(); iter.hasNext();) {
			all.add(iter.next());
		}
		ALL = Collections.unmodifiableList(all);
		
		int theSize = 0;
		for (Iterator iter = iterator(); iter.hasNext();iter.next()) {			
			theSize++;
		}
		size = theSize;

		nodeToSuccs = new IdentityHashMap();
		nodeToPreds = new IdentityHashMap();
	}

	//the following methods implement the adaptor
	
	/** 
	 * {@inheritDoc}
	 */
	public List getHeads() {
		return new ArrayList(INITIAL_STATE_TO_ENTRY_EDGE.values());
	}

	/** 
	 * {@inheritDoc}
	 */
	public List getPredsOf(Object s) {		
		List preds = (List) nodeToPreds.get(s); 
		if(preds==null) {
			preds = new ArrayList();
			SMEdge edge = (SMEdge) s;
			if(edge.getSource()!=null) {
				for (Iterator iter = edge.getSource().getInEdgeIterator(); iter.hasNext();) {
					//TODO possible optimization: filter for epsilon-edges and edges of a 
					//specific tracematch
					preds.add(iter.next());
				}
			}
			//if we calculate the predecessors of an initial node, we also need to include
			//its associated entry-edge
			SMNode n = edge.getSource();
			if(n!=null && n.isInitialNode()) { //for entry-edges, n might be null 
				assert INITIAL_STATE_TO_ENTRY_EDGE.containsKey(n);
				preds.add(INITIAL_STATE_TO_ENTRY_EDGE.get(n));
			}
			nodeToPreds.put(s,preds);
		}
		return preds;
	}

	/** 
	 * {@inheritDoc}
	 */
	public List getSuccsOf(Object s) {
		List succs = (List) nodeToSuccs.get(s); 
		if(succs==null) {
			succs = new ArrayList();
			SMEdge edge = (SMEdge) s;
			if(edge.getTarget()!=null) { //for exit-edges, n might be null
				for (Iterator iter = edge.getTarget().getOutEdgeIterator(); iter.hasNext();) {
					succs.add(iter.next());
				}
			}
			//if we calculate the sucessors of a final node, we also need to include
			//its associated exit-edge
			SMNode n = edge.getTarget();
			if(n!=null && n.isFinalNode()) {
				assert FINAL_STATE_TO_EXIT_EDGE.containsKey(n);
				succs.add(FINAL_STATE_TO_EXIT_EDGE.get(n));
			}
			nodeToSuccs.put(s,succs);
		}
		return succs;
	}

	/** 
	 * {@inheritDoc}
	 */
	public List getTails() {
		return new ArrayList(FINAL_STATE_TO_EXIT_EDGE.values());
	}

	/** 
	 * {@inheritDoc}
	 */
	public Iterator iterator() {
		return ALL.iterator();
	}

	/** 
	 * {@inheritDoc}
	 */
	public int size() {
		return size;
	}

	//only delegate methods follow
	
	/**
	 * 
	 * @see abc.tm.weaving.matching.TMStateMachine#cleanup()
	 */
	public void cleanup() {
		delegate.cleanup();
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	/**
	 * @return
	 * @see abc.tm.weaving.matching.TMStateMachine#getNumberOfStates()
	 */
	public int getNumberOfStates() {
		return delegate.getNumberOfStates();
	}

	/**
	 * @param n
	 * @return
	 * @see abc.tm.weaving.matching.TMStateMachine#getStateByNumber(int)
	 */
	public SMNode getStateByNumber(int n) {
		return delegate.getStateByNumber(n);
	}

	/**
	 * @return
	 * @see abc.tm.weaving.matching.TMStateMachine#getStateIterator()
	 */
	public Iterator getStateIterator() {
		return delegate.getStateIterator();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return delegate.hashCode();
	}

	/**
	 * @param source
	 * @param toInsert
	 * @param target
	 * @see abc.tm.weaving.matching.TMStateMachine#insertStateMachine(abc.tm.weaving.matching.State, abc.tm.weaving.matching.TMStateMachine, abc.tm.weaving.matching.State)
	 */
	public void insertStateMachine(State source, TMStateMachine toInsert, State target) {
		delegate.insertStateMachine(source, toInsert, target);
	}

	/**
	 * @return
	 * @see abc.tm.weaving.matching.TMStateMachine#newState()
	 */
	public State newState() {
		return delegate.newState();
	}

	/**
	 * @param from
	 * @param to
	 * @param s
	 * @see abc.tm.weaving.matching.TMStateMachine#newTransition(abc.tm.weaving.matching.State, abc.tm.weaving.matching.State, java.lang.String)
	 */
	public void newTransition(State from, State to, String s) {
		delegate.newTransition(from, to, s);
	}

	/**
	 * @param from
	 * @param to
	 * @param s
	 * @param toClone
	 * @see abc.tm.weaving.matching.TMStateMachine#newTransitionFromClone(abc.tm.weaving.matching.State, abc.tm.weaving.matching.State, java.lang.String, abc.tm.weaving.matching.SMEdge)
	 */
	public void newTransitionFromClone(State from, State to, String s, SMEdge toClone) {
		delegate.newTransitionFromClone(from, to, s, toClone);
	}

	/**
	 * @param tm
	 * @param attemptDeterminization
	 * @see abc.tm.weaving.matching.TMStateMachine#prepareForMatching(abc.tm.weaving.aspectinfo.TraceMatch,boolean)
	 */
	public void prepareForMatching(TraceMatch tm, boolean attemptDeterminization) {
		delegate.prepareForMatching(tm,attemptDeterminization);
	}

	/**
	 * 
	 * @see abc.tm.weaving.matching.TMStateMachine#renumberStates()
	 */
	public void renumberStates() {
		delegate.renumberStates();
	}

	/**
	 * @return
	 * @see abc.tm.weaving.matching.TMStateMachine#toString()
	 */
	public String toString() {
		return delegate.toString();
	}

}
