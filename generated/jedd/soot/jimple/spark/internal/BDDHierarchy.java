package soot.jimple.spark.internal;

import soot.*;
import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import java.util.*;

public final class BDDHierarchy {
    public jedd.Relation subtypeRelation() {
        this.update();
        return new jedd.Relation(new jedd.Attribute[] { supt.v(), subt.v() },
                                 new jedd.PhysicalDomain[] { T2.v(), T1.v() },
                                 this.closure);
    }
    
    public void update() {
        ArrayNumberer tn = Scene.v().getTypeNumberer();
        for (int i = this.maxType + 1; i < tn.size(); i++) { this.processNewType((Type) tn.get(i)); }
        this.updateClosure();
    }
    
    private final jedd.Relation identity =
      new jedd.Relation(new jedd.Attribute[] { subt.v(), supt.v() },
                        new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                        jedd.Jedd.v().falseBDD());
    
    private final jedd.Relation extend =
      new jedd.Relation(new jedd.Attribute[] { subt.v(), supt.v() },
                        new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                        jedd.Jedd.v().falseBDD());
    
    public jedd.Relation extend() {
        return new jedd.Relation(new jedd.Attribute[] { supt.v(), subt.v() },
                                 new jedd.PhysicalDomain[] { T2.v(), T1.v() },
                                 this.extend);
    }
    
    private final jedd.Relation implement =
      new jedd.Relation(new jedd.Attribute[] { subt.v(), supt.v() },
                        new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                        jedd.Jedd.v().falseBDD());
    
    private final jedd.Relation array =
      new jedd.Relation(new jedd.Attribute[] { subt.v(), supt.v() },
                        new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                        jedd.Jedd.v().falseBDD());
    
    private final jedd.Relation anySub =
      new jedd.Relation(new jedd.Attribute[] { anyst.v(), type.v() },
                        new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                        jedd.Jedd.v().falseBDD());
    
    public jedd.Relation anySub() {
        return new jedd.Relation(new jedd.Attribute[] { anyst.v(), type.v() },
                                 new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                 this.anySub);
    }
    
    private final jedd.Relation closure =
      new jedd.Relation(new jedd.Attribute[] { subt.v(), supt.v() },
                        new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                        jedd.Jedd.v().falseBDD());
    
    private RefType jlo = RefType.v("java.lang.Object");
    
    private Type jloArray(int dimensions) {
        if (dimensions == 0) return this.jlo;
        return ArrayType.v(this.jlo, dimensions);
    }
    
    private int maxType = 0;
    
