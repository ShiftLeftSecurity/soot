package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Rsrc_dstBDD extends Rsrc_dst {
    private final jedd.internal.RelationContainer bdd =
      new jedd.internal.RelationContainer(new Attribute[] { src.v(), dst.v() },
                                          new PhysicalDomain[] { V1.v(), V2.v() },
                                          ("private <soot.jimple.spark.bdddomains.src:soot.jimple.spark." +
                                           "bdddomains.V1, soot.jimple.spark.bdddomains.dst:soot.jimple." +
                                           "spark.bdddomains.V2> bdd = jedd.internal.Jedd.v().falseBDD()" +
                                           " at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/Rs" +
                                           "rc_dstBDD.jedd:31,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    void add(final jedd.internal.RelationContainer tuple) { bdd.eqUnion(tuple); }
    
    public Rsrc_dstBDD(final jedd.internal.RelationContainer bdd) {
        super();
        this.bdd.eq(bdd);
    }
    
    Rsrc_dstBDD() { super(); }
    
    public Iterator iterator() {
        ;
        final Iterator it =
          new jedd.internal.RelationContainer(new Attribute[] { dst.v(), src.v() },
                                              new PhysicalDomain[] { V2.v(), V1.v() },
                                              ("bdd.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-" +
                                               "2-jedd/src/soot/jimple/spark/queue/Rsrc_dstBDD.jedd:36,28"),
                                              bdd).iterator(new Attribute[] { src.v(), dst.v() });
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new Iterator() {
            public boolean hasNext() { return it.hasNext(); }
            
            public Object next() { return Rsrc_dstBDD.this.new Tuple((Object[]) it.next()); }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { src.v(), dst.v() },
                                              new PhysicalDomain[] { V1.v(), V2.v() },
                                              ("<soot.jimple.spark.bdddomains.src:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.dst:soot.jimple.spark.bd" +
                                               "ddomains.V2> ret = bdd; at /home/olhotak/soot-2-jedd/src/soo" +
                                               "t/jimple/spark/queue/Rsrc_dstBDD.jedd:47,8"),
                                              bdd);
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new jedd.internal.RelationContainer(new Attribute[] { dst.v(), src.v() },
                                                   new PhysicalDomain[] { V2.v(), V1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rsrc_dstBDD.jedd:49,8"),
                                                   ret);
    }
    
    public boolean hasNext() {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD());
    }
    
    private final class Tuple extends soot.jimple.spark.queue.Rsrc_dst.Tuple {
        private Object[] tuple;
        
        public Tuple(Object[] tuple) {
            super();
            this.tuple = tuple;
        }
        
        public VarNode src() { return (VarNode) tuple[0]; }
        
        public VarNode dst() { return (VarNode) tuple[1]; }
    }
    
}
