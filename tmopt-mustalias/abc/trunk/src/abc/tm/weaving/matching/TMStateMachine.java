/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Pavel Avgustinov
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

package abc.tm.weaving.matching;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.util.ErrorInfo;
import polyglot.util.Position;
import soot.util.IdentityHashSet;
import soot.util.SingletonList;
import abc.main.Debug;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdgeFactory.DefaultSMEdgeFactory;

/**
 * Implementation of the StateMachine interface for tracematch matching
 * @author Pavel Avgustinov
 */

public class TMStateMachine implements StateMachine {

	protected static SMEdgeFactory edgeFactory;

	/** List of edges. Some algorithms in the static TM analysis assume that this set compares on identity. */	
	protected IdentityHashSet edges = new IdentityHashSet();
	
	/** List of nodes. The indexing code generation relies on iteration order here. Hence, it has to be a *linked* hash set. */
	protected LinkedHashSet nodes = new LinkedHashSet();

	protected TraceMatch tm;
    
    /**
     * {@inheritDoc}
     */
    public State newState() {
        SMNode n = new SMNode(this, false, false);
        nodes.add(n);
        return n;
    }
    
    /**
     * @return the currently set edge factory
     */
    protected SMEdgeFactory getEdgeFactory() {
    	if(edgeFactory==null) {
    		edgeFactory = DefaultSMEdgeFactory.v();
    	}
    	return edgeFactory;
    }
    
    /**
     * Sets the edge factory for creating SMEdges.
     * This must only be set once at the beginning
     * for consistency.
     * @param factory the factory
     */
    public static void setEdgeFactory(SMEdgeFactory factory) {
    	if(edgeFactory!=null) {
    		throw new RuntimeException("Edge factory already set!");
    	}
    	edgeFactory = factory; 
    }
    
    // for creating new nodes without adding them to the nodes collection, e.g. while
    // iterating over it
    protected State newStateDontAdd() {
        SMNode n = new SMNode(this, false, false);
        return n;
    }

    /**
     * Assumes the from and to variables are actually the relevant implementations from
     * the abc.tm.weaving.matching package -- will throw ClassCastException otherwise
     * @return the new edge
     */
    public SMEdge newTransition(State from, State to, String s) {
        SMNode f = (SMNode)from;
        SMNode t = (SMNode)to;
        SMEdge edge = getEdgeFactory().createTransition(f, t, s);
        f.addOutgoingEdge(edge);
        t.addIncomingEdge(edge);
        edges.add(edge);

    	return edge;
    }

    /**
     * Creates a new transition but based on a clone of <code>toClone</code>.
     */
    public void newTransitionFromClone(State from, State to, String s, SMEdge toClone) {
    	assert isConsistent();
        try {
    		SMEdge copiedEdge = (SMEdge) toClone.clone();
        	SMNode f = (SMNode)from;
            SMNode t = (SMNode)to;
    		copiedEdge.setSource(f);
    		copiedEdge.setTarget(t);
    		copiedEdge.setLabel(s);
    		f.addOutgoingEdge(copiedEdge);
    		t.addIncomingEdge(copiedEdge);
    		edges.add(copiedEdge);
    	} catch (CloneNotSupportedException e) {
    		throw new RuntimeException(e);
    	}
    	assert isConsistent();
    }
    
    /**
     * Adds a new skip loop to <code>state</code> with label <code>label</code>.
     * @param state the state to attach the skip loop to
     * @param label the label for the skip loop
     */
    protected void newSkipLoop(State state, String label) {
    	assert isConsistent();
    	SMEdge skipLoop = edgeFactory.createSkipTransition((SMNode) state,label);
    	newTransitionFromClone(state, state, label, skipLoop);
    	assert isConsistent();
    }

    /**
	 * Eliminates epsilon transitions and unreachable states,
	 * then renumbers the states.
	 */
	public void cleanup() {
    	assert isConsistent();
		eliminateEpsilonTransitions();
		compressStates();
		renumberStates();
    	assert isConsistent();
	}
	
	/**
	 * Inserts another state machine into this one between the states
	 * source and target, such that from <code>source</code> there exists
	 * an epsilon transition to each initial node of <code>toInsert</code>
	 * and from each final state of <code>toInsert</code> to <code>target</code>.
	 * The resulting automaton also contains all edges and nodes of <code>toInsert</code>.
	 * Note that those are not copied deeply!
	 * The initial/final states of the inserted automaton are reset to be non-initial, resp. non-final.
	 * @param source the source node to insert the automaton at
	 * @param toInsert the automaton to insert
	 * @param target the target node to insert the automaton at; may be <code>null</code>,
	 * in which case no epsilon transitions are added from final nodes of the inserted automaton
	 */
	public void insertStateMachine(State source, TMStateMachine toInsert, State target) {
		assert isConsistent();
		
		//add epsilon transitions from source to all initial states
		//and from all final states to target
		boolean addedSomething = false;
		for (Iterator iter = toInsert.nodes.iterator(); iter.hasNext();) {
			State state = (State) iter.next();
			if(state.isInitialNode()) {
				newTransition(source, state, null);
				//the state is now not initial any more
				state.setInitial(false);
				addedSomething = true;
			}
			if(target!=null && state.isFinalNode()) {
				newTransition(state, target, null);
				//the state is now not final any more
				state.setFinal(false);
			}
		}
		//if this fails, this means that the state machine to be inserted
		//has no initial state
		assert addedSomething;
		//copy all edges and nodes into this machine
		nodes.addAll(toInsert.nodes);
		edges.addAll(toInsert.edges);

		assert isConsistent();
	}
    
