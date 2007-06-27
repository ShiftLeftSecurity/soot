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
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.Util;
import abc.tm.weaving.weaver.tmanalysis.ds.Configuration;
import abc.tm.weaving.weaver.tmanalysis.ds.Constraint;
import abc.tm.weaving.weaver.tmanalysis.ds.Disjunct;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;
import abc.tm.weaving.weaver.tmanalysis.stages.CallGraphAbstraction;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowTag;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolShadow;

public class IntraProceduralTMFlowAnalysis extends ForwardFlowAnalysis<Unit,Set<Configuration>> implements TMFlowAnalysis {

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
	
	protected final Map<Stmt,Set<Configuration>> shadowStmtToFirstAfterFlow;

	protected final CallGraph abstractedCallGraph;
	
	protected final State additionalInitialState;

	protected final Collection<Stmt> projection;

	protected final Collection<String> overlappingShadowIDs;
    
    protected final Set<SymbolShadow> invariantShadows;

	protected Status status;
    
	/**
	 * Creates and performs a new flow analysis.
	 */
	public IntraProceduralTMFlowAnalysis(TraceMatch tm, UnitGraph ug, Disjunct prototype, State additionalInitialState, Collection<Stmt> projection) {
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
		
		this.stateMachine = (TMStateMachine) tm.getStateMachine();
		this.tracematch = tm;

		this.visited = new HashSet<Stmt>();
		this.shadowStmtToFirstAfterFlow = new HashMap<Stmt,Set<Configuration>>();
		
		this.abstractedCallGraph = CallGraphAbstraction.v().abstractedCallGraph();

		//see which shadow groups are present in the code we look at;
        //also initialize invariantStatements to the set of all statements with a shadow
		Set<SymbolShadow> allShadows = Util.getAllActiveShadows(projection);
        
        this.invariantShadows = new HashSet<SymbolShadow>(allShadows);

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

    protected void flowThrough(Set<Configuration> in, Unit u, Set<Configuration> out) {
        Stmt stmt = (Stmt) u;
		//check for side-effects
		if(mightHaveSideEffects(stmt)) {
			status = ABORTED_CALLS_OTHER_METHOD_WITH_SHADOWS;
		}
		//abort if we have side-effects
		if(status.isAborted()) return;
				
		//if we are to ignore this statement
		if(!projection.contains(stmt)) {
			copy(in,out);
			return;
		}

		//if there are no shadows at all at this statement, just copy over and return
		if(!stmt.hasTag(SymbolShadowTag.NAME)) {
			copy(in,out);
			return;
		}
		
		//retrive matches fot the current tracematch
		SymbolShadowTag tag = (SymbolShadowTag) stmt.getTag(SymbolShadowTag.NAME);		
		Set<SymbolShadow> matchesForThisTracematch = tag.getMatchesForTracematch(tracematch);

        out.clear();
        
        boolean foundEnabledShadow = false;
        //for each match, if it is still active, compute the successor and join 
        for (SymbolShadow shadow : matchesForThisTracematch) {
            if(shadow.isEnabled()) {                
                foundEnabledShadow = true;
                for (Configuration oldConfig : in) {
                    Configuration newConfig = oldConfig.doTransition(shadow);
                    if(!newConfig.equals(oldConfig)) {
                        //shadow is not invariant
                        invariantShadows.remove(shadow);
                    }
                    out.add(newConfig);
                }
            }
        }
        if(foundEnabledShadow) {
            //if not yet visited...
            if(!visited.contains(stmt)) {
                visited.add(stmt);
                //...record this after-flow for comparison
                shadowStmtToFirstAfterFlow.put(stmt, out);
            }           
        } else {
            //if we not actually computed a join, copy instead 
            copy(in, out);
        }
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
	protected void copy(Set<Configuration> source, Set<Configuration> dest) {
        dest.clear();
        dest.addAll(source);
	}

	/** 
	 * {@inheritDoc}
	 */
	protected Set<Configuration> entryInitialFlow() {
        Set<Configuration> configs = new HashSet<Configuration>();
		Configuration entryInitialConfiguration = new Configuration(this,additionalInitialState);
		configs.add(entryInitialConfiguration);
		return configs;
	}

	/** 
	 * {@inheritDoc}
	 */
	protected Set<Configuration> newInitialFlow() {
		return new HashSet<Configuration>();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	protected void merge(Set<Configuration> in1, Set<Configuration> in2, Set<Configuration> out) {
        out.clear();
        out.addAll(in1);
        out.addAll(in2);
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
	
	public Set<Configuration> getFirstAfterFlow(Stmt statementWithShadow) {
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
        assert !status.isAborted() && !status.isFinishedSuccessfully();
        status = RUNNING_HIT_FINAL;
    }

    /**
     * @return
     */
    public Collection<SymbolShadow> getInvariantShadows() {
        return new HashSet<SymbolShadow>(invariantShadows); 
    }

}
