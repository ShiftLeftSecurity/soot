package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Qctxt_methodBDD extends Qctxt_method {
    private LinkedList readers = new LinkedList();
    
    public void add(Context _ctxt, SootMethod _method) {
        this.add(new jedd.internal.RelationContainer(new Attribute[] { ctxt.v(), method.v() },
                                                     new PhysicalDomain[] { V1.v(), T1.v() },
                                                     ("this.add(jedd.internal.Jedd.v().literal(new java.lang.Object" +
                                                      "[...], new jedd.Attribute[...], new jedd.PhysicalDomain[...]" +
                                                      ")) at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/" +
                                                      "Qctxt_methodBDD.jedd:33,8"),
                                                     jedd.internal.Jedd.v().literal(new Object[] { _ctxt, _method },
                                                                                    new Attribute[] { ctxt.v(), method.v() },
                                                                                    new PhysicalDomain[] { V1.v(), T1.v() })));
    }
    
    public void add(final jedd.internal.RelationContainer in) {
        for (Iterator it = readers.iterator(); it.hasNext(); ) {
            Rctxt_methodBDD reader = (Rctxt_methodBDD) it.next();
            reader.add(new jedd.internal.RelationContainer(new Attribute[] { ctxt.v(), method.v() },
                                                           new PhysicalDomain[] { V1.v(), T1.v() },
                                                           ("reader.add(in) at /home/olhotak/soot-2-jedd/src/soot/jimple/" +
                                                            "spark/queue/Qctxt_methodBDD.jedd:38,12"),
                                                           in));
        }
    }
    
    public Rctxt_method reader() {
        Rctxt_method ret = new Rctxt_methodBDD();
        readers.add(ret);
        return ret;
    }
    
    public Qctxt_methodBDD() { super(); }
}
