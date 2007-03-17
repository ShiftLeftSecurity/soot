/** Propagates states and locals through a method. 
 * Assumes that everything involved is thread-local. */

package abc.tm.weaving.weaver.tmanalysis.mustalias;

import java.util.List;
import java.util.Map;

import soot.*;
import soot.jimple.*;
import soot.toolkits.scalar.*;
import soot.toolkits.graph.UnitGraph;

import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;

public class StatePropagatorFlowAnalysis extends ForwardFlowAnalysis {
    SootMethod meth;
    Shadow initialShadow; SmMaPair initialValue;
    boolean initializedInitial;

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
	SmMaPair in = (SmMaPair)inVal, out = (SmMaPair)outVal;

	Stmt s = (Stmt) stmt;

	for (Shadow ss : Shadow.allActiveShadowsForHost(s, meth)) {
	    if (!initializedInitial && ss.equals(initialShadow)) {
		unitToBeforeFlow.put(s, initialValue);
		inVal = initialValue;
	    }
	}

	// 1. does s redefine the local we're depending on;
	out.updateForPotentialWrites(in, s);

	// 2. does s change the state of the tracematch automaton?
	// (In general, we'd like to match stmts with possible effects on the tm automaton.)
	for (Shadow ss : Shadow.allActiveShadowsForHost(s, meth)) {
	    // retrieve the SMEdge for ss.
	}
    }

    protected Object newInitialFlow() {
	return new SmMaPair (null, null);
    }

    protected Object entryInitialFlow() {
	return newInitialFlow();
    }

    public void copy(Object src, Object dest) {
	SmMaPair s = (SmMaPair)src, d = (SmMaPair)dest;
	d.copyFrom(s);
    }

    public void merge(Object i1, Object i2, Object o) {
	SmMaPair in1 = (SmMaPair)i1, in2 = (SmMaPair)i2;
	SmMaPair out = (SmMaPair)o;

	if (!in1.equals(in2))
	    out.sendToBottom();
	else
	    out.copyFrom(in1);
    }

    /** Analysis abstraction object: tracks a tracematch state and
     * a must-alias object. 
     */
    public static class SmMaPair {
	SMNode s;
	
	/** For now, use Soot locals; change this if we need to. */
	List<Local> l; 

	boolean hitFinal;

	SmMaPair (SMNode s, List<Local> l) { 
	    this.s = s; this.l = l; 
	    this.hitFinal = false;
	}

	void sendToBottom() {
	    this.s = null; this.l = null; this.hitFinal = false;
	}

	void copyFrom(SmMaPair in) {
	    this.s = in.s; this.l = in.l; this.hitFinal = in.hitFinal;
	}

	void updateForPotentialWrites (SmMaPair in, Stmt s) {
	    boolean badness = false;

	    if (in.s == null || in.l == null) {
		sendToBottom();
		return;
	    }

	    for (Local l : in.l)
		if (s.getDefBoxes().contains(l))
		    badness = true;

	    if (badness) {
		sendToBottom();
	    } else {
		copyFrom(in);
	    }
	}

	public boolean equals(Object o) {
	    if (!(o instanceof SmMaPair)) return false;
	    SmMaPair p = (SmMaPair) o;
	    return p.s == s && p.l == l && p.hitFinal == hitFinal;
	}
    }

    public static SmMaPair newSmMaPair(SMNode s, List<Local> l) { return new SmMaPair(s, l); }
}

