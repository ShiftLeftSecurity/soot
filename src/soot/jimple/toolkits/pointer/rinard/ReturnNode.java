package soot.jimple.toolkits.pointer.rinard;
import soot.jimple.*;
import soot.*;

class ReturnNode extends Node {
    protected Stmt s;
    ReturnNode( Stmt _s ) {
	s = _s;
    }
    public Stmt getStmt() {
	return s;
    }
    public String toString() {
	return "R:"+( (InvokeExpr) s.getInvokeExpr()).toBriefString();
    }
}
