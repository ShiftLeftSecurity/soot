package soot.jimple.toolkits.callgraph;

import soot.*;
import soot.util.*;
import java.util.*;
import soot.util.queue.*;
import soot.jimple.spark.bdddomains.*;

public class BDDReachableMethods {
    private BDDCallGraph cg;
    
    private List entryPoints = new ArrayList();
    
    private BDDReader edgeSource;
    
    private BDDQueue reachables = new BDDQueue();
    
    private final jedd.Relation set =
      new jedd.Relation(new jedd.Attribute[] { method.v(), ctxt.v() },
                        new jedd.PhysicalDomain[] { V2.v(), T2.v() },
                        jedd.Jedd.v().falseBDD());
    
    private BDDReader unprocessedMethods;
    
    private BDDReader allReachables = this.reachables.reader();
    
    public BDDReachableMethods(BDDCallGraph graph, Iterator entryPoints) {
        super();
        this.cg = graph;
        this.addMethods(entryPoints);
        this.unprocessedMethods = this.reachables.reader();
        this.edgeSource = graph.newListener();
    }
    
    public BDDReachableMethods(BDDCallGraph graph, Collection entryPoints) { this(graph, entryPoints.iterator()); }
    
    private void addMethods(Iterator methods) {
        while (methods.hasNext()) this.addMethod((MethodOrMethodContext) methods.next());
    }
    
    private jedd.Relation toBDD(MethodOrMethodContext m) {
        return new jedd.Relation(new jedd.Attribute[] { method.v(), ctxt.v() },
                                 new jedd.PhysicalDomain[] { V2.v(), T2.v() },
                                 jedd.Jedd.v().literal(new Object[] { m.method(), m.context() },
                                                       new jedd.Attribute[] { method.v(), ctxt.v() },
                                                       new jedd.PhysicalDomain[] { V2.v(), T2.v() }));
    }
    
    private void addMethod(MethodOrMethodContext m) {
        final jedd.Relation bdd =
          new jedd.Relation(new jedd.Attribute[] { method.v(), ctxt.v() },
                            new jedd.PhysicalDomain[] { V2.v(), T2.v() },
                            this.toBDD(m));
        this.addMethod(new jedd.Relation(new jedd.Attribute[] { method.v(), ctxt.v() },
                                         new jedd.PhysicalDomain[] { V2.v(), T2.v() },
                                         bdd));
    }
    
    private void addMethod(final jedd.Relation m) {
        final jedd.Relation addToReachables =
          new jedd.Relation(new jedd.Attribute[] { method.v(), ctxt.v() },
                            new jedd.PhysicalDomain[] { V1.v(), T2.v() },
                            jedd.Jedd.v().replace(jedd.Jedd.v().minus(jedd.Jedd.v().read(m), this.set),
                                                  new jedd.PhysicalDomain[] { V2.v() },
                                                  new jedd.PhysicalDomain[] { V1.v() }));
        this.set.eqUnion(jedd.Jedd.v().replace(addToReachables,
                                               new jedd.PhysicalDomain[] { V1.v() },
                                               new jedd.PhysicalDomain[] { V2.v() }));
        this.reachables.add(new jedd.Relation(new jedd.Attribute[] { method.v(), ctxt.v() },
                                              new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                              jedd.Jedd.v().replace(addToReachables,
                                                                    new jedd.PhysicalDomain[] { T2.v() },
                                                                    new jedd.PhysicalDomain[] { T1.v() })));
    }
    
    public jedd.Relation targets(final jedd.Relation edges) {
        return new jedd.Relation(new jedd.Attribute[] { method.v(), ctxt.v() },
                                 new jedd.PhysicalDomain[] { V2.v(), T2.v() },
                                 jedd.Jedd.v().project(edges,
                                                       new jedd.PhysicalDomain[] { T1.v(), ST.v(), V1.v(), H2.v() }));
    }
    
    public void update() {
        while (true) {
            final jedd.Relation e =
              new jedd.Relation(new jedd.Attribute[] { srcm.v(), srcc.v(), stmt.v(), tgtm.v(), tgtc.v(), kind.v() },
                                new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), V2.v(), T2.v(), H2.v() },
                                this.edgeSource.next());
            if (jedd.Jedd.v().equals(jedd.Jedd.v().read(e), jedd.Jedd.v().falseBDD())) break;
            e.eqIntersect(jedd.Jedd.v().replace(this.set,
                                                new jedd.PhysicalDomain[] { V2.v(), T2.v() },
                                                new jedd.PhysicalDomain[] { V1.v(), T1.v() }));
            this.addMethod(new jedd.Relation(new jedd.Attribute[] { method.v(), ctxt.v() },
                                             new jedd.PhysicalDomain[] { V2.v(), T2.v() },
                                             this.targets(new jedd.Relation(new jedd.Attribute[] { srcc.v(), tgtm.v(), stmt.v(), srcm.v(), kind.v(), tgtc.v() },
                                                                            new jedd.PhysicalDomain[] { T1.v(), V2.v(), ST.v(), V1.v(), H2.v(), T2.v() },
                                                                            e))));
        }
        while (true) {
            final jedd.Relation m =
              new jedd.Relation(new jedd.Attribute[] { method.v(), ctxt.v() },
                                new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                this.unprocessedMethods.next());
            if (jedd.Jedd.v().equals(jedd.Jedd.v().read(m), jedd.Jedd.v().falseBDD())) break;
            final jedd.Relation e =
              new jedd.Relation(new jedd.Attribute[] { srcm.v(), srcc.v(), stmt.v(), tgtm.v(), tgtc.v(), kind.v() },
                                new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), V2.v(), T2.v(), H2.v() },
                                this.cg.edgesOutOf(new jedd.Relation(new jedd.Attribute[] { srcm.v(), srcc.v() },
                                                                     new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                                                     m)));
            this.addMethod(new jedd.Relation(new jedd.Attribute[] { method.v(), ctxt.v() },
                                             new jedd.PhysicalDomain[] { V2.v(), T2.v() },
                                             this.targets(new jedd.Relation(new jedd.Attribute[] { srcc.v(), tgtm.v(), stmt.v(), srcm.v(), kind.v(), tgtc.v() },
                                                                            new jedd.PhysicalDomain[] { T1.v(), V2.v(), ST.v(), V1.v(), H2.v(), T2.v() },
                                                                            e))));
        }
    }
    
    public BDDReader listener() { return (BDDReader) this.allReachables.clone(); }
    
    public BDDReader newListener() { return this.reachables.reader(); }
    
    public boolean contains(MethodOrMethodContext m) {
        return !jedd.Jedd.v().equals(jedd.Jedd.v().read(jedd.Jedd.v().intersect(jedd.Jedd.v().read(this.set),
                                                                                this.toBDD(m))),
                                     jedd.Jedd.v().falseBDD());
    }
    
    public int size() {
        return new jedd.Relation(new jedd.Attribute[] { method.v(), ctxt.v() },
                                 new jedd.PhysicalDomain[] { V2.v(), T2.v() },
                                 this.set).size();
    }
}
