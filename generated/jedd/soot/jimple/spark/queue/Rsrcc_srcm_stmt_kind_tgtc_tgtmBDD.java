package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Rsrcc_srcm_stmt_kind_tgtc_tgtmBDD extends Rsrcc_srcm_stmt_kind_tgtc_tgtm {
    private final jedd.internal.RelationContainer bdd =
      new jedd.internal.RelationContainer(new Attribute[] { srcc.v(), srcm.v(), stmt.v(), kind.v(), tgtc.v(), tgtm.v() },
                                          new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), FD.v(), V2.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.srcc:soot.jimple.spark" +
                                           ".bdddomains.V1, soot.jimple.spark.bdddomains.srcm:soot.jimpl" +
                                           "e.spark.bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soo" +
                                           "t.jimple.spark.bdddomains.ST, soot.jimple.spark.bdddomains.k" +
                                           "ind:soot.jimple.spark.bdddomains.FD, soot.jimple.spark.bdddo" +
                                           "mains.tgtc:soot.jimple.spark.bdddomains.V2, soot.jimple.spar" +
                                           "k.bdddomains.tgtm:soot.jimple.spark.bdddomains.T2> bdd at /h" +
                                           "ome/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/Rsrcc_sr" +
                                           "cm_stmt_kind_tgtc_tgtmBDD.jedd:31,12-66"));
    
    void add(final jedd.internal.RelationContainer tuple) { bdd.eqUnion(tuple); }
    
    public Rsrcc_srcm_stmt_kind_tgtc_tgtmBDD(final jedd.internal.RelationContainer bdd) {
        this();
        this.add(new jedd.internal.RelationContainer(new Attribute[] { srcc.v(), srcm.v(), tgtm.v(), tgtc.v(), stmt.v(), kind.v() },
                                                     new PhysicalDomain[] { V1.v(), T1.v(), T2.v(), V2.v(), ST.v(), FD.v() },
                                                     ("this.add(bdd) at /home/olhotak/soot-2-jedd/src/soot/jimple/s" +
                                                      "park/queue/Rsrcc_srcm_stmt_kind_tgtc_tgtmBDD.jedd:33,117-120"),
                                                     bdd));
    }
    
    Rsrcc_srcm_stmt_kind_tgtc_tgtmBDD() {
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
                      new jedd.internal.RelationContainer(new Attribute[] { srcc.v(), srcm.v(), tgtm.v(), tgtc.v(), stmt.v(), kind.v() },
                                                          new PhysicalDomain[] { V1.v(), T1.v(), T2.v(), V2.v(), ST.v(), FD.v() },
                                                          ("bdd.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-" +
                                                           "2-jedd/src/soot/jimple/spark/queue/Rsrcc_srcm_stmt_kind_tgtc" +
                                                           "_tgtmBDD.jedd:45,25-28"),
                                                          bdd).iterator(new Attribute[] { srcc.v(), srcm.v(), stmt.v(), kind.v(), tgtc.v(), tgtm.v() });
                    bdd.eq(jedd.internal.Jedd.v().falseBDD());
                }
                Object[] components = (Object[]) it.next();
                return new Tuple((Context) components[0],
                                 (SootMethod) components[1],
                                 (Unit) components[2],
                                 (Kind) components[3],
                                 (Context) components[4],
                                 (SootMethod) components[5]);
            }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { srcc.v(), srcm.v(), stmt.v(), kind.v(), tgtc.v(), tgtm.v() },
                                              new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), FD.v(), V2.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.srcc:soot.jimple.spark.bdddoma" +
                                               "ins.V1, soot.jimple.spark.bdddomains.srcm:soot.jimple.spark." +
                                               "bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimple" +
                                               ".spark.bdddomains.ST, soot.jimple.spark.bdddomains.kind:soot" +
                                               ".jimple.spark.bdddomains.FD, soot.jimple.spark.bdddomains.tg" +
                                               "tc:soot.jimple.spark.bdddomains.V2, soot.jimple.spark.bdddom" +
                                               "ains.tgtm:soot.jimple.spark.bdddomains.T2> ret = bdd; at /ho" +
                                               "me/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/Rsrcc_src" +
                                               "m_stmt_kind_tgtc_tgtmBDD.jedd:55,63-66"),
                                              bdd);
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new jedd.internal.RelationContainer(new Attribute[] { srcc.v(), srcm.v(), tgtm.v(), tgtc.v(), stmt.v(), kind.v() },
                                                   new PhysicalDomain[] { V1.v(), T1.v(), T2.v(), V2.v(), ST.v(), FD.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rsrcc_srcm_stmt_kind_tgtc_tgtmBDD.jedd:57,8-14"),
                                                   ret);
    }
    
    public boolean hasNext() {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD());
    }
}
