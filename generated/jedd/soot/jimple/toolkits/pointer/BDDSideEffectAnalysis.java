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
    
    final jedd.Relation ntread =
      new jedd.Relation(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                        new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), H1.v() },
                        jedd.Jedd.v().falseBDD());
    
    final jedd.Relation ntwrite =
      new jedd.Relation(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                        new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), H1.v() },
                        jedd.Jedd.v().falseBDD());
    
    final jedd.Relation read =
      new jedd.Relation(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                        new jedd.PhysicalDomain[] { V1.v(), ST.v(), FD.v(), H1.v() },
                        jedd.Jedd.v().falseBDD());
    
    final jedd.Relation write =
      new jedd.Relation(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                        new jedd.PhysicalDomain[] { V1.v(), ST.v(), FD.v(), H1.v() },
                        jedd.Jedd.v().falseBDD());
    
    private jedd.Relation addValue(Value v, SootMethod m, Stmt s) {
        RWSet ret = null;
        if (v instanceof InstanceFieldRef) {
            System.out.println("***" + m + s + v);
            Scene.v().getUnitNumberer().add(s);
            InstanceFieldRef ifr = (InstanceFieldRef) v;
            return new jedd.Relation(new jedd.Attribute[] { method.v(), fld.v(), stmt.v(), obj.v() },
                                     new jedd.PhysicalDomain[] { V2.v(), FD.v(), ST.v(), H1.v() },
                                     jedd.Jedd.v().compose(jedd.Jedd.v().read(jedd.Jedd.v().literal(new Object[] { m, s, ifr.getField(), this.pag.findLocalVarNode((Local)
                                                                                                                                                                     ifr.getBase()) },
                                                                                                    new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), var.v() },
                                                                                                    new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), V1.v() })),
                                                           this.pag.pointsTo,
                                                           new jedd.PhysicalDomain[] { V1.v() }));
        } else
            if (v instanceof StaticFieldRef) {
                System.out.println("***" + m + s + v);
                Scene.v().getUnitNumberer().add(s);
                StaticFieldRef sfr = (StaticFieldRef) v;
                return new jedd.Relation(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                                         new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), H1.v() },
                                         jedd.Jedd.v().literal(new Object[] { m, s, sfr.getField(), null },
                                                               new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                                                               new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), H1.v() }));
            } else
                if (v instanceof ArrayRef) {
                    System.out.println("***" + m + s + v);
                    Scene.v().getUnitNumberer().add(s);
                    ArrayRef ar = (ArrayRef) v;
                    return new jedd.Relation(new jedd.Attribute[] { method.v(), fld.v(), stmt.v(), obj.v() },
                                             new jedd.PhysicalDomain[] { V2.v(), FD.v(), ST.v(), H1.v() },
                                             jedd.Jedd.v().compose(jedd.Jedd.v().read(jedd.Jedd.v().literal(new Object[] { m, s, ArrayElement.v(), this.pag.findLocalVarNode((Local)
                                                                                                                                                                               ar.getBase()) },
                                                                                                            new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), var.v() },
                                                                                                            new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), V1.v() })),
                                                                   this.pag.pointsTo,
                                                                   new jedd.PhysicalDomain[] { V1.v() }));
                }
        return new jedd.Relation(new jedd.Attribute[] {  }, new jedd.PhysicalDomain[] {  }, jedd.Jedd.v().falseBDD());
    }
    
    private jedd.Relation ntReadSet(SootMethod m, Stmt s) {
        if (s instanceof AssignStmt) {
            AssignStmt a = (AssignStmt) s;
            Value r = a.getRightOp();
            return new jedd.Relation(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                                     new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), H1.v() },
                                     this.addValue(r, m, s));
        }
        return new jedd.Relation(new jedd.Attribute[] {  }, new jedd.PhysicalDomain[] {  }, jedd.Jedd.v().falseBDD());
    }
    
    private jedd.Relation ntWriteSet(SootMethod m, Stmt s) {
        if (s instanceof AssignStmt) {
            AssignStmt a = (AssignStmt) s;
            Value l = a.getLeftOp();
            return new jedd.Relation(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                                     new jedd.PhysicalDomain[] { V2.v(), ST.v(), FD.v(), H1.v() },
                                     this.addValue(l, m, s));
        }
        return new jedd.Relation(new jedd.Attribute[] {  }, new jedd.PhysicalDomain[] {  }, jedd.Jedd.v().falseBDD());
    }
    
    private void findNTRWSets(SootMethod m) {
        for (Iterator sIt = m.retrieveActiveBody().getUnits().iterator(); sIt.hasNext(); ) {
            final Stmt s = (Stmt) sIt.next();
            this.ntwrite.eqUnion(this.ntWriteSet(m, s));
            this.ntread.eqUnion(this.ntReadSet(m, s));
        }
        SootClass c = m.getDeclaringClass();
        if (!c.isApplicationClass()) { m.releaseActiveBody(); }
    }
    
    private void closure() {
        this.read.eqUnion(jedd.Jedd.v().replace(this.ntread,
                                                new jedd.PhysicalDomain[] { V2.v() },
                                                new jedd.PhysicalDomain[] { V1.v() }));
        this.write.eqUnion(jedd.Jedd.v().replace(this.ntwrite,
                                                 new jedd.PhysicalDomain[] { V2.v() },
                                                 new jedd.PhysicalDomain[] { V1.v() }));
        final jedd.Relation closecg =
          new jedd.Relation(new jedd.Attribute[] { srcm.v(), stmt.v(), tgtm.v() },
                            new jedd.PhysicalDomain[] { V1.v(), ST.v(), V2.v() },
                            jedd.Jedd.v().project(this.cg.edges, new jedd.PhysicalDomain[] { T1.v(), H2.v(), T2.v() }));
        while (!jedd.Jedd.v().equals(jedd.Jedd.v().read(closecg),
                                     closecg.eqUnion(jedd.Jedd.v().compose(jedd.Jedd.v().read(jedd.Jedd.v().replace(closecg,
                                                                                                                    new jedd.PhysicalDomain[] { V2.v() },
                                                                                                                    new jedd.PhysicalDomain[] { V3.v() })),
                                                                           jedd.Jedd.v().project(jedd.Jedd.v().replace(closecg,
                                                                                                                       new jedd.PhysicalDomain[] { V1.v() },
                                                                                                                       new jedd.PhysicalDomain[] { V3.v() }),
                                                                                                 new jedd.PhysicalDomain[] { ST.v() }),
                                                                           new jedd.PhysicalDomain[] { V3.v() }))))
            ;
        this.read.eqUnion(jedd.Jedd.v().compose(jedd.Jedd.v().read(closecg),
                                                jedd.Jedd.v().project(this.ntread,
                                                                      new jedd.PhysicalDomain[] { ST.v() }),
                                                new jedd.PhysicalDomain[] { V2.v() }));
        this.write.eqUnion(jedd.Jedd.v().compose(jedd.Jedd.v().read(closecg),
                                                 jedd.Jedd.v().project(this.ntwrite,
                                                                       new jedd.PhysicalDomain[] { ST.v() }),
                                                 new jedd.PhysicalDomain[] { V2.v() }));
    }
    
    public void analyze() {
        System.out.println("side effect analysis analyzing methods");
        for (Iterator mIt =
               new jedd.Relation(new jedd.Attribute[] { tgtm.v() },
                                 new jedd.PhysicalDomain[] { V2.v() },
                                 jedd.Jedd.v().project(this.cg.edges,
                                                       new jedd.PhysicalDomain[] { T1.v(), ST.v(), V1.v(), H2.v(), T2.v() })).iterator();
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
        final jedd.Relation methods =
          new jedd.Relation(new jedd.Attribute[] { method.v() },
                            new jedd.PhysicalDomain[] { V1.v() },
                            jedd.Jedd.v().falseBDD());
        for (Iterator cIt = Scene.v().getApplicationClasses().iterator(); cIt.hasNext(); ) {
            final SootClass c = (SootClass) cIt.next();
            for (Iterator mIt = c.getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                System.out.println("adding reachable method " + m);
                methods.eqUnion(jedd.Jedd.v().literal(new Object[] { m },
                                                      new jedd.Attribute[] { method.v() },
                                                      new jedd.PhysicalDomain[] { V1.v() }));
            }
        }
        System.out.println("Read sets:\n" +
                           new jedd.Relation(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                                             new jedd.PhysicalDomain[] { V1.v(), ST.v(), FD.v(), H1.v() },
                                             jedd.Jedd.v().join(jedd.Jedd.v().read(this.read),
                                                                methods,
                                                                new jedd.PhysicalDomain[] { V1.v() })).toString());
        System.out.println("Write sets:\n" +
                           new jedd.Relation(new jedd.Attribute[] { method.v(), stmt.v(), fld.v(), obj.v() },
                                             new jedd.PhysicalDomain[] { V1.v(), ST.v(), FD.v(), H1.v() },
                                             jedd.Jedd.v().join(jedd.Jedd.v().read(this.write),
                                                                methods,
                                                                new jedd.PhysicalDomain[] { V1.v() })).toString());
        System.out.println("NTRead:\n" +
                           new jedd.Relation(new jedd.Attribute[] { method.v(), fld.v(), stmt.v(), obj.v() },
                                             new jedd.PhysicalDomain[] { V2.v(), FD.v(), ST.v(), H1.v() },
                                             this.ntread).toString());
        System.out.println("NTWrite:\n" +
                           new jedd.Relation(new jedd.Attribute[] { method.v(), fld.v(), stmt.v(), obj.v() },
                                             new jedd.PhysicalDomain[] { V2.v(), FD.v(), ST.v(), H1.v() },
                                             this.ntwrite).toString());
    }
}