    /**
     * Transforms the NFA into an NFA without epsilon transitions. Here the algorithm:
     * 
     * Define the 'epsilon boundary' of a node N, epsilonBoundary(N), to be the set of all nodes which can
     * be reached by epsilon transitions from N and have an outgoing non-epsilon transition or are final.
     * 
     * In a first pass, for each node N, for each node N' in epsilonBoundary(N) such that N != N', 
     * copy each non-epsilon incoming transition to N onto N'.
     * 
     * In a second pass, delete each epsilon transition.
     * 
     * This will leave some nodes inaccessible from the initial nodes and some from which
     * one cannot reach a final node -- those should be deleted in a clean-up pass which
     * should be done anyway.
     */
    protected void eliminateEpsilonTransitions() {
        assert isConsistent();
        IdentityHashSet epsilonBoundary = new IdentityHashSet();
        SMNode cur, next;
        SMEdge edge;
        Iterator closureIt, edgeIt;
        // For each node...
        Iterator stateIt = nodes.iterator();
        while(stateIt.hasNext()) {
            epsilonBoundary.clear();
            cur = (SMNode)stateIt.next();
            // .. construct the epsilon-closure
            cur.fillInEpsilonBoundary(epsilonBoundary);
            // .. and, ignoring the node itself
            epsilonBoundary.remove(cur);
            closureIt = epsilonBoundary.iterator();
            boolean isInitial = cur.isInitialNode();

            // .. for every node in the closure
            while(closureIt.hasNext()) {
            		next = (SMNode)closureIt.next();
            		// .. for every edge coming into the original node
            		edgeIt = cur.getInEdgeIterator();
            		while(edgeIt.hasNext()) {
            			edge = (SMEdge)edgeIt.next();
            			// .. copy that edge onto the node from the closure if it isn't an epsilon transition
            			if(edge.getLabel() != null && !edge.getSource().hasEqualEdgeTo(next, edge)) {
            				newTransitionFromClone(edge.getSource(), next, edge.getLabel(), edge);
            			}
            		}
            		// Any node in the closure of an initial node is initial.
            		if(isInitial) next.setInitial(true);
            }
        }
        
        IdentityHashSet epsilonEdges = new IdentityHashSet();
        // Find all epsilon edges
        edgeIt = edges.iterator();
        while(edgeIt.hasNext()) {
            edge = (SMEdge)edgeIt.next();
            if(edge.getLabel() == null) {
            	epsilonEdges.add(edge);
            }
        }
        
        //remove the epsilon edges
        removeEdges(epsilonEdges.iterator());
        
        assert isConsistent();
    }
    
    /**
     * compute all states that are forwards-reachable from an initial state
     * @return reachable states
     */
    private Set initReachable() {
    	Set result = new IdentityHashSet();
		for(Iterator it=getStateIterator(); it.hasNext(); ) {
				   SMNode node = (SMNode) it.next();
				   if (node.isInitialNode())
					   node.fillInClosure(result,false,true);
	   }
        return result;
    }
    
    /**
     * compute all the states that are backwards reachable from a final state
     * @return set of reachable states
     */
	private Set finalReachable() {
		Set result = new IdentityHashSet();
		for(Iterator it=getStateIterator(); it.hasNext(); ) {
				SMNode node = (SMNode) it.next();
				if (node.isFinalNode())
					node.fillInClosure(result,false,false);
		}
		return result;
	}
    
    /**
     * Removes 'unneeded' states -- i.e. states that cannot possibly lie on a path from
     * an initial state to a finnal state. Assumes there are no epsilon transitions (not
     * sure if this is necessary, though).
     */
    public void compressStates() {
        // TODO: This might be better done with flags on the nodes...
        Set initReachable = initReachable();
        Set finalReachable = finalReachable();
       
        // The set of nodes we need to keep is (initReachable intersect finalReachable), 
        IdentityHashSet nodesToRemove = new IdentityHashSet();
        nodesToRemove.addAll(nodes);
        initReachable.retainAll(finalReachable); // nodes that are both init- and final-reachable
        nodesToRemove.removeAll(initReachable);  // -- we want to keep them
        
        // iterate over all nodes we want to remove and remove them, i.e. destroy their edges
        Iterator it = nodesToRemove.iterator();
        while(it.hasNext()) {
            SMNode cur = (SMNode)it.next();
            Iterator edgeIt = cur.getOutEdgeIterator();
            while(edgeIt.hasNext()) {
                SMEdge edge = (SMEdge)edgeIt.next();
                edge.getTarget().removeInEdge(edge);
                edges.remove(edge);
                edgeIt.remove(); // call this rather than removeOutEdge, as we mustn't
                                 // alter the collection while iterating over it
            }
            edgeIt = cur.getInEdgeIterator();
            while(edgeIt.hasNext()) {
                SMEdge edge = (SMEdge)edgeIt.next();
                edge.getSource().removeOutEdge(edge);
                edges.remove(edge);
                edgeIt.remove(); // call this rather than removeInEdge, as we mustn't
                                 // alter the collection while iterating over it
            }
            nodes.remove(cur);
        }
    }
    
