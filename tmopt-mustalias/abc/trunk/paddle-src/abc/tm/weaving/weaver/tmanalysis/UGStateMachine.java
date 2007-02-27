package abc.tm.weaving.weaver.tmanalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import polyglot.util.ErrorInfo;
import soot.Kind;
import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.tagkit.Host;
import soot.toolkits.graph.UnitGraph;
import soot.util.IdentityHashSet;
import abc.main.Main;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.ShadowSMEdgeFactory.SMShadowEdge;
import abc.tm.weaving.weaver.tmanalysis.VariableSMEdgeFactory.SMVariableEdge;
import abc.tm.weaving.weaver.tmanalysis.ds.ThreadContext;
import abc.tm.weaving.weaver.tmanalysis.query.Naming;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.query.TaggedHosts;
import abc.tm.weaving.weaver.tmanalysis.stages.PerMethodStateMachines;
import abc.weaving.aspectinfo.MethodCategory;

/**
 * A state machine abstracting a unit graph.
 * The state machine reflects the transition structure of a unit graph
 * with respect to tracematch symbols that match its units.
 * More specificly, this state machine has a unique starting state reflecting
 * the program state when entering the unit graph and a unique end state
 * reflecting the program state when leaving it.
 * Then, there exist transitions <i>(q,l,p)</i> from <i>q</i> to <i>p</i> with label <i>l</i>
 * and <i>l = {l1,...,ln)</i> when from q the state p is directly reachable (i.e. without passing
 * through another state first) by issuing the symbols <i>{l1,...,ln)</i>. 
 *
 * @author Eric Bodden
 */
public class UGStateMachine extends TMStateMachine implements Cloneable {
	
    /**
     * Epsilon is represented by <code>null</code>.
     */
    public final static String EPSILON = null; 
	
    /**
     * A mapping from a unit to a state representing the program state
     * immediately before executing the unit. 
     */
    protected /*final*/ IdentityHashMap unitToState; //cannot be final cause we set it within clone()
    
	/**
	 * The associated unit graph. 
	 */
	protected final UnitGraph ug;
	
	/**
	 * The unique stating state / end state.
	 */
	protected State uniqueInitialState, uniqueEndState;
	
    /** A list of all state UGStateMachines that were produced.
     *  Used for statistics. */
    protected static Collection allStateMachines = new IdentityHashSet();    
    
    /** Used for statistics. Records how often UGStateMachines are shared. */
    protected static int shareCount = 0;

	/** the call graph this state machine is based on */
	protected final CallGraph cg;
	
	/** this callback is informed of any shadows that are discovered during construction in order
	 * to determine reachable shadows */
	protected final PerMethodStateMachines callback; 
        
    /** temporary set, only used internally. */
    protected transient IdentityHashSet processedUnits;

    /** used internally for cloning this state machine */
    protected transient IdentityHashMap stateToClone;
			
	/** a cache, caching all matching shadows for a host during construction */
	protected final transient Map hostToMatches;

	/**
     * Creates a new unit graph state machine for the given unit graph.
     * It will hold {@link SMVariableEdge}s for any unit flagged with a shadow, 
     * and {@link InvokeEdge} for any statement that has outgoing edges in the call graph
     * cg. Those edges can be inlined by calling {@link #fold()}.
     * The callback is informed about shadows being found via
     * {@link PerMethodStateMachines#registerReachableShadow(String)}.
     * This state machine has a unique start end end state (those might have outgoing, resp.
     * incoming epsilon edges).
	 * @param unitGraph the unit graph of this state machine is created for
     * @param cg the call graph on whose basis {@link InvokeEdge}s should be created
     * @param callback the callback to inform about shadows discovered
	 */
	public UGStateMachine(UnitGraph unitGraph, CallGraph cg, PerMethodStateMachines callback) {		
		assert unitGraph!=null;
		this.cg = cg;
		this.ug = unitGraph;
		this.callback = callback;
		processedUnits = new IdentityHashSet(); 
		unitToState = new IdentityHashMap();
		hostToMatches = new HashMap();

		allStateMachines.add(this);
		
        //build initial state
		uniqueInitialState = newState();
		uniqueInitialState.setInitial(true);

        //construct unique end state
		uniqueEndState = newState();
		uniqueEndState.setFinal(true);

        assert isConsistent();
		
        //add epsilon transitions from initial state to a successor state for each
        //entry unit; then adds states for their whole "tail"
		for (Iterator headIter = ug.getHeads().iterator(); headIter.hasNext();) {
			Stmt head = (Stmt) headIter.next();
			
            //initial transition; might have labels which are associated
			//with the body (this is, for body shadows)
	    	newSymbolTransition(uniqueInitialState, stateFor(head), ug.getBody().getMethod());
            //all other states
			addStatesFor(head);
		}
			
        //create epsilon transitions for all states representing a tail
        //unit to the unique end state
		for (Iterator tailIter = ug.getTails().iterator(); tailIter.hasNext();) {
			Unit tail = (Unit) tailIter.next();
			
			newTransition(stateFor(tail), uniqueEndState, EPSILON);

			checkIfTailReachable(tail);
			
		}
				
		this.unitToState = null;
		
		assert isConsistent();
		
		//minimize
		eliminateEpsilonTransitions();
		minimizeIfSmaller();

		assert isConsistent();

        //do not need this any more
		processedUnits = null;
		hostToMatches.clear();
        
	}

