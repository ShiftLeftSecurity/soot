package soot.jimple.toolkits.pointer;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.*;
import java.util.*;
import soot.util.*;

/** Generates side-effect information from a PointerAnalysis. */
public class SideEffectAnalysis {
    PointerAnalysis pa;
    InvokeGraph ig;
    Map methodToReadSet = new HashMap();
    Map methodToWriteSet = new HashMap();
    int rwsetcount = 0;

    public void findRWSetsForMethod( SootMethod method ) {
	if( methodToReadSet.containsKey( method ) && 
		methodToWriteSet.containsKey( method ) ) return;

	// First find all methods transitively callable from method whose RWSets
	// have not yet been computed, in an order that puts callees
	// before callers (unless they call each other recursively).
	LinkedList workStack = new LinkedList();
	HashSet reachableMethods = new HashSet();
	reachableMethods.add( method );
	workStack.add( method );
	boolean ch;
	System.out.println( "Finding callee sets for "+method );
	do {
	    ch = false;
	    for( Iterator it = new LinkedList( reachableMethods ).iterator();
		    it.hasNext(); ) {
		SootMethod m = (SootMethod) it.next();
		for( Iterator sIt = m.retrieveActiveBody().getUnits().iterator();
			sIt.hasNext(); ) {
		    Object o = sIt.next();
		    Stmt s = (Stmt) o;
		    if( !s.containsInvokeExpr() ) continue;
		    for( Iterator targets = ig.getTargetsOf( s ).iterator();
			    targets.hasNext(); ) {
			SootMethod target = (SootMethod) targets.next();
			if( !reachableMethods.contains( target ) 
			&& (!methodToReadSet.containsKey( target ) 
			    || !methodToWriteSet.containsKey( target ) ) ) {
			    if( !target.isConcrete() ) continue;
			    reachableMethods.add( target );
			    workStack.addFirst( target );
			    ch = true;
			}
		    }
		}
	    }
	} while( ch );

	MultiMap targetToCaller = new HashMultiMap();

	System.out.println( "Finding RW sets for "+method );
	// Now iterate through the found methods, building up the RWSets.
	do {
	    ch = false;
	    for( Iterator it = workStack.iterator(); it.hasNext(); ) {
		SootMethod m = (SootMethod) it.next();
		boolean analyzedThisMethodBefore = true;
		RWSet read = (RWSet) methodToReadSet.get( m );
		if( read == null ) {
		    rwsetcount++;
		    methodToReadSet.put( m, read = new MethodRWSet() );
		    analyzedThisMethodBefore = false;
		    {
			int size = methodToReadSet.keySet().size();
			if( 0 == (size % 1000) )
			    System.out.println( "There are "+size+
				    " methods with read sets" );
		    }
		}
		RWSet write = (RWSet) methodToWriteSet.get( m );
		if( write == null ) {
		    rwsetcount++;
		    methodToWriteSet.put( m, write = new MethodRWSet() );
		    analyzedThisMethodBefore = false;
		    {
			int size = methodToWriteSet.keySet().size();
			if( 0 == (size % 1000) )
			    System.out.println( "There are "+size+
				    " methods with write sets" );
		    }
		}
		if( (rwsetcount % 1000 ) == 0 ) System.out.println( "rwsetcount= "+rwsetcount );
		for( Iterator sIt = m.retrieveActiveBody().getUnits().iterator();
			sIt.hasNext(); ) {
		    Object o = sIt.next();
		    Stmt s = (Stmt) o;
		    if( s.containsInvokeExpr() ) {
			for( Iterator targets = ig.getTargetsOf( s ).iterator();
				targets.hasNext(); ) {
			    SootMethod target = (SootMethod) targets.next();
			    targetToCaller.put( target, m );
			    if( !target.isConcrete() ) continue;
			    ch = read.union( (RWSet)
				    methodToReadSet.get( target ) ) | ch;
			    ch = write.union( (RWSet) 
				    methodToWriteSet.get( target ) ) | ch;
			}
		    } else if( !analyzedThisMethodBefore ) {
			ch = read.union( readSet( m, s ) ) | ch;
			ch = write.union( writeSet( m, s ) ) | ch;
		    }
		}
	    }
	} while( ch );

	System.out.println( "Finding native sets for "+method );
	do {
	    ch = false;
	    for( Iterator it = targetToCaller.keySet().iterator();
		    it.hasNext(); ) {
		SootMethod target = (SootMethod) it.next();
		RWSet targetReadSet = (RWSet) methodToReadSet.get( target );
		if( target.isNative() || target.isPhantom() 
		|| ( targetReadSet != null && targetReadSet.getCallsNative() ) ) {
		    for( Iterator it2 = targetToCaller.get( target ).iterator();
			    it2.hasNext(); ) {
			SootMethod caller = (SootMethod) it2.next();
			RWSet read = (RWSet) methodToReadSet.get( caller );
			RWSet write = (RWSet) methodToWriteSet.get( caller );
			ch = read.setCallsNative() | ch;
			ch = write.setCallsNative() | ch;
		    }
		}
	    }
	} while( ch );
	System.out.println( "Done finding native sets for "+method );
    }

