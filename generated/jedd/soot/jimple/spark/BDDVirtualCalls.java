package soot.jimple.spark;

import soot.*;
import soot.jimple.spark.queue.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.toolkits.callgraph.*;
import java.util.*;

public class BDDVirtualCalls extends AbsVirtualCalls {
    BDDVirtualCalls(Rvar_obj pt,
                    Rlocal_srcm_stmt_signature_kind receivers,
                    Rlocal_srcm_stmt_tgtm specials,
                    Qctxt_local_obj_srcm_stmt_kind_tgtm out) {
        super(pt, receivers, specials, out);
        for (Iterator clIt = Scene.v().getClasses().iterator(); clIt.hasNext(); ) {
            final SootClass cl = (SootClass) clIt.next();
            for (Iterator mIt = cl.getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                if (m.isAbstract()) continue;
                declaresMethod.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { m.getDeclaringClass().getType(), m.getNumberedSubSignature(), m },
                                                                      new jedd.Attribute[] { type.v(), signature.v(), method.v() },
                                                                      new jedd.PhysicalDomain[] { T2.v(), H2.v(), T3.v() }));
            }
        }
    }
    
    private int lastVarNode = 1;
    
    private int lastAllocNode = 1;
    
    private final jedd.internal.RelationContainer varNodes =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { ctxt.v(), local.v(), var.v() },
                                          new jedd.PhysicalDomain[] { V2.v(), V1.v(), V3.v() },
                                          ("private <soot.jimple.spark.bdddomains.ctxt, soot.jimple.spar" +
                                           "k.bdddomains.local, soot.jimple.spark.bdddomains.var> varNod" +
                                           "es at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDVir" +
                                           "tualCalls.jedd:53,12"));
    
    private final jedd.internal.RelationContainer allocNodes =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), type.v() },
                                          new jedd.PhysicalDomain[] { H1.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.obj, soot.jimple.spark" +
                                           ".bdddomains.type> allocNodes at /home/olhotak/soot-2-jedd/sr" +
                                           "c/soot/jimple/spark/BDDVirtualCalls.jedd:54,12"));
    
    private final jedd.internal.RelationContainer rcv =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { local.v(), srcm.v(), stmt.v(), signature.v(), kind.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), FD.v() },
                                          ("private <soot.jimple.spark.bdddomains.local, soot.jimple.spa" +
                                           "rk.bdddomains.srcm, soot.jimple.spark.bdddomains.stmt, soot." +
                                           "jimple.spark.bdddomains.signature, soot.jimple.spark.bdddoma" +
                                           "ins.kind> rcv at /home/olhotak/soot-2-jedd/src/soot/jimple/s" +
                                           "park/BDDVirtualCalls.jedd:55,12"));
    
    private final jedd.internal.RelationContainer virtual =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { kind.v() },
                                          new jedd.PhysicalDomain[] { FD.v() },
                                          ("private <soot.jimple.spark.bdddomains.kind> virtual = jedd.i" +
                                           "nternal.Jedd.v().union(jedd.internal.Jedd.v().read(jedd.inte" +
                                           "rnal.Jedd.v().literal(new java.lang.Object[...], new jedd.At" +
                                           "tribute[...], new jedd.PhysicalDomain[...])), jedd.internal." +
                                           "Jedd.v().literal(new java.lang.Object[...], new jedd.Attribu" +
                                           "te[...], new jedd.PhysicalDomain[...])) at /home/olhotak/soo" +
                                           "t-2-jedd/src/soot/jimple/spark/BDDVirtualCalls.jedd:56,12"),
                                          jedd.internal.Jedd.v().union(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().literal(new Object[] { Kind.VIRTUAL },
                                                                                                                                  new jedd.Attribute[] { kind.v() },
                                                                                                                                  new jedd.PhysicalDomain[] { FD.v() })),
                                                                       jedd.internal.Jedd.v().literal(new Object[] { Kind.INTERFACE },
                                                                                                      new jedd.Attribute[] { kind.v() },
                                                                                                      new jedd.PhysicalDomain[] { FD.v() })));
    
    private void updateNodes() {
        for (; lastVarNode < SparkNumberers.v().varNodeNumberer().size(); lastVarNode++) {
            VarNode vn = (VarNode) SparkNumberers.v().varNodeNumberer().get(lastVarNode);
            if (vn.getVariable() instanceof Local) {
                varNodes.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { vn.context(), vn.getVariable(), vn },
                                                                new jedd.Attribute[] { ctxt.v(), local.v(), var.v() },
                                                                new jedd.PhysicalDomain[] { V2.v(), V1.v(), V3.v() }));
            }
        }
        for (; lastAllocNode < SparkNumberers.v().allocNodeNumberer().size(); lastAllocNode++) {
            AllocNode an = (AllocNode) SparkNumberers.v().allocNodeNumberer().get(lastVarNode);
            allocNodes.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { an, an.getType() },
                                                              new jedd.Attribute[] { obj.v(), type.v() },
                                                              new jedd.PhysicalDomain[] { H1.v(), T2.v() }));
        }
    }
    
    private final jedd.internal.RelationContainer targets =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { type.v(), signature.v(), method.v() },
                                          new jedd.PhysicalDomain[] { T1.v(), H2.v(), T3.v() },
                                          ("private <soot.jimple.spark.bdddomains.type, soot.jimple.spar" +
                                           "k.bdddomains.signature, soot.jimple.spark.bdddomains.method>" +
                                           " targets = jedd.internal.Jedd.v().falseBDD() at /home/olhota" +
                                           "k/soot-2-jedd/src/soot/jimple/spark/BDDVirtualCalls.jedd:75," +
                                           "12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final jedd.internal.RelationContainer declaresMethod =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { type.v(), signature.v(), method.v() },
                                          new jedd.PhysicalDomain[] { T2.v(), H2.v(), T3.v() },
                                          ("private <soot.jimple.spark.bdddomains.type, soot.jimple.spar" +
                                           "k.bdddomains.signature, soot.jimple.spark.bdddomains.method:" +
                                           "soot.jimple.spark.bdddomains.T3> declaresMethod = jedd.inter" +
                                           "nal.Jedd.v().falseBDD() at /home/olhotak/soot-2-jedd/src/soo" +
                                           "t/jimple/spark/BDDVirtualCalls.jedd:76,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private BDDHierarchy hier = new BDDHierarchy();
    
    public void update() {
        this.updateNodes();
        final jedd.internal.RelationContainer newRcv =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { local.v(), srcm.v(), stmt.v(), signature.v(), kind.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), FD.v() },
                                              ("<soot.jimple.spark.bdddomains.local:soot.jimple.spark.bdddom" +
                                               "ains.V1, soot.jimple.spark.bdddomains.srcm:soot.jimple.spark" +
                                               ".bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimpl" +
                                               "e.spark.bdddomains.ST, soot.jimple.spark.bdddomains.signatur" +
                                               "e:soot.jimple.spark.bdddomains.H2, soot.jimple.spark.bdddoma" +
                                               "ins.kind:soot.jimple.spark.bdddomains.FD> newRcv = receivers" +
                                               ".get(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/B" +
                                               "DDVirtualCalls.jedd:82,8"),
                                              receivers.get());
        rcv.eqUnion(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(newRcv),
                                                virtual,
                                                new jedd.PhysicalDomain[] { FD.v() }));
        final jedd.internal.RelationContainer newPt =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1> newPt = pt.get(); at /home/olhotak/soot-2-jedd/" +
                                               "src/soot/jimple/spark/BDDVirtualCalls.jedd:86,8"),
                                              pt.get());
        final jedd.internal.RelationContainer newTypes =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { type.v(), signature.v() },
                                              new jedd.PhysicalDomain[] { T2.v(), H2.v() },
                                              ("<soot.jimple.spark.bdddomains.type:soot.jimple.spark.bdddoma" +
                                               "ins.T2, soot.jimple.spark.bdddomains.signature:soot.jimple.s" +
                                               "park.bdddomains.H2> newTypes = jedd.internal.Jedd.v().compos" +
                                               "e(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().compose" +
                                               "(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(" +
                                               "jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(a" +
                                               "llocNodes), newPt, new jedd.PhysicalDomain[...]), new jedd.P" +
                                               "hysicalDomain[...], new jedd.PhysicalDomain[...])), jedd.int" +
                                               "ernal.Jedd.v().project(varNodes, new jedd.PhysicalDomain[..." +
                                               "]), new jedd.PhysicalDomain[...])), jedd.internal.Jedd.v().p" +
                                               "roject(rcv, new jedd.PhysicalDomain[...]), new jedd.Physical" +
                                               "Domain[...]); at /home/olhotak/soot-2-jedd/src/soot/jimple/s" +
                                               "park/BDDVirtualCalls.jedd:87,8"),
                                              jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(allocNodes),
                                                                                                                                                                                                                                  newPt,
                                                                                                                                                                                                                                  new jedd.PhysicalDomain[] { H1.v() }),
                                                                                                                                                                                                   new jedd.PhysicalDomain[] { V1.v() },
                                                                                                                                                                                                   new jedd.PhysicalDomain[] { V3.v() })),
                                                                                                                                        jedd.internal.Jedd.v().project(varNodes,
                                                                                                                                                                       new jedd.PhysicalDomain[] { V2.v() }),
                                                                                                                                        new jedd.PhysicalDomain[] { V3.v() })),
                                                                             jedd.internal.Jedd.v().project(rcv,
                                                                                                            new jedd.PhysicalDomain[] { FD.v(), T1.v(), ST.v() }),
                                                                             new jedd.PhysicalDomain[] { V1.v() }));
        newTypes.eqMinus(jedd.internal.Jedd.v().project(jedd.internal.Jedd.v().replace(targets,
                                                                                       new jedd.PhysicalDomain[] { T1.v() },
                                                                                       new jedd.PhysicalDomain[] { T2.v() }),
                                                        new jedd.PhysicalDomain[] { T3.v() }));
        final jedd.internal.RelationContainer toResolve =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), signature.v(), supt.v() },
                                              new jedd.PhysicalDomain[] { T1.v(), H2.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.subt:soot.jimple.spark.bdddoma" +
                                               "ins.T1, soot.jimple.spark.bdddomains.signature:soot.jimple.s" +
                                               "park.bdddomains.H2, soot.jimple.spark.bdddomains.supt:soot.j" +
                                               "imple.spark.bdddomains.T2> toResolve = jedd.internal.Jedd.v(" +
                                               ").copy(newTypes, new jedd.PhysicalDomain[...], new jedd.Phys" +
                                               "icalDomain[...]); at /home/olhotak/soot-2-jedd/src/soot/jimp" +
                                               "le/spark/BDDVirtualCalls.jedd:96,8"),
                                              jedd.internal.Jedd.v().copy(newTypes,
                                                                          new jedd.PhysicalDomain[] { T2.v() },
                                                                          new jedd.PhysicalDomain[] { T1.v() }));
        hier.update();
        toResolve.eqUnion(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(toResolve),
                                                                                        jedd.internal.Jedd.v().replace(hier.anySub(),
                                                                                                                       new jedd.PhysicalDomain[] { T2.v() },
                                                                                                                       new jedd.PhysicalDomain[] { T3.v() }),
                                                                                        new jedd.PhysicalDomain[] { T1.v() }),
                                                         new jedd.PhysicalDomain[] { T3.v() },
                                                         new jedd.PhysicalDomain[] { T1.v() }));
        do  {
            final jedd.internal.RelationContainer resolved =
              new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), signature.v(), supt.v(), method.v() },
                                                  new jedd.PhysicalDomain[] { T1.v(), H2.v(), T2.v(), T3.v() },
                                                  ("<soot.jimple.spark.bdddomains.subt:soot.jimple.spark.bdddoma" +
                                                   "ins.T1, soot.jimple.spark.bdddomains.signature:soot.jimple.s" +
                                                   "park.bdddomains.H2, soot.jimple.spark.bdddomains.supt:soot.j" +
                                                   "imple.spark.bdddomains.T2, soot.jimple.spark.bdddomains.meth" +
                                                   "od:soot.jimple.spark.bdddomains.T3> resolved = jedd.internal" +
                                                   ".Jedd.v().join(jedd.internal.Jedd.v().read(toResolve), decla" +
                                                   "resMethod, new jedd.PhysicalDomain[...]); at /home/olhotak/s" +
                                                   "oot-2-jedd/src/soot/jimple/spark/BDDVirtualCalls.jedd:109,12"),
                                                  jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(toResolve),
                                                                              declaresMethod,
                                                                              new jedd.PhysicalDomain[] { T2.v(), H2.v() }));
            toResolve.eqMinus(jedd.internal.Jedd.v().project(resolved, new jedd.PhysicalDomain[] { T3.v() }));
            targets.eqUnion(jedd.internal.Jedd.v().project(resolved, new jedd.PhysicalDomain[] { T2.v() }));
            toResolve.eq(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(toResolve),
                                                                                       jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().replace(hier.extend(),
                                                                                                                                                     new jedd.PhysicalDomain[] { T2.v() },
                                                                                                                                                     new jedd.PhysicalDomain[] { T3.v() }),
                                                                                                                      new jedd.PhysicalDomain[] { T1.v() },
                                                                                                                      new jedd.PhysicalDomain[] { T2.v() }),
                                                                                       new jedd.PhysicalDomain[] { T2.v() }),
                                                        new jedd.PhysicalDomain[] { T3.v() },
                                                        new jedd.PhysicalDomain[] { T2.v() }));
        }while(!jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(toResolve),
                                              jedd.internal.Jedd.v().falseBDD())); 
        out.add(new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), ctxt.v(), local.v(), tgtm.v(), kind.v(), stmt.v(), srcm.v() },
                                                    new jedd.PhysicalDomain[] { H1.v(), V2.v(), V1.v(), T2.v(), FD.v(), ST.v(), T1.v() },
                                                    ("out.add(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v(" +
                                                     ").project(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v()" +
                                                     ".read(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v()." +
                                                     "read(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read" +
                                                     "(allocNodes), jedd.internal.Jedd.v().replace(newPt, new jedd" +
                                                     ".PhysicalDomain[...], new jedd.PhysicalDomain[...]), new jed" +
                                                     "d.PhysicalDomain[...])), varNodes, new jedd.PhysicalDomain[." +
                                                     "..])), jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v()" +
                                                     ".read(rcv), jedd.internal.Jedd.v().replace(targets, new jedd" +
                                                     ".PhysicalDomain[...], new jedd.PhysicalDomain[...]), new jed" +
                                                     "d.PhysicalDomain[...]), new jedd.PhysicalDomain[...]), new j" +
                                                     "edd.PhysicalDomain[...]), new jedd.PhysicalDomain[...], new " +
                                                     "jedd.PhysicalDomain[...])) at /home/olhotak/soot-2-jedd/src/" +
                                                     "soot/jimple/spark/BDDVirtualCalls.jedd:122,8"),
                                                    jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().project(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(allocNodes),
                                                                                                                                                                                                                                                                 jedd.internal.Jedd.v().replace(newPt,
                                                                                                                                                                                                                                                                                                new jedd.PhysicalDomain[] { V1.v() },
                                                                                                                                                                                                                                                                                                new jedd.PhysicalDomain[] { V3.v() }),
                                                                                                                                                                                                                                                                 new jedd.PhysicalDomain[] { H1.v() })),
                                                                                                                                                                                                         varNodes,
                                                                                                                                                                                                         new jedd.PhysicalDomain[] { V3.v() })),
                                                                                                                                              jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(rcv),
                                                                                                                                                                             jedd.internal.Jedd.v().replace(targets,
                                                                                                                                                                                                            new jedd.PhysicalDomain[] { T1.v() },
                                                                                                                                                                                                            new jedd.PhysicalDomain[] { T2.v() }),
                                                                                                                                                                             new jedd.PhysicalDomain[] { H2.v() }),
                                                                                                                                              new jedd.PhysicalDomain[] { T2.v(), V1.v() }),
                                                                                                                  new jedd.PhysicalDomain[] { T2.v() }),
                                                                                   new jedd.PhysicalDomain[] { T3.v() },
                                                                                   new jedd.PhysicalDomain[] { T2.v() })));
    }
}
