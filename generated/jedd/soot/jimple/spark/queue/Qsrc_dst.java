package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public abstract class Qsrc_dst {
    public abstract void add(VarNode _src, VarNode _dst);
    
    public abstract void add(final jedd.internal.RelationContainer in);
    
    public abstract Rsrc_dst reader();
    
    public Qsrc_dst() { super(); }
}
