package soot.jimple.toolkits.pointer;
import soot.*;
import soot.jimple.*;

/** A generic interface to any type of pointer analysis. */
public interface PointerAnalysis {
    public static final Integer THIS_NODE = new Integer( -1 );
    public static final Integer RETURN_NODE = new Integer( -2 );
    public static final Integer THROW_NODE = new Integer( -3 );
    public static final Integer ARRAY_ELEMENTS_NODE = new Integer( -4 );
    public static final Integer CAST_NODE = new Integer( -5 );

    /** Returns the set of objects reaching variable l before stmt in method. */
    public ObjectSet reachingObjects( SootMethod method, Stmt stmt,
	    Local l );
}

