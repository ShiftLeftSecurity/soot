/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
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
package abc.tm.weaving.weaver.tmanalysis.mustalias;

import static abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status.ABORTED_CALLS_OTHER_METHOD_WITH_SHADOWS;
import static abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status.FINISHED;
import static abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status.FINISHED_HIT_FINAL;
import static abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status.RUNNING;
import static abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status.RUNNING_HIT_FINAL;

import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import soot.MethodOrMethodContext;
import soot.Body;
import soot.Unit;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.ds.Configuration;
import abc.tm.weaving.weaver.tmanalysis.ds.ConfigurationBox;
import abc.tm.weaving.weaver.tmanalysis.ds.Constraint;
import abc.tm.weaving.weaver.tmanalysis.ds.Disjunct;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;
import abc.tm.weaving.weaver.tmanalysis.stages.CallGraphAbstraction;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowTag;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolShadow;

public class IntraProceduralTMFlowAnalysis extends ForwardFlowAnalysis implements TMFlowAnalysis {

	/**
	 * Status
	 *
	 * @author Eric Bodden
	 */
	public enum Status {
		RUNNING{
			public boolean isAborted() { return false; }
			public boolean isFinishedSuccessfully() { return false; }
            public boolean hitFinal() { return false; }
			public String toString() { return "running"; }
		},
        RUNNING_HIT_FINAL{
            public boolean isAborted() { return false; }
            public boolean isFinishedSuccessfully() { return false; }
            public boolean hitFinal() { return true; }
            public String toString() { return "running, hit final state"; }
        },
		ABORTED_CALLS_OTHER_METHOD_WITH_SHADOWS {
			public boolean isAborted() { return true; }
			public boolean isFinishedSuccessfully() { return false; }
            public boolean hitFinal() { return false; }
			public String toString() { return "aborted (calls other method with shadows)"; }
		},
		FINISHED {
			public boolean isAborted() { return false; }
			public boolean isFinishedSuccessfully() { return true; }
            public boolean hitFinal() { return false; }
			public String toString() { return "finished"; }
		},
        FINISHED_HIT_FINAL {
            public boolean isAborted() { return false; }
            public boolean isFinishedSuccessfully() { return true; }
            public boolean hitFinal() { return true; }
            public String toString() { return "finished, hit final state"; }
        };
		public abstract boolean isAborted(); 
		public abstract boolean isFinishedSuccessfully();
        public abstract boolean hitFinal();
	}

	/**
	 * The state machine to interpret.
	 */
	protected final TMStateMachine stateMachine;	
	
	protected final TraceMatch tracematch;	
	
	protected final UnitGraph ug;

	protected final Set<Stmt> visited;
	
	protected final Map<Stmt,Configuration> shadowStmtToFirstAfterFlow;

	protected final CallGraph abstractedCallGraph;
	
	protected final State additionalInitialState;

	protected final Collection<Stmt> projection;

	protected final Collection<String> overlappingShadowIDs;

	protected Status status;


