package soot.jimple.toolkits.callgraph;

import soot.*;
import soot.options.*;
import soot.jimple.*;
import java.util.*;
import soot.util.*;
import soot.util.queue.*;
import soot.jimple.spark.bdddomains.*;

public interface BDDContextManager {
    void addStaticEdge(MethodOrMethodContext src, Unit srcUnit, SootMethod target, int kind);
    
    void addStaticEdges(Object sourceContext, final jedd.Relation edges);
    
    void addVirtualEdge(MethodOrMethodContext src, Unit srcUnit, SootMethod target, int kind, Object typeContext);
    
    void addVirtualEdges(final jedd.Relation edges);
    
    BDDCallGraph callGraph();
}
