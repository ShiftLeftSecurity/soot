/*
 * Created on 5-Mar-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.jimple.Stmt;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowMatchTag;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolFinder.SymbolShadowMatch;

/**
 * TransitionUtils
 *
 * @author Eric Bodden
 */
public class TransitionUtils {
	
	/**
	 * For any state of a {@link TMStateMachine} of {@link TraceMatch} tm, returns the successor states
	 * under the statement stmt. 
	 * @param currentState the current state of the tracematch automaton
	 * @param tm the tracematch owning that state
	 * @param stmt any statement; tagged with a tracematch shadow
	 * @return
	 */
	public static Set<SMNode> getSuccessorStatesFor(SMNode currentState, TraceMatch tm, Stmt stmt) {
		//if the current statement is not tagged, we don't switch states
		if(!stmt.hasTag(SymbolShadowMatchTag.NAME)) {
			return Collections.singleton(currentState);
		}
		
		Set<SMNode> res = new HashSet<SMNode>();		
		SymbolShadowMatchTag tag = (SymbolShadowMatchTag) stmt.getTag(SymbolShadowMatchTag.NAME);
		
		boolean atLeastOneShadowActive = false;
		//for all shadow matches registered in the tag
		for (SymbolShadowMatch match : tag.getMatchesForTracematch(tm)) {
			//if the shadow is still enabled
			if(ShadowRegistry.v().enabledShadows().contains(match.getUniqueShadowId())) {
				
				//add all states which we can reach directly via this symbol
				String symbolName = match.getSymbolName();
				for (Iterator edgeIter = currentState.getOutEdgeIterator(); edgeIter.hasNext();) {
					SMEdge edge = (SMEdge) edgeIter.next();
					if(edge.getLabel().equals(symbolName)) {
						res.add(edge.getTarget());
					}
				}
				
				atLeastOneShadowActive = true;
			}
		}

		//if we actually made a transition, return the result, otherwise treat as a no-op
		if(atLeastOneShadowActive)
			return res;
		else
			return Collections.singleton(currentState);
	}

}
