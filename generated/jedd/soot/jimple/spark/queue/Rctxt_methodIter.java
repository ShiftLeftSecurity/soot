package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public class Rctxt_methodIter extends Rctxt_method {
    protected Iterator r;
    
    public Rctxt_methodIter(Iterator r) {
        super();
        this.r = r;
    }
    
    public Iterator iterator() {
        return new Iterator() {
            public boolean hasNext() {
                boolean ret = r.hasNext();
                return ret;
            }
            
            public Object next() { return new Tuple((Context) r.next(), (SootMethod) r.next()); }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { ctxt.v(), method.v() },
                                              new PhysicalDomain[] { V1.v(), T1.v() },
                                              ("<soot.jimple.spark.bdddomains.ctxt:soot.jimple.spark.bdddoma" +
                                               "ins.V1, soot.jimple.spark.bdddomains.method:soot.jimple.spar" +
                                               "k.bdddomains.T1> ret = jedd.internal.Jedd.v().falseBDD(); at" +
                                               " /home/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/Rctxt" +
                                               "_methodIter.jedd:46,29-32"),
                                              jedd.internal.Jedd.v().falseBDD());
        while (r.hasNext()) {
            ret.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { r.next(), r.next() },
                                                       new Attribute[] { ctxt.v(), method.v() },
                                                       new PhysicalDomain[] { V1.v(), T1.v() }));
        }
        return new jedd.internal.RelationContainer(new Attribute[] { ctxt.v(), method.v() },
                                                   new PhysicalDomain[] { V1.v(), T1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rctxt_methodIter.jedd:50,8-14"),
                                                   ret);
    }
    
    public boolean hasNext() { return r.hasNext(); }
}
