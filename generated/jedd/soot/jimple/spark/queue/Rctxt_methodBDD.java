package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Rctxt_methodBDD extends Rctxt_method {
    private final jedd.internal.RelationContainer bdd =
      new jedd.internal.RelationContainer(new Attribute[] { ctxt.v(), method.v() },
                                          new PhysicalDomain[] { V1.v(), T1.v() },
                                          ("private <soot.jimple.spark.bdddomains.ctxt:soot.jimple.spark" +
                                           ".bdddomains.V1, soot.jimple.spark.bdddomains.method:soot.jim" +
                                           "ple.spark.bdddomains.T1> bdd = jedd.internal.Jedd.v().falseB" +
                                           "DD() at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/queu" +
                                           "e/Rctxt_methodBDD.jedd:31,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    void add(final jedd.internal.RelationContainer tuple) { bdd.eqUnion(tuple); }
    
    public Rctxt_methodBDD(final jedd.internal.RelationContainer bdd) {
        super();
        this.bdd.eq(bdd);
    }
    
    Rctxt_methodBDD() { super(); }
    
    public Iterator iterator() {
        ;
        final Iterator it =
          new jedd.internal.RelationContainer(new Attribute[] { ctxt.v(), method.v() },
                                              new PhysicalDomain[] { V1.v(), T1.v() },
                                              ("bdd.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-" +
                                               "2-jedd/src/soot/jimple/spark/queue/Rctxt_methodBDD.jedd:36,2" +
                                               "8"),
                                              bdd).iterator(new Attribute[] { ctxt.v(), method.v() });
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new Iterator() {
            public boolean hasNext() { return it.hasNext(); }
            
            public Object next() { return Rctxt_methodBDD.this.new Tuple((Object[]) it.next()); }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { ctxt.v(), method.v() },
                                              new PhysicalDomain[] { V1.v(), T1.v() },
                                              ("<soot.jimple.spark.bdddomains.ctxt:soot.jimple.spark.bdddoma" +
                                               "ins.V1, soot.jimple.spark.bdddomains.method:soot.jimple.spar" +
                                               "k.bdddomains.T1> ret = bdd; at /home/olhotak/soot-2-jedd/src" +
                                               "/soot/jimple/spark/queue/Rctxt_methodBDD.jedd:47,8"),
                                              bdd);
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new jedd.internal.RelationContainer(new Attribute[] { ctxt.v(), method.v() },
                                                   new PhysicalDomain[] { V1.v(), T1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rctxt_methodBDD.jedd:49,8"),
                                                   ret);
    }
    
    public boolean hasNext() {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD());
    }
    
    private final class Tuple extends soot.jimple.spark.queue.Rctxt_method.Tuple {
        private Object[] tuple;
        
        public Tuple(Object[] tuple) {
            super();
            this.tuple = tuple;
        }
        
        public Context ctxt() { return (Context) tuple[0]; }
        
        public SootMethod method() { return (SootMethod) tuple[1]; }
    }
    
}
