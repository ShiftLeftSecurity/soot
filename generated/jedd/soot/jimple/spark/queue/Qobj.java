package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public abstract class Qobj {
    public abstract void add(AllocNode _obj);
    
    public abstract void add(final jedd.internal.RelationContainer in);
    
    public abstract Robj reader();
    
    public Qobj() { super(); }
}
