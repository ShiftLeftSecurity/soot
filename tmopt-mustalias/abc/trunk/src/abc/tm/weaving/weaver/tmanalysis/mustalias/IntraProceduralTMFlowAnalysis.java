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
package abc.tm.weaving.weaver.tmanalysis.mustalias;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.ds.Configuration;
import abc.tm.weaving.weaver.tmanalysis.ds.ConfigurationBox;
import abc.tm.weaving.weaver.tmanalysis.ds.Disjunct;
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
	 * The state machine to interpret.
	 */
	protected final TMStateMachine stateMachine;	
	
	protected final TraceMatch tracematch;	
	
	protected final Set activeShadows;
	
	protected final Configuration initialConfiguration;

	protected boolean analysisFinished;

	protected final UnitGraph ug;

	/**
	 * Creates and performs a new flow analysis.
	 * @param tm a tracematch
	 * @param programGraph the program graph to iterate over 
	 * @param stateMachine the tracematch statemachine to use
	 * @param fullIteration if <code>true</code>, we do a full, unoptimized iteration
	 * @param defaultOrder if <code>true</code>, we use the (inappropriate) default iteration order
	 */
	public IntraProceduralTMFlowAnalysis(TraceMatch tm, UnitGraph ug, Disjunct prototype) {
		super(ug);
		
		Disjunct.PROTOTYPE = prototype;
		
		this.ug = ug;

		//since we are iterating over state machine edges, which implement equals(..)
		//we need to separate those edges by using identity hash maps
		this.filterUnitToAfterFlow = new IdentityHashMap();
		this.filterUnitToBeforeFlow = new IdentityHashMap();
		this.unitToAfterFlow = new IdentityHashMap();
		this.unitToBeforeFlow = new IdentityHashMap();
		
		this.stateMachine = (TMStateMachine) tm.getStateMachine();
		this.tracematch = tm;
		this.activeShadows = new HashSet();		

		this.initialConfiguration = new Configuration(this);
		
		//do the analysis
		this.analysisFinished = false;
		doAnalysis();
		this.analysisFinished = true;
		
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
				
		if(!stmt.hasTag(SymbolShadowTag.NAME)) {
			cout.set(cin.get());
			return;
		}

		SymbolShadowTag tag = (SymbolShadowTag) stmt.getTag(SymbolShadowTag.NAME);		
		Configuration inConfig = cin.get();
		Configuration join = new Configuration(this); 
		for (SymbolShadow shadow : tag.getMatchesForTracematch(tracematch)) {
			if(shadow.isEnabled()) {				
				Configuration newConfig = inConfig.doTransition(shadow);
				join = join.getJoinWith(newConfig);
			}
		}		
		cout.set(join);
	}

	/**
	 * Registeres shadows as <i>active</i>, i.e. as being used in order
	 * to reach a final state.
	 * @param activeShadows a list of shadow IDs (int) for all active shadows
	 */
	public void registerActiveShadows(Set activeShadows) {
		if(analysisFinished) {
			throw new RuntimeException("This is only to be used from within the analysis.");			
		}
		this.activeShadows.addAll(activeShadows);
	}

	/**
	 * Returns the set of <i>active</i> shadows, i.e. shadows being used in order
	 * to reach a final state.
	 * @return a list of shadow IDs (int) for all active shadows
	 */
	public Set<String> getActiveShadows() {
		return Collections.unmodifiableSet(activeShadows);
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
		
		if(cin1.isEmpty()) {
			copy(cin2,cout);
		} else if(cin2.isEmpty()) {
			copy(cin1,cout);
		} else {
			Configuration c1 = cin1.get();
			Configuration c2 = cin2.get();
			assert c1.getStates().equals(c2.getStates());
			
			cout.set(c1.getJoinWith(c2));
			
			ConfigurationBox copyin2 = new ConfigurationBox();
			copy(cin2,copyin2);
			ConfigurationBox copyout = new ConfigurationBox();
			copy(cout,copyout);
		}
		
	}
	
	/** 
	 * {@inheritDoc}
	 */
	protected Object newInitialFlow() {
		return new ConfigurationBox();
	}

	/**
	 * @return the stateMachine
	 */
	public TMStateMachine getStateMachine() {
		return stateMachine;
	}

}
