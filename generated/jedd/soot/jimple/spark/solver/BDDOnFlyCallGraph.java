package soot.jimple.spark.solver;

import soot.jimple.*;
import soot.jimple.spark.*;
import soot.jimple.spark.sets.*;
import soot.jimple.spark.pag.*;
import soot.jimple.spark.builder.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import java.util.*;
import soot.util.*;
import soot.util.queue.*;
import soot.options.SparkOptions;
import soot.toolkits.scalar.Pair;
import soot.jimple.spark.bdddomains.*;
import jedd.*;

public class BDDOnFlyCallGraph {
    private BDDOnFlyCallGraphBuilder ofcgb;
    
    private BDDReachableMethods reachableMethods;
    
    private BDDReader reachablesReader;
    
    private BDDReader callEdges;
    
    private BDDCallGraph callGraph;
    
    public BDDReachableMethods reachableMethods() { return reachableMethods; }
    
    public BDDCallGraph callGraph() { return callGraph; }
    
    public BDDOnFlyCallGraph(BDDPAG pag) {
        super();
        this.pag = pag;
        callGraph = new BDDCallGraph();
        BDDContextManager cm = BDDCallGraphBuilder.makeContextManager(callGraph);
        reachableMethods = new BDDReachableMethods(callGraph, Scene.v().getEntryPoints());
        ofcgb = new BDDOnFlyCallGraphBuilder(pag, cm, reachableMethods);
        reachablesReader = reachableMethods.listener();
        callEdges = cm.callGraph().listener();
    }
    
    public void build() {
        ofcgb.processReachables();
        this.processReachables();
        this.processCallEdges();
    }
    
