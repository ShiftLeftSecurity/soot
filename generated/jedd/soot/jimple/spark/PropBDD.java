package soot.jimple.spark;

import soot.*;
import soot.util.queue.*;
import java.util.*;
import soot.options.SparkOptions;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.queue.*;
import jedd.*;

public final class PropBDD extends AbsPropagator {
    public PropBDD(Rsrc_dst simple,
                   Rsrc_fld_dst load,
                   Rsrc_fld_dst store,
                   Robj_var alloc,
                   Qvar_obj propout,
                   AbsPAG pag) {
        super(simple, load, store, alloc, propout, pag);
    }
    
    final jedd.internal.RelationContainer pointsTo =
      new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v() },
                                          new PhysicalDomain[] { V1.v(), H1.v() },
                                          ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                           "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                           "ddomains.H1> pointsTo = jedd.internal.Jedd.v().falseBDD() at" +
                                           " /home/olhotak/soot-2-jedd/src/soot/jimple/spark/PropBDD.jed" +
                                           "d:39,4-20"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    final jedd.internal.RelationContainer fieldPt =
      new jedd.internal.RelationContainer(new Attribute[] { base.v(), fld.v(), obj.v() },
                                          new PhysicalDomain[] { H1.v(), FD.v(), H2.v() },
                                          ("<soot.jimple.spark.bdddomains.base:soot.jimple.spark.bdddoma" +
                                           "ins.H1, soot.jimple.spark.bdddomains.fld:soot.jimple.spark.b" +
                                           "dddomains.FD, soot.jimple.spark.bdddomains.obj:soot.jimple.s" +
                                           "park.bdddomains.H2> fieldPt = jedd.internal.Jedd.v().falseBD" +
                                           "D() at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/PropB" +
                                           "DD.jedd:40,4-29"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    public final void update() {
        final jedd.internal.RelationContainer oldPointsTo =
          new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v() },
                                              new PhysicalDomain[] { V1.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1> oldPointsTo = jedd.internal.Jedd.v().falseBDD()" +
                                               "; at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/PropBDD" +
                                               ".jedd:147,25-36"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer veryOldPointsTo =
          new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v() },
                                              new PhysicalDomain[] { V1.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1> veryOldPointsTo = jedd.internal.Jedd.v().falseB" +
                                               "DD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/Pro" +
                                               "pBDD.jedd:148,25-40"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer outputtedPointsTo =
          new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v() },
                                              new PhysicalDomain[] { V1.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1> outputtedPointsTo = jedd.internal.Jedd.v().fals" +
                                               "eBDD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/P" +
                                               "ropBDD.jedd:149,25-42"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer objectsBeingStored =
          new jedd.internal.RelationContainer(new Attribute[] { obj.v(), var.v(), fld.v() },
                                              new PhysicalDomain[] { H2.v(), V1.v(), FD.v() },
                                              ("<soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bdddomai" +
                                               "ns.H2, soot.jimple.spark.bdddomains.var:soot.jimple.spark.bd" +
                                               "ddomains.V1, soot.jimple.spark.bdddomains.fld:soot.jimple.sp" +
                                               "ark.bdddomains.FD> objectsBeingStored; at /home/olhotak/soot" +
                                               "-2-jedd/src/soot/jimple/spark/PropBDD.jedd:151,33-51"));
        final jedd.internal.RelationContainer oldStorePt =
          new jedd.internal.RelationContainer(new Attribute[] { obj.v(), var.v(), fld.v() },
                                              new PhysicalDomain[] { H2.v(), V1.v(), FD.v() },
                                              ("<soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bdddomai" +
                                               "ns.H2, soot.jimple.spark.bdddomains.var:soot.jimple.spark.bd" +
                                               "ddomains.V1, soot.jimple.spark.bdddomains.fld:soot.jimple.sp" +
                                               "ark.bdddomains.FD> oldStorePt = jedd.internal.Jedd.v().false" +
                                               "BDD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/Pr" +
                                               "opBDD.jedd:152,33-43"),
                                              jedd.internal.Jedd.v().falseBDD());
        final jedd.internal.RelationContainer loadsFromHeap =
          new jedd.internal.RelationContainer(new Attribute[] { base.v(), fld.v(), dst.v() },
                                              new PhysicalDomain[] { H1.v(), FD.v(), V2.v() },
                                              ("<soot.jimple.spark.bdddomains.base:soot.jimple.spark.bdddoma" +
                                               "ins.H1, soot.jimple.spark.bdddomains.fld:soot.jimple.spark.b" +
                                               "dddomains.FD, soot.jimple.spark.bdddomains.dst:soot.jimple.s" +
                                               "park.bdddomains.V2> loadsFromHeap = jedd.internal.Jedd.v().f" +
                                               "alseBDD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spar" +
                                               "k/PropBDD.jedd:155,34-47"),
                                              jedd.internal.Jedd.v().falseBDD());
        final AbsTypeManager typeManager = SparkScene.v().tm;
        do  {
            veryOldPointsTo.eq(pointsTo);
            pointsTo.eqUnion(jedd.internal.Jedd.v().intersect(jedd.internal.Jedd.v().read(newAlloc.get()),
                                                              typeManager.get()));
            do  {
                oldPointsTo.eq(pointsTo);
                pointsTo.eqUnion(jedd.internal.Jedd.v().intersect(jedd.internal.Jedd.v().read(typeManager.get()),
                                                                  jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(pag.allSimple().get()),
                                                                                                                                pointsTo,
                                                                                                                                new PhysicalDomain[] { V1.v() }),
                                                                                                 new PhysicalDomain[] { V2.v() },
                                                                                                 new PhysicalDomain[] { V1.v() })));
            }while(!jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(pointsTo), oldPointsTo)); 
            ptout.add(new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v() },
                                                          new PhysicalDomain[] { V1.v(), H1.v() },
                                                          ("ptout.add(jedd.internal.Jedd.v().minus(jedd.internal.Jedd.v(" +
                                                           ").read(pointsTo), outputtedPointsTo)) at /home/olhotak/soot-" +
                                                           "2-jedd/src/soot/jimple/spark/PropBDD.jedd:176,12-17"),
                                                          jedd.internal.Jedd.v().minus(jedd.internal.Jedd.v().read(pointsTo),
                                                                                       outputtedPointsTo)));
            outputtedPointsTo.eq(pointsTo);
            SparkScene.v().updateCallGraph();
            objectsBeingStored.eq(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(pag.allStore().get()),
                                                                                                pointsTo,
                                                                                                new PhysicalDomain[] { V1.v() }),
                                                                 new PhysicalDomain[] { V2.v(), H1.v() },
                                                                 new PhysicalDomain[] { V1.v(), H2.v() }));
            fieldPt.eqUnion(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(objectsBeingStored),
                                                           pointsTo,
                                                           new PhysicalDomain[] { V1.v() }));
            loadsFromHeap.eq(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(pag.allLoad().get()),
                                                            pointsTo,
                                                            new PhysicalDomain[] { V1.v() }));
            pointsTo.eqUnion(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(loadsFromHeap,
                                                                                                                                                      new PhysicalDomain[] { V2.v() },
                                                                                                                                                      new PhysicalDomain[] { V1.v() })),
                                                                                           fieldPt,
                                                                                           new PhysicalDomain[] { H1.v(), FD.v() }),
                                                            new PhysicalDomain[] { H2.v() },
                                                            new PhysicalDomain[] { H1.v() }));
            pointsTo.eqIntersect(typeManager.get());
            ptout.add(new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v() },
                                                          new PhysicalDomain[] { V1.v(), H1.v() },
                                                          ("ptout.add(jedd.internal.Jedd.v().minus(jedd.internal.Jedd.v(" +
                                                           ").read(pointsTo), outputtedPointsTo)) at /home/olhotak/soot-" +
                                                           "2-jedd/src/soot/jimple/spark/PropBDD.jedd:193,12-17"),
                                                          jedd.internal.Jedd.v().minus(jedd.internal.Jedd.v().read(pointsTo),
                                                                                       outputtedPointsTo)));
            outputtedPointsTo.eq(pointsTo);
            SparkScene.v().updateCallGraph();
            if (SparkScene.v().options().verbose()) { G.v().out.println("Major iteration: "); }
        }while(!jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(pointsTo), veryOldPointsTo)); 
    }
}
