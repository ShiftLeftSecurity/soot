package soot.jimple.toolkits.pointer;
import soot.toolkits.graph.*;
import soot.jimple.toolkits.invoke.*;
import soot.*;
import soot.jimple.*;
import java.util.*;

public abstract class PointerPropagationGraph extends PointerStmtSwitch
{
    public boolean parmsAsFields = false;
    public boolean returnsAsFields = false;
    public boolean collapseObjects = false;
    public boolean typesForSites = false;
    public boolean mergeStringbuffer = false;
    public boolean simulateNatives = false;

    InvokeGraph ig;

    static int castsSameAsDest = 0;
    static int castsDifferentFromDest = 0;

    protected SootMethod currentMethod;

    public PointerPropagationGraph( InvokeGraph ig ) { this.ig = ig; }

    public static boolean isType( Object o ) {
	return o instanceof RefType || o instanceof ArrayType;
    }
    public void addAnyEdge( Object o, Object p ) {
    }
    public void addSimpleEdge( 
	Object src, Type srcType,
	Object dest, Type destType )
    {
	addAnyEdge( src, dest );
    }
    public void addLoadEdge( 
	Object base, Type baseType,
	Object field, Type fieldType,
	Object dest, Type destType )
    {
	addAnyEdge( new Pair( base, field ), dest );
    }
    public void addStoreEdge( 
	Object src, Type srcType,
	Object base, Type baseType,
	Object field, Type fieldType ) 
    {
	addAnyEdge( src, new Pair( base, field ) );
    }
    public void addNewEdge( 
	Object newExpr, Type newExprType,
	Object dest, Type destType ) 
    {
	addAnyEdge( newExpr, dest );
    }
    protected Stmt stmt;
    protected SootMethod method;
    public void handleStmt( final Stmt stmt, final SootMethod method ) {
	this.stmt = stmt; this.method = method;
	stmt.apply( this );
    }
    protected void caseAssignConstStmt( Value dest, Constant c ) {}
    protected void caseCastStmt( Local dest, Local src, CastExpr c ) {
	if( dest.getType().equals( c.getCastType() ) ) {
	    addSimpleEdge( src, src.getType(), dest, dest.getType() );
	    castsSameAsDest++;
	} else {
	    Pair castNode = new Pair( stmt, PointerAnalysis.CAST_NODE );
	    addSimpleEdge( src, src.getType(), castNode, c.getType() );
	    addSimpleEdge( castNode, c.getType(), dest, dest.getType() );
	    castsDifferentFromDest++;
	}
    }
    protected void caseCopyStmt( Local dest, Local src ) {
	addSimpleEdge( src, src.getType(), dest, dest.getType() );
    }
    protected void caseIdentityStmt( Local dest, IdentityRef src ) {
	if( src instanceof ThisRef ) {
	    addSimpleEdge( new Pair( method, PointerAnalysis.THIS_NODE ),
		    src.getType(),

		dest, dest.getType() );
	} else if( src instanceof ParameterRef ) {
	    if( !parmsAsFields ) {
		addSimpleEdge( 
		    new Pair( method, 
			new Integer( ((ParameterRef) src).getIndex() ) ), 
		    src.getType(), 
		    dest, 
		    dest.getType() );
	    } else {
		addLoadEdge(
		    new Pair( method, PointerAnalysis.THIS_NODE ),
		    method.getDeclaringClass().getType(),

		    new Pair( method.getSubSignature(),
			new Integer( ((ParameterRef) src).getIndex() ) ),
		    src.getType(),

		    dest,
		    dest.getType() );
	    }
	}
    }
    protected void caseLoadStmt( Local dest, InstanceFieldRef src ) {
	if( collapseObjects ) {
	    addSimpleEdge( src.getField(), src.getField().getType(),
		dest, dest.getType() );
	} else {
	    addLoadEdge( src.getBase(), src.getBase().getType(),
		src.getField(), src.getField().getType(),
		dest, dest.getType() );
	}
    }
    protected void caseStoreStmt( InstanceFieldRef dest, Local src ) {
	if( collapseObjects ) {
	    addSimpleEdge( src, src.getType(),
		dest.getField(), dest.getField().getType() );
	} else {
	    addStoreEdge( src, src.getType(),
		dest.getBase(), dest.getBase().getType(),
		dest.getField(), dest.getField().getType() );
	}
    }
    protected void caseArrayLoadStmt( Local dest, ArrayRef src ) {
	addLoadEdge( src.getBase(), src.getBase().getType(),

	    PointerAnalysis.ARRAY_ELEMENTS_NODE, 
	    ( (ArrayType) src.getBase().getType() ).baseType,

	    dest, dest.getType() );
    }
    protected void caseArrayStoreStmt( ArrayRef dest, Local src ) {
	addStoreEdge( src, src.getType(),

	    dest.getBase(), dest.getBase().getType(),

	    PointerAnalysis.ARRAY_ELEMENTS_NODE,
	    ( (ArrayType) dest.getBase().getType() ).baseType );
    }
    protected void caseGlobalLoadStmt( Local dest, StaticFieldRef src ) {
	addSimpleEdge( src.getField(), src.getField().getType(),
	    dest, dest.getType() ); 
    }
    protected void caseGlobalStoreStmt( StaticFieldRef dest, Local src ) {
	addSimpleEdge( src, src.getType(), 
	    dest.getField(), dest.getField().getType() ); 
    }
    protected void caseAnyNewStmt( Local dest, Expr e ) {
	if( typesForSites ||
		( mergeStringbuffer 
		  && RefType.v( "java.lang.StringBuffer" )
		    .equals( e.getType() ) ) ) {

	    addNewEdge( e.getType(), e.getType(), dest, dest.getType() );
	} else {
	    addNewEdge( e, e.getType(), dest, dest.getType() );
	}
    }
    protected void caseInvokeStmt( Local dest, InvokeExpr e ) {
	Iterator it = ig.getTargetsOf( stmt ).iterator();
	while( it.hasNext() ) {
	    SootMethod m = (SootMethod) it.next();
	    if( !m.hasActiveBody() ) continue;
	    Body b = m.getActiveBody();
	    Iterator it2 = e.getArgs().iterator();
	    int i = 0;
	    while( it2.hasNext() ) {
		Value v = (Value) it2.next();
		if( v instanceof Local && isType( v.getType() ) ) {
		    if( !parmsAsFields ) {
			addSimpleEdge( v, v.getType(), 
			    new Pair( m, new Integer(i) ), m.getParameterType(i) );
		    } else {
			addStoreEdge( 
			    v, v.getType(),

			    new Pair( m, PointerAnalysis.THIS_NODE ),
			    m.getDeclaringClass().getType(),

			    new Pair( m.getSubSignature(), new Integer( i ) ),
			    m.getParameterType(i) );
		    }
		}
		i++;
	    }
	    if( e instanceof InstanceInvokeExpr ) {
		InstanceInvokeExpr iie = (InstanceInvokeExpr) e;
		addSimpleEdge( iie.getBase(), iie.getBase().getType(),

		    new Pair( m, PointerAnalysis.THIS_NODE ), 
		    m.getDeclaringClass().getType() );
	    }
	    if( dest != null && isType( m.getReturnType() ) ) {
		if( !returnsAsFields ) {
		    addSimpleEdge( new Pair( m, PointerAnalysis.RETURN_NODE ),
			m.getReturnType(),
			dest, dest.getType() );
		} else {
		    addLoadEdge( 
			new Pair( method, PointerAnalysis.THIS_NODE ),
			method.getDeclaringClass().getType(),

			new Pair( method.getSubSignature(),
			    PointerAnalysis.RETURN_NODE ),
			method.getReturnType(),
			
			dest, dest.getType() );
		}
	    }

	}
    }
    protected void caseReturnStmt( Local val ) {
	if( val != null ) {
	    if( !returnsAsFields ) {
		addSimpleEdge( val, val.getType(),
		    new Pair( method, PointerAnalysis.RETURN_NODE ),
			method.getReturnType() );
	    } else {
		addStoreEdge( 
		
		    val, val.getType(),
		
		    new Pair( method, PointerAnalysis.THIS_NODE ),
		    method.getDeclaringClass().getType(),

		    new Pair( method.getSubSignature(),
			PointerAnalysis.RETURN_NODE ),
		    method.getReturnType() );
	    }
	}
    }

    public void buildNative( SootMethod m ) {
	throw new RuntimeException( 
	    "Needs to be overridden in subclasses of PointerPropagationGraph" );
    }

    public void build()
    {
	Iterator classesIt = Scene.v().getClasses().iterator();
	while( classesIt.hasNext() )
	{
	    SootClass c = (SootClass) classesIt.next();
	    Iterator methodsIt = c.getMethods().iterator();
	    while( methodsIt.hasNext() )
	    {
		SootMethod m = (SootMethod) methodsIt.next();
		currentMethod = m;
		if( simulateNatives && m.isNative() ) {
		    buildNative( m );
		}
		if( !m.hasActiveBody() ) continue;
		Body b = m.retrieveActiveBody();
		Iterator unitsIt = b.getUnits().iterator();
		while( unitsIt.hasNext() )
		{
		    handleStmt( (Stmt) unitsIt.next(), m );
		}
	    }
	}
	System.out.println( "Casts same type as dest: "+castsSameAsDest );
	System.out.println( "Casts different type from dest: "+castsDifferentFromDest );
    }
}