	/**
	 * Creates a unit graph state machine for a method without a body.
	 * (e.g. used for native methods)
	 * This state machine simply holds {@link InvokeEdge}s for all the edges
	 * which are outgoing of this method, according to the abstractedCallGraph.
	 * There can be no shadows in a method without body, hence no {@link SMVariableEdge} will be created.
	 * @param the method this state machine will represent
	 * @param abstractedCallGraph a (possibly abstracted) call graph
     * @param callback the callback to inform about shadows discovered
     * @see #UGStateMachine(UnitGraph, CallGraph, PerMethodStateMachines)
	 */
	public UGStateMachine(SootMethod method, CallGraph abstractedCallGraph, PerMethodStateMachines callback) {
		assert !method.hasActiveBody();
		this.ug = null;
		this.cg = null;
		this.callback = callback;
		this.hostToMatches = null;

		//build start/end states
		uniqueInitialState = newState();
		uniqueInitialState.setInitial(true);
		this.nodes.add(uniqueInitialState);
		
		uniqueEndState = newState();
		uniqueEndState.setFinal(true);
		this.nodes.add(uniqueEndState);
		
		Iterator outEdges = abstractedCallGraph.edgesOutOf(method);
		if(outEdges.hasNext()) {
			//if there are outgoing edges
			
			//make two new inner start/end states (necessary according to Thomson construction)
			SMNode start = (SMNode) newState();
			newTransition(getInitialState(), start, EPSILON);
			SMNode end = (SMNode) newState();
			newTransition(end, getEndState(), EPSILON);
			
			//add an invoke edge between the two states;
			//this edge has an iterator "outEdges" over all outgoing call edges associated with it
            SMEdge edge = new InvokeEdge(start, end, outEdges, method);
            start.addOutgoingEdge(edge);
            end.addIncomingEdge(edge);
            edges.add(edge);			
			
		} else {
			//else, just draw an epsilon transition form start to end
			newTransition(getInitialState(), getEndState(), EPSILON);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean minimizeIfSmaller() {
		assert isConsistent();
		//determinization assumes there are no epsilon transitions
		eliminateEpsilonTransitions();
		assert isConsistent();
		boolean res = super.minimizeIfSmaller();
		//at this point the start end states are not yet restored
		//do so to become consistent again
		uniqueStartEndStates();
		assert isConsistent();
		return res;
	}
	
	/**
	 * Issues an error message if the tail unit is not reachable.
	 * This should actually only happen with method which hold a definetely infinite loop.
	 * (so actually rather a theoretical issue, but it did happen on some unrealistic microbenchmarks)
	 * @param tail a unit from {@link UnitGraph#getTails()}
	 */
	protected void checkIfTailReachable(Unit tail) {
		//we check for ug.size()>1 because one can have the special case
		//where the only unit is "return", which has no predecessor		
		if(ug.getPredsOf(tail).isEmpty() && ug.size()>1) {
			Main.v().error_queue.enqueue(ErrorInfo.WARNING,"Found unreachable method exit point in " +
					ug.getBody().getMethod().getSignature() + ". Analysis information for static tracematch analysis " +
					"might be inaccurate for this method.");
			//add a fake transition
			newTransition(uniqueInitialState, uniqueEndState, EPSILON);
		}
	}
	
    /**
	 * Adds all states reachable via edges to the node set. 
	 * Also eliminates epsilon transitions (except the ones we need to
	 * maintain a unique starting state and end state) and unreachable states,
	 * then renumbers the states.
	 * One property of the resulting automaton is that {@link #isConsistent()} returns true
	 * afterwards.
	 */
	public void cleanup() {
		
		//restore "edges" and "nodes"
		nodes.add(uniqueInitialState);
		nodes.add(uniqueEndState);		
		edges.clear();
		Stack worklist = new Stack();
		for (Iterator nodeIter = nodes.iterator(); nodeIter.hasNext();) {
			SMNode n = (SMNode) nodeIter.next();
			worklist.push(n);
		}
		
		while(!worklist.isEmpty()) {
			SMNode n = (SMNode) worklist.pop();
			nodes.add(n);
			
			for (Iterator inIter = n.getInEdgeIterator(); inIter.hasNext();) {
				SMEdge edge = (SMEdge) inIter.next();
				if(edges.add(edge)) {
					worklist.push(edge.getSource());
					worklist.push(edge.getTarget());
				}				
			}
			for (Iterator outIter = n.getOutEdgeIterator(); outIter.hasNext();) {
				SMEdge edge = (SMEdge) outIter.next();
				if(edges.add(edge)) {
					worklist.push(edge.getSource());
					worklist.push(edge.getTarget());
				}				
			}
		}
		////////////////////
		
		//reset final states and initial states
		for (Iterator nodeIter = nodes.iterator(); nodeIter.hasNext();) {
			SMNode n = (SMNode) nodeIter.next();
			n.setInitial(n==getInitialState());
			n.setFinal(n==getEndState());
		}
		
		eliminateEpsilonTransitions();
		compressStates();
		uniqueStartEndStates();
		renumberStates();		
		
		assert isConsistent();		
	}
	
	/** 
     * Generates unique start/end states. 
     */
    protected void uniqueStartEndStates() {

    	//add fresh start node
    	uniqueInitialState = newState();
    	uniqueInitialState.setInitial(true);
    	
    	//add fresh end node
    	uniqueEndState = newState();
    	uniqueEndState.setFinal(true);    	
    	
        //restore again the uniqueness of start/end nodes
        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			State state = (State) iter.next();
			
			if(state.isInitialNode() && state!=uniqueInitialState) {
				newTransition(uniqueInitialState,state,EPSILON);
				state.setInitial(false);
			}
			if(state.isFinalNode() && state!=uniqueEndState) {
				newTransition(state,uniqueEndState,EPSILON);
				state.setFinal(false);
			}
		}

    }
    
	/**
	 * @return the unique starting state
	 */
	public State getInitialState() {
		return uniqueInitialState;
	}
	
	/**
	 * @return the unique end state
	 */
	public State getEndState() {
		return uniqueEndState;
	}

	/**
     * Adds states and transition for all (transitive) successor units for u.
	 * @param s a unit
	 */
	protected void addStatesFor(Stmt s) {
		if(!processedUnits.contains(s)) {
			processedUnits.add(s);
			for (Iterator iter = ug.getSuccsOf(s).iterator(); iter.hasNext();) {
				Stmt succ = (Stmt) iter.next();
				
				newTransition(stateFor(s), stateFor(succ), s, succ);
				
				addStatesFor(succ);			
			}
		}
	}

	/** 
     * Generates a new transition. The label depends on the kind of unit.
     * @param from the starting state
     * @param to the end state
     * @param s the stmt whose execution triggers the transition
	 * @param succ the statement associated with "to"; used for optimization purposes
	 * (epsilon edges an be saved if succ has no other incoming edges)
     */
    public void newTransition(SMNode from, SMNode to, Stmt s, Stmt succ) {

    	assert isConsistent();
    	
		boolean uniquePred = ug.getPredsOf(succ).size()<2;
		//filter outgoing edges
    	Iterator filteredEdgesOutOf = removeInternalAdviceMethods(cg.edgesOutOf(s));
		boolean needInvokeEdge = filteredEdgesOutOf.hasNext();    	
		boolean needMatchingSymbolsEdge = matchingSymbols(s).size()>0;
		
		if(needMatchingSymbolsEdge) {
			if(!needInvokeEdge) {
				newSymbolTransition(from, to, s);
			} else {
				SMNode tmp = (SMNode) newState();
				newSymbolTransition(from, tmp, s);
				from = tmp;
			}
		}
    	
    	assert isConsistent();

    	if(needInvokeEdge) {        	
        	//for invoke statements we generate a special edge,
        	//cause we need to treat them in a special way during interprocedural
        	//analysis
            //in particular, the invoke edge holds a reference to outgoing edges
            SMEdge edge = new InvokeEdge(from, to, filteredEdgesOutOf, s);
            from.addOutgoingEdge(edge);
            to.addIncomingEdge(edge);
            edges.add(edge);
        } 
        
    	assert isConsistent();

    	if(!needMatchingSymbolsEdge && !needInvokeEdge) {
        	if(uniquePred && !to.getInEdgeIterator().hasNext() && !to.getOutEdgeIterator().hasNext()) {
	        	unitToState.put(succ,from);
	        	nodes.remove(to);
	        } else {
	        	newTransition(from, to, EPSILON);
	        }
        }
    	
    	assert isConsistent();        
    }

	/**
	 * Filters {@link Edge}s iterated by edgesOutOf so that only those edges
	 * are retained that point to methods which can actually be woven into.
	 * @param edgesOutOf any iterator over {@link Edge}s
	 * @return the subset of edges that point to methods for which
	 * {@link MethodCategory#weaveInside(SootMethod)} returns true.
	 * 
	 */
	protected Iterator removeInternalAdviceMethods(Iterator edgesOutOf) {
		List filtered = new ArrayList();
		while(edgesOutOf.hasNext()) {
			Edge edge = (Edge) edgesOutOf.next();
			SootMethod target = edge.getTgt().method();
			assert target!=null;
			boolean canWeaveInto = MethodCategory.weaveInside(target);
			if(canWeaveInto) {
				filtered.add(edge);
			}
		}
		return filtered.iterator();
	}

	/**
	 * Adds a new edge from "from" to "to" for each symbol that matches h. 
	 * If there is not match at all, an epsilon transition is added instead.
	 * @param from source state
	 * @param to end state
	 * @param h any host, possibly tagged with a {@link MatchingTMSymbolTag}
	 */
	protected void newSymbolTransition(State from, State to, Host h) {
		//Currently we only support the host types Unit and SootMethod
		//(c.f. abc.tm.weaving.aspectinfo.TMOptPerSymbolAdviceDecl)				
		assert h instanceof Unit || h instanceof SootMethod;
		
		boolean addedEdge = false;
		
		if(TaggedHosts.v().hasTag(h)) {
			MatchingTMSymbolTag tag = TaggedHosts.v().getTag(h);
			Set allShadowsForTag = Shadow.allShadowsForTag(tag, ug.getBody().getMethod());
			
			Iterator iter = allShadowsForTag.iterator();
	    	if(iter.hasNext()) {
	    		//if there are any, create an edge labeled with each symbol
	        	while(iter.hasNext()) {
					Shadow shadow = (Shadow) iter.next();
					if(shadow.hasEmptyMapping()) {
						//if the shadow points to an empty points-to set
			            super.newTransition(from, to, EPSILON);
					} else {
						callback.registerReachableShadow(shadow.getUniqueShadowId());	            	
						String symbolId = Naming.uniqueSymbolID(Naming.getTracematchName(shadow.getUniqueShadowId()), Naming.getSymbolShortName(shadow.getUniqueShadowId()));
			            SMShadowEdge newEdge = (SMShadowEdge) super.newTransition(from, to, symbolId);
			            //store the id of the shadow in the edge, so that we can refer to it later on		            
			            newEdge.setShadowId(shadowId(h,symbolId));		            
		            	SMVariableEdge variableEdge = (SMVariableEdge) newEdge;
						variableEdge.setShadow(shadow);
					}
				}
	    		addedEdge = true;
	    	}
		}
		
		if(!addedEdge) {
			//else just add an epsilon transition
	        super.newTransition(from, to, EPSILON);
		}
		
	}   
	
//	/**
//	 * Adds free variable mappings to the edge. 
//	 * @param variableEdge the edge to attach the bindings to
//	 * @param symbolId the id of the symbol for which the bindings should be added
//	 * @param h the host which the edge is accociated with
//	 * @throws EmptyVariableMappingException thrown when there is an empty points-to set for one of
//	 * the weaving variables. This means most possibly that it would be null during runtime.  
//	 */
//	protected void addVariableMappings(SMVariableEdge variableEdge, String symbolId, Host h) throws EmptyVariableMappingException {
//		MatchingTMSymbolTag tag = TaggedHosts.v().getTag(h);
//		assert tag.getMatchingSymbolIDs().contains(symbolId);
//		//variableEdge.importVariableMapping(tag.getVariableMappingForSymbol(symbolId), ug.getBody().getMethod());
//		Shadow.allShadowsForTag(tag, ug.getBody().getMethod());
//		variableEdge.setShadow(shadow);
//		
//	}

	/**
     * Returns a list of symbols matching h.
     * The return value is cached.
	 * @param h a host
	 * @return the list of tracematch symbols matching the host h
	 */
	protected Set matchingSymbols(Host h) {
		Set matchingIDs = (Set) hostToMatches.get(h);
		if(matchingIDs==null) {
			if(TaggedHosts.v().hasTag(h)) {
			    MatchingTMSymbolTag tag = TaggedHosts.v().getTag(h);
	            //get the appropriate symbol IDs
	            matchingIDs = tag.getMatchingSymbolIDs();
			} else {
				matchingIDs = Collections.EMPTY_SET;
			}
			hostToMatches.put(h,matchingIDs);
		}
		
		return matchingIDs;
	}
	
	/**
	 * Returns the shadow id attached for this host or <code>-1</code> if there
	 * is not such tag attached.
	 * @param h a host
	 * @param symbolId 
	 * @return the shadow id if attached or <code>-1</code> else
	 */
	protected int shadowId(Host h, String symbolId) {
	    MatchingTMSymbolTag tag = TaggedHosts.v().getTag(h);
        //look for a matching tag
		if(tag!=null) {
            //get the appropriate shadow ID
			assert tag.getShadowId(symbolId)>-1;
            return tag.getShadowId(symbolId);
		}
		return -1;
	}

	/**
     * Returns the unique state for u.
	 * @param u a unit
	 * @return the state representing the program state immediately
     * before executing u
	 */
	protected SMNode stateFor(Unit u) {
		SMNode s = (SMNode) unitToState.get(u);
		
		if(s==null) {
			s = (SMNode) newState();
			unitToState.put(u, s);
		}
		
		return s;
	}	
		
	

	/**
	 * Folds this state machine interprocedurally, i.e. inlines all {@link InvokeEdge}s
	 * by adding outgoing edges to state machines of their call targets and incoming ones back from the end states
	 * of those state machines to the call site.
	 * Folding is performed on a clone, the original state machine is not touched.
	 * @return the folded state machine 
	 */
	public UGStateMachine fold() {
		return fold(ThreadContext.contextOf(ThreadContext.MAIN));
	}
	
	/**
	 * Folds this state machine interprocedurally, i.e. inlines all {@link InvokeEdge}s
	 * by adding outgoing edges to state machines of their call targets and incoming ones back from the end states
	 * of those state machines to the call site.
	 * Folding is performed on a clone, the original state machine is not touched.
	 * @param tc a thread contexts object to accumulate per-thread information during construction;
	 * clients should likely pass in {@link ThreadContext#contextOfMainThread()} here
	 * @return the folded state machine 
	 */
	protected UGStateMachine fold(ThreadContext tc) {
		StateMachineFoldingCache cache = StateMachineFoldingCache.v();
		UGStateMachine cachedFoldedInstance = cache.getCachedFoldedInstance(this);
		if(cachedFoldedInstance == null) {	
			try {
				//create a copy and cache it
				cachedFoldedInstance = (UGStateMachine) clone();
				//make sure that cachedFoldedInstance is consistent
	            assert cachedFoldedInstance.isConsistent();
	            
	            //register the folded copy
	            cache.registerCachedFoldedInstance(this, cachedFoldedInstance);	            
	            
				//fold this copy
				cachedFoldedInstance.foldThis(cache,cg,tc);
	            //here, cachedFoldedInstance is not necessarily in a "consistent state";
				//it may hold edges to/from "caller" graphs
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException("A problem occurred when trying to clone" +
						" a UGStateMachine.",e);
			}
		} else {
			shareCount++;
		}
		//and return
		return cachedFoldedInstance;
	}

	/**
	 * Folds this state machine, i.e. inlines invoke edges.
	 * @param cache the cache over which other state machines can be accessed
	 * @param cg a (possibly abstracted) call graph
	 * @param tc a thread contexts object to accumulate per-thread information during construction
	 */
	protected void foldThis(StateMachineFoldingCache cache, CallGraph cg, ThreadContext tc) {
		Collection edgesCopy = new ArrayList(edges);
		
		//over all transitions
		for (Iterator transIter = edgesCopy.iterator(); transIter.hasNext();) {
			SMEdge edge = (SMEdge) transIter.next();
			
			//if we have an invoke edge, "inline" it
			if(edge instanceof InvokeEdge) {
				InvokeEdge invokeEdge = (InvokeEdge) edge;

				//remove the edge from 'edges' so that recursive calls
				//further down the stack do not see it any more
				edges.remove(invokeEdge);
				invokeEdge.getSource().removeOutEdge(invokeEdge);
				invokeEdge.getTarget().removeInEdge(invokeEdge);			
				
				//get all outgoing call edges
				Iterator callEdgeIter = invokeEdge.edgesOutOf();
				
				if(callEdgeIter.hasNext()) {
					//if we have any outgoing call edges, add for each such edge
					//epsilon transitions to the state machine of the call target
				
					//for each call graph edge going out from the invoke statement
					while(callEdgeIter.hasNext()) {
						Edge outEdge = (Edge) callEdgeIter.next();
												
						MethodOrMethodContext target = outEdge.getTgt();
						
						//each method should by now have an associated state machine
						assert target.method().hasTag(UGStateMachineTag.NAME);
	
						//get the state machine of the target method
						UGStateMachineTag smTag =
							(UGStateMachineTag) target.method().getTag(UGStateMachineTag.NAME);
						UGStateMachine targetStateMachine = smTag.getStateMachine();
			
						ThreadContext childContext = outEdge.kind().equals(Kind.THREAD)
							? ThreadContext.contextOf(outEdge) : tc;
						
						//recurse for this state machine
						targetStateMachine = targetStateMachine.fold(childContext);
						SMNode targetState = (SMNode) targetStateMachine.getInitialState();
						SMNode sourceState = (SMNode) targetStateMachine.getEndState();

						SMEdge newOutEdge, newInEdge;
						//if we have an implicit call edge from Thread.start() to Thread.run()
//						if(outEdge.kind().equals(Kind.THREAD)) {
//
//							newOutEdge = new SMThreadCallEdge(invokeEdge.getSource(),targetState,outEdge,SMThreadCallEdge.OUTGOING);
//							newInEdge = new SMThreadCallEdge(sourceState,invokeEdge.getTarget(),outEdge,SMThreadCallEdge.RETURNING);
//							
//						} else {
//						
							newOutEdge = new SMEdge(invokeEdge.getSource(),targetState,EPSILON);
							newInEdge = new SMEdge(sourceState,invokeEdge.getTarget(),EPSILON);
//						}

						//add a transition from the start node of the
						//invoke transition to the start node of the target state machine
						invokeEdge.getSource().addOutgoingEdge(newOutEdge);
						targetState.addIncomingEdge(newOutEdge);
						
						
						//add a transition from the end node of the
						//target state machine to the end node of the invoke statement
						sourceState.addOutgoingEdge(newInEdge);
						invokeEdge.getTarget().addIncomingEdge(newInEdge);
						
						//copy over all nodes from the target state machine;
						//cannot directly copy into nodes/edges lists cause we are
						//iterating over those (would give ConcurrentModificationException)
						nodes.addAll(targetStateMachine.nodes);

					}
					
				} else {
					
					//if we have no outgoing call edges then retain the original edge
					//but make it an epsilon transition

					//create a new epsilon transition with the same source/target
					SMEdge epsilonEdge = new SMEdge(invokeEdge.getSource(), invokeEdge.getTarget(), EPSILON);
					//make sure to be consistent
					epsilonEdge.getSource().addOutgoingEdge(epsilonEdge);
					epsilonEdge.getTarget().addIncomingEdge(epsilonEdge);

				}

			} 
			//notify the thread context that an edge was processed in its thread
			tc.notifyEdge(edge);
			
			//and copy the nodes
			nodes.add(edge.getSource());
			nodes.add(edge.getTarget());
		}
		
	}
	
//	/**
//	 * When folding a state machine, the control flow of threads is separated by {@link SMThreadCallEdge}s.
//	 * This method transforms this state machine so that all such control flow is encapsulaed in a {@link ThreadSummary}.
//	 * @param tc a thread context to access thread information
//	 * @return edges not affecting the thread the context belongs to 
//	 */
//	public Set buildThreadSummaries(ThreadContext tc) {
//		
//		Set otherThreadEdges = new IdentityHashSet();
//		
//		//for every call graph edge that startes a thread (i.e. edges from start() to run())
//		for (Iterator threadIter = SMThreadCallEdge.allThreadStartEdges().iterator(); threadIter.hasNext();) {
//			Edge threadStartEdge = (Edge) threadIter.next();
//			
//			//get the corresponding thread call edges 
//			Set outgoing = outGoingCallEdgesFor(threadStartEdge);
//			//and the return edges
//			Set returning = returningCallEdgesFor(threadStartEdge);
//			
//			//push all target states of the outgoing edges on the worklist 
//			Stack worklist = new Stack();
//			for (Iterator outIter = outgoing.iterator(); outIter.hasNext();) {
//				SMEdge outEdge = (SMEdge) outIter.next();
//				worklist.push(outEdge.getTarget());
//			}
//			
//			//add every edge to summaryInfo, that is reachable from a state in the worklist,
//			//until seeing an edge in "returning"
//			Set processed = new HashSet();
//			Set summaryInfo = new HashSet(); 
//			while(!worklist.isEmpty()) {
//				SMNode n = (SMNode) worklist.pop();
//				processed.add(n);
//
//				Iterator outIter = n.getOutEdgeIterator();
//				while(outIter.hasNext()) {
//					SMEdge outEdge = (SMEdge) outIter.next();
//
//					if(outEdge instanceof SMThreadSpawnEdge) {
//						SMThreadSpawnEdge threadSpawnEdge = (SMThreadSpawnEdge) outEdge;
//						summaryInfo.addAll(threadSpawnEdge.getThreadSummary().edgesMaybeTriggeredByThread());
//					} else if(!returning.contains(outEdge)) {
//						
//						if(!(outEdge instanceof SMThreadCallEdge) && outEdge.getLabel()!=EPSILON) {
//							if(hasOverlap(outEdge, tc)) {
//								summaryInfo.add(outEdge);
//							} else {
//								otherThreadEdges.add(outEdge);
//							}
//						}
//						
//						SMNode next = outEdge.getTarget();
//						if(!processed.contains(next)) {
//							worklist.push(next);
//						}
//					}
//				}
//			}
//			
//			//create the thread summary
//			ThreadSummary threadSummary = new ThreadSummary(summaryInfo);
//
//			//find all source states of outgoing thread edges
//			Set from = new HashSet();
//			for (Iterator outIter = outgoing.iterator(); outIter.hasNext();) {
//				SMEdge outEdge = (SMEdge) outIter.next();
//				from.add(outEdge.getSource());
//			}
//			//and all target states of returning ones
//			Set to = new HashSet();
//			for (Iterator retIter = returning.iterator(); retIter.hasNext();) {
//				SMEdge retEdge = (SMEdge) retIter.next();
//				to.add(retEdge.getTarget());
//			}
//			
//			//create a thread spawn edge holding the summary between all states in from
//			//and all states in to
//			for (Iterator fromIter = from.iterator(); fromIter.hasNext();) {
//				SMNode fromNode = (SMNode) fromIter.next();
//				for (Iterator toIter = to.iterator(); toIter.hasNext();) {
//					SMNode toNode = (SMNode) toIter.next();
//
//					SMEdge summaryEdge = new SMThreadSpawnEdge(fromNode,toNode,threadSummary);
//					newTransitionFromClone(fromNode, toNode, summaryEdge.getLabel(), summaryEdge);
//				}
//			}
//			
//			//remove all thread 
//			removeEdges(outgoing.iterator());
//			removeEdges(returning.iterator());
//
//			//compress states; this will eliminate the entire control flow of this thread from the graph
//			compressStates();
//		}
//		
//		return otherThreadEdges;
//	}

//	/**
//	 * @param edge
//	 * @param tc 
//	 * @return 
//	 */
//	private boolean hasOverlap(SMEdge edge, ThreadContext tc) {
//		assert edge instanceof SMVariableEdge;
//		
//		Map threadSummaryMapping = tc.getThreadSummaryMapping();
//		
//		SMVariableEdge varEdge = (SMVariableEdge) edge;
//		for (Iterator varIter = varEdge.getBoundVariables().iterator(); varIter.hasNext();) {
//			String var = (String) varIter.next();
//			PointsToSet pts = varEdge.getPointsToSet(var);
//			PointsToSet threadPts = (PointsToSet) threadSummaryMapping.get(var);
//			
//			PointsToSet intersection;
//			if(threadPts==null) {
//				intersection = pts;
//			} else {
//				intersection = Intersection.intersect(pts, threadPts);
//			}
//			
//			if(intersection.isEmpty()) {
//				return false;
//			}
//		}
//		
//		return true;
//	}
	
	/**
	 * Returns all outgoing thread call edges ever created for the given thread start edge.
	 * @param threadStartEdge a call graph edge from start() to run()
	 * @return the set of corresponding outgoing {@link SMThreadCallEdge}s ever created 
	 * @see SMThreadCallEdge#SMThreadCallEdge(SMNode, SMNode, Edge, boolean)
	 */
	protected Set outGoingCallEdgesFor(Edge threadStartEdge) {
		Set result = new HashSet();
		for (Iterator iter = getEdgeIterator(); iter.hasNext();) {
			SMEdge edge = (SMEdge) iter.next();
			if(edge instanceof SMThreadCallEdge) {
				SMThreadCallEdge threadCallEdge = (SMThreadCallEdge) edge;
				if(threadCallEdge.isOutgoing() && threadCallEdge.getThreadStartEdge()==threadStartEdge) {
					result.add(threadCallEdge);
				}
			}
		}
		return result;
	}
	
//	/**
//	 * Returns all returning thread call edges ever created for the given thread start edge.
//	 * @param threadStartEdge a call graph edge from start() to run()
//	 * @return the set of corresponding returning {@link SMThreadCallEdge}s ever created 
//	 * @see SMThreadCallEdge#SMThreadCallEdge(SMNode, SMNode, Edge, boolean)
//	 */
//	private Set returningCallEdgesFor(Edge threadStartEdge) {
//		Set result = new HashSet();
//		for (Iterator iter = getEdgeIterator(); iter.hasNext();) {
//			SMEdge edge = (SMEdge) iter.next();
//			if(edge instanceof SMThreadCallEdge) {
//				SMThreadCallEdge threadCallEdge = (SMThreadCallEdge) edge;
//				if(!threadCallEdge.isOutgoing() && threadCallEdge.getThreadStartEdge()==threadStartEdge) {
//					result.add(threadCallEdge);
//				}
//			}
//		}
//		return result;
//	}

	/** 
	 * {@inheritDoc}
	 */
	protected Object clone() throws CloneNotSupportedException {		
		UGStateMachine clone = (UGStateMachine) super.clone();
		
		clone.edges = new IdentityHashSet();
		clone.nodes = new LinkedHashSet();
		clone.stateToClone = new IdentityHashMap();

		clone.uniqueInitialState = clone.stateFor(uniqueInitialState);
		clone.uniqueInitialState.setInitial(true);
		clone.uniqueEndState = clone.stateFor(uniqueEndState);
		clone.uniqueEndState.setFinal(true);
		
		for (Iterator nodeIter = nodes.iterator(); nodeIter.hasNext();) {
			SMNode s = (SMNode) nodeIter.next();
			
			for (Iterator outIter = s.getOutEdgeIterator(); outIter.hasNext();) {
				SMEdge edge = (SMEdge) outIter.next();
		
				State from = clone.stateFor(edge.getSource());				
				State to = clone.stateFor(edge.getTarget());
				
				clone.newTransitionFromClone(from, to, edge.getLabel(), edge);
			}
		}
		
		clone.stateToClone = null;
		
		assert clone.isConsistent();
		
		return clone;
	}
	
	/**
	 * Returns the cloned state for orig and creates a new one if necessary. 
	 * @param orig any state of this state machine
	 * @return the cloned version of orig
	 */
	protected State stateFor(State orig) {
		State s = (State) stateToClone.get(orig);
		
		if(s==null) {
			s = newState();
			stateToClone.put(orig, s);
		}
		
		return s;
	}	

    
	/**
	 * Special edge in the automaton which reflects an invoke expression.
	 * We have to retain those for the interprocedural analysis.
	 * @author Eric Bodden
	 */
	protected class InvokeEdge extends SMEdge {

		private final Iterator edgesOutOf;
		private final SootMethod method;
		private final Stmt stmt; 
        
        /**
         * Creates an invoke edge for an invocation through native code.
         * @param from
         * @param to
         */
        public InvokeEdge(SMNode from, SMNode to, Iterator edgesOutOf, SootMethod m) {
            this(from, to, edgesOutOf, m, null);
            assert !m.hasActiveBody();
        }
        
        /**
         * @param s the associated invoke statement
         * @see SMEdge#SMEdge(SMNode, SMNode, String)
         */
        public InvokeEdge(SMNode from, SMNode to, Iterator edgesOutOf, Stmt s) {            
            this(from, to, edgesOutOf, null, s);
        }
        
        private InvokeEdge(SMNode from, SMNode to, Iterator edgesOutOf, SootMethod m, Stmt s) {
        	super(from, to, (s==null)?"native":s.toString());
			this.edgesOutOf = edgesOutOf;
			this.method = m;
			this.stmt = s;
        }

		/**
		 * @return the associated invoke statement
		 */
		public Iterator edgesOutOf() {
			return edgesOutOf;
		}

		/**
		 * {@inheritDoc}
		 */
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((method == null) ? 0 : method.hashCode());
			result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			final InvokeEdge other = (InvokeEdge) obj;
			if (method == null) {
				if (other.method != null)
					return false;
			} else if (!method.equals(other.method))
				return false;
			if (stmt == null) {
				if (other.stmt != null)
					return false;
			} else if (!stmt.equals(other.stmt))
				return false;
			return true;
		}

    }

