package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public abstract class Qvar_obj {
    public abstract void add(VarNode _var, AllocNode _obj);
    
    public abstract void add(final jedd.internal.RelationContainer in);
    
    public abstract Rvar_obj reader();
    
    public Qvar_obj() { super(); }
}
