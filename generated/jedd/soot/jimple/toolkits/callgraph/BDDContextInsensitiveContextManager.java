package soot.jimple.toolkits.callgraph;

import soot.*;
import soot.options.*;
import soot.jimple.*;
import java.util.*;
import soot.util.*;
import soot.util.queue.*;
import soot.jimple.spark.bdddomains.*;

public class BDDContextInsensitiveContextManager implements BDDContextManager {
    private BDDCallGraph cg;
    
    public BDDCallGraph callGraph() { return cg; }
    
    public BDDContextInsensitiveContextManager(BDDCallGraph cg) {
        super();
        this.cg = cg;
    }
    
    public void addStaticEdge(MethodOrMethodContext src, Unit srcUnit, SootMethod target, int kind) {
        cg.addEdge(src.method(), srcUnit, target, kind);
    }
    
    public void addStaticEdges(Object sourceContext, final jedd.internal.RelationContainer edges) {
        cg.addEdges(new jedd.internal.RelationContainer(new jedd.Attribute[] { srcc.v(), tgtc.v(), tgtm.v(), stmt.v(), srcm.v(), kind.v() },
                                                        new jedd.PhysicalDomain[] { T1.v(), T2.v(), V2.v(), ST.v(), V1.v(), H2.v() },
                                                        ("cg.addEdges(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v" +
                                                         "().read(jedd.internal.Jedd.v().literal(new java.lang.Object[" +
                                                         "...], new jedd.Attribute[...], new jedd.PhysicalDomain[...])" +
                                                         "), edges, new jedd.PhysicalDomain[...])) at /home/olhotak/so" +
                                                         "ot-2-jedd/src/soot/jimple/toolkits/callgraph/BDDContextInsen" +
                                                         "sitiveContextManager.jedd:47,8"),
                                                        jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().literal(new Object[] { null, null },
                                                                                                                                               new jedd.Attribute[] { srcc.v(), tgtc.v() },
                                                                                                                                               new jedd.PhysicalDomain[] { T1.v(), T2.v() })),
                                                                                    edges,
                                                                                    new jedd.PhysicalDomain[] {  })));
    }
    
    public void addVirtualEdge(MethodOrMethodContext src,
                               Unit srcUnit,
                               SootMethod target,
                               int kind,
                               Object typeContext) {
        cg.addEdge(src.method(), srcUnit, target, kind);
    }
    
    public void addVirtualEdges(final jedd.internal.RelationContainer edges) {
        cg.addEdges(new jedd.internal.RelationContainer(new jedd.Attribute[] { srcc.v(), tgtc.v(), tgtm.v(), stmt.v(), srcm.v(), kind.v() },
                                                        new jedd.PhysicalDomain[] { T1.v(), T2.v(), V2.v(), ST.v(), V1.v(), H2.v() },
                                                        ("cg.addEdges(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v" +
                                                         "().read(jedd.internal.Jedd.v().literal(new java.lang.Object[" +
                                                         "...], new jedd.Attribute[...], new jedd.PhysicalDomain[...])" +
                                                         "), edges, new jedd.PhysicalDomain[...])) at /home/olhotak/so" +
                                                         "ot-2-jedd/src/soot/jimple/toolkits/callgraph/BDDContextInsen" +
                                                         "sitiveContextManager.jedd:55,8"),
                                                        jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().literal(new Object[] { null, null },
                                                                                                                                               new jedd.Attribute[] { srcc.v(), tgtc.v() },
                                                                                                                                               new jedd.PhysicalDomain[] { T1.v(), T2.v() })),
                                                                                    edges,
                                                                                    new jedd.PhysicalDomain[] {  })));
    }
}
