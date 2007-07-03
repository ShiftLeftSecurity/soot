/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Patrick Lam
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
package abc.tm.weaving.weaver.tmanalysis.stages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import soot.Body;
import soot.Kind;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.jimple.toolkits.pointer.LocalNotMayAliasAnalysis;
import soot.jimple.toolkits.thread.IThreadLocalObjectsAnalysis;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.weaver.tmanalysis.Util;
import abc.tm.weaving.weaver.tmanalysis.ds.Configuration;
import abc.tm.weaving.weaver.tmanalysis.ds.MustMayNotAliasDisjunct;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status;
import abc.tm.weaving.weaver.tmanalysis.query.ReachableShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.SymbolShadowWithPTS;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowTag;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;
import abc.tm.weaving.weaver.tmanalysis.util.ShadowsPerTMSplitter;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.weaver.Weaver;

/**
 * IntraproceduralAnalysis: This analysis propagates tracematch
 * automaton states through the method.
 *
 * @author Patrick Lam
 * @author Eric Bodden
 */
public class IntraproceduralAnalysis extends AbstractAnalysisStage {
	
	protected final static boolean RUN_REAL_POINTS_TO = true;
	
	protected static TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();

	/**
	 * {@inheritDoc}
	 */
	protected void doAnalysis() {
		TMShadowTagger.v().apply();
		CallGraph cg; 
		@SuppressWarnings("unused") //maybe used later
		IThreadLocalObjectsAnalysis tloa = new IThreadLocalObjectsAnalysis() {
			public boolean isObjectThreadLocal(Value localOrRef,SootMethod sm) {
				//assume that any variable is thread-local;
				//THIS IS UNSAFE!
				return true;
			}
		};
		if(RUN_REAL_POINTS_TO) {
			cg = CallGraphAbstraction.v().abstractedCallGraph();
		} else {
			CallGraphBuilder cgb = new CallGraphBuilder(DumbPointerAnalysis.v());
			soot.Scene.v().setPointsToAnalysis(DumbPointerAnalysis.v());
			cg = cgb.getCallGraph();
			cgb.build();
		}
		
		Set reachableShadows = ReachableShadowFinder.v().reachableShadows(cg);
		Map tmNameToShadows = ShadowsPerTMSplitter.splitShadows(reachableShadows);
		
        boolean mayStartThreads = mayStartThreads();
        
        for (TraceMatch tm : (Collection<TraceMatch>)gai.getTraceMatches()) {
            if(mayStartThreads && !tm.isPerThread()) {
                System.err.println("#####################################################");
                System.err.println(" Application may start threads that execute shadows! ");
                System.err.println(" Tracematch "+tm.getName()+" is not per-thread!");
                System.err.println("#####################################################");
            }
            
        	Set<SootMethod> methodsWithShadows = new HashSet<SootMethod>();
        	Set<SymbolShadowWithPTS> thisTMsShadows = (Set<SymbolShadowWithPTS>) tmNameToShadows.get(tm.getName());
            for (SymbolShadowWithPTS s : thisTMsShadows) {
                SootMethod m = s.getContainer();
                methodsWithShadows.add(m);
            }

            for (SootMethod m : methodsWithShadows) {
                System.err.println("Analyzing: "+m+" on tracematch: "+tm.getName());
                
                UnitGraph g = new ExceptionalUnitGraph(m.retrieveActiveBody());
    			LocalMustAliasAnalysis localMustAliasAnalysis = new LocalMustAliasAnalysis(g);
				LocalNotMayAliasAnalysis localNotMayAliasAnalysis = new LocalNotMayAliasAnalysis(g);
                Map<Local,Stmt> tmLocalsToDefStatements = findTmLocalDefinitions(g,tm);

                //optimizeNeverStored(tm, g, tmLocalsToDefStatements, localMustAliasAnalysis, localNotMayAliasAnalysis);
                
                MHGPostDominatorsFinder pda = new MHGPostDominatorsFinder(new BriefUnitGraph(m.retrieveActiveBody()));
				LoopNestTree loopNestTree = new LoopNestTree(m.getActiveBody());
                if(loopNestTree.hasNestedLoops()) {
                    System.err.println("Method has nested loops.");
                }

				//for each loop, in ascending order (inner loops first) 
				for (Loop loop : loopNestTree) {
                    System.err.println("Optimizing loop...");
					optimizeLoop(tm, g, tmLocalsToDefStatements, localMustAliasAnalysis,localNotMayAliasAnalysis, pda, loop);
				}
				
                System.err.println("Method body...");
				removeQuasiNopStmts(tm, g, tmLocalsToDefStatements, localMustAliasAnalysis, localNotMayAliasAnalysis);
    			
                System.err.println("Done analyzing: "+m+" on tracematch: "+tm.getName());    			
	        }
		}
        
//        //set shadow-points
//        Weaver weaver = Main.v().getAbcExtension().getWeaver();        
//        ShadowPointsSetter sps = new ShadowPointsSetter(weaver.getUnitBindings());
//        for (SootClass c : classesWithReroutetShadows) {
//            sps.setShadowPointsPass1(c);
//        }
	}

