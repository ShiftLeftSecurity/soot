package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Rlocal_srcm_stmt_tgtmBDD extends Rlocal_srcm_stmt_tgtm {
    private final jedd.internal.RelationContainer bdd =
      new jedd.internal.RelationContainer(new Attribute[] { local.v(), srcm.v(), stmt.v(), tgtm.v() },
                                          new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.local:soot.jimple.spar" +
                                           "k.bdddomains.V1, soot.jimple.spark.bdddomains.srcm:soot.jimp" +
                                           "le.spark.bdddomains.T1, soot.jimple.spark.bdddomains.stmt:so" +
                                           "ot.jimple.spark.bdddomains.ST, soot.jimple.spark.bdddomains." +
                                           "tgtm:soot.jimple.spark.bdddomains.T2> bdd at /home/olhotak/s" +
                                           "oot-2-jedd/src/soot/jimple/spark/queue/Rlocal_srcm_stmt_tgtm" +
                                           "BDD.jedd:31,12-49"));
    
    void add(final jedd.internal.RelationContainer tuple) { bdd.eqUnion(tuple); }
    
    public Rlocal_srcm_stmt_tgtmBDD(final jedd.internal.RelationContainer bdd) {
        this();
        add(new jedd.internal.RelationContainer(new Attribute[] { stmt.v(), local.v(), srcm.v(), tgtm.v() },
                                                new PhysicalDomain[] { ST.v(), V1.v(), T1.v(), T2.v() },
                                                ("add(bdd) at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/" +
                                                 "queue/Rlocal_srcm_stmt_tgtmBDD.jedd:33,91-94"),
                                                bdd));
    }
    
    Rlocal_srcm_stmt_tgtmBDD() {
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
                      new jedd.internal.RelationContainer(new Attribute[] { stmt.v(), local.v(), srcm.v(), tgtm.v() },
                                                          new PhysicalDomain[] { ST.v(), V1.v(), T1.v(), T2.v() },
                                                          ("bdd.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-" +
                                                           "2-jedd/src/soot/jimple/spark/queue/Rlocal_srcm_stmt_tgtmBDD." +
                                                           "jedd:45,25-28"),
                                                          bdd).iterator(new Attribute[] { local.v(), srcm.v(), stmt.v(), tgtm.v() });
                    bdd.eq(jedd.internal.Jedd.v().falseBDD());
                }
                Object[] components = (Object[]) it.next();
                return new Tuple((Local) components[0],
                                 (SootMethod) components[1],
                                 (Unit) components[2],
                                 (SootMethod) components[3]);
            }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { local.v(), srcm.v(), stmt.v(), tgtm.v() },
                                              new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.local:soot.jimple.spark.bdddom" +
                                               "ains.V1, soot.jimple.spark.bdddomains.srcm:soot.jimple.spark" +
                                               ".bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimpl" +
                                               "e.spark.bdddomains.ST, soot.jimple.spark.bdddomains.tgtm:soo" +
                                               "t.jimple.spark.bdddomains.T2> ret = bdd; at /home/olhotak/so" +
                                               "ot-2-jedd/src/soot/jimple/spark/queue/Rlocal_srcm_stmt_tgtmB" +
                                               "DD.jedd:55,46-49"),
                                              bdd);
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new jedd.internal.RelationContainer(new Attribute[] { stmt.v(), local.v(), srcm.v(), tgtm.v() },
                                                   new PhysicalDomain[] { ST.v(), V1.v(), T1.v(), T2.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rlocal_srcm_stmt_tgtmBDD.jedd:57,8-14"),
                                                   ret);
    }
    
    public boolean hasNext() {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD());
    }
}
