package soot.jimple.toolkits.pointer;
import soot.toolkits.graph.*;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.*;
import java.util.*;
import soot.util.*;
import java.io.*;
import soot.jimple.toolkits.pointer.util.*;

public class NodePPG extends PointerPropagationGraph
{
    int countSimples, countLoads, countStores, countNews;
    protected MultiMap simple = new HashMultiMap();
    protected MultiMap loads = new HashMultiMap();
    protected MultiMap stores = new HashMultiMap();
    protected MultiMap news = new HashMultiMap();
    protected Map ebbMap = null;

    public MultiMap getSimple() { return simple; }
    public MultiMap getLoads() { return loads; }
    public MultiMap getStores() { return stores; }
    public MultiMap getNews() { return news; }
    public void addAnyEdge( Object o, Object p ) {
	throw new RuntimeException( "Forgot to override something" );
    }
    public void addSimpleEdge( 
        Object src, Type srcType, 
        Object dest, Type destType )
    {
	VarNode from = VarNode.v( src, srcType, currentMethod ); 
	VarNode to = VarNode.v( dest, destType, currentMethod );

	addSimpleEdge( from, to );
    }
    public void addSimpleEdge( VarNode src, VarNode dest )
    {
	if( simple.put( src, dest ) ) {
	    countSimples++;
	    dest.edgesIn++;
	}
    }
    public void addLoadEdge( 
        Object base, Type baseType,
        Object field, Type fieldType, 
        Object dest, Type destType ) 
    {
	VarNode to = VarNode.v( dest, destType, currentMethod  );

	addLoadEdge(
		FieldRefNode.v( 
		    VarNode.v( base, baseType, currentMethod  ), 
		    field, fieldType, currentMethod  ), 
		to );
    }
    public void addLoadEdge( FieldRefNode src, VarNode dest ) {
	if( loads.put( src, dest ) ) {
	    countLoads++;
	    dest.edgesIn++;
	}
    }

    public void addStoreEdge( 
        Object src, Type srcType,
        Object base, Type baseType,
        Object field, Type fieldType ) 
    {
	VarNode from = VarNode.v( src, srcType, currentMethod  );
	addStoreEdge( from,
		FieldRefNode.v( 
		    VarNode.v( base, baseType, currentMethod  ),
		    field, fieldType, currentMethod  ) );
    }
    public void addStoreEdge( VarNode src, FieldRefNode dest ) {
	if( stores.put( src, dest ) ) {
	    countStores++;
	}
    }
    public void addNewEdge( 
        Object newExpr, Type newExprType, 
        Object dest, Type destType ) 
    {
	VarNode to = VarNode.v( dest, destType, currentMethod  );
	addNewEdge( 
		AllocNode.v( newExpr, newExprType, currentMethod ),
		to );
    }
    public void addNewEdge( AllocNode src, VarNode dest ) {
	if( news.put( src, dest ) ) { 
	    countNews++;
	    dest.edgesIn++;
	}
    }
    public NodePPG( InvokeGraph ig ) {
	super( ig );
    }
    public void build() {
	super.build();
	System.out.println( "News:   "+countNews );
	System.out.println( "Simple: "+countSimples );
	System.out.println( "Loads:  "+countLoads );
	System.out.println( "Stores: "+countStores );
    }