    private boolean mayStartThreads() {
        CallGraph callGraph = CallGraphAbstraction.v().abstractedCallGraph();
        for (Iterator iterator = callGraph.listener(); iterator.hasNext();) {
            Edge edge = (Edge) iterator.next();
            if(edge.kind().equals(Kind.THREAD)) {
                return true;
            }
        }
        return false;
    }

    private void optimizeNeverStored(TraceMatch tm, UnitGraph g, Map<Local, Stmt> tmLocalsToDefStatements, LocalMustAliasAnalysis localMustAliasAnalysis, LocalNotMayAliasAnalysis localNotMayAliasAnalysis) {
        SootMethod m = g.getBody().getMethod();

        Set<ISymbolShadow> activeShadows = Util.getAllActiveShadows(g.getBody().getUnits());
        
        System.err.println("Running never-stored analysis...");
        
        Collection<Stmt> allStmts = new HashSet<Stmt>();
        for (Unit u : g.getBody().getUnits()) {
            Stmt st = (Stmt)u;
            allStmts.add(st);
        }

        HashSet<State> nonFinalStates = new HashSet<State>();
        for (Iterator stateIter = tm.getStateMachine().getStateIterator(); stateIter.hasNext();) {
			State state = (State) stateIter.next();
			if(!state.isFinalNode())
				nonFinalStates.add(state);
		}
        
		IntraProceduralTMFlowAnalysis flowAnalysis = new IntraProceduralTMFlowAnalysis(
                tm,
                g,
                new MustMayNotAliasDisjunct(
                        localMustAliasAnalysis,
                        localNotMayAliasAnalysis,
                        tmLocalsToDefStatements,
                        m,
                        tm
                ),
                nonFinalStates,
                allStmts
        );
        
        Status status = flowAnalysis.getStatus();
        System.err.println("Analysis done with status: "+status);
        
        if(!status.isFinishedSuccessfully() || status.hitFinal()) {
        	return;
        }
        
        //check if shadows in other methods could have any effect 
        
        //collect all shadow groups which have shadows in common with the current method
        Set<ShadowGroup> allShadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
		Set<ShadowGroup> overlappingShadowGroups = new HashSet<ShadowGroup>();
		for (ShadowGroup shadowGroup : allShadowGroups) {
			for (ISymbolShadow shadowInGroup : shadowGroup.getAllShadows()) {
				for (ISymbolShadow shadowHere : activeShadows) {
					if(shadowInGroup.getUniqueShadowId().equals(shadowHere.getUniqueShadowId())) {
						overlappingShadowGroups.add(shadowGroup);
					}
				}
			}
		}
		
		//generate a fake unit graph holding n nop-units labeled with all shadows
		//that overlap
		Set<ISymbolShadow> overlappingShadows = new HashSet<ISymbolShadow>();
		for (ShadowGroup shadowGroup : overlappingShadowGroups) {
			overlappingShadows.addAll(shadowGroup.getAllShadows());			
		}        
		int lengthOfLongestPath = FlowInsensitiveAnalysis.v().lengthOfLongestPathFor(tm);
		
		Map<TraceMatch,Set<ISymbolShadow>> tmToShadows = new HashMap<TraceMatch, Set<ISymbolShadow>>();
		tmToShadows.put(tm, overlappingShadows);
				
		final List<Unit> units = new ArrayList<Unit>(); 
		for(int i=0; i <lengthOfLongestPath; i++) {
			NopStmt nop = Jimple.v().newNopStmt();
			nop.addTag( new SymbolShadowTag(tmToShadows) );
		}
		
		DirectedGraph<Unit> fakeUnitGraph = new DirectedGraph<Unit>() {

			public List<Unit> getHeads() {
				//return first unit
				return Collections.singletonList(units.get(0));
			}

			public List<Unit> getPredsOf(Unit s) {
				int index = units.indexOf(s);
				assert index>-1;
				if(index==0) {
					//first element
					return Collections.emptyList();
				} else {
					return Collections.singletonList(units.get(index-1));
				}				
			}

			public List<Unit> getSuccsOf(Unit s) {
				int index = units.indexOf(s);
				assert index>-1;
				if(index==units.size()-1) {
					//last element
					return Collections.emptyList();
				} else {
					return Collections.singletonList(units.get(index+1));
				}				
			}

			public List<Unit> getTails() {
				//return last unit
				return Collections.singletonList(units.get(units.size()-1));
			}

			public Iterator iterator() {
				return units.iterator();
			}

			public int size() {
				return units.size();
			}			
		};
		

//        //eliminate all shadows which have the same before-flow and after-flow upon reaching the fixed point
//        for (SymbolShadow shadow : activeShadows) {
//            System.err.println();
//            System.err.println("The following shadow does not have any effect (same before and after flow):");
//            System.err.println(shadow);
//            System.err.println("Shadow will be disabled.");
//            String uniqueShadowId = shadow.getUniqueShadowId();
//            System.err.println(uniqueShadowId);
//            disableShadow(uniqueShadowId);
//            System.err.println();
//        }
    }
    
