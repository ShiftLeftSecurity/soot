package soot.jimple.toolkits.pointer;
import java.util.*;
import soot.*;

/** Represents the read or write set of a statement. */
public class StmtRWSet extends RWSet {
    protected Object field;
    protected ObjectSet base;
    protected boolean callsNative = false;

    public boolean getCallsNative() {
	return callsNative;
    }

    public boolean setCallsNative() {
	boolean ret = !callsNative;
	callsNative = true;
	return ret;
    }

    /** Returns an iterator over any globals read/written. */
    public Set getGlobals() {
	if( base == null ) {
	    HashSet ret = new HashSet();
	    ret.add( field );
	    return ret;
	}
	return Collections.EMPTY_SET;
    }

    /** Returns an iterator over any fields read/written. */
    public Set getFields() {
	if( base != null ) {
	    HashSet ret = new HashSet();
	    ret.add( field );
	    return ret;
	}
	return Collections.EMPTY_SET;
    }

    /** Returns a set of base objects whose field f is read/written. */
    public ObjectSet getBaseForField( Object f ) {
	if( field.equals( f ) ) return base;
	return null;
    }

    public boolean hasNonEmptyIntersection( RWSet other ) {
	if( field == null ) return false;
	if( other instanceof StmtRWSet ) {
	    StmtRWSet o = (StmtRWSet) other;
	    if( !field.equals( o.field ) ) return false;
	    if( base == null ) return o.base == null;
	    return base.equals( o.base );
	}
	MethodRWSet o = (MethodRWSet) other;
	if( base == null ) return other.getGlobals().contains( field );
	return base.hasNonEmptyIntersection( other.getBaseForField( field ) );
    }

    /** Adds the RWSet other into this set. */
    public boolean union( RWSet other ) {
	throw new RuntimeException( "Can't do that" );
    }

    public boolean addGlobal( SootField global ) {
	if( field != null || base != null ) 
	    throw new RuntimeException( "Can't do that" );
	field = global;
	return true;
    }
    public boolean addFieldRef( ObjectSet otherBase, Object field ) {
	if( this.field != null || base != null ) 
	    throw new RuntimeException( "Can't do that" );
	this.field = field;
	base = otherBase;
	return true;
    }
}
