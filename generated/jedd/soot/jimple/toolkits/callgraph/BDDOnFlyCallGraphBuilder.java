package soot.jimple.toolkits.callgraph;

import soot.*;
import soot.options.*;
import soot.jimple.*;
import java.util.*;
import soot.util.*;
import soot.util.queue.*;
import soot.jimple.spark.bdddomains.*;
import jedd.*;
import soot.jimple.spark.pag.*;

public final class BDDOnFlyCallGraphBuilder extends AbstractOnFlyCallGraphBuilder {
    private BDDCallGraph cicg = new BDDCallGraph();
    
    private BDDContextInsensitiveContextManager cm;
    
    private ChunkedQueue targetsQueue = new ChunkedQueue();
    
    private QueueReader targets = targetsQueue.reader();
    
    private BDDReachableMethods rm;
    
    private BDDReader worklist;
    
    private BDDVirtualCalls virtualCalls;
    
    private BDDPAG pag;
    
    public BDDOnFlyCallGraphBuilder(BDDPAG pag, BDDContextManager cm, BDDReachableMethods rm) {
        this(pag, cm, rm, false);
    }
    
    public BDDOnFlyCallGraphBuilder(BDDPAG pag, BDDContextManager cm, BDDReachableMethods rm, boolean appOnly) {
        super(appOnly);
        this.pag = pag;
        this.cm = (BDDContextInsensitiveContextManager) cm;
        this.rm = rm;
        worklist = rm.listener();
        virtualCalls = new BDDVirtualCalls(Scene.v().getOrMakeBDDHierarchy());
    }
    
    private ChunkedQueue reachablesQueue = new ChunkedQueue();
    
    private QueueReader reachablesListener = reachablesQueue.reader();
    
    protected Iterator newReachables() { return reachablesListener; }
    
    protected void updateReachables() {
        rm.update();
        while (worklist.hasNext()) {
            final jedd.internal.RelationContainer methodContext =
              new jedd.internal.RelationContainer(new Attribute[] { method.v(), ctxt.v() },
                                                  new PhysicalDomain[] { V1.v(), T1.v() },
                                                  ("<soot.jimple.spark.bdddomains.method:soot.jimple.spark.bdddo" +
                                                   "mains.V1, soot.jimple.spark.bdddomains.ctxt:soot.jimple.spar" +
                                                   "k.bdddomains.T1> methodContext = worklist.next(); at /home/o" +
                                                   "lhotak/soot-2-jedd/src/soot/jimple/toolkits/callgraph/BDDOnF" +
                                                   "lyCallGraphBuilder.jedd:73,12"),
                                                  worklist.next());
            Iterator it =
              new jedd.internal.RelationContainer(new Attribute[] { method.v(), ctxt.v() },
                                                  new PhysicalDomain[] { V1.v(), T1.v() },
                                                  ("methodContext.iterator(new jedd.Attribute[...]) at /home/olh" +
                                                   "otak/soot-2-jedd/src/soot/jimple/toolkits/callgraph/BDDOnFly" +
                                                   "CallGraphBuilder.jedd:74,26"),
                                                  methodContext).iterator(new Attribute[] { method.v(), ctxt.v() });
            while (it.hasNext()) {
                Object[] pair = (Object[]) it.next();
                reachablesQueue.add(MethodContext.v((SootMethod) pair[0], pair[1]));
            }
        }
    }
    
