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
import soot.jimple.spark.bdddomains.*;

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
    }
    
    public void addToPAG(Object varNodeParameter) {
        if (varNodeParameter != null) throw new RuntimeException("NYI");
        if (this.hasBeenAdded) return;
        this.hasBeenAdded = true;
        this.pag.edgeSet.eqUnion(this.internalEdgeSet);
        this.pag.edgeSet.eqUnion(this.inEdgeSet);
        this.pag.edgeSet.eqUnion(this.outEdgeSet);
        this.pag.stores.eqUnion(this.stores);
        this.pag.loads.eqUnion(this.loads);
        this.pag.alloc.eqUnion(this.alloc);
    }
    
    private static Numberable[] box(Numberable n1, Numberable n2) {
        Numberable[] ret = { n1, n2 };
        return ret;
    }
    
    private static Numberable[] box(Numberable n1, Numberable n2, Numberable n3) {
        Numberable[] ret = { n1, n2, n3 };
        return ret;
    }
    
    public void addInternalEdge(Node srcN, Node dstN) {
        this.internalEdgeSet.eq(this.addEdge(srcN,
                                             dstN,
                                             new jedd.Relation(new jedd.Domain[] { src.v(), dst.v() },
                                                               new jedd.PhysicalDomain[] { V1.v(), V2.v() },
                                                               this.internalEdgeSet)));
    }
    
    public void addInEdge(Node srcN, Node dstN) {
        this.inEdgeSet.eq(this.addEdge(srcN,
                                       dstN,
                                       new jedd.Relation(new jedd.Domain[] { src.v(), dst.v() },
                                                         new jedd.PhysicalDomain[] { V1.v(), V2.v() },
                                                         this.inEdgeSet)));
    }
    
    public void addOutEdge(Node srcN, Node dstN) {
        this.outEdgeSet.eq(this.addEdge(srcN,
                                        dstN,
                                        new jedd.Relation(new jedd.Domain[] { src.v(), dst.v() },
                                                          new jedd.PhysicalDomain[] { V1.v(), V2.v() },
                                                          this.outEdgeSet)));
    }
    
    private jedd.Relation addEdge(Node srcN, Node dstN, final jedd.Relation edgeSet) {
        if (srcN == null)
            return new jedd.Relation(new jedd.Domain[] { src.v(), dst.v() },
                                     new jedd.PhysicalDomain[] { V1.v(), V2.v() },
                                     edgeSet);
        if (srcN instanceof VarNode) {
            if (dstN instanceof VarNode) {
                edgeSet.eqUnion(jedd.Jedd.v().literal(new Object[] { srcN, dstN },
                                                      new jedd.Domain[] { src.v(), dst.v() },
                                                      new jedd.PhysicalDomain[] { V1.v(), V2.v() }));
            } else {
                FieldRefNode fdst = (FieldRefNode) dstN;
                this.stores.eqUnion(jedd.Jedd.v().literal(new Object[] { srcN, fdst.getBase(), fdst.getField() },
                                                          new jedd.Domain[] { src.v(), dst.v(), fld.v() },
                                                          new jedd.PhysicalDomain[] { V1.v(), V2.v(), FD.v() }));
            }
        } else
            if (srcN instanceof FieldRefNode) {
                FieldRefNode fsrc = (FieldRefNode) srcN;
                this.loads.eqUnion(jedd.Jedd.v().literal(new Object[] { fsrc.getBase(), fsrc.getField(), dstN },
                                                         new jedd.Domain[] { src.v(), fld.v(), dst.v() },
                                                         new jedd.PhysicalDomain[] { V1.v(), FD.v(), V2.v() }));
            } else {
                this.alloc.eqUnion(jedd.Jedd.v().literal(new Object[] { srcN, dstN },
                                                         new jedd.Domain[] { obj.v(), var.v() },
                                                         new jedd.PhysicalDomain[] { H1.v(), V1.v() }));
            }
        return new jedd.Relation(new jedd.Domain[] { src.v(), dst.v() },
                                 new jedd.PhysicalDomain[] { V1.v(), V2.v() },
                                 edgeSet);
    }
    
    public final jedd.Relation internalEdgeSet =
      new jedd.Relation(new jedd.Domain[] { src.v(), dst.v() }, new jedd.PhysicalDomain[] { V1.v(), V2.v() });
    
    public final jedd.Relation inEdgeSet =
      new jedd.Relation(new jedd.Domain[] { src.v(), dst.v() }, new jedd.PhysicalDomain[] { V1.v(), V2.v() });
    
    public final jedd.Relation outEdgeSet =
      new jedd.Relation(new jedd.Domain[] { src.v(), dst.v() }, new jedd.PhysicalDomain[] { V1.v(), V2.v() });
    
    public final jedd.Relation stores =
      new jedd.Relation(new jedd.Domain[] { src.v(), dst.v(), fld.v() },
                        new jedd.PhysicalDomain[] { V1.v(), V2.v(), FD.v() });
    
    public final jedd.Relation loads =
      new jedd.Relation(new jedd.Domain[] { src.v(), fld.v(), dst.v() },
                        new jedd.PhysicalDomain[] { V1.v(), FD.v(), V2.v() });
    
    public final jedd.Relation alloc =
      new jedd.Relation(new jedd.Domain[] { obj.v(), var.v() }, new jedd.PhysicalDomain[] { H1.v(), V1.v() });
}
