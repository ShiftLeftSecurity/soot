package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class RobjTrad extends RobjIter {
    RobjTrad(QueueReader r) { super(r); }
    
    public RobjTrad copy() { return new RobjTrad((QueueReader) ((QueueReader) r).clone()); }
}
