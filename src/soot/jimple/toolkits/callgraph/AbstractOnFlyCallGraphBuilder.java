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
public abstract class AbstractOnFlyCallGraphBuilder
{ 
    /** context-insensitive stuff */
    protected HashSet analyzedMethods = new HashSet();

    protected LargeNumberedMap receiverToSites = new LargeNumberedMap( Scene.v().getLocalNumberer() ); // Local -> List(VirtualCallSite)
    protected LargeNumberedMap methodToReceivers = new LargeNumberedMap( Scene.v().getMethodNumberer() ); // SootMethod -> List(Local)
    public LargeNumberedMap methodToReceivers() { return methodToReceivers; }

    protected SmallNumberedMap stringConstToSites = new SmallNumberedMap( Scene.v().getLocalNumberer() ); // Local -> List(VirtualCallSite)
    protected LargeNumberedMap methodToStringConstants = new LargeNumberedMap( Scene.v().getMethodNumberer() ); // SootMethod -> List(Local)
    public LargeNumberedMap methodToStringConstants() { return methodToStringConstants; }

    protected CGOptions options;

    protected boolean appOnly;

    public AbstractOnFlyCallGraphBuilder( boolean appOnly ) {
        options = new CGOptions( PhaseOptions.v().getPhaseOptions("cg") );
        if( !options.verbose() ) {
            G.v().out.println( "[Call Graph] For information on where the call graph may be incomplete, use the verbose option to the cg phase." );
        }
        this.appOnly = appOnly;
    }
    protected abstract Iterator newReachables();
    protected abstract void updateReachables();
    public void processReachables() {
        Iterator worklist = newReachables();
        while(true) {
            if( !worklist.hasNext() ) {
                updateReachables();
                if( !worklist.hasNext() ) break;
            }
            MethodOrMethodContext momc = (MethodOrMethodContext) worklist.next();
            SootMethod m = momc.method();
            if( appOnly && !m.getDeclaringClass().isApplicationClass() ) continue;
            if( analyzedMethods.add( m ) ) processNewMethod( m );
            processNewMethodContext( momc );
        }
    }
    public abstract boolean wantTypes( Local receiver );
    public abstract void addType( Local receiver, Context srcContext, Type type, Context typeContext );
    public boolean wantStringConstants( Local stringConst ) {
        return stringConstToSites.get(stringConst) != null;
    }
    public abstract void addStringConstant( Local l, Context srcContext, String constant );

    /* End of public methods. */

    protected void processNewMethod( SootMethod m ) {
        if( m.isNative() || m.isPhantom() ) {
            return;
        }
        Body b = m.retrieveActiveBody();
        getImplicitTargets( m );
        findReceivers(m, b);
    }
    protected abstract void addVirtualCallSite( Stmt s, SootMethod m, Local receiver, InstanceInvokeExpr iie, NumberedString subSig, Kind kind );
    protected void findReceivers(SootMethod m, Body b) {
        for( Iterator sIt = b.getUnits().iterator(); sIt.hasNext(); ) {
            final Stmt s = (Stmt) sIt.next();
            if (s.containsInvokeExpr()) {
                InvokeExpr ie = (InvokeExpr) s.getInvokeExpr();

                if (ie instanceof InstanceInvokeExpr) {
                    InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
                    Local receiver = (Local) iie.getBase();
                    NumberedString subSig = 
                        iie.getMethod().getNumberedSubSignature();
                    addVirtualCallSite( s, m, receiver, iie, subSig,
                            Edge.ieToKind(iie) );
                    if( subSig == sigStart ) {
                        addVirtualCallSite( s, m, receiver, iie, sigRun,
                                Kind.THREAD );
                    }
                } else {
                    SootMethod tgt = ((StaticInvokeExpr) ie).getMethod();
                    addEdge(m, s, tgt);
                    if( tgt.getSignature().equals( "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedAction)>" )
                    ||  tgt.getSignature().equals( "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedExceptionAction)>" )
                    ||  tgt.getSignature().equals( "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedAction,java.security.AccessControlContext)>" )
                    ||  tgt.getSignature().equals( "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedExceptionAction,java.security.AccessControlContext)>" ) ) {

                        Local receiver = (Local) ie.getArg(0);
                        addVirtualCallSite( s, m, receiver, null, sigObjRun,
                                Kind.PRIVILEGED );
                    }
                }
            }
        }
    }
    