    /**
     * Part of the automaton construction -- we add a skip self-loop to every state, and
     * a self-loop for each declared symbol for initial states. Compare email from Oege
     * from 13:22 04/07/05.
     * 
     * Ammendment: We don't actually include the self loops on initial states, since their
     * constraints are always considered true (we match all suffixes, i.e. we can always be
     * in an initial state).
     * 
     * This assumes that no state already has a skip loop. Skips are empty labels (as opposed
     * to null labels, which represent epsilon transitions -- those should have been eliminated).
     * 
     * TODO coment
     */
    protected void addSelfLoops(Collection/*<String>*/ declaredSymbols) {
        SMNode cur;
        Iterator it = nodes.iterator();
        while(it.hasNext()) {
            cur = (SMNode)it.next();
            // Initial states always have 'true' constraints anyway.
            if(!cur.isInitialNode()) { 
            	//for each symbol...
            	for (Iterator symIter = declaredSymbols.iterator(); symIter.hasNext();) {
					String symbolName = (String) symIter.next();
					//... for which there does not exist a loop already at the state...					
					if(!cur.hasEdgeTo(cur, symbolName)) {
						//we implement a small optimization here:
						//in general we do not need a skip loop for a symbol s at a state q
						//if we know that whenever taking this skip loop another edge coming
						//into q must be taken as well;
						//this is always the case if this incoming edge originates from an
						//initial state, because we are always in an initial state;
						//further optimization potential could be yielded by a "must-flow"
						//analysis
						boolean needSkipLoopForThisSymbol = true;
						if(Debug.v().skipLoopOpt) {
							for (Iterator nodeIter = nodes.iterator(); nodeIter.hasNext();) {
								SMNode state = (SMNode) nodeIter.next();
								if(state.isInitialNode() && state.hasEdgeTo(cur, symbolName)) {
									needSkipLoopForThisSymbol = false;
								}							
							}
						}
						//if we need the skip loop
						if(needSkipLoopForThisSymbol) {
							//... add a skip loop for that symbol
							newSkipLoop(cur, symbolName);
						}
					}
	            }
	        }
	    }
    }
    
    /**
     * To avoid having to update automaton states after *every* joinpoint, our implementation
     * crucially depends on the fact that the only transitions labelled with 'skip' are self-
     * loops.
     * 
     * The natural construction for that is to add self loops to all states (this is done by
     * addSelfLoops(), which should be called immediately before this method), then forming
     * another automaton Q with two states, self-loops for all symbols and skip on the first state
     * and transitions for every symbol to the second and final state, and taking the automaton
     * product of the two.
     * 
     * However, note that, since the first state of Q has a self-loop for every symbol and for
     * skip, every state that is paired with this state retains all transitions. Also, any non-
     * final state that is paired with the second state of Q is not final and has no outgoing 
     * edges, hence can be ignored. Thus the only interesting additions are the final states
     * that are paired with Q's second state.
     * 
     * We can obtain an equivalent automaton by doing the following for each final state S: Mark
     * it as non-final, and create a new node N (which will represent S paired with Q's second 
     * state). For each incoming edge of S that is not a skip, create an incoming edge on N from 
     * the same node and with the same label. Mark N as final. 
     * 
     * Finally, observe that no final node has outgoing edges, so we can obtain an automaton 
     * that's equivalent by collapsing all the resulting final states into a single state whose
     * set of incoming edges is the union of the incoming edges for the nodes we're collapsing
     * into it.
     */
    protected void removeSkipToFinal() {
        SMNode cur;
        SMNode newFinalNode = null;
        SMEdge edge;

	// Try to find a final state in the current automaton that can be used, i.e. that only
	// has an outgoing transition labelled with 'skip'.
	IdentityHashSet finalNodes = new IdentityHashSet();
        Iterator it = nodes.iterator();
	while(it.hasNext()) {
	    cur = (SMNode)it.next();
	    if(cur.isFinalNode()) finalNodes.add(cur);
	}
	it = finalNodes.iterator();
	while(it.hasNext()) {
	    cur = (SMNode)it.next();
	    boolean suitable = true;
	    Iterator edgeIt = cur.getOutEdgeIterator();
	    while(edgeIt.hasNext()) {
		edge = (SMEdge)edgeIt.next();
		if(!edge.isSkipEdge() || (edge.getTarget() != cur)) {
		    suitable = false;
		    break;
		}
	    }
	    if(suitable) {
		newFinalNode = cur;
		edgeIt = cur.getOutEdgeIterator();
		while(edgeIt.hasNext()) {
		    edge = (SMEdge)edgeIt.next();
		    edgeIt.remove();						//remove edge as outgoing from cur
		    edge.getTarget().removeInEdge(edge);	//remove edge as incoming from its target
		    edges.remove(edge);						//remove it from the edge list
		}
		break;
	    }
	}
	if(newFinalNode == null) newFinalNode = (SMNode)newState();

	it = nodes.iterator();
        while(it.hasNext()) {
            cur = (SMNode)it.next();
            if(cur.isFinalNode()) {
                cur.setFinal(false);
                Iterator edgeIt = cur.getInEdgeIterator();
                while(edgeIt.hasNext()) {
                    edge = (SMEdge)edgeIt.next();
                    if(!edge.isSkipEdge() && !edge.getSource().hasEdgeTo(newFinalNode, edge.getLabel())) { 
                    	// i.e. if not a skip-edge and not a duplicate
                        newTransitionFromClone(edge.getSource(), newFinalNode, edge.getLabel(),edge);
                    }
                }
            }
        }
        newFinalNode.setFinal(true);
    }
    
