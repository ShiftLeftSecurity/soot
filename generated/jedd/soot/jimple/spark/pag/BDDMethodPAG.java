package soot.jimple.spark.pag;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.jimple.spark.*;
import soot.jimple.spark.builder.*;
import soot.jimple.spark.internal.*;
import soot.util.*;
import soot.util.queue.*;
import soot.toolkits.scalar.Pair;
import soot.jimple.toolkits.pointer.util.NativeMethodDriver;
import soot.relations.*;

public final class BDDMethodPAG extends AbstractMethodPAG {
    private BDDPAG pag;
    
    public AbstractPAG pag() { return this.pag; }
    
    public static BDDMethodPAG v(BDDPAG pag, SootMethod m) {
        BDDMethodPAG ret = (BDDMethodPAG) G.v().MethodPAG_methodToPag.get(m);
        if (ret == null) {
            ret = new BDDMethodPAG(pag, m);
            G.v().MethodPAG_methodToPag.put(m, ret);
        }
        return ret;
    }
    
    protected BDDMethodPAG(BDDPAG pag, SootMethod m) {
        super();
        this.pag = pag;
        this.method = m;
        this.nodeFactory = new MethodNodeFactory(pag, this);
        this.internalEdgeSet = pag.edgeSet.sameDomains();
        this.inEdgeSet = pag.edgeSet.sameDomains();
        this.outEdgeSet = pag.edgeSet.sameDomains();
        this.stores = pag.stores.sameDomains();
        this.loads = pag.loads.sameDomains();
        this.alloc = pag.alloc.sameDomains();
    }
    
    public void addToPAG(Object varNodeParameter) {
        if (varNodeParameter != null) throw new RuntimeException("NYI");
        if (this.hasBeenAdded) return;
        this.hasBeenAdded = true;
        this.pag.edgeSet.eqUnion(this.pag.edgeSet, this.internalEdgeSet);
        this.pag.edgeSet.eqUnion(this.pag.edgeSet, this.inEdgeSet);
        this.pag.edgeSet.eqUnion(this.pag.edgeSet, this.outEdgeSet);
        this.pag.stores.eqUnion(this.pag.stores, this.stores);
        this.pag.loads.eqUnion(this.pag.loads, this.loads);
        this.pag.alloc.eqUnion(this.pag.alloc, this.alloc);
    }
    
    private static Numberable[] box(Numberable n1, Numberable n2) {
        Numberable[] ret = { n1, n2 };
        return ret;
    }
    
    private static Numberable[] box(Numberable n1, Numberable n2, Numberable n3) {
        Numberable[] ret = { n1, n2, n3 };
        return ret;
    }
    
    public void addInternalEdge(Node src, Node dst) { this.addEdge(src, dst, this.internalEdgeSet); }
    
    public void addInEdge(Node src, Node dst) { this.addEdge(src, dst, this.inEdgeSet); }
    
    public void addOutEdge(Node src, Node dst) { this.addEdge(src, dst, this.outEdgeSet); }
    
    private void addEdge(Node src, Node dst, Relation edgeSet) {
        if (src instanceof VarNode) {
            if (dst instanceof VarNode) {
                edgeSet.add(this.pag.src, (VarNode) src, this.pag.dst, (VarNode) dst);
            } else {
                FieldRefNode fdst = (FieldRefNode) dst;
                this.stores.add(this.pag.src,
                                (VarNode) src,
                                this.pag.dst,
                                fdst.getBase(),
                                this.pag.fld,
                                fdst.getField());
            }
        } else
            if (src instanceof FieldRefNode) {
                FieldRefNode fsrc = (FieldRefNode) src;
                this.loads.add(this.pag.src,
                               fsrc.getBase(),
                               this.pag.fld,
                               fsrc.getField(),
                               this.pag.dst,
                               (VarNode) dst);
            } else {
                this.alloc.add(this.pag.obj, (AllocNode) src, this.pag.var, (VarNode) dst);
            }
    }
    
    public final Relation internalEdgeSet;
    
    public final Relation inEdgeSet;
    
    public final Relation outEdgeSet;
    
    public final Relation stores;
    
    public final Relation loads;
    
    public final Relation alloc;
}
