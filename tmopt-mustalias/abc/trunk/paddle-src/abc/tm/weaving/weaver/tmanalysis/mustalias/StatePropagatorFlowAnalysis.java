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
	protected Shadow initialShadow;
	protected Stmt initialStmt;
	protected StateMachine sm;
	private TraceMatch traceMatch;
	private final UnitGraph g;
    /** True if we ever hit final state (or have to give up for some reason) */
	private boolean gaveUp;
	private final CallGraph abstractedCallGraph;
    private LocalMustAliasAnalysis lma;

    /* Pick out a shadow associated with the given tracematch and in the appropriate method.
     * Use it to determine the bindings that we're tracking. */
    /* In principle, we ought to try all possible principalShadows. */
    private Shadow principalShadow;

	/**
	 * Computes possible states of <code>tm</code> in the given procedure.
     * (todo:) Checks that only one binding of tracematch formals occurs in this
     * procedure and that the bound advice actuals are never written to.
	 */
	public StatePropagatorFlowAnalysis(TraceMatch tm, UnitGraph g, CallGraph abstractedCallGraph) {
		super(g);
		this.g = g;
		this.abstractedCallGraph = abstractedCallGraph;
		this.meth = g.getBody().getMethod();
		this.gaveUp = false; 

		this.traceMatch = tm;
		this.sm = tm.getStateMachine();
		this.lma = new LocalMustAliasAnalysis(g);

		Set<ShadowGroup> shadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
		for (ShadowGroup group : shadowGroups) {
			Set<Shadow> allShadows = group.getAllShadows();
            Set<String> seenAlready = new HashSet<String>();
			for (Shadow ss : allShadows) {
                if (ss.getTraceMatch() != this.traceMatch || !ss.getContainer().equals(meth))
                    continue;
                if (seenAlready.contains(ss.getUniqueShadowId())) continue;
                seenAlready.add(ss.getUniqueShadowId());

                // Pick the first shadow we see...
                principalShadow = ss;
                break;
            }
        }
		
		doAnalysis();
    }

	protected void flowThrough(Object inVal, Object stmt, Object outVal) {
		if(gaveUp) {
			return;
		}
		
		Collection<SMNode> in = (Collection<SMNode>) inVal, out = (Collection<SMNode>) outVal;
		Stmt s = (Stmt) stmt;
		
		// This check verifies that stmt s does not redefine the
		// variables which we're tracking.  However, if
		// <code>in</code> is empty, we have not seen any shadows yet
		// and hence do not (yet) care about definitions
		if(!in.isEmpty()) {
			for (ValueBox box : (Collection<ValueBox>)s.getDefBoxes()) {
				Value value = box.getValue();
				// 1. does s redefine the local we're depending on;
                for (Local l : principalShadow.getBoundLocals()) {
                    if(l == value) {
                        gaveUp = true;
                        return;
                    }
				}
			}
		}
		
		out.clear();
		Collection<SMNode> successorStates = TransitionUtils.getSuccessorStatesFor
			(in, traceMatch, principalShadow, s, lma);
		for (SMNode succ : successorStates) {
			if(succ.isFinalNode()) {
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
