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
package abc.tm.weaving.aspectinfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import polyglot.util.ErrorInfo;
import polyglot.util.Position;
import soot.SootMethod;
import abc.main.Debug;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.StateMachine;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.util.AdviceApplicationVisitor;
import abc.tm.weaving.weaver.tmanalysis.util.AdviceApplicationVisitor.AdviceApplicationHandler;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.residues.NeverMatch;

/** 
 * TODO
 *
 *  @author Eric Bodden
 */
public class TMOptTraceMatch extends TraceMatch
{
	
    private final Map adviceNameToTymbolName;

	/**
     * @see TraceMatch#TraceMatch(String, List, List, StateMachine, boolean, Map, List, Map, String, String, String, Aspect, Position)
     */
    public TMOptTraceMatch(String name, List formals, List new_advice_body_formals,
                        StateMachine state_machine, boolean per_thread,
                        Map sym_to_vars, List frequent_symbols,
                        Map sym_to_advice_name, String synch_advice_name,
                        String some_advice_name, String dummy_proceed_name,
                        Aspect container, Position pos)
    {
    	super(
    			name, 
    			formals, 
    			new_advice_body_formals, 
    			state_machine, 
    			per_thread, 
    			sym_to_vars, 
    			frequent_symbols, 
    			sym_to_advice_name, 
    			synch_advice_name, 
    			some_advice_name, 
    			dummy_proceed_name, 
    			container, 
    			pos
    	);
    	//store reverse mapping
    	adviceNameToTymbolName = new HashMap();
		for (Iterator iterator = sym_to_advice_name.entrySet().iterator(); iterator.hasNext();) {
			Entry entry = (Entry) iterator.next();
			adviceNameToTymbolName.put(entry.getValue(), entry.getKey());
		}
    }
    
	/**
	 * Removes all the symbols from the tracematch alphabet for which
	 * the related per-symbol advice never matched during the last weaving phase.
	 * @return <code>true</code> if it is safe to remove this tracematch entirely 
	 */
	public boolean removeNonMatchingSymbols() {
		GlobalAspectInfo gai = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo();
		TMStateMachine sm = (TMStateMachine) getStateMachine();
		
		//determine the symbols that do not match;
		//those can be removed
		Set symbolsToRemove = new HashSet();
		
		//for all advice declarations
		for (Iterator iter = gai.getAdviceDecls().iterator(); iter.hasNext();) {
			AdviceDecl ad = (AdviceDecl) iter.next();

			//of kind "per-symbol advice declaration"
			if(ad instanceof TMOptPerSymbolAdviceDecl) {
				TMOptPerSymbolAdviceDecl psad = (TMOptPerSymbolAdviceDecl) ad;
				
				//for this tracematch
				if(psad.getTraceMatchID().equals(name)) {
					
					//if this advice was never applied
					if(psad.getApplCount()==0) {

						//mark symbol as a symbol that can be removed
						symbolsToRemove.add(psad.getSymbolId());
					}
				}
			}
		}
		
		if(symbolsToRemove.size()>0 && Debug.v().debugTmAnalysis) {
			System.err.println("The following symbols do not match and will be removed " +
					"in "+name+": "+symbolsToRemove);
		}
		
		//remove all edges for those symbols
		Set edgesToRemove = new HashSet();
		for (Iterator symIter = symbolsToRemove.iterator(); symIter.hasNext();) {
			String symbolName = (String) symIter.next();

			//for all edges in the state machine
			for (Iterator iterator = sm.getEdgeIterator(); iterator.hasNext();) {
				SMEdge edge = (SMEdge) iterator.next();
				
				//with the same symbol name as the advice that never applied 
				if(edge.getLabel().equals(symbolName)) {
					//mark the edge as to remove
					edgesToRemove.add(edge);
				}
			}
		}
		sm.removeEdges(edgesToRemove.iterator());
		sm.compressStates();

		if(Debug.v().debugTmAnalysis && symbolsToRemove.size()>0 && !sm.isEmpty()) {
			System.err.println("Remaining state machine:");
			System.err.println(sm);
		}

		if(sm.isEmpty()) {
			//if the state machine is now empty, remove this tracematch entirely
			
			if(Debug.v().debugTmAnalysis) {
				System.err.println("State machine now empty. Removing "+name+".");
			}
			
			return true;
		} else {
			//and remove all the symbols which don't match from the tracematch;
			//this will also remove the handling for skip loops for those symbols,
			//which is safe cause we know the symbol can never occur anyway

			for (Iterator symIter = symbolsToRemove.iterator(); symIter.hasNext();) {
				String symbolName = (String) symIter.next();
				sym_to_advice_name.remove(symbolName);
				sym_to_vars.remove(symbolName);
			}

			if(Debug.v().debugTmAnalysis && symbolsToRemove.size()>0) {
				System.err.println("Remaining alphabet of "+name+": "+getSymbols());
			}
			return false;
		}
	}
	
	public String symbolNameForSymbolAdvice(SootMethod symbolAdviceMethod) {
		return (String) adviceNameToTymbolName.get(symbolAdviceMethod.getName());
	}
}