	/**
	 * {@inheritDoc}
	 */
	public boolean isConsistent() {
		if(!super.isConsistent()) return false;
		
		if(!nodes.contains(uniqueInitialState)) {
			return false;
		}
		if(!nodes.contains(uniqueEndState)) {
			return false;
		}
		
		if(!uniqueInitialState.isInitialNode()) {
			return false;
		}
		if(!uniqueEndState.isFinalNode()) {
			return false;
		}
		
		return true;
	}

	/**
	 * @return 
	 * 
	 */
	public Set removeEdgesWithSharedVariables() {
		Collection edgesCopy = new ArrayList(edges);
		
		Set result = new HashSet();
		
		for (Iterator edgeIter = edgesCopy.iterator(); edgeIter.hasNext();) {
			SMEdge edge = (SMEdge) edgeIter.next();
			if(edge instanceof SMVariableEdge && edge.getLabel()!=EPSILON) {
				SMVariableEdge variableEdge = (SMVariableEdge) edge;
				if(ThreadContext.pointsToSharedObject(variableEdge.getVariableMapping())) {
					replaceEdge(edge, new SMEdge(edge.getSource(),edge.getTarget(),EPSILON));
					result.add(edge);
				}
			}
		}
		
		return result;
	}

//	public static void statistics() {
//		try {
//			if(Debug.v().debugTmAnalysis) {
//				System.err.println("================================================================");
//				System.err.println("================================================================");
//				System.err.println("Statistic for UGStateMachines follows:");
//				System.err.println("================================================================");
//			}
//			int max = 0;
//			UGStateMachine maxSm = null;
//			//find maximal size
//			for (Iterator smIter = allStateMachines.iterator(); smIter.hasNext();) {
//				UGStateMachine sm = (UGStateMachine) smIter.next();
//				if(sm.size()>max) {
//					max = sm.size();
//					maxSm = sm;
//				}
//			}		
//			//aggregate the number of state machines for each size
//			int[] sizes = new int[max+1];
//			for (Iterator smIter = allStateMachines.iterator(); smIter.hasNext();) {
//				UGStateMachine sm = (UGStateMachine) smIter.next();
//				sizes[sm.size()]++;
//			}
//	
//			if(Debug.v().debugTmAnalysis) {
//				System.err.println("Size      Count");
//				DecimalFormat formatter = new DecimalFormat("000000");
//				for (int size = 0; size < sizes.length; size++) {
//					int count = sizes[size];
//					System.err.print(formatter.format(size) + "  "+formatter.format(count)+ "  ");
//					for(int i=0; i<count; i++) {
//						System.err.print("*");
//					}
//					System.err.println();
//				}
//			}
//			
//			double average=0;
//			for (int size = 0; size < sizes.length; size++) {
//				int count = sizes[size];
//				average += size*count;
//			}
//			average /= allStateMachines.size();
//	
//	
//			float sharingRate = ((float)shareCount)/allStateMachines.size();
//	
//			if(Debug.v().debugTmAnalysis) {
//				System.err.println("\nTotal number: "+allStateMachines.size());
//				System.err.println("Average size: "+average);
//				System.err.println("Average share rate: "+sharingRate);		
//				System.err.println("================================================================");
//			}
//			
//			Bench.U1_UG_COUNT = allStateMachines.size();
//			Bench.U2_UG_SIZE_AVG = average;
//			Bench.U3_UG_SIZE_MAX = max;
//			Bench.U4_SHARING_COUNT = shareCount;
//			Bench.U5_LARGEST_SM = maxSm.ug.getBody().getMethod().toString();
//		} catch(RuntimeException e) {
//			e.printStackTrace();
//		}
//	}	
}