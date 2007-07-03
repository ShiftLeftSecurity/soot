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
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import soot.Local;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.jimple.toolkits.pointer.LocalNotMayAliasAnalysis;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.weaver.tmanalysis.Util;
import abc.tm.weaving.weaver.tmanalysis.ds.MustMayNotAliasDisjunct;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.stages.IntraproceduralAnalysis;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.weaver.Weaver;

/**
 * ShadowMotion
 *
 * @author Eric Bodden
 */
public class ShadowMotion {

    public static void apply(TraceMatch tm, UnitGraph g, Map<Local, Stmt> tmLocalsToDefStatements, LocalMustAliasAnalysis localMustAliasAnalysis, LocalNotMayAliasAnalysis localNotMayAliasAnalysis) {
        System.err.println("Loop optimization...");

        //create post-dominator analysis
        MHGPostDominatorsFinder pda = new MHGPostDominatorsFinder(new BriefUnitGraph(g.getBody()));
        //build a loop nest tree
        LoopNestTree loopNestTree = new LoopNestTree(g.getBody());
        if(loopNestTree.hasNestedLoops()) {
            System.err.println("Method has nested loops.");
        }
    
        if(loopNestTree.isEmpty()) {
            System.err.println("Method has no loops.");
        }
        
        //for each loop, in ascending order (inner loops first) 
        for (Loop loop : loopNestTree) {
        	optimizeLoop(tm, g, tmLocalsToDefStatements, localMustAliasAnalysis,localNotMayAliasAnalysis, pda, loop);
        }
    }

    public static void optimizeLoop(TraceMatch tm, UnitGraph g, Map<Local, Stmt> tmLocalsToDefStatements, LocalMustAliasAnalysis localMustAliasAnalysis, LocalNotMayAliasAnalysis localNotMayAliasAnalysis,
            MHGPostDominatorsFinder pda, Loop loop) {
        System.err.println("Optimizing loop...");
        
        //find all active shadows in the method
        Collection<Stmt> loopStatements = loop.getLoopStatements();
        
        IntraProceduralTMFlowAnalysis flowAnalysis = new IntraProceduralTMFlowAnalysis(
                tm,
                g,
                g.getBody().getMethod(),
                tmLocalsToDefStatements,
                new MustMayNotAliasDisjunct(),
                new HashSet<State>(),
                loopStatements,
                localMustAliasAnalysis,
                localNotMayAliasAnalysis,
                true
        );
        
        Status status = flowAnalysis.getStatus();
        System.err.println("Analysis done with status: "+status);

        //if we abort once, we are gonna abort for the other additional initial states, too so
        //just proceed with the same method
        if(status.isAborted() || status.hitFinal()) return;
        
        assert status.isFinishedSuccessfully();
    
        Weaver weaver = Main.v().getAbcExtension().getWeaver();
        
        //get the advice list for that method
        MethodAdviceList adviceList = IntraproceduralAnalysis.gai.getAdviceList(g.getBody().getMethod());
        //unflush it (open it for modifications)
        adviceList.unflush();
        //for each loop exit
        for (Stmt loopExit : loop.getLoopExits()) {
            if(!flowAnalysis.statementsReachingFixedPointAtOnce().contains(loopExit)) {
                System.err.println("FP not reached after one iteration. Cannot optimize.");
                break;
            }
            
            System.err.println("Processing loop exit: "+loopExit);

            //should either all reach the fixed point at once or none of them
            for (Stmt loopStmt : loopStatements) {
                assert flowAnalysis.statementsReachingFixedPointAtOnce().contains(loopStmt);
            }
            
            //walk through all statements in the loop, recording all shadows up to the loop exit;
            //we store a set for each statement that is annotated with shadows
            Stack<Set<ISymbolShadow>> shadowsOnPath = new Stack<Set<ISymbolShadow>>();
            for (Stmt stmt : loop.getLoopStatements()) {
                Set<ISymbolShadow> shadows = Util.getAllActiveShadows(Collections.singleton(stmt));
                if(!shadows.isEmpty()) {
                    shadowsOnPath.push(shadows);
                }
                if(stmt.equals(loopExit)) {
                    break;
                }
            }

            //debug output
            System.err.println("Shadows for this loop exit:");
            for (Set<ISymbolShadow> shadows : shadowsOnPath) {
                System.err.print("{");
                for (Iterator<ISymbolShadow> iterator = shadows.iterator(); iterator.hasNext();) {
                    ISymbolShadow symbolShadow = iterator.next();
                    System.err.print(symbolShadow.getUniqueShadowId());
                    if(iterator.hasNext()) {
                        System.err.print(", ");                        
                    }
                }
                System.err.println("}");
            }

            //for each statement that is target of a loop exit
            for (Stmt target : loop.targetsOfLoopExit(loopExit)) {
                
                System.err.println("Copying shadows to target of loop exit: "+target);

                //get the corresponding Stmt before weaving
                Stmt originalTarget = (Stmt)weaver.reverseRebind(target);
                for (Set<ISymbolShadow> shadows : shadowsOnPath) {
                    //should really not be empty because we exclude empty sets explicitly in the above code
                    assert !shadows.isEmpty();                        
                    //get sync advice for those shadows;
                    //they all have the same sync advice so we can just get the one for the first symbol advice
                    ISymbolShadow firstShadow = shadows.iterator().next();                                
                    AdviceApplication syncAa = ShadowRegistry.v().getSyncAdviceApplicationForSymbolShadow(firstShadow.getUniqueShadowId());
                    //copy over sync advice
                    adviceList.copyAdviceApplication(syncAa,originalTarget);
                    //now process al symbol shadows
                    for (ISymbolShadow shadow : shadows) {
                        AdviceApplication symbolAa = ShadowRegistry.v().getSymbolAdviceApplicationForShadow(shadow.getUniqueShadowId());
                        //copy over symbol advice
                        adviceList.copyAdviceApplication(symbolAa,originalTarget);
                    }
                    AdviceApplication someAa = ShadowRegistry.v().getSomeAdviceApplicationForSymbolShadow(firstShadow.getUniqueShadowId());
                    //copy over sync advice
                    adviceList.copyAdviceApplication(someAa,originalTarget);
                    //handle body
                    AdviceApplication bodyAa = ShadowRegistry.v().getBodyAdviceApplicationForSymbolShadow(firstShadow.getUniqueShadowId());
                    //copy over body advice if present
                    if(bodyAa!=null)
                        adviceList.copyAdviceApplication(bodyAa,originalTarget);
                }
            }
        }
        //disable all shadows in the loop
        Set<ISymbolShadow> loopShadows = Util.getAllActiveShadows(loop.getLoopStatements());
        for (ISymbolShadow shadow : loopShadows) {
            ShadowRegistry.v().disableShadow(shadow.getUniqueShadowId());
        }
        //disable all unneeded supporting advice
        ShadowRegistry.v().disableAllUnneededSomeSyncAndBodyAdvice();
        //flush (commit) the advice list
        adviceList.flush();
    }

    
}
