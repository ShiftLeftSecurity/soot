package soot.jimple.toolkits.pointer.rinard;
import java.util.*;
import soot.toolkits.scalar.*;
import soot.toolkits.graph.*;
import soot.toolkits.*;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.io.*;

class NodeManager {
    Map nodeMap = new HashMap(0);

    protected Node findNode( Object o ) {
	Node newNode = (Node) nodeMap.get( o );
	if( newNode == null ) {
	    if( o instanceof NewExpr 
	    || o instanceof NewArrayExpr
	    || o instanceof NewMultiArrayExpr ) {
		newNode = new InsideNode( (Expr) o );
	    } else if( o instanceof Stmt ) {
		Stmt s = (Stmt) o;
		if( s.containsInvokeExpr() ) {
		    newNode = new ReturnNode( s );
		} else {
		    newNode = new LoadNode( (AssignStmt) s );
		}
	    } else if( o instanceof IdentityRef ) {
		newNode = new ParameterNode( (IdentityRef) o );
	    } else if( o instanceof SootClass ) {
		newNode = new ClassNode( (SootClass) o );
	    } else throw new RuntimeException( "Unhandled value type "+o );
	    nodeMap.put( o, newNode );
	    return newNode;
	}
	return newNode;
    }
}

