package soot.jimple.spark;

import soot.jimple.spark.queue.*;
import soot.jimple.spark.bdddomains.*;
import soot.*;

public class BDDReachableMethods extends AbsReachableMethods {
    private final jedd.internal.RelationContainer reachables =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { ctxt.v(), method.v() },
                                          new jedd.PhysicalDomain[] { V2.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.ctxt, soot.jimple.spar" +
                                           "k.bdddomains.method> reachables at /home/olhotak/soot-2-jedd" +
                                           "/src/soot/jimple/spark/BDDReachableMethods.jedd:30,12"));
    
    private AbsCallGraph cg;
    
    private Rctxt_method newMethods;
    
    BDDReachableMethods(Rsrcc_srcm_stmt_kind_tgtc_tgtm in, Qctxt_method out, AbsCallGraph cg) {
        super(in, out);
        this.cg = cg;
        newMethods = out.reader();
    }
    
    void update() {
        final jedd.internal.RelationContainer newEdges =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { srcc.v(), srcm.v(), stmt.v(), kind.v(), tgtc.v(), tgtm.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), FD.v(), V2.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.srcc:soot.jimple.spark.bdddoma" +
                                               "ins.V1, soot.jimple.spark.bdddomains.srcm:soot.jimple.spark." +
                                               "bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimple" +
                                               ".spark.bdddomains.ST, soot.jimple.spark.bdddomains.kind:soot" +
                                               ".jimple.spark.bdddomains.FD, soot.jimple.spark.bdddomains.tg" +
                                               "tc:soot.jimple.spark.bdddomains.V2, soot.jimple.spark.bdddom" +
                                               "ains.tgtm:soot.jimple.spark.bdddomains.T2> newEdges = jedd.i" +
                                               "nternal.Jedd.v().join(jedd.internal.Jedd.v().read(in.get())," +
                                               " jedd.internal.Jedd.v().replace(reachables, new jedd.Physica" +
                                               "lDomain[...], new jedd.PhysicalDomain[...]), new jedd.Physic" +
                                               "alDomain[...]); at /home/olhotak/soot-2-jedd/src/soot/jimple" +
                                               "/spark/BDDReachableMethods.jedd:39,8"),
                                              jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(in.get()),
                                                                          jedd.internal.Jedd.v().replace(reachables,
                                                                                                         new jedd.PhysicalDomain[] { V2.v(), T2.v() },
                                                                                                         new jedd.PhysicalDomain[] { V1.v(), T1.v() }),
                                                                          new jedd.PhysicalDomain[] { V1.v(), T1.v() }));
        while (!jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(newEdges),
                                              jedd.internal.Jedd.v().falseBDD())) {
            final jedd.internal.RelationContainer newTargets =
              new jedd.internal.RelationContainer(new jedd.Attribute[] { ctxt.v(), method.v() },
                                                  new jedd.PhysicalDomain[] { V2.v(), T2.v() },
                                                  ("<soot.jimple.spark.bdddomains.ctxt:soot.jimple.spark.bdddoma" +
                                                   "ins.V2, soot.jimple.spark.bdddomains.method:soot.jimple.spar" +
                                                   "k.bdddomains.T2> newTargets = jedd.internal.Jedd.v().project" +
                                                   "(newEdges, new jedd.PhysicalDomain[...]); at /home/olhotak/s" +
                                                   "oot-2-jedd/src/soot/jimple/spark/BDDReachableMethods.jedd:41" +
                                                   ",12"),
                                                  jedd.internal.Jedd.v().project(newEdges,
                                                                                 new jedd.PhysicalDomain[] { V1.v(), FD.v(), T1.v(), ST.v() }));
            newTargets.eqMinus(reachables);
            out.add(new jedd.internal.RelationContainer(new jedd.Attribute[] { ctxt.v(), method.v() },
                                                        new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                                        ("out.add(jedd.internal.Jedd.v().replace(newTargets, new jedd." +
                                                         "PhysicalDomain[...], new jedd.PhysicalDomain[...])) at /home" +
                                                         "/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDReachableMetho" +
                                                         "ds.jedd:43,12"),
                                                        jedd.internal.Jedd.v().replace(newTargets,
                                                                                       new jedd.PhysicalDomain[] { V2.v(), T2.v() },
                                                                                       new jedd.PhysicalDomain[] { V1.v(), T1.v() })));
            reachables.eqUnion(newTargets);
            newEdges.eq(cg.edgesOutOf(newMethods).get());
        }
    }
    
    void add(MethodOrMethodContext m) {
        final jedd.internal.RelationContainer newM =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { ctxt.v(), method.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                              ("<soot.jimple.spark.bdddomains.ctxt:soot.jimple.spark.bdddoma" +
                                               "ins.V1, soot.jimple.spark.bdddomains.method:soot.jimple.spar" +
                                               "k.bdddomains.T1> newM = jedd.internal.Jedd.v().literal(new j" +
                                               "ava.lang.Object[...], new jedd.Attribute[...], new jedd.Phys" +
                                               "icalDomain[...]); at /home/olhotak/soot-2-jedd/src/soot/jimp" +
                                               "le/spark/BDDReachableMethods.jedd:49,8"),
                                              jedd.internal.Jedd.v().literal(new Object[] { m.context(), m.method() },
                                                                             new jedd.Attribute[] { ctxt.v(), method.v() },
                                                                             new jedd.PhysicalDomain[] { V1.v(), T1.v() }));
        final jedd.internal.RelationContainer newReachables =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { ctxt.v(), method.v() },
                                              new jedd.PhysicalDomain[] { V2.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.ctxt:soot.jimple.spark.bdddoma" +
                                               "ins.V2, soot.jimple.spark.bdddomains.method:soot.jimple.spar" +
                                               "k.bdddomains.T2> newReachables = jedd.internal.Jedd.v().repl" +
                                               "ace(jedd.internal.Jedd.v().union(jedd.internal.Jedd.v().read" +
                                               "(jedd.internal.Jedd.v().replace(reachables, new jedd.Physica" +
                                               "lDomain[...], new jedd.PhysicalDomain[...])), newM), new jed" +
                                               "d.PhysicalDomain[...], new jedd.PhysicalDomain[...]); at /ho" +
                                               "me/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDReachableMet" +
                                               "hods.jedd:50,8"),
                                              jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().union(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(reachables,
                                                                                                                                                                     new jedd.PhysicalDomain[] { V2.v(), T2.v() },
                                                                                                                                                                     new jedd.PhysicalDomain[] { V1.v(), T1.v() })),
                                                                                                          newM),
                                                                             new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                                                             new jedd.PhysicalDomain[] { V2.v(), T2.v() }));
        if (!jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(newReachables), reachables)) {
            reachables.eq(newReachables);
            out.add(new jedd.internal.RelationContainer(new jedd.Attribute[] { ctxt.v(), method.v() },
                                                        new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                                        ("out.add(newM) at /home/olhotak/soot-2-jedd/src/soot/jimple/s" +
                                                         "park/BDDReachableMethods.jedd:53,12"),
                                                        newM));
        }
    }
}
