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
    
    public BDDReachableMethods reachableMethods() { return this.reachableMethods; }
    
    public BDDCallGraph callGraph() { return this.callGraph; }
    
    public BDDOnFlyCallGraph(BDDPAG pag) {
        super();
        this.pag = pag;
        this.callGraph = new BDDCallGraph();
        BDDContextManager cm = BDDCallGraphBuilder.makeContextManager(this.callGraph);
        this.reachableMethods = new BDDReachableMethods(this.callGraph, Scene.v().getEntryPoints());
        this.ofcgb = new BDDOnFlyCallGraphBuilder(pag, cm, this.reachableMethods);
        this.reachablesReader = this.reachableMethods.listener();
        this.callEdges = cm.callGraph().listener();
    }
    
    public void build() {
        this.ofcgb.processReachables();
        this.processReachables();
        this.processCallEdges();
    }
    
    private void processReachables() {
        this.reachableMethods.update();
        while (true) {
            final Relation methodContexts =
              new Relation(new Domain[] { method.v(), ctxt.v() },
                           new PhysicalDomain[] { V1.v(), T1.v() },
                           this.reachablesReader.next());
            if (Jedd.v().equals(Jedd.v().read(methodContexts), Jedd.v().falseBDD())) return;
            this.addToPAG(new Relation(new Domain[] { method.v(), ctxt.v() },
                                       new PhysicalDomain[] { V2.v(), T2.v() },
                                       Jedd.v().replace(methodContexts,
                                                        new PhysicalDomain[] { V1.v(), T1.v() },
                                                        new PhysicalDomain[] { V2.v(), T2.v() })));
        }
    }
    
    private void addToPAG(final Relation methodContexts) {
        final Relation methods =
          new Relation(new Domain[] { method.v() },
                       new PhysicalDomain[] { V2.v() },
                       Jedd.v().project(methodContexts, new PhysicalDomain[] { T2.v() }));
        for (Iterator methIt =
               new Relation(new Domain[] { method.v() }, new PhysicalDomain[] { V2.v() }, methods).iterator();
             methIt.hasNext();
             ) {
            final SootMethod meth = (SootMethod) methIt.next();
            AbstractMethodPAG mpag = AbstractMethodPAG.v(this.pag, meth);
            mpag.build();
            final Relation contexts =
              new Relation(new Domain[] { ctxt.v() },
                           new PhysicalDomain[] { T2.v() },
                           Jedd.v().compose(Jedd.v().read(methodContexts),
                                            Jedd.v().literal(new Object[] { meth },
                                                             new Domain[] { method.v() },
                                                             new PhysicalDomain[] { V2.v() }),
                                            new PhysicalDomain[] { V2.v() }));
            for (Iterator contextIt =
                   new Relation(new Domain[] { ctxt.v() }, new PhysicalDomain[] { T2.v() }, contexts).iterator();
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
            final Relation e =
              new Relation(new Domain[] { srcm.v(), srcc.v(), stmt.v(), kind.v(), tgtm.v(), tgtc.v() },
                           new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), V2.v(), T2.v() },
                           this.callEdges.next());
            if (Jedd.v().equals(Jedd.v().read(e), Jedd.v().falseBDD())) break;
            final Relation mc =
              new Relation(new Domain[] { method.v(), ctxt.v() },
                           new PhysicalDomain[] { V2.v(), T2.v() },
                           Jedd.v().project(e, new PhysicalDomain[] { T1.v(), ST.v(), V1.v(), H2.v() }));
            this.addToPAG(new Relation(new Domain[] { method.v(), ctxt.v() },
                                       new PhysicalDomain[] { V2.v(), T2.v() },
                                       mc));
            Iterator it =
              new Relation(new Domain[] { srcc.v(), tgtm.v(), stmt.v(), srcm.v(), kind.v(), tgtc.v() },
                           new PhysicalDomain[] { T1.v(), V2.v(), ST.v(), V1.v(), H2.v(), T2.v() },
                           e).iterator(new Domain[] { srcm.v(), srcc.v(), stmt.v(), kind.v(), tgtm.v(), tgtc.v() });
            while (it.hasNext()) {
                Object[] edge = (Object[]) it.next();
                this.pag.addCallTarget(new Edge(MethodContext.v((SootMethod) edge[0], edge[1]),
                                                (Stmt) edge[2],
                                                MethodContext.v((SootMethod) edge[4], edge[5]),
                                                ((Integer) edge[3]).intValue()));
            }
        }
    }
    
    public BDDOnFlyCallGraphBuilder ofcgb() { return this.ofcgb; }
    
    public void updatedNodes(final Relation types) {
        this.ofcgb.addTypes(new Relation(new Domain[] { var.v(), type.v() },
                                         new PhysicalDomain[] { V3.v(), T2.v() },
                                         Jedd.v().replace(types,
                                                          new PhysicalDomain[] { V1.v(), T1.v() },
                                                          new PhysicalDomain[] { V3.v(), T2.v() })));
    }
    
    public void mergedWith(Node n1, Node n2) {  }
    
    private BDDPAG pag;
}
