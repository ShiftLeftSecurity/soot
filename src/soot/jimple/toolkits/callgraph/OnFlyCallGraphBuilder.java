/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Ondrej Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package soot.jimple.toolkits.callgraph;
import soot.*;
import soot.options.*;
import soot.jimple.*;
import java.util.*;
import soot.util.*;
import soot.util.queue.*;

/** Models the call graph.
 * @author Ondrej Lhotak
 */
public final class OnFlyCallGraphBuilder extends AbstractOnFlyCallGraphBuilder
{ 
    /** context-insensitive stuff */
    private CallGraph cicg = new CallGraph();

    /** context-sensitive stuff */
    private ContextManager cm;

    private ChunkedQueue targetsQueue = new ChunkedQueue();
    private QueueReader targets = targetsQueue.reader();

    private ReachableMethods rm;
    private QueueReader worklist;


    public OnFlyCallGraphBuilder( ContextManager cm, ReachableMethods rm ) {
        this(cm, rm, false);
    }
    public OnFlyCallGraphBuilder( ContextManager cm, ReachableMethods rm, boolean appOnly ) {
        super(appOnly);
        this.cm = cm;
        this.rm = rm;
        worklist = rm.listener();
    }
    protected Iterator newReachables() { return worklist; }
    protected void updateReachables() { rm.update(); }
    public void addType( Local receiver, Context srcContext, Type type, Context typeContext ) {
        FastHierarchy fh = Scene.v().getOrMakeFastHierarchy();
        for( Iterator siteIt = ((Collection) receiverToSites.get( receiver )).iterator(); siteIt.hasNext(); ) {
            final VirtualCallSite site = (VirtualCallSite) siteIt.next();
            InstanceInvokeExpr iie = site.iie();
            if( site.kind() == Kind.THREAD 
            && !fh.canStoreType( type, clRunnable ) )
                continue;

            if( site.iie() instanceof SpecialInvokeExpr ) {
                targetsQueue.add( VirtualCalls.v().resolveSpecial( 
                            (SpecialInvokeExpr) site.iie(),
                            site.subSig(),
                            site.container() ) );
            } else {
                VirtualCalls.v().resolve( type,
                        receiver.getType(),
                        site.subSig(),
                        site.container(), 
                        targetsQueue );
            }
            while(true) {
                SootMethod target = (SootMethod) targets.next();
                if( target == null ) break;
                cm.addVirtualEdge(
                        MethodContext.v( site.container(), srcContext ),
                        site.stmt(),
                        target,
                        site.kind(),
                        typeContext );
            }
        }
    }
    public void addStringConstant( Local l, Context srcContext, String constant ) {
        for( Iterator siteIt = ((Collection) stringConstToSites.get( l )).iterator(); siteIt.hasNext(); ) {
            final VirtualCallSite site = (VirtualCallSite) siteIt.next();
            if( constant == null ) {
                if( options.verbose() ) {
                    G.v().out.println( "Warning: Method "+site.container()+
                        " is reachable, and calls Class.forName on a"+
                        " non-constant String; graph will be incomplete!"+
                        " Use safe-forname option for a conservative result." );
                }
            } else {
                if( constant.charAt(0) == '[' ) {
                    if( constant.length() > 1 && constant.charAt(1) == 'L' 
                    && constant.charAt(constant.length()-1) == ';' ) {
                        constant = constant.substring(2,constant.length()-1);
                    } else continue;
                }
                if( !Scene.v().containsClass( constant ) ) {
                    if( options.verbose() ) {
                        G.v().out.println( "Warning: Class "+constant+" is"+
                            " a dynamic class, and you did not specify"+
                            " it as such; graph will be incomplete!" );
                    }
                } else {
                    SootClass sootcls = Scene.v().getSootClass( constant );
                    if( !sootcls.isApplicationClass() ) {
                        sootcls.setLibraryClass();
                    }
                    for( Iterator clinitIt = EntryPoints.v().clinitsOf(sootcls).iterator(); clinitIt.hasNext(); ) {
                        final SootMethod clinit = (SootMethod) clinitIt.next();
                        cm.addStaticEdge(
                                MethodContext.v( site.container(), srcContext ),
                                site.stmt(),
                                clinit,
                                Kind.CLINIT );
                    }
                }
            }
        }
    }
    public boolean wantTypes( Local receiver ) {
        return receiverToSites.get(receiver) != null;
    }


    /* End of public methods. */

    protected void processNewMethodContext( MethodOrMethodContext momc ) {
        SootMethod m = momc.method();
        Object ctxt = momc.context();
        Iterator it = cicg.edgesOutOf(m);
        while( it.hasNext() ) {
            Edge e = (Edge) it.next();
            cm.addStaticEdge( momc, e.srcUnit(), e.tgt(), e.kind() );
        }
    }

    protected void addEdge( SootMethod src, Stmt stmt, SootMethod tgt,
            Kind kind ) {
        cicg.addEdge( new Edge( src, stmt, tgt, kind ) );
    }

    protected void addVirtualCallSite( Stmt s, SootMethod m, Local receiver,
            InstanceInvokeExpr iie, NumberedString subSig, Kind kind ) {
        List sites = (List) receiverToSites.get(receiver);
        if (sites == null) {
            receiverToSites.put(receiver, sites = new ArrayList());
            List receivers = (List) methodToReceivers.get(m);
            if( receivers == null )
                methodToReceivers.put(m, receivers = new ArrayList());
            receivers.add(receiver);
        }
        sites.add(new VirtualCallSite(s, m, iie, subSig, kind));
    }
}

