/** Propagates states and locals through a method. 
 * Assumes that everything involved is thread-local. */

package abc.tm.weaving.weaver.tmanalysis.mustalias;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import soot.Local;
import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;

public class StatePropagatorFlowAnalysis extends ForwardFlowAnalysis {
	
	protected SootMethod meth;
	protected Shadow initialShadow;
	protected SmMaPair initialValue;
	protected boolean initializedInitial;

	public StatePropagatorFlowAnalysis(SootMethod m, UnitGraph g,
			Shadow initialShadow, SmMaPair iv,
			Map<String, SMEdge> shadowIdsToSMEdges) {
		super(g);

		this.meth = m;
		// We cannot put iv into unitBeforeFlow, it'll get trampled.
		this.initialValue = iv;
		this.initializedInitial = false;
		// bah, need to get the initial stmt for the Shadow initialShadow

		doAnalysis();
	}

	protected void flowThrough(Object inVal, Object stmt, Object outVal) {
		SmMaPair in = (SmMaPair) inVal, out = (SmMaPair) outVal;

		Stmt s = (Stmt) stmt;

		//initialize when seeing the initial shadow
		//TODO cache those shadows
		for (Shadow ss : Shadow.allActiveShadowsForHost(s, meth)) {
			if (!initializedInitial && ss.equals(initialShadow)) {
				unitToBeforeFlow.put(s, initialValue);
				inVal = initialValue;
			}
		}

		// 1. does s redefine the local we're depending on;
		out.updateForPotentialWrites(in, s);

		// 2. does s change the state of the tracematch automaton?
		// (In general, we'd like to match stmts with possible effects on the tm
		// automaton.)
		for (Shadow ss : Shadow.allActiveShadowsForHost(s, meth)) {
			// retrieve the SMEdge for ss.
		}
	}

	protected Object newInitialFlow() {
		return new SmMaPair(null, null);
	}

	protected Object entryInitialFlow() {
		return newInitialFlow();
	}

	public void copy(Object src, Object dest) {
		SmMaPair s = (SmMaPair) src, d = (SmMaPair) dest;
		d.copyFrom(s);
	}

	public void merge(Object i1, Object i2, Object o) {
		SmMaPair in1 = (SmMaPair) i1, in2 = (SmMaPair) i2;
		SmMaPair out = (SmMaPair) o;

		if (!in1.equals(in2))
			out.sendToBottom();
		else
			out.copyFrom(in1);
	}

	/**
	 * Analysis abstraction object: tracks a tracematch state and a must-alias
	 * object.
	 */
	public static class SmMaPair {
		SMNode s;

		/** For now, use Soot locals; change this if we need to. */
		List<Local> l;

		boolean hitFinal;

		SmMaPair(SMNode s, List<Local> l) {
			this.s = s;
			this.l = l;
			this.hitFinal = false;
		}

		void sendToBottom() {
			this.s = null;
			this.l = null;
			this.hitFinal = false;
		}

		void copyFrom(SmMaPair in) {
			this.s = in.s;
			this.l = in.l;
			this.hitFinal = in.hitFinal;
		}

		void updateForPotentialWrites(SmMaPair in, Stmt s) {
			boolean badness = false;

			if (in.s == null || in.l == null) {
				sendToBottom();
				return;
			}

			for (Local l : in.l)
				for (ValueBox box : (Collection<ValueBox>)s.getDefBoxes()) {
					if(box.getValue().equals(l)) {
						badness = true;
					}
				}

			if (badness) {
				sendToBottom();
			} else {
				copyFrom(in);
			}
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (hitFinal ? 1231 : 1237);
			result = prime * result + ((l == null) ? 0 : l.hashCode());
			result = prime * result + ((s == null) ? 0 : s.hashCode());
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final SmMaPair other = (SmMaPair) obj;
			if (hitFinal != other.hitFinal)
				return false;
			if (l == null) {
				if (other.l != null)
					return false;
			} else if (!l.equals(other.l))
				return false;
			if (s == null) {
				if (other.s != null)
					return false;
			} else if (!s.equals(other.s))
				return false;
			return true;
		}

	}

	public static SmMaPair newSmMaPair(SMNode s, List<Local> l) {
		return new SmMaPair(s, l);
	}
}
