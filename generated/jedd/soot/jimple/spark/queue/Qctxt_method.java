package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public abstract class Qctxt_method {
    public abstract void add(Context _ctxt, SootMethod _method);
    
    public abstract void add(final jedd.internal.RelationContainer in);
    
    public abstract Rctxt_method reader();
    
    public Qctxt_method() { super(); }
}