    protected void getImplicitTargets( SootMethod source ) {
        List stringConstants = (List) methodToStringConstants.get(source);
        if( stringConstants == null )
            methodToStringConstants.put(source, stringConstants = new ArrayList());
        final SootClass scl = source.getDeclaringClass();
        if( source.isNative() || source.isPhantom() ) return;
        if( source.getSubSignature().indexOf( "<init>" ) >= 0 ) {
            handleInit(source, scl);
        }
        Body b = source.retrieveActiveBody();
        boolean warnedAlready = false;
        for( Iterator sIt = b.getUnits().iterator(); sIt.hasNext(); ) {
            final Stmt s = (Stmt) sIt.next();
            if( s.containsInvokeExpr() ) {
                InvokeExpr ie = (InvokeExpr) s.getInvokeExpr();
                if( ie.getMethod().getSignature().equals( "<java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])>" ) ) {
                    if( !warnedAlready ) {
                        if( options.verbose() ) {
                            G.v().out.println( "Warning: call to "+
                                "java.lang.reflect.Method: invoke() from "+source+
                                "; graph will be incomplete!" );
                        }
                        warnedAlready = true;
                    }
                }
                if( ie.getMethod().getSignature().equals( "<java.lang.Class: java.lang.Object newInstance()>" ) ) {
                    if( options.safe_newinstance() ) {
                        for( Iterator tgtIt = EntryPoints.v().inits().iterator(); tgtIt.hasNext(); ) {
                            final SootMethod tgt = (SootMethod) tgtIt.next();
                            addEdge( source, s, tgt, Kind.NEWINSTANCE );
                        }
                    } else {
                        if( options.verbose() ) {
                            G.v().out.println( "Warning: Method "+source+
                                " is reachable, and calls Class.newInstance;"+
                                " graph will be incomplete!"+
                                " Use safe-newinstance option for a conservative result." );
                        }
                    } 
                }
                if( ie instanceof StaticInvokeExpr ) {
                    addEdge( source, s, ie.getMethod().getDeclaringClass(),
                        sigClinit, Kind.CLINIT );
                }
                if( ie.getMethod().getNumberedSubSignature() == sigForName ) {
                    Value className = ie.getArg(0);
                    if( className instanceof StringConstant ) {
                        String cls = ((StringConstant) className ).value;
                        constantForName( cls, source, s );
                    } else {
                        Local constant = (Local) className;
                        if( options.safe_forname() ) {
                            for( Iterator tgtIt = EntryPoints.v().clinits().iterator(); tgtIt.hasNext(); ) {
                                final SootMethod tgt = (SootMethod) tgtIt.next();
                                addEdge( source, s, tgt, Kind.CLINIT );
                            }
                        } else {
                            VirtualCallSite site = new VirtualCallSite( s, source, null, null, Kind.CLINIT );
                            List sites = (List) stringConstToSites.get(constant);
                            if (sites == null) {
                                stringConstToSites.put(constant, sites = new ArrayList());
                                stringConstants.add(constant);
                            }
                            sites.add(site);
                        }
                    }
                }
            }
            if( s.containsFieldRef() ) {
                FieldRef fr = (FieldRef) s.getFieldRef();
                if( fr instanceof StaticFieldRef ) {
                    SootClass cl = fr.getField().getDeclaringClass();
                    addEdge( source, s, cl, sigClinit, Kind.CLINIT );
                }
            }
            if( s instanceof AssignStmt ) {
                Value rhs = ((AssignStmt)s).getRightOp();
                if( rhs instanceof NewExpr ) {
                    NewExpr r = (NewExpr) rhs;
                    addEdge( source, s, r.getBaseType().getSootClass(),
                            sigClinit, Kind.CLINIT );
                } else if( rhs instanceof NewArrayExpr || rhs instanceof NewMultiArrayExpr ) {
                    Type t = rhs.getType();
                    if( t instanceof ArrayType ) t = ((ArrayType)t).baseType;
                    if( t instanceof RefType ) {
                        addEdge( source, s, ((RefType) t).getSootClass(),
                                sigClinit, Kind.CLINIT );
                    }
                }
            }
        }
    }