    public void addTypes(final jedd.internal.RelationContainer types) {
        final jedd.internal.RelationContainer signatures =
          new jedd.internal.RelationContainer(new Attribute[] { type.v(), stmt.v(), method.v(), signature.v(), kind.v() },
                                              new PhysicalDomain[] { T2.v(), ST.v(), V1.v(), H1.v(), H2.v() },
                                              ("<soot.jimple.spark.bdddomains.type:soot.jimple.spark.bdddoma" +
                                               "ins.T2, soot.jimple.spark.bdddomains.stmt:soot.jimple.spark." +
                                               "bdddomains.ST, soot.jimple.spark.bdddomains.method:soot.jimp" +
                                               "le.spark.bdddomains.V1, soot.jimple.spark.bdddomains.signatu" +
                                               "re:soot.jimple.spark.bdddomains.H1, soot.jimple.spark.bdddom" +
                                               "ains.kind:soot.jimple.spark.bdddomains.H2> signatures = jedd" +
                                               ".internal.Jedd.v().compose(jedd.internal.Jedd.v().read(virtu" +
                                               "alCallSites), jedd.internal.Jedd.v().replace(types, new jedd" +
                                               ".PhysicalDomain[...], new jedd.PhysicalDomain[...]), new jed" +
                                               "d.PhysicalDomain[...]); at /home/olhotak/soot-2-jedd/src/soo" +
                                               "t/jimple/toolkits/callgraph/BDDOnFlyCallGraphBuilder.jedd:82" +
                                               ",8"),
                                              jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(virtualCallSites),
                                                                             jedd.internal.Jedd.v().replace(types,
                                                                                                            new PhysicalDomain[] { V1.v() },
                                                                                                            new PhysicalDomain[] { V3.v() }),
                                                                             new PhysicalDomain[] { V3.v() }));
        virtualCalls.addTypes(new jedd.internal.RelationContainer(new Attribute[] { signature.v(), type.v() },
                                                                  new PhysicalDomain[] { H1.v(), T1.v() },
                                                                  ("virtualCalls.addTypes(jedd.internal.Jedd.v().replace(jedd.in" +
                                                                   "ternal.Jedd.v().project(signatures, new jedd.PhysicalDomain[" +
                                                                   "...]), new jedd.PhysicalDomain[...], new jedd.PhysicalDomain" +
                                                                   "[...])) at /home/olhotak/soot-2-jedd/src/soot/jimple/toolkit" +
                                                                   "s/callgraph/BDDOnFlyCallGraphBuilder.jedd:84,8"),
                                                                  jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().project(signatures,
                                                                                                                                new PhysicalDomain[] { V1.v(), H2.v(), ST.v() }),
                                                                                                 new PhysicalDomain[] { T2.v() },
                                                                                                 new PhysicalDomain[] { T1.v() })));
        final jedd.internal.RelationContainer edges =
          new jedd.internal.RelationContainer(new Attribute[] { srcm.v(), stmt.v(), kind.v(), tgtm.v() },
                                              new PhysicalDomain[] { V1.v(), ST.v(), H2.v(), V2.v() },
                                              ("<soot.jimple.spark.bdddomains.srcm:soot.jimple.spark.bdddoma" +
                                               "ins.V1, soot.jimple.spark.bdddomains.stmt:soot.jimple.spark." +
                                               "bdddomains.ST, soot.jimple.spark.bdddomains.kind:soot.jimple" +
                                               ".spark.bdddomains.H2, soot.jimple.spark.bdddomains.tgtm:soot" +
                                               ".jimple.spark.bdddomains.V2> edges = jedd.internal.Jedd.v()." +
                                               "compose(jedd.internal.Jedd.v().read(signatures), jedd.intern" +
                                               "al.Jedd.v().replace(virtualCalls.answer(), new jedd.Physical" +
                                               "Domain[...], new jedd.PhysicalDomain[...]), new jedd.Physica" +
                                               "lDomain[...]); at /home/olhotak/soot-2-jedd/src/soot/jimple/" +
                                               "toolkits/callgraph/BDDOnFlyCallGraphBuilder.jedd:85,8"),
                                              jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(signatures),
                                                                             jedd.internal.Jedd.v().replace(virtualCalls.answer(),
                                                                                                            new PhysicalDomain[] { T1.v() },
                                                                                                            new PhysicalDomain[] { T2.v() }),
                                                                             new PhysicalDomain[] { T2.v(), H1.v() }));
        cm.addVirtualEdges(new jedd.internal.RelationContainer(new Attribute[] { srcm.v(), tgtm.v(), kind.v(), stmt.v() },
                                                               new PhysicalDomain[] { V1.v(), V2.v(), H2.v(), ST.v() },
                                                               ("cm.addVirtualEdges(edges) at /home/olhotak/soot-2-jedd/src/s" +
                                                                "oot/jimple/toolkits/callgraph/BDDOnFlyCallGraphBuilder.jedd:" +
                                                                "88,8"),
                                                               edges));
    }
    
    protected void addVirtualCallSite(Stmt s,
                                      SootMethod m,
                                      Local receiver,
                                      InstanceInvokeExpr iie,
                                      NumberedString subSig,
                                      int _kind) {
        Scene.v().getUnitNumberer().add(s);
        LocalVarNode rvn = pag.makeLocalVarNode(receiver, receiver.getType(), m);
        virtualCallSites.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { rvn, s, m, subSig, KindNumberer.v().get(_kind) },
                                                                new Attribute[] { var.v(), stmt.v(), method.v(), signature.v(), kind.v() },
                                                                new PhysicalDomain[] { V3.v(), ST.v(), V1.v(), H1.v(), H2.v() }));
    }
    
    public void addType(Local receiver, Object srcContext, Type type, Object typeContext) {
        throw new RuntimeException("shouldn\'t be called");
    }
    
    public void addStringConstant(Local l, Object srcContext, String constant) {
        for (Iterator siteIt = ((Collection) stringConstToSites.get(l)).iterator(); siteIt.hasNext(); ) {
            final VirtualCallSite site = (VirtualCallSite) siteIt.next();
            if (constant == null) {
                if (options.verbose()) {
                    G.v().out.println("Warning: Method " + site.container() +
                                      " is reachable, and calls Class.forName on a" +
                                      " non-constant String; graph will be incomplete!" +
                                      " Use safe-forname option for a conservative result.");
                }
            } else {
                if (constant.charAt(0) == '[') {
                    if (constant.length() > 1 && constant.charAt(1) == 'L' &&
                          constant.charAt(constant.length() - 1) == ';') {
                        constant = constant.substring(2, constant.length() - 1);
                    } else
                        continue;
                }
                if (!Scene.v().containsClass(constant)) {
                    if (options.verbose()) {
                        G.v().out.println("Warning: Class " + constant + " is" +
                                          " a dynamic class, and you did not specify" +
                                          " it as such; graph will be incomplete!");
                    }
                } else {
                    SootClass sootcls = Scene.v().getSootClass(constant);
                    if (!sootcls.isApplicationClass()) { sootcls.setLibraryClass(); }
                    if (sootcls.declaresMethod(sigClinit)) {
                        cm.addStaticEdge(MethodContext.v(site.container(), srcContext),
                                         site.stmt(),
                                         sootcls.getMethod(sigClinit),
                                         Edge.CLINIT);
                    }
                }
            }
        }
    }
    
    public boolean wantTypes(Local receiver) { throw new RuntimeException("shouldn\'t get here"); }
    
    protected void processNewMethodContext(MethodOrMethodContext momc) {
        cm.addStaticEdges(momc.context(),
                          new jedd.internal.RelationContainer(new Attribute[] { srcm.v(), stmt.v(), kind.v(), tgtm.v() },
                                                              new PhysicalDomain[] { V1.v(), ST.v(), H2.v(), V2.v() },
                                                              ("cm.addStaticEdges(momc.context(), jedd.internal.Jedd.v().pro" +
                                                               "ject(cicg.edgesOutOf(momc.method()), new jedd.PhysicalDomain" +
                                                               "[...])) at /home/olhotak/soot-2-jedd/src/soot/jimple/toolkit" +
                                                               "s/callgraph/BDDOnFlyCallGraphBuilder.jedd:153,8"),
                                                              jedd.internal.Jedd.v().project(cicg.edgesOutOf(momc.method()),
                                                                                             new PhysicalDomain[] { T2.v(), T1.v() })));
    }
    
    protected void addEdge(SootMethod src, Stmt stmt, SootMethod tgt, int kind) { cicg.addEdge(src, stmt, tgt, kind); }
    
    private final jedd.internal.RelationContainer virtualCallSites =
      new jedd.internal.RelationContainer(new Attribute[] { var.v(), stmt.v(), method.v(), signature.v(), kind.v() },
                                          new PhysicalDomain[] { V3.v(), ST.v(), V1.v(), H1.v(), H2.v() },
                                          ("private <soot.jimple.spark.bdddomains.var:soot.jimple.spark." +
                                           "bdddomains.V3, soot.jimple.spark.bdddomains.stmt, soot.jimpl" +
                                           "e.spark.bdddomains.method, soot.jimple.spark.bdddomains.sign" +
                                           "ature, soot.jimple.spark.bdddomains.kind> virtualCallSites =" +
                                           " jedd.internal.Jedd.v().falseBDD() at /home/olhotak/soot-2-j" +
                                           "edd/src/soot/jimple/toolkits/callgraph/BDDOnFlyCallGraphBuil" +
                                           "der.jedd:161,12"),
                                          jedd.internal.Jedd.v().falseBDD());
}
