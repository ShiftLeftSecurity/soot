package soot.jimple.toolkits.pointer.rinard;
import java.util.*;
import soot.*;
import soot.util.*;

class EdgeSet {
    protected MultiMap sourceToTargetSet;
    protected MultiMap nodeToTargetSet;
    protected MultiMap nodeToNodeXFieldSet;
    EdgeSet() {
	sourceToTargetSet = new HashMultiMap();
	nodeToTargetSet = new HashMultiMap();
	nodeToNodeXFieldSet = new HashMultiMap();
    }
    EdgeSet( EdgeSet e ) {
	sourceToTargetSet = new HashMultiMap( e.sourceToTargetSet );
	nodeToTargetSet = new HashMultiMap( e.nodeToTargetSet );
	nodeToNodeXFieldSet = new HashMultiMap( e.nodeToNodeXFieldSet );
    }
    void mergeFrom( EdgeSet e ) {
	sourceToTargetSet.putAll( e.sourceToTargetSet );
	nodeToTargetSet.putAll( e.nodeToTargetSet );
	nodeToNodeXFieldSet.putAll( e.nodeToNodeXFieldSet );
    }
    public void addEdge( Object from, Object to )
    {
	sourceToTargetSet.put( from, to );
	if( from instanceof NodeXField ) {
	    NodeXField nf = (NodeXField) from;
	    nodeToTargetSet.put( nf.node, to );
	    nodeToNodeXFieldSet.put( nf.node, nf );
	}
    }
    public void addEdges( Object from, Set to )
    {
	sourceToTargetSet.putAll( from, to );
	if( from instanceof NodeXField ) {
	    NodeXField nf = (NodeXField) from;
	    nodeToTargetSet.putAll( nf.node, to );
	    nodeToNodeXFieldSet.put( nf.node, nf );
	}
    }
    public void killEdgesFrom( Object from )
    {
	sourceToTargetSet.remove( from );
	if( from instanceof NodeXField ) {
	    nodeToTargetSet.remove( ((NodeXField) from).node );
	}
    }
    public Set getNodeXFields( Node from ) {
	return nodeToNodeXFieldSet.get( from );
    }
    public Set getTargetsFor( Local from )
    {
	return sourceToTargetSet.get( from );
    }
    public Set getTargetsFor( NodeXField from )
    {
	return sourceToTargetSet.get( from );
    }
    public Set getTargetsFor( Node from )
    {
	return nodeToTargetSet.get( from );
    }
    public Set getAllNodes()
    {
	Set ret = new HashSet( nodeToTargetSet.keySet() );
	ret.addAll( sourceToTargetSet.values() );
	return ret;
    }
    public Set getAllLocals()
    {
	Set ret = new ArraySet();
	Iterator it = sourceToTargetSet.keySet().iterator();
	while( it.hasNext() ) {
	    Object o = it.next();
	    if( o instanceof Local ) {
		ret.add( o );
	    }
	}
	return ret;
    }
    public boolean equals( Object o ) {
	if( !( o instanceof EdgeSet ) ) return false;
	EdgeSet e = (EdgeSet) o;
	return sourceToTargetSet.equals( e.sourceToTargetSet )
	&& nodeToTargetSet.equals( e.nodeToTargetSet );
    }
    public int hashCode() {
	return sourceToTargetSet.hashCode() + nodeToTargetSet.hashCode();
    }
    public String toDotGraph( String edgeopts, String localPrefix ) {
	StringBuffer ret = new StringBuffer("");
	Iterator it = sourceToTargetSet.keySet().iterator();
	while( it.hasNext() ) {
	    Object o = it.next();
	    Iterator it2 = sourceToTargetSet.get( o ).iterator();
	    while( it2.hasNext() ) {
		Object o2 = it2.next();
		if( o instanceof NodeXField ) {
		    NodeXField nf = (NodeXField) o;
		    ret.append( ""+nf.node.getId() );
		    ret.append( " -> " );
		    ret.append( ""+((Node)o2).getId() );
		    ret.append( " [ " );
		    if( nf.field == null ) {
			ret.append( "label=\"[]\" " );
		    } else {
			ret.append( "label=\"" + nf.field.getName()+ "\" " );
		    }
		    ret.append( edgeopts );
		    ret.append( " ] ;" );
		    ret.append( "\n" );
		} else {
		    String name = o.toString();
		    if( name.charAt(0) == '$' ) {
			if( !Rinard.printTempLocals ) continue;
			name = name.substring(1);
		    }
		    ret.append( localPrefix+name );
		    ret.append( " -> " );
		    ret.append( ""+((Node)o2).getId() );
		    ret.append( " [ " );
		    ret.append( edgeopts );
		    ret.append( " ] ;" );
		    ret.append( "\n" );
		}
	    }
	}
	return ret.toString();
    }
    public String toString() {
	StringBuffer ret = new StringBuffer("");
	Iterator it = sourceToTargetSet.keySet().iterator();
	while( it.hasNext() ) {
	    Object o = it.next();
	    Iterator it2 = sourceToTargetSet.get( o ).iterator();
	    while( it2.hasNext() ) {
		Object o2 = it2.next();
		ret.append( o.toString() );
		ret.append( " -> " );
		ret.append( o2.toString() );
		ret.append( "\n" );
	    }
	}
	return ret.toString();
    }
}
