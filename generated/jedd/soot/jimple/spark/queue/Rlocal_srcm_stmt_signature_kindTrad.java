package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Rlocal_srcm_stmt_signature_kindTrad extends Rlocal_srcm_stmt_signature_kindIter {
    Rlocal_srcm_stmt_signature_kindTrad(QueueReader r) { super(r); }
    
    public Rlocal_srcm_stmt_signature_kindTrad copy() {
        return new Rlocal_srcm_stmt_signature_kindTrad((QueueReader) ((QueueReader) r).clone());
    }
}
