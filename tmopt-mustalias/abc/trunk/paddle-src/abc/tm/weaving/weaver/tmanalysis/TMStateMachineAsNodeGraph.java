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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

import soot.toolkits.graph.DirectedGraph;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.TMStateMachine;

/**
 * This is an adaptor (object version) that interprets a tracematch state machine as a directed graph,
 * so that it can be used in Soot's flow analysis framework.
 * <i>Note that the <b>edges</b> of the state machine are interpreted as
 * <b>nodes</b> in the directed graph and vice versa!</i> 
 * @author Eric Bodden
 * @deprecated HANDLE WITH CARE; IN THE CURRENT STATE IT LEADS TO INCOMPLETE FLOW COMPUTATIONS!
 */
public class TMStateMachineAsNodeGraph implements DirectedGraph {

	protected final TMStateMachine DELEGATE;
	
	protected final List HEADS,TAILS;
	
	protected final int NODE_COUNT;
	
	protected final IdentityHashMap NODE_TO_PREDS, NODE_TO_SUCCS;
	
	/**
	 * 
	 */
	public TMStateMachineAsNodeGraph(TMStateMachine sm) {
		DELEGATE = sm;
		HEADS = new ArrayList();
		TAILS = new ArrayList();
		NODE_TO_PREDS = new IdentityHashMap();
		NODE_TO_SUCCS = new IdentityHashMap();
	
		int nodeCount = 0;
		for (Iterator nodeIter = DELEGATE.getStateIterator(); nodeIter.hasNext();) {
			SMNode state = (SMNode) nodeIter.next();
			nodeCount++;
			if(state.isInitialNode())
				HEADS.add(state);
			if(state.isFinalNode())
				TAILS.add(state);
			List preds = new ArrayList();			
			for (Iterator inIter = state.getInEdgeIterator(); inIter.hasNext();) {
				SMEdge inEdge = (SMEdge) inIter.next();
				SMNode pred = inEdge.getSource();
				if(!preds.contains(pred))
					preds.add(pred);
			}
			NODE_TO_PREDS.put(state, preds);
			List succs = new ArrayList();			
			for (Iterator outIter = state.getOutEdgeIterator(); outIter.hasNext();) {
				SMEdge outEdge = (SMEdge) outIter.next();
				SMNode succ = outEdge.getTarget();
				if(!succs.contains(succ))
					succs.add(succ);
			}
			NODE_TO_SUCCS.put(state, succs);
		}
		NODE_COUNT = nodeCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getHeads() {
		return HEADS;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getPredsOf(Object s) {
		return (List) NODE_TO_PREDS.get(s);
	}

	/**
	 * {@inheritDoc}
	 */
	public List getSuccsOf(Object s) {
		return (List) NODE_TO_SUCCS.get(s);
	}

	/**
	 * {@inheritDoc}
	 */
	public List getTails() {
		return TAILS;
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator iterator() {
		return DELEGATE.getStateIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return NODE_COUNT;
	}
	
	
}
