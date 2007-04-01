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
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.StateMachine;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
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

    /* A tale of three maps. Two of the maps are here.
     * Definitions: 
     * 1) A tracematch formal is the name used within the
     * body of the tracematch. 
     * 2) A tracematch variable is the variable used by the 
     * weaver for the argument to the tracematch at the caller.
     * 3) An advice actual is the variable (local) used
     * in the caller for tracematch formals.
     *
     * An example:
     *   tracematch(Object a) { ... }
     *   ...
     *   x.foo() => becomes => tm$0 = x; tm$0.advice(); x.foo();
     *
     * a is the tracematch formal; x is the advice actual;
     * and tm$0 is the tracematch variable.
     *
     * So the two maps we have here are f: tm formal -> tm var
     * and g: advice actual -> tm var. The missing map is
     * h: tm formal -> advice actual, which exists at the level of the 
     * SymbolShadowMatch. We check that h \circ g = f.
     */
	private Map<String,Local> tmFormalToTmVar;
	private Map<Local,Local> adviceActualToTmVar;

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
		this.adviceActualToTmVar = new HashMap<Local,Local>();
		this.tmFormalToTmVar = new HashMap<String, Local>();

        // Attempt to compute the maps adviceActualToTmVar and
        // tmFormalToTmVar.
		Set<ShadowGroup> shadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
		for (ShadowGroup group : shadowGroups) {
			Set<Shadow> allShadows = group.getAllShadows();
            Set<String> seenAlready = new HashSet<String>();
			for (Shadow ss : allShadows) {
                if (ss.getTraceMatch() != this.traceMatch || !ss.getContainer().equals(meth))
                    continue;
                if (seenAlready.contains(ss.getUniqueShadowId())) continue;
                seenAlready.add(ss.getUniqueShadowId());
                for (Stmt s : (Collection<Stmt>)g.getBody().getUnits()) {
                    for (ValueBox defBox : (Collection<ValueBox>)s.getDefBoxes()) {
                        Value lValue = defBox.getValue();
                        if(ss.getBoundLocals().contains(lValue)) {
                            AssignStmt assign = (AssignStmt) s;
                            if(assign.getLeftOp() instanceof Local && assign.getRightOp() instanceof Local) {
                                Local lv = (Local) assign.getLeftOp(), rv = (Local) assign.getRightOp();
                                this.adviceActualToTmVar.put(lv, rv);						
                                String tmFormal = ss.getVarNameForLocal(lv);
                                tmFormalToTmVar.put(tmFormal, rv);
                            } else {
                                gaveUp = true;
                            }
                        }
                    }
                }
            }
        }
		
		doAnalysis();	
	}

	protected void flowThrough(Object inVal, Object stmt, Object outVal) {
		if(gaveUp) {
			return;
		}
		
		Stmt s = (Stmt) stmt;
		
		//if this statement may have sideeffects on the automaton configuration,
		//currently we just give up
		if(mayHaveSideEffects(s)) {
			gaveUp = true;
			return;
		}

		Collection<SMNode> in = (Collection<SMNode>) inVal, out = (Collection<SMNode>) outVal;

		//if in is empty we have not seen any shadows yet and do not (yet) care about
		//definitions
		if(!in.isEmpty()) {
			for (ValueBox box : (Collection<ValueBox>)s.getDefBoxes()) {
				Value value = box.getValue();
				// 1. does s redefine the local we're depending on;
				if(adviceActualToTmVar.containsValue(value)) {
					gaveUp = true;
				}
			}
		}
		
		out.clear();
		Collection<SMNode> successorStates = TransitionUtils.getSuccessorStatesFor
			(in, traceMatch, s, adviceActualToTmVar, tmFormalToTmVar);
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