    public SideEffectAnalysis( PointerAnalysis pa, InvokeGraph ig ) {
	System.out.println( "Created new sea" );
	this.pa = pa;
	this.ig = ig;
    }

    HashMap stmtToReadSet = new HashMap();
    public RWSet readSet( SootMethod method, Stmt stmt ) {
	RWSet ret = (RWSet) stmtToReadSet.get( stmt );
	if( ret != null ) return ret;
	if( stmt.containsInvokeExpr() ) {
	    for( Iterator targets = ig.getTargetsOf( stmt ).iterator();
		    targets.hasNext(); ) {
		if( ret == null ) ret = new MethodRWSet();
		SootMethod target = (SootMethod) targets.next();
		if( !target.isConcrete() ) continue;
		findRWSetsForMethod( target );
		ret.union( (RWSet) methodToReadSet.get( target ) );
	    }
	}
	if( stmt instanceof AssignStmt ) {
	    AssignStmt a = (AssignStmt) stmt;
	    Value r = a.getRightOp();
	    ret = addValue( r, method, stmt );
	}
	stmtToReadSet.put( stmt, ret );
	return ret;
    }

    HashMap stmtToWriteSet = new HashMap();
    public RWSet writeSet( SootMethod method, Stmt stmt ) {
	RWSet ret = (RWSet) stmtToWriteSet.get( stmt );
	if( ret != null ) return ret;
	if( stmt.containsInvokeExpr() ) {
	    for( Iterator targets = ig.getTargetsOf( stmt ).iterator();
		    targets.hasNext(); ) {
		SootMethod target = (SootMethod) targets.next();
		if( ret == null ) ret = new MethodRWSet();
		if( !target.isConcrete() ) continue;
		findRWSetsForMethod( target );
		ret.union( (RWSet) methodToWriteSet.get( target ) );
	    }
	}
	if( stmt instanceof AssignStmt ) {
	    AssignStmt a = (AssignStmt) stmt;
	    Value l = a.getLeftOp();
	    ret = addValue( l, method, stmt );
	}
	stmtToWriteSet.put( stmt, ret );
	return ret;
    }

    protected RWSet addValue( Value v, SootMethod m, Stmt s ) {
	RWSet ret = null;
	if( v instanceof InstanceFieldRef ) {
	    InstanceFieldRef ifr = (InstanceFieldRef) v;
	    ObjectSet base = pa.reachingObjects( m, s, (Local) ifr.getBase() );
	    ret = new StmtRWSet();
	    ret.addFieldRef( base, ifr.getField() );
	} else if( v instanceof StaticFieldRef ) {
	    StaticFieldRef sfr = (StaticFieldRef) v;
	    ret = new StmtRWSet();
	    ret.addGlobal( sfr.getField() );
	} else if( v instanceof ArrayRef ) {
	    ArrayRef ar = (ArrayRef) v;
	    ObjectSet base = pa.reachingObjects( m, s, (Local) ar.getBase() );
	    ret = new StmtRWSet();
	    ret.addFieldRef( base, PointerAnalysis.ARRAY_ELEMENTS_NODE );
	}
	return ret;
    }
}

