package soot.jimple.spark.internal;

import soot.jimple.spark.*;
import soot.jimple.spark.pag.*;
import soot.*;
import soot.util.*;
import soot.util.queue.*;
import soot.Type;
import soot.options.SparkOptions;
import soot.jimple.spark.bdddomains.*;
import java.util.*;

public final class BDDTypeManager extends AbstractTypeManager {
    public BDDTypeManager(BDDPAG bddpag) {
        super(bddpag);
        bddhier = Scene.v().getOrMakeBDDHierarchy();
    }
    
    public final void clearTypeMask() {
        lastAllocNode = 0;
        lastVarNode = 0;
        typeMask.eq(jedd.internal.Jedd.v().falseBDD());
        varNodeType.eq(jedd.internal.Jedd.v().falseBDD());
        allocNodeType.eq(jedd.internal.Jedd.v().falseBDD());
        typeSubtype.eq(jedd.internal.Jedd.v().falseBDD());
        seenVTypes = new HashSet();
        seenATypes = new HashSet();
    }
    
    public final void makeTypeMask() {
        if (fh == null) { typeMask.eq(jedd.internal.Jedd.v().trueBDD()); }
        this.update();
    }
    
    public final jedd.internal.RelationContainer get() {
        this.update();
        bddhier.update();
        final jedd.internal.RelationContainer hier =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), supt.v() },
                                              new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.subt:soot.jimple.spark.bdddoma" +
                                               "ins.T1, soot.jimple.spark.bdddomains.supt:soot.jimple.spark." +
                                               "bdddomains.T2> hier = bddhier.subtypeRelation(); at /home/ol" +
                                               "hotak/soot-2-jedd/src/soot/jimple/spark/internal/BDDTypeMana" +
                                               "ger.jedd:64,8"),
                                              bddhier.subtypeRelation());
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                                   new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                                   ("return jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v()" +
                                                    ".read(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v()." +
                                                    "read(jedd.internal.Jedd.v().replace(varNodeType, new jedd.Ph" +
                                                    "ysicalDomain[...], new jedd.PhysicalDomain[...])), hier, new" +
                                                    " jedd.PhysicalDomain[...])), allocNodeType, new jedd.Physica" +
                                                    "lDomain[...]); at /home/olhotak/soot-2-jedd/src/soot/jimple/" +
                                                    "spark/internal/BDDTypeManager.jedd:65,8"),
                                                   jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(varNodeType,
                                                                                                                                                                                                        new jedd.PhysicalDomain[] { T1.v() },
                                                                                                                                                                                                        new jedd.PhysicalDomain[] { T2.v() })),
                                                                                                                                             hier,
                                                                                                                                             new jedd.PhysicalDomain[] { T2.v() })),
                                                                                  allocNodeType,
                                                                                  new jedd.PhysicalDomain[] { T1.v() }));
    }
    
    int lastAllocNode = 0;
    
    int lastVarNode = 0;
    
    private void update() {
        if (fh == null) return;
        ArrayNumberer anNumb = pag.getAllocNodeNumberer();
        ArrayNumberer vnNumb = pag.getVarNodeNumberer();
        HashSet newSeenVTypes = new HashSet();
        HashSet newSeenATypes = new HashSet();
        for (int j = lastAllocNode + 1; j <= anNumb.size(); j++) {
            AllocNode an = (AllocNode) anNumb.get(j);
            Type antype = an.getType();
            newAnType.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { an, antype },
                                                             new jedd.Attribute[] { obj.v(), type.v() },
                                                             new jedd.PhysicalDomain[] { H1.v(), T1.v() }));
            if (!seenATypes.contains(antype)) newSeenATypes.add(antype);
        }
        for (int i = lastVarNode + 1; i <= vnNumb.size(); i++) {
            VarNode vn = (VarNode) vnNumb.get(i);
            Type vntype = vn.getType();
            newVnType.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { vn, vntype },
                                                             new jedd.Attribute[] { var.v(), type.v() },
                                                             new jedd.PhysicalDomain[] { V1.v(), T1.v() }));
            if (!seenVTypes.contains(vntype)) newSeenVTypes.add(vntype);
        }
        seenATypes.addAll(newSeenATypes);
        for (Iterator antypeIt = seenATypes.iterator(); antypeIt.hasNext(); ) {
            final Type antype = (Type) antypeIt.next();
            for (Iterator vntypeIt = newSeenVTypes.iterator(); vntypeIt.hasNext(); ) {
                final Type vntype = (Type) vntypeIt.next();
                if (this.castNeverFails(antype, vntype)) {
                    typeSubtype.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { antype, vntype },
                                                                       new jedd.Attribute[] { subt.v(), supt.v() },
                                                                       new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                }
            }
        }
        for (Iterator antypeIt = newSeenATypes.iterator(); antypeIt.hasNext(); ) {
            final Type antype = (Type) antypeIt.next();
            for (Iterator vntypeIt = seenVTypes.iterator(); vntypeIt.hasNext(); ) {
                final Type vntype = (Type) vntypeIt.next();
                if (this.castNeverFails(antype, vntype)) {
                    typeSubtype.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { antype, vntype },
                                                                       new jedd.Attribute[] { subt.v(), supt.v() },
                                                                       new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                }
            }
        }
        seenVTypes.addAll(newSeenVTypes);
        varNodeType.eqUnion(newVnType);
        allocNodeType.eqUnion(newAnType);
        final jedd.internal.RelationContainer tmp =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), subt.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.subt:soot.jimple.spark.b" +
                                               "dddomains.T1> tmp; at /home/olhotak/soot-2-jedd/src/soot/jim" +
                                               "ple/spark/internal/BDDTypeManager.jedd:122,8"));
        final jedd.internal.RelationContainer tmp2 =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1> tmp2; at /home/olhotak/soot-2-jedd/src/soot/jim" +
                                               "ple/spark/internal/BDDTypeManager.jedd:123,8"));
        tmp.eq(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(typeSubtype),
                                              jedd.internal.Jedd.v().replace(newVnType,
                                                                             new jedd.PhysicalDomain[] { T1.v() },
                                                                             new jedd.PhysicalDomain[] { T2.v() }),
                                              new jedd.PhysicalDomain[] { T2.v() }));
        tmp2.eq(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(allocNodeType),
                                               tmp,
                                               new jedd.PhysicalDomain[] { T1.v() }));
        typeMask.eqUnion(tmp2);
        tmp.eq(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(typeSubtype),
                                              jedd.internal.Jedd.v().replace(varNodeType,
                                                                             new jedd.PhysicalDomain[] { T1.v() },
                                                                             new jedd.PhysicalDomain[] { T2.v() }),
                                              new jedd.PhysicalDomain[] { T2.v() }));
        tmp2.eq(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(newAnType),
                                               tmp,
                                               new jedd.PhysicalDomain[] { T1.v() }));
        typeMask.eqUnion(tmp2);
        newVnType.eq(jedd.internal.Jedd.v().falseBDD());
        newAnType.eq(jedd.internal.Jedd.v().falseBDD());
        lastAllocNode = anNumb.size();
        lastVarNode = vnNumb.size();
        tmp.eq(jedd.internal.Jedd.v().falseBDD());
        tmp2.eq(jedd.internal.Jedd.v().falseBDD());
        bddhier.update();
        final jedd.internal.RelationContainer bddts =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), supt.v() },
                                              new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.subt:soot.jimple.spark.bdddoma" +
                                               "ins.T1, soot.jimple.spark.bdddomains.supt:soot.jimple.spark." +
                                               "bdddomains.T2> bddts = bddhier.subtypeRelation(); at /home/o" +
                                               "lhotak/soot-2-jedd/src/soot/jimple/spark/internal/BDDTypeMan" +
                                               "ager.jedd:143,8"),
                                              bddhier.subtypeRelation());
        bddts.eq(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(bddts),
                                             jedd.internal.Jedd.v().project(jedd.internal.Jedd.v().replace(varNodeType,
                                                                                                           new jedd.PhysicalDomain[] { T1.v() },
                                                                                                           new jedd.PhysicalDomain[] { T2.v() }),
                                                                            new jedd.PhysicalDomain[] { V1.v() }),
                                             new jedd.PhysicalDomain[] { T2.v() }));
        bddts.eq(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(bddts),
                                             jedd.internal.Jedd.v().project(allocNodeType,
                                                                            new jedd.PhysicalDomain[] { H1.v() }),
                                             new jedd.PhysicalDomain[] { T1.v() }));
        if (jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bddts), typeSubtype)) {
            System.out.println("bdd and non-bdd hierarchy match");
        } else {
            System.out.println("bdd and non-bdd hierarchy don\'t match");
            System.out.println("size of bdd: " +
                               new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), supt.v() },
                                                                   new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                                                   ("bddts.size() at /home/olhotak/soot-2-jedd/src/soot/jimple/sp" +
                                                                    "ark/internal/BDDTypeManager.jedd:150,48"),
                                                                   bddts).size() +
                               " size of non-bdd: " +
                               new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), supt.v() },
                                                                   new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                                                   ("typeSubtype.size() at /home/olhotak/soot-2-jedd/src/soot/jim" +
                                                                    "ple/spark/internal/BDDTypeManager.jedd:150,82"),
                                                                   typeSubtype).size());
            System.out.println("missing pairs: ");
            System.out.println(new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), supt.v() },
                                                                   new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                                                   ("jedd.internal.Jedd.v().minus(jedd.internal.Jedd.v().read(bdd" +
                                                                    "ts), typeSubtype).toString() at /home/olhotak/soot-2-jedd/sr" +
                                                                    "c/soot/jimple/spark/internal/BDDTypeManager.jedd:152,52"),
                                                                   jedd.internal.Jedd.v().minus(jedd.internal.Jedd.v().read(bddts),
                                                                                                typeSubtype)).toString());
        }
    }
    
    final jedd.internal.RelationContainer typeSubtype =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), supt.v() },
                                          new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                          ("<soot.jimple.spark.bdddomains.subt:soot.jimple.spark.bdddoma" +
                                           "ins.T1, soot.jimple.spark.bdddomains.supt:soot.jimple.spark." +
                                           "bdddomains.T2> typeSubtype at /home/olhotak/soot-2-jedd/src/" +
                                           "soot/jimple/spark/internal/BDDTypeManager.jedd:158,4"));
    
    final jedd.internal.RelationContainer varNodeType =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), type.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                          ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                           "ns.V1, soot.jimple.spark.bdddomains.type:soot.jimple.spark.b" +
                                           "dddomains.T1> varNodeType at /home/olhotak/soot-2-jedd/src/s" +
                                           "oot/jimple/spark/internal/BDDTypeManager.jedd:159,4"));
    
    final jedd.internal.RelationContainer newVnType =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), type.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                          ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                           "ns.V1, soot.jimple.spark.bdddomains.type:soot.jimple.spark.b" +
                                           "dddomains.T1> newVnType at /home/olhotak/soot-2-jedd/src/soo" +
                                           "t/jimple/spark/internal/BDDTypeManager.jedd:160,4"));
    
    final jedd.internal.RelationContainer allocNodeType =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), type.v() },
                                          new jedd.PhysicalDomain[] { H1.v(), T1.v() },
                                          ("<soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bdddomai" +
                                           "ns.H1, soot.jimple.spark.bdddomains.type:soot.jimple.spark.b" +
                                           "dddomains.T1> allocNodeType at /home/olhotak/soot-2-jedd/src" +
                                           "/soot/jimple/spark/internal/BDDTypeManager.jedd:161,4"));
    
    public jedd.internal.RelationContainer allocNodeType() {
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { type.v(), obj.v() },
                                                   new jedd.PhysicalDomain[] { T1.v(), H1.v() },
                                                   ("return allocNodeType; at /home/olhotak/soot-2-jedd/src/soot/" +
                                                    "jimple/spark/internal/BDDTypeManager.jedd:162,41"),
                                                   allocNodeType);
    }
    
    final jedd.internal.RelationContainer newAnType =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), type.v() },
                                          new jedd.PhysicalDomain[] { H1.v(), T1.v() },
                                          ("<soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bdddomai" +
                                           "ns.H1, soot.jimple.spark.bdddomains.type:soot.jimple.spark.b" +
                                           "dddomains.T1> newAnType at /home/olhotak/soot-2-jedd/src/soo" +
                                           "t/jimple/spark/internal/BDDTypeManager.jedd:163,4"));
    
    final jedd.internal.RelationContainer typeMask =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                          ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                           "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                           "ddomains.H1> typeMask at /home/olhotak/soot-2-jedd/src/soot/" +
                                           "jimple/spark/internal/BDDTypeManager.jedd:164,4"));
    
    HashSet seenVTypes = new HashSet();
    
    HashSet seenATypes = new HashSet();
    
    private BDDHierarchy bddhier;
}
