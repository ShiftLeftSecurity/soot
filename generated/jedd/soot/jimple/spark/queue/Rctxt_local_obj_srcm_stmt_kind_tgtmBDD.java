package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Rctxt_local_obj_srcm_stmt_kind_tgtmBDD extends Rctxt_local_obj_srcm_stmt_kind_tgtm {
    private final jedd.internal.RelationContainer bdd =
      new jedd.internal.RelationContainer(new Attribute[] { ctxt.v(), local.v(), obj.v(), srcm.v(), stmt.v(), kind.v(), tgtm.v() },
                                          new PhysicalDomain[] { V2.v(), V1.v(), H1.v(), T1.v(), ST.v(), FD.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.ctxt:soot.jimple.spark" +
                                           ".bdddomains.V2, soot.jimple.spark.bdddomains.local:soot.jimp" +
                                           "le.spark.bdddomains.V1, soot.jimple.spark.bdddomains.obj:soo" +
                                           "t.jimple.spark.bdddomains.H1, soot.jimple.spark.bdddomains.s" +
                                           "rcm:soot.jimple.spark.bdddomains.T1, soot.jimple.spark.bdddo" +
                                           "mains.stmt:soot.jimple.spark.bdddomains.ST, soot.jimple.spar" +
                                           "k.bdddomains.kind:soot.jimple.spark.bdddomains.FD, soot.jimp" +
                                           "le.spark.bdddomains.tgtm:soot.jimple.spark.bdddomains.T2> bd" +
                                           "d at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/R" +
                                           "ctxt_local_obj_srcm_stmt_kind_tgtmBDD.jedd:31,12-75"));
    
    void add(final jedd.internal.RelationContainer tuple) { bdd.eqUnion(tuple); }
    
    public Rctxt_local_obj_srcm_stmt_kind_tgtmBDD(final jedd.internal.RelationContainer bdd) {
        this();
        this.add(new jedd.internal.RelationContainer(new Attribute[] { srcm.v(), local.v(), ctxt.v(), tgtm.v(), stmt.v(), obj.v(), kind.v() },
                                                     new PhysicalDomain[] { T1.v(), V1.v(), V2.v(), T2.v(), ST.v(), H1.v(), FD.v() },
                                                     ("this.add(bdd) at /home/olhotak/soot-2-jedd/src/soot/jimple/s" +
                                                      "park/queue/Rctxt_local_obj_srcm_stmt_kind_tgtmBDD.jedd:33,13" +
                                                      "1-134"),
                                                     bdd));
    }
    
    Rctxt_local_obj_srcm_stmt_kind_tgtmBDD() {
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
                      new jedd.internal.RelationContainer(new Attribute[] { srcm.v(), local.v(), ctxt.v(), tgtm.v(), stmt.v(), obj.v(), kind.v() },
                                                          new PhysicalDomain[] { T1.v(), V1.v(), V2.v(), T2.v(), ST.v(), H1.v(), FD.v() },
                                                          ("bdd.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-" +
                                                           "2-jedd/src/soot/jimple/spark/queue/Rctxt_local_obj_srcm_stmt" +
                                                           "_kind_tgtmBDD.jedd:45,25-28"),
                                                          bdd).iterator(new Attribute[] { ctxt.v(), local.v(), obj.v(), srcm.v(), stmt.v(), kind.v(), tgtm.v() });
                    bdd.eq(jedd.internal.Jedd.v().falseBDD());
                }
                Object[] components = (Object[]) it.next();
                return new Tuple((Context) components[0],
                                 (Local) components[1],
                                 (AllocNode) components[2],
                                 (SootMethod) components[3],
                                 (Unit) components[4],
                                 (Kind) components[5],
                                 (SootMethod) components[6]);
            }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { ctxt.v(), local.v(), obj.v(), srcm.v(), stmt.v(), kind.v(), tgtm.v() },
                                              new PhysicalDomain[] { V2.v(), V1.v(), H1.v(), T1.v(), ST.v(), FD.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.ctxt:soot.jimple.spark.bdddoma" +
                                               "ins.V2, soot.jimple.spark.bdddomains.local:soot.jimple.spark" +
                                               ".bdddomains.V1, soot.jimple.spark.bdddomains.obj:soot.jimple" +
                                               ".spark.bdddomains.H1, soot.jimple.spark.bdddomains.srcm:soot" +
                                               ".jimple.spark.bdddomains.T1, soot.jimple.spark.bdddomains.st" +
                                               "mt:soot.jimple.spark.bdddomains.ST, soot.jimple.spark.bdddom" +
                                               "ains.kind:soot.jimple.spark.bdddomains.FD, soot.jimple.spark" +
                                               ".bdddomains.tgtm:soot.jimple.spark.bdddomains.T2> ret = bdd;" +
                                               " at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/Rc" +
                                               "txt_local_obj_srcm_stmt_kind_tgtmBDD.jedd:55,72-75"),
                                              bdd);
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new jedd.internal.RelationContainer(new Attribute[] { srcm.v(), local.v(), ctxt.v(), tgtm.v(), stmt.v(), obj.v(), kind.v() },
                                                   new PhysicalDomain[] { T1.v(), V1.v(), V2.v(), T2.v(), ST.v(), H1.v(), FD.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rctxt_local_obj_srcm_stmt_kind_tgtmBDD.jedd:57,8-14"),
                                                   ret);
    }
    
    public boolean hasNext() {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD());
    }
}
