package soot.jimple.toolkits.pointer.rinard;
import soot.jimple.*;
import soot.*;

class LoadNode extends Node {
    protected AssignStmt s;
    LoadNode( AssignStmt _s ) {
	s = _s;
    }
    public AssignStmt getStmt() {
	return s;
    }
    public String toString() {
	return "L:"+s.toBriefString();
    }
}
