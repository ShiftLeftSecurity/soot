package soot.jimple.toolkits.pointer;
import java.util.*;

/** A generic interface to some set of runtime objects computed by a pointer analysis. */
public interface ObjectSet {
    /** Returns true if this set contains no run-time objects. */
    public boolean isEmpty();
    /** Returns true if this set is a subset of other. */
    public boolean hasNonEmptyIntersection( ObjectSet other );
    /** Set of all possible run-time types of objects in the set. */
    public Set possibleTypes();
}

