package soot.jimple.spark;

import soot.*;
import soot.util.*;
import soot.jimple.spark.queue.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.toolkits.callgraph.*;
import java.util.*;

public class BDDVirtualCalls extends AbsVirtualCalls {
    BDDVirtualCalls(Rvar_obj pt,
                    Rlocal_srcm_stmt_signature_kind receivers,
                    Rlocal_srcm_stmt_tgtm specials,
                    Qctxt_local_obj_srcm_stmt_kind_tgtm out,
                    Qsrcc_srcm_stmt_kind_tgtc_tgtm statics) {
        super(pt, receivers, specials, out, statics);
        for (Iterator clIt = Scene.v().getClasses().iterator(); clIt.hasNext(); ) {
            final SootClass cl = (SootClass) clIt.next();
            for (Iterator mIt = cl.getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                if (m.isAbstract()) continue;
                declaresMethod.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { m.getDeclaringClass().getType(), m.getNumberedSubSignature(), m },
                                                                      new jedd.Attribute[] { type.v(), signature.v(), method.v() },
                                                                      new jedd.PhysicalDomain[] { T1.v(), H2.v(), T3.v() }));
            }
        }
    }
    
    private int lastVarNode = 1;
    
    private int lastAllocNode = 1;
    
    private final jedd.internal.RelationContainer varNodes =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { ctxt.v(), local.v(), var.v(), type.v() },
                                          new jedd.PhysicalDomain[] { V3.v(), V2.v(), V1.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.ctxt:soot.jimple.spark" +
                                           ".bdddomains.V3, soot.jimple.spark.bdddomains.local:soot.jimp" +
                                           "le.spark.bdddomains.V2, soot.jimple.spark.bdddomains.var:soo" +
                                           "t.jimple.spark.bdddomains.V1, soot.jimple.spark.bdddomains.t" +
                                           "ype> varNodes at /home/olhotak/soot-2-jedd/src/soot/jimple/s" +
                                           "park/BDDVirtualCalls.jedd:55,12-45"));
    
    private final jedd.internal.RelationContainer allocNodes =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), type.v() },
                                          new jedd.PhysicalDomain[] { H1.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.obj, soot.jimple.spark" +
                                           ".bdddomains.type> allocNodes at /home/olhotak/soot-2-jedd/sr" +
                                           "c/soot/jimple/spark/BDDVirtualCalls.jedd:56,12-23"));
    
    private final jedd.internal.RelationContainer virtual =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { kind.v() },
                                          new jedd.PhysicalDomain[] { FD.v() },
                                          ("private <soot.jimple.spark.bdddomains.kind> virtual = jedd.i" +
                                           "nternal.Jedd.v().union(jedd.internal.Jedd.v().read(jedd.inte" +
                                           "rnal.Jedd.v().literal(new java.lang.Object[...], new jedd.At" +
                                           "tribute[...], new jedd.PhysicalDomain[...])), jedd.internal." +
                                           "Jedd.v().literal(new java.lang.Object[...], new jedd.Attribu" +
                                           "te[...], new jedd.PhysicalDomain[...])) at /home/olhotak/soo" +
                                           "t-2-jedd/src/soot/jimple/spark/BDDVirtualCalls.jedd:57,12-18"),
                                          jedd.internal.Jedd.v().union(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().literal(new Object[] { Kind.VIRTUAL },
                                                                                                                                  new jedd.Attribute[] { kind.v() },
                                                                                                                                  new jedd.PhysicalDomain[] { FD.v() })),
                                                                       jedd.internal.Jedd.v().literal(new Object[] { Kind.INTERFACE },
                                                                                                      new jedd.Attribute[] { kind.v() },
                                                                                                      new jedd.PhysicalDomain[] { FD.v() })));
    
    private final jedd.internal.RelationContainer threads =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { type.v() },
                                          new jedd.PhysicalDomain[] { T1.v() },
                                          ("private <soot.jimple.spark.bdddomains.type> threads = jedd.i" +
                                           "nternal.Jedd.v().falseBDD() at /home/olhotak/soot-2-jedd/src" +
                                           "/soot/jimple/spark/BDDVirtualCalls.jedd:58,12-18"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private void updateNodes() {
        for (; lastVarNode <= SparkNumberers.v().varNodeNumberer().size(); lastVarNode++) {
            VarNode vn = (VarNode) SparkNumberers.v().varNodeNumberer().get(lastVarNode);
            if (vn.getVariable() instanceof Local) {
                varNodes.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { vn.context(), vn.getVariable(), vn, vn.getType() },
                                                                new jedd.Attribute[] { ctxt.v(), local.v(), var.v(), type.v() },
                                                                new jedd.PhysicalDomain[] { V3.v(), V2.v(), V1.v(), T2.v() }));
            }
        }
        for (; lastAllocNode <= SparkNumberers.v().allocNodeNumberer().size(); lastAllocNode++) {
            AllocNode an = (AllocNode) SparkNumberers.v().allocNodeNumberer().get(lastAllocNode);
            allocNodes.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { an, an.getType() },
                                                              new jedd.Attribute[] { obj.v(), type.v() },
                                                              new jedd.PhysicalDomain[] { H1.v(), T2.v() }));
            if (an instanceof StringConstantNode) {
                StringConstantNode scn = (StringConstantNode) an;
                String constant = scn.getString();
                if (constant.charAt(0) == '[') {
                    if (constant.length() > 1 && constant.charAt(1) == 'L' &&
                          constant.charAt(constant.length() - 1) == ';') {
                        constant = constant.substring(2, constant.length() - 1);
                    } else
                        constant = null;
                }
                if (constant != null && Scene.v().containsClass(constant)) {
                    SootClass cls = Scene.v().getSootClass(constant);
                    if (cls.declaresMethod(sigClinit)) {
                        SootMethod method = cls.getMethod(sigClinit);
                        stringConstants.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { an, method },
                                                                               new jedd.Attribute[] { obj.v(), tgtm.v() },
                                                                               new jedd.PhysicalDomain[] { H1.v(), T2.v() }));
                    }
                }
            } else {
                nonStringConstants.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { an },
                                                                          new jedd.Attribute[] { obj.v() },
                                                                          new jedd.PhysicalDomain[] { H1.v() }));
            }
        }
        threads.eq(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(hier.subtypeRelation()),
                                                  jedd.internal.Jedd.v().literal(new Object[] { clRunnable },
                                                                                 new jedd.Attribute[] { type.v() },
                                                                                 new jedd.PhysicalDomain[] { T2.v() }),
                                                  new jedd.PhysicalDomain[] { T2.v() }));
    }
    
    protected final RefType clRunnable = RefType.v("java.lang.Runnable");
    
    private final jedd.internal.RelationContainer stringConstants =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), tgtm.v() },
                                          new jedd.PhysicalDomain[] { H1.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.obj, soot.jimple.spark" +
                                           ".bdddomains.tgtm> stringConstants = jedd.internal.Jedd.v().f" +
                                           "alseBDD() at /home/olhotak/soot-2-jedd/src/soot/jimple/spark" +
                                           "/BDDVirtualCalls.jedd:103,12-23"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final jedd.internal.RelationContainer nonStringConstants =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v() },
                                          new jedd.PhysicalDomain[] { H1.v() },
                                          ("private <soot.jimple.spark.bdddomains.obj:soot.jimple.spark." +
                                           "bdddomains.H1> nonStringConstants = jedd.internal.Jedd.v().f" +
                                           "alseBDD() at /home/olhotak/soot-2-jedd/src/soot/jimple/spark" +
                                           "/BDDVirtualCalls.jedd:104,12-20"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final NumberedString sigClinit = Scene.v().getSubSigNumberer().findOrAdd("void <clinit>()");
    
    private final jedd.internal.RelationContainer targets =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { type.v(), signature.v(), method.v() },
                                          new jedd.PhysicalDomain[] { T2.v(), H2.v(), T3.v() },
                                          ("private <soot.jimple.spark.bdddomains.type, soot.jimple.spar" +
                                           "k.bdddomains.signature, soot.jimple.spark.bdddomains.method>" +
                                           " targets = jedd.internal.Jedd.v().falseBDD() at /home/olhota" +
                                           "k/soot-2-jedd/src/soot/jimple/spark/BDDVirtualCalls.jedd:108" +
                                           ",12-37"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final jedd.internal.RelationContainer declaresMethod =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { type.v(), signature.v(), method.v() },
                                          new jedd.PhysicalDomain[] { T1.v(), H2.v(), T3.v() },
                                          ("private <soot.jimple.spark.bdddomains.type, soot.jimple.spar" +
                                           "k.bdddomains.signature, soot.jimple.spark.bdddomains.method:" +
                                           "soot.jimple.spark.bdddomains.T3> declaresMethod = jedd.inter" +
                                           "nal.Jedd.v().falseBDD() at /home/olhotak/soot-2-jedd/src/soo" +
                                           "t/jimple/spark/BDDVirtualCalls.jedd:109,12-40"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private BDDHierarchy hier = new BDDHierarchy();
    
    private final jedd.internal.RelationContainer newPt =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                          ("private <soot.jimple.spark.bdddomains.var, soot.jimple.spark" +
                                           ".bdddomains.obj> newPt = jedd.internal.Jedd.v().falseBDD() a" +
                                           "t /home/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDVirtual" +
                                           "Calls.jedd:112,12-22"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final jedd.internal.RelationContainer allPt =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                          ("private <soot.jimple.spark.bdddomains.var, soot.jimple.spark" +
                                           ".bdddomains.obj> allPt = jedd.internal.Jedd.v().falseBDD() a" +
                                           "t /home/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDVirtual" +
                                           "Calls.jedd:113,12-22"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final jedd.internal.RelationContainer newRcv =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { local.v(), srcm.v(), stmt.v(), signature.v(), kind.v() },
                                          new jedd.PhysicalDomain[] { V2.v(), T1.v(), ST.v(), H2.v(), FD.v() },
                                          ("private <soot.jimple.spark.bdddomains.local, soot.jimple.spa" +
                                           "rk.bdddomains.srcm, soot.jimple.spark.bdddomains.stmt, soot." +
                                           "jimple.spark.bdddomains.signature, soot.jimple.spark.bdddoma" +
                                           "ins.kind> newRcv = jedd.internal.Jedd.v().falseBDD() at /hom" +
                                           "e/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDVirtualCalls." +
                                           "jedd:114,12-48"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final jedd.internal.RelationContainer allRcv =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { local.v(), srcm.v(), stmt.v(), signature.v(), kind.v() },
                                          new jedd.PhysicalDomain[] { V2.v(), T1.v(), ST.v(), H2.v(), FD.v() },
                                          ("private <soot.jimple.spark.bdddomains.local, soot.jimple.spa" +
                                           "rk.bdddomains.srcm, soot.jimple.spark.bdddomains.stmt, soot." +
                                           "jimple.spark.bdddomains.signature, soot.jimple.spark.bdddoma" +
                                           "ins.kind> allRcv = jedd.internal.Jedd.v().falseBDD() at /hom" +
                                           "e/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDVirtualCalls." +
                                           "jedd:115,12-48"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final jedd.internal.RelationContainer newSpc =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { local.v(), srcm.v(), stmt.v(), tgtm.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.local, soot.jimple.spa" +
                                           "rk.bdddomains.srcm, soot.jimple.spark.bdddomains.stmt, soot." +
                                           "jimple.spark.bdddomains.tgtm> newSpc = jedd.internal.Jedd.v(" +
                                           ").falseBDD() at /home/olhotak/soot-2-jedd/src/soot/jimple/sp" +
                                           "ark/BDDVirtualCalls.jedd:116,12-37"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final jedd.internal.RelationContainer allSpc =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { local.v(), srcm.v(), stmt.v(), tgtm.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.local, soot.jimple.spa" +
                                           "rk.bdddomains.srcm, soot.jimple.spark.bdddomains.stmt, soot." +
                                           "jimple.spark.bdddomains.tgtm> allSpc = jedd.internal.Jedd.v(" +
                                           ").falseBDD() at /home/olhotak/soot-2-jedd/src/soot/jimple/sp" +
                                           "ark/BDDVirtualCalls.jedd:117,12-37"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    public void update() {
        this.updateNodes();
        newPt.eq(pt.get());
        allPt.eqUnion(newPt);
        newRcv.eq(jedd.internal.Jedd.v().replace(receivers.get(),
                                                 new jedd.PhysicalDomain[] { V1.v() },
                                                 new jedd.PhysicalDomain[] { V2.v() }));
        allRcv.eqUnion(newRcv);
        newSpc.eq(specials.get());
        allSpc.eqUnion(newSpc);
        this.updateClinits();
        this.updateVirtuals();
        this.updateSpecials();
    }
    
    private void updateClinits() {
        final jedd.internal.RelationContainer clinits =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { local.v(), srcm.v(), stmt.v(), kind.v() },
                                              new jedd.PhysicalDomain[] { V2.v(), T1.v(), ST.v(), FD.v() },
                                              ("<soot.jimple.spark.bdddomains.local:soot.jimple.spark.bdddom" +
                                               "ains.V2, soot.jimple.spark.bdddomains.srcm:soot.jimple.spark" +
                                               ".bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimpl" +
                                               "e.spark.bdddomains.ST, soot.jimple.spark.bdddomains.kind:soo" +
                                               "t.jimple.spark.bdddomains.FD> clinits = jedd.internal.Jedd.v" +
                                               "().join(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().p" +
                                               "roject(allRcv, new jedd.PhysicalDomain[...])), jedd.internal" +
                                               ".Jedd.v().literal(new java.lang.Object[...], new jedd.Attrib" +
                                               "ute[...], new jedd.PhysicalDomain[...]), new jedd.PhysicalDo" +
                                               "main[...]); at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                               "rk/BDDVirtualCalls.jedd:139,34-41"),
                                              jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().project(allRcv,
                                                                                                                                     new jedd.PhysicalDomain[] { H2.v() })),
                                                                          jedd.internal.Jedd.v().literal(new Object[] { Kind.CLINIT },
                                                                                                         new jedd.Attribute[] { kind.v() },
                                                                                                         new jedd.PhysicalDomain[] { FD.v() }),
                                                                          new jedd.PhysicalDomain[] { FD.v() }));
        final jedd.internal.RelationContainer ctxtLocalPt =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { srcc.v(), local.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { V3.v(), V2.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.srcc:soot.jimple.spark.bdddoma" +
                                               "ins.V3, soot.jimple.spark.bdddomains.local:soot.jimple.spark" +
                                               ".bdddomains.V2, soot.jimple.spark.bdddomains.obj:soot.jimple" +
                                               ".spark.bdddomains.H1> ctxtLocalPt = jedd.internal.Jedd.v().c" +
                                               "ompose(jedd.internal.Jedd.v().read(newPt), jedd.internal.Jed" +
                                               "d.v().project(varNodes, new jedd.PhysicalDomain[...]), new j" +
                                               "edd.PhysicalDomain[...]); at /home/olhotak/soot-2-jedd/src/s" +
                                               "oot/jimple/spark/BDDVirtualCalls.jedd:142,27-38"),
                                              jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(newPt),
                                                                             jedd.internal.Jedd.v().project(varNodes,
                                                                                                            new jedd.PhysicalDomain[] { T2.v() }),
                                                                             new jedd.PhysicalDomain[] { V1.v() }));
        final jedd.internal.RelationContainer tgtMethods =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { srcc.v(), local.v(), tgtm.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), V2.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.srcc:soot.jimple.spark.bdddoma" +
                                               "ins.V1, soot.jimple.spark.bdddomains.local:soot.jimple.spark" +
                                               ".bdddomains.V2, soot.jimple.spark.bdddomains.tgtm:soot.jimpl" +
                                               "e.spark.bdddomains.T2> tgtMethods = jedd.internal.Jedd.v().r" +
                                               "eplace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v()" +
                                               ".read(ctxtLocalPt), stringConstants, new jedd.PhysicalDomain" +
                                               "[...]), new jedd.PhysicalDomain[...], new jedd.PhysicalDomai" +
                                               "n[...]); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/" +
                                               "BDDVirtualCalls.jedd:144,28-38"),
                                              jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(ctxtLocalPt),
                                                                                                            stringConstants,
                                                                                                            new jedd.PhysicalDomain[] { H1.v() }),
                                                                             new jedd.PhysicalDomain[] { V3.v() },
                                                                             new jedd.PhysicalDomain[] { V1.v() }));
        statics.add(new jedd.internal.RelationContainer(new jedd.Attribute[] { stmt.v(), srcm.v(), tgtm.v(), kind.v(), srcc.v(), tgtc.v() },
                                                        new jedd.PhysicalDomain[] { ST.v(), T1.v(), T2.v(), FD.v(), V1.v(), V2.v() },
                                                        ("statics.add(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v" +
                                                         "().read(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v(" +
                                                         ").read(tgtMethods), clinits, new jedd.PhysicalDomain[...]))," +
                                                         " jedd.internal.Jedd.v().literal(new java.lang.Object[...], n" +
                                                         "ew jedd.Attribute[...], new jedd.PhysicalDomain[...]), new j" +
                                                         "edd.PhysicalDomain[...])) at /home/olhotak/soot-2-jedd/src/s" +
                                                         "oot/jimple/spark/BDDVirtualCalls.jedd:146,8-15"),
                                                        jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(tgtMethods),
                                                                                                                                               clinits,
                                                                                                                                               new jedd.PhysicalDomain[] { V2.v() })),
                                                                                    jedd.internal.Jedd.v().literal(new Object[] { null },
                                                                                                                   new jedd.Attribute[] { tgtc.v() },
                                                                                                                   new jedd.PhysicalDomain[] { V2.v() }),
                                                                                    new jedd.PhysicalDomain[] {  })));
    }
    
    private final jedd.internal.RelationContainer resolvedSpecials =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { ctxt.v(), local.v(), obj.v(), srcm.v(), stmt.v(), tgtm.v() },
                                          new jedd.PhysicalDomain[] { V3.v(), V2.v(), H1.v(), T1.v(), ST.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.ctxt, soot.jimple.spar" +
                                           "k.bdddomains.local, soot.jimple.spark.bdddomains.obj, soot.j" +
                                           "imple.spark.bdddomains.srcm, soot.jimple.spark.bdddomains.st" +
                                           "mt, soot.jimple.spark.bdddomains.tgtm> resolvedSpecials = je" +
                                           "dd.internal.Jedd.v().falseBDD() at /home/olhotak/soot-2-jedd" +
                                           "/src/soot/jimple/spark/BDDVirtualCalls.jedd:149,12-48"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private void updateSpecials() {
        final jedd.internal.RelationContainer ctxtLocalPt =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { ctxt.v(), local.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { V3.v(), V2.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.ctxt:soot.jimple.spark.bdddoma" +
                                               "ins.V3, soot.jimple.spark.bdddomains.local:soot.jimple.spark" +
                                               ".bdddomains.V2, soot.jimple.spark.bdddomains.obj:soot.jimple" +
                                               ".spark.bdddomains.H1> ctxtLocalPt = jedd.internal.Jedd.v().c" +
                                               "ompose(jedd.internal.Jedd.v().read(newPt), jedd.internal.Jed" +
                                               "d.v().project(varNodes, new jedd.PhysicalDomain[...]), new j" +
                                               "edd.PhysicalDomain[...]); at /home/olhotak/soot-2-jedd/src/s" +
                                               "oot/jimple/spark/BDDVirtualCalls.jedd:152,27-38"),
                                              jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(newPt),
                                                                             jedd.internal.Jedd.v().project(varNodes,
                                                                                                            new jedd.PhysicalDomain[] { T2.v() }),
                                                                             new jedd.PhysicalDomain[] { V1.v() }));
        final jedd.internal.RelationContainer newSpecials =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { ctxt.v(), local.v(), obj.v(), srcm.v(), stmt.v(), tgtm.v() },
                                              new jedd.PhysicalDomain[] { V3.v(), V2.v(), H1.v(), T1.v(), ST.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.ctxt:soot.jimple.spark.bdddoma" +
                                               "ins.V3, soot.jimple.spark.bdddomains.local:soot.jimple.spark" +
                                               ".bdddomains.V2, soot.jimple.spark.bdddomains.obj:soot.jimple" +
                                               ".spark.bdddomains.H1, soot.jimple.spark.bdddomains.srcm:soot" +
                                               ".jimple.spark.bdddomains.T1, soot.jimple.spark.bdddomains.st" +
                                               "mt:soot.jimple.spark.bdddomains.ST, soot.jimple.spark.bdddom" +
                                               "ains.tgtm:soot.jimple.spark.bdddomains.T2> newSpecials = jed" +
                                               "d.internal.Jedd.v().join(jedd.internal.Jedd.v().read(ctxtLoc" +
                                               "alPt), jedd.internal.Jedd.v().replace(allSpc, new jedd.Physi" +
                                               "calDomain[...], new jedd.PhysicalDomain[...]), new jedd.Phys" +
                                               "icalDomain[...]); at /home/olhotak/soot-2-jedd/src/soot/jimp" +
                                               "le/spark/BDDVirtualCalls.jedd:153,45-56"),
                                              jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(ctxtLocalPt),
                                                                          jedd.internal.Jedd.v().replace(allSpc,
                                                                                                         new jedd.PhysicalDomain[] { V1.v() },
                                                                                                         new jedd.PhysicalDomain[] { V2.v() }),
                                                                          new jedd.PhysicalDomain[] { V2.v() }));
        newSpecials.eqMinus(resolvedSpecials);
        resolvedSpecials.eqUnion(newSpecials);
        out.add(new jedd.internal.RelationContainer(new jedd.Attribute[] { stmt.v(), local.v(), srcm.v(), obj.v(), tgtm.v(), ctxt.v(), kind.v() },
                                                    new jedd.PhysicalDomain[] { ST.v(), V1.v(), T1.v(), H1.v(), T2.v(), V2.v(), FD.v() },
                                                    ("out.add(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v(" +
                                                     ").join(jedd.internal.Jedd.v().read(newSpecials), jedd.intern" +
                                                     "al.Jedd.v().literal(new java.lang.Object[...], new jedd.Attr" +
                                                     "ibute[...], new jedd.PhysicalDomain[...]), new jedd.Physical" +
                                                     "Domain[...]), new jedd.PhysicalDomain[...], new jedd.Physica" +
                                                     "lDomain[...])) at /home/olhotak/soot-2-jedd/src/soot/jimple/" +
                                                     "spark/BDDVirtualCalls.jedd:159,8-11"),
                                                    jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(newSpecials),
                                                                                                               jedd.internal.Jedd.v().literal(new Object[] { Kind.SPECIAL },
                                                                                                                                              new jedd.Attribute[] { kind.v() },
                                                                                                                                              new jedd.PhysicalDomain[] { FD.v() }),
                                                                                                               new jedd.PhysicalDomain[] {  }),
                                                                                   new jedd.PhysicalDomain[] { V2.v(), V3.v() },
                                                                                   new jedd.PhysicalDomain[] { V1.v(), V2.v() })));
    }
    
    private void updateVirtuals() {
        final jedd.internal.RelationContainer rcv =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { local.v(), srcm.v(), stmt.v(), signature.v(), kind.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), FD.v() },
                                              ("<soot.jimple.spark.bdddomains.local:soot.jimple.spark.bdddom" +
                                               "ains.V1, soot.jimple.spark.bdddomains.srcm:soot.jimple.spark" +
                                               ".bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimpl" +
                                               "e.spark.bdddomains.ST, soot.jimple.spark.bdddomains.signatur" +
                                               "e:soot.jimple.spark.bdddomains.H2, soot.jimple.spark.bdddoma" +
                                               "ins.kind:soot.jimple.spark.bdddomains.FD> rcv = jedd.interna" +
                                               "l.Jedd.v().join(jedd.internal.Jedd.v().read(jedd.internal.Je" +
                                               "dd.v().replace(allRcv, new jedd.PhysicalDomain[...], new jed" +
                                               "d.PhysicalDomain[...])), virtual, new jedd.PhysicalDomain[.." +
                                               ".]); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDV" +
                                               "irtualCalls.jedd:163,45-48"),
                                              jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(allRcv,
                                                                                                                                     new jedd.PhysicalDomain[] { V2.v() },
                                                                                                                                     new jedd.PhysicalDomain[] { V1.v() })),
                                                                          virtual,
                                                                          new jedd.PhysicalDomain[] { FD.v() }));
        final jedd.internal.RelationContainer threadRcv =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { local.v(), srcm.v(), stmt.v(), signature.v(), kind.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), FD.v() },
                                              ("<soot.jimple.spark.bdddomains.local:soot.jimple.spark.bdddom" +
                                               "ains.V1, soot.jimple.spark.bdddomains.srcm:soot.jimple.spark" +
                                               ".bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimpl" +
                                               "e.spark.bdddomains.ST, soot.jimple.spark.bdddomains.signatur" +
                                               "e:soot.jimple.spark.bdddomains.H2, soot.jimple.spark.bdddoma" +
                                               "ins.kind:soot.jimple.spark.bdddomains.FD> threadRcv = jedd.i" +
                                               "nternal.Jedd.v().join(jedd.internal.Jedd.v().read(jedd.inter" +
                                               "nal.Jedd.v().replace(allRcv, new jedd.PhysicalDomain[...], n" +
                                               "ew jedd.PhysicalDomain[...])), jedd.internal.Jedd.v().litera" +
                                               "l(new java.lang.Object[...], new jedd.Attribute[...], new je" +
                                               "dd.PhysicalDomain[...]), new jedd.PhysicalDomain[...]); at /" +
                                               "home/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDVirtualCal" +
                                               "ls.jedd:166,45-54"),
                                              jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(allRcv,
                                                                                                                                     new jedd.PhysicalDomain[] { V2.v() },
                                                                                                                                     new jedd.PhysicalDomain[] { V1.v() })),
                                                                          jedd.internal.Jedd.v().literal(new Object[] { Kind.THREAD },
                                                                                                         new jedd.Attribute[] { kind.v() },
                                                                                                         new jedd.PhysicalDomain[] { FD.v() }),
                                                                          new jedd.PhysicalDomain[] { FD.v() }));
        final jedd.internal.RelationContainer ptTypes =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { local.v(), type.v() },
                                              new jedd.PhysicalDomain[] { V2.v(), T1.v() },
                                              ("<soot.jimple.spark.bdddomains.local:soot.jimple.spark.bdddom" +
                                               "ains.V2, soot.jimple.spark.bdddomains.type:soot.jimple.spark" +
                                               ".bdddomains.T1> ptTypes = jedd.internal.Jedd.v().compose(jed" +
                                               "d.internal.Jedd.v().read(jedd.internal.Jedd.v().compose(jedd" +
                                               ".internal.Jedd.v().read(jedd.internal.Jedd.v().replace(alloc" +
                                               "Nodes, new jedd.PhysicalDomain[...], new jedd.PhysicalDomain" +
                                               "[...])), newPt, new jedd.PhysicalDomain[...])), jedd.interna" +
                                               "l.Jedd.v().project(varNodes, new jedd.PhysicalDomain[...]), " +
                                               "new jedd.PhysicalDomain[...]); at /home/olhotak/soot-2-jedd/" +
                                               "src/soot/jimple/spark/BDDVirtualCalls.jedd:170,22-29"),
                                              jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(allocNodes,
                                                                                                                                                                                                   new jedd.PhysicalDomain[] { T2.v() },
                                                                                                                                                                                                   new jedd.PhysicalDomain[] { T1.v() })),
                                                                                                                                        newPt,
                                                                                                                                        new jedd.PhysicalDomain[] { H1.v() })),
                                                                             jedd.internal.Jedd.v().project(varNodes,
                                                                                                            new jedd.PhysicalDomain[] { T2.v(), V3.v() }),
                                                                             new jedd.PhysicalDomain[] { V1.v() }));
        final jedd.internal.RelationContainer newTypes =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { type.v(), signature.v() },
                                              new jedd.PhysicalDomain[] { T2.v(), H2.v() },
                                              ("<soot.jimple.spark.bdddomains.type:soot.jimple.spark.bdddoma" +
                                               "ins.T2, soot.jimple.spark.bdddomains.signature:soot.jimple.s" +
                                               "park.bdddomains.H2> newTypes = jedd.internal.Jedd.v().replac" +
                                               "e(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read" +
                                               "(ptTypes), jedd.internal.Jedd.v().project(jedd.internal.Jedd" +
                                               ".v().replace(rcv, new jedd.PhysicalDomain[...], new jedd.Phy" +
                                               "sicalDomain[...]), new jedd.PhysicalDomain[...]), new jedd.P" +
                                               "hysicalDomain[...]), new jedd.PhysicalDomain[...], new jedd." +
                                               "PhysicalDomain[...]); at /home/olhotak/soot-2-jedd/src/soot/" +
                                               "jimple/spark/BDDVirtualCalls.jedd:173,26-34"),
                                              jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(ptTypes),
                                                                                                            jedd.internal.Jedd.v().project(jedd.internal.Jedd.v().replace(rcv,
                                                                                                                                                                          new jedd.PhysicalDomain[] { V1.v() },
                                                                                                                                                                          new jedd.PhysicalDomain[] { V2.v() }),
                                                                                                                                           new jedd.PhysicalDomain[] { ST.v(), T1.v(), FD.v() }),
                                                                                                            new jedd.PhysicalDomain[] { V2.v() }),
                                                                             new jedd.PhysicalDomain[] { T1.v() },
                                                                             new jedd.PhysicalDomain[] { T2.v() }));
        newTypes.eqUnion(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(ptTypes),
                                                                                                                                               threads,
                                                                                                                                               new jedd.PhysicalDomain[] { T1.v() })),
                                                                                       jedd.internal.Jedd.v().project(jedd.internal.Jedd.v().replace(threadRcv,
                                                                                                                                                     new jedd.PhysicalDomain[] { V1.v() },
                                                                                                                                                     new jedd.PhysicalDomain[] { V2.v() }),
                                                                                                                      new jedd.PhysicalDomain[] { ST.v(), T1.v(), FD.v() }),
                                                                                       new jedd.PhysicalDomain[] { V2.v() }),
                                                        new jedd.PhysicalDomain[] { T1.v() },
                                                        new jedd.PhysicalDomain[] { T2.v() }));
        hier.update();
        newTypes.eqUnion(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(newTypes,
                                                                                                                   new jedd.PhysicalDomain[] { T2.v() },
                                                                                                                   new jedd.PhysicalDomain[] { T1.v() })),
                                                        hier.anySub(),
                                                        new jedd.PhysicalDomain[] { T1.v() }));
        newTypes.eqMinus(jedd.internal.Jedd.v().project(targets, new jedd.PhysicalDomain[] { T3.v() }));
        final jedd.internal.RelationContainer toResolve =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), signature.v(), supt.v() },
                                              new jedd.PhysicalDomain[] { T2.v(), H2.v(), T1.v() },
                                              ("<soot.jimple.spark.bdddomains.subt:soot.jimple.spark.bdddoma" +
                                               "ins.T2, soot.jimple.spark.bdddomains.signature:soot.jimple.s" +
                                               "park.bdddomains.H2, soot.jimple.spark.bdddomains.supt:soot.j" +
                                               "imple.spark.bdddomains.T1> toResolve = jedd.internal.Jedd.v(" +
                                               ").copy(jedd.internal.Jedd.v().replace(newTypes, new jedd.Phy" +
                                               "sicalDomain[...], new jedd.PhysicalDomain[...]), new jedd.Ph" +
                                               "ysicalDomain[...], new jedd.PhysicalDomain[...]); at /home/o" +
                                               "lhotak/soot-2-jedd/src/soot/jimple/spark/BDDVirtualCalls.jed" +
                                               "d:188,32-41"),
                                              jedd.internal.Jedd.v().copy(jedd.internal.Jedd.v().replace(newTypes,
                                                                                                         new jedd.PhysicalDomain[] { T2.v() },
                                                                                                         new jedd.PhysicalDomain[] { T1.v() }),
                                                                          new jedd.PhysicalDomain[] { T1.v() },
                                                                          new jedd.PhysicalDomain[] { T2.v() }));
        do  {
            final jedd.internal.RelationContainer resolved =
              new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), signature.v(), supt.v(), method.v() },
                                                  new jedd.PhysicalDomain[] { T2.v(), H2.v(), T1.v(), T3.v() },
                                                  ("<soot.jimple.spark.bdddomains.subt:soot.jimple.spark.bdddoma" +
                                                   "ins.T2, soot.jimple.spark.bdddomains.signature:soot.jimple.s" +
                                                   "park.bdddomains.H2, soot.jimple.spark.bdddomains.supt:soot.j" +
                                                   "imple.spark.bdddomains.T1, soot.jimple.spark.bdddomains.meth" +
                                                   "od:soot.jimple.spark.bdddomains.T3> resolved = jedd.internal" +
                                                   ".Jedd.v().join(jedd.internal.Jedd.v().read(toResolve), decla" +
                                                   "resMethod, new jedd.PhysicalDomain[...]); at /home/olhotak/s" +
                                                   "oot-2-jedd/src/soot/jimple/spark/BDDVirtualCalls.jedd:193,44" +
                                                   "-52"),
                                                  jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(toResolve),
                                                                              declaresMethod,
                                                                              new jedd.PhysicalDomain[] { T1.v(), H2.v() }));
            toResolve.eqMinus(jedd.internal.Jedd.v().project(resolved, new jedd.PhysicalDomain[] { T3.v() }));
            targets.eqUnion(jedd.internal.Jedd.v().project(resolved, new jedd.PhysicalDomain[] { T1.v() }));
            toResolve.eq(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(toResolve),
                                                                                       jedd.internal.Jedd.v().replace(hier.extend(),
                                                                                                                      new jedd.PhysicalDomain[] { T2.v() },
                                                                                                                      new jedd.PhysicalDomain[] { T3.v() }),
                                                                                       new jedd.PhysicalDomain[] { T1.v() }),
                                                        new jedd.PhysicalDomain[] { T3.v() },
                                                        new jedd.PhysicalDomain[] { T1.v() }));
        }while(!jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(toResolve),
                                              jedd.internal.Jedd.v().falseBDD())); 
        final jedd.internal.RelationContainer typedPt =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v(), type.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), H1.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1, soot.jimple.spark.bdddomains.type:soot.jimple.s" +
                                               "park.bdddomains.T2> typedPt = jedd.internal.Jedd.v().join(je" +
                                               "dd.internal.Jedd.v().read(allocNodes), newPt, new jedd.Physi" +
                                               "calDomain[...]); at /home/olhotak/soot-2-jedd/src/soot/jimpl" +
                                               "e/spark/BDDVirtualCalls.jedd:206,25-32"),
                                              jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(allocNodes),
                                                                          newPt,
                                                                          new jedd.PhysicalDomain[] { H1.v() }));
        typedPt.eqUnion(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(typedPt,
                                                                                                                  new jedd.PhysicalDomain[] { T2.v() },
                                                                                                                  new jedd.PhysicalDomain[] { T1.v() })),
                                                       hier.anySub(),
                                                       new jedd.PhysicalDomain[] { T1.v() }));
        final jedd.internal.RelationContainer localCtxtPt =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { ctxt.v(), local.v(), supt.v(), obj.v(), type.v() },
                                              new jedd.PhysicalDomain[] { V3.v(), V2.v(), T2.v(), H1.v(), T1.v() },
                                              ("<soot.jimple.spark.bdddomains.ctxt:soot.jimple.spark.bdddoma" +
                                               "ins.V3, soot.jimple.spark.bdddomains.local:soot.jimple.spark" +
                                               ".bdddomains.V2, soot.jimple.spark.bdddomains.supt:soot.jimpl" +
                                               "e.spark.bdddomains.T2, soot.jimple.spark.bdddomains.obj:soot" +
                                               ".jimple.spark.bdddomains.H1, soot.jimple.spark.bdddomains.ty" +
                                               "pe:soot.jimple.spark.bdddomains.T1> localCtxtPt = jedd.inter" +
                                               "nal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.intern" +
                                               "al.Jedd.v().replace(typedPt, new jedd.PhysicalDomain[...], n" +
                                               "ew jedd.PhysicalDomain[...])), varNodes, new jedd.PhysicalDo" +
                                               "main[...]); at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                               "rk/BDDVirtualCalls.jedd:212,39-50"),
                                              jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(typedPt,
                                                                                                                                        new jedd.PhysicalDomain[] { T2.v() },
                                                                                                                                        new jedd.PhysicalDomain[] { T1.v() })),
                                                                             varNodes,
                                                                             new jedd.PhysicalDomain[] { V1.v() }));
        localCtxtPt.eq(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(localCtxtPt),
                                                   hier.subtypeRelation(),
                                                   new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
        final jedd.internal.RelationContainer callSiteTargets =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { local.v(), srcm.v(), stmt.v(), type.v(), kind.v(), tgtm.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), T2.v(), FD.v(), T3.v() },
                                              ("<soot.jimple.spark.bdddomains.local:soot.jimple.spark.bdddom" +
                                               "ains.V1, soot.jimple.spark.bdddomains.srcm:soot.jimple.spark" +
                                               ".bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimpl" +
                                               "e.spark.bdddomains.ST, soot.jimple.spark.bdddomains.type:soo" +
                                               "t.jimple.spark.bdddomains.T2, soot.jimple.spark.bdddomains.k" +
                                               "ind:soot.jimple.spark.bdddomains.FD, soot.jimple.spark.bdddo" +
                                               "mains.tgtm:soot.jimple.spark.bdddomains.T3> callSiteTargets " +
                                               "= jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read" +
                                               "(jedd.internal.Jedd.v().union(jedd.internal.Jedd.v().read(rc" +
                                               "v), threadRcv)), targets, new jedd.PhysicalDomain[...]); at " +
                                               "/home/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDVirtualCa" +
                                               "lls.jedd:220,46-61"),
                                              jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().union(jedd.internal.Jedd.v().read(rcv),
                                                                                                                                      threadRcv)),
                                                                             targets,
                                                                             new jedd.PhysicalDomain[] { H2.v() }));
        out.add(new jedd.internal.RelationContainer(new jedd.Attribute[] { local.v(), obj.v(), ctxt.v(), stmt.v(), srcm.v(), tgtm.v(), kind.v() },
                                                    new jedd.PhysicalDomain[] { V1.v(), H1.v(), V2.v(), ST.v(), T1.v(), T2.v(), FD.v() },
                                                    ("out.add(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v(" +
                                                     ").project(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v()" +
                                                     ".read(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v()." +
                                                     "replace(jedd.internal.Jedd.v().project(localCtxtPt, new jedd" +
                                                     ".PhysicalDomain[...]), new jedd.PhysicalDomain[...], new jed" +
                                                     "d.PhysicalDomain[...]), new jedd.PhysicalDomain[...], new je" +
                                                     "dd.PhysicalDomain[...])), callSiteTargets, new jedd.Physical" +
                                                     "Domain[...]), new jedd.PhysicalDomain[...]), new jedd.Physic" +
                                                     "alDomain[...], new jedd.PhysicalDomain[...])) at /home/olhot" +
                                                     "ak/soot-2-jedd/src/soot/jimple/spark/BDDVirtualCalls.jedd:22" +
                                                     "3,8-11"),
                                                    jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().project(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().project(localCtxtPt,
                                                                                                                                                                                                                                                                       new jedd.PhysicalDomain[] { T2.v() }),
                                                                                                                                                                                                                                        new jedd.PhysicalDomain[] { V2.v() },
                                                                                                                                                                                                                                        new jedd.PhysicalDomain[] { V1.v() }),
                                                                                                                                                                                                         new jedd.PhysicalDomain[] { T1.v() },
                                                                                                                                                                                                         new jedd.PhysicalDomain[] { T2.v() })),
                                                                                                                                              callSiteTargets,
                                                                                                                                              new jedd.PhysicalDomain[] { T2.v(), V1.v() }),
                                                                                                                  new jedd.PhysicalDomain[] { T2.v() }),
                                                                                   new jedd.PhysicalDomain[] { T3.v(), V3.v() },
                                                                                   new jedd.PhysicalDomain[] { T2.v(), V2.v() })));
    }
}