    public void addLocalAfterLoad() {
	for( Iterator loadsIt = new LinkedList( loads.keySet() ).iterator();
		loadsIt.hasNext(); ) {
	    FieldRefNode n = (FieldRefNode) loadsIt.next();
	    VarNode target = VarNode.v( n, n.getType(), n.m );
	    simple.putAll( target, loads.get( n ) );
	    loads.remove( n );
	    loads.put( n, target );
	}
    }
    public void collapseEBBs( FastHierarchy fh ) {
	ebbMap = new HashMap();
	for( Iterator froms = VarNode.nodeMap.values().iterator(); froms.hasNext(); ) {
	    VarNode from = (VarNode) froms.next();
	    if( ebbMap.containsKey( from ) ) continue;
	    if( !simple.containsKey( from ) ) continue;
	    Set fromSuccessors = simple.get( from );
	    boolean changes;
	    do {
		changes = false;
		for( Iterator tos = new LinkedList( fromSuccessors ).iterator(); tos.hasNext(); ) {
		    VarNode to = (VarNode) tos.next();
		    if( to.edgesIn != 1 ) continue;
		    if( to.equals( from ) ) continue;
		    Type toType = to.getType();
		    Type fromType = from.getType();
		    if( !toType.equals( fromType ) ) {
			if( toType instanceof RefType && fromType instanceof RefType ) {
			    if( fh != null && !fh. 
				canStoreType( fromType, toType ) ) continue;
			} else continue;
		    }

		    // collapse to into from
		    changes = true;
		    if( simple.containsKey( to ) ) {
			Set toSuccessors = simple.get( to );
			for( Iterator it = fromSuccessors.iterator(); it.hasNext(); ) {
			    VarNode node = (VarNode) it.next();
			    if( toSuccessors.contains( node ) ) {
				node.edgesIn--;
			    }
			}
			fromSuccessors.addAll( toSuccessors );
		    }
		    fromSuccessors.remove( to );

		    ebbMap.put( to, from );
		}
	    } while( changes );
	}
	for( Iterator it = new LinkedList( loads.keySet() ).iterator(); it.hasNext(); ) {
	    FieldRefNode n = (FieldRefNode) it.next();
	    Set targets = loads.get( n );
	    for( Iterator targIt = new LinkedList( targets ).iterator(); targIt.hasNext(); ) {
		VarNode target = (VarNode) targIt.next();
		VarNode forwardedTarget = (VarNode) ebbMap.get( target );
		if( forwardedTarget != null ) {
		    targets.remove( target );
		    targets.add( forwardedTarget );
		}
	    }
	    VarNode forwardedNode = (VarNode) ebbMap.get( n.getBase() );
	    if( forwardedNode != null ) {
		loads.putAll( FieldRefNode.v( forwardedNode, n.field, n.type, n.m ), targets );
		loads.remove( n );
	    }
	}
	for( Iterator it = new LinkedList( stores.keySet() ).iterator(); it.hasNext(); ) {
	    VarNode from = (VarNode) it.next();
	    Set s = stores.get( from );
	    for( Iterator it2 = new ArrayList( s ).iterator(); it2.hasNext(); ) {
		FieldRefNode n = (FieldRefNode) it2.next();
		VarNode forwardedNode = (VarNode) ebbMap.get( n.getBase() );
		if( forwardedNode == null ) continue;
		s.add( FieldRefNode.v( forwardedNode, n.field, n.type, n.m ) );
		s.remove( n );
	    }
	    VarNode forwardedNode = (VarNode) ebbMap.get( from );
	    if( forwardedNode == null ) continue;
	    stores.putAll( forwardedNode, s );
	    stores.remove( from );
	}
	System.out.println( "Total locals: "+VarNode.nodeMap.size() );
	System.out.println( "Total EBBs: "+( VarNode.nodeMap.size() - ebbMap.keySet().size() ) );
    }

    private void dumpEdgeSet( PrintStream out, MultiMap m ) {
	out.println( m.keySet().size() );
	for( Iterator it = m.keySet().iterator(); it.hasNext(); ) {
	    Node n = (Node) it.next();
	    out.print( n.id );
	    for( Iterator it2 = m.get(n).iterator(); it2.hasNext(); ) {
		Node n2 = (Node) it2.next();
		out.print( " "+n2.id );
	    }
	    out.println();
	}
    }
    public void compute() {
	PrintStream out = null;
	try {
	    out = new PrintStream( new FileOutputStream( "pag" ) );
	} catch( FileNotFoundException e ) {
	    return;
	}
	dumpEdgeSet( out, simple );
	dumpEdgeSet( out, loads );
	dumpEdgeSet( out, stores );
	dumpEdgeSet( out, news );
	out.println( VarNode.nodeMap.size() );
	for( Iterator it = VarNode.getAll().iterator(); it.hasNext(); ) {
	    VarNode n = (VarNode) it.next();
	    out.print( n.id );
	    for( Iterator it2 = n.getAllFieldRefs().iterator(); it2.hasNext(); ) {
		FieldRefNode n2 = (FieldRefNode) it2.next();
		out.print( " "+n2.id );
	    }
	    out.println();
	}
    }
    public void dumpStats() {
    }
    public void buildNative( SootMethod m ) {
	ValNode thisNode = null;
	ValNode retNode = null;
	if( !m.isStatic() ) {
	    thisNode = VarNode.v( new Pair( m, PointerAnalysis.THIS_NODE ),
		    m.getDeclaringClass().getType(), m );
	}
	if( isType( m.getReturnType() ) ) {
	    if( !returnsAsFields ) {
		retNode = VarNode.v( new Pair( m, PointerAnalysis.RETURN_NODE ),
			m.getReturnType(), m );
	    } else {
		retNode = FieldRefNode.v(
			VarNode.v( 
			    new Pair( m, PointerAnalysis.THIS_NODE ),
			    m.getDeclaringClass().getType(),
			    m ),

			new Pair( m.getSubSignature(),
			    PointerAnalysis.RETURN_NODE ),
			m.getReturnType(),
			m
			);
	    }
	}
	ValNode[] args = new ValNode[ m.getParameterCount() ];
	for( int i = 0; i < m.getParameterCount(); i++ ) {
	    if( !isType( m.getParameterType(i) ) ) continue;
	    if( !parmsAsFields ) {
		args[i] = VarNode.v(new Pair( m, new Integer(i) ), m.getParameterType(i), m );
	    } else {
		args[i] = FieldRefNode.v(
			VarNode.v( new Pair( m, PointerAnalysis.THIS_NODE ),
			    m.getDeclaringClass().getType(), m ),
			new Pair( m.getSubSignature(), new Integer( i ) ),
			m.getParameterType(i),
			m
			);
	    }
	}
	NativeMethodDriver.process( m, thisNode, retNode, args );
    }
}

