package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Rlocal_srcm_stmt_tgtmTrad extends Rlocal_srcm_stmt_tgtmIter {
    Rlocal_srcm_stmt_tgtmTrad(QueueReader r) { super(r); }
    
    public Rlocal_srcm_stmt_tgtmTrad copy() {
        return new Rlocal_srcm_stmt_tgtmTrad((QueueReader) ((QueueReader) r).clone());
    }
}
