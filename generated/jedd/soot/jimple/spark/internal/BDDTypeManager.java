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
        this.bddhier = new BDDHierarchy();
    }
    
    public final void clearTypeMask() {
        this.lastAllocNode = 0;
        this.lastVarNode = 0;
        this.typeMask.eq(jedd.Jedd.v().falseBDD());
        this.varNodeType.eq(jedd.Jedd.v().falseBDD());
        this.allocNodeType.eq(jedd.Jedd.v().falseBDD());
        this.typeSubtype.eq(jedd.Jedd.v().falseBDD());
        this.seenVTypes = new HashSet();
        this.seenATypes = new HashSet();
    }
    
    public final void makeTypeMask() {
        if (this.fh == null) { this.typeMask.eq(jedd.Jedd.v().trueBDD()); }
        this.update();
    }
    
    public final jedd.Relation get() {
        this.update();
        this.bddhier.update();
        final jedd.Relation hier =
          new jedd.Relation(new jedd.Domain[] { subt.v(), supt.v() },
                            new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                            this.bddhier.subtypeRelation());
        return new jedd.Relation(new jedd.Domain[] { var.v(), obj.v() },
                                 new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                 jedd.Jedd.v().relprod(jedd.Jedd.v().read(jedd.Jedd.v().relprod(jedd.Jedd.v().read(jedd.Jedd.v().replace(this.varNodeType,
                                                                                                                                         new jedd.PhysicalDomain[] { T1.v() },
                                                                                                                                         new jedd.PhysicalDomain[] { T2.v() })),
                                                                                                hier,
                                                                                                new jedd.PhysicalDomain[] { T2.v() })),
                                                       this.allocNodeType,
                                                       new jedd.PhysicalDomain[] { T1.v() }));
    }
    
    int lastAllocNode = 0;
    
    int lastVarNode = 0;
    
    private void update() {
        if (this.fh == null) return;
        ArrayNumberer anNumb = this.pag.getAllocNodeNumberer();
        ArrayNumberer vnNumb = this.pag.getVarNodeNumberer();
        HashSet newSeenVTypes = new HashSet();
        HashSet newSeenATypes = new HashSet();
        for (int j = this.lastAllocNode + 1; j <= anNumb.size(); j++) {
            AllocNode an = (AllocNode) anNumb.get(j);
            Type antype = an.getType();
            this.newAnType.eqUnion(jedd.Jedd.v().literal(new Object[] { an, antype },
                                                         new jedd.Domain[] { obj.v(), type.v() },
                                                         new jedd.PhysicalDomain[] { H1.v(), T1.v() }));
            if (!this.seenATypes.contains(antype)) newSeenATypes.add(antype);
        }
        for (int i = this.lastVarNode + 1; i <= vnNumb.size(); i++) {
            VarNode vn = (VarNode) vnNumb.get(i);
            Type vntype = vn.getType();
            this.newVnType.eqUnion(jedd.Jedd.v().literal(new Object[] { vn, vntype },
                                                         new jedd.Domain[] { var.v(), type.v() },
                                                         new jedd.PhysicalDomain[] { V1.v(), T1.v() }));
            if (!this.seenVTypes.contains(vntype)) newSeenVTypes.add(vntype);
        }
        this.seenATypes.addAll(newSeenATypes);
        for (Iterator antypeIt = this.seenATypes.iterator(); antypeIt.hasNext(); ) {
            final Type antype = (Type) antypeIt.next();
            for (Iterator vntypeIt = newSeenVTypes.iterator(); vntypeIt.hasNext(); ) {
                final Type vntype = (Type) vntypeIt.next();
                if (this.castNeverFails(antype, vntype)) {
                    this.typeSubtype.eqUnion(jedd.Jedd.v().literal(new Object[] { antype, vntype },
                                                                   new jedd.Domain[] { subt.v(), supt.v() },
                                                                   new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                }
            }
        }
        for (Iterator antypeIt = newSeenATypes.iterator(); antypeIt.hasNext(); ) {
            final Type antype = (Type) antypeIt.next();
            for (Iterator vntypeIt = this.seenVTypes.iterator(); vntypeIt.hasNext(); ) {
                final Type vntype = (Type) vntypeIt.next();
                if (this.castNeverFails(antype, vntype)) {
                    this.typeSubtype.eqUnion(jedd.Jedd.v().literal(new Object[] { antype, vntype },
                                                                   new jedd.Domain[] { subt.v(), supt.v() },
                                                                   new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
                }
            }
        }
        this.seenVTypes.addAll(newSeenVTypes);
        this.varNodeType.eqUnion(this.newVnType);
        this.allocNodeType.eqUnion(this.newAnType);
        final jedd.Relation tmp =
          new jedd.Relation(new jedd.Domain[] { var.v(), subt.v() }, new jedd.PhysicalDomain[] { V1.v(), T1.v() });
        final jedd.Relation tmp2 =
          new jedd.Relation(new jedd.Domain[] { var.v(), obj.v() }, new jedd.PhysicalDomain[] { V1.v(), H1.v() });
        tmp.eq(jedd.Jedd.v().relprod(jedd.Jedd.v().read(this.typeSubtype),
                                     jedd.Jedd.v().replace(this.newVnType,
                                                           new jedd.PhysicalDomain[] { T1.v() },
                                                           new jedd.PhysicalDomain[] { T2.v() }),
                                     new jedd.PhysicalDomain[] { T2.v() }));
        tmp2.eq(jedd.Jedd.v().relprod(jedd.Jedd.v().read(this.allocNodeType),
                                      tmp,
                                      new jedd.PhysicalDomain[] { T1.v() }));
        this.typeMask.eqUnion(tmp2);
        tmp.eq(jedd.Jedd.v().relprod(jedd.Jedd.v().read(this.typeSubtype),
                                     jedd.Jedd.v().replace(this.varNodeType,
                                                           new jedd.PhysicalDomain[] { T1.v() },
                                                           new jedd.PhysicalDomain[] { T2.v() }),
                                     new jedd.PhysicalDomain[] { T2.v() }));
        tmp2.eq(jedd.Jedd.v().relprod(jedd.Jedd.v().read(this.newAnType), tmp, new jedd.PhysicalDomain[] { T1.v() }));
        this.typeMask.eqUnion(tmp2);
        this.newVnType.eq(jedd.Jedd.v().falseBDD());
        this.newAnType.eq(jedd.Jedd.v().falseBDD());
        this.lastAllocNode = anNumb.size();
        this.lastVarNode = vnNumb.size();
        tmp.eq(jedd.Jedd.v().falseBDD());
        tmp2.eq(jedd.Jedd.v().falseBDD());
        this.bddhier.update();
        final jedd.Relation bddts =
          new jedd.Relation(new jedd.Domain[] { subt.v(), supt.v() },
                            new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                            this.bddhier.subtypeRelation());
        bddts.eq(jedd.Jedd.v().intersect(jedd.Jedd.v().read(bddts),
                                         jedd.Jedd.v().project(jedd.Jedd.v().replace(this.varNodeType,
                                                                                     new jedd.PhysicalDomain[] { T1.v() },
                                                                                     new jedd.PhysicalDomain[] { T2.v() }),
                                                               new jedd.PhysicalDomain[] { V1.v() })));
        bddts.eq(jedd.Jedd.v().intersect(jedd.Jedd.v().read(bddts),
                                         jedd.Jedd.v().project(this.allocNodeType,
                                                               new jedd.PhysicalDomain[] { H1.v() })));
        if (jedd.Jedd.v().equals(jedd.Jedd.v().read(bddts), this.typeSubtype)) {
            System.out.println("bdd and non-bdd hierarchy match");
        } else {
            System.out.println("bdd and non-bdd hierarchy don\'t match");
            System.out.println("size of bdd: " +
                               new jedd.Relation(new jedd.Domain[] { supt.v(), subt.v() },
                                                 new jedd.PhysicalDomain[] { T2.v(), T1.v() },
                                                 bddts).size() +
                               " size of non-bdd: " +
                               new jedd.Relation(new jedd.Domain[] { supt.v(), subt.v() },
                                                 new jedd.PhysicalDomain[] { T2.v(), T1.v() },
                                                 this.typeSubtype).size());
            System.out.println("missing pairs: ");
            System.out.println(new jedd.Relation(new jedd.Domain[] { supt.v(), subt.v() },
                                                 new jedd.PhysicalDomain[] { T2.v(), T1.v() },
                                                 jedd.Jedd.v().minus(jedd.Jedd.v().read(bddts),
                                                                     this.typeSubtype)).toString());
        }
    }
    
    final jedd.Relation typeSubtype =
      new jedd.Relation(new jedd.Domain[] { subt.v(), supt.v() }, new jedd.PhysicalDomain[] { T1.v(), T2.v() });
    
    final jedd.Relation varNodeType =
      new jedd.Relation(new jedd.Domain[] { var.v(), type.v() }, new jedd.PhysicalDomain[] { V1.v(), T1.v() });
    
    final jedd.Relation newVnType =
      new jedd.Relation(new jedd.Domain[] { var.v(), type.v() }, new jedd.PhysicalDomain[] { V1.v(), T1.v() });
    
    final jedd.Relation allocNodeType =
      new jedd.Relation(new jedd.Domain[] { obj.v(), type.v() }, new jedd.PhysicalDomain[] { H1.v(), T1.v() });
    
    final jedd.Relation newAnType =
      new jedd.Relation(new jedd.Domain[] { obj.v(), type.v() }, new jedd.PhysicalDomain[] { H1.v(), T1.v() });
    
    final jedd.Relation typeMask =
      new jedd.Relation(new jedd.Domain[] { var.v(), obj.v() }, new jedd.PhysicalDomain[] { V1.v(), H1.v() });
    
    HashSet seenVTypes = new HashSet();
    
    HashSet seenATypes = new HashSet();
    
    private BDDHierarchy bddhier;
}
