package soot.jimple.toolkits.pointer;
import java.util.*;

/** A very naive object set that contains all objects ever created in the program. */
public class DumbObjectSet implements ObjectSet {
    /** Returns true if this set contains no run-time objects. */
    public boolean isEmpty() {
	return false;
    }
    /** Returns true if this set shares some objects with other. */
    public boolean hasNonEmptyIntersection( ObjectSet other ) {
	return true;
    }
    /** Iterator over all possible run-time types of objects in the set. */
    public Set possibleTypes() {
	throw new RuntimeException( "Not implemented" );
    }
}