    /**
     * Accumulates, for each state, information about which tracematch vars must be
     * stored using a strong reference. We want to use a weak reference for a variable X in
     * state S if and only if every path from the S to a final state F binds X.
     * Conversely, we must keep a strong reference if and only if there is some path
     * from S to a final state that does not bind X. 
     * 
     * @param formals variables declared in tracematch
     * @param symtovar mapping from symbols to sets of bound variables
     * @param notused variables not used in tracematch body
     */
    protected void collectBindingInfo(List formals,TraceMatch tm,Collection notused,Position pos) {
        // do a backwards analysis from the final nodes
    	//  
    	// for an edge e, the flow function is
    	//  flowAlongEdge(e)(X) = X union e.boundVars
    	//
    	// we want to compute the meet-over-all-paths solution at each state
    	initCollectableWeakRefs(tm);
    	fixCollectableWeakRefs(tm);
        collectableWeakRefsToOtherRefs(formals,notused,tm);
        initBoundVars(formals);
        fixBoundVars(tm);
        if (!formals.isEmpty())
        	generateLeakWarnings(pos);
    }
    
   	
	/**
     * initialise the collectableWeakRefs fields for the meet-over-all-paths computation
     * 
	 * @param formals all variables declared in the tracematch
	 */
	private void initCollectableWeakRefs(TraceMatch tm) {
    	// we want a maximal fixpoint so for all final nodes the
    	// starting value is the empty set
		// and for all other nodes it is the set of all non-primitive formals
    	for (Iterator edgeIter = getStateIterator(); edgeIter.hasNext(); ) {
        	SMNode node = (SMNode) edgeIter.next();
        	if (node.isFinalNode())
        		node.collectableWeakRefs = new LinkedHashSet();
        	else
        		node.collectableWeakRefs = new LinkedHashSet(tm.getNonPrimitiveFormalNames()); 
        }
	}
	
	/**
		 * initialise the boundVars fields for the meet-over-all-paths computation
		 * 
		 * @param formals all variables declared in the tracematch
		 */
	private void initBoundVars(Collection formals) {
			// we want a maximal fixpoint so for all final nodes the
			// starting value is the empty set
			// and for all other nodes it is the set of all formals
			for (Iterator edgeIter = getStateIterator(); edgeIter.hasNext(); ) {
				SMNode node = (SMNode) edgeIter.next();
				if (node.isInitialNode())
					node.boundVars = new LinkedHashSet();
				else
					node.boundVars = new LinkedHashSet(formals); 
			}
		}

	/**
	 * do fixpoint iteration using a worklist of edges
	 * 
	 * @param tm tracematch, which provides a mapping from symbols
     *           to sets of bound variables
	 */
	private void fixCollectableWeakRefs(TraceMatch tm) {
		// the worklist contains edges whose target has changed value
        List worklist = new LinkedList(edges);
        while (!worklist.isEmpty()) {
        	SMEdge edge = (SMEdge) worklist.remove(0);
        	SMNode src = edge.getSource();
        	SMNode tgt = edge.getTarget();
        	// now compute the flow function along this edge
        	Set flowAlongEdge = new LinkedHashSet(tgt.collectableWeakRefs);
        	Collection c = tm.getVariableOrder(edge.getLabel());
        	if (c != null)
        	   flowAlongEdge.addAll(c);
        	// if src.collectableWeakRefs is already smaller, skip
        	if (!flowAlongEdge.containsAll(src.collectableWeakRefs)) {
               // otherwise compute intersection of 
        	   // src.collectableWeakRefs and flowAlongEdge
        	   src.collectableWeakRefs.retainAll(flowAlongEdge);
               // add any edges whose target has been affected to
        	   // the worklist
        	   for (Iterator edgeIter=edges.iterator(); edgeIter.hasNext(); ) {
        	   	   SMEdge anotherEdge = (SMEdge) edgeIter.next();
        	   	   if (anotherEdge.getTarget() == src && 
        	   	   		!worklist.contains(anotherEdge))
        	   	   	worklist.add(0,anotherEdge);	
        	   }
        	}
        }
	}

