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
    
    public AbstractPAG pag() { return pag; }
    
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
        if (hasBeenAdded) return;
        hasBeenAdded = true;
        pag.edgeSet.eqUnion(internalEdgeSet);
        pag.edgeSet.eqUnion(inEdgeSet);
        pag.edgeSet.eqUnion(outEdgeSet);
        pag.stores.eqUnion(stores);
        pag.loads.eqUnion(loads);
        pag.alloc.eqUnion(alloc);
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
        internalEdgeSet.eq(this.addEdge(srcN,
                                        dstN,
                                        new jedd.internal.RelationContainer(new jedd.Attribute[] { src.v(), dst.v() },
                                                                            new jedd.PhysicalDomain[] { V1.v(), V2.v() },
                                                                            ("this.addEdge(srcN, dstN, internalEdgeSet) at /home/olhotak/s" +
                                                                             "oot-2-jedd/src/soot/jimple/spark/pag/BDDMethodPAG.jedd:79,26"),
                                                                            internalEdgeSet)));
    }
    
    public void addInEdge(Node srcN, Node dstN) {
        inEdgeSet.eq(this.addEdge(srcN,
                                  dstN,
                                  new jedd.internal.RelationContainer(new jedd.Attribute[] { src.v(), dst.v() },
                                                                      new jedd.PhysicalDomain[] { V1.v(), V2.v() },
                                                                      ("this.addEdge(srcN, dstN, inEdgeSet) at /home/olhotak/soot-2-" +
                                                                       "jedd/src/soot/jimple/spark/pag/BDDMethodPAG.jedd:83,20"),
                                                                      inEdgeSet)));
    }
    
    public void addOutEdge(Node srcN, Node dstN) {
        outEdgeSet.eq(this.addEdge(srcN,
                                   dstN,
                                   new jedd.internal.RelationContainer(new jedd.Attribute[] { src.v(), dst.v() },
                                                                       new jedd.PhysicalDomain[] { V1.v(), V2.v() },
                                                                       ("this.addEdge(srcN, dstN, outEdgeSet) at /home/olhotak/soot-2" +
                                                                        "-jedd/src/soot/jimple/spark/pag/BDDMethodPAG.jedd:87,21"),
                                                                       outEdgeSet)));
    }
    
    private jedd.internal.RelationContainer addEdge(Node srcN,
                                                    Node dstN,
                                                    final jedd.internal.RelationContainer edgeSet) {
        if (srcN == null)
            return new jedd.internal.RelationContainer(new jedd.Attribute[] { src.v(), dst.v() },
                                                       new jedd.PhysicalDomain[] { V1.v(), V2.v() },
                                                       ("return edgeSet; at /home/olhotak/soot-2-jedd/src/soot/jimple" +
                                                        "/spark/pag/BDDMethodPAG.jedd:91,27"),
                                                       edgeSet);
        if (srcN instanceof VarNode) {
            if (dstN instanceof VarNode) {
                edgeSet.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { srcN, dstN },
                                                               new jedd.Attribute[] { src.v(), dst.v() },
                                                               new jedd.PhysicalDomain[] { V1.v(), V2.v() }));
            } else {
                FieldRefNode fdst = (FieldRefNode) dstN;
                stores.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { srcN, fdst.getBase(), fdst.getField() },
                                                              new jedd.Attribute[] { src.v(), dst.v(), fld.v() },
                                                              new jedd.PhysicalDomain[] { V1.v(), V2.v(), FD.v() }));
            }
        } else
            if (srcN instanceof FieldRefNode) {
                FieldRefNode fsrc = (FieldRefNode) srcN;
                loads.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { fsrc.getBase(), fsrc.getField(), dstN },
                                                             new jedd.Attribute[] { src.v(), fld.v(), dst.v() },
                                                             new jedd.PhysicalDomain[] { V1.v(), FD.v(), V2.v() }));
            } else {
                alloc.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { srcN, dstN },
                                                             new jedd.Attribute[] { obj.v(), var.v() },
                                                             new jedd.PhysicalDomain[] { H1.v(), V1.v() }));
            }
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { src.v(), dst.v() },
                                                   new jedd.PhysicalDomain[] { V1.v(), V2.v() },
                                                   ("return edgeSet; at /home/olhotak/soot-2-jedd/src/soot/jimple" +
                                                    "/spark/pag/BDDMethodPAG.jedd:107,8"),
                                                   edgeSet);
    }
    
    public final jedd.internal.RelationContainer internalEdgeSet =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { src.v(), dst.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), V2.v() },
                                          ("public <soot.jimple.spark.bdddomains.src:soot.jimple.spark.b" +
                                           "dddomains.V1, soot.jimple.spark.bdddomains.dst:soot.jimple.s" +
                                           "park.bdddomains.V2> internalEdgeSet at /home/olhotak/soot-2-" +
                                           "jedd/src/soot/jimple/spark/pag/BDDMethodPAG.jedd:110,11"));
    
    public final jedd.internal.RelationContainer inEdgeSet =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { src.v(), dst.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), V2.v() },
                                          ("public <soot.jimple.spark.bdddomains.src:soot.jimple.spark.b" +
                                           "dddomains.V1, soot.jimple.spark.bdddomains.dst:soot.jimple.s" +
                                           "park.bdddomains.V2> inEdgeSet at /home/olhotak/soot-2-jedd/s" +
                                           "rc/soot/jimple/spark/pag/BDDMethodPAG.jedd:111,11"));
    
    public final jedd.internal.RelationContainer outEdgeSet =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { src.v(), dst.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), V2.v() },
                                          ("public <soot.jimple.spark.bdddomains.src:soot.jimple.spark.b" +
                                           "dddomains.V1, soot.jimple.spark.bdddomains.dst:soot.jimple.s" +
                                           "park.bdddomains.V2> outEdgeSet at /home/olhotak/soot-2-jedd/" +
                                           "src/soot/jimple/spark/pag/BDDMethodPAG.jedd:112,11"));
    
    public final jedd.internal.RelationContainer stores =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { src.v(), dst.v(), fld.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), V2.v(), FD.v() },
                                          ("public <soot.jimple.spark.bdddomains.src:soot.jimple.spark.b" +
                                           "dddomains.V1, soot.jimple.spark.bdddomains.dst:soot.jimple.s" +
                                           "park.bdddomains.V2, soot.jimple.spark.bdddomains.fld:soot.ji" +
                                           "mple.spark.bdddomains.FD> stores at /home/olhotak/soot-2-jed" +
                                           "d/src/soot/jimple/spark/pag/BDDMethodPAG.jedd:113,11"));
    
    public final jedd.internal.RelationContainer loads =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { src.v(), fld.v(), dst.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), FD.v(), V2.v() },
                                          ("public <soot.jimple.spark.bdddomains.src:soot.jimple.spark.b" +
                                           "dddomains.V1, soot.jimple.spark.bdddomains.fld:soot.jimple.s" +
                                           "park.bdddomains.FD, soot.jimple.spark.bdddomains.dst:soot.ji" +
                                           "mple.spark.bdddomains.V2> loads at /home/olhotak/soot-2-jedd" +
                                           "/src/soot/jimple/spark/pag/BDDMethodPAG.jedd:114,11"));
    
    public final jedd.internal.RelationContainer alloc =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), var.v() },
                                          new jedd.PhysicalDomain[] { H1.v(), V1.v() },
                                          ("public <soot.jimple.spark.bdddomains.obj:soot.jimple.spark.b" +
                                           "dddomains.H1, soot.jimple.spark.bdddomains.var:soot.jimple.s" +
                                           "park.bdddomains.V1> alloc at /home/olhotak/soot-2-jedd/src/s" +
                                           "oot/jimple/spark/pag/BDDMethodPAG.jedd:115,11"));
}
