/*
 * Created on 12-Feb-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.stages;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import soot.jimple.toolkits.thread.IThreadLocalObjectsAnalysis;
import soot.jimple.toolkits.thread.ThreadLocalObjectsAnalysis;
import soot.jimple.toolkits.thread.mhp.UnsynchronizedMhpAnalysis;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.ds.MustMayNotAliasDisjunct;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.LocalMustAliasAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.LocalNotMayAliasAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.PathsReachingFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.TMFlowAnalysis;
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
 */
public class IntraproceduralAnalysis extends AbstractAnalysisStage {
	
	protected final static boolean MAKE_SAFE = false;
	
	protected static TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();

	/**
	 * {@inheritDoc}
	 */
	protected void doAnalysis() {
		TMShadowTagger.v().apply();
		CallGraph cg; 
		IThreadLocalObjectsAnalysis tloa;
		if(MAKE_SAFE) {
			CallGraphAbstraction.v().apply();
			cg = CallGraphAbstraction.v().abstractedCallGraph();
			tloa = new ThreadLocalObjectsAnalysis(new UnsynchronizedMhpAnalysis());
		} else {
			CallGraphBuilder cgb = new CallGraphBuilder(DumbPointerAnalysis.v());
			soot.Scene.v().setPointsToAnalysis(DumbPointerAnalysis.v());
			cg = cgb.getCallGraph();
			cgb.build();
			tloa = new IThreadLocalObjectsAnalysis() {
				public boolean isObjectThreadLocal(Value localOrRef,SootMethod sm) {
					//assume that any variable is thread-local;
					//THIS IS UNSAFE!
					return true;
				}
			};
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
                UnitGraph g = new ExceptionalUnitGraph(m.retrieveActiveBody());
                
                Map<Local,Stmt> tmLocalsToDefStatements = findTmLocalDefinitions(g,tm);
                System.err.println("Analyzing: "+m);
    			TMFlowAnalysis flowAnalysis = new IntraProceduralTMFlowAnalysis(
                		tm,
                		g,
                		new MustMayNotAliasDisjunct(
                				new LocalMustAliasAnalysis(g),
                				new LocalNotMayAliasAnalysis(g),
                				tmLocalsToDefStatements
                		)
                );
    			
    			System.err.println(flowAnalysis.getActiveShadows());
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
		
		PathsReachingFlowAnalysis pathsReachingFlowAnalysis = new PathsReachingFlowAnalysis(g);
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
		
		for (Stmt stmt : (Collection<Stmt>)b.getUnits()) {
            for (soot.ValueBox vb : (Collection<soot.ValueBox>)stmt.getDefBoxes()) {
                soot.Value v = vb.getValue();
                if(rhsLocals.contains(v)) {
                	if(((PathsReachingFlowAnalysis.Box)pathsReachingFlowAnalysis.getFlowAfter(stmt)).getValue() == PathsReachingFlowAnalysis.MANY) {
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
