package soot.jimple.spark.pag;

import java.util.*;
import soot.jimple.*;
import soot.jimple.spark.*;
import soot.*;
import soot.jimple.spark.sets.*;
import soot.jimple.spark.solver.OnFlyCallGraph;
import soot.jimple.spark.internal.*;
import soot.util.*;
import soot.util.queue.*;
import soot.options.BDDSparkOptions;
import soot.tagkit.*;
import soot.jimple.spark.bdddomains.*;

public class BDDPAG extends AbstractPAG {
    public BDDPAG(final BDDSparkOptions opts) {
        super(opts);
        this.typeManager = new BDDTypeManager(this);
        if (!opts.ignore_types()) { this.typeManager.setFastHierarchy(Scene.v().getOrMakeFastHierarchy()); }
    }
    
    public PointsToSet reachingObjects(Local l) {
        VarNode vn = this.findLocalVarNode(l);
        if (vn == null) return EmptyPointsToSet.v();
        return new BDDPointsToSet(new jedd.Relation(new jedd.Domain[] { obj.v() },
                                                    new jedd.PhysicalDomain[] { H1.v() },
                                                    jedd.Jedd.v().project(jedd.Jedd.v().intersect(jedd.Jedd.v().read(this.pointsTo),
                                                                                                  jedd.Jedd.v().literal(new Object[] { vn },
                                                                                                                        new jedd.Domain[] { var.v() },
                                                                                                                        new jedd.PhysicalDomain[] { V1.v() })),
                                                                          new jedd.PhysicalDomain[] { V1.v() })));
    }
    
    public PointsToSet reachingObjects(SootField f) { throw new RuntimeException("NYI"); }
    
    public PointsToSet reachingObjects(PointsToSet ptset, SootField f) { throw new RuntimeException("NYI"); }
    
    public PointsToSet reachingObjectsOfArrayElement(PointsToSet ptset) { throw new RuntimeException("NYI"); }
    
    public Iterator simpleSourcesIterator() {
        return new jedd.Relation(new jedd.Domain[] { src.v() },
                                 new jedd.PhysicalDomain[] { V1.v() },
                                 jedd.Jedd.v().project(this.edgeSet, new jedd.PhysicalDomain[] { V2.v() })).iterator();
    }
    
    public Iterator allocSourcesIterator() {
        return new jedd.Relation(new jedd.Domain[] { obj.v() },
                                 new jedd.PhysicalDomain[] { H1.v() },
                                 jedd.Jedd.v().project(this.alloc, new jedd.PhysicalDomain[] { V1.v() })).iterator();
    }
    
    public Iterator storeSourcesIterator() {
        return new jedd.Relation(new jedd.Domain[] { src.v() },
                                 new jedd.PhysicalDomain[] { V1.v() },
                                 jedd.Jedd.v().project(this.stores,
                                                       new jedd.PhysicalDomain[] { FD.v(), V2.v() })).iterator();
    }
    
    public Iterator loadSourcesIterator() { throw new RuntimeException("NYI"); }
    
    public Iterator simpleInvSourcesIterator() {
        return new jedd.Relation(new jedd.Domain[] { dst.v() },
                                 new jedd.PhysicalDomain[] { V2.v() },
                                 jedd.Jedd.v().project(this.edgeSet, new jedd.PhysicalDomain[] { V1.v() })).iterator();
    }
    
    public Iterator allocInvSourcesIterator() {
        return new jedd.Relation(new jedd.Domain[] { var.v() },
                                 new jedd.PhysicalDomain[] { V1.v() },
                                 jedd.Jedd.v().project(this.alloc, new jedd.PhysicalDomain[] { H1.v() })).iterator();
    }
    
    public Iterator storeInvSourcesIterator() { throw new RuntimeException("NYI"); }
    
    public Iterator loadInvSourcesIterator() {
        return new jedd.Relation(new jedd.Domain[] { dst.v() },
                                 new jedd.PhysicalDomain[] { V2.v() },
                                 jedd.Jedd.v().project(this.loads,
                                                       new jedd.PhysicalDomain[] { V1.v(), FD.v() })).iterator();
    }
    
    public boolean doAddSimpleEdge(VarNode from, VarNode to) {
        return !jedd.Jedd.v().equals(jedd.Jedd.v().read(this.edgeSet),
                                     this.edgeSet.eqUnion(jedd.Jedd.v().literal(new Object[] { from, to },
                                                                                new jedd.Domain[] { src.v(), dst.v() },
                                                                                new jedd.PhysicalDomain[] { V1.v(), V2.v() })));
    }
    
    public boolean doAddStoreEdge(VarNode from, FieldRefNode to) {
        return !jedd.Jedd.v().equals(jedd.Jedd.v().read(this.stores),
                                     this.stores.eqUnion(jedd.Jedd.v().literal(new Object[] { from, to.getBase(), to.getField() },
                                                                               new jedd.Domain[] { src.v(), dst.v(), fld.v() },
                                                                               new jedd.PhysicalDomain[] { V1.v(), V2.v(), FD.v() })));
    }
    
    public boolean doAddLoadEdge(FieldRefNode from, VarNode to) {
        return !jedd.Jedd.v().equals(jedd.Jedd.v().read(this.loads),
                                     this.loads.eqUnion(jedd.Jedd.v().literal(new Object[] { from.getBase(), from.getField(), to },
                                                                              new jedd.Domain[] { src.v(), fld.v(), dst.v() },
                                                                              new jedd.PhysicalDomain[] { V1.v(), FD.v(), V2.v() })));
    }
    
    public boolean doAddAllocEdge(AllocNode from, VarNode to) {
        return !jedd.Jedd.v().equals(jedd.Jedd.v().read(this.alloc),
                                     this.alloc.eqUnion(jedd.Jedd.v().literal(new Object[] { from, to },
                                                                              new jedd.Domain[] { obj.v(), var.v() },
                                                                              new jedd.PhysicalDomain[] { H1.v(), V1.v() })));
    }
    
    private BDDSparkOptions opts;
    
    public final jedd.Relation alloc =
      new jedd.Relation(new jedd.Domain[] { obj.v(), var.v() }, new jedd.PhysicalDomain[] { H1.v(), V1.v() });
    
    public final jedd.Relation pointsTo =
      new jedd.Relation(new jedd.Domain[] { var.v(), obj.v() }, new jedd.PhysicalDomain[] { V1.v(), H1.v() });
    
    public final jedd.Relation edgeSet =
      new jedd.Relation(new jedd.Domain[] { src.v(), dst.v() }, new jedd.PhysicalDomain[] { V1.v(), V2.v() });
    
    public final jedd.Relation loads =
      new jedd.Relation(new jedd.Domain[] { src.v(), fld.v(), dst.v() },
                        new jedd.PhysicalDomain[] { V1.v(), FD.v(), V2.v() });
    
    public final jedd.Relation stores =
      new jedd.Relation(new jedd.Domain[] { src.v(), dst.v(), fld.v() },
                        new jedd.PhysicalDomain[] { V1.v(), V2.v(), FD.v() });
    
    public final jedd.Relation fieldPt =
      new jedd.Relation(new jedd.Domain[] { base.v(), fld.v(), obj.v() },
                        new jedd.PhysicalDomain[] { H1.v(), FD.v(), H2.v() });
}
