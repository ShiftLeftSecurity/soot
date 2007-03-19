/** Propagates states and locals through a method. 
 * Assumes that everything involved is thread-local. */

package abc.tm.weaving.weaver.tmanalysis.mustalias;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
import abc.tm.weaving.weaver.tmanalysis.util.TransitionUtils;

/** StatePropagatorFlowAnalysis: Propagates sets of pairs (SMNode, List<Local>).
 * 
 * When a statement has multiple pairs associated with it, this abstractly
 * represents the fact that the automaton we're tracking may have multiple states
 * at that program point.
 *
 * @author Eric Bodden
 * @author Patrick Lam
 */
public class StatePropagatorFlowAnalysis extends ForwardFlowAnalysis {
	
	protected SootMethod meth;
	protected Shadow initialShadow;
	protected Stmt initialStmt;
	protected Set<SMNode> initialStates;
	protected boolean initializedInitial;
	private TraceMatch traceMatch;
	private final BriefUnitGraph g;
	private Collection<Local> boundLocals;
	private boolean gaveUp;
	private final CallGraph abstractedCallGraph;

	/**
	 * @param g
	 * @param initialShadow 
	 * @param abstractedCallGraph 
	 */
	public StatePropagatorFlowAnalysis(BriefUnitGraph g, Shadow initialShadow, CallGraph abstractedCallGraph) {
		super(g);
		this.g = g;
		this.abstractedCallGraph = abstractedCallGraph;
		this.meth = g.getBody().getMethod();
		this.initializedInitial = false;
		this.gaveUp = false;
		//find initial tracematch state (initialValue)
		this.initialStates = new HashSet<SMNode>();
		StateMachine stateMachine = initialShadow.getTraceMatch().getStateMachine();
		for (Iterator iterator = stateMachine.getStateIterator(); iterator.hasNext();) {
			SMNode state = (SMNode) iterator.next();
			if(state.isInitialNode()) {
				initialStates.add(state);
			}
		}
		//find initial statement
		for (Stmt s : (Collection<Stmt>)g.getBody().getUnits()) {
			for (Shadow ss : Shadow.allActiveShadowsForHost(s, meth)) {
				if (ss.equals(initialShadow)) {
					initialStmt = s;
				}
			}
		}
		assert initialStmt!=null;
		//
		this.traceMatch = initialShadow.getTraceMatch();
		this.boundLocals = new HashSet<Local>();
		
		for (Stmt s : (Collection<Stmt>)g.getBody().getUnits()) {
			for (ValueBox defBox : (Collection<ValueBox>)s.getDefBoxes()) {
				if(initialShadow.getBoundLocals().contains(defBox.getValue())) {
					AssignStmt assign = (AssignStmt) s;
					if(assign.getRightOp() instanceof Local) {
						this.boundLocals.add((Local)assign.getRightOp());
					} else {
						gaveUp = true;
					}
				}
			}
		}
		
		doAnalysis();	
	}

	/**
	 * We return true if <code>g</code> never causes the tracematch associated with <code>initialShadow</code>
	 * to hit a final state and g always leaves the tracematch automaton always in its initial configuration on exit. 
	 * @return
	 */
	public boolean isSafelyInvariant() {
		if(gaveUp) {
			return false;
		}
		
		//check that for each tail unit we are in the initial state
		for (Stmt tailUnit : (Collection<Stmt>)g.getTails()) {
			Collection<SMNode> flowAfter = (Collection<SMNode>) getFlowAfter(tailUnit);
			for (SMNode state : flowAfter) {
				if(!state.isInitialNode()) {
					return false;
				}
			}
		}
		//we know that if we ever hit a final state the former check is going to return false,
		//so we are done with checking here
		return true;
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

		//initialize when seeing the initial shadow
		if(s == initialStmt && !initializedInitial) {
			unitToBeforeFlow.put(s, initialStates);
			inVal = initialStates;
			initializedInitial = true;
		}

		Collection<SMNode> in = (Collection<SMNode>) inVal, out = (Collection<SMNode>) outVal;

		//if in is empty we have not seen the initial shadow yet and do not (yet) care about
		//definitions
		if(!in.isEmpty()) {
			for (ValueBox box : (Collection<ValueBox>)s.getDefBoxes()) {
				Value value = box.getValue();
				// 1. does s redefine the local we're depending on;
				if(boundLocals.contains(value)) {
					gaveUp = true;
				}
			}
		}
		
		out.clear();
		for (SMNode state : in) {
			Set<SMNode> successorStates = TransitionUtils.getSuccessorStatesFor(state,traceMatch,s,initialStates);
			for (SMNode succ : successorStates) {
				if(succ.isFinalNode()) {
					gaveUp = true;
					return;
				}
			}
			out.addAll(successorStates);			
		}
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
	protected boolean mayHaveSideEffects(Stmt s) {
		return abstractedCallGraph.edgesOutOf(s).hasNext();
	}
	
}
