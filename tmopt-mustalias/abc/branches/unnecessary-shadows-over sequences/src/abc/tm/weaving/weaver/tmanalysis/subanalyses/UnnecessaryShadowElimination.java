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
package abc.tm.weaving.weaver.tmanalysis.subanalyses;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.pointer.LocalNotMayAliasAnalysis;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.UnitGraph;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.weaver.tmanalysis.StmtSequenceFinder;
import abc.tm.weaving.weaver.tmanalysis.Util;
import abc.tm.weaving.weaver.tmanalysis.ds.MustMayNotAliasDisjunct;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.LoopAwareLocalMustAliasAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;

/**
 * An optimization that eliminates <i>unnecessary shadows</i>. A shadow is deemed unnecessary if for any possible input at configuration at its location it can never
 * change this configuration.
 *
 * @author Eric Bodden
 */
public class UnnecessaryShadowElimination {
    
    public static boolean apply(TraceMatch tm, UnitGraph g, Map<Local, Stmt> tmLocalsToDefStatements, LoopAwareLocalMustAliasAnalysis localMustAliasAnalysis, LocalNotMayAliasAnalysis localNotMayAliasAnalysis) {
        System.err.println("Unnecessary shadow elimination...");

        SootMethod m = g.getBody().getMethod();

        Collection<Stmt> allStmts = new HashSet<Stmt>();
        for (Unit u : g.getBody().getUnits()) {
            Stmt st = (Stmt)u;
            allStmts.add(st);
        }
        
        System.err.println("Analyzing entire method...");
        Collection<ISymbolShadow> unnecessaryShadows =
        	analyzeStmtSequence(allStmts, tm, g, tmLocalsToDefStatements,
                    localMustAliasAnalysis, localNotMayAliasAnalysis, m);
        
		//get all shadows in the method body
        Set<ISymbolShadow> allShadows = Util.getAllActiveShadows(tm,g.getBody().getUnits());
        boolean allRemoved = unnecessaryShadows.equals(allShadows);
        
        if(allRemoved) {
            System.err.println("Unnecessary shadow elimination removed all shadows.");
            return true;
        }   

        
        LoopNestTree loopNestTree = new LoopNestTree(g.getBody());
        for (Loop loop : loopNestTree) {
            System.err.println("Analyzing loop with head "+loop.getHead());
            unnecessaryShadows.addAll(
                analyzeStmtSequence(loop.getLoopStatements(), tm, g, tmLocalsToDefStatements,
                        localMustAliasAnalysis, localNotMayAliasAnalysis, m)
            );
            
            //get all shadows in the method body
            allShadows = Util.getAllActiveShadows(tm,g.getBody().getUnits());
            allRemoved = unnecessaryShadows.equals(allShadows);
            if(allRemoved) {
                System.err.println("Unnecessary shadow elimination removed all shadows.");
                return true;
            }
        }
        
        
        Collection<List<Stmt>> allStatementSequences = StmtSequenceFinder.allStatementSequences(g);
        int maxLength = 0;
        for (List<Stmt> sequence : allStatementSequences) {
            maxLength = Math.max(maxLength, sequence.size());
        }
        maxLength = Math.max(maxLength, 50);        
        
        for (int length = maxLength; length>5; length = length-10) {
            System.err.println("Stmt sequences of length at most "+length);
            allStatementSequences = StmtSequenceFinder.splitSequencesLargerThan(allStatementSequences, length);
            //filter out sequences without shadows
            for (Iterator<List<Stmt>> iterator = allStatementSequences.iterator(); iterator.hasNext();) {
                List<Stmt> seq = iterator.next();
                if(Util.getAllActiveShadows(tm, seq).isEmpty())
                    iterator.remove();
            }
            System.err.println("Sequences of that length containing shadows: "+allStatementSequences.size());
            for (List<Stmt> sequence : allStatementSequences) {
                unnecessaryShadows.addAll(
                    analyzeStmtSequence(sequence, tm, g, tmLocalsToDefStatements,
                            localMustAliasAnalysis, localNotMayAliasAnalysis, m)
                );
                
                //get all shadows in the method body
                allShadows = Util.getAllActiveShadows(tm,g.getBody().getUnits());
                allRemoved = unnecessaryShadows.equals(allShadows);
                if(allRemoved) {
                    System.err.println("Unnecessary shadow elimination removed all shadows.");
                    return true;
                }
            }
        }
        
        return false; //shadows remaining
    }

    private static Collection<ISymbolShadow> analyzeStmtSequence(Collection<Stmt> stmtsToAnalyze,
            TraceMatch tm, UnitGraph g,
            Map<Local, Stmt> tmLocalsToDefStatements,
            LoopAwareLocalMustAliasAnalysis localMustAliasAnalysis,
            LocalNotMayAliasAnalysis localNotMayAliasAnalysis, SootMethod m) {
        IntraProceduralTMFlowAnalysis flowAnalysis = new IntraProceduralTMFlowAnalysis(
        		tm,
        		g,
                m,
                tmLocalsToDefStatements,
        		new MustMayNotAliasDisjunct(),
        		new HashSet<State>(),
        		stmtsToAnalyze,
        		stmtsToAnalyze,
                localMustAliasAnalysis,
                localNotMayAliasAnalysis,
                false /* do not abort if final state is hit --- we don't care here */
        );
		
		Status status = flowAnalysis.getStatus();
		//System.err.println("Analysis done with status: "+status);

		//if we abort once, we are going to abort for the other additional initial states, too so
		//just proceed with the same method
		if(!status.isAborted()) {
		
        	//eliminate all shadows which have the same before-flow and after-flow upon reaching the fixed point
		    Collection<ISymbolShadow> unnecessaryShadows = flowAnalysis.getUnnecessaryShadows();
            for (ISymbolShadow shadow : unnecessaryShadows) {
                if(!shadow.isEnabled())
                    continue;
        		System.err.println();
        		System.err.println("The following shadow does not have any effect (same before and after flow):");
        		System.err.println(shadow);
        		System.err.println("Shadow will be disabled.");
        		String uniqueShadowId = shadow.getUniqueShadowId();
        		System.err.println(uniqueShadowId);
        		ShadowRegistry.v().disableShadow(uniqueShadowId);
        		System.err.println();
        	}
        
            return unnecessaryShadows;
		}
		
		return Collections.emptySet();
    }

}
