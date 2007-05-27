/** Propagates states and locals through a method. 
 * Assumes that everything involved is thread-local. */

package abc.tm.weaving.weaver.tmanalysis.mustalias;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.jimple.toolkits.callgraph.Edge;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.StateMachine;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.query.ReachableShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.stages.CallGraphAbstraction;
import abc.tm.weaving.weaver.tmanalysis.util.TransitionUtils;

/** StatePropagatorFlowAnalysis: Propagates sets of SMNodes.
 * 
 * When a statement has multiple SMNodes associated with it, this abstractly
 * represents the fact that the automaton we're tracking may have multiple states
 * at that program point.
 *
 * FIXME This implementation is still not 100% sound, at least for the following reasons:
 * 
 *  2.) Thread safety: Right now, we do not take threads into account.
 *
 * @author Eric Bodden
 * @author Patrick Lam
 */
public class StatePropagatorFlowAnalysis extends ForwardFlowAnalysis {
	
	protected SootMethod meth;
	protected StateMachine sm;
	private TraceMatch traceMatch;
	private final UnitGraph g;
    /** True if we ever hit final state (or have to give up for some reason) */
	private boolean gaveUp;
	private final CallGraph abstractedCallGraph;
    private LocalMustAliasAnalysis lma;
    private LocalNotMayAliasAnalysis lnma;
    private PathsReachingFlowAnalysis prf;

    /* Pick out a shadow associated with the given tracematch and in the appropriate method.
     * Use it to determine the bindings that we're tracking. */
    /* In principle, we ought to try all possible principalShadows. */
    private Shadow principalShadow;
    private Collection<Stmt> principalShadowDefs;
    private HashMap<Value, Stmt> principalShadowDefMap;

	/**
	 * Computes possible states of <code>tm</code> in the given procedure, based on the bindings for shadow <code>s</code>.
	 */
	public StatePropagatorFlowAnalysis(TraceMatch tm, Shadow s, UnitGraph g, CallGraph abstractedCallGraph) {
		super(g);
		this.g = g;
		this.abstractedCallGraph = abstractedCallGraph;
		this.meth = g.getBody().getMethod();
		this.gaveUp = false; 

		this.traceMatch = tm;
		this.sm = tm.getStateMachine();
        this.principalShadow = s;
		this.lma = new LocalMustAliasAnalysis(g);
        this.lnma = new LocalNotMayAliasAnalysis(g);
        this.prf = new PathsReachingFlowAnalysis(g);

        // dump the woven body...
//           java.io.PrintWriter pw = new java.io.PrintWriter(System.out);
//           soot.Printer.v().printTo(g.getBody(), pw);
//           pw.close();

			// for after-flow-ins analysis
        // 	Set<ShadowGroup> shadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
        // 	for (ShadowGroup group : shadowGroups) {
        //	Set<Shadow> allShadows = group.getAllShadows();

			// for quick-state-prop
 		Set<Shadow> allShadows = ReachableShadowFinder.v().reachableShadows(abstractedCallGraph);

        /*
        int maxBindingCount = -1;
        for (Shadow ss : allShadows) {
            if (ss.getTraceMatch() != this.traceMatch || !ss.getContainer().equals(meth))
                continue;
            
            if (ss.getBoundLocals().size() > maxBindingCount) {
                principalShadow = ss;
                maxBindingCount = ss.getBoundLocals().size();
        */
        principalShadowDefs = new java.util.LinkedList();
        principalShadowDefMap = new java.util.HashMap();

        Collection seenBoundLocals = new java.util.LinkedList();
                
        for (Stmt u : (Collection<Stmt>)g.getBody().getUnits()) {
            for (soot.ValueBox vb : (Collection<soot.ValueBox>)u.getDefBoxes()) {
                soot.Value v = vb.getValue();
                if (principalShadow.getBoundLocals().contains(v)) {
                    if (prf.getFlowAfter(u) == PathsReachingFlowAnalysis.MANY) {
                        gaveUp = true;
                    }
                    if (seenBoundLocals.contains(v)) {
                        gaveUp = true;
                    }
                    seenBoundLocals.add(v);
                    principalShadowDefMap.put(v, (Stmt)(g.getSuccsOf(u).get(0)));
                    principalShadowDefs.add(u);
                }
            }
        }

        /*
            }
        }
        */

        // quick-state-prop
        // 		}

        System.out.println("principalShadow's bound variables: "+principalShadow.getBoundLocals());
		
		doAnalysis();
    }

	protected void flowThrough(Object inVal, Object stmt, Object outVal) {
		if(gaveUp) {
			return;
		}
		
		Collection<SMNode> in = (Collection<SMNode>) inVal, out = (Collection<SMNode>) outVal;
		Stmt s = (Stmt) stmt;

		out.clear();
		Collection<SMNode> successorStates = TransitionUtils.getSuccessorStatesFor
			(in, traceMatch, principalShadow, principalShadowDefMap, s, lma, lnma);
		for (SMNode succ : successorStates) {
			if(succ.isFinalNode()) {
                System.out.println("gave up: final");
				gaveUp = true;
				return;
			}
		}
		out.addAll(successorStates);
	}

	protected Object newInitialFlow() {
		return new HashSet<SMNode>();
	}

	protected Object entryInitialFlow() {
		return newInitialFlow();
	}

	protected void copy(Object src, Object dest) {
		Collection s = (Collection) src, d = (Collection) dest;
		d.clear();
		d.addAll(s);
	}

	protected void merge(Object i1, Object i2, Object o) {
		Collection in1 = (Collection) i1, in2 = (Collection) i2;
		Collection out = (Collection) o;

		out.clear();
		out.addAll(in1);
		out.addAll(in2);

        if (!in1.equals(in2)) {
            // actually we should do something at flowThrough
            // when we have a set of output nodes or something.
            // Except when we have an initial flow. Then it's fine.
//             System.out.println("unequal merges");
            //            gaveUp = true;
        }
	}

	/**
	 * Returns <code>true</code> if <code>s</code> may have any sideeffects on a tracematch automaton, i.e.
	 * if the statement could transitively cause any shadow to be triggered.
	 * This information is computed using the abstractedCallGraph.
	 * @param s any statement
	 */
	private boolean mayHaveSideEffects(Stmt s) {
		return abstractedCallGraph.edgesOutOf(s).hasNext();
	}
}
