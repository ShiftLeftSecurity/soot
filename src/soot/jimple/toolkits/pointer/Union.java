package soot.jimple.toolkits.pointer;

/** A generic interface to some set of runtime objects computed by a pointer analysis. */
public abstract class Union implements ObjectSet {
    /** Adds all objects in s into this union of sets, returning true if this
     * union was changed. */
    public abstract boolean addAll( ObjectSet s );

    public static UnionFactory factory = null;
}

