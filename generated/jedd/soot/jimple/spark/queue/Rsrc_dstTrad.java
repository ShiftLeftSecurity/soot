package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Rsrc_dstTrad extends Rsrc_dstIter {
    Rsrc_dstTrad(QueueReader r) { super(r); }
    
    public Rsrc_dstTrad copy() { return new Rsrc_dstTrad((QueueReader) ((QueueReader) r).clone()); }
}
