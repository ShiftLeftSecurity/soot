package soot.jimple.toolkits.pointer.rinard;
import java.util.*;
import soot.toolkits.scalar.*;
import soot.jimple.toolkits.invoke.*;
import soot.toolkits.graph.*;
import soot.toolkits.*;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.io.*;

class Rinard extends ForwardFlowAnalysis {
    static final boolean printTempLocals = false;
    Graph endGraph;
    InvokeGraph ig;
    NodeManager nm;
    RinardPointerStmtSwitch sw = new RinardPointerStmtSwitch();

    public Rinard(DirectedGraph cfg, InvokeGraph ig ) 
    {
	super(cfg);
	this.ig = ig;
	Chain locals = ((CompleteUnitGraph)cfg).getBody().getLocals();
	endGraph = new Graph();
	nm = new NodeManager();
	doAnalysis();
    }

    protected Object newInitialFlow()
    {
	return new Graph();
    }

    protected void flowThrough( Object inValue, Object unit, Object outValue ) 
    {
	final Graph in = (Graph) inValue;
	final Graph out = (Graph) outValue;
	final Stmt s = (Stmt) unit;

	copy( (Graph) inValue, (Graph) outValue );

	sw.flowThrough( (Graph) inValue, (Stmt) unit, (Graph) outValue );
    }
    protected void copy( Object source, Object dest )
    {
	Graph s = (Graph) source;
	Graph d = (Graph) dest;
	s.copyTo( d );
    }

    protected void merge( Object in1, Object in2, Object out )
    {
	((Graph)out).mergeFrom( (Graph) in1 );
	((Graph)out).mergeFrom( (Graph) in2 );
    }

    class RinardPointerStmtSwitch extends PointerStmtSwitch {
	protected Graph in;
	protected Graph out;
	protected Stmt s;

	public void flowThrough( Graph in, Stmt s, Graph out ) {
	    this.in = in; this.s = s; this.out = out;
	    s.apply( this );
	}
	
	protected void caseAssignConstStmt( Value dest, Constant c ) {
	    if( dest instanceof Local ) {
		out.killEdgesFrom( (Local) dest );
	    }
	}
	protected void caseCopyStmt( Local dest, Local src ) {
	    out.killEdgesFrom( dest );
	    out.addIEdges( dest, in.getITargetsFor( src ) );
	}
	protected void caseIdentityStmt( Local dest, IdentityRef src ) {
	    out.killEdgesFrom( dest );
	    out.addIEdge( dest, nm.findNode( src ) );
	}
	protected void caseLoadStmt( Local dest, InstanceFieldRef src ) {
	    out.killEdgesFrom( dest );
	    SootField field = src.getField();
	    Iterator it = in.getITargetsFor( (Local) src.getBase() ).iterator();
	    while( it.hasNext() ) {
		Node n = (Node) it.next();
		NodeXField nf = new NodeXField( n, field );
		if( in.hasEscaped( n ) ) {
		    out.addOEdge( nf, nm.findNode( s ) );
		    out.addIEdge( dest, nm.findNode( s ) );
		}
		out.addIEdges( dest, in.getITargetsFor( nf ) );
		out.addIEdges( dest, in.getOTargetsFor( nf ) );
	    }
	}
	protected void caseStoreStmt( InstanceFieldRef dest, Local src ) {
	    SootField field = dest.getField();
	    Iterator it = in.getITargetsFor( (Local)dest.getBase() ).iterator();
	    Set srcNodes = in.getITargetsFor( src );
	    while( it.hasNext() ) {
		Node n = (Node) it.next();
		out.addIEdges( new NodeXField( n, field ), srcNodes );
	    }
	}
	protected void caseArrayLoadStmt( Local dest, ArrayRef src ) {
	    out.killEdgesFrom( dest );
	    Iterator it = in.getITargetsFor( (Local) src.getBase() ).iterator();
	    while( it.hasNext() ) {
		Node n = (Node) it.next();
		NodeXField nf = new NodeXField( n, null );
		if( in.hasEscaped( n ) ) {
		    out.addOEdge( nf, nm.findNode( s ) );
		    out.addIEdge( dest, nm.findNode( s ) );
		}
		out.addIEdges( dest, in.getITargetsFor( nf ) );
		out.addIEdges( dest, in.getOTargetsFor( nf ) );
	    }
	}
	protected void caseArrayStoreStmt( ArrayRef dest, Local src ) {
	    Iterator it = in.getITargetsFor( (Local)dest.getBase() ).iterator();
	    Set srcNodes = in.getITargetsFor( src );
	    while( it.hasNext() ) {
		Node n = (Node) it.next();
		out.addIEdges( new NodeXField( n, null ), srcNodes );
	    }
	}
	protected void caseGlobalLoadStmt( Local dest, StaticFieldRef src ) {
	    out.killEdgesFrom( dest );
	    SootField field = src.getField();
	    Node n = (Node) nm.findNode( field.getDeclaringClass() );
	    NodeXField nf = new NodeXField( n, field );
	    out.addIEdges( dest, in.getITargetsFor( nf ) );
	    out.addIEdges( dest, in.getOTargetsFor( nf ) );
	}
	protected void caseGlobalStoreStmt( StaticFieldRef dest, Local src ) {
	    SootField field = dest.getField();
	    Node n = (Node) nm.findNode( field.getDeclaringClass() );
	    NodeXField nf = new NodeXField( n, field );
	    out.addIEdges( nf, in.getITargetsFor( src ) );
	}
	protected void caseAnyNewStmt( Local dest, Expr e ) {
	    out.killEdgesFrom( dest );
	    out.addIEdge( dest, nm.findNode( e ) );
	}
	protected void caseInvokeStmt( Local dest, InvokeExpr e ) {
	    if( dest != null ) {
		out.killEdgesFrom( dest );
		out.addIEdge( dest, nm.findNode( s ) );
	    }
	    tryToAnalyzeCallees: {
		if( ig != null ) {
		    Iterator it = ig.getTargetsOf( s ).iterator();
		    while( it.hasNext() ) {
			SootMethod m = (SootMethod) it.next();
			if( !m.hasActiveBody() ) {
			    if( m.getDeclaringClass().getName().equals( "java.lang.Object" ) && m.getName().equals("<init>" ) ) continue;
			    break tryToAnalyzeCallees;
			}
			Body b = m.getActiveBody();
			Graph g = RinardTransformer.v().getGraphForBody( b );
			if( g == null ) break tryToAnalyzeCallees;
		    }
		    it = ig.getTargetsOf( s ).iterator();
		    while( it.hasNext() ) {
			SootMethod m = (SootMethod) it.next();
			if( !m.hasActiveBody() ) continue;
			Body b = m.getActiveBody();
			Graph g = RinardTransformer.v().getGraphForBody( b );
			InterProc ip = new InterProc();
			ip.go( dest, e, m, in, g );
			out.mergeFrom( ip.m );
		    }
		    return;
		}
	    }
	    Iterator it = e.getArgs().iterator();
	    while( it.hasNext() ) {
		Value v = (Value) it.next();
		if( v instanceof Local ) {
		    out.makeEscape( in.getITargetsFor( (Local)v ) );
		}
	    }
	    if( e instanceof InstanceInvokeExpr ) {
		InstanceInvokeExpr iie = (InstanceInvokeExpr) e;
		out.makeEscape( in.getITargetsFor( (Local)iie.getBase() ) );
	    }
	}
	protected void caseReturnStmt( Local val ) {
	    endGraph.mergeFrom( in ); 
	    if( val != null ) {
		endGraph.makeReturned( in.getITargetsFor( (Local) val ) );
	    }
	}
    }
}

