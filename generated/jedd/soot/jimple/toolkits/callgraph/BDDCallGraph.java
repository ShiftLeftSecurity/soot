package soot.jimple.toolkits.callgraph;

import soot.*;
import soot.util.*;
import soot.util.queue.*;
import soot.jimple.spark.bdddomains.*;
import java.util.*;
import jedd.*;

public final class BDDCallGraph {
    public final Relation edges =
      new Relation(new Domain[] { srcm.v(), srcc.v(), stmt.v(), kind.v(), tgtm.v(), tgtc.v() },
                   new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), V2.v(), T2.v() });
    
    public void addEdge(MethodOrMethodContext src, Unit s, MethodOrMethodContext tgt, int _kind) {
        this.unitNumberer.add(s);
        this.addEdges(new Relation(new Domain[] { srcm.v(), srcc.v(), stmt.v(), kind.v(), tgtm.v(), tgtc.v() },
                                   new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), V2.v(), T2.v() },
                                   Jedd.v().literal(new Object[] { src.method(), src.context(), s, KindNumberer.v().get(_kind), tgt.method(), tgt.context() },
                                                    new Domain[] { srcm.v(), srcc.v(), stmt.v(), kind.v(), tgtm.v(), tgtc.v() },
                                                    new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), V2.v(), T2.v() })));
    }
    
    public void addEdges(final Relation edges) {
        this.edges.eqUnion(edges);
        this.queue.add(new Relation(new Domain[] { srcc.v(), tgtm.v(), stmt.v(), srcm.v(), kind.v(), tgtc.v() },
                                    new PhysicalDomain[] { T1.v(), V2.v(), ST.v(), V1.v(), H2.v(), T2.v() },
                                    edges));
    }
    
    public Relation edgesOutOf(final Relation m) {
        return new Relation(new Domain[] { srcc.v(), stmt.v(), tgtm.v(), srcm.v(), kind.v(), tgtc.v() },
                            new PhysicalDomain[] { T1.v(), ST.v(), V2.v(), V1.v(), H2.v(), T2.v() },
                            Jedd.v().join(Jedd.v().read(this.edges), m, new PhysicalDomain[] { V1.v(), T1.v() }));
    }
    
    public Relation edgesOutOf(SootMethod m) {
        return new Relation(new Domain[] { srcc.v(), stmt.v(), tgtm.v(), srcm.v(), kind.v(), tgtc.v() },
                            new PhysicalDomain[] { T1.v(), ST.v(), V2.v(), V1.v(), H2.v(), T2.v() },
                            Jedd.v().join(Jedd.v().read(this.edges),
                                          Jedd.v().literal(new Object[] { m },
                                                           new Domain[] { srcm.v() },
                                                           new PhysicalDomain[] { V1.v() }),
                                          new PhysicalDomain[] { V1.v() }));
    }
    
    public BDDReader newListener() { return this.queue.reader(); }
    
    public BDDReader listener() { return (BDDReader) this.reader.clone(); }
    
    private BDDQueue queue = new BDDQueue();
    
    private BDDReader reader = this.queue.reader();
    
    private Numberer unitNumberer = Scene.v().getUnitNumberer();
    
    public BDDCallGraph() { super(); }
}
