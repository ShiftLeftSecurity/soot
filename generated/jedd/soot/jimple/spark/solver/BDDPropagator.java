package soot.jimple.spark.solver;

import soot.jimple.spark.*;
import soot.jimple.spark.pag.*;
import soot.jimple.spark.sets.*;
import soot.jimple.spark.internal.*;
import soot.*;
import soot.util.queue.*;
import java.util.*;
import soot.options.SparkOptions;
import soot.relations.*;

public final class BDDPropagator extends Propagator {
    public BDDPropagator(BDDPAG pag) {
        super();
        this.pag = pag;
    }
    
    public final void propagate() {
        final Domain var = this.pag.var;
        final Domain src = this.pag.src;
        final Domain dst = this.pag.dst;
        final Domain base = this.pag.base;
        final Domain obj = this.pag.obj;
        final Domain fld = this.pag.fld;
        final PhysicalDomain v1 = this.pag.v1;
        final PhysicalDomain v2 = this.pag.v2;
        final PhysicalDomain h1 = this.pag.h1;
        final PhysicalDomain h2 = this.pag.h2;
        final PhysicalDomain fd = this.pag.fd;
        final Relation edgeSet = this.pag.edgeSet;
        final Relation pointsTo = this.pag.pointsTo;
        final Relation alloc = this.pag.alloc;
        final Relation loads = this.pag.loads;
        final Relation stores = this.pag.stores;
        final Relation fieldPt = this.pag.fieldPt;
        final Relation oldPointsTo = pointsTo.sameDomains();
        final Relation newPointsTo = pointsTo.sameDomains();
        final Relation tmpPointsTo = newPointsTo.sameDomains();
        final Relation objectsBeingStored = new Relation(obj, var, fld, h2, v1, fd);
        final Relation oldStorePt = objectsBeingStored.sameDomains();
        final Relation newStorePt = objectsBeingStored.sameDomains();
        final Relation newFieldPt = fieldPt.sameDomains();
        final Relation tmpFieldPt = fieldPt.sameDomains();
        final Relation loadsFromHeap = new Relation(base, fld, dst, h1, fd, v2);
        final Relation loadAss = loadsFromHeap.sameDomains();
        final BDDTypeManager typeManager = (BDDTypeManager) this.pag.getTypeManager();
        pointsTo.eqUnion(pointsTo, alloc);
        newPointsTo.eqUnion(newPointsTo, pointsTo);
        do  {
            do  {
                newPointsTo.eqRelprod(edgeSet, src, newPointsTo, var, var, edgeSet, dst, obj, newPointsTo, obj);
                newPointsTo.eqMinus(newPointsTo, pointsTo);
                newPointsTo.eqIntersect(newPointsTo, typeManager.get());
                pointsTo.eqUnion(pointsTo, newPointsTo);
                if (this.pag.getOpts().verbose()) {
                    G.v().out.println("Minor iteration: " + newPointsTo.projectDownTo(var).size() + " changed p2sets");
                }
            }while(!newPointsTo.isEmpty()); 
            newPointsTo.eqMinus(pointsTo, oldPointsTo);
            objectsBeingStored.eqRelprod(stores,
                                         src,
                                         newPointsTo,
                                         var,
                                         obj,
                                         newPointsTo,
                                         obj,
                                         var,
                                         stores,
                                         dst,
                                         fld,
                                         stores,
                                         fld);
            newStorePt.eqMinus(objectsBeingStored, oldStorePt);
            oldStorePt.eqUnion(oldStorePt, newStorePt);
            newFieldPt.eqRelprod(oldStorePt,
                                 var,
                                 newPointsTo,
                                 var,
                                 base,
                                 newPointsTo,
                                 obj,
                                 fld,
                                 oldStorePt,
                                 fld,
                                 obj,
                                 oldStorePt,
                                 obj);
            tmpFieldPt.eqRelprod(newStorePt,
                                 var,
                                 oldPointsTo,
                                 var,
                                 base,
                                 oldPointsTo,
                                 obj,
                                 fld,
                                 newStorePt,
                                 fld,
                                 obj,
                                 newStorePt,
                                 obj);
            newFieldPt.eqUnion(newFieldPt, tmpFieldPt);
            newFieldPt.eqMinus(newFieldPt, fieldPt);
            fieldPt.eqUnion(fieldPt, newFieldPt);
            loadsFromHeap.eqRelprod(loads,
                                    src,
                                    newPointsTo,
                                    var,
                                    base,
                                    newPointsTo,
                                    obj,
                                    fld,
                                    loads,
                                    fld,
                                    dst,
                                    loads,
                                    dst);
            loadsFromHeap.eqMinus(loadsFromHeap, loadAss);
            newPointsTo.eqRelprod(loadAss, base, fld, newFieldPt, base, fld, var, loadAss, dst, obj, newFieldPt, obj);
            tmpPointsTo.eqRelprod(loadsFromHeap,
                                  base,
                                  fld,
                                  fieldPt,
                                  base,
                                  fld,
                                  var,
                                  loadsFromHeap,
                                  dst,
                                  obj,
                                  fieldPt,
                                  obj);
            newPointsTo.eqUnion(newPointsTo, tmpPointsTo);
            loadAss.eqUnion(loadAss, loadsFromHeap);
            oldPointsTo.eq(pointsTo);
            newPointsTo.eqMinus(newPointsTo, pointsTo);
            newPointsTo.eqIntersect(newPointsTo, typeManager.get());
            pointsTo.eqUnion(pointsTo, newPointsTo);
            if (this.pag.getOpts().verbose()) {
                G.v().out.println("Major iteration: " + newPointsTo.projectDownTo(this.pag.var).size() +
                                  " changed p2sets");
            }
        }while(!newPointsTo.isEmpty()); 
    }
    
    protected BDDPAG pag;
    
    protected OnFlyCallGraph ofcg;
}
