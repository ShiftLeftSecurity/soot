package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Rsrc_fld_dstBDD extends Rsrc_fld_dst {
    private final jedd.internal.RelationContainer bdd =
      new jedd.internal.RelationContainer(new Attribute[] { src.v(), fld.v(), dst.v() },
                                          new PhysicalDomain[] { V1.v(), FD.v(), V2.v() },
                                          ("private <soot.jimple.spark.bdddomains.src:soot.jimple.spark." +
                                           "bdddomains.V1, soot.jimple.spark.bdddomains.fld:soot.jimple." +
                                           "spark.bdddomains.FD, soot.jimple.spark.bdddomains.dst:soot.j" +
                                           "imple.spark.bdddomains.V2> bdd = jedd.internal.Jedd.v().fals" +
                                           "eBDD() at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/qu" +
                                           "eue/Rsrc_fld_dstBDD.jedd:31,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    void add(final jedd.internal.RelationContainer tuple) { bdd.eqUnion(tuple); }
    
    public Rsrc_fld_dstBDD(final jedd.internal.RelationContainer bdd) {
        super();
        this.bdd.eq(bdd);
    }
    
    Rsrc_fld_dstBDD() { super(); }
    
    public Iterator iterator() {
        ;
        final Iterator it =
          new jedd.internal.RelationContainer(new Attribute[] { dst.v(), src.v(), fld.v() },
                                              new PhysicalDomain[] { V2.v(), V1.v(), FD.v() },
                                              ("bdd.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-" +
                                               "2-jedd/src/soot/jimple/spark/queue/Rsrc_fld_dstBDD.jedd:36,2" +
                                               "8"),
                                              bdd).iterator(new Attribute[] { src.v(), fld.v(), dst.v() });
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new Iterator() {
            public boolean hasNext() { return it.hasNext(); }
            
            public Object next() { return Rsrc_fld_dstBDD.this.new Tuple((Object[]) it.next()); }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { src.v(), fld.v(), dst.v() },
                                              new PhysicalDomain[] { V1.v(), FD.v(), V2.v() },
                                              ("<soot.jimple.spark.bdddomains.src:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.fld:soot.jimple.spark.bd" +
                                               "ddomains.FD, soot.jimple.spark.bdddomains.dst:soot.jimple.sp" +
                                               "ark.bdddomains.V2> ret = bdd; at /home/olhotak/soot-2-jedd/s" +
                                               "rc/soot/jimple/spark/queue/Rsrc_fld_dstBDD.jedd:47,8"),
                                              bdd);
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new jedd.internal.RelationContainer(new Attribute[] { dst.v(), src.v(), fld.v() },
                                                   new PhysicalDomain[] { V2.v(), V1.v(), FD.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rsrc_fld_dstBDD.jedd:49,8"),
                                                   ret);
    }
    
    public boolean hasNext() {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD());
    }
    
    private final class Tuple extends soot.jimple.spark.queue.Rsrc_fld_dst.Tuple {
        private Object[] tuple;
        
        public Tuple(Object[] tuple) {
            super();
            this.tuple = tuple;
        }
        
        public VarNode src() { return (VarNode) tuple[0]; }
        
        public SparkField fld() { return (SparkField) tuple[1]; }
        
        public VarNode dst() { return (VarNode) tuple[2]; }
    }
    
}
