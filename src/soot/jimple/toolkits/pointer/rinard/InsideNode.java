package soot.jimple.toolkits.pointer.rinard;
import soot.*;
import soot.jimple.*;

class InsideNode extends Node {
    protected Expr e; // a NewExpr or a NewArrayExpr
    InsideNode( Expr newE ) {
	e = newE;
    }
    public Expr getNewExpr() {
	return e;
    }
    public String toString() {
	return "I:"+e;
    }
    String dotGraphOptions() {
	return " style=filled fillcolor=grey ";
    }
}
