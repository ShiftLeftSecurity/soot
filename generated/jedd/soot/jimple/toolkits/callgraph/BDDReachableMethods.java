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
    
    private final jedd.internal.RelationContainer set =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), ctxt.v() },
                                          new jedd.PhysicalDomain[] { V2.v(), T1.v() },
                                          ("private <soot.jimple.spark.bdddomains.method, soot.jimple.sp" +
                                           "ark.bdddomains.ctxt> set = jedd.internal.Jedd.v().falseBDD()" +
                                           " at /home/olhotak/soot-2-jedd/src/soot/jimple/toolkits/callg" +
                                           "raph/BDDReachableMethods.jedd:38,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private BDDReader unprocessedMethods;
    
    private BDDReader allReachables = reachables.reader();
    
    public BDDReachableMethods(BDDCallGraph graph, Iterator entryPoints) {
        super();
        this.cg = graph;
        this.addMethods(entryPoints);
        unprocessedMethods = reachables.reader();
        this.edgeSource = graph.newListener();
    }
    
    public BDDReachableMethods(BDDCallGraph graph, Collection entryPoints) { this(graph, entryPoints.iterator()); }
    
    private void addMethods(Iterator methods) {
        while (methods.hasNext()) this.addMethod((MethodOrMethodContext) methods.next());
    }
    
    private jedd.internal.RelationContainer toBDD(MethodOrMethodContext m) {
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), ctxt.v() },
                                                   new jedd.PhysicalDomain[] { V2.v(), T1.v() },
                                                   ("return jedd.internal.Jedd.v().literal(new java.lang.Object[." +
                                                    "..], new jedd.Attribute[...], new jedd.PhysicalDomain[...]);" +
                                                    " at /home/olhotak/soot-2-jedd/src/soot/jimple/toolkits/callg" +
                                                    "raph/BDDReachableMethods.jedd:55,8"),
                                                   jedd.internal.Jedd.v().literal(new Object[] { m.method(), m.context() },
                                                                                  new jedd.Attribute[] { method.v(), ctxt.v() },
                                                                                  new jedd.PhysicalDomain[] { V2.v(), T1.v() }));
    }
    
    private void addMethod(MethodOrMethodContext m) {
        final jedd.internal.RelationContainer bdd =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), ctxt.v() },
                                              new jedd.PhysicalDomain[] { V2.v(), T1.v() },
                                              ("<soot.jimple.spark.bdddomains.method:soot.jimple.spark.bdddo" +
                                               "mains.V2, soot.jimple.spark.bdddomains.ctxt:soot.jimple.spar" +
                                               "k.bdddomains.T1> bdd = this.toBDD(m); at /home/olhotak/soot-" +
                                               "2-jedd/src/soot/jimple/toolkits/callgraph/BDDReachableMethod" +
                                               "s.jedd:58,8"),
                                              this.toBDD(m));
        this.addMethod(new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), ctxt.v() },
                                                           new jedd.PhysicalDomain[] { V2.v(), T1.v() },
                                                           ("this.addMethod(bdd) at /home/olhotak/soot-2-jedd/src/soot/ji" +
                                                            "mple/toolkits/callgraph/BDDReachableMethods.jedd:59,8"),
                                                           bdd));
    }
    
    private void addMethod(final jedd.internal.RelationContainer m) {
        final jedd.internal.RelationContainer addToReachables =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), ctxt.v() },
                                              new jedd.PhysicalDomain[] { V2.v(), T1.v() },
                                              ("<soot.jimple.spark.bdddomains.method:soot.jimple.spark.bdddo" +
                                               "mains.V2, soot.jimple.spark.bdddomains.ctxt:soot.jimple.spar" +
                                               "k.bdddomains.T1> addToReachables = jedd.internal.Jedd.v().mi" +
                                               "nus(jedd.internal.Jedd.v().read(m), set); at /home/olhotak/s" +
                                               "oot-2-jedd/src/soot/jimple/toolkits/callgraph/BDDReachableMe" +
                                               "thods.jedd:62,8"),
                                              jedd.internal.Jedd.v().minus(jedd.internal.Jedd.v().read(m), set));
        set.eqUnion(addToReachables);
        reachables.add(new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), ctxt.v() },
                                                           new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                                           ("reachables.add(jedd.internal.Jedd.v().replace(addToReachable" +
                                                            "s, new jedd.PhysicalDomain[...], new jedd.PhysicalDomain[..." +
                                                            "])) at /home/olhotak/soot-2-jedd/src/soot/jimple/toolkits/ca" +
                                                            "llgraph/BDDReachableMethods.jedd:64,8"),
                                                           jedd.internal.Jedd.v().replace(addToReachables,
                                                                                          new jedd.PhysicalDomain[] { V2.v() },
                                                                                          new jedd.PhysicalDomain[] { V1.v() })));
    }
    
    public jedd.internal.RelationContainer targets(final jedd.internal.RelationContainer edges) {
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { ctxt.v(), method.v() },
                                                   new jedd.PhysicalDomain[] { T2.v(), V2.v() },
                                                   ("return jedd.internal.Jedd.v().project(edges, new jedd.Physic" +
                                                    "alDomain[...]); at /home/olhotak/soot-2-jedd/src/soot/jimple" +
                                                    "/toolkits/callgraph/BDDReachableMethods.jedd:67,8"),
                                                   jedd.internal.Jedd.v().project(edges,
                                                                                  new jedd.PhysicalDomain[] { V1.v(), T1.v(), H2.v(), ST.v() }));
    }
    
    public void update() {
        while (true) {
            final jedd.internal.RelationContainer e =
              new jedd.internal.RelationContainer(new jedd.Attribute[] { srcm.v(), srcc.v(), stmt.v(), tgtm.v(), tgtc.v(), kind.v() },
                                                  new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), V2.v(), T2.v(), H2.v() },
                                                  ("<soot.jimple.spark.bdddomains.srcm:soot.jimple.spark.bdddoma" +
                                                   "ins.V1, soot.jimple.spark.bdddomains.srcc:soot.jimple.spark." +
                                                   "bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimple" +
                                                   ".spark.bdddomains.ST, soot.jimple.spark.bdddomains.tgtm:soot" +
                                                   ".jimple.spark.bdddomains.V2, soot.jimple.spark.bdddomains.tg" +
                                                   "tc:soot.jimple.spark.bdddomains.T2, soot.jimple.spark.bdddom" +
                                                   "ains.kind:soot.jimple.spark.bdddomains.H2> e = edgeSource.ne" +
                                                   "xt(); at /home/olhotak/soot-2-jedd/src/soot/jimple/toolkits/" +
                                                   "callgraph/BDDReachableMethods.jedd:73,12"),
                                                  edgeSource.next());
            if (jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(e), jedd.internal.Jedd.v().falseBDD())) break;
            e.eqIntersect(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(e),
                                                      jedd.internal.Jedd.v().replace(set,
                                                                                     new jedd.PhysicalDomain[] { V2.v() },
                                                                                     new jedd.PhysicalDomain[] { V1.v() }),
                                                      new jedd.PhysicalDomain[] { V1.v(), T1.v() }));
            this.addMethod(new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), ctxt.v() },
                                                               new jedd.PhysicalDomain[] { V2.v(), T1.v() },
                                                               ("this.addMethod(jedd.internal.Jedd.v().replace(this.targets(n" +
                                                                "ew jedd.internal.RelationContainer(...)), new jedd.PhysicalD" +
                                                                "omain[...], new jedd.PhysicalDomain[...])) at /home/olhotak/" +
                                                                "soot-2-jedd/src/soot/jimple/toolkits/callgraph/BDDReachableM" +
                                                                "ethods.jedd:76,12"),
                                                               jedd.internal.Jedd.v().replace(this.targets(new jedd.internal.RelationContainer(new jedd.Attribute[] { tgtc.v(), srcm.v(), srcc.v(), tgtm.v(), kind.v(), stmt.v() },
                                                                                                                                               new jedd.PhysicalDomain[] { T2.v(), V1.v(), T1.v(), V2.v(), H2.v(), ST.v() },
                                                                                                                                               ("this.targets(e) at /home/olhotak/soot-2-jedd/src/soot/jimple" +
                                                                                                                                                "/toolkits/callgraph/BDDReachableMethods.jedd:76,23"),
                                                                                                                                               e)),
                                                                                              new jedd.PhysicalDomain[] { T2.v() },
                                                                                              new jedd.PhysicalDomain[] { T1.v() })));
        }
        while (true) {
            final jedd.internal.RelationContainer m =
              new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), ctxt.v() },
                                                  new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                                  ("<soot.jimple.spark.bdddomains.method:soot.jimple.spark.bdddo" +
                                                   "mains.V1, soot.jimple.spark.bdddomains.ctxt:soot.jimple.spar" +
                                                   "k.bdddomains.T1> m = unprocessedMethods.next(); at /home/olh" +
                                                   "otak/soot-2-jedd/src/soot/jimple/toolkits/callgraph/BDDReach" +
                                                   "ableMethods.jedd:79,12"),
                                                  unprocessedMethods.next());
            if (jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(m), jedd.internal.Jedd.v().falseBDD())) break;
            final jedd.internal.RelationContainer e =
              new jedd.internal.RelationContainer(new jedd.Attribute[] { srcm.v(), srcc.v(), stmt.v(), tgtm.v(), tgtc.v(), kind.v() },
                                                  new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), V2.v(), T2.v(), H2.v() },
                                                  ("<soot.jimple.spark.bdddomains.srcm:soot.jimple.spark.bdddoma" +
                                                   "ins.V1, soot.jimple.spark.bdddomains.srcc:soot.jimple.spark." +
                                                   "bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimple" +
                                                   ".spark.bdddomains.ST, soot.jimple.spark.bdddomains.tgtm:soot" +
                                                   ".jimple.spark.bdddomains.V2, soot.jimple.spark.bdddomains.tg" +
                                                   "tc:soot.jimple.spark.bdddomains.T2, soot.jimple.spark.bdddom" +
                                                   "ains.kind:soot.jimple.spark.bdddomains.H2> e = cg.edgesOutOf" +
                                                   "(new jedd.internal.RelationContainer(...)); at /home/olhotak" +
                                                   "/soot-2-jedd/src/soot/jimple/toolkits/callgraph/BDDReachable" +
                                                   "Methods.jedd:81,12"),
                                                  cg.edgesOutOf(new jedd.internal.RelationContainer(new jedd.Attribute[] { srcm.v(), srcc.v() },
                                                                                                    new jedd.PhysicalDomain[] { V1.v(), T1.v() },
                                                                                                    ("cg.edgesOutOf(m) at /home/olhotak/soot-2-jedd/src/soot/jimpl" +
                                                                                                     "e/toolkits/callgraph/BDDReachableMethods.jedd:82,16"),
                                                                                                    m)));
            this.addMethod(new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), ctxt.v() },
                                                               new jedd.PhysicalDomain[] { V2.v(), T1.v() },
                                                               ("this.addMethod(jedd.internal.Jedd.v().replace(this.targets(n" +
                                                                "ew jedd.internal.RelationContainer(...)), new jedd.PhysicalD" +
                                                                "omain[...], new jedd.PhysicalDomain[...])) at /home/olhotak/" +
                                                                "soot-2-jedd/src/soot/jimple/toolkits/callgraph/BDDReachableM" +
                                                                "ethods.jedd:83,12"),
                                                               jedd.internal.Jedd.v().replace(this.targets(new jedd.internal.RelationContainer(new jedd.Attribute[] { tgtc.v(), srcm.v(), srcc.v(), tgtm.v(), kind.v(), stmt.v() },
                                                                                                                                               new jedd.PhysicalDomain[] { T2.v(), V1.v(), T1.v(), V2.v(), H2.v(), ST.v() },
                                                                                                                                               ("this.targets(e) at /home/olhotak/soot-2-jedd/src/soot/jimple" +
                                                                                                                                                "/toolkits/callgraph/BDDReachableMethods.jedd:83,23"),
                                                                                                                                               e)),
                                                                                              new jedd.PhysicalDomain[] { T2.v() },
                                                                                              new jedd.PhysicalDomain[] { T1.v() })));
        }
    }
    
    public BDDReader listener() { return (BDDReader) allReachables.clone(); }
    
    public BDDReader newListener() { return reachables.reader(); }
    
    public boolean contains(MethodOrMethodContext m) {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().intersect(jedd.internal.Jedd.v().read(set),
                                                                                                           this.toBDD(m))),
                                              jedd.internal.Jedd.v().falseBDD());
    }
    
    public int size() {
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { method.v(), ctxt.v() },
                                                   new jedd.PhysicalDomain[] { V2.v(), T1.v() },
                                                   ("set.size() at /home/olhotak/soot-2-jedd/src/soot/jimple/tool" +
                                                    "kits/callgraph/BDDReachableMethods.jedd:104,12"),
                                                   set).size();
    }
}
