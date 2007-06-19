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
import java.util.LinkedList;
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
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.ds.MustMayNotAliasDisjunct;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.InitKind;
import abc.tm.weaving.weaver.tmanalysis.query.ReachableShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowTag;
import abc.tm.weaving.weaver.tmanalysis.util.ShadowsPerTMSplitter;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolShadow;

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
		
		List<InitKind> initKinds = new LinkedList<InitKind>();
		initKinds.add(InitKind.MINIMAL_ASSUMPTION);
		initKinds.add(InitKind.MAXIMAL_ASSUMPTION);
		
        for (TraceMatch tm : (Collection<TraceMatch>)gai.getTraceMatches()) {
        	Set<SootMethod> methodsWithShadows = new HashSet<SootMethod>();
        	Set<Shadow> thisTMsShadows = (Set<Shadow>) tmNameToShadows.get(tm.getName());
            for (Shadow s : thisTMsShadows) {
                SootMethod m = s.getContainer();
                methodsWithShadows.add(m);
            }

            for (SootMethod m : methodsWithShadows) {
        		for (InitKind initKind : initKinds) {
	                UnitGraph g = new ExceptionalUnitGraph(m.retrieveActiveBody());
	                
	                Map<Local,Stmt> tmLocalsToDefStatements = findTmLocalDefinitions(g,tm);
	                System.err.println("Analyzing: "+m+" on tracematch: "+tm.getName());
	    			IntraProceduralTMFlowAnalysis flowAnalysis = new IntraProceduralTMFlowAnalysis(
	                		tm,
	                		g,
	                		new MustMayNotAliasDisjunct(
	                				new LocalMustAliasAnalysis(g),
	                				new LocalNotMayAliasAnalysis(g),
	                				tmLocalsToDefStatements,
	                				m,
	                				tm
	                		),
	                		initKind
	                );
	    			
	    			for (Stmt s : (Collection<Stmt>)g.getBody().getUnits()) {
						if(s.hasTag(SymbolShadowTag.NAME)) {
							SymbolShadowTag tag = (SymbolShadowTag) s.getTag(SymbolShadowTag.NAME);
							if(!tag.getMatchesForTracematch(tm).isEmpty()) {
								System.err.println();
								System.err.println();
								System.err.println(flowAnalysis.getFlowBefore(s));
								System.err.println(s);
								for (SymbolShadow shadow : tag.getMatchesForTracematch(tm)) {
									System.err.println(shadow.getUniqueShadowId());
								}
								System.err.println(flowAnalysis.getFlowAfter(s));
							}
						}
					}
				}
	        }
		}
	}
	
	//singleton pattern

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
