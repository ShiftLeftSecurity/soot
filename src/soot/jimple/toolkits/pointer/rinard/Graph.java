package soot.jimple.toolkits.pointer.rinard;
import java.util.*;
import soot.*;

class Graph {
    class IntegerBox {
	int count = 1;
	int getCount() { return count; }
	void inc() { count++; }
	void dec() { count--; }
    }
    NodeManager nm;
    protected EdgeSet insideEdges;
    protected EdgeSet outsideEdges;
    protected Set escapeCausingNodes;
    protected Set escapedNodes;
    boolean escapedNodesUpToDate;
    protected Set returnedNodes;
    /** If mutableCount is 1, the graph is mutable; otherwise, it is the total number
     * of graphs created that share data with this one. */
    IntegerBox mutableCount;

    Graph()
    {
	mutableCount = new IntegerBox();
	escapedNodesUpToDate = true;
    }
    void copyTo( Graph to )
    {
	to.mutableCount = mutableCount;
	mutableCount.inc();
	to.insideEdges = insideEdges;
	to.outsideEdges = outsideEdges;
	to.escapeCausingNodes = escapeCausingNodes;
	to.escapedNodes = escapedNodes;
	to.returnedNodes = returnedNodes;
	to.escapedNodesUpToDate = false;
	escapedNodesUpToDate = false;
    }
    protected void ensureMutability()
    {
	if( insideEdges == null ) {
	    insideEdges = new EdgeSet();
	    outsideEdges = new EdgeSet();
	    escapeCausingNodes = new HashSet(0);
	    escapedNodes = new HashSet(0);
	    returnedNodes = new HashSet(0);
	    mutableCount = new IntegerBox();
	} else if( mutableCount.getCount() > 1 ) {
	    insideEdges = new EdgeSet( insideEdges );
	    outsideEdges = new EdgeSet( outsideEdges );
	    escapeCausingNodes = new HashSet( escapeCausingNodes );
	    escapedNodes = new HashSet( escapedNodes );
	    returnedNodes = new HashSet( returnedNodes );
	    mutableCount.dec();
	    mutableCount = new IntegerBox();
	}
	escapedNodesUpToDate = false;
    }
    public String toString()
    {
	updateEscaped();
	return
	"Inside edges:\n" + insideEdges +
	"\nOutside edges:\n" + outsideEdges +
	"\nEscaped nodes:\n" + escapedNodes +
	"\nReturned nodes:\n" + returnedNodes +
	"\n";
    }
    public void mergeFrom( Graph g ) {
	if( g.insideEdges == null ) return;
	ensureMutability();
	insideEdges.mergeFrom( g.insideEdges );
	outsideEdges.mergeFrom( g.outsideEdges );
	escapeCausingNodes.addAll( g.escapeCausingNodes );
	escapedNodes.addAll( g.escapedNodes );
	returnedNodes.addAll( g.returnedNodes );
    }
    void addIEdges( Local from, Set to )
    {
	if( getITargetsFor( from ).containsAll( to ) ) return;
	ensureMutability();
	insideEdges.addEdges( from, to );
    }
    void addIEdges( NodeXField from, Set to )
    {
	if( getITargetsFor( from ).containsAll( to ) ) return;
	ensureMutability();
	insideEdges.addEdges( from, to );
    }
    void addIEdge( Local from, Node to )
    {
	if( getITargetsFor( from ).contains( to ) ) return;
	ensureMutability();
	insideEdges.addEdge( from, to );
    }
    void addIEdge( NodeXField from, Node to )
    {
	if( getITargetsFor( from ).contains( to ) ) return;
	ensureMutability();
	insideEdges.addEdge( from, to );
    }
    void addOEdges( NodeXField from, Set to )
    {
	if( getOTargetsFor( from ).containsAll( to ) ) return;
	ensureMutability();
	outsideEdges.addEdges( from, to );
    }
    void addOEdge( NodeXField from, Node to )
    {
	if( getOTargetsFor( from ).contains( to ) ) return;
	ensureMutability();
	outsideEdges.addEdge( from, to );
    }
    void killEdgesFrom( Local n ) {
	if( getITargetsFor( n ).isEmpty() ) return;
	ensureMutability();
	insideEdges.killEdgesFrom( n );
	outsideEdges.killEdgesFrom( n );
    }
    Set getINodeXFields( Node n ) {
	if( insideEdges == null ) return Collections.EMPTY_SET;
	return insideEdges.getNodeXFields( n );
    }
    Set getONodeXFields( Node n ) {
	if( insideEdges == null ) return Collections.EMPTY_SET;
	return outsideEdges.getNodeXFields( n );
    }
    Set getITargetsFor( Local n ) {
	if( insideEdges == null ) return Collections.EMPTY_SET;
	return insideEdges.getTargetsFor( n );
    }
    Set getITargetsFor( NodeXField n ) {
	if( insideEdges == null ) return Collections.EMPTY_SET;
	return insideEdges.getTargetsFor( n );
    }
    Set getOTargetsFor( NodeXField n ) {
	if( insideEdges == null ) return Collections.EMPTY_SET;
	return outsideEdges.getTargetsFor( n );
    }
    Set reachableNodes( Set from ) {
	HashSet ret = new HashSet( from );
	LinkedList q = new LinkedList( from );
	while( !q.isEmpty() ) {
	    Node n = (Node) q.removeFirst();
	    HashSet reachables = new HashSet( outsideEdges.getTargetsFor( n ) );
	    reachables.addAll( insideEdges.getTargetsFor( n ) );
	    reachables.removeAll( ret );
	    q.addAll( reachables );
	    ret.addAll( reachables );
	}
	return ret;
    }
    private void updateInherentlyEscapedNodes( Set in ) {
	Iterator it = in.iterator();
	while( it.hasNext() ) {
	    Object o = it.next();
	    if( o instanceof ParameterNode
		|| o instanceof ReturnNode
		|| o instanceof ClassNode ) escapedNodes.add( o );
	}
    }
    private void updateEscaped() {
	if( !escapedNodesUpToDate ) {
	    updateInherentlyEscapedNodes( insideEdges.getAllNodes () );
	    updateInherentlyEscapedNodes( outsideEdges.getAllNodes () );
	    escapedNodes.addAll( escapeCausingNodes );
	    escapedNodes.addAll( reachableNodes( escapedNodes ) );
	    escapedNodesUpToDate = true;
	}
    }
    boolean isEscapeCausing( Node n ) {
	if( insideEdges == null ) return false;
	return escapeCausingNodes.contains( n );
    }
    boolean hasEscaped( Node n ) {
	if( insideEdges == null ) return false;
	if( escapedNodes.contains( n ) ) return true;
	if( escapeCausingNodes.contains( n ) ) return true;
	updateInherentlyEscapedNodes( insideEdges.getAllNodes () );
	updateInherentlyEscapedNodes( outsideEdges.getAllNodes () );
	if( escapeCausingNodes.contains( n ) ) return true;
	updateEscaped();
	return escapedNodes.contains( n );
    }
    void makeEscape( Node n ) {
	ensureMutability();
	escapeCausingNodes.add( n );
    }
    void makeEscape( Set c ) {
	ensureMutability();
	escapeCausingNodes.addAll( c );
    }
    void makeReturned( Set c ) {
	ensureMutability();
	returnedNodes.addAll( c );
    }
    public boolean isReturned( Node n ) {
	if( returnedNodes == null ) return false;
	return returnedNodes.contains( n );
    }
    public Set getReturned() {
	if( returnedNodes == null ) return Collections.EMPTY_SET;
	return returnedNodes;
    }
    public boolean equals( Object o ) {
	if( !( o instanceof Graph ) ) return false;
	Graph g = (Graph) o;
	if( insideEdges == null ) return g.insideEdges == null;
	if( g.insideEdges == null ) return false;
	return insideEdges.equals( g.insideEdges )
	&& outsideEdges.equals( g.outsideEdges )
	&& escapeCausingNodes.equals( g.escapeCausingNodes )
	&& returnedNodes.equals( g.returnedNodes );
    }
    public String toDotGraph() {
	return "digraph G {\nrankdir=LR;\nrotate=90;\n"
	+dotGraphGuts("","","","")+"}\n";

    }
    public String dotGraphGuts( String nodeOptions, String localPrefix, String localOptions, String edgeOptions ) {
	if( insideEdges == null ) return "";
	StringBuffer ret = new StringBuffer();
	updateEscaped();
	Set nodes = insideEdges.getAllNodes();
	nodes.addAll( outsideEdges.getAllNodes() );
	Iterator it = nodes.iterator();
	while( it.hasNext() ) {
	    Node n = (Node) it.next();
	    String options = "";
	    if( returnedNodes.contains( n ) ) {
		options = options + " peripheries=2 ";
	    }
	    if( hasEscaped( n ) ) {
		options = options + " shape=box ";
	    }
	    options = options + nodeOptions;
	    ret.append( n.toDotGraph( options ) );
	}
	nodes = insideEdges.getAllLocals();
	it = nodes.iterator();
	while( it.hasNext() ) {
	    Local l = (Local) it.next();
	    String name = l.getName();
	    if( name.charAt(0) == '$' ) {
		if( !Rinard.printTempLocals ) continue;
		name = name.substring(1);
	    }
	    ret.append( localPrefix+name + " [ shape=plaintext "+localOptions+" ];\n"  );
	}
	return ret.toString()
	    +insideEdges.toDotGraph( edgeOptions, localPrefix ) 
	    +outsideEdges.toDotGraph( "style=dashed "+edgeOptions, localPrefix );
    }
}

