package soot.jimple.spark.internal;

import soot.jimple.spark.*;
import soot.jimple.spark.pag.*;
import soot.*;
import soot.util.*;
import java.util.Iterator;
import soot.util.queue.*;
import soot.Type;
import soot.options.SparkOptions;
import soot.jimple.spark.bdddomains.*;

public final class BDDTypeManager extends AbstractTypeManager {
    public BDDTypeManager(BDDPAG bddpag) { super(bddpag); }
    
    public final void clearTypeMask() {
        this.lastAllocNode = 0;
        this.lastVarNode = 0;
        this.typeMask.eq(jedd.Jedd.v().falseBDD());
        this.varNodeType.eq(jedd.Jedd.v().falseBDD());
        this.allocNodeType.eq(jedd.Jedd.v().falseBDD());
        this.typeSubtype.eq(jedd.Jedd.v().falseBDD());
    }
    
    public final void makeTypeMask() {
        if (this.fh == null) { this.typeMask.eq(jedd.Jedd.v().trueBDD()); }
        this.update();
    }
    
    public final jedd.Relation get() {
        this.update();
        return this.typeMask;
    }
    
    int lastAllocNode = 0;
    
    int lastVarNode = 0;
    
    private void update() {
        if (this.fh == null) return;
        System.out.println("called update");
        ArrayNumberer anNumb = this.pag.getAllocNodeNumberer();
        ArrayNumberer vnNumb = this.pag.getVarNodeNumberer();
        for (int j = this.lastAllocNode + 1; j <= anNumb.size(); j++) {
            AllocNode an = (AllocNode) anNumb.get(j);
            this.newAnType.eqUnion(jedd.Jedd.v().literal(new Object[] { an, an.getType() },
                                                         new jedd.Domain[] { obj.v(), type.v() },
                                                         new jedd.PhysicalDomain[] { H1.v(), T1.v() }));
            for (int i = 1; i <= vnNumb.size(); i++) { this.updatePair((VarNode) vnNumb.get(i), an); }
        }
        for (int i = this.lastVarNode + 1; i <= vnNumb.size(); i++) {
            VarNode vn = (VarNode) vnNumb.get(i);
            this.newVnType.eqUnion(jedd.Jedd.v().literal(new Object[] { vn, vn.getType() },
                                                         new jedd.Domain[] { var.v(), type.v() },
                                                         new jedd.PhysicalDomain[] { V1.v(), T1.v() }));
            for (int j = 1; j <= this.lastAllocNode; j++) { this.updatePair(vn, (AllocNode) anNumb.get(j)); }
        }
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
    }
    
    private void updatePair(VarNode vn, AllocNode an) {
        Type vtype = vn.getType();
        Type atype = an.getType();
        final jedd.Relation pair =
          new jedd.Relation(new jedd.Domain[] { atp.v(), dtp.v() },
                            new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                            jedd.Jedd.v().literal(new Object[] { atype, vtype },
                                                  new jedd.Domain[] { atp.v(), dtp.v() },
                                                  new jedd.PhysicalDomain[] { T1.v(), T2.v() }));
        if (!jedd.Jedd.v().equals(jedd.Jedd.v().read(this.seenPairs), this.seenPairs.eqUnion(pair))) {
            if (this.castNeverFails(atype, vtype)) { this.typeSubtype.eqUnion(pair); }
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
    
    final jedd.Relation seenPairs =
      new jedd.Relation(new jedd.Domain[] { atp.v(), dtp.v() }, new jedd.PhysicalDomain[] { T1.v(), T2.v() });
}
