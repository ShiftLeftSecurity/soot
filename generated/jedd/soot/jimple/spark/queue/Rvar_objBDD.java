package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Rvar_objBDD extends Rvar_obj {
    private final jedd.internal.RelationContainer bdd =
      new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v() },
                                          new PhysicalDomain[] { V1.v(), H1.v() },
                                          ("private <soot.jimple.spark.bdddomains.var:soot.jimple.spark." +
                                           "bdddomains.V1, soot.jimple.spark.bdddomains.obj:soot.jimple." +
                                           "spark.bdddomains.H1> bdd = jedd.internal.Jedd.v().falseBDD()" +
                                           " at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/Rv" +
                                           "ar_objBDD.jedd:31,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    void add(final jedd.internal.RelationContainer tuple) { bdd.eqUnion(tuple); }
    
    public Rvar_objBDD(final jedd.internal.RelationContainer bdd) {
        super();
        this.bdd.eq(bdd);
    }
    
    Rvar_objBDD() { super(); }
    
    public Iterator iterator() {
        ;
        final Iterator it =
          new jedd.internal.RelationContainer(new Attribute[] { obj.v(), var.v() },
                                              new PhysicalDomain[] { H1.v(), V1.v() },
                                              ("bdd.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-" +
                                               "2-jedd/src/soot/jimple/spark/queue/Rvar_objBDD.jedd:36,28"),
                                              bdd).iterator(new Attribute[] { var.v(), obj.v() });
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new Iterator() {
            public boolean hasNext() { return it.hasNext(); }
            
            public Object next() { return Rvar_objBDD.this.new Tuple((Object[]) it.next()); }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v() },
                                              new PhysicalDomain[] { V1.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1> ret = bdd; at /home/olhotak/soot-2-jedd/src/soo" +
                                               "t/jimple/spark/queue/Rvar_objBDD.jedd:47,8"),
                                              bdd);
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new jedd.internal.RelationContainer(new Attribute[] { obj.v(), var.v() },
                                                   new PhysicalDomain[] { H1.v(), V1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rvar_objBDD.jedd:49,8"),
                                                   ret);
    }
    
    public boolean hasNext() {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD());
    }
    
    private final class Tuple extends soot.jimple.spark.queue.Rvar_obj.Tuple {
        private Object[] tuple;
        
        public Tuple(Object[] tuple) {
            super();
            this.tuple = tuple;
        }
        
        public VarNode var() { return (VarNode) tuple[0]; }
        
        public AllocNode obj() { return (AllocNode) tuple[1]; }
    }
    
}
