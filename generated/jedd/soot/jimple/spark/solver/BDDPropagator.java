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
        final BDDOnFlyCallGraph ofcg = pag.getOnFlyCallGraph();
        final jedd.internal.RelationContainer oldPointsTo =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1> oldPointsTo = jedd.internal.Jedd.v().falseBDD()" +
                                               "; at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/solver/" +
                                               "BDDPropagator.jedd:40,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer newPointsTo =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1> newPointsTo = jedd.internal.Jedd.v().falseBDD()" +
                                               "; at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/solver/" +
                                               "BDDPropagator.jedd:41,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer tmpPointsTo =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1> tmpPointsTo = jedd.internal.Jedd.v().falseBDD()" +
                                               "; at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/solver/" +
                                               "BDDPropagator.jedd:42,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer objectsBeingStored =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), var.v(), fld.v() },
                                              new jedd.PhysicalDomain[] { H2.v(), V1.v(), FD.v() },
                                              ("<soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bdddomai" +
                                               "ns.H2, soot.jimple.spark.bdddomains.var:soot.jimple.spark.bd" +
                                               "ddomains.V1, soot.jimple.spark.bdddomains.fld:soot.jimple.sp" +
                                               "ark.bdddomains.FD> objectsBeingStored; at /home/olhotak/soot" +
                                               "-2-jedd/src/soot/jimple/spark/solver/BDDPropagator.jedd:44,8"));
        final jedd.internal.RelationContainer oldStorePt =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), var.v(), fld.v() },
                                              new jedd.PhysicalDomain[] { H2.v(), V1.v(), FD.v() },
                                              ("<soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bdddomai" +
                                               "ns.H2, soot.jimple.spark.bdddomains.var:soot.jimple.spark.bd" +
                                               "ddomains.V1, soot.jimple.spark.bdddomains.fld:soot.jimple.sp" +
                                               "ark.bdddomains.FD> oldStorePt = jedd.internal.Jedd.v().false" +
                                               "BDD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/so" +
                                               "lver/BDDPropagator.jedd:45,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer newStorePt =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), var.v(), fld.v() },
                                              new jedd.PhysicalDomain[] { H2.v(), V1.v(), FD.v() },
                                              ("<soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bdddomai" +
                                               "ns.H2, soot.jimple.spark.bdddomains.var:soot.jimple.spark.bd" +
                                               "ddomains.V1, soot.jimple.spark.bdddomains.fld:soot.jimple.sp" +
                                               "ark.bdddomains.FD> newStorePt = jedd.internal.Jedd.v().false" +
                                               "BDD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/so" +
                                               "lver/BDDPropagator.jedd:46,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer newFieldPt =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { base.v(), fld.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { H1.v(), FD.v(), H2.v() },
                                              ("<soot.jimple.spark.bdddomains.base:soot.jimple.spark.bdddoma" +
                                               "ins.H1, soot.jimple.spark.bdddomains.fld:soot.jimple.spark.b" +
                                               "dddomains.FD, soot.jimple.spark.bdddomains.obj:soot.jimple.s" +
                                               "park.bdddomains.H2> newFieldPt = jedd.internal.Jedd.v().fals" +
                                               "eBDD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/s" +
                                               "olver/BDDPropagator.jedd:48,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer tmpFieldPt =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { base.v(), fld.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { H1.v(), FD.v(), H2.v() },
                                              ("<soot.jimple.spark.bdddomains.base:soot.jimple.spark.bdddoma" +
                                               "ins.H1, soot.jimple.spark.bdddomains.fld:soot.jimple.spark.b" +
                                               "dddomains.FD, soot.jimple.spark.bdddomains.obj:soot.jimple.s" +
                                               "park.bdddomains.H2> tmpFieldPt = jedd.internal.Jedd.v().fals" +
                                               "eBDD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/s" +
                                               "olver/BDDPropagator.jedd:49,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer loadsFromHeap =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { base.v(), fld.v(), dst.v() },
                                              new jedd.PhysicalDomain[] { H1.v(), FD.v(), V2.v() },
                                              ("<soot.jimple.spark.bdddomains.base:soot.jimple.spark.bdddoma" +
                                               "ins.H1, soot.jimple.spark.bdddomains.fld:soot.jimple.spark.b" +
                                               "dddomains.FD, soot.jimple.spark.bdddomains.dst:soot.jimple.s" +
                                               "park.bdddomains.V2> loadsFromHeap = jedd.internal.Jedd.v().f" +
                                               "alseBDD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spar" +
                                               "k/solver/BDDPropagator.jedd:52,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer loadAss =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { base.v(), fld.v(), dst.v() },
                                              new jedd.PhysicalDomain[] { H1.v(), FD.v(), V2.v() },
                                              ("<soot.jimple.spark.bdddomains.base:soot.jimple.spark.bdddoma" +
                                               "ins.H1, soot.jimple.spark.bdddomains.fld:soot.jimple.spark.b" +
                                               "dddomains.FD, soot.jimple.spark.bdddomains.dst:soot.jimple.s" +
                                               "park.bdddomains.V2> loadAss = jedd.internal.Jedd.v().falseBD" +
                                               "D(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/solv" +
                                               "er/BDDPropagator.jedd:53,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final BDDTypeManager typeManager = (BDDTypeManager) pag.getTypeManager();
        pag.pointsTo.eq(pag.alloc);
        newPointsTo.eq(pag.pointsTo);
        int iterations = 0;
        do  {
            do  {
                newPointsTo.eq(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(pag.edgeSet),
                                                                                             newPointsTo,
                                                                                             new jedd.PhysicalDomain[] { V1.v() }),
                                                              new jedd.PhysicalDomain[] { V2.v() },
                                                              new jedd.PhysicalDomain[] { V1.v() }));
                newPointsTo.eqMinus(pag.pointsTo);
                newPointsTo.eqIntersect(typeManager.get());
                pag.pointsTo.eqUnion(newPointsTo);
                if (pag.getOpts().verbose()) {
                    G.v().out.println("Minor iteration: " +
                                      new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v() },
                                                                          new jedd.PhysicalDomain[] { V1.v() },
                                                                          ("jedd.internal.Jedd.v().project(newPointsTo, new jedd.Physica" +
                                                                           "lDomain[...]).size() at /home/olhotak/soot-2-jedd/src/soot/j" +
                                                                           "imple/spark/solver/BDDPropagator.jedd:73,45"),
                                                                          jedd.internal.Jedd.v().project(newPointsTo,
                                                                                                         new jedd.PhysicalDomain[] { H1.v() })).size() +
                                      " changed p2sets");
                }
            }while(!jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(newPointsTo),
                                                  jedd.internal.Jedd.v().falseBDD())); 
            newPointsTo.eq(jedd.internal.Jedd.v().minus(jedd.internal.Jedd.v().read(pag.pointsTo), oldPointsTo));
            if (ofcg != null) {
                ofcg.updatedNodes(new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), type.v() },
                                                                      new jedd.PhysicalDomain[] { V3.v(), T2.v() },
                                                                      ("ofcg.updatedNodes(jedd.internal.Jedd.v().compose(jedd.intern" +
                                                                       "al.Jedd.v().read(jedd.internal.Jedd.v().replace(newPointsTo," +
                                                                       " new jedd.PhysicalDomain[...], new jedd.PhysicalDomain[...])" +
                                                                       "), jedd.internal.Jedd.v().replace(typeManager.allocNodeType(" +
                                                                       "), new jedd.PhysicalDomain[...], new jedd.PhysicalDomain[..." +
                                                                       "]), new jedd.PhysicalDomain[...])) at /home/olhotak/soot-2-j" +
                                                                       "edd/src/soot/jimple/spark/solver/BDDPropagator.jedd:80,16"),
                                                                      jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(newPointsTo,
                                                                                                                                                                new jedd.PhysicalDomain[] { V1.v() },
                                                                                                                                                                new jedd.PhysicalDomain[] { V3.v() })),
                                                                                                     jedd.internal.Jedd.v().replace(typeManager.allocNodeType(),
                                                                                                                                    new jedd.PhysicalDomain[] { T1.v() },
                                                                                                                                    new jedd.PhysicalDomain[] { T2.v() }),
                                                                                                     new jedd.PhysicalDomain[] { H1.v() })));
                ofcg.build();
            }
            objectsBeingStored.eq(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(pag.stores),
                                                                                                newPointsTo,
                                                                                                new jedd.PhysicalDomain[] { V1.v() }),
                                                                 new jedd.PhysicalDomain[] { V2.v(), H1.v() },
                                                                 new jedd.PhysicalDomain[] { V1.v(), H2.v() }));
            newStorePt.eq(jedd.internal.Jedd.v().minus(jedd.internal.Jedd.v().read(objectsBeingStored), oldStorePt));
            oldStorePt.eqUnion(newStorePt);
            newFieldPt.eq(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(oldStorePt),
                                                         newPointsTo,
                                                         new jedd.PhysicalDomain[] { V1.v() }));
            tmpFieldPt.eq(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(newStorePt),
                                                         oldPointsTo,
                                                         new jedd.PhysicalDomain[] { V1.v() }));
            newFieldPt.eqUnion(tmpFieldPt);
            newFieldPt.eqMinus(pag.fieldPt);
            pag.fieldPt.eqUnion(newFieldPt);
            loadsFromHeap.eq(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(pag.loads),
                                                            newPointsTo,
                                                            new jedd.PhysicalDomain[] { V1.v() }));
            loadsFromHeap.eqMinus(loadAss);
            newPointsTo.eq(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(loadAss,
                                                                                                                                                    new jedd.PhysicalDomain[] { V2.v() },
                                                                                                                                                    new jedd.PhysicalDomain[] { V1.v() })),
                                                                                         newFieldPt,
                                                                                         new jedd.PhysicalDomain[] { H1.v(), FD.v() }),
                                                          new jedd.PhysicalDomain[] { H2.v() },
                                                          new jedd.PhysicalDomain[] { H1.v() }));
            tmpPointsTo.eq(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(loadsFromHeap),
                                                                                         pag.fieldPt,
                                                                                         new jedd.PhysicalDomain[] { H1.v(), FD.v() }),
                                                          new jedd.PhysicalDomain[] { V2.v(), H2.v() },
                                                          new jedd.PhysicalDomain[] { V1.v(), H1.v() }));
            newPointsTo.eqUnion(tmpPointsTo);
            loadAss.eqUnion(loadsFromHeap);
            oldPointsTo.eq(pag.pointsTo);
            newPointsTo.eqMinus(pag.pointsTo);
            newPointsTo.eqIntersect(typeManager.get());
            pag.pointsTo.eqUnion(newPointsTo);
            if (pag.getOpts().verbose()) {
                G.v().out.println("Major iteration: " +
                                  new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v() },
                                                                      new jedd.PhysicalDomain[] { V1.v() },
                                                                      ("jedd.internal.Jedd.v().project(newPointsTo, new jedd.Physica" +
                                                                       "lDomain[...]).size() at /home/olhotak/soot-2-jedd/src/soot/j" +
                                                                       "imple/spark/solver/BDDPropagator.jedd:123,45"),
                                                                      jedd.internal.Jedd.v().project(newPointsTo,
                                                                                                     new jedd.PhysicalDomain[] { H1.v() })).size() +
                                  " changed p2sets");
            }
        }while(!jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(newPointsTo),
                                              jedd.internal.Jedd.v().falseBDD()) ||
                 iterations++ < 10); 
    }
    
    protected BDDPAG pag;
}
