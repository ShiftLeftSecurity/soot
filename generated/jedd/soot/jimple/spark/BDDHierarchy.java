package soot.jimple.spark;

import soot.*;
import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import java.util.*;

public final class BDDHierarchy {
    public jedd.internal.RelationContainer subtypeRelation() {
        this.update();
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), supt.v() },
                                                   new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                                   ("return closure; at /home/olhotak/soot-2-jedd/src/soot/jimple" +
                                                    "/spark/BDDHierarchy.jedd:36,8"),
                                                   closure);
    }
    
    public void update() {
        ArrayNumberer tn = Scene.v().getTypeNumberer();
        for (int i = maxType + 1; i < tn.size(); i++) { this.processNewType((Type) tn.get(i)); }
        this.updateClosure();
    }
    
    private final jedd.internal.RelationContainer identity =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), supt.v() },
                                          new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.subt:soot.jimple.spark" +
                                           ".bdddomains.T1, soot.jimple.spark.bdddomains.supt:soot.jimpl" +
                                           "e.spark.bdddomains.T2> identity = jedd.internal.Jedd.v().fal" +
                                           "seBDD() at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/B" +
                                           "DDHierarchy.jedd:53,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final jedd.internal.RelationContainer extend =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), supt.v() },
                                          new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.subt:soot.jimple.spark" +
                                           ".bdddomains.T1, soot.jimple.spark.bdddomains.supt:soot.jimpl" +
                                           "e.spark.bdddomains.T2> extend = jedd.internal.Jedd.v().false" +
                                           "BDD() at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/BDD" +
                                           "Hierarchy.jedd:58,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    public jedd.internal.RelationContainer extend() {
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), supt.v() },
                                                   new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                                   ("return extend; at /home/olhotak/soot-2-jedd/src/soot/jimple/" +
                                                    "spark/BDDHierarchy.jedd:59,35"),
                                                   extend);
    }
    
    private final jedd.internal.RelationContainer implement =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), supt.v() },
                                          new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.subt:soot.jimple.spark" +
                                           ".bdddomains.T1, soot.jimple.spark.bdddomains.supt:soot.jimpl" +
                                           "e.spark.bdddomains.T2> implement = jedd.internal.Jedd.v().fa" +
                                           "lseBDD() at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/" +
                                           "BDDHierarchy.jedd:64,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final jedd.internal.RelationContainer array =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), supt.v() },
                                          new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.subt:soot.jimple.spark" +
                                           ".bdddomains.T1, soot.jimple.spark.bdddomains.supt:soot.jimpl" +
                                           "e.spark.bdddomains.T2> array = jedd.internal.Jedd.v().falseB" +
                                           "DD() at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDH" +
                                           "ierarchy.jedd:72,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final jedd.internal.RelationContainer anySub =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { anyst.v(), type.v() },
                                          new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.anyst:soot.jimple.spar" +
                                           "k.bdddomains.T1, soot.jimple.spark.bdddomains.type:soot.jimp" +
                                           "le.spark.bdddomains.T2> anySub = jedd.internal.Jedd.v().fals" +
                                           "eBDD() at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/BD" +
                                           "DHierarchy.jedd:77,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    public jedd.internal.RelationContainer anySub() {
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { anyst.v(), type.v() },
                                                   new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                                   ("return anySub; at /home/olhotak/soot-2-jedd/src/soot/jimple/" +
                                                    "spark/BDDHierarchy.jedd:78,36"),
                                                   anySub);
    }
    
    private final jedd.internal.RelationContainer closure =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), supt.v() },
                                          new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.subt:soot.jimple.spark" +
                                           ".bdddomains.T1, soot.jimple.spark.bdddomains.supt:soot.jimpl" +
                                           "e.spark.bdddomains.T2> closure = jedd.internal.Jedd.v().fals" +
                                           "eBDD() at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/BD" +
                                           "DHierarchy.jedd:83,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private RefType jlo = RefType.v("java.lang.Object");
    
    private Type jloArray(int dimensions) {
        if (dimensions == 0) return jlo;
        return ArrayType.v(jlo, dimensions);
    }
    
    private int maxType = 0;
    
    private void processNewType(Type t) {
        if (t instanceof RefType) {
            RefType rt = (RefType) t;
            SootClass sc = rt.getSootClass();
            if (sc == null) return;
            identity.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { rt, rt },
                                                            new jedd.Attribute[] { subt.v(), supt.v() },
                                                            new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
            if (sc.hasSuperclass()) {
                extend.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { rt, sc.getSuperclass().getType() },
                                                              new jedd.Attribute[] { subt.v(), supt.v() },
                                                              new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
            }
            for (Iterator ifaceIt = sc.getInterfaces().iterator(); ifaceIt.hasNext(); ) {
                final SootClass iface = (SootClass) ifaceIt.next();
                implement.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { rt, iface.getType() },
                                                                 new jedd.Attribute[] { subt.v(), supt.v() },
                                                                 new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
            }
        } else
            if (t instanceof ArrayType) {
                identity.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { t, t },
                                                                new jedd.Attribute[] { subt.v(), supt.v() },
                                                                new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                ArrayType at = (ArrayType) t;
                if (at.baseType instanceof PrimType) {
                    array.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { at, this.jloArray(at.numDimensions -
                                                                                                    1) },
                                                                 new jedd.Attribute[] { subt.v(), supt.v() },
                                                                 new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                } else
                    if (at.baseType instanceof RefType) {
                        RefType rt = (RefType) at.baseType;
                        if (rt.equals(jlo)) {
                            array.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { at, this.jloArray(at.numDimensions -
                                                                                                            1) },
                                                                         new jedd.Attribute[] { subt.v(), supt.v() },
                                                                         new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                        } else {
                            array.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { at, this.jloArray(at.numDimensions) },
                                                                         new jedd.Attribute[] { subt.v(), supt.v() },
                                                                         new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                        }
                    } else
                        throw new RuntimeException("unhandled: " + at.baseType);
            } else
                if (t instanceof AnySubType) {
                    AnySubType as = (AnySubType) t;
                    anySub.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { as, as.getBase() },
                                                                  new jedd.Attribute[] { anyst.v(), type.v() },
                                                                  new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                    anySub.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { as, NullType.v() },
                                                                  new jedd.Attribute[] { anyst.v(), type.v() },
                                                                  new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                } else
                    if (t instanceof NullType) {
                        identity.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { t, t },
                                                                        new jedd.Attribute[] { subt.v(), supt.v() },
                                                                        new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                    }
        if (t.getNumber() > maxType) maxType = t.getNumber();
    }
    
    private void updateClosure() {
        closure.eqUnion(jedd.internal.Jedd.v().union(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().union(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().union(jedd.internal.Jedd.v().read(extend),
                                                                                                                                                                       implement)),
                                                                                                              array)),
                                                     identity));
        while (!jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(closure),
                                              closure.eqUnion(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(closure),
                                                                                                                            jedd.internal.Jedd.v().replace(closure,
                                                                                                                                                           new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                                                                                                                                           new jedd.PhysicalDomain[] { T3.v(), T1.v() }),
                                                                                                                            new jedd.PhysicalDomain[] { T1.v() }),
                                                                                             new jedd.PhysicalDomain[] { T3.v() },
                                                                                             new jedd.PhysicalDomain[] { T1.v() }))))
            ;
        anySub.eqUnion(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(anySub,
                                                                                                                 new jedd.PhysicalDomain[] { T2.v() },
                                                                                                                 new jedd.PhysicalDomain[] { T3.v() })),
                                                      jedd.internal.Jedd.v().replace(closure,
                                                                                     new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                                                                     new jedd.PhysicalDomain[] { T2.v(), T3.v() }),
                                                      new jedd.PhysicalDomain[] { T3.v() }));
        closure.eqUnion(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(anySub),
                                                                                      jedd.internal.Jedd.v().replace(closure,
                                                                                                                     new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                                                                                                     new jedd.PhysicalDomain[] { T2.v(), T3.v() }),
                                                                                      new jedd.PhysicalDomain[] { T2.v() }),
                                                       new jedd.PhysicalDomain[] { T3.v() },
                                                       new jedd.PhysicalDomain[] { T2.v() }));
    }
    
    public BDDHierarchy() { super(); }
}
