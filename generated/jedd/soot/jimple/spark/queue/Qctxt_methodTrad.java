package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public class Qctxt_methodTrad extends Qctxt_method {
    private ChunkedQueue q = new ChunkedQueue();
    
    public void add(Context _ctxt, SootMethod _method) {
        q.add(_ctxt);
        q.add(_method);
    }
    
    public void add(final jedd.internal.RelationContainer in) {
        Iterator it =
          new jedd.internal.RelationContainer(new Attribute[] { ctxt.v(), method.v() },
                                              new PhysicalDomain[] { V1.v(), T1.v() },
                                              ("in.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-2" +
                                               "-jedd/src/soot/jimple/spark/queue/Qctxt_methodTrad.jedd:37,2" +
                                               "2"),
                                              in).iterator(new Attribute[] { ctxt.v(), method.v() });
        while (it.hasNext()) {
            Object[] tuple = (Object[]) it.next();
            for (int i = 0; i < 2; i++) { q.add(tuple[i]); }
        }
    }
    
    public Rctxt_method reader() { return new Rctxt_methodTrad(q.reader()); }
    
    public Qctxt_methodTrad() { super(); }
}