	/**
	 * Creates and performs a new flow analysis.
	 */
	public IntraProceduralTMFlowAnalysis(TraceMatch tm, TMStateMachine stateMachine, UnitGraph ug, Disjunct prototype, State additionalInitialState, Collection<Stmt> projection) {
		super(ug);
		this.projection = new HashSet(projection);
		
		//initialize prototypes
		Constraint.initialize(prototype);
		
		this.ug = ug;
		this.additionalInitialState = additionalInitialState;

		//since we are iterating over state machine edges, which implement equals(..)
		//we need to separate those edges by using identity hash maps
		this.filterUnitToAfterFlow = new IdentityHashMap();
		this.filterUnitToBeforeFlow = new IdentityHashMap();
		this.unitToAfterFlow = new IdentityHashMap();
		this.unitToBeforeFlow = new IdentityHashMap();
		
		this.stateMachine = stateMachine;
		this.tracematch = tm;

		this.visited = new HashSet<Stmt>();
		this.shadowStmtToFirstAfterFlow = new HashMap<Stmt,Configuration>();
		
		this.abstractedCallGraph = CallGraphAbstraction.v().abstractedCallGraph();

		//see which shadow groups are present in the code we look at
		Set<SymbolShadow> allShadows = new HashSet<SymbolShadow>();
		for (Stmt stmt : projection) {
            if(stmt.hasTag(SymbolShadowTag.NAME)) {
            	SymbolShadowTag tag = (SymbolShadowTag) stmt.getTag(SymbolShadowTag.NAME);
            	for (SymbolShadow match : tag.getAllMatches()) {
					if(match.isEnabled()) {
						allShadows.add(match);
					}
				}
            }
		}
		Set<ShadowGroup> allShadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
		Set<ShadowGroup> shadowGroups = new HashSet<ShadowGroup>();
		for (ShadowGroup shadowGroup : allShadowGroups) {
			for (Shadow shadowInGroup : shadowGroup.getAllShadows()) {
				for (SymbolShadow shadowHere : allShadows) {
					if(shadowInGroup.getUniqueShadowId().equals(shadowHere.getUniqueShadowId())) {
						shadowGroups.add(shadowGroup);
					}
				}
			}
		}
        //store all IDs of shadows in those groups
        this.overlappingShadowIDs= new HashSet<String>();
        for (ShadowGroup shadowGroup : shadowGroups) {
            for (Shadow shadow : shadowGroup.getAllShadows()) {
                this.overlappingShadowIDs.add(shadow.getUniqueShadowId());
            }
        }		
		
		//do the analysis
		this.status = RUNNING;
		doAnalysis();
		if(!this.status.isAborted()) {
            if(this.status.hitFinal()) {
                this.status = FINISHED_HIT_FINAL;
            } else {
                this.status = FINISHED;
            }
        }
		
		//clear caches
		Disjunct.reset();
		Constraint.reset();
		Configuration.reset();
		ShadowSideEffectsAnalysis.reset();
	}

	protected void flowThrough(Object in, Object d, Object out) {
		ConfigurationBox cin = (ConfigurationBox) in;		
		Stmt stmt = (Stmt) d;
		ConfigurationBox cout = (ConfigurationBox) out;		

		//check for side-effects
		if(mightHaveSideEffects(stmt)) {
			status = ABORTED_CALLS_OTHER_METHOD_WITH_SHADOWS;
		}
		//abort if we have side-effects
		if(status.isAborted()) return;
				
		//if we are to ignore this statement
		if(!projection.contains(stmt)) {
			copy(cin,cout);
			return;
		}

		//if there are no shadows at all at this statement, just copy over and return
		if(!stmt.hasTag(SymbolShadowTag.NAME)) {
			copy(cin,cout);
			return;
		}
		
		//retrive matches fot the current tracematch
		SymbolShadowTag tag = (SymbolShadowTag) stmt.getTag(SymbolShadowTag.NAME);		
		Configuration inConfig = cin.get();
		Set<SymbolShadow> matchesForThisTracematch = tag.getMatchesForTracematch(tracematch);

		//"join" is initialized to the initial configuration (the neutral element of the join operation)
		Configuration join = new Configuration(this,stateMachine);		
		//for each match, if it is still active, compute the successor and join 
		boolean foundEnabledShadow = false;
		for (SymbolShadow shadow : matchesForThisTracematch) {
			if(shadow.isEnabled()) {				
				foundEnabledShadow = true;
				Configuration newConfig = inConfig.doTransition(shadow);
				join = join.getJoinWith(newConfig);
			}
		}
		//if we actually computed a join, set it, else copy over 
		if(foundEnabledShadow) {
			cout.set(join);
			//if not yet visited...
			if(!visited.contains(stmt)) {
				visited.add(stmt);
				//...record this after-flow for comparison
				shadowStmtToFirstAfterFlow.put(stmt, join);
			}			
		} else
			copy(cin, cout);
	}
	
