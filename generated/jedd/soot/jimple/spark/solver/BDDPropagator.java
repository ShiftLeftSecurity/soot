package soot.jimple.spark.solver;

import soot.jimple.spark.*;
import soot.jimple.spark.pag.*;
import soot.jimple.spark.sets.*;
import soot.jimple.spark.internal.*;
import soot.*;
import soot.util.queue.*;
import java.util.*;
import soot.options.SparkOptions;
import soot.jimple.spark.bdddomains.*;

public final class BDDPropagator extends Propagator {
    public BDDPropagator(BDDPAG pag) {
        super();
        this.pag = pag;
    }
    
    public final void propagate() {
        final jedd.Relation oldPointsTo =
          new jedd.Relation(new jedd.Domain[] { var.v(), obj.v() },
                            new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                            jedd.Jedd.v().falseBDD());
        final jedd.Relation newPointsTo =
          new jedd.Relation(new jedd.Domain[] { var.v(), obj.v() },
                            new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                            jedd.Jedd.v().falseBDD());
        final jedd.Relation tmpPointsTo =
          new jedd.Relation(new jedd.Domain[] { var.v(), obj.v() },
                            new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                            jedd.Jedd.v().falseBDD());
        final jedd.Relation objectsBeingStored =
          new jedd.Relation(new jedd.Domain[] { obj.v(), var.v(), fld.v() },
                            new jedd.PhysicalDomain[] { H2.v(), V1.v(), FD.v() });
        final jedd.Relation oldStorePt =
          new jedd.Relation(new jedd.Domain[] { obj.v(), var.v(), fld.v() },
                            new jedd.PhysicalDomain[] { H2.v(), V1.v(), FD.v() },
                            jedd.Jedd.v().falseBDD());
        final jedd.Relation newStorePt =
          new jedd.Relation(new jedd.Domain[] { obj.v(), var.v(), fld.v() },
                            new jedd.PhysicalDomain[] { H2.v(), V1.v(), FD.v() },
                            jedd.Jedd.v().falseBDD());
        final jedd.Relation newFieldPt =
          new jedd.Relation(new jedd.Domain[] { base.v(), fld.v(), obj.v() },
                            new jedd.PhysicalDomain[] { H1.v(), FD.v(), H2.v() },
                            jedd.Jedd.v().falseBDD());
        final jedd.Relation tmpFieldPt =
          new jedd.Relation(new jedd.Domain[] { base.v(), fld.v(), obj.v() },
                            new jedd.PhysicalDomain[] { H1.v(), FD.v(), H2.v() },
                            jedd.Jedd.v().falseBDD());
        final jedd.Relation loadsFromHeap =
          new jedd.Relation(new jedd.Domain[] { base.v(), fld.v(), dst.v() },
                            new jedd.PhysicalDomain[] { H1.v(), FD.v(), V2.v() },
                            jedd.Jedd.v().falseBDD());
        final jedd.Relation loadAss =
          new jedd.Relation(new jedd.Domain[] { base.v(), fld.v(), dst.v() },
                            new jedd.PhysicalDomain[] { H1.v(), FD.v(), V2.v() },
                            jedd.Jedd.v().falseBDD());
        final BDDTypeManager typeManager = (BDDTypeManager) this.pag.getTypeManager();
        this.pag.pointsTo.eq(this.pag.alloc);
        newPointsTo.eq(this.pag.pointsTo);
        do  {
            do  {
                newPointsTo.eq(jedd.Jedd.v().replace(jedd.Jedd.v().relprod(jedd.Jedd.v().read(this.pag.edgeSet),
                                                                           newPointsTo,
                                                                           new jedd.PhysicalDomain[] { V1.v() }),
                                                     new jedd.PhysicalDomain[] { V2.v() },
                                                     new jedd.PhysicalDomain[] { V1.v() }));
                newPointsTo.eqMinus(this.pag.pointsTo);
                newPointsTo.eqIntersect(typeManager.get());
                this.pag.pointsTo.eqUnion(newPointsTo);
                if (this.pag.getOpts().verbose()) {
                    G.v().out.println("Minor iteration: " +
                                      new jedd.Relation(new jedd.Domain[] { var.v() },
                                                        new jedd.PhysicalDomain[] { V1.v() },
                                                        jedd.Jedd.v().project(newPointsTo,
                                                                              new jedd.PhysicalDomain[] { H1.v() })).size() +
                                      " changed p2sets");
                }
            }while(!jedd.Jedd.v().equals(jedd.Jedd.v().read(newPointsTo), jedd.Jedd.v().falseBDD())); 
            newPointsTo.eq(jedd.Jedd.v().minus(jedd.Jedd.v().read(this.pag.pointsTo), oldPointsTo));
            objectsBeingStored.eq(jedd.Jedd.v().replace(jedd.Jedd.v().relprod(jedd.Jedd.v().read(this.pag.stores),
                                                                              jedd.Jedd.v().replace(newPointsTo,
                                                                                                    new jedd.PhysicalDomain[] { H1.v() },
                                                                                                    new jedd.PhysicalDomain[] { H2.v() }),
                                                                              new jedd.PhysicalDomain[] { V1.v() }),
                                                        new jedd.PhysicalDomain[] { V2.v() },
                                                        new jedd.PhysicalDomain[] { V1.v() }));
            newStorePt.eq(jedd.Jedd.v().minus(jedd.Jedd.v().read(objectsBeingStored), oldStorePt));
            oldStorePt.eqUnion(newStorePt);
            newFieldPt.eq(jedd.Jedd.v().relprod(jedd.Jedd.v().read(oldStorePt),
                                                newPointsTo,
                                                new jedd.PhysicalDomain[] { V1.v() }));
            tmpFieldPt.eq(jedd.Jedd.v().relprod(jedd.Jedd.v().read(newStorePt),
                                                oldPointsTo,
                                                new jedd.PhysicalDomain[] { V1.v() }));
            newFieldPt.eqUnion(tmpFieldPt);
            newFieldPt.eqMinus(this.pag.fieldPt);
            this.pag.fieldPt.eqUnion(newFieldPt);
            loadsFromHeap.eq(jedd.Jedd.v().relprod(jedd.Jedd.v().read(this.pag.loads),
                                                   newPointsTo,
                                                   new jedd.PhysicalDomain[] { V1.v() }));
            loadsFromHeap.eqMinus(loadAss);
            newPointsTo.eq(jedd.Jedd.v().replace(jedd.Jedd.v().relprod(jedd.Jedd.v().read(loadAss),
                                                                       newFieldPt,
                                                                       new jedd.PhysicalDomain[] { H1.v(), FD.v() }),
                                                 new jedd.PhysicalDomain[] { V2.v(), H2.v() },
                                                 new jedd.PhysicalDomain[] { V1.v(), H1.v() }));
            tmpPointsTo.eq(jedd.Jedd.v().replace(jedd.Jedd.v().relprod(jedd.Jedd.v().read(loadsFromHeap),
                                                                       this.pag.fieldPt,
                                                                       new jedd.PhysicalDomain[] { H1.v(), FD.v() }),
                                                 new jedd.PhysicalDomain[] { V2.v(), H2.v() },
                                                 new jedd.PhysicalDomain[] { V1.v(), H1.v() }));
            newPointsTo.eqUnion(tmpPointsTo);
            loadAss.eqUnion(loadsFromHeap);
            oldPointsTo.eq(this.pag.pointsTo);
            newPointsTo.eqMinus(this.pag.pointsTo);
            newPointsTo.eqIntersect(typeManager.get());
            this.pag.pointsTo.eqUnion(newPointsTo);
            if (this.pag.getOpts().verbose()) {
                G.v().out.println("Major iteration: " +
                                  new jedd.Relation(new jedd.Domain[] { var.v() },
                                                    new jedd.PhysicalDomain[] { V1.v() },
                                                    jedd.Jedd.v().project(newPointsTo,
                                                                          new jedd.PhysicalDomain[] { H1.v() })).size() +
                                  " changed p2sets");
            }
        }while(!jedd.Jedd.v().equals(jedd.Jedd.v().read(newPointsTo), jedd.Jedd.v().falseBDD())); 
    }
    
    protected BDDPAG pag;
    
    protected OnFlyCallGraph ofcg;
}
