package soot.jimple.spark;

import soot.*;
import soot.jimple.toolkits.callgraph.*;
import soot.jimple.spark.queue.*;
import soot.jimple.spark.bdddomains.*;

public class BDDCallGraph extends AbsCallGraph {
    private final jedd.internal.RelationContainer edges =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { srcc.v(), srcm.v(), stmt.v(), kind.v(), tgtc.v(), tgtm.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), FD.v(), V2.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.srcc, soot.jimple.spar" +
                                           "k.bdddomains.srcm, soot.jimple.spark.bdddomains.stmt, soot.j" +
                                           "imple.spark.bdddomains.kind, soot.jimple.spark.bdddomains.tg" +
                                           "tc, soot.jimple.spark.bdddomains.tgtm> edges at /home/olhota" +
                                           "k/soot-2-jedd/src/soot/jimple/spark/BDDCallGraph.jedd:31,12"));
    
    BDDCallGraph(Rsrcc_srcm_stmt_kind_tgtc_tgtm in, Qsrcc_srcm_stmt_kind_tgtc_tgtm out) { super(in, out); }
    
    public boolean update() {
        final jedd.internal.RelationContainer newEdges =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { srcc.v(), srcm.v(), stmt.v(), kind.v(), tgtc.v(), tgtm.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), FD.v(), V2.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.srcc:soot.jimple.spark.bdddoma" +
                                               "ins.V1, soot.jimple.spark.bdddomains.srcm:soot.jimple.spark." +
                                               "bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimple" +
                                               ".spark.bdddomains.ST, soot.jimple.spark.bdddomains.kind:soot" +
                                               ".jimple.spark.bdddomains.FD, soot.jimple.spark.bdddomains.tg" +
                                               "tc:soot.jimple.spark.bdddomains.V2, soot.jimple.spark.bdddom" +
                                               "ains.tgtm:soot.jimple.spark.bdddomains.T2> newEdges = in.get" +
                                               "(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDCa" +
                                               "llGraph.jedd:36,8"),
                                              in.get());
        newEdges.eqMinus(edges);
        edges.eqUnion(newEdges);
        out.add(new jedd.internal.RelationContainer(new jedd.Attribute[] { srcc.v(), tgtc.v(), tgtm.v(), kind.v(), stmt.v(), srcm.v() },
                                                    new jedd.PhysicalDomain[] { V1.v(), V2.v(), T2.v(), FD.v(), ST.v(), T1.v() },
                                                    ("out.add(edges) at /home/olhotak/soot-2-jedd/src/soot/jimple/" +
                                                     "spark/BDDCallGraph.jedd:39,8"),
                                                    edges));
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(newEdges), jedd.internal.Jedd.v().falseBDD());
    }
    
    public Rsrcc_srcm_stmt_kind_tgtc_tgtm edgesOutOf(Rctxt_method methods) {
        return new Rsrcc_srcm_stmt_kind_tgtc_tgtmBDD(new jedd.internal.RelationContainer(new jedd.Attribute[] { srcc.v(), tgtc.v(), tgtm.v(), kind.v(), srcm.v(), stmt.v() },
                                                                                         new jedd.PhysicalDomain[] { V1.v(), V2.v(), T2.v(), FD.v(), T1.v(), ST.v() },
                                                                                         ("new soot.jimple.spark.queue.Rsrcc_srcm_stmt_kind_tgtc_tgtmBD" +
                                                                                          "D(...) at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/BD" +
                                                                                          "DCallGraph.jedd:43,15"),
                                                                                         jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(edges),
                                                                                                                     methods.get(),
                                                                                                                     new jedd.PhysicalDomain[] { T1.v(), V1.v() })));
    }
    
    public Rsrcc_srcm_stmt_kind_tgtc_tgtm edgesOutOf(MethodOrMethodContext m) {
        return new Rsrcc_srcm_stmt_kind_tgtc_tgtmBDD(new jedd.internal.RelationContainer(new jedd.Attribute[] { srcc.v(), tgtc.v(), tgtm.v(), kind.v(), srcm.v(), stmt.v() },
                                                                                         new jedd.PhysicalDomain[] { V1.v(), V2.v(), T2.v(), FD.v(), T1.v(), ST.v() },
                                                                                         ("new soot.jimple.spark.queue.Rsrcc_srcm_stmt_kind_tgtc_tgtmBD" +
                                                                                          "D(...) at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/BD" +
                                                                                          "DCallGraph.jedd:47,15"),
                                                                                         jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(edges),
                                                                                                                     jedd.internal.Jedd.v().literal(new Object[] { m.context(), m.method() },
                                                                                                                                                    new jedd.Attribute[] { ctxt.v(), method.v() },
                                                                                                                                                    new jedd.PhysicalDomain[] { V1.v(), T1.v() }),
                                                                                                                     new jedd.PhysicalDomain[] { T1.v(), V1.v() })));
    }
    
    public Rsrcc_srcm_stmt_kind_tgtc_tgtm edges() {
        return new Rsrcc_srcm_stmt_kind_tgtc_tgtmBDD(new jedd.internal.RelationContainer(new jedd.Attribute[] { srcc.v(), tgtc.v(), tgtm.v(), kind.v(), stmt.v(), srcm.v() },
                                                                                         new jedd.PhysicalDomain[] { V1.v(), V2.v(), T2.v(), FD.v(), ST.v(), T1.v() },
                                                                                         ("new soot.jimple.spark.queue.Rsrcc_srcm_stmt_kind_tgtc_tgtmBD" +
                                                                                          "D(...) at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/BD" +
                                                                                          "DCallGraph.jedd:53,15"),
                                                                                         edges));
    }
}