    private void processNewType(Type t) {
        if (t instanceof RefType) {
            RefType rt = (RefType) t;
            SootClass sc = rt.getSootClass();
            if (sc == null) return;
            this.identity.eqUnion(jedd.Jedd.v().literal(new Object[] { rt, rt },
                                                        new jedd.Attribute[] { subt.v(), supt.v() },
                                                        new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
            if (sc.hasSuperclass()) {
                this.extend.eqUnion(jedd.Jedd.v().literal(new Object[] { rt, sc.getSuperclass().getType() },
                                                          new jedd.Attribute[] { subt.v(), supt.v() },
                                                          new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
            }
            for (Iterator ifaceIt = sc.getInterfaces().iterator(); ifaceIt.hasNext(); ) {
                final SootClass iface = (SootClass) ifaceIt.next();
                this.implement.eqUnion(jedd.Jedd.v().literal(new Object[] { rt, iface.getType() },
                                                             new jedd.Attribute[] { subt.v(), supt.v() },
                                                             new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
            }
        } else
            if (t instanceof ArrayType) {
                this.identity.eqUnion(jedd.Jedd.v().literal(new Object[] { t, t },
                                                            new jedd.Attribute[] { subt.v(), supt.v() },
                                                            new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                ArrayType at = (ArrayType) t;
                if (at.baseType instanceof PrimType) {
                    this.array.eqUnion(jedd.Jedd.v().literal(new Object[] { at, this.jloArray(at.numDimensions - 1) },
                                                             new jedd.Attribute[] { subt.v(), supt.v() },
                                                             new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                } else
                    if (at.baseType instanceof RefType) {
                        RefType rt = (RefType) at.baseType;
                        if (rt.equals(this.jlo)) {
                            this.array.eqUnion(jedd.Jedd.v().literal(new Object[] { at, this.jloArray(at.numDimensions -
                                                                                                        1) },
                                                                     new jedd.Attribute[] { subt.v(), supt.v() },
                                                                     new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                        } else {
                            this.array.eqUnion(jedd.Jedd.v().literal(new Object[] { at, this.jloArray(at.numDimensions) },
                                                                     new jedd.Attribute[] { subt.v(), supt.v() },
                                                                     new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                        }
                    } else
                        throw new RuntimeException("unhandled: " + at.baseType);
            } else
                if (t instanceof AnySubType) {
                    AnySubType as = (AnySubType) t;
                    this.anySub.eqUnion(jedd.Jedd.v().literal(new Object[] { as, as.getBase() },
                                                              new jedd.Attribute[] { anyst.v(), type.v() },
                                                              new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                    this.anySub.eqUnion(jedd.Jedd.v().literal(new Object[] { as, NullType.v() },
                                                              new jedd.Attribute[] { anyst.v(), type.v() },
                                                              new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                } else
                    if (t instanceof NullType) {
                        this.identity.eqUnion(jedd.Jedd.v().literal(new Object[] { t, t },
                                                                    new jedd.Attribute[] { subt.v(), supt.v() },
                                                                    new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                    }
        if (t.getNumber() > this.maxType) this.maxType = t.getNumber();
    }
    
    private void updateClosure() {
        this.closure.eqUnion(jedd.Jedd.v().union(jedd.Jedd.v().read(jedd.Jedd.v().union(jedd.Jedd.v().read(jedd.Jedd.v().union(jedd.Jedd.v().read(this.extend),
                                                                                                                               this.implement)),
                                                                                        this.array)),
                                                 this.identity));
        while (!jedd.Jedd.v().equals(jedd.Jedd.v().read(this.closure),
                                     this.closure.eqUnion(jedd.Jedd.v().replace(jedd.Jedd.v().compose(jedd.Jedd.v().read(this.closure),
                                                                                                      jedd.Jedd.v().replace(jedd.Jedd.v().replace(this.closure,
                                                                                                                                                  new jedd.PhysicalDomain[] { T1.v() },
                                                                                                                                                  new jedd.PhysicalDomain[] { T3.v() }),
                                                                                                                            new jedd.PhysicalDomain[] { T2.v() },
                                                                                                                            new jedd.PhysicalDomain[] { T1.v() }),
                                                                                                      new jedd.PhysicalDomain[] { T1.v() }),
                                                                                new jedd.PhysicalDomain[] { T3.v() },
                                                                                new jedd.PhysicalDomain[] { T1.v() }))))
            ;
        this.anySub.eqUnion(jedd.Jedd.v().compose(jedd.Jedd.v().read(jedd.Jedd.v().replace(this.anySub,
                                                                                           new jedd.PhysicalDomain[] { T2.v() },
                                                                                           new jedd.PhysicalDomain[] { T3.v() })),
                                                  jedd.Jedd.v().replace(jedd.Jedd.v().replace(this.closure,
                                                                                              new jedd.PhysicalDomain[] { T2.v() },
                                                                                              new jedd.PhysicalDomain[] { T3.v() }),
                                                                        new jedd.PhysicalDomain[] { T1.v() },
                                                                        new jedd.PhysicalDomain[] { T2.v() }),
                                                  new jedd.PhysicalDomain[] { T3.v() }));
        this.closure.eqUnion(jedd.Jedd.v().replace(jedd.Jedd.v().compose(jedd.Jedd.v().read(this.anySub),
                                                                         jedd.Jedd.v().replace(jedd.Jedd.v().replace(this.closure,
                                                                                                                     new jedd.PhysicalDomain[] { T2.v() },
                                                                                                                     new jedd.PhysicalDomain[] { T3.v() }),
                                                                                               new jedd.PhysicalDomain[] { T1.v() },
                                                                                               new jedd.PhysicalDomain[] { T2.v() }),
                                                                         new jedd.PhysicalDomain[] { T2.v() }),
                                                   new jedd.PhysicalDomain[] { T3.v() },
                                                   new jedd.PhysicalDomain[] { T2.v() }));
    }
    
    public BDDHierarchy() { super(); }
}
