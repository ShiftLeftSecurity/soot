package soot.jimple.toolkits.pointer;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.*;
import java.util.*;
import soot.util.*;
import soot.jimple.spark.*;
import soot.jimple.spark.pag.*;
import soot.jimple.spark.pag.*;
import soot.jimple.spark.bdddomains.*;

public class BDDSideEffectAnalysis {
    public BDDSideEffectAnalysis(BDDPAG pag, BDDCallGraph cg, BDDReachableMethods rm) {
        super();
        this.pag = pag;
        this.cg = cg;
        this.rm = rm;
    }
    
    BDDPAG pag;
    
    BDDCallGraph cg;
    
    BDDReachableMethods rm;
    
    final jedd.internal.RelationContainer ntread =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                                          new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), H1.v() },
                                          ("<soot.jimple.spark.bdddomains.method, soot.jimple.spark.bddd" +
                                           "omains.stmt:soot.jimple.spark.bdddomains.ST, soot.jimple.spa" +
                                           "rk.bdddomains.fld:soot.jimple.spark.bdddomains.FD, soot.jimp" +
                                           "le.spark.bdddomains.obj> ntread = jedd.internal.Jedd.v().fal" +
                                           "seBDD() at /home/olhotak/soot-2-jedd/src/soot/jimple/toolkit" +
                                           "s/pointer/BDDSideEffectAnalysis.jedd:45,4"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    final jedd.internal.RelationContainer ntwrite =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), ST.v(), FD.v(), H1.v() },
                                          ("<soot.jimple.spark.bdddomains.method, soot.jimple.spark.bddd" +
                                           "omains.stmt:soot.jimple.spark.bdddomains.ST, soot.jimple.spa" +
                                           "rk.bdddomains.fld:soot.jimple.spark.bdddomains.FD, soot.jimp" +
                                           "le.spark.bdddomains.obj> ntwrite = jedd.internal.Jedd.v().fa" +
                                           "lseBDD() at /home/olhotak/soot-2-jedd/src/soot/jimple/toolki" +
                                           "ts/pointer/BDDSideEffectAnalysis.jedd:46,4"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    final jedd.internal.RelationContainer read =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                                          new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), H1.v() },
                                          ("<soot.jimple.spark.bdddomains.method, soot.jimple.spark.bddd" +
                                           "omains.stmt, soot.jimple.spark.bdddomains.fld, soot.jimple.s" +
                                           "park.bdddomains.obj> read = jedd.internal.Jedd.v().falseBDD(" +
                                           ") at /home/olhotak/soot-2-jedd/src/soot/jimple/toolkits/poin" +
                                           "ter/BDDSideEffectAnalysis.jedd:50,4"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    final jedd.internal.RelationContainer write =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), ST.v(), FD.v(), H1.v() },
                                          ("<soot.jimple.spark.bdddomains.method, soot.jimple.spark.bddd" +
                                           "omains.stmt, soot.jimple.spark.bdddomains.fld, soot.jimple.s" +
                                           "park.bdddomains.obj> write = jedd.internal.Jedd.v().falseBDD" +
                                           "() at /home/olhotak/soot-2-jedd/src/soot/jimple/toolkits/poi" +
                                           "nter/BDDSideEffectAnalysis.jedd:51,4"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private jedd.internal.RelationContainer addValue(Value v, SootMethod m, Stmt s) {
        RWSet ret = null;
        if (v instanceof InstanceFieldRef) {
            System.out.println("***" + m + s + v);
            Scene.v().getUnitNumberer().add(s);
            InstanceFieldRef ifr = (InstanceFieldRef) v;
            return new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), fld.v(), stmt.v(), obj.v() },
                                                       new jedd.PhysicalDomain[] { V2.v(), FD.v(), ST.v(), H1.v() },
                                                       ("return jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v()" +
                                                        ".read(jedd.internal.Jedd.v().literal(new java.lang.Object[.." +
                                                        ".], new jedd.Attribute[...], new jedd.PhysicalDomain[...]))," +
                                                        " pag.pointsTo, new jedd.PhysicalDomain[...]); at /home/olhot" +
                                                        "ak/soot-2-jedd/src/soot/jimple/toolkits/pointer/BDDSideEffec" +
                                                        "tAnalysis.jedd:61,12"),
                                                       jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().literal(new Object[] { m, s, ifr.getField(), pag.findLocalVarNode((Local)
                                                                                                                                                                                                             ifr.getBase()) },
                                                                                                                                                 new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), var.v() },
                                                                                                                                                 new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), V1.v() })),
                                                                                      pag.pointsTo,
                                                                                      new jedd.PhysicalDomain[] { V1.v() }));
        } else
            if (v instanceof StaticFieldRef) {
                System.out.println("***" + m + s + v);
                Scene.v().getUnitNumberer().add(s);
                StaticFieldRef sfr = (StaticFieldRef) v;
                return new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                                                           new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), H1.v() },
                                                           ("return jedd.internal.Jedd.v().literal(new java.lang.Object[." +
                                                            "..], new jedd.Attribute[...], new jedd.PhysicalDomain[...]);" +
                                                            " at /home/olhotak/soot-2-jedd/src/soot/jimple/toolkits/point" +
                                                            "er/BDDSideEffectAnalysis.jedd:72,12"),
                                                           jedd.internal.Jedd.v().literal(new Object[] { m, s, sfr.getField(), null },
                                                                                          new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                                                                                          new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), H1.v() }));
            } else
                if (v instanceof ArrayRef) {
                    System.out.println("***" + m + s + v);
                    Scene.v().getUnitNumberer().add(s);
                    ArrayRef ar = (ArrayRef) v;
                    return new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), fld.v(), stmt.v(), obj.v() },
                                                               new jedd.PhysicalDomain[] { V2.v(), FD.v(), ST.v(), H1.v() },
                                                               ("return jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v()" +
                                                                ".read(jedd.internal.Jedd.v().literal(new java.lang.Object[.." +
                                                                ".], new jedd.Attribute[...], new jedd.PhysicalDomain[...]))," +
                                                                " pag.pointsTo, new jedd.PhysicalDomain[...]); at /home/olhot" +
                                                                "ak/soot-2-jedd/src/soot/jimple/toolkits/pointer/BDDSideEffec" +
                                                                "tAnalysis.jedd:81,12"),
                                                               jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().literal(new Object[] { m, s, ArrayElement.v(), pag.findLocalVarNode((Local)
                                                                                                                                                                                                                       ar.getBase()) },
                                                                                                                                                         new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), var.v() },
                                                                                                                                                         new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), V1.v() })),
                                                                                              pag.pointsTo,
                                                                                              new jedd.PhysicalDomain[] { V1.v() }));
                }
        return new jedd.internal.RelationContainer(new jedd.Attribute[] {  },
                                                   new jedd.PhysicalDomain[] {  },
                                                   ("return jedd.internal.Jedd.v().falseBDD(); at /home/olhotak/s" +
                                                    "oot-2-jedd/src/soot/jimple/toolkits/pointer/BDDSideEffectAna" +
                                                    "lysis.jedd:89,1"),
                                                   jedd.internal.Jedd.v().falseBDD());
    }
    
    private jedd.internal.RelationContainer ntReadSet(SootMethod m, Stmt s) {
        if (s instanceof AssignStmt) {
            AssignStmt a = (AssignStmt) s;
            Value r = a.getRightOp();
            return new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                                                       new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), H1.v() },
                                                       ("return this.addValue(r, m, s); at /home/olhotak/soot-2-jedd/" +
                                                        "src/soot/jimple/toolkits/pointer/BDDSideEffectAnalysis.jedd:" +
                                                        "97,5"),
                                                       this.addValue(r, m, s));
        }
        return new jedd.internal.RelationContainer(new jedd.Attribute[] {  },
                                                   new jedd.PhysicalDomain[] {  },
                                                   ("return jedd.internal.Jedd.v().falseBDD(); at /home/olhotak/s" +
                                                    "oot-2-jedd/src/soot/jimple/toolkits/pointer/BDDSideEffectAna" +
                                                    "lysis.jedd:99,8"),
                                                   jedd.internal.Jedd.v().falseBDD());
    }
    
    private jedd.internal.RelationContainer ntWriteSet(SootMethod m, Stmt s) {
        if (s instanceof AssignStmt) {
            AssignStmt a = (AssignStmt) s;
            Value l = a.getLeftOp();
            return new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                                                       new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), H1.v() },
                                                       ("return this.addValue(l, m, s); at /home/olhotak/soot-2-jedd/" +
                                                        "src/soot/jimple/toolkits/pointer/BDDSideEffectAnalysis.jedd:" +
                                                        "107,5"),
                                                       this.addValue(l, m, s));
        }
        return new jedd.internal.RelationContainer(new jedd.Attribute[] {  },
                                                   new jedd.PhysicalDomain[] {  },
                                                   ("return jedd.internal.Jedd.v().falseBDD(); at /home/olhotak/s" +
                                                    "oot-2-jedd/src/soot/jimple/toolkits/pointer/BDDSideEffectAna" +
                                                    "lysis.jedd:109,8"),
                                                   jedd.internal.Jedd.v().falseBDD());
    }
    
    private void findNTRWSets(SootMethod m) {
        for (Iterator sIt = m.retrieveActiveBody().getUnits().iterator(); sIt.hasNext(); ) {
            final Stmt s = (Stmt) sIt.next();
            ntwrite.eqUnion(jedd.internal.Jedd.v().replace(this.ntWriteSet(m, s),
                                                           new jedd.PhysicalDomain[] { V2.v() },
                                                           new jedd.PhysicalDomain[] { V1.v() }));
            ntread.eqUnion(this.ntReadSet(m, s));
        }
        SootClass c = m.getDeclaringClass();
        if (!c.isApplicationClass()) { m.releaseActiveBody(); }
    }
    
    private void closure() {
        read.eqUnion(ntread);
        write.eqUnion(ntwrite);
        final jedd.internal.RelationContainer closecg =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { srcm.v(), stmt.v(), tgtm.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), ST.v(), V2.v() },
                                              ("<soot.jimple.spark.bdddomains.srcm:soot.jimple.spark.bdddoma" +
                                               "ins.V1, soot.jimple.spark.bdddomains.stmt:soot.jimple.spark." +
                                               "bdddomains.ST, soot.jimple.spark.bdddomains.tgtm:soot.jimple" +
                                               ".spark.bdddomains.V2> closecg = jedd.internal.Jedd.v().proje" +
                                               "ct(cg.edges, new jedd.PhysicalDomain[...]); at /home/olhotak" +
                                               "/soot-2-jedd/src/soot/jimple/toolkits/pointer/BDDSideEffectA" +
                                               "nalysis.jedd:133,8"),
                                              jedd.internal.Jedd.v().project(cg.edges,
                                                                             new jedd.PhysicalDomain[] { T2.v(), T1.v(), H2.v() }));
        while (!jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(closecg),
                                              closecg.eqUnion(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(closecg,
                                                                                                                                                        new jedd.PhysicalDomain[] { V2.v() },
                                                                                                                                                        new jedd.PhysicalDomain[] { V3.v() })),
                                                                                             jedd.internal.Jedd.v().project(jedd.internal.Jedd.v().replace(closecg,
                                                                                                                                                           new jedd.PhysicalDomain[] { V1.v() },
                                                                                                                                                           new jedd.PhysicalDomain[] { V3.v() }),
                                                                                                                            new jedd.PhysicalDomain[] { ST.v() }),
                                                                                             new jedd.PhysicalDomain[] { V3.v() }))))
            ;
        read.eqUnion(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(closecg),
                                                                                   jedd.internal.Jedd.v().project(ntread,
                                                                                                                  new jedd.PhysicalDomain[] { ST.v() }),
                                                                                   new jedd.PhysicalDomain[] { V2.v() }),
                                                    new jedd.PhysicalDomain[] { V1.v() },
                                                    new jedd.PhysicalDomain[] { V2.v() }));
        write.eqUnion(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(closecg),
                                                     jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().project(ntwrite,
                                                                                                                   new jedd.PhysicalDomain[] { ST.v() }),
                                                                                    new jedd.PhysicalDomain[] { V1.v() },
                                                                                    new jedd.PhysicalDomain[] { V2.v() }),
                                                     new jedd.PhysicalDomain[] { V2.v() }));
    }
    
    public void analyze() {
        System.out.println("side effect analysis analyzing methods");
        for (Iterator mIt =
               new jedd.internal.RelationContainer(new jedd.Attribute[] { tgtm.v() },
                                                   new jedd.PhysicalDomain[] { V2.v() },
                                                   ("jedd.internal.Jedd.v().project(cg.edges, new jedd.PhysicalDo" +
                                                    "main[...]).iterator() at /home/olhotak/soot-2-jedd/src/soot/" +
                                                    "jimple/toolkits/pointer/BDDSideEffectAnalysis.jedd:144,80"),
                                                   jedd.internal.Jedd.v().project(cg.edges,
                                                                                  new jedd.PhysicalDomain[] { T2.v(), V1.v(), T1.v(), H2.v(), ST.v() })).iterator();
             mIt.hasNext();
             ) {
            final SootMethod m = (SootMethod) mIt.next();
            if (m.isAbstract()) continue;
            if (m.isNative()) continue;
            this.findNTRWSets(m);
        }
        System.out.println("side effect analysis calculating closure");
        this.closure();
        System.out.println("side effect analysis done");
        this.output();
    }
    
    private void output() {
        final jedd.internal.RelationContainer methods =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v() },
                                              new jedd.PhysicalDomain[] { V1.v() },
                                              ("<soot.jimple.spark.bdddomains.method:soot.jimple.spark.bdddo" +
                                               "mains.V1> methods = jedd.internal.Jedd.v().falseBDD(); at /h" +
                                               "ome/olhotak/soot-2-jedd/src/soot/jimple/toolkits/pointer/BDD" +
                                               "SideEffectAnalysis.jedd:158,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        for (Iterator cIt = Scene.v().getApplicationClasses().iterator(); cIt.hasNext(); ) {
            final SootClass c = (SootClass) cIt.next();
            for (Iterator mIt = c.getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                System.out.println("adding reachable method " + m);
                methods.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { m },
                                                               new jedd.Attribute[] { method.v() },
                                                               new jedd.PhysicalDomain[] { V1.v() }));
            }
        }
        System.out.println("Read sets:\n" +
                           new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), fld.v(), stmt.v(), obj.v() },
                                                               new jedd.PhysicalDomain[] { V1.v(), FD.v(), ST.v(), H1.v() },
                                                               ("jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(jedd" +
                                                                ".internal.Jedd.v().replace(read, new jedd.PhysicalDomain[..." +
                                                                "], new jedd.PhysicalDomain[...])), methods, new jedd.Physica" +
                                                                "lDomain[...]).toString() at /home/olhotak/soot-2-jedd/src/so" +
                                                                "ot/jimple/toolkits/pointer/BDDSideEffectAnalysis.jedd:167,79"),
                                                               jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(read,
                                                                                                                                                      new jedd.PhysicalDomain[] { V2.v() },
                                                                                                                                                      new jedd.PhysicalDomain[] { V1.v() })),
                                                                                           methods,
                                                                                           new jedd.PhysicalDomain[] { V1.v() })).toString());
        System.out.println("Write sets:\n" +
                           new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), fld.v(), stmt.v(), obj.v() },
                                                               new jedd.PhysicalDomain[] { V1.v(), FD.v(), ST.v(), H1.v() },
                                                               ("jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(writ" +
                                                                "e), methods, new jedd.PhysicalDomain[...]).toString() at /ho" +
                                                                "me/olhotak/soot-2-jedd/src/soot/jimple/toolkits/pointer/BDDS" +
                                                                "ideEffectAnalysis.jedd:168,81"),
                                                               jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(write),
                                                                                           methods,
                                                                                           new jedd.PhysicalDomain[] { V1.v() })).toString());
        System.out.println("NTRead:\n" +
                           new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), fld.v(), stmt.v(), obj.v() },
                                                               new jedd.PhysicalDomain[] { V2.v(), FD.v(), ST.v(), H1.v() },
                                                               ("ntread.toString() at /home/olhotak/soot-2-jedd/src/soot/jimp" +
                                                                "le/toolkits/pointer/BDDSideEffectAnalysis.jedd:169,40"),
                                                               ntread).toString());
        System.out.println("NTWrite:\n" +
                           new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), fld.v(), stmt.v(), obj.v() },
                                                               new jedd.PhysicalDomain[] { V1.v(), FD.v(), ST.v(), H1.v() },
                                                               ("ntwrite.toString() at /home/olhotak/soot-2-jedd/src/soot/jim" +
                                                                "ple/toolkits/pointer/BDDSideEffectAnalysis.jedd:170,41"),
                                                               ntwrite).toString());
    }
}
