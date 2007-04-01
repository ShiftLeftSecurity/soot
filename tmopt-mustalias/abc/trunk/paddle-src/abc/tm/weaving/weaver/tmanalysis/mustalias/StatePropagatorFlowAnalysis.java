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
import soot.toolkits.graph.BriefUnitGraph;
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
 *  1.) Initial configuration: We have to make sure that we enter the initial shadow in this
 *    method with an initial configuration.
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
	private final BriefUnitGraph g;
	private boolean gaveUp;
	private final CallGraph abstractedCallGraph;
	private Map<String,Local> tmFormalToTmVar;
	private Map<Local,Local> adviceActualToTmVar;

	/**
	 * Computes possible states of <code>tm</code> in the given procedure.
	 */
	public StatePropagatorFlowAnalysis(TraceMatch tm, BriefUnitGraph g, CallGraph abstractedCallGraph) {
		super(g);
		this.g = g;
		this.abstractedCallGraph = abstractedCallGraph;
		this.meth = g.getBody().getMethod();
		this.gaveUp = false;

		this.traceMatch = tm;
		this.sm = tm.getStateMachine();
		this.adviceActualToTmVar = new HashMap<Local,Local>();
		this.tmFormalToTmVar = new HashMap<String, Local>();
		//for each bound advice local find the (hopefully unique?) local which is assigned to it
		//TODO we might want to make this a little more stable and failsafe

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
