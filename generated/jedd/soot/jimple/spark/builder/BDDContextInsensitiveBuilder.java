package soot.jimple.spark.builder;

import soot.jimple.spark.*;
import soot.jimple.spark.pag.*;
import soot.jimple.toolkits.callgraph.*;
import soot.jimple.toolkits.pointer.util.NativeMethodDriver;
import soot.jimple.toolkits.pointer.util.NativeHelper;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.*;
import java.util.*;
import soot.jimple.*;
import soot.jimple.spark.internal.*;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.jimple.spark.solver.BDDOnFlyCallGraph;
import soot.util.queue.*;
import soot.options.*;

public class BDDContextInsensitiveBuilder {
    public void preJimplify() {
        for (Iterator cIt = Scene.v().getClasses().iterator(); cIt.hasNext(); ) {
            final SootClass c = (SootClass) cIt.next();
            for (Iterator mIt = c.methodIterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                if (!m.isConcrete()) continue;
                if (m.isNative()) continue;
                if (m.isPhantom()) continue;
                m.retrieveActiveBody();
            }
        }
    }
    
    public AbstractPAG setup(BDDSparkOptions opts) {
        pag = new BDDPAG(opts);
        if (opts.simulate_natives()) { NativeHelper.register(new SparkNativeHelper(pag)); }
        if (opts.on_fly_cg() && !opts.vta()) {
            ofcg = new BDDOnFlyCallGraph(pag);
            pag.setOnFlyCallGraph(ofcg);
        } else {
            
        }
        return pag;
    }
    
    public void build() {
        BDDReader callEdges;
        if (ofcg != null) {
            callEdges = ofcg.callGraph().listener();
            ofcg.build();
        }
    }
    
    private BDDPAG pag;
    
    private CallGraphBuilder cgb;
    
    private BDDOnFlyCallGraph ofcg;
    
    private ReachableMethods reachables;
    
    int classes = 0;
    
    int totalMethods = 0;
    
    int analyzedMethods = 0;
    
    int stmts = 0;
    
    public BDDContextInsensitiveBuilder() { super(); }
}
