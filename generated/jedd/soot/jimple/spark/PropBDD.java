package soot.jimple.spark;

import soot.*;
import soot.util.queue.*;
import java.util.*;
import soot.options.SparkOptions;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.queue.*;

public final class PropBDD extends AbsPropagator {
    public PropBDD(Rsrc_dst simple,
                   Rsrc_fld_dst load,
                   Rsrc_fld_dst store,
                   Robj_var alloc,
                   Qvar_obj propout,
                   AbsPAG pag) {
        super(simple, load, store, alloc, propout, pag);
    }
    
    public final void update() {
        final jedd.internal.RelationContainer oldPointsTo =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1> oldPointsTo = jedd.internal.Jedd.v().falseBDD()" +
                                               "; at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/PropBDD" +
                                               ".jedd:39,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer newPointsTo =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1> newPointsTo = jedd.internal.Jedd.v().falseBDD()" +
                                               "; at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/PropBDD" +
                                               ".jedd:40,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer tmpPointsTo =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1> tmpPointsTo = jedd.internal.Jedd.v().falseBDD()" +
                                               "; at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/PropBDD" +
                                               ".jedd:41,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer pointsTo =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1> pointsTo = jedd.internal.Jedd.v().falseBDD(); a" +
                                               "t /home/olhotak/soot-2-jedd/src/soot/jimple/spark/PropBDD.je" +
                                               "dd:42,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer objectsBeingStored =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), var.v(), fld.v() },
                                              new jedd.PhysicalDomain[] { H2.v(), V1.v(), FD.v() },
                                              ("<soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bdddomai" +
                                               "ns.H2, soot.jimple.spark.bdddomains.var:soot.jimple.spark.bd" +
                                               "ddomains.V1, soot.jimple.spark.bdddomains.fld:soot.jimple.sp" +
                                               "ark.bdddomains.FD> objectsBeingStored; at /home/olhotak/soot" +
                                               "-2-jedd/src/soot/jimple/spark/PropBDD.jedd:44,8"));
        final jedd.internal.RelationContainer oldStorePt =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), var.v(), fld.v() },
                                              new jedd.PhysicalDomain[] { H2.v(), V1.v(), FD.v() },
                                              ("<soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bdddomai" +
                                               "ns.H2, soot.jimple.spark.bdddomains.var:soot.jimple.spark.bd" +
                                               "ddomains.V1, soot.jimple.spark.bdddomains.fld:soot.jimple.sp" +
                                               "ark.bdddomains.FD> oldStorePt = jedd.internal.Jedd.v().false" +
                                               "BDD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/Pr" +
                                               "opBDD.jedd:45,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer newStorePt =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), var.v(), fld.v() },
                                              new jedd.PhysicalDomain[] { H2.v(), V1.v(), FD.v() },
                                              ("<soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bdddomai" +
                                               "ns.H2, soot.jimple.spark.bdddomains.var:soot.jimple.spark.bd" +
                                               "ddomains.V1, soot.jimple.spark.bdddomains.fld:soot.jimple.sp" +
                                               "ark.bdddomains.FD> newStorePt = jedd.internal.Jedd.v().false" +
                                               "BDD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/Pr" +
                                               "opBDD.jedd:46,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer fieldPt =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { base.v(), fld.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { H1.v(), FD.v(), H2.v() },
                                              ("<soot.jimple.spark.bdddomains.base:soot.jimple.spark.bdddoma" +
                                               "ins.H1, soot.jimple.spark.bdddomains.fld:soot.jimple.spark.b" +
                                               "dddomains.FD, soot.jimple.spark.bdddomains.obj:soot.jimple.s" +
                                               "park.bdddomains.H2> fieldPt = jedd.internal.Jedd.v().falseBD" +
                                               "D(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/Prop" +
                                               "BDD.jedd:48,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer newFieldPt =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { base.v(), fld.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { H1.v(), FD.v(), H2.v() },
                                              ("<soot.jimple.spark.bdddomains.base:soot.jimple.spark.bdddoma" +
                                               "ins.H1, soot.jimple.spark.bdddomains.fld:soot.jimple.spark.b" +
                                               "dddomains.FD, soot.jimple.spark.bdddomains.obj:soot.jimple.s" +
                                               "park.bdddomains.H2> newFieldPt = jedd.internal.Jedd.v().fals" +
                                               "eBDD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/P" +
                                               "ropBDD.jedd:49,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer tmpFieldPt =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { base.v(), fld.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { H1.v(), FD.v(), H2.v() },
                                              ("<soot.jimple.spark.bdddomains.base:soot.jimple.spark.bdddoma" +
                                               "ins.H1, soot.jimple.spark.bdddomains.fld:soot.jimple.spark.b" +
                                               "dddomains.FD, soot.jimple.spark.bdddomains.obj:soot.jimple.s" +
                                               "park.bdddomains.H2> tmpFieldPt = jedd.internal.Jedd.v().fals" +
                                               "eBDD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/P" +
                                               "ropBDD.jedd:50,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer loadsFromHeap =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { base.v(), fld.v(), dst.v() },
                                              new jedd.PhysicalDomain[] { H1.v(), FD.v(), V2.v() },
                                              ("<soot.jimple.spark.bdddomains.base:soot.jimple.spark.bdddoma" +
                                               "ins.H1, soot.jimple.spark.bdddomains.fld:soot.jimple.spark.b" +
                                               "dddomains.FD, soot.jimple.spark.bdddomains.dst:soot.jimple.s" +
                                               "park.bdddomains.V2> loadsFromHeap = jedd.internal.Jedd.v().f" +
                                               "alseBDD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spar" +
                                               "k/PropBDD.jedd:53,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer loadAss =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { base.v(), fld.v(), dst.v() },
                                              new jedd.PhysicalDomain[] { H1.v(), FD.v(), V2.v() },
                                              ("<soot.jimple.spark.bdddomains.base:soot.jimple.spark.bdddoma" +
                                               "ins.H1, soot.jimple.spark.bdddomains.fld:soot.jimple.spark.b" +
                                               "dddomains.FD, soot.jimple.spark.bdddomains.dst:soot.jimple.s" +
                                               "park.bdddomains.V2> loadAss = jedd.internal.Jedd.v().falseBD" +
                                               "D(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/Prop" +
                                               "BDD.jedd:54,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer newPtFromNewEdges =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v(), obj.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1> newPtFromNewEdges = jedd.internal.Jedd.v().fals" +
                                               "eBDD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/P" +
                                               "ropBDD.jedd:56,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        final AbsTypeManager typeManager = SparkScene.v().tm;
        pointsTo.eq(pag.allAlloc().get());
        newPointsTo.eq(pointsTo);
        do  {
            do  {
                newPointsTo.eq(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(pag.allSimple().get()),
                                                                                             newPointsTo,
                                                                                             new jedd.PhysicalDomain[] { V1.v() }),
                                                              new jedd.PhysicalDomain[] { V2.v() },
                                                              new jedd.PhysicalDomain[] { V1.v() }));
                newPointsTo.eqMinus(pointsTo);
                newPointsTo.eqIntersect(typeManager.get());
                pointsTo.eqUnion(newPointsTo);
                if (SparkScene.v().options().verbose()) {
                    G.v().out.println("Minor iteration: " +
                                      new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v() },
                                                                          new jedd.PhysicalDomain[] { V1.v() },
                                                                          ("jedd.internal.Jedd.v().project(newPointsTo, new jedd.Physica" +
                                                                           "lDomain[...]).size() at /home/olhotak/soot-2-jedd/src/soot/j" +
                                                                           "imple/spark/PropBDD.jedd:75,45"),
                                                                          jedd.internal.Jedd.v().project(newPointsTo,
                                                                                                         new jedd.PhysicalDomain[] { H1.v() })).size() +
                                      " changed p2sets");
                }
            }while(!jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(newPointsTo),
                                                  jedd.internal.Jedd.v().falseBDD())); 
            newPointsTo.eq(jedd.internal.Jedd.v().minus(jedd.internal.Jedd.v().read(pointsTo), oldPointsTo));
            ptout.add(new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), var.v() },
                                                          new jedd.PhysicalDomain[] { H1.v(), V1.v() },
                                                          ("ptout.add(newPointsTo) at /home/olhotak/soot-2-jedd/src/soot" +
                                                           "/jimple/spark/PropBDD.jedd:82,12"),
                                                          newPointsTo));
            SparkScene.v().updateCallGraph();
            objectsBeingStored.eq(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(pag.allStore().get()),
                                                                                                jedd.internal.Jedd.v().replace(newPointsTo,
                                                                                                                               new jedd.PhysicalDomain[] { H1.v() },
                                                                                                                               new jedd.PhysicalDomain[] { H2.v() }),
                                                                                                new jedd.PhysicalDomain[] { V1.v() }),
                                                                 new jedd.PhysicalDomain[] { V2.v() },
                                                                 new jedd.PhysicalDomain[] { V1.v() }));
            newStorePt.eq(jedd.internal.Jedd.v().minus(jedd.internal.Jedd.v().read(objectsBeingStored), oldStorePt));
            oldStorePt.eqUnion(newStorePt);
            newFieldPt.eq(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(oldStorePt),
                                                         newPointsTo,
                                                         new jedd.PhysicalDomain[] { V1.v() }));
            tmpFieldPt.eq(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(newStorePt),
                                                         oldPointsTo,
                                                         new jedd.PhysicalDomain[] { V1.v() }));
            newFieldPt.eqUnion(tmpFieldPt);
            newFieldPt.eqMinus(fieldPt);
            fieldPt.eqUnion(newFieldPt);
            loadsFromHeap.eq(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(pag.allLoad().get()),
                                                            newPointsTo,
                                                            new jedd.PhysicalDomain[] { V1.v() }));
            loadsFromHeap.eqMinus(loadAss);
            newPointsTo.eq(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(loadAss),
                                                                                         newFieldPt,
                                                                                         new jedd.PhysicalDomain[] { H1.v(), FD.v() }),
                                                          new jedd.PhysicalDomain[] { V2.v(), H2.v() },
                                                          new jedd.PhysicalDomain[] { V1.v(), H1.v() }));
            tmpPointsTo.eq(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(loadsFromHeap),
                                                                                         fieldPt,
                                                                                         new jedd.PhysicalDomain[] { H1.v(), FD.v() }),
                                                          new jedd.PhysicalDomain[] { V2.v(), H2.v() },
                                                          new jedd.PhysicalDomain[] { V1.v(), H1.v() }));
            newPointsTo.eqUnion(tmpPointsTo);
            loadAss.eqUnion(loadsFromHeap);
            oldPointsTo.eq(pointsTo);
            newPointsTo.eqMinus(pointsTo);
            newPointsTo.eqIntersect(typeManager.get());
            ptout.add(new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v(), var.v() },
                                                          new jedd.PhysicalDomain[] { H1.v(), V1.v() },
                                                          ("ptout.add(newPointsTo) at /home/olhotak/soot-2-jedd/src/soot" +
                                                           "/jimple/spark/PropBDD.jedd:121,12"),
                                                          newPointsTo));
            SparkScene.v().updateCallGraph();
            pointsTo.eqUnion(newPointsTo);
            newPointsTo.eqUnion(newPtFromNewEdges);
            newPtFromNewEdges.eq(jedd.internal.Jedd.v().falseBDD());
            if (SparkScene.v().options().verbose()) {
                G.v().out.println("Major iteration: " +
                                  new jedd.internal.RelationContainer(new jedd.Attribute[] { var.v() },
                                                                      new jedd.PhysicalDomain[] { V1.v() },
                                                                      ("jedd.internal.Jedd.v().project(newPointsTo, new jedd.Physica" +
                                                                       "lDomain[...]).size() at /home/olhotak/soot-2-jedd/src/soot/j" +
                                                                       "imple/spark/PropBDD.jedd:132,45"),
                                                                      jedd.internal.Jedd.v().project(newPointsTo,
                                                                                                     new jedd.PhysicalDomain[] { H1.v() })).size() +
                                  " changed p2sets");
            }
        }while(!jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(newPointsTo),
                                              jedd.internal.Jedd.v().falseBDD())); 
    }
}
