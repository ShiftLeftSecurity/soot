package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Robj_varBDD extends Robj_var {
    private final jedd.internal.RelationContainer bdd =
      new jedd.internal.RelationContainer(new Attribute[] { obj.v(), var.v() },
                                          new PhysicalDomain[] { H1.v(), V1.v() },
                                          ("private <soot.jimple.spark.bdddomains.obj:soot.jimple.spark." +
                                           "bdddomains.H1, soot.jimple.spark.bdddomains.var:soot.jimple." +
                                           "spark.bdddomains.V1> bdd = jedd.internal.Jedd.v().falseBDD()" +
                                           " at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/Ro" +
                                           "bj_varBDD.jedd:31,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    void add(final jedd.internal.RelationContainer tuple) { bdd.eqUnion(tuple); }
    
    public Robj_varBDD(final jedd.internal.RelationContainer bdd) {
        super();
        this.bdd.eq(bdd);
    }
    
    Robj_varBDD() { super(); }
    
    public Iterator iterator() {
        ;
        final Iterator it =
          new jedd.internal.RelationContainer(new Attribute[] { obj.v(), var.v() },
                                              new PhysicalDomain[] { H1.v(), V1.v() },
                                              ("bdd.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-" +
                                               "2-jedd/src/soot/jimple/spark/queue/Robj_varBDD.jedd:36,28"),
                                              bdd).iterator(new Attribute[] { obj.v(), var.v() });
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new Iterator() {
            public boolean hasNext() { return it.hasNext(); }
            
            public Object next() { return Robj_varBDD.this.new Tuple((Object[]) it.next()); }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { obj.v(), var.v() },
                                              new PhysicalDomain[] { H1.v(), V1.v() },
                                              ("<soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bdddomai" +
                                               "ns.H1, soot.jimple.spark.bdddomains.var:soot.jimple.spark.bd" +
                                               "ddomains.V1> ret = bdd; at /home/olhotak/soot-2-jedd/src/soo" +
                                               "t/jimple/spark/queue/Robj_varBDD.jedd:47,8"),
                                              bdd);
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new jedd.internal.RelationContainer(new Attribute[] { obj.v(), var.v() },
                                                   new PhysicalDomain[] { H1.v(), V1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Robj_varBDD.jedd:49,8"),
                                                   ret);
    }
    
    public boolean hasNext() {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD());
    }
    
    private final class Tuple extends soot.jimple.spark.queue.Robj_var.Tuple {
        private Object[] tuple;
        
        public Tuple(Object[] tuple) {
            super();
            this.tuple = tuple;
        }
        
        public AllocNode obj() { return (AllocNode) tuple[0]; }
        
        public VarNode var() { return (VarNode) tuple[1]; }
    }
    
}
