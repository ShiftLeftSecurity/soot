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
    
    private QueueReader targets = this.targetsQueue.reader();
    
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
        this.worklist = rm.listener();
        this.virtualCalls = new BDDVirtualCalls(Scene.v().getOrMakeBDDHierarchy());
    }
    
    private ChunkedQueue reachablesQueue = new ChunkedQueue();
    
    private QueueReader reachablesListener = this.reachablesQueue.reader();
    
    protected Iterator newReachables() { return this.reachablesListener; }
    
    protected void updateReachables() {
        this.rm.update();
        while (this.worklist.hasNext()) {
            final Relation methodContext =
              new Relation(new Domain[] { method.v(), ctxt.v() },
                           new PhysicalDomain[] { V1.v(), T1.v() },
                           this.worklist.next());
            Iterator it =
              new Relation(new Domain[] { method.v(), ctxt.v() },
                           new PhysicalDomain[] { V1.v(), T1.v() },
                           methodContext).iterator(new Domain[] { method.v(), ctxt.v() });
            while (it.hasNext()) {
                Object[] pair = (Object[]) it.next();
                this.reachablesQueue.add(MethodContext.v((SootMethod) pair[0], pair[1]));
            }
        }
    }
    
    public void addTypes(final Relation types) {
        final Relation signatures =
          new Relation(new Domain[] { type.v(), stmt.v(), method.v(), signature.v(), kind.v() },
                       new PhysicalDomain[] { T2.v(), ST.v(), V1.v(), H1.v(), H2.v() },
                       Jedd.v().compose(Jedd.v().read(this.virtualCallSites), types, new PhysicalDomain[] { V3.v() }));
        this.virtualCalls.addTypes(new Relation(new Domain[] { signature.v(), type.v() },
                                                new PhysicalDomain[] { H1.v(), T2.v() },
                                                Jedd.v().project(signatures,
                                                                 new PhysicalDomain[] { V1.v(), ST.v(), H2.v() })));
        final Relation edges =
          new Relation(new Domain[] { srcm.v(), stmt.v(), kind.v(), tgtm.v() },
                       new PhysicalDomain[] { V1.v(), ST.v(), H2.v(), V2.v() },
                       Jedd.v().compose(Jedd.v().read(Jedd.v().replace(signatures,
                                                                       new PhysicalDomain[] { T2.v() },
                                                                       new PhysicalDomain[] { T1.v() })),
                                        this.virtualCalls.answer(),
                                        new PhysicalDomain[] { T1.v(), H1.v() }));
        this.cm.addVirtualEdges(new Relation(new Domain[] { tgtm.v(), stmt.v(), srcm.v(), kind.v() },
                                             new PhysicalDomain[] { V2.v(), ST.v(), V1.v(), H2.v() },
                                             edges));
    }
    
    protected void addVirtualCallSite(Stmt s,
                                      SootMethod m,
                                      Local receiver,
                                      InstanceInvokeExpr iie,
                                      NumberedString subSig,
                                      int _kind) {
        Scene.v().getUnitNumberer().add(s);
        LocalVarNode rvn = this.pag.makeLocalVarNode(receiver, receiver.getType(), m);
        this.virtualCallSites.eqUnion(Jedd.v().literal(new Object[] { rvn, s, m, subSig, KindNumberer.v().get(_kind) },
                                                       new Domain[] { var.v(), stmt.v(), method.v(), signature.v(), kind.v() },
                                                       new PhysicalDomain[] { V3.v(), ST.v(), V1.v(), H1.v(), H2.v() }));
    }
    
    public void addType(Local receiver, Object srcContext, Type type, Object typeContext) {
        throw new RuntimeException("shouldn\'t be called");
    }
    
    public void addStringConstant(Local l, Object srcContext, String constant) {
        for (Iterator siteIt = ((Collection) this.stringConstToSites.get(l)).iterator(); siteIt.hasNext(); ) {
            final VirtualCallSite site = (VirtualCallSite) siteIt.next();
            if (constant == null) {
                if (this.options.verbose()) {
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
                    if (this.options.verbose()) {
                        G.v().out.println("Warning: Class " + constant + " is" +
                                          " a dynamic class, and you did not specify" +
                                          " it as such; graph will be incomplete!");
                    }
                } else {
                    SootClass sootcls = Scene.v().getSootClass(constant);
                    if (!sootcls.isApplicationClass()) { sootcls.setLibraryClass(); }
                    if (sootcls.declaresMethod(this.sigClinit)) {
                        this.cm.addStaticEdge(MethodContext.v(site.container(), srcContext),
                                              site.stmt(),
                                              sootcls.getMethod(this.sigClinit),
                                              Edge.CLINIT);
                    }
                }
            }
        }
    }
    
    public boolean wantTypes(Local receiver) { throw new RuntimeException("shouldn\'t get here"); }
    
    protected void processNewMethodContext(MethodOrMethodContext momc) {
        this.cm.addStaticEdges(momc.context(),
                               new Relation(new Domain[] { srcm.v(), stmt.v(), kind.v(), tgtm.v() },
                                            new PhysicalDomain[] { V1.v(), ST.v(), H2.v(), V2.v() },
                                            Jedd.v().project(this.cicg.edgesOutOf(momc.method()),
                                                             new PhysicalDomain[] { T1.v(), T2.v() })));
    }
    
    protected void addEdge(SootMethod src, Stmt stmt, SootMethod tgt, int kind) {
        this.cicg.addEdge(src, stmt, tgt, kind);
    }
    
    private final Relation virtualCallSites =
      new Relation(new Domain[] { var.v(), stmt.v(), method.v(), signature.v(), kind.v() },
                   new PhysicalDomain[] { V3.v(), ST.v(), V1.v(), H1.v(), H2.v() },
                   Jedd.v().falseBDD());
}
