package soot.jimple.toolkits.pointer.rinard;
import soot.jimple.*;
import soot.*;

class ParameterNode extends Node {
    protected IdentityRef p;
    ParameterNode( IdentityRef _p ) {
	p = _p;
    }
    public IdentityRef getIdentityRef() {
	return p;
    }
    public String toString() {
	return "P:"+((ToBriefString)p).toBriefString();
    }
}
