package soot.jimple.toolkits.callgraph;

import soot.*;
import soot.util.*;
import soot.util.queue.*;
import soot.jimple.spark.bdddomains.*;
import java.util.*;
import jedd.*;

public final class BDDCallGraph {
    public final jedd.internal.RelationContainer edges =
      new jedd.internal.RelationContainer(new Attribute[] { srcm.v(), srcc.v(), stmt.v(), kind.v(), tgtm.v(), tgtc.v() },
                                          new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), V2.v(), T2.v() },
                                          ("public <soot.jimple.spark.bdddomains.srcm:soot.jimple.spark." +
                                           "bdddomains.V1, soot.jimple.spark.bdddomains.srcc:soot.jimple" +
                                           ".spark.bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot" +
                                           ".jimple.spark.bdddomains.ST, soot.jimple.spark.bdddomains.ki" +
                                           "nd:soot.jimple.spark.bdddomains.H2, soot.jimple.spark.bdddom" +
                                           "ains.tgtm:soot.jimple.spark.bdddomains.V2, soot.jimple.spark" +
                                           ".bdddomains.tgtc:soot.jimple.spark.bdddomains.T2> edges at /" +
                                           "home/olhotak/soot-2-jedd/src/soot/jimple/toolkits/callgraph/" +
                                           "BDDCallGraph.jedd:33,11"));
    
    public void addEdge(MethodOrMethodContext src, Unit s, MethodOrMethodContext tgt, int _kind) {
        unitNumberer.add(s);
        this.addEdges(new jedd.internal.RelationContainer(new Attribute[] { srcm.v(), srcc.v(), stmt.v(), kind.v(), tgtm.v(), tgtc.v() },
                                                          new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), V2.v(), T2.v() },
                                                          ("this.addEdges(jedd.internal.Jedd.v().literal(new java.lang.O" +
                                                           "bject[...], new jedd.Attribute[...], new jedd.PhysicalDomain" +
                                                           "[...])) at /home/olhotak/soot-2-jedd/src/soot/jimple/toolkit" +
                                                           "s/callgraph/BDDCallGraph.jedd:36,8"),
                                                          jedd.internal.Jedd.v().literal(new Object[] { src.method(), src.context(), s, KindNumberer.v().get(_kind), tgt.method(), tgt.context() },
                                                                                         new Attribute[] { srcm.v(), srcc.v(), stmt.v(), kind.v(), tgtm.v(), tgtc.v() },
                                                                                         new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), V2.v(), T2.v() })));
    }
    
    public void addEdges(final jedd.internal.RelationContainer edges) {
        this.edges.eqUnion(edges);
        queue.add(new jedd.internal.RelationContainer(new Attribute[] { tgtc.v(), srcm.v(), srcc.v(), tgtm.v(), kind.v(), stmt.v() },
                                                      new PhysicalDomain[] { T2.v(), V1.v(), T1.v(), V2.v(), H2.v(), ST.v() },
                                                      ("queue.add(edges) at /home/olhotak/soot-2-jedd/src/soot/jimpl" +
                                                       "e/toolkits/callgraph/BDDCallGraph.jedd:42,8"),
                                                      edges));
    }
    
    public jedd.internal.RelationContainer edgesOutOf(final jedd.internal.RelationContainer m) {
        return new jedd.internal.RelationContainer(new Attribute[] { tgtc.v(), srcm.v(), srcc.v(), tgtm.v(), kind.v(), stmt.v() },
                                                   new PhysicalDomain[] { T2.v(), V1.v(), T1.v(), V2.v(), H2.v(), ST.v() },
                                                   ("return jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().re" +
                                                    "ad(edges), m, new jedd.PhysicalDomain[...]); at /home/olhota" +
                                                    "k/soot-2-jedd/src/soot/jimple/toolkits/callgraph/BDDCallGrap" +
                                                    "h.jedd:45,8"),
                                                   jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(edges),
                                                                               m,
                                                                               new PhysicalDomain[] { V1.v(), T1.v() }));
    }
    
    public jedd.internal.RelationContainer edgesOutOf(SootMethod m) {
        return new jedd.internal.RelationContainer(new Attribute[] { tgtc.v(), srcm.v(), srcc.v(), tgtm.v(), kind.v(), stmt.v() },
                                                   new PhysicalDomain[] { T2.v(), V1.v(), T1.v(), V2.v(), H2.v(), ST.v() },
                                                   ("return jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().re" +
                                                    "ad(edges), jedd.internal.Jedd.v().literal(new java.lang.Obje" +
                                                    "ct[...], new jedd.Attribute[...], new jedd.PhysicalDomain[.." +
                                                    ".]), new jedd.PhysicalDomain[...]); at /home/olhotak/soot-2-" +
                                                    "jedd/src/soot/jimple/toolkits/callgraph/BDDCallGraph.jedd:48" +
                                                    ",8"),
                                                   jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(edges),
                                                                               jedd.internal.Jedd.v().literal(new Object[] { m },
                                                                                                              new Attribute[] { srcm.v() },
                                                                                                              new PhysicalDomain[] { V1.v() }),
                                                                               new PhysicalDomain[] { V1.v() }));
    }
    
    public BDDReader newListener() { return queue.reader(); }
    
    public BDDReader listener() { return (BDDReader) reader.clone(); }
    
    private BDDQueue queue = new BDDQueue();
    
    private BDDReader reader = queue.reader();
    
    private Numberer unitNumberer = Scene.v().getUnitNumberer();
    
    public BDDCallGraph() { super(); }
}
