package soot.jimple.spark.pag;

import java.util.*;
import soot.jimple.*;
import soot.jimple.spark.*;
import soot.*;
import soot.jimple.spark.sets.*;
import soot.jimple.spark.solver.*;
import soot.jimple.spark.internal.*;
import soot.util.*;
import soot.util.queue.*;
import soot.options.BDDSparkOptions;
import soot.tagkit.*;
import soot.jimple.spark.bdddomains.*;

public class BDDPAG extends AbstractPAG {
    public BDDPAG(final BDDSparkOptions opts) {
        super(opts);
        typeManager = new BDDTypeManager(this);
        if (!opts.ignore_types()) { typeManager.setFastHierarchy(Scene.v().getOrMakeFastHierarchy()); }
    }
    
    public PointsToSet reachingObjects(Local l) {
        VarNode vn = this.findLocalVarNode(l);
        if (vn == null) return EmptyPointsToSet.v();
        return new BDDPointsToSet(new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v() },
                                                                      new jedd.PhysicalDomain[] { H1.v() },
                                                                      ("new soot.jimple.spark.sets.BDDPointsToSet(...) at /home/olho" +
                                                                       "tak/soot-2-jedd/src/soot/jimple/spark/pag/BDDPAG.jedd:51,15"),
                                                                      jedd.internal.Jedd.v().project(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(pointsTo),
                                                                                                                                 jedd.internal.Jedd.v().literal(new Object[] { vn },
                                                                                                                                                                new jedd.Attribute[] { var.v() },
                                                                                                                                                                new jedd.PhysicalDomain[] { V1.v() }),
                                                                                                                                 new jedd.PhysicalDomain[] { V1.v() }),
                                                                                                     new jedd.PhysicalDomain[] { V1.v() })));
    }
    
    public PointsToSet reachingObjects(SootField f) { throw new RuntimeException("NYI"); }
    
    public PointsToSet reachingObjects(PointsToSet ptset, SootField f) { throw new RuntimeException("NYI"); }
    
    public PointsToSet reachingObjectsOfArrayElement(PointsToSet ptset) { throw new RuntimeException("NYI"); }
    
    public Iterator simpleSourcesIterator() {
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { src.v() },
                                                   new jedd.PhysicalDomain[] { V1.v() },
                                                   ("jedd.internal.Jedd.v().project(edgeSet, new jedd.PhysicalDom" +
                                                    "ain[...]).iterator() at /home/olhotak/soot-2-jedd/src/soot/j" +
                                                    "imple/spark/pag/BDDPAG.jedd:65,34"),
                                                   jedd.internal.Jedd.v().project(edgeSet,
                                                                                  new jedd.PhysicalDomain[] { V2.v() })).iterator();
    }
    
    public Iterator allocSourcesIterator() {
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v() },
                                                   new jedd.PhysicalDomain[] { H1.v() },
                                                   ("jedd.internal.Jedd.v().project(alloc, new jedd.PhysicalDomai" +
                                                    "n[...]).iterator() at /home/olhotak/soot-2-jedd/src/soot/jim" +
                                                    "ple/spark/pag/BDDPAG.jedd:68,32"),
                                                   jedd.internal.Jedd.v().project(alloc,
                                                                                  new jedd.PhysicalDomain[] { V1.v() })).iterator();
    }
    
    public Iterator storeSourcesIterator() {
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { src.v() },
                                                   new jedd.PhysicalDomain[] { V1.v() },
                                                   ("jedd.internal.Jedd.v().project(stores, new jedd.PhysicalDoma" +
                                                    "in[...]).iterator() at /home/olhotak/soot-2-jedd/src/soot/ji" +
                                                    "mple/spark/pag/BDDPAG.jedd:71,39"),
                                                   jedd.internal.Jedd.v().project(stores,
                                                                                  new jedd.PhysicalDomain[] { FD.v(), V2.v() })).iterator();
    }
    
    public Iterator loadSourcesIterator() { throw new RuntimeException("NYI"); }
    
    public Iterator simpleInvSourcesIterator() {
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { dst.v() },
                                                   new jedd.PhysicalDomain[] { V2.v() },
                                                   ("jedd.internal.Jedd.v().project(edgeSet, new jedd.PhysicalDom" +
                                                    "ain[...]).iterator() at /home/olhotak/soot-2-jedd/src/soot/j" +
                                                    "imple/spark/pag/BDDPAG.jedd:77,34"),
                                                   jedd.internal.Jedd.v().project(edgeSet,
                                                                                  new jedd.PhysicalDomain[] { V1.v() })).iterator();
    }
    
    public Iterator allocInvSourcesIterator() {
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v() },
                                                   new jedd.PhysicalDomain[] { V1.v() },
                                                   ("jedd.internal.Jedd.v().project(alloc, new jedd.PhysicalDomai" +
                                                    "n[...]).iterator() at /home/olhotak/soot-2-jedd/src/soot/jim" +
                                                    "ple/spark/pag/BDDPAG.jedd:80,32"),
                                                   jedd.internal.Jedd.v().project(alloc,
                                                                                  new jedd.PhysicalDomain[] { H1.v() })).iterator();
    }
    
    public Iterator storeInvSourcesIterator() { throw new RuntimeException("NYI"); }
    
    public Iterator loadInvSourcesIterator() {
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { dst.v() },
                                                   new jedd.PhysicalDomain[] { V2.v() },
                                                   ("jedd.internal.Jedd.v().project(loads, new jedd.PhysicalDomai" +
                                                    "n[...]).iterator() at /home/olhotak/soot-2-jedd/src/soot/jim" +
                                                    "ple/spark/pag/BDDPAG.jedd:86,39"),
                                                   jedd.internal.Jedd.v().project(loads,
                                                                                  new jedd.PhysicalDomain[] { V1.v(), FD.v() })).iterator();
    }
    
    public boolean doAddSimpleEdge(VarNode from, VarNode to) {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(edgeSet),
                                              edgeSet.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { from, to },
                                                                                             new jedd.Attribute[] { src.v(), dst.v() },
                                                                                             new jedd.PhysicalDomain[] { V1.v(), V2.v() })));
    }
    
    public boolean doAddStoreEdge(VarNode from, FieldRefNode to) {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(stores),
                                              stores.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { from, to.getBase(), to.getField() },
                                                                                            new jedd.Attribute[] { src.v(), dst.v(), fld.v() },
                                                                                            new jedd.PhysicalDomain[] { V1.v(), V2.v(), FD.v() })));
    }
    
    public boolean doAddLoadEdge(FieldRefNode from, VarNode to) {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(loads),
                                              loads.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { from.getBase(), from.getField(), to },
                                                                                           new jedd.Attribute[] { src.v(), fld.v(), dst.v() },
                                                                                           new jedd.PhysicalDomain[] { V1.v(), FD.v(), V2.v() })));
    }
    
    public boolean doAddAllocEdge(AllocNode from, VarNode to) {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(alloc),
                                              alloc.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { from, to },
                                                                                           new jedd.Attribute[] { obj.v(), var.v() },
                                                                                           new jedd.PhysicalDomain[] { H1.v(), V1.v() })));
    }
    
    private BDDSparkOptions opts;
    
    public final jedd.internal.RelationContainer alloc =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), var.v() },
                                          new jedd.PhysicalDomain[] { H1.v(), V1.v() },
                                          ("public <soot.jimple.spark.bdddomains.obj:soot.jimple.spark.b" +
                                           "dddomains.H1, soot.jimple.spark.bdddomains.var:soot.jimple.s" +
                                           "park.bdddomains.V1> alloc at /home/olhotak/soot-2-jedd/src/s" +
                                           "oot/jimple/spark/pag/BDDPAG.jedd:110,11"));
    
    public final jedd.internal.RelationContainer pointsTo =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                          ("public <soot.jimple.spark.bdddomains.var:soot.jimple.spark.b" +
                                           "dddomains.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.s" +
                                           "park.bdddomains.H1> pointsTo at /home/olhotak/soot-2-jedd/sr" +
                                           "c/soot/jimple/spark/pag/BDDPAG.jedd:113,11"));
    
    public final jedd.internal.RelationContainer edgeSet =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { src.v(), dst.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), V2.v() },
                                          ("public <soot.jimple.spark.bdddomains.src:soot.jimple.spark.b" +
                                           "dddomains.V1, soot.jimple.spark.bdddomains.dst:soot.jimple.s" +
                                           "park.bdddomains.V2> edgeSet at /home/olhotak/soot-2-jedd/src" +
                                           "/soot/jimple/spark/pag/BDDPAG.jedd:116,11"));
    
    public final jedd.internal.RelationContainer loads =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { src.v(), fld.v(), dst.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), FD.v(), V2.v() },
                                          ("public <soot.jimple.spark.bdddomains.src:soot.jimple.spark.b" +
                                           "dddomains.V1, soot.jimple.spark.bdddomains.fld:soot.jimple.s" +
                                           "park.bdddomains.FD, soot.jimple.spark.bdddomains.dst:soot.ji" +
                                           "mple.spark.bdddomains.V2> loads at /home/olhotak/soot-2-jedd" +
                                           "/src/soot/jimple/spark/pag/BDDPAG.jedd:119,11"));
    
    public final jedd.internal.RelationContainer stores =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { src.v(), dst.v(), fld.v() },
                                          new jedd.PhysicalDomain[] { V1.v(), V2.v(), FD.v() },
                                          ("public <soot.jimple.spark.bdddomains.src:soot.jimple.spark.b" +
                                           "dddomains.V1, soot.jimple.spark.bdddomains.dst:soot.jimple.s" +
                                           "park.bdddomains.V2, soot.jimple.spark.bdddomains.fld:soot.ji" +
                                           "mple.spark.bdddomains.FD> stores at /home/olhotak/soot-2-jed" +
                                           "d/src/soot/jimple/spark/pag/BDDPAG.jedd:122,11"));
    
    public final jedd.internal.RelationContainer fieldPt =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { base.v(), fld.v(), obj.v() },
                                          new jedd.PhysicalDomain[] { H1.v(), FD.v(), H2.v() },
                                          ("public <soot.jimple.spark.bdddomains.base:soot.jimple.spark." +
                                           "bdddomains.H1, soot.jimple.spark.bdddomains.fld:soot.jimple." +
                                           "spark.bdddomains.FD, soot.jimple.spark.bdddomains.obj:soot.j" +
                                           "imple.spark.bdddomains.H2> fieldPt at /home/olhotak/soot-2-j" +
                                           "edd/src/soot/jimple/spark/pag/BDDPAG.jedd:125,11"));
    
    private BDDOnFlyCallGraph ofcg;
    
    public void setOnFlyCallGraph(BDDOnFlyCallGraph ofcg) { this.ofcg = ofcg; }
    
    public BDDOnFlyCallGraph getOnFlyCallGraph() { return ofcg; }
    
    public BDDOnFlyCallGraph ofcg() { return ofcg; }
}
