package soot.jimple.spark;

import soot.*;
import soot.util.*;
import soot.jimple.spark.queue.*;
import soot.jimple.spark.bdddomains.*;

public abstract class AbsTypeManager {
    protected Rvar vars;
    
    protected Robj allocs;
    
    AbsTypeManager(Rvar vars, Robj allocs) {
        super();
        this.vars = vars;
        this.allocs = allocs;
    }
    
    public abstract void update();
    
    public abstract BitVector get(Type type);
    
    public abstract jedd.internal.RelationContainer get();
    
    public abstract boolean castNeverFails(Type from, Type to);
}