	protected boolean mightHaveSideEffects(Stmt s) {
		Collection<SymbolShadow> shadows = transitivelyCalledShadows(s);
		for (SymbolShadow shadow : shadows) {
			if(overlappingShadowIDs.contains(shadow.getUniqueShadowId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the collection of <code>SymbolShadow</code>s triggered in transitive callees from <code>s</code>.
	 * @param s any statement
	 */
	protected Collection<SymbolShadow> transitivelyCalledShadows(Stmt s) {
        HashSet<SymbolShadow> symbols = new HashSet<SymbolShadow>();
        HashSet<SootMethod> calleeMethods = new HashSet<SootMethod>();
        LinkedList<MethodOrMethodContext> methodsToProcess = new LinkedList();

        // Collect initial edges out of given statement in methodsToProcess
        Iterator<Edge> initialEdges = abstractedCallGraph.edgesOutOf(s);
        while (initialEdges.hasNext()) {
            Edge e = initialEdges.next();
            methodsToProcess.add(e.getTgt());
            calleeMethods.add(e.getTgt().method());
        }

        // Collect transitive callees of methodsToProcess
        while (!methodsToProcess.isEmpty()) {
            MethodOrMethodContext mm = methodsToProcess.removeFirst();
            Iterator mIt = abstractedCallGraph.edgesOutOf(mm);

            while (mIt.hasNext()) {
                Edge e = (Edge)mIt.next();
                if (!calleeMethods.contains(e.getTgt().method())) {
                    methodsToProcess.add(e.getTgt());
                    calleeMethods.add(e.getTgt().method());
                }
            }
        }

        // Collect all shadows in calleeMethods
        for (SootMethod method : calleeMethods) {
	        if(method.hasActiveBody()) {
	            Body body = method.getActiveBody();
	            
	            for (Iterator iter = body.getUnits().iterator(); iter.hasNext();) {
	                Unit u = (Unit) iter.next();
	                if(u.hasTag(SymbolShadowTag.NAME)) {
	                	SymbolShadowTag tag = (SymbolShadowTag) u.getTag(SymbolShadowTag.NAME);
	                	for (SymbolShadow match : tag.getAllMatches()) {
							if(match.isEnabled()) {
								symbols.add(match);
							}
						}
	                }
	            }
	        }
        }
        return symbols;
	}

	/**
	 * @return the tracematch
	 */
	public TraceMatch getTracematch() {
		return tracematch;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	protected void copy(Object source, Object dest) {
		ConfigurationBox s = (ConfigurationBox) source;		
		ConfigurationBox d = (ConfigurationBox) dest;
		d.set(s.get());
	}

	/** 
	 * {@inheritDoc}
	 */
	protected Object entryInitialFlow() {
		ConfigurationBox initialFlow = new ConfigurationBox();
		Configuration entryInitialConfiguration = new Configuration(this,stateMachine,additionalInitialState);
		initialFlow.set(entryInitialConfiguration);
		return initialFlow;
	}

	/** 
	 * {@inheritDoc}
	 */
	protected Object newInitialFlow() {
		//intial configuration (neutral element for "join")
		ConfigurationBox initialFlow = new ConfigurationBox();
		Configuration initialConfiguration = new Configuration(this,stateMachine);
		initialFlow.set(initialConfiguration);
		return initialFlow;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	protected void merge(Object in1, Object in2, Object out) {
		ConfigurationBox cin1 = (ConfigurationBox) in1;
		ConfigurationBox cin2 = (ConfigurationBox) in2;
		ConfigurationBox cout = (ConfigurationBox) out;

		assert !cin1.isEmpty() && !cin2.isEmpty();
		
		Configuration c1 = cin1.get();
		Configuration c2 = cin2.get();
		assert c1.getStates().equals(c2.getStates());
		
		cout.set(c1.getJoinWith(c2));
	}
	
	/**
	 * @return the stateMachine
	 */
	public TMStateMachine getStateMachine() {
		return stateMachine;
	}

	/**
	 * Returns all statements for which at least one active shadow exists.
	 */
	public Set<Stmt> statemementsWithActiveShadows() {
		return shadowStmtToFirstAfterFlow.keySet();
	}
	
	public Configuration getFirstAfterFlow(Stmt statementWithShadow) {
		assert shadowStmtToFirstAfterFlow.containsKey(statementWithShadow);
		return shadowStmtToFirstAfterFlow.get(statementWithShadow);
	}
	
	public UnitGraph getUnitGraph() {
		return (UnitGraph) graph;
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}
    
    public void hitFinal() {
        //FIXME REMOVE
//        assert !status.isAborted() && !status.isFinishedSuccessfully();
//        status = RUNNING_HIT_FINAL;
    }

}
