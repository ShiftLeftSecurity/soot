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
import soot.relations.*;
import soot.jbuddy.JBuddy;

public class BDDPAG extends AbstractPAG {
    public BDDPAG(final BDDSparkOptions opts) {
        super(opts);
        this.typeManager = new BDDTypeManager(this);
        if (!opts.ignore_types()) { this.typeManager.setFastHierarchy(Scene.v().getOrMakeFastHierarchy()); }
        PhysicalDomain[] interleaved = { this.v1, this.v2, this.fd, this.h1, this.h2, this.t1, this.t2 };
        PhysicalDomain[] v1v2 = { this.v1, this.v2 };
        Object[] order = { this.fd, v1v2, this.h1, this.h2, this.t1, this.t2 };
    }
    
    public PointsToSet reachingObjects(Local l) {
        VarNode vn = this.findLocalVarNode(l);
        if (vn == null) return EmptyPointsToSet.v();
        return new BDDPointsToSet(this.pointsTo.restrict(this.var, vn).projectDownTo(this.obj));
    }
    
    public PointsToSet reachingObjects(SootField f) { throw new RuntimeException("NYI"); }
    
    public PointsToSet reachingObjects(PointsToSet ptset, SootField f) { throw new RuntimeException("NYI"); }
    
    public PointsToSet reachingObjectsOfArrayElement(PointsToSet ptset) { throw new RuntimeException("NYI"); }
    
    public Iterator simpleSourcesIterator() { return this.edgeSet.projectDownTo(this.src).iterator(); }
    
    public Iterator allocSourcesIterator() { return this.alloc.projectDownTo(this.obj).iterator(); }
    
    public Iterator storeSourcesIterator() { return this.stores.projectDownTo(this.src).iterator(); }
    
    public Iterator loadSourcesIterator() { throw new RuntimeException("NYI"); }
    
    public Iterator simpleInvSourcesIterator() { return this.edgeSet.projectDownTo(this.dst).iterator(); }
    
    public Iterator allocInvSourcesIterator() { return this.alloc.projectDownTo(this.var).iterator(); }
    
    public Iterator storeInvSourcesIterator() { throw new RuntimeException("NYI"); }
    
    public Iterator loadInvSourcesIterator() { return this.loads.projectDownTo(this.dst).iterator(); }
    
    public boolean doAddSimpleEdge(VarNode from, VarNode to) { return this.edgeSet.add(this.src, from, this.dst, to); }
    
    public boolean doAddStoreEdge(VarNode from, FieldRefNode to) {
        return this.stores.add(this.src, from, this.dst, to.getBase(), this.fld, to.getField());
    }
    
    public boolean doAddLoadEdge(FieldRefNode from, VarNode to) {
        return this.loads.add(this.src, from.getBase(), this.fld, from.getField(), this.dst, to);
    }
    
    public boolean doAddAllocEdge(AllocNode from, VarNode to) { return this.alloc.add(this.obj, from, this.var, to); }
    
    private BDDSparkOptions opts;
    
    public PhysicalDomain v1 = new PhysicalDomain(20, "V1");
    
    public PhysicalDomain v2 = new PhysicalDomain(20, "V2");
    
    public PhysicalDomain fd = new PhysicalDomain(20, "FD");
    
    public PhysicalDomain h1 = new PhysicalDomain(20, "H1");
    
    public PhysicalDomain h2 = new PhysicalDomain(20, "H2");
    
    public PhysicalDomain t1 = new PhysicalDomain(20, "T1");
    
    public PhysicalDomain t2 = new PhysicalDomain(20, "T2");
    
    public Domain var = new Domain(this.getVarNodeNumberer(), "var");
    
    public Domain src = new Domain(this.getVarNodeNumberer(), "src");
    
    public Domain dst = new Domain(this.getVarNodeNumberer(), "dst");
    
    public Domain base = new Domain(this.getAllocNodeNumberer(), "base");
    
    public Domain obj = new Domain(this.getAllocNodeNumberer(), "obj");
    
    public Domain fld = new Domain(Scene.v().getFieldNumberer(), "fld");
    
    public final Relation alloc = new Relation(this.obj, this.var, this.h1, this.v1);
    
    public final Relation pointsTo = new Relation(this.var, this.obj, this.v1, this.h1);
    
    public final Relation edgeSet = new Relation(this.src, this.dst, this.v1, this.v2);
    
    public final Relation loads = new Relation(this.src, this.fld, this.dst, this.v1, this.fd, this.v2);
    
    public final Relation stores = new Relation(this.src, this.dst, this.fld, this.v1, this.v2, this.fd);
    
    public final Relation fieldPt = new Relation(this.base, this.fld, this.obj, this.h1, this.fd, this.h2);
    
    private void reportOrdering() {
        this.reportVarOrderingOfDomain("FD", this.fd);
        this.reportVarOrderingOfDomain("V1", this.v1);
        this.reportVarOrderingOfDomain("V2", this.v2);
        this.reportVarOrderingOfDomain("H1", this.h1);
        this.reportVarOrderingOfDomain("H2", this.h2);
    }
    
    private void reportVarOrderingOfDomain(String dname, PhysicalDomain var) {
        int vnum = JBuddy.fdd_varnum(var.var());
        int[] vars = new int[vnum];
        JBuddy.fdd_getvars(vars, var.var());
        for (int i = 0; i < vnum; i++) { G.v().out.print("" + JBuddy.bdd_var2level(vars[i]) + " "); }
        G.v().out.println("");
    }
}
