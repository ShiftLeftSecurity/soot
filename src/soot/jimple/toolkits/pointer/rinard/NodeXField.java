package soot.jimple.toolkits.pointer.rinard;
import soot.*;

class NodeXField {
    Node node;
    SootField field; // null if it's an array reference
    NodeXField( Node n, SootField f ) {
	node = n;
	field = f;
    }
    public boolean equals( Object o ) {
	if( !(o instanceof NodeXField ) ) return false;
	NodeXField nf = (NodeXField) o;
	if( field == null ) {
	    if( nf.field != null ) return false;
	    return node.equals( nf.node );
	}
	if( nf.field == null ) return false;
	return node.equals( nf.node ) && field.equals( nf.field );
    }
    public int hashCode() {
	if( field == null ) return node.hashCode();
	return node.hashCode() + field.hashCode();
    }
    public String toString() {
	if( field == null ) {
	    return "("+node.toString()+")[]";
	}
	return "("+node.toString()+")."+field.getName();
    }
}

