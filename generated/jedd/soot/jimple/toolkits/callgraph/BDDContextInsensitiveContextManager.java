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
    
    public BDDCallGraph callGraph() { return this.cg; }
    
    public BDDContextInsensitiveContextManager(BDDCallGraph cg) {
        super();
        this.cg = cg;
    }
    
    public void addStaticEdge(MethodOrMethodContext src, Unit srcUnit, SootMethod target, int kind) {
        this.cg.addEdge(src.method(), srcUnit, target, kind);
    }
    
    public void addStaticEdges(Object sourceContext, final jedd.Relation edges) {
        this.cg.addEdges(new jedd.Relation(new jedd.Attribute[] { srcc.v(), tgtc.v(), stmt.v(), tgtm.v(), srcm.v(), kind.v() },
                                           new jedd.PhysicalDomain[] { T1.v(), T2.v(), ST.v(), V2.v(), V1.v(), H2.v() },
                                           jedd.Jedd.v().join(jedd.Jedd.v().read(jedd.Jedd.v().literal(new Object[] { null, null },
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
        this.cg.addEdge(src.method(), srcUnit, target, kind);
    }
    
    public void addVirtualEdges(final jedd.Relation edges) {
        this.cg.addEdges(new jedd.Relation(new jedd.Attribute[] { srcc.v(), tgtc.v(), stmt.v(), tgtm.v(), srcm.v(), kind.v() },
                                           new jedd.PhysicalDomain[] { T1.v(), T2.v(), ST.v(), V2.v(), V1.v(), H2.v() },
                                           jedd.Jedd.v().join(jedd.Jedd.v().read(jedd.Jedd.v().literal(new Object[] { null, null },
                                                                                                       new jedd.Attribute[] { srcc.v(), tgtc.v() },
                                                                                                       new jedd.PhysicalDomain[] { T1.v(), T2.v() })),
                                                              edges,
                                                              new jedd.PhysicalDomain[] {  })));
    }
}