    protected abstract void processNewMethodContext( MethodOrMethodContext momc );

    protected void handleInit(SootMethod source, final SootClass scl) {
        addEdge( source, null, scl, sigFinalize, Kind.FINALIZE );
        FastHierarchy fh = Scene.v().getOrMakeFastHierarchy();
    }
    protected void constantForName( String cls, SootMethod src, Stmt srcUnit ) {
        if( cls.charAt(0) == '[' ) {
            if( cls.charAt(1) == 'L' && cls.charAt(cls.length()-1) == ';' ) {
                cls = cls.substring(2,cls.length()-1);
                constantForName( cls, src, srcUnit );
            }
        } else {
            if( !Scene.v().containsClass( cls ) ) {
                if( options.verbose() ) {
                    G.v().out.println( "Warning: Class "+cls+" is"+
                        " a dynamic class, and you did not specify"+
                        " it as such; graph will be incomplete!" );
                }
            } else {
                SootClass sootcls = Scene.v().getSootClass( cls );
                if( !sootcls.isApplicationClass() ) {
                    sootcls.setLibraryClass();
                }
                addEdge( src, srcUnit, sootcls, sigClinit, Kind.CLINIT );
            }
        }
    }

    protected abstract void addEdge( SootMethod src, Stmt stmt, SootMethod tgt,
            Kind kind );
    protected void addEdge(  SootMethod src, Stmt stmt, SootClass cls, NumberedString methodSubSig, Kind kind ) {
        if( cls.declaresMethod( methodSubSig ) ) {
            addEdge( src, stmt, cls.getMethod( methodSubSig ), kind );
        }
    }
    protected void addEdge( SootMethod src, Stmt stmt, String methodSig, Kind kind ) {
        if( Scene.v().containsMethod( methodSig ) ) {
            addEdge( src, stmt, Scene.v().getMethod( methodSig ), kind );
        }
    }
    protected void addEdge( SootMethod src, Stmt stmt, SootMethod tgt ) {
        InvokeExpr ie = stmt.getInvokeExpr();
        addEdge( src, stmt, tgt, Edge.ieToKind(ie) );
    }

    protected final NumberedString sigMain = Scene.v().getSubSigNumberer().
        findOrAdd( "void main(java.lang.String[])" );
    protected final NumberedString sigFinalize = Scene.v().getSubSigNumberer().
        findOrAdd( "void finalize()" );
    protected final NumberedString sigExit = Scene.v().getSubSigNumberer().
        findOrAdd( "void exit()" );
    protected final NumberedString sigClinit = Scene.v().getSubSigNumberer().
        findOrAdd( "void <clinit>()" );
    protected final NumberedString sigStart = Scene.v().getSubSigNumberer().
        findOrAdd( "void start()" );
    protected final NumberedString sigRun = Scene.v().getSubSigNumberer().
        findOrAdd( "void run()" );
    protected final NumberedString sigObjRun = Scene.v().getSubSigNumberer().
        findOrAdd( "java.lang.Object run()" );
    protected final NumberedString sigForName = Scene.v().getSubSigNumberer().
        findOrAdd( "java.lang.Class forName(java.lang.String)" );
    protected final RefType clRunnable = RefType.v("java.lang.Runnable");
    
}

