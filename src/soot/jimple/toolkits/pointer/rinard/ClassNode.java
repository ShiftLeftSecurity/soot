package soot.jimple.toolkits.pointer.rinard;
import soot.jimple.*;
import soot.*;

class ClassNode extends Node {
    protected SootClass c;
    ClassNode( SootClass _c ) {
	c = _c;
    }
    public SootClass getSootClass() {
	return c;
    }
    public String toString() {
	return "C:"+c.getName();
    }
}
