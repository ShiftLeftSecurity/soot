package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Rsrcc_srcm_stmt_kind_tgtc_tgtmTrad extends Rsrcc_srcm_stmt_kind_tgtc_tgtmIter {
    Rsrcc_srcm_stmt_kind_tgtc_tgtmTrad(QueueReader r) { super(r); }
    
    public Rsrcc_srcm_stmt_kind_tgtc_tgtmTrad copy() {
        return new Rsrcc_srcm_stmt_kind_tgtc_tgtmTrad((QueueReader) ((QueueReader) r).clone());
    }
}
