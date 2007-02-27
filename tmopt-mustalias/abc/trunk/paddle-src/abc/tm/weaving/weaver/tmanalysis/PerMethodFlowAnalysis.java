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

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

import soot.Local;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.ds.Configuration;
import abc.tm.weaving.weaver.tmanalysis.ds.ConfigurationBox;
import abc.tm.weaving.weaver.tmanalysis.mustalias.LocalMustAliasAnalysis;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;

/**
 */
public class PerMethodFlowAnalysis extends ForwardFlowAnalysis implements TMFlowAnalysis {

	/**
	 * The state machine to interpret.
	 */
	protected final TMStateMachine stateMachine;	
	
	protected final TraceMatch tracematch;	
	
	protected final Set activeShadows;
	
	protected final Configuration initialConfiguration;

	protected boolean analysisFinished;

	protected final boolean defaultOrder;
	
//	protected Map edgeToBeforeFlowToAfterFlow = new IdentityHashMap();//new MemoryStableMRUCache("transitions",10*1024*1024,true);	

//	protected Map leftToRightToMerge = new HashMap();//new MemoryStableMRUCache("merge",10*1024*1024,false); 

	protected final UnitGraph programGraph;
	
	protected final SootMethod container;
	
	protected final LocalMustAliasAnalysis mustAlias;

	/**
	 * Creates and performs a new flow analysis.
	 * @param tm a tracematch
	 * @param programGraph the program graph to iterate over 
	 * @param stateMachine the tracematch statemachine to use
	 * @param fullIteration if <code>true</code>, we do a full, unoptimized iteration
	 * @param defaultOrder if <code>true</code>, we use the (inappropriate) default iteration order
	 */
	public PerMethodFlowAnalysis(TraceMatch tm, TMStateMachine stateMachine, UnitGraph programGraph, boolean fullIteration, boolean defaultOrder) {
		super(programGraph);
		this.defaultOrder = defaultOrder;

		//since we are iterating over state machine edges, which implement equals(..)
		//we need to separate those edges by using identity hash maps
		this.filterUnitToAfterFlow = new IdentityHashMap();
		this.filterUnitToBeforeFlow = new IdentityHashMap();
		this.unitToAfterFlow = new IdentityHashMap();
		this.unitToBeforeFlow = new IdentityHashMap();
		
		this.stateMachine = stateMachine;
		this.tracematch = tm;
		this.programGraph = programGraph;
		this.container = programGraph.getBody().getMethod();
		this.activeShadows = new HashSet();		

		//at this point really SM should not be empty
		assert !this.stateMachine.isEmpty();
		
		//SM should be consistent
		assert this.stateMachine.isConsistent();
		
		this.initialConfiguration = new Configuration(stateMachine.getStateIterator(),this);
		
		this.mustAlias = new LocalMustAliasAnalysis(programGraph);
		
		//do the analysis
		this.analysisFinished = false;
		doAnalysis();
		this.analysisFinished = true;
		
	}

//	/**
//	 * @param beforeFlow
//	 * @param s
//	 * @param afterFlow
//	 */
//	protected void flowThrough(Object beforeFlow, Object s, Object afterFlow) {
//		Map beforeFlowToAfterFlow = (Map) edgeToBeforeFlowToAfterFlow.get(s);
//		if(beforeFlowToAfterFlow==null) {
//			beforeFlowToAfterFlow = new HashMap();
//			edgeToBeforeFlowToAfterFlow.put(s,beforeFlowToAfterFlow);
//		}
//		ConfigurationBox cachedAfterFlow = (ConfigurationBox) beforeFlowToAfterFlow.get(beforeFlow);
//		if(cachedAfterFlow==null) {
//			cachedAfterFlow = new ConfigurationBox();
//			doFlowThrough(beforeFlow, s, cachedAfterFlow);
//			ConfigurationBox copyBeforeFlow = new ConfigurationBox();
//			copy(beforeFlow,copyBeforeFlow);
//			beforeFlowToAfterFlow.put(copyBeforeFlow, cachedAfterFlow);
////			Bench.FI9_TRANS_CACHE_MISSES++;
//		}
////		else {
////			Bench.FI8_TRANS_CACHE_HITS++;
////		}
//		copy(cachedAfterFlow,afterFlow);
//	}

	
	/** 
	 * Computes the transformation of analysis information when flowing through an edge.
	 * The out-set of an edge p -s-> q holds all transitions p' -s-> q' for which
	 * a transition n' -t-> p' is in the in-set for some symbol t'.
	 * @param in the in-set
	 * @param d the current edge
	 * @param out the out-set
	 */
	protected void flowThrough(Object in, Object d, Object out) {
		ConfigurationBox cin = (ConfigurationBox) in;		
		//SMEdge edge = (SMEdge) d;
		Stmt s = (Stmt) d;
		ConfigurationBox cout = (ConfigurationBox) out;
		Configuration inConfig = cin.get();

		Set shadows = Shadow.allShadowsForHostAndTM(s, container, tracematch);

		Configuration outConfig;
		
		if(shadows.isEmpty()) {
			outConfig = inConfig; //copy
		} else {
			//if one shadow or more, compute successor config
			Iterator shadowIter = shadows.iterator();
			Shadow shadow = (Shadow) shadowIter.next();
			outConfig = inConfig.doTransition(shadow,s);
			//if there are more than one shadows, for each shadow,
			//join the successor config with the one we already have
			while(shadowIter.hasNext()) {
				Shadow nextShadow = (Shadow) shadowIter.next(); 
				Configuration newConfig = inConfig.doTransition(nextShadow,s);
				outConfig = outConfig.getJoinWith(newConfig);
			}
		}
		
		cout.set(outConfig);
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
	public Set getActiveShadows() {
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
//			HashMap rightToMerge = (HashMap) leftToRightToMerge.get(in1);
//			if(rightToMerge==null) {
//				rightToMerge = new HashMap();
//				ConfigurationBox copyin1 = new ConfigurationBox();
//				copy(cin1,copyin1);
//				leftToRightToMerge.put(copyin1, rightToMerge);
//			}
//			Object merge = rightToMerge.get(in2);
//			if(merge==null) {
				Configuration c1 = cin1.get();
				Configuration c2 = cin2.get();
				assert c1.getStates().equals(c2.getStates());
				
				cout.set(c1.getJoinWith(c2));
				
				ConfigurationBox copyin2 = new ConfigurationBox();
				copy(cin2,copyin2);
				ConfigurationBox copyout = new ConfigurationBox();
				copy(cout,copyout);
//				rightToMerge.put(copyin2, copyout);
//				Bench.FI7_MERGE_CACHE_MISSES++;
//			} else {
//				ConfigurationBox fmerge = (ConfigurationBox) merge;
//				copy(fmerge,cout);
////				Bench.FI6_MERGE_CACHE_HITS++;
//			}
		}
		
	}
	