	/**
		 * do fixpoint iteration using a worklist of edges
		 * 
		 * @param tm tracematch, which provides a mapping from symbols
         *           to sets of bound variables
		 */
		private void fixBoundVars(TraceMatch tm) {
			// the worklist contains edges whose target has changed value
			List worklist = new LinkedList(edges);
			while (!worklist.isEmpty()) {
				SMEdge edge = (SMEdge) worklist.remove(0);
				SMNode src = edge.getSource();
				SMNode tgt = edge.getTarget();
				// now compute the flow function along this edge
				Set flowAlongEdge = new LinkedHashSet(src.boundVars);
				Collection c = tm.getVariableOrder(edge.getLabel());
				if (c != null)
				   flowAlongEdge.addAll(c);
				// if tgt.boundVars is already smaller, skip
				if (!flowAlongEdge.containsAll(tgt.boundVars)) {
				   // otherwise compute intersection of 
				   // tgt.boundVars and flowAlongEdge
				   tgt.boundVars.retainAll(flowAlongEdge);
				   // add any edges whose target has been affected to
				   // the worklist
				   for (Iterator edgeIter=edges.iterator(); edgeIter.hasNext(); ) {
					   SMEdge anotherEdge = (SMEdge) edgeIter.next();
					   if (anotherEdge.getSource() == tgt && 
							!worklist.contains(anotherEdge))
						worklist.add(0,anotherEdge);	
				   }
				}
			}
		}
	
	
	 /**
	  * compute for each node n, n.needStrongRefs := complement(n.collectableWeakRefs);
	 * @param formals variables declared in the tracematch
	 */
	private void collectableWeakRefsToOtherRefs(Collection formals, Collection notUsed, TraceMatch tm) {
		// for codegen we really need the complement of src.collectableWeakRefs
        // so compute that in 
		for (Iterator stateIter = getStateIterator(); stateIter.hasNext(); ) {
			SMNode node = (SMNode) stateIter.next();
			// start with the set of all declared formals
			node.needStrongRefs = new LinkedHashSet(formals);
			if (Debug.v().onlyStrongRefs) {
				//use only strong references
				node.collectableWeakRefs.clear();
				node.weakRefs = new LinkedHashSet();
			} else if (Debug.v().noCollectableWeakRefs) {
				//use no collectable weak refs
				//i.e. make them all "usual" weak refs
				node.weakRefs.addAll(node.collectableWeakRefs);
				node.collectableWeakRefs.clear();
			} else {
			// and remove those that are in node.weakRefs and those that are not used
			// everything else is a non-collectable weakRef
			node.weakRefs = new LinkedHashSet(tm.getNonPrimitiveFormalNames());
                        node.weakRefs.removeAll(node.collectableWeakRefs);
                        node.weakRefs.retainAll(notUsed);
                        node.needStrongRefs.removeAll(node.collectableWeakRefs);
                        node.needStrongRefs.removeAll(node.weakRefs);
			}
		}
	}
	
	/**
	 * generate warnings for potential space leaks; ignoring possible null bindings for now.
	 * there ought to be a check that the weak references are not bound to null, or we
	 * should completely rule out null bindings in tracematches.
	 *
	 */
	private void generateLeakWarnings(Position pos) {
		boolean hasWarned = false;
		for (Iterator it = getStateIterator(); it.hasNext() && !hasWarned; ) {
			SMNode node = (SMNode) it.next();

			Set rebound = new HashSet(node.collectableWeakRefs);
			rebound.retainAll(node.boundVars);

			if (rebound.isEmpty() && !node.isInitialNode() && !node.isFinalNode()) {
				hasWarned = true;
				String msg="Variable bindings may cause space leak";
		        abc.main.Main.v().error_queue.enqueue
						(new ErrorInfo(ErrorInfo.WARNING,
									   msg,
									   pos));
			}
		}
	}

	/**
     * Renumbers the states, starting from 0 and going in the iteration order of the
     * nodes set. Can break state-constraint class associations, so only call this once
     * after the FSA is fully transformed. Node numbers are -1 prior to this method
     * being called.
     */
    public void renumberStates() {
        int cnt = 0;
        Iterator it = nodes.iterator();
        while(it.hasNext()) {
            ((SMNode)it.next()).setNumber(cnt++);
        }
    }

    /**
     * PROTOTYPE METHOD
     *
     * Prints out which variables should be used to index the disjuncts
     * on each state.
     *
     * @param tm represents the tracematch for this automaton and has
     *           lists of the symbols used and the variables that they
     *           bind
     */
    private void chooseIndices(TraceMatch tm)
    {
        if (abc.main.Debug.v().printIndices)
        	System.out.println(this);

        Collection frequentSymbols = tm.getFrequentSymbols();

        Iterator nodeIt = nodes.iterator();
        while(nodeIt.hasNext()) {
            SMNode cur = (SMNode) nodeIt.next();

            // we do not index on the initial or final node
            if (cur.isInitialNode() || cur.isFinalNode())
                continue;

            // calculate indices[i] = intersect[sym] (bound[i] /\ binds[sym])
            //   BUT only for the symbols where the inner
            //       intersection is not empty
            //
            // if some symbols have been annotated as frequent
            // then only consider them when making indexing decisions

            Iterator symIt =
                frequentSymbols == null ? tm.getSymbols().iterator()
                                        : frequentSymbols.iterator();

            HashSet indices = new HashSet(cur.boundVars);
            while (symIt.hasNext()) {
                String symbol = (String) symIt.next();

                if (frequentSymbols != null
                        && !frequentSymbols.contains(symbol))
                    continue;

                HashSet tmp = new HashSet(cur.boundVars);
                tmp.retainAll(tm.getVariableOrder(symbol));

                if (!tmp.isEmpty())
                    indices.retainAll(tmp);
            }

            HashSet collectable = new HashSet(indices);
            collectable.retainAll(cur.collectableWeakRefs);
            HashSet primitive = new HashSet(indices);
            primitive.removeAll(tm.getNonPrimitiveFormalNames());
            HashSet weak = new HashSet(indices);
            weak.retainAll(cur.weakRefs);

            indices.removeAll(collectable);
            indices.removeAll(primitive);
            indices.removeAll(weak);


            if (abc.main.Debug.v().printIndices) {
                System.out.println("State " + cur.getNumber());
                System.out.println(" - collectable indices: " + collectable);
                System.out.println(" -   primitive indices: " + primitive);
                System.out.println(" -        weak indices: " + weak);
                System.out.println(" -       other indices: " + indices);
            }

            cur.indices.clear();
            
            cur.indices.addAll(collectable); cur.nCollectable = collectable.size();
            cur.indices.addAll(primitive);   cur.nPrimitive = primitive.size();
            cur.indices.addAll(weak);        cur.nWeak = weak.size();
            cur.indices.addAll(indices);     cur.nStrong = indices.size();
        }
    }

