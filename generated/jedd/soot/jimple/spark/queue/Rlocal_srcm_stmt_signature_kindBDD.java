package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Rlocal_srcm_stmt_signature_kindBDD extends Rlocal_srcm_stmt_signature_kind {
    private final jedd.internal.RelationContainer bdd =
      new jedd.internal.RelationContainer(new Attribute[] { local.v(), srcm.v(), stmt.v(), signature.v(), kind.v() },
                                          new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), FD.v() },
                                          ("private <soot.jimple.spark.bdddomains.local:soot.jimple.spar" +
                                           "k.bdddomains.V1, soot.jimple.spark.bdddomains.srcm:soot.jimp" +
                                           "le.spark.bdddomains.T1, soot.jimple.spark.bdddomains.stmt:so" +
                                           "ot.jimple.spark.bdddomains.ST, soot.jimple.spark.bdddomains." +
                                           "signature:soot.jimple.spark.bdddomains.H2, soot.jimple.spark" +
                                           ".bdddomains.kind:soot.jimple.spark.bdddomains.FD> bdd at /ho" +
                                           "me/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/Rlocal_sr" +
                                           "cm_stmt_signature_kindBDD.jedd:31,12-63"));
    
    void add(final jedd.internal.RelationContainer tuple) { bdd.eqUnion(tuple); }
    
    public Rlocal_srcm_stmt_signature_kindBDD(final jedd.internal.RelationContainer bdd) {
        this();
        this.add(new jedd.internal.RelationContainer(new Attribute[] { stmt.v(), local.v(), signature.v(), srcm.v(), kind.v() },
                                                     new PhysicalDomain[] { ST.v(), V1.v(), H2.v(), T1.v(), FD.v() },
                                                     ("this.add(bdd) at /home/olhotak/soot-2-jedd/src/soot/jimple/s" +
                                                      "park/queue/Rlocal_srcm_stmt_signature_kindBDD.jedd:33,115-11" +
                                                      "8"),
                                                     bdd));
    }
    
    Rlocal_srcm_stmt_signature_kindBDD() {
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
                      new jedd.internal.RelationContainer(new Attribute[] { stmt.v(), local.v(), signature.v(), srcm.v(), kind.v() },
                                                          new PhysicalDomain[] { ST.v(), V1.v(), H2.v(), T1.v(), FD.v() },
                                                          ("bdd.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-" +
                                                           "2-jedd/src/soot/jimple/spark/queue/Rlocal_srcm_stmt_signatur" +
                                                           "e_kindBDD.jedd:45,25-28"),
                                                          bdd).iterator(new Attribute[] { local.v(), srcm.v(), stmt.v(), signature.v(), kind.v() });
                    bdd.eq(jedd.internal.Jedd.v().falseBDD());
                }
                Object[] components = (Object[]) it.next();
                return new Tuple((Local) components[0],
                                 (SootMethod) components[1],
                                 (Unit) components[2],
                                 (NumberedString) components[3],
                                 (Kind) components[4]);
            }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { local.v(), srcm.v(), stmt.v(), signature.v(), kind.v() },
                                              new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), FD.v() },
                                              ("<soot.jimple.spark.bdddomains.local:soot.jimple.spark.bdddom" +
                                               "ains.V1, soot.jimple.spark.bdddomains.srcm:soot.jimple.spark" +
                                               ".bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimpl" +
                                               "e.spark.bdddomains.ST, soot.jimple.spark.bdddomains.signatur" +
                                               "e:soot.jimple.spark.bdddomains.H2, soot.jimple.spark.bdddoma" +
                                               "ins.kind:soot.jimple.spark.bdddomains.FD> ret = bdd; at /hom" +
                                               "e/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/Rlocal_src" +
                                               "m_stmt_signature_kindBDD.jedd:55,60-63"),
                                              bdd);
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new jedd.internal.RelationContainer(new Attribute[] { stmt.v(), local.v(), signature.v(), srcm.v(), kind.v() },
                                                   new PhysicalDomain[] { ST.v(), V1.v(), H2.v(), T1.v(), FD.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rlocal_srcm_stmt_signature_kindBDD.jedd:57,8-14"),
                                                   ret);
    }
    
    public boolean hasNext() {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD());
    }
}