	/**
	 * @param tm
	 * @param g
	 * @param tmLocalsToDefStatements
	 * @param localMustAliasAnalysis
	 * @param localNotMayAliasAnalysis
	 * @param pda 
	 * @param pda 
	 * @param loopStatements 
	 * @param loopHead 
	 * @param pda2 
	 */
	private void optimizeLoop(TraceMatch tm, UnitGraph g, Map<Local, Stmt> tmLocalsToDefStatements, LocalMustAliasAnalysis localMustAliasAnalysis, LocalNotMayAliasAnalysis localNotMayAliasAnalysis,
			MHGPostDominatorsFinder pda, Loop loop) {
        Collection<Stmt> loopStatements = loop.getLoopStatements();
		SootMethod m = g.getBody().getMethod();
		//initialize to the maximal set, i.e. all units
		Set<Unit> shadowStatementsReachingFixedPointAtOnce = new HashSet<Unit>(m.getActiveBody().getUnits());
		
		for (Iterator stateIter = tm.getStateMachine().getStateIterator(); stateIter.hasNext();) {
			SMNode s = (SMNode) stateIter.next();
			if(!s.isFinalNode()) {

				System.err.println("Running analysis with additional initial state number "+s.getNumber()+".");
				
				HashSet<State> singleton = new HashSet<State>();
                singleton.add(s);
                IntraProceduralTMFlowAnalysis flowAnalysis = new IntraProceduralTMFlowAnalysis(
		        		tm,
		        		g,
		        		new MustMayNotAliasDisjunct(
		        				localMustAliasAnalysis,
		        				localNotMayAliasAnalysis,
		        				tmLocalsToDefStatements,
		        				m,
		        				tm
		        		),
		        		singleton,
		        		loopStatements
		        );
				
				Status status = flowAnalysis.getStatus();
				System.err.println("Analysis done with status: "+status);

				//if we abort once, we are gonna abort for the other additional initial states, too so
				//just proceed with the same method
				if(status.isAborted() || status.hitFinal()) return;
				
				assert status.isFinishedSuccessfully();
				
				//retain only those statements that reach the fixed point after one iteration (i.e. we intersect here over all additional initial states)
				shadowStatementsReachingFixedPointAtOnce.retainAll(flowAnalysis.statementsReachingFixedPointAtOnce());
			}
		}

        Weaver weaver = Main.v().getAbcExtension().getWeaver();
        
        MethodAdviceList adviceList = gai.getAdviceList(m);
        adviceList.unflush();
        //for each loop exit
        for (Stmt loopExit : loop.getLoopExits()) {
            if(!shadowStatementsReachingFixedPointAtOnce.contains(loopExit)) {
                System.err.println("FP not reached after one iteration. Cannot optimize.");
                break;
            }
            
            //walk through all statements in the loop, recording all shadows up to the loop exit 
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
            
            for (Stmt target : loop.targetsOfLoopExit(loopExit)) {
                
                Stmt originalTarget = (Stmt)weaver.reverseRebind(target);
                for (Set<ISymbolShadow> shadows : shadowsOnPath) {
                    if(!shadows.isEmpty()) {
                        
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
        }
        //disable all shadows in the loop
        Set<ISymbolShadow> loopShadows = Util.getAllActiveShadows(loop.getLoopStatements());
        for (ISymbolShadow shadow : loopShadows) {
            ShadowRegistry.v().disableShadow(shadow.getUniqueShadowId());
        }
        ShadowRegistry.v().disableAllUnneededSomeSyncAndBodyAdvice();
        adviceList.flush();
	}
    
	/**
	 * @param m
	 * @param tm
	 * @param g
	 * @param tmLocalsToDefStatements
	 * @param localMustAliasAnalysis
	 * @param localNotMayAliasAnalysis
	 * @param pda 
	 * @param pda2 
	 * @param pda 
	 * @param loopStatements 
	 * @param loopHead 
	 */
	private void removeQuasiNopStmts(TraceMatch tm, UnitGraph g, Map<Local, Stmt> tmLocalsToDefStatements, LocalMustAliasAnalysis localMustAliasAnalysis, LocalNotMayAliasAnalysis localNotMayAliasAnalysis) {
		SootMethod m = g.getBody().getMethod();
		//initialize to the maximal set, i.e. all active shadows
		Set<ISymbolShadow> invariantShadows = Util.getAllActiveShadows(g.getBody().getUnits());
		
		for (Iterator stateIter = tm.getStateMachine().getStateIterator(); stateIter.hasNext();) {
			SMNode s = (SMNode) stateIter.next();
			if(!s.isFinalNode()) {

				System.err.println("Running analysis with additional initial state number "+s.getNumber()+".");
				
                Collection<Stmt> allStmts = new HashSet<Stmt>();
                for (Unit u : g.getBody().getUnits()) {
                    Stmt st = (Stmt)u;
                    allStmts.add(st);
                }

                HashSet<State> singleton = new HashSet<State>();
                singleton.add(s);
				IntraProceduralTMFlowAnalysis flowAnalysis = new IntraProceduralTMFlowAnalysis(
		        		tm,
		        		g,
		        		new MustMayNotAliasDisjunct(
		        				localMustAliasAnalysis,
		        				localNotMayAliasAnalysis,
		        				tmLocalsToDefStatements,
		        				m,
		        				tm
		        		),
		        		singleton,
		        		allStmts
		        );
				
				Status status = flowAnalysis.getStatus();
				System.err.println("Analysis done with status: "+status);

				//if we abort once, we are gonna abort for the other additional initial states, too so
				//just proceed with the same method
				if(status.isAborted()) return;
				
				assert status.isFinishedSuccessfully();
				
				//retain only those shadows that are invariant (i.e. we intersect here over all additional initial states)						
				invariantShadows.retainAll(flowAnalysis.getUnnecessaryShadows());
			}
		}

		//eliminate all shadows which have the same before-flow and after-flow upon reaching the fixed point
		for (ISymbolShadow shadow : invariantShadows) {
			System.err.println();
			System.err.println("The following shadow does not have any effect (same before and after flow):");
			System.err.println(shadow);
			System.err.println("Shadow will be disabled.");
			String uniqueShadowId = shadow.getUniqueShadowId();
			System.err.println(uniqueShadowId);
			disableShadow(uniqueShadowId);
			System.err.println();
		}
		
	}
	
	public Set<Stmt> sameBeforeAndAfterFlow(IntraProceduralTMFlowAnalysis flowAnalysis) {
		Set<Stmt> result = new HashSet<Stmt>();
		
		//for each statement with an active shadow
		for (Stmt stmt : flowAnalysis.statemementsWithActiveShadows()) {
            Set<Configuration> flowBefore = flowAnalysis.getFlowBefore(stmt);
            Set<Configuration> flowAfter = flowAnalysis.getFlowAfter(stmt);
			//is the before-flow equal to the after-flow?
			if(flowBefore.equals(flowAfter)) {
				result.add(stmt);
			}
		}		
		return result;
	}

	/**
	 * @param b
	 * @param tm 
	 * @return
	 */
	private Map<Local, Stmt> findTmLocalDefinitions(UnitGraph g, TraceMatch tm) {
		
		Body b = g.getBody();
		
		Set<Local> boundLocals = new HashSet<Local>();
		
		//find all localc bound by shadows of the given tracematch		
		for (Unit u: b.getUnits()) {
            Stmt stmt = (Stmt)u;
			if(stmt.hasTag(SymbolShadowTag.NAME)) {
				SymbolShadowTag tag = (SymbolShadowTag) stmt.getTag(SymbolShadowTag.NAME);
				Set<ISymbolShadow> matchesForTracematch = tag.getMatchesForTracematch(tm);
				for (ISymbolShadow shadow : matchesForTracematch) {
					boundLocals.addAll(shadow.getAdviceLocals());
				}
			}
		}
		
		Map<Local,Stmt> localToStmtAfterDefStmt = new HashMap<Local, Stmt>();
		
        for (Unit u: b.getUnits()) {
            Stmt stmt = (Stmt)u;
            for (soot.ValueBox vb : (Collection<soot.ValueBox>)stmt.getDefBoxes()) {
                soot.Value v = vb.getValue();
                if(boundLocals.contains(v)) {
                    //have a definition of v already!
                    if(localToStmtAfterDefStmt.containsKey(v)) {
                        throw new RuntimeException("multiple defs");
                    }
                    
                	//we know that such def statements always have the form "adviceLocal = someLocal;",
                	//hence taking the first successor is always sound
                	localToStmtAfterDefStmt.put((Local)v, (Stmt)g.getSuccsOf(stmt).get(0));
                }
            }			
		}
		
		return localToStmtAfterDefStmt;		
	}

	
	
	//singleton pattern
	
	protected static IntraproceduralAnalysis instance;

	private IntraproceduralAnalysis() {}
	
	public static IntraproceduralAnalysis v() {
		if(instance==null) {
			instance = new IntraproceduralAnalysis();
		}
		return instance;		
	}
	
	/**
	 * Frees the singleton object. 
	 */
	public static void reset() {
		instance = null;
	}

}
