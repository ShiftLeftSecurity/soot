package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public abstract class Qlocal_srcm_stmt_signature_kind {
    public abstract void add(Local _local, SootMethod _srcm, Unit _stmt, NumberedString _signature, Kind _kind);
    
    public abstract void add(final jedd.internal.RelationContainer in);
    
    public abstract Rlocal_srcm_stmt_signature_kind reader();
    
    public Qlocal_srcm_stmt_signature_kind() { super(); }
}
