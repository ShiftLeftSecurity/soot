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
                                           "imple.spark.bdddomains.V2> bdd at /home/olhotak/soot-2-jedd/" +
                                           "src/soot/jimple/spark/queue/Rsrc_fld_dstBDD.jedd:31,12-36"));
    
    void add(final jedd.internal.RelationContainer tuple) { bdd.eqUnion(tuple); }
    
    public Rsrc_fld_dstBDD(final jedd.internal.RelationContainer bdd) {
        this();
        this.add(new jedd.internal.RelationContainer(new Attribute[] { fld.v(), dst.v(), src.v() },
                                                     new PhysicalDomain[] { FD.v(), V2.v(), V1.v() },
                                                     ("this.add(bdd) at /home/olhotak/soot-2-jedd/src/soot/jimple/s" +
                                                      "park/queue/Rsrc_fld_dstBDD.jedd:33,69-72"),
                                                     bdd));
    }
    
    Rsrc_fld_dstBDD() {
        super();
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
    }
    
    public Iterator iterator() {
        ;
        return new Iterator() {
            private Iterator it;
            
            public boolean hasNext() {
                if (it != null && it.hasNext()) return true;
                if (!jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD()))
                    return true;
                return false;
            }
            
            public Object next() {
                if (it == null || !it.hasNext()) {
                    it =
                      new jedd.internal.RelationContainer(new Attribute[] { fld.v(), dst.v(), src.v() },
                                                          new PhysicalDomain[] { FD.v(), V2.v(), V1.v() },
                                                          ("bdd.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-" +
                                                           "2-jedd/src/soot/jimple/spark/queue/Rsrc_fld_dstBDD.jedd:45,2" +
                                                           "5-28"),
                                                          bdd).iterator(new Attribute[] { src.v(), fld.v(), dst.v() });
                    bdd.eq(jedd.internal.Jedd.v().falseBDD());
                }
                Object[] components = (Object[]) it.next();
                return new Tuple((VarNode) components[0], (SparkField) components[1], (VarNode) components[2]);
            }
            
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
                                               "rc/soot/jimple/spark/queue/Rsrc_fld_dstBDD.jedd:55,33-36"),
                                              bdd);
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new jedd.internal.RelationContainer(new Attribute[] { fld.v(), dst.v(), src.v() },
                                                   new PhysicalDomain[] { FD.v(), V2.v(), V1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rsrc_fld_dstBDD.jedd:57,8-14"),
                                                   ret);
    }
    
    public boolean hasNext() {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD());
    }
}
