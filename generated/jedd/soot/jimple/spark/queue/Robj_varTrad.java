package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Robj_varTrad extends Robj_varIter {
    Robj_varTrad(QueueReader r) { super(r); }
    
    public Robj_varTrad copy() { return new Robj_varTrad((QueueReader) ((QueueReader) r).clone()); }
}
