package soot.jimple.toolkits.pointer;
import soot.*;
import soot.jimple.*;

/** A very naive pointer analysis that just reports that any points can point
 * to any object. */
public class DumbPointerAnalysis implements PointerAnalysis {

    /** Returns the set of objects reaching variable l before stmt in method. */
    public ObjectSet reachingObjects( SootMethod method, Stmt stmt,
	    Local l ) {
	return new DumbObjectSet();
    }
}

