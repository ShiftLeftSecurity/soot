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

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.ds.Configuration;
import abc.tm.weaving.weaver.tmanalysis.ds.ConfigurationBox;
import abc.tm.weaving.weaver.tmanalysis.ds.Constraint;
import abc.tm.weaving.weaver.tmanalysis.ds.Disjunct;
import abc.tm.weaving.weaver.tmanalysis.query.PathInfoFinder;
import abc.tm.weaving.weaver.tmanalysis.query.PathInfoFinder.PathInfo;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowTag;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolShadow;

/**
 * This analysis computes a "may control flow" analysis for tracematches.
 * The bit that is important here to understand is the form of the directed graph that we analyze
 * and the analysis information we use. Opposed to standard flow analyses where we have an in-set and
 * an out-set for each <i>node</i> in a directed graph, we want to say something about <i>edges</i> here.
 * Hence, we model edges as nodes and nodes as edges. This is done by the adapter
 * {@link TMStateMachineAsGraph}, which does this conversion on-the-fly.
 * Our analysis information in the fixed point is for each edge the set of possible
 * tracematch transition that <i>could possibly be taken</i> at runtime when triggering the symbol
 * that is associated with this edge.<br>
 * Hence, the out-set of an edge p -s-> q holds all transitions p' -s-> q' for which
 * a transition n' -t-> p' is already in the in-set for some symbol t'.<br>
 * Unused edges can then be accessed via {@link #unusedEdgeIterator()}.
 * TODO additional comments about variables
 * @author Eric Bodden
 */
public class IntraProceduralTMFlowAnalysis extends ForwardFlowAnalysis implements TMFlowAnalysis {

	/**
	 * Determines the configuration used for initialization at the method entry point.
	 *
	 * @author Eric Bodden
	 */
	public enum InitKind {
		/** Minimal assumption: TRUE for any initial state, FALSE else. */
		MINIMAL_ASSUMPTION {
			public Configuration getEntryInitialConfiguration(TMFlowAnalysis analysis) {
				return new Configuration(analysis);
			}
		},
		/** Maximal assumption: TRUE for any non-final state, FALSE for any final state. */
		MAXIMAL_ASSUMPTION{ 
			public Configuration getEntryInitialConfiguration(TMFlowAnalysis analysis) {
				return new Configuration(analysis).getMaximalAssumption();
			}
		};
		public abstract Configuration getEntryInitialConfiguration(TMFlowAnalysis analysis);
	}

	/**
	 * The state machine to interpret.
	 */
	protected final TMStateMachine stateMachine;	
	
	protected final TraceMatch tracematch;	
	
	protected boolean analysisFinished;

	protected final UnitGraph ug;

	protected final InitKind initializationKind;
	
	protected final Set<Stmt> visited;
	
	protected final Map<Stmt,Configuration> shadowStmtToFirstAfterFlow;

	/**
	 * Creates and performs a new flow analysis.
	 * @param tm a tracematch
	 * @param programGraph the program graph to iterate over 
	 * @param stateMachine the tracematch statemachine to use
	 * @param fullIteration if <code>true</code>, we do a full, unoptimized iteration
	 * @param defaultOrder if <code>true</code>, we use the (inappropriate) default iteration order
	 */
	public IntraProceduralTMFlowAnalysis(TraceMatch tm, UnitGraph ug, Disjunct prototype, InitKind initializationKind) {
		super(ug);
		
		//initialize prototypes
		Constraint.initialize(prototype);
		
		this.ug = ug;
		this.initializationKind = initializationKind;

		//since we are iterating over state machine edges, which implement equals(..)
		//we need to separate those edges by using identity hash maps
		this.filterUnitToAfterFlow = new IdentityHashMap();
		this.filterUnitToBeforeFlow = new IdentityHashMap();
		this.unitToAfterFlow = new IdentityHashMap();
		this.unitToBeforeFlow = new IdentityHashMap();
		
		this.stateMachine = (TMStateMachine) tm.getStateMachine();
		this.tracematch = tm;

		this.visited = new HashSet<Stmt>();
		this.shadowStmtToFirstAfterFlow = new HashMap<Stmt,Configuration>();
		
		//do the analysis
		this.analysisFinished = false;
		doAnalysis();
		this.analysisFinished = true;
		
		//clear caches
		Disjunct.reset();
		Constraint.reset();
		Configuration.reset();
		ShadowSideEffectsAnalysis.reset();
	}

	/**
	 * @param beforeFlow
	 * @param s
	 * @param afterFlow
	 */
	protected void flowThrough(Object in, Object d, Object out) {
		ConfigurationBox cin = (ConfigurationBox) in;		
		Stmt stmt = (Stmt) d;
		ConfigurationBox cout = (ConfigurationBox) out;		
				
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
		Configuration join = new Configuration(this);		
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
		Configuration entryInitialConfiguration = initializationKind.getEntryInitialConfiguration(this);
		initialFlow.set(entryInitialConfiguration);
		return initialFlow;
	}

	/** 
	 * {@inheritDoc}
	 */
	protected Object newInitialFlow() {
		//intial configuration (neutral element for "join")
		ConfigurationBox initialFlow = new ConfigurationBox();
		Configuration initialConfiguration = new Configuration(this);
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
	 * @return the initializationKind
	 */
	public InitKind getInitializationKind() {
		return initializationKind;
	}
	
	/**
	 * Determines all statements that are contained in the associated methods
	 * which are annotated with a shadow and are in a loop but for which it is guaranteed that
	 * one loop iteration suffices to reach the fixed point.
	 * @return
	 */
	public Set<Stmt> shadowStatementsReachingFixedPointAtOnce() {
		Set<PathInfo> pathInfos = new PathInfoFinder(tracematch).getPathInfos();
		
		PathsReachingFlowAnalysis prf = new PathsReachingFlowAnalysis(ug);
		
		Set<Stmt> result = new HashSet<Stmt>();
		
		//for each statement with an active shadow
		for (Stmt stmt : shadowStmtToFirstAfterFlow.keySet()) {
			//if contained in a loop
			if(prf.visitedPotentiallyManyTimes(stmt)) {
				//if the first after-flow is equal to the final one
				Configuration firstAfterFlow = shadowStmtToFirstAfterFlow.get(stmt);
				Configuration finalAfterFlow = ((ConfigurationBox) getFlowAfter(stmt)).get();
				assert firstAfterFlow!=null && finalAfterFlow!=null;
				//is the first after-flow equal to the last?
				if(firstAfterFlow.equals(finalAfterFlow)) {
					//still need to check for cases where we have to see a symbol more than once, e.g. a pattern "a a".
					//in this case, the maximal assumption is *too* conservative: it assumes that the first a could
					//already have been seen, which is generally unsound;
					//so if a path info contains a symbol "a" more than once, we bail
					boolean allSymbolsOnlyOnceOnPathInfo = true;
					SymbolShadowTag tag = (SymbolShadowTag) stmt.getTag(SymbolShadowTag.NAME);
					for (SymbolShadow shadow : tag.getAllMatches()) {
						String symbolName = shadow.getSymbolName();
						for (PathInfo pathInfo : pathInfos) {
							if(pathInfo.getDominatingLabels().countOf(symbolName)>1) {
								allSymbolsOnlyOnceOnPathInfo = false;
							}
						}
					}
					if(allSymbolsOnlyOnceOnPathInfo)
						result.add(stmt);
				}
			}
		}		
		return result;
	}

}
