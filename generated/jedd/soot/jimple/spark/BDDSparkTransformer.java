package soot.jimple.spark;

import soot.*;
import soot.jimple.spark.builder.*;
import soot.jimple.spark.pag.*;
import soot.jimple.spark.solver.*;
import soot.jimple.spark.sets.*;
import soot.jimple.toolkits.callgraph.*;
import soot.jimple.*;
import java.util.*;
import soot.util.*;
import soot.options.BDDSparkOptions;
import soot.tagkit.*;
import soot.relations.*;

public class BDDSparkTransformer extends AbstractSparkTransformer {
    public BDDSparkTransformer(soot.Singletons.Global g) { super(); }
    
    public static BDDSparkTransformer v() { return G.v().BDDSparkTransformer(); }
    
    protected void internalTransform(String phaseName, Map options) {
        BDDSparkOptions opts = new BDDSparkOptions(options);
        ContextInsensitiveBuilder b = new ContextInsensitiveBuilder();
        if (opts.pre_jimplify()) b.preJimplify();
        if (opts.force_gc()) AbstractSparkTransformer.doGC();
        Date startBuild = new Date();
        final BDDPAG pag = (BDDPAG) b.setup(opts);
        b.build();
        Date endBuild = new Date();
        AbstractSparkTransformer.reportTime("Pointer Assignment Graph", startBuild, endBuild);
        if (opts.force_gc()) AbstractSparkTransformer.doGC();
        Date startTM = new Date();
        pag.getTypeManager().makeTypeMask();
        Date endTM = new Date();
        AbstractSparkTransformer.reportTime("Type masks", startTM, endTM);
        if (opts.force_gc()) AbstractSparkTransformer.doGC();
        if (opts.verbose()) {
            G.v().out.println("VarNodes: " + pag.getVarNodeNumberer().size());
            G.v().out.println("FieldRefNodes: " + pag.getFieldRefNodeNumberer().size());
            G.v().out.println("AllocNodes: " + pag.getAllocNodeNumberer().size());
        }
        Date startProp = new Date();
        BDDPropagator propagator = new BDDPropagator(pag);
        propagator.propagate();
        Date endProp = new Date();
        AbstractSparkTransformer.reportTime("Propagation", startProp, endProp);
        if (opts.force_gc()) AbstractSparkTransformer.doGC();
        if (!opts.on_fly_cg() || opts.vta()) {
            CallGraphBuilder cgb = new CallGraphBuilder(pag);
            cgb.build();
        }
        if (opts.verbose()) {
            G.v().out.println("[Spark] Number of reachable methods: " + Scene.v().getReachableMethods().size());
        }
        if (opts.set_mass()) this.findSetMass(pag, b);
        Scene.v().setPointsToAnalysis(pag);
        if (opts.add_tags()) { this.addTags(pag); }
        if (opts.verbose()) { JBuddyProfiler.v().printInfo(); }
    }
    
    private void addTags(BDDPAG pag) { throw new RuntimeException("NYI"); }
    
    private void findSetMass(BDDPAG pag, ContextInsensitiveBuilder b) { throw new RuntimeException("NYI"); }
}
