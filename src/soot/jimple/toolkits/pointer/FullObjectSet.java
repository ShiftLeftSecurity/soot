package soot.jimple.toolkits.pointer;
import java.util.*;

public class FullObjectSet implements ObjectSet {
    /** Returns true if this set contains no run-time objects. */
    public boolean isEmpty() {
	return false;
    }
    /** Returns true if this set is a subset of other. */
    public boolean hasNonEmptyIntersection( ObjectSet other ) {
	return other != null;
    }
    /** Set of all possible run-time types of objects in the set. */
    public Set possibleTypes() {
	throw new RuntimeException( "Not implemented" );
    }
}