    /**
     * Reverses the automaton (i.e. flip the direction of every edge, make final states initial
     * and initial states final).
     */
    protected void reverse() {
    		for(Iterator edgeIt = this.edges.iterator(); edgeIt.hasNext(); ) {
    			((SMEdge)edgeIt.next()).flip();
    		}
    		for(Iterator nodeIt = this.nodes.iterator(); nodeIt.hasNext(); ) {
    			SMNode node = (SMNode)nodeIt.next();
    			boolean init = node.isFinalNode();
    			boolean fin = node.isInitialNode();
    			node.setInitial(init);
    			node.setFinal(fin);
    		}
    }
    
    /**
     * Uses the standard powerset construction to determinise the current automaton.
     * Assumes there are no epsilon transitions (eliminate those first)
     * and no skip loops yet.
     */
    protected TMStateMachine determinise() {
    		//the algorithm currently does not correctly clone skip loops;
    		//hence assert that there are no skip loops yet
    		assert !hasSkipLoops();
    	
    		TMStateMachine result = new TMStateMachine();
    		HashMap nodeMap = new HashMap();
    		
    		// Create the initial state of the new automaton
    		IdentityHashSet initialNodeSet = new IdentityHashSet();
    		for(Iterator nodeIt = nodes.iterator(); nodeIt.hasNext(); ) {
    			SMNode node = (SMNode)nodeIt.next();
    			if(node.isInitialNode()) initialNodeSet.add(node);
    		}
    		nodeMap.put(initialNodeSet, result.newState());
    		
    		/* The following block is to allow edges to have richer information than
    		 * just a string label but still be usable in a state machine that can be
    		 * determinized.
    		 * In order to do so, we associate a temporary label (a number) with each edge.
    		 * This number must be unique for edges with "equal associated content", which can
    		 * be an equal string label in the easiest case.
    		 * In order to not compare on the states associated with the edge, we call  
    		 * SMEdge.setEqualsDespiteState(true), which temporarily disables this part of the comparison.
    		 */ 
    		
    		Map edgeToLabel = new HashMap();
    		Map labelToEdge = new HashMap();
    		{    			
	    		SMEdge.setEqualsDespiteState(true);
	    		int label = 0;
	    		for (Iterator edgeIter = getEdgeIterator(); edgeIter.hasNext();) {
					SMEdge edge = (SMEdge) edgeIter.next();
					assert edge.getLabel()!=null; //no epsilon transitions
					if(!edgeToLabel.containsKey(edge)) {
						edgeToLabel.put(edge, new Integer(label));
						labelToEdge.put(new Integer(label),edge);
						label++;
					}
				}
	    		SMEdge.setEqualsDespiteState(false);
    		}
    		
    		// add the initial state to the worklist
    		LinkedList worklist = new LinkedList();
    		worklist.add(initialNodeSet);
    		
    		// While we have things in the worklist...
    		while(!worklist.isEmpty()) {
    			IdentityHashSet curSet = (IdentityHashSet)worklist.removeFirst();
    			HashMap/*<String,IdentityHashSet>*/ succForSym = new HashMap();
    			// ... for each of the nodes in the next worklist item...
    			for(Iterator nodeIt = curSet.iterator(); nodeIt.hasNext(); ) {
    				SMNode node = (SMNode)nodeIt.next();
    				// ... for each outgoing edge of that node...
    				for(Iterator edgeIt = node.getOutEdgeIterator(); edgeIt.hasNext(); ) {
    					SMEdge edge = (SMEdge)edgeIt.next();
    					
    					SMEdge.setEqualsDespiteState(true);
    					Integer label = (Integer) edgeToLabel.get(edge); 
    					assert label!=null;
    					SMEdge.setEqualsDespiteState(false);
    					
    					if(succForSym.get(label) == null)
    						succForSym.put(label, new IdentityHashSet());
    					// record that the target of the edge is reachable via a transition with the label.
    					((IdentityHashSet)succForSym.get(label)).add(edge.getTarget());
    				}
    			}
    			// Then, for each of the sets reachable with transitions of a given label, ...
    			for(Iterator symIt = succForSym.keySet().iterator(); symIt.hasNext(); ) {
    				Integer label = (Integer)symIt.next();
    				Object succSet = succForSym.get(label);
					if(nodeMap.get(succSet) == null) { 
    					nodeMap.put(succSet, result.newState());
    					worklist.addLast(succSet);
    				}
    				// if the DFA doesn't have a corresponding transition, add it.
    				SMNode from = (SMNode)nodeMap.get(curSet);
    				SMNode to = (SMNode)nodeMap.get(succSet);
    				SMEdge currEdge = (SMEdge) labelToEdge.get(label);
    				
    				if(!from.hasEqualEdgeTo(to, currEdge))
    					result.newTransitionFromClone(from, to, currEdge.getLabel(), currEdge);
    			}
    		}
    		// Finally, determine initial and final states of the new automaton.
    		// The only initial node is the one we started off with.
    		((SMNode)nodeMap.get(initialNodeSet)).setInitial(true);
    		
    		// A node is final if its nodeset contains a node that was final in the NFA.
    		for(Iterator setIt = nodeMap.keySet().iterator(); setIt.hasNext(); ) {
    			IdentityHashSet curSet = (IdentityHashSet)setIt.next();
    			boolean isFinal = false;
    			for(Iterator nodeIt = curSet.iterator(); nodeIt.hasNext() && !isFinal; ) {
    				isFinal |= ((SMNode)nodeIt.next()).isFinalNode();
    			}
    			((SMNode)nodeMap.get(curSet)).setFinal(isFinal);
    		}
		return result;
    }
    
