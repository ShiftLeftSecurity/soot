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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.pointer.LocalNotMayAliasAnalysis;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.util.IdentityHashSet;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.weaver.tmanalysis.Util;
import abc.tm.weaving.weaver.tmanalysis.ds.MustMayNotAliasDisjunct;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.LoopAwareLocalMustAliasAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.stages.IntraproceduralAnalysis;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.OnceResidue;
import abc.weaving.weaver.Weaver;

/**
 * ShadowMotion
 *
 * @author Eric Bodden
 */
public class ShadowMotion {

    public static void apply(TraceMatch tm, UnitGraph g, Map<Local, Stmt> tmLocalsToDefStatements, LoopAwareLocalMustAliasAnalysis localMustAliasAnalysis, LocalNotMayAliasAnalysis localNotMayAliasAnalysis) {
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

    public static void optimizeLoop(TraceMatch tm, UnitGraph g, Map<Local, Stmt> tmLocalsToDefStatements, LoopAwareLocalMustAliasAnalysis localMustAliasAnalysis, LocalNotMayAliasAnalysis localNotMayAliasAnalysis,
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
                false
        );
        
        Status status = flowAnalysis.getStatus();
        System.err.println("Analysis done with status: "+status);

        //if we abort once, we are gonna abort for the other additional initial states too, so
        //just return, to proceed with the next loop
        
        if(status.isAborted()) return;
        
        assert status.isFinishedSuccessfully();
    
        //check how often we had to iterate...
        for (Stmt loopExit : loop.getLoopExits()) {
            if(!flowAnalysis.statementsReachingFixedPointAtOnce().contains(loopExit)) {
                System.err.println("FP not reached after one iteration. Cannot optimize.");
                return;
            }
        }
        
        Weaver weaver = Main.v().getAbcExtension().getWeaver();
        
        Set<ISymbolShadow> shadowsThatWereMoved = new HashSet<ISymbolShadow>();
        
        //get the advice list for that method
        MethodAdviceList adviceList = IntraproceduralAnalysis.gai.getAdviceList(g.getBody().getMethod());
        //unflush it (open it for modifications)
        adviceList.unflush();
        //for each loop exit
        for (Stmt loopExit : loop.getLoopExits()) {
            System.err.println("Processing loop exit: "+loopExit);

            //get the flow that wraps around the loop
//            Set<Configuration> wrapFlow = flowAnalysis.getFlowAfter(loop.getBackJumpStmt());
            
            //walk through all statements in the loop, recording all shadows up to the loop exit;
            //we store a set for each statement that is annotated with shadows;
            //we start at the loop exit and then walk backwards through the loop in a depth-first fashion
            LinkedList<Set<ISymbolShadow>> shadowsOnPath = new LinkedList<Set<ISymbolShadow>>();
            Set<Unit> visited = new IdentityHashSet<Unit>();
            Queue<Unit> worklist = new LinkedList<Unit>();
            worklist.add(loopExit);
            while(!worklist.isEmpty()) {
                Unit curr = worklist.remove();
                visited.add(curr);
                
//                //we see a statement after which the analysis information is the same as after
//                //the back jump; so all shadows that precede the statement are unnecessary; hence stop
//                //TODO unsound -> what do we actually want here?
//                Set<Configuration> thisFlow = flowAnalysis.getFlowAfter(curr);
//                if(wrapFlow.equals(thisFlow)) break;
                
                Set<ISymbolShadow> shadows = Util.getAllActiveShadows(tm,Collections.singleton(curr));
                if(!shadows.isEmpty()) {
                    //add to the front
                    shadowsOnPath.add(0,shadows);
                }
                //stop at the loop head
                if(curr.equals(loop.getHead())) {
                    break;
                }
                
                //get all predecessors in the loop and add them to the worklist
                List<Unit> preds = new ArrayList<Unit>(g.getPredsOf(curr));
                preds.retainAll(loopStatements);     
                preds.removeAll(visited);
                worklist.addAll(preds);
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
                        //register shadow as being moved (in particular, *not* eliminated)
                        shadowsThatWereMoved.add(shadow);
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
        Set<ISymbolShadow> loopShadows = Util.getAllActiveShadows(tm,loop.getLoopStatements());
        for (ISymbolShadow shadow : loopShadows) {
            if(status.hitFinal()) {
                //we hit the final state; hence, it is not ok to disable the shadows in the loop completely;
                //however it is ok to only execute them once, because we know that the final state can only be hit once
                System.err.println("Executing shadow once because the final state can be hit only in the first iteration: "+shadow.getUniqueShadowId());
                ShadowRegistry.v().conjoinShadowWithResidue(shadow.getUniqueShadowId(), new OnceResidue((Stmt)weaver.reverseRebind(loop.getHead())));
            } else {           
                if(shadowsThatWereMoved.contains(shadow)) {
                    //TODO actually we should really re-tag the statements appropriately and also reconcile the shadow registry 
                    
                    //do *not* call ShadowRegistry.v().disableShadow(shadow.getUniqueShadowId()) because the shadow is not actually disabled (it just moved);
                    //if we called this method, this could falsify the results of any subsequent analysis stage 
                    ShadowRegistry.v().conjoinShadowWithResidue(shadow.getUniqueShadowId(), NeverMatch.v());
                } else {
                    ShadowRegistry.v().disableShadow(shadow.getUniqueShadowId());
                }
            }
        }
        //disable all unneeded supporting advice
        ShadowRegistry.v().disableAllUnneededSomeSyncAndBodyAdvice();
        //flush (commit) the advice list
        adviceList.flush();
    }

    
}
