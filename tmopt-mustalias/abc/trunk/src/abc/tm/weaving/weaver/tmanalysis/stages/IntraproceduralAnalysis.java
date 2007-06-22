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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.jimple.toolkits.pointer.LocalNotMayAliasAnalysis;
import soot.jimple.toolkits.thread.IThreadLocalObjectsAnalysis;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.Pair;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.weaver.tmanalysis.ds.Configuration;
import abc.tm.weaving.weaver.tmanalysis.ds.ConfigurationBox;
import abc.tm.weaving.weaver.tmanalysis.ds.MustMayNotAliasDisjunct;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.PathsReachingFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status;
import abc.tm.weaving.weaver.tmanalysis.query.ReachableShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowTag;
import abc.tm.weaving.weaver.tmanalysis.util.ShadowsPerTMSplitter;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolShadow;
import abc.weaving.residues.OnceResidue;
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
		
        for (TraceMatch tm : (Collection<TraceMatch>)gai.getTraceMatches()) {
        	Set<SootMethod> methodsWithShadows = new HashSet<SootMethod>();
        	Set<Shadow> thisTMsShadows = (Set<Shadow>) tmNameToShadows.get(tm.getName());
            for (Shadow s : thisTMsShadows) {
                SootMethod m = s.getContainer();
                methodsWithShadows.add(m);
            }

            for (SootMethod m : methodsWithShadows) {
                System.err.println("Analyzing: "+m+" on tracematch: "+tm.getName());
                
                UnitGraph g = new ExceptionalUnitGraph(m.retrieveActiveBody());
    			LocalMustAliasAnalysis localMustAliasAnalysis = new LocalMustAliasAnalysis(g);
				LocalNotMayAliasAnalysis localNotMayAliasAnalysis = new LocalNotMayAliasAnalysis(g);
                Map<Local,Stmt> tmLocalsToDefStatements = findTmLocalDefinitions(g,tm);
				MHGPostDominatorsFinder pda = new MHGPostDominatorsFinder(new BriefUnitGraph(m.retrieveActiveBody()));
				LoopNestTree loopNestTree = new LoopNestTree(m.getActiveBody());

				//for each loop, in ascending order (inner loops first) 
				for (Pair<Stmt, List<Stmt>> pair : loopNestTree) {
					Stmt loopHead = pair.getO1();
					List<Stmt> loopStatements = pair.getO2();
					optimizeLoop(tm, g, tmLocalsToDefStatements, localMustAliasAnalysis,localNotMayAliasAnalysis, pda, loopStatements, loopHead);
				}
				
				removeQuasiNopStmts(tm, g, tmLocalsToDefStatements, localMustAliasAnalysis, localNotMayAliasAnalysis);
    			
                System.err.println("Done analyzing: "+m+" on tracematch: "+tm.getName());    			
	        }
		}
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
			MHGPostDominatorsFinder pda, List<Stmt> loopStatements, Stmt loopHead) {
		SootMethod m = g.getBody().getMethod();
		//initialize to the maximal set, i.e. all units
		Set<Stmt> shadowStatementsReachingFixedPointAtOnce = new HashSet<Stmt>(m.getActiveBody().getUnits());
		
		for (Iterator stateIter = tm.getStateMachine().getStateIterator(); stateIter.hasNext();) {
			SMNode s = (SMNode) stateIter.next();
			if(!s.isFinalNode()) {

				System.err.println("Running analysis with additional initial state number "+s.getNumber()+".");
				
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
		        		s,
		        		loopStatements
		        );
				
				Status status = flowAnalysis.getStatus();
				System.err.println("Analysis done with status: "+status);

				//if we abort once, we are gonna abort for the other additional initial states, too so
				//just proceed with the same method
				if(status.isAborted()) return;
				
				assert status.isFinishedSuccessfully();
				
				//retain only those statements that reach the fixed point after one iteration (i.e. we intersect here over all additional initial states)
				shadowStatementsReachingFixedPointAtOnce.retainAll(shadowStatementsReachingFixedPointAtOnce(flowAnalysis));
			}
		}

		Weaver weaver = abc.main.Main.v().getAbcExtension().getWeaver();
		
		for(Stmt s : shadowStatementsReachingFixedPointAtOnce) {
			SymbolShadowTag tag = (SymbolShadowTag) s.getTag(SymbolShadowTag.NAME);
			System.err.println();
			boolean postDominatedByOtherLoopShadowStmt = false;
			for(Stmt otherStmt : shadowStatementsReachingFixedPointAtOnce) {
				if(pda.isDominatedBy(s, otherStmt) && s!=otherStmt) {
					postDominatedByOtherLoopShadowStmt = true;
					break;
				}
			}			
			//if s is post-dominated by another shadow in the loop
			if(postDominatedByOtherLoopShadowStmt) {
				System.err.println("The following shadow statement is contained in a loop, has an effect but only needs to be executed once\n" +
						"and is post-dominated by the another shadow in the loop.");
				System.err.println(s);
				System.err.println("Disabling shadows.");
				for (SymbolShadow shadow : tag.getMatchesForTracematch(tm)) {
					String uniqueShadowId = shadow.getUniqueShadowId();
					System.err.println(uniqueShadowId);
					disableShadow(uniqueShadowId);
				}
			} else {
				System.err.println("The following shadow statement is contained in a loop but the shadow only needs to be executed once:");
				System.err.println(s);
				System.err.println("Applying optimization 'execute only once per method execution'.");
				for (SymbolShadow shadow : tag.getMatchesForTracematch(tm)) {
					System.err.println(shadow.getUniqueShadowId());
					ShadowRegistry.v().conjoinShadowWithResidue(shadow.getUniqueShadowId(), new OnceResidue((Stmt) weaver.reverseRebind(loopHead)));
				}
			}
			System.err.println();
		}
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
		//initialize to the maximal set, i.e. all units
		Set<Stmt> sameBeforeAndAfterFlow = new HashSet<Stmt>(m.getActiveBody().getUnits());
		
		for (Iterator stateIter = tm.getStateMachine().getStateIterator(); stateIter.hasNext();) {
			SMNode s = (SMNode) stateIter.next();
			if(!s.isFinalNode()) {

				System.err.println("Running analysis with additional initial state number "+s.getNumber()+".");
				
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
		        		s,
		        		g.getBody().getUnits()
		        );
				
				Status status = flowAnalysis.getStatus();
				System.err.println("Analysis done with status: "+status);

				//if we abort once, we are gonna abort for the other additional initial states, too so
				//just proceed with the same method
				if(status.isAborted()) return;
				
				assert status.isFinishedSuccessfully();
				
				//retain only those statements that have the same before and after-flow (i.e. we intersect here over all additional initial states)						
				sameBeforeAndAfterFlow.retainAll(sameBeforeAndAfterFlow(flowAnalysis));
			}
		}

		//eliminate all shadows which have the same before-flow and after-flow upon reaching the fixed point
		for (Stmt s : sameBeforeAndAfterFlow) {
			SymbolShadowTag tag = (SymbolShadowTag) s.getTag(SymbolShadowTag.NAME);
			System.err.println();
			System.err.println("The following shadow statement does not have any effect (same before and after flow):");
			System.err.println(s);
			System.err.println("Disabling shadows.");
			for (SymbolShadow shadow : tag.getMatchesForTracematch(tm)) {
				String uniqueShadowId = shadow.getUniqueShadowId();
				System.err.println(uniqueShadowId);
				disableShadow(uniqueShadowId);
			}
			System.err.println();
		}
		
	}
	
	public Set<Stmt> sameBeforeAndAfterFlow(IntraProceduralTMFlowAnalysis flowAnalysis) {
		Set<Stmt> result = new HashSet<Stmt>();
		
		//for each statement with an active shadow
		for (Stmt stmt : flowAnalysis.statemementsWithActiveShadows()) {
			Configuration beforeFlow = ((ConfigurationBox) flowAnalysis.getFlowBefore(stmt)).get();
			Configuration afterFlow = ((ConfigurationBox) flowAnalysis.getFlowAfter(stmt)).get();
			assert beforeFlow!=null && afterFlow!=null;
			//is the before-flow equal to the after-flow?
			if(beforeFlow.equals(afterFlow)) {
				result.add(stmt);
			}
		}		
		return result;
	}

	/**
	 * Determines all statements that are contained in the associated methods
	 * which are annotated with a shadow and are in a loop but for which it is guaranteed that
	 * one loop iteration suffices to reach the fixed point.
	 * @param flowAnalysis 
	 * @return
	 */
	public Set<Stmt> shadowStatementsReachingFixedPointAtOnce(IntraProceduralTMFlowAnalysis flowAnalysis) {
		PathsReachingFlowAnalysis prf = new PathsReachingFlowAnalysis(flowAnalysis.getUnitGraph());
		
		Set<Stmt> result = new HashSet<Stmt>();
		
		//for each statement with an active shadow
		for (Stmt stmt : flowAnalysis.statemementsWithActiveShadows()) {
			//if contained in a loop
			if(prf.visitedPotentiallyManyTimes(stmt)) {
				//if the first after-flow is equal to the final one
				Configuration firstAfterFlow = flowAnalysis.getFirstAfterFlow(stmt);
				Configuration finalAfterFlow = ((ConfigurationBox) flowAnalysis.getFlowAfter(stmt)).get();
				assert firstAfterFlow!=null && finalAfterFlow!=null;
				//is the first after-flow equal to the last?
				if(firstAfterFlow.equals(finalAfterFlow)) {
					result.add(stmt);
				}
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
		for (Stmt stmt : (Collection<Stmt>)b.getUnits()) {
			if(stmt.hasTag(SymbolShadowTag.NAME)) {
				SymbolShadowTag tag = (SymbolShadowTag) stmt.getTag(SymbolShadowTag.NAME);
				Set<SymbolShadow> matchesForTracematch = tag.getMatchesForTracematch(tm);
				for (SymbolShadow shadow : matchesForTracematch) {
					boundLocals.addAll(shadow.getTmFormalToAdviceLocal().values());
				}
			}
		}
		
		Map<Local,Stmt> localToStmtAfterDefStmt = new HashMap<Local, Stmt>();
		
		Set<Local> rhsLocals = new HashSet<Local>(); 
		for (Stmt stmt : (Collection<Stmt>)b.getUnits()) {
            for (soot.ValueBox vb : (Collection<soot.ValueBox>)stmt.getDefBoxes()) {
                soot.Value v = vb.getValue();
                if(boundLocals.contains(v)) {
                	rhsLocals.add((Local)((AssignStmt)stmt).getRightOp());

                	//we know that such def statements always have the form "adviceLocal = someLocal;",
                	//hence taking the first successor is always sound
                	localToStmtAfterDefStmt.put((Local)v, (Stmt)g.getSuccsOf(stmt).get(0));
                }
            }			
		}
		
		Set<Local> oneDef = new HashSet<Local>();
		for (Stmt stmt : (Collection<Stmt>)b.getUnits()) {
            for (soot.ValueBox vb : (Collection<soot.ValueBox>)stmt.getDefBoxes()) {
                soot.Value v = vb.getValue();
                if(rhsLocals.contains(v)) {
                	//if was already added, we have multiple (static) defs of v
               		if(!oneDef.add((Local)v)) {
                		throw new RuntimeException("multiple defs");
                	}
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