      /**
       * Minimizes by the well-known (?)
       * reverse/determinize/reverse/determinize method.
       * 
       * @param prepareForMatching if <code>true</code>, the resulting automaton is prepared for matching;
       * @return the minimized automaton 
       */
      public TMStateMachine getMinimized(boolean prepareForMatching) {
          //reverse
          reverse();
          //remove skip loops for determinization
          boolean skipLoopsRemoved = removeSkipLoops();
          
          //if there were skip loops present, we really should add them again by calling prepareForMatching
          assert !skipLoopsRemoved || prepareForMatching;
          
          //create determinized copy
          TMStateMachine det = determinise();
          //restore original
          reverse();
          //do second iteration on copy
          det.reverse();
          TMStateMachine min = det.determinise();
          
          if(prepareForMatching) {
        	  assert tm!=null;
        	  
        	  //prepare the automaton for matching, before we return it;
        	  //do not need to minimize there, yet again
        	  min.prepareForMatching(tm,false);
        	  
        	  prepareForMatching(tm,!Debug.v().useNFA);
          }
          return min;
      }
      
    /**
     * Minimizes by the well-known (?)
     * reverse/determinize/reverse/determinize method.
     * Note that this does a destructive update.
     * Use {@link #getMinimized()} to retrieve a determinized copy.
     */
    public void minimize() {
        TMStateMachine minimized = getMinimized(true);
        this.nodes = minimized.nodes;
        this.edges = minimized.edges;
    }
    
    /**
     * Builds a minimized DFA for this NFA and substitutes this
     * with the DFA if it is smaller.
     * @return <code>true</code> if the DFA was smaller and replacement took place
     */
    public boolean minimizeIfSmaller() {
        TMStateMachine minimized = getMinimized(false);
        if(minimized.size()<size()) {
        	this.nodes = minimized.nodes;
        	this.edges = minimized.edges;
        	return true;
        } else {
        	return false;
        }
    }
    
    
    /**
     * Transforms the FSA that was generated from the regular expression into an NFA for
     * matching suffixes interleaved with skips and ending in a declared symbol against
     * the regular expression. Should be called once.
     * @param tm tracematch contains the set of symbols, and the variables that
     *           those symbols bind; if this method is called multiple times on the same object
     *           the same instance of tm must be used every time
     * @param attemptDeterminization if <code>true</code>, the method uses a DFA if it is smaller
     */
    public void prepareForMatching(TraceMatch tm, boolean attemptDeterminization) {
	assert tm!=null && (this.tm==null || this.tm==tm); //should be consistent 
    this.tm = tm;
	TMStateMachine det = null;
	
	final List formals = tm.getFormalNames();
	final Collection notused = tm.getUnusedFormals();
	final Position pos = tm.getPosition();
	
	//remove skip loops if we have some already
	//this can happen during reweaving
	removeSkipLoops();
	
	eliminateEpsilonTransitions();
	
	if(attemptDeterminization) {
	    reverse();
	    det = determinise();
	    reverse();
	    det.reverse();
	    det = det.determinise();
	    det.addSelfLoops(tm.getSymbols());
	    det.removeSkipToFinal();
	    
	    det.compressStates();
	    det.renumberStates();
	}
	addSelfLoops(tm.getSymbols());
    removeSkipToFinal();
	
    compressStates();
    renumberStates();

	if(attemptDeterminization) {
	    if(this.nodes.size() >= det.nodes.size()) {
		this.edges = det.edges;
		this.nodes = det.nodes;
	    }
	}

        collectBindingInfo(formals, tm, notused, pos);
        chooseIndices(tm);
    }
    
    public Iterator getStateIterator() {
        return nodes.iterator();
    }

    public Iterator getEdgeIterator() {
        return edges.iterator();
    }

	public Set<SMNode> getInitialStates() {
		// In principle, we could memoize this.
		Set<SMNode> initialStates = new HashSet();

		for (Iterator iterator = getStateIterator(); iterator.hasNext();) {
			SMNode state = (SMNode) iterator.next();
			if(state.isInitialNode()) {
				initialStates.add(state);
			}
		}
		return initialStates;
	}

    public SMNode getStateByNumber(int n) {
        Iterator i = getStateIterator();
        while (i.hasNext()) {
            SMNode node = (SMNode) i.next();
            if (node.getNumber() == n)
                return node;
        }
        throw new RuntimeException("Looking up state number " + n +
                        ", but it does not exist.\n" + this);
    }

    public int getNumberOfStates() {
        return nodes.size();
    }
    
