package soot.jimple.spark.internal;

import soot.jimple.spark.*;
import soot.jimple.spark.pag.*;
import soot.*;
import soot.util.*;
import java.util.Iterator;
import soot.util.queue.*;
import soot.Type;
import soot.options.SparkOptions;
import soot.relations.*;
import soot.jbuddy.JBuddy;

public final class BDDTypeManager extends AbstractTypeManager {
    public BDDTypeManager(BDDPAG bddpag) { super(bddpag); }
    
    public final void clearTypeMask() {
        this.lastAllocNode = 0;
        this.lastVarNode = 0;
        this.typeMask.makeEmpty();
        this.varNodeType.makeEmpty();
        this.allocNodeType.makeEmpty();
        this.typeSubtype.makeEmpty();
    }
    
    public final void makeTypeMask() {
        if (this.fh == null) { this.typeMask.makeFull(); }
        this.update();
    }
    
    public final Relation get() {
        this.update();
        return this.typeMask;
    }
    
    int lastAllocNode = 0;
    
    int lastVarNode = 0;
    
    private void update() {
        if (this.fh == null) return;
        Numberer anNumb = this.pag.getAllocNodeNumberer();
        Numberer vnNumb = this.pag.getVarNodeNumberer();
        for (int i = 1; i <= vnNumb.size(); i++) {
            for (int j = this.lastAllocNode + 1; j <= anNumb.size(); j++) {
                this.updatePair((VarNode) vnNumb.get(i), (AllocNode) anNumb.get(j));
            }
        }
        for (int i = this.lastVarNode + 1; i <= vnNumb.size(); i++) {
            for (int j = 1; j <= this.lastAllocNode; j++) {
                this.updatePair((VarNode) vnNumb.get(i), (AllocNode) anNumb.get(j));
            }
        }
        this.varNodeType.eqUnion(this.varNodeType, this.newVnType);
        this.allocNodeType.eqUnion(this.allocNodeType, this.newAnType);
        final Relation tmp = new Relation(this.var, this.subt, this.v1, this.t1);
        final Relation tmp2 = new Relation(this.var, this.obj, this.v1, this.h1);
        tmp.eqRelprod(this.typeSubtype,
                      this.supt,
                      this.newVnType,
                      this.type,
                      this.var,
                      this.newVnType,
                      this.var,
                      this.subt,
                      this.typeSubtype,
                      this.subt);
        tmp2.eqRelprod(this.allocNodeType,
                       this.type,
                       tmp,
                       this.subt,
                       this.var,
                       tmp,
                       this.var,
                       this.obj,
                       this.allocNodeType,
                       this.obj);
        this.typeMask.eqUnion(this.typeMask, tmp2);
        tmp.eqRelprod(this.typeSubtype,
                      this.supt,
                      this.varNodeType,
                      this.type,
                      this.var,
                      this.varNodeType,
                      this.var,
                      this.subt,
                      this.typeSubtype,
                      this.subt);
        tmp2.eqRelprod(this.newAnType,
                       this.type,
                       tmp,
                       this.subt,
                       this.var,
                       tmp,
                       this.var,
                       this.obj,
                       this.newAnType,
                       this.obj);
        this.typeMask.eqUnion(this.typeMask, tmp2);
        this.newVnType.makeEmpty();
        this.newAnType.makeEmpty();
        this.lastAllocNode = anNumb.size();
        this.lastVarNode = vnNumb.size();
        tmp.makeEmpty();
        tmp2.makeEmpty();
    }
    
    private void updatePair(VarNode vn, AllocNode an) {
        Type vtype = vn.getType();
        Type atype = an.getType();
        if (this.varNodeType.restrict(this.type, vtype).isEmpty() ||
              this.allocNodeType.restrict(this.type, atype).isEmpty()) {
            if (this.castNeverFails(atype, vtype)) { this.typeSubtype.add(this.subt, atype, this.supt, vtype); }
        }
        this.newVnType.add(this.var, vn, this.type, vtype);
        this.newAnType.add(this.obj, an, this.type, atype);
    }
    
    private final PhysicalDomain t1 = ((BDDPAG) this.pag).t1;
    
    private final PhysicalDomain t2 = ((BDDPAG) this.pag).t2;
    
    private final PhysicalDomain v1 = ((BDDPAG) this.pag).v1;
    
    private final PhysicalDomain h1 = ((BDDPAG) this.pag).h1;
    
    private final Domain subt = new Domain(Scene.v().getTypeNumberer(), "subt");
    
    private final Domain supt = new Domain(Scene.v().getTypeNumberer(), "supt");
    
    private final Domain type = new Domain(Scene.v().getTypeNumberer(), "type");
    
    private final Domain var = ((BDDPAG) this.pag).var;
    
    private final Domain obj = ((BDDPAG) this.pag).obj;
    
    private final Relation typeSubtype = new Relation(this.subt, this.supt, this.t1, this.t2);
    
    private final Relation varNodeType = new Relation(this.var, this.type, this.v1, this.t1);
    
    private final Relation newVnType = this.varNodeType.sameDomains();
    
    private final Relation allocNodeType = new Relation(this.obj, this.type, this.h1, this.t1);
    
    private final Relation newAnType = this.allocNodeType.sameDomains();
    
    private final Relation typeMask = new Relation(this.var, this.obj, this.v1, this.h1);
}