    private void processReachables() {
        reachableMethods.update();
        while (true) {
            final jedd.internal.RelationContainer methodContexts =
              new jedd.internal.RelationContainer(new Attribute[] { method.v(), ctxt.v() },
                                                  new PhysicalDomain[] { V1.v(), T1.v() },
                                                  ("<soot.jimple.spark.bdddomains.method:soot.jimple.spark.bdddo" +
                                                   "mains.V1, soot.jimple.spark.bdddomains.ctxt:soot.jimple.spar" +
                                                   "k.bdddomains.T1> methodContexts = reachablesReader.next(); a" +
                                                   "t /home/olhotak/soot-2-jedd/src/soot/jimple/spark/solver/BDD" +
                                                   "OnFlyCallGraph.jedd:70,12"),
                                                  reachablesReader.next());
            if (jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(methodContexts),
                                              jedd.internal.Jedd.v().falseBDD()))
                return;
            this.addToPAG(new jedd.internal.RelationContainer(new Attribute[] { ctxt.v(), method.v() },
                                                              new PhysicalDomain[] { T2.v(), V2.v() },
                                                              ("this.addToPAG(jedd.internal.Jedd.v().replace(methodContexts," +
                                                               " new jedd.PhysicalDomain[...], new jedd.PhysicalDomain[...])" +
                                                               ") at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/solver/" +
                                                               "BDDOnFlyCallGraph.jedd:72,12"),
                                                              jedd.internal.Jedd.v().replace(methodContexts,
                                                                                             new PhysicalDomain[] { T1.v(), V1.v() },
                                                                                             new PhysicalDomain[] { T2.v(), V2.v() })));
        }
    }
    
    private void addToPAG(final jedd.internal.RelationContainer methodContexts) {
        final jedd.internal.RelationContainer methods =
          new jedd.internal.RelationContainer(new Attribute[] { method.v() },
                                              new PhysicalDomain[] { V2.v() },
                                              ("<soot.jimple.spark.bdddomains.method:soot.jimple.spark.bdddo" +
                                               "mains.V2> methods = jedd.internal.Jedd.v().project(methodCon" +
                                               "texts, new jedd.PhysicalDomain[...]); at /home/olhotak/soot-" +
                                               "2-jedd/src/soot/jimple/spark/solver/BDDOnFlyCallGraph.jedd:7" +
                                               "6,8"),
                                              jedd.internal.Jedd.v().project(methodContexts,
                                                                             new PhysicalDomain[] { T2.v() }));
        for (Iterator methIt =
               new jedd.internal.RelationContainer(new Attribute[] { method.v() },
                                                   new PhysicalDomain[] { V2.v() },
                                                   ("methods.iterator() at /home/olhotak/soot-2-jedd/src/soot/jim" +
                                                    "ple/spark/solver/BDDOnFlyCallGraph.jedd:77,31"),
                                                   methods).iterator();
             methIt.hasNext();
             ) {
            final SootMethod meth = (SootMethod) methIt.next();
            AbstractMethodPAG mpag = AbstractMethodPAG.v(pag, meth);
            mpag.build();
            final jedd.internal.RelationContainer contexts =
              new jedd.internal.RelationContainer(new Attribute[] { ctxt.v() },
                                                  new PhysicalDomain[] { T2.v() },
                                                  ("<soot.jimple.spark.bdddomains.ctxt:soot.jimple.spark.bdddoma" +
                                                   "ins.T2> contexts = jedd.internal.Jedd.v().compose(jedd.inter" +
                                                   "nal.Jedd.v().read(methodContexts), jedd.internal.Jedd.v().li" +
                                                   "teral(new java.lang.Object[...], new jedd.Attribute[...], ne" +
                                                   "w jedd.PhysicalDomain[...]), new jedd.PhysicalDomain[...]); " +
                                                   "at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/solver/BD" +
                                                   "DOnFlyCallGraph.jedd:81,12"),
                                                  jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(methodContexts),
                                                                                 jedd.internal.Jedd.v().literal(new Object[] { meth },
                                                                                                                new Attribute[] { method.v() },
                                                                                                                new PhysicalDomain[] { V2.v() }),
                                                                                 new PhysicalDomain[] { V2.v() }));
            for (Iterator contextIt =
                   new jedd.internal.RelationContainer(new Attribute[] { ctxt.v() },
                                                       new PhysicalDomain[] { T2.v() },
                                                       ("contexts.iterator() at /home/olhotak/soot-2-jedd/src/soot/ji" +
                                                        "mple/spark/solver/BDDOnFlyCallGraph.jedd:83,38"),
                                                       contexts).iterator();
                 contextIt.hasNext();
                 ) {
                final Object context = (Object) contextIt.next();
                mpag.addToPAG(context);
            }
        }
    }
    
    private void processCallEdges() {
        Stmt s = null;
        while (true) {
            final jedd.internal.RelationContainer e =
              new jedd.internal.RelationContainer(new Attribute[] { srcm.v(), srcc.v(), stmt.v(), kind.v(), tgtm.v(), tgtc.v() },
                                                  new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), V2.v(), T2.v() },
                                                  ("<soot.jimple.spark.bdddomains.srcm:soot.jimple.spark.bdddoma" +
                                                   "ins.V1, soot.jimple.spark.bdddomains.srcc:soot.jimple.spark." +
                                                   "bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimple" +
                                                   ".spark.bdddomains.ST, soot.jimple.spark.bdddomains.kind:soot" +
                                                   ".jimple.spark.bdddomains.H2, soot.jimple.spark.bdddomains.tg" +
                                                   "tm:soot.jimple.spark.bdddomains.V2, soot.jimple.spark.bdddom" +
                                                   "ains.tgtc:soot.jimple.spark.bdddomains.T2> e = callEdges.nex" +
                                                   "t(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/solv" +
                                                   "er/BDDOnFlyCallGraph.jedd:92,12"),
                                                  callEdges.next());
            if (jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(e), jedd.internal.Jedd.v().falseBDD())) break;
            final jedd.internal.RelationContainer mc =
              new jedd.internal.RelationContainer(new Attribute[] { method.v(), ctxt.v() },
                                                  new PhysicalDomain[] { V2.v(), T2.v() },
                                                  ("<soot.jimple.spark.bdddomains.method:soot.jimple.spark.bdddo" +
                                                   "mains.V2, soot.jimple.spark.bdddomains.ctxt:soot.jimple.spar" +
                                                   "k.bdddomains.T2> mc = jedd.internal.Jedd.v().project(e, new " +
                                                   "jedd.PhysicalDomain[...]); at /home/olhotak/soot-2-jedd/src/" +
                                                   "soot/jimple/spark/solver/BDDOnFlyCallGraph.jedd:94,12"),
                                                  jedd.internal.Jedd.v().project(e,
                                                                                 new PhysicalDomain[] { T1.v(), ST.v(), V1.v(), H2.v() }));
            this.addToPAG(new jedd.internal.RelationContainer(new Attribute[] { ctxt.v(), method.v() },
                                                              new PhysicalDomain[] { T2.v(), V2.v() },
                                                              ("this.addToPAG(mc) at /home/olhotak/soot-2-jedd/src/soot/jimp" +
                                                               "le/spark/solver/BDDOnFlyCallGraph.jedd:96,12"),
                                                              mc));
            Iterator it =
              new jedd.internal.RelationContainer(new Attribute[] { tgtm.v(), srcc.v(), tgtc.v(), stmt.v(), srcm.v(), kind.v() },
                                                  new PhysicalDomain[] { V2.v(), T1.v(), T2.v(), ST.v(), V1.v(), H2.v() },
                                                  ("e.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-2-" +
                                                   "jedd/src/soot/jimple/spark/solver/BDDOnFlyCallGraph.jedd:97," +
                                                   "26"),
                                                  e).iterator(new Attribute[] { srcm.v(), srcc.v(), stmt.v(), kind.v(), tgtm.v(), tgtc.v() });
            while (it.hasNext()) {
                Object[] edge = (Object[]) it.next();
                pag.addCallTarget(new Edge(MethodContext.v((SootMethod) edge[0], edge[1]),
                                           (Stmt) edge[2],
                                           MethodContext.v((SootMethod) edge[4], edge[5]),
                                           ((Integer) edge[3]).intValue()));
            }
        }
    }
    
    public BDDOnFlyCallGraphBuilder ofcgb() { return ofcgb; }
    
    public void updatedNodes(final jedd.internal.RelationContainer types) {
        ofcgb.addTypes(new jedd.internal.RelationContainer(new Attribute[] { type.v(), var.v() },
                                                           new PhysicalDomain[] { T2.v(), V3.v() },
                                                           ("ofcgb.addTypes(types) at /home/olhotak/soot-2-jedd/src/soot/" +
                                                            "jimple/spark/solver/BDDOnFlyCallGraph.jedd:113,8"),
                                                           types));
    }
    
    public void mergedWith(Node n1, Node n2) {  }
    
    private BDDPAG pag;
}