    public String toString() {
        String result = "State machine:\n==============\n";
        java.util.Map stateNumbers = new java.util.HashMap();
        SMNode cur; SMEdge edge;
        int cnt = 0;
        Iterator it = nodes.iterator();
        while(it.hasNext()) {
            stateNumbers.put(it.next(), new Integer(cnt++));
        }
        it = nodes.iterator();
        while(it.hasNext()) {
            cur = (SMNode)it.next();
            if(cur.isInitialNode()) result += "Initial ";
            if(cur.isFinalNode()) result += "Final ";
            result += "State " + stateNumbers.get(cur) + " (";
            result += "needStrongRefs" + cur.needStrongRefs + ", ";
            result += "collectableWeakRefs" + cur.collectableWeakRefs + ", ";
			result += "weakRefs" + cur.weakRefs + ", ";
			result += "boundVars" + cur.boundVars + ")\n";
            Iterator edgeIt = cur.getOutEdgeIterator();
            while(edgeIt.hasNext()) {
                edge = (SMEdge)edgeIt.next();
                result += "[" + edge+ "]" +
                		"\n  --> to State " + stateNumbers.get(edge.getTarget()) + "\n";
            }
        }
        return result;
    }

	/**
	 * Removes the edges enumerated by the iterator from this state machine
	 * @param edgeIterator an iterator over edges from this state machine
	 */
	public void removeEdges(Iterator/*<SMEdge>*/ edgeIterator) {
		while(edgeIterator.hasNext()) {
			SMEdge edge = (SMEdge) edgeIterator.next();

			assert edges.contains(edge);
			
			//remove the edge p-->q from the list
			edges.remove(edge);
			
			//remove it as outedge from p 
			edge.getSource().removeOutEdge(edge);
			
			//remove it as inedge from q
			edge.getTarget().removeInEdge(edge);
		}
	}	
	
	/**
	 * Removes all skip loops.
	 * @return <code>true</code> if there were any skip loops to remove
	 */
	public boolean removeSkipLoops() {
		Set skipLoops = new HashSet(); 
		
		for (Iterator iter = getEdgeIterator(); iter.hasNext();) {
			SMEdge edge = (SMEdge) iter.next();
			if(edge.isSkipEdge()) {
				skipLoops.add(edge);
			}
		}
		
		skipLoops = Collections.unmodifiableSet(skipLoops);
		removeEdges(skipLoops.iterator());
		return skipLoops.size()>0;
	}
	
	/**
	 * Returns <code>true</code> is this state machine holds no states.
	 * @return <code>true</code> is this state machine holds no states
	 */
	public boolean isEmpty() {
		//FIXME here we actually should rather return true 
		//when a final state is reachable
		
		return nodes.isEmpty();
	}
	
	/**
	 * @return <code>true</code> if this state machine has any skip loop
	 */
	protected boolean hasSkipLoops() {
		for (Iterator nodeIter = edges.iterator(); nodeIter.hasNext();) {
			SMEdge edge = (SMEdge) nodeIter.next();
			if(edge.isSkipEdge()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the transition struture of this automaton is conistent,
	 * i.e. the automaton contains all nodes all its edges point to
	 * and no node refers to an edge that is not in its edge set.
	 * Should be used guarded by <code>assert</code> or a debug flag to
	 * avoid unnecessary overheads at deployment time.
	 * @return <code>true</code> if this automaton is consistent
	 */
	public boolean isConsistent() {
		
		for (Iterator nodeIter = nodes.iterator(); nodeIter.hasNext();) {
			SMNode node = (SMNode) nodeIter.next();
			for (Iterator iter = node.getInEdgeIterator(); iter.hasNext();) {
				SMEdge edge = (SMEdge) iter.next();
				if(!edges.contains(edge)) {
					return false;
				}
			}
			for (Iterator iter = node.getOutEdgeIterator(); iter.hasNext();) {
				SMEdge edge = (SMEdge) iter.next();
				if(!edges.contains(edge)) {
					return false;
				}
			}
		}
		
		for (Iterator edgeIter = edges.iterator(); edgeIter.hasNext();) {
			SMEdge edge = (SMEdge) edgeIter.next();
			if(!nodes.contains(edge.getSource()) || !nodes.contains(edge.getTarget())) {
				return false;
			}
			
			boolean found = false;
			for (Iterator outIter = edge.getSource().getOutEdgeIterator(); outIter.hasNext();) {
				SMEdge outEdge = (SMEdge) outIter.next();
				if(outEdge==edge) {
					found = true;
					break;
				}
			}
			if(!found) {
				return false;
			}

			found = false;
			for (Iterator inIter = edge.getTarget().getInEdgeIterator(); inIter.hasNext();) {
				SMEdge inEdge = (SMEdge) inIter.next();
				if(inEdge==edge) {
					found = true;
					break;
				}
			}
			if(!found) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Replaces the original edge by a replacement (if the original edge is contained
	 * in this automaton).
	 * @param original the edge to replace
	 * @param replacement the edge to replace with
	 */
	public void replaceEdge(SMEdge original, SMEdge replacement) {
		if(edges.contains(original)) {
			//hook up replacement
			replacement.setSource(original.getSource());
			replacement.setTarget(original.getTarget());
			original.getSource().addOutgoingEdge(replacement);
			original.getTarget().addIncomingEdge(replacement);
			edges.add(replacement);

			//remove the original
			removeEdges(new SingletonList(original).iterator());
		}
	}
	
	/**
	 * @return the number of edges
	 */
	public int size() {
		return edges.size();
	}

	public static void reset() {
		edgeFactory = null;
	}
}
