package soot.jimple.spark;

import soot.jimple.spark.queue.*;
import soot.jimple.spark.bdddomains.*;

public class BDDContextCallGraphBuilder extends AbsContextCallGraphBuilder {
    BDDContextCallGraphBuilder(Rctxt_method in, Qsrcc_srcm_stmt_kind_tgtc_tgtm out, AbsCallGraph cicg) {
        super(in, out, cicg);
    }
    
    public void update() {
        final jedd.internal.RelationContainer methods =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { ctxt.v(), method.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                              ("<soot.jimple.spark.bdddomains.ctxt:soot.jimple.spark.bdddoma" +
                                               "ins.V1, soot.jimple.spark.bdddomains.method:soot.jimple.spar" +
                                               "k.bdddomains.T1> methods = in.get(); at /home/olhotak/soot-2" +
                                               "-jedd/src/soot/jimple/spark/BDDContextCallGraphBuilder.jedd:" +
                                               "35,23-30"),
                                              in.get());
        Rsrcc_srcm_stmt_kind_tgtc_tgtm edges =
          cicg.edgesOutOf(new Rctxt_methodBDD(new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), ctxt.v() },
                                                                                  new jedd.PhysicalDomain[] { T1.v(), V1.v() },
                                                                                  ("new soot.jimple.spark.queue.Rctxt_methodBDD(...) at /home/ol" +
                                                                                   "hotak/soot-2-jedd/src/soot/jimple/spark/BDDContextCallGraphB" +
                                                                                   "uilder.jedd:38,12-15"),
                                                                                  jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().project(methods,
                                                                                                                                                                         new jedd.PhysicalDomain[] { V1.v() })),
                                                                                                              jedd.internal.Jedd.v().literal(new Object[] { null },
                                                                                                                                             new jedd.Attribute[] { ctxt.v() },
                                                                                                                                             new jedd.PhysicalDomain[] { V1.v() }),
                                                                                                              new jedd.PhysicalDomain[] {  }))));
        out.add(new jedd.internal.RelationContainer(new jedd.Attribute[] { srcm.v(), tgtm.v(), tgtc.v(), stmt.v(), kind.v(), srcc.v() },
                                                    new jedd.PhysicalDomain[] { T1.v(), T2.v(), V2.v(), ST.v(), FD.v(), V1.v() },
                                                    ("out.add(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().r" +
                                                     "ead(jedd.internal.Jedd.v().project(edges.get(), new jedd.Phy" +
                                                     "sicalDomain[...])), methods, new jedd.PhysicalDomain[...])) " +
                                                     "at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDContex" +
                                                     "tCallGraphBuilder.jedd:41,8-11"),
                                                    jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().project(edges.get(),
                                                                                                                                           new jedd.PhysicalDomain[] { V1.v() })),
                                                                                methods,
                                                                                new jedd.PhysicalDomain[] { T1.v() })));
    }
}