	/** 
	 * {@inheritDoc}
	 */
	protected Object newInitialFlow() {
		return new ConfigurationBox();
	}
	
	public boolean mustAlias(Local l1, Local l2, Stmt s) {
		return mustAlias.mustAlias(l1,l2, s);
	}

//	/**
//	 * Here we construct a special topological orderer which
//	 * handles split points in a way that inner SCCs are computer first.
//	 * @return a {@link SlowPseudoTopologicalOrderer}
//	 */
//	protected Orderer constructOrderer() {
//		/* 
//		 * Ordering seems to be important for the caching strategy.
//		 */
//		
//		Orderer orderer;
//		if(defaultOrder) {
//			orderer = super.constructOrderer();
//		} else {
//			orderer = new SlowPseudoTopologicalOrderer();
//		}
//		//orderer = new FastOrderer();
//		
//		System.err.println("Using "+orderer.getClass().getName());
//		return orderer;
//	}
//	
//	/**
//	 * Here we construct a worklist imlemented by a {@link BoundedPriorityList}.
//	 * @return a {@link BoundedPriorityList}
//	 */
//	protected Collection constructWorklist(Map numbers) {
//		if(defaultOrder) {
//			return super.constructWorklist(numbers);
//		}
//
//		//we get a map object->index;
//		//convert that into an ordered list
//		List orderedList = new ArrayList(numbers.size());
//		final Object SPACEHOLDER = new Object();
//		for(int i=0;i<numbers.size();i++) {
//			orderedList.add(SPACEHOLDER);
//		}
//		for (Iterator iter = numbers.entrySet().iterator(); iter.hasNext();) {
//			Entry entry = (Entry) iter.next();
//			Object oldVal = orderedList.set(((Integer)entry.getValue()).intValue()-1, entry.getKey());
//			assert oldVal==SPACEHOLDER; //there should be no overrides
//		}
//		
//		return new BoundedPriorityList(orderedList);
//	}

	/**
	 * @return the stateMachine
	 */
	public TMStateMachine getStateMachine() {
		return stateMachine;
	}

}
