package soot.jimple.toolkits.pointer.rinard;
import java.util.*;
import soot.toolkits.scalar.*;
import soot.toolkits.graph.*;
import soot.toolkits.*;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.io.*;

class InterProc {
    static int graphNum = 0;
    HashSet d = new HashSet(0);
    MultiMap mu = new HashMultiMap();
    Graph m;
    Graph r;
    MultiMap delta = new HashMultiMap();
    Set we = new HashSet(0);
    Set wi = new HashSet(0);
    Set wo = new HashSet(0);
    class NodePair {
	Node n1;
	Node n2;
	NodePair( Node n1, Node n2 ) { this.n1 = n1; this.n2 = n2; }
	public int hashCode() { return n1.hashCode() + n2.hashCode(); }
	public boolean equals( Object o ) {
	    if( ! (o instanceof NodePair) ) return false;
	    NodePair np = (NodePair) o;
	    if( !n1.equals( np.n1 ) ) return false;
	    if( !n2.equals( np.n2 ) ) return false;
	    return true;
	}
    }

    class WorkListEntry {
	Node n3;
	NodeXField nf;
	Node n2;
	WorkListEntry( Node n3, NodeXField nf, Node n2 ) {
	    this.n3 = n3; this.nf = nf; this.n2 = n2;
	}
	public int hashCode() { return n3.hashCode() + nf.hashCode() + n2.hashCode(); }
	public boolean equals( Object o ) {
	    if( ! ( o instanceof WorkListEntry ) ) return false;
	    WorkListEntry wle = (WorkListEntry) o;
	    if( !n3.equals( wle.n3 ) ) return false;
	    if( !n2.equals( wle.n2 ) ) return false;
	    if( !nf.equals( wle.nf ) ) return false;
	    return true;
	}
    }

    protected void mapNode( Node n1, Set n ) {
	for( Iterator it = n.iterator(); it.hasNext(); ) {
	    mapNode( n1, (Node) it.next() );
	}
    }
    /** mapNode from Figure 10, Whaley and Rinard, OOPSLA 99 */
    protected void mapNode( Node n1, Node n ) {
	// if <n1,n> \notin D then
	if( d.contains( new NodePair( n1, n ) ) ) return;

	// \mu(n1) = \mu(n1) \cup \{n\}
	mu.put( n1, n );


	// D = D \cup \{ <n1,n> \}
	d.add( new NodePair( n1, n ) );

	// if( e_R(n1) \neq 0 ) then e_M(n) = e_M(n) \cup \{m\}
	if( r.isEscapeCausing( n1 ) ) {
	    m.makeEscape( n );
	}

	// I_M = I_M \cup ( \delta(n1) \times \{n\}
	for( Iterator it = delta.get(n1).iterator(); it.hasNext(); ) {
	    NodeXField nf = (NodeXField) it.next();
	    m.addIEdge( nf, n );
	}

	// forall <<n1,f>,n2> \in I_R do
	//   I_M = I_M \cup \{<n,f>} \times \mu(n2)
	//   \delta(n2) = \delta(n2) \cup {<n,f>}
	for( Iterator it = r.getINodeXFields( n1 ).iterator(); it.hasNext(); ) {
	    NodeXField nf = (NodeXField) it.next();
	    for( Iterator it2 = r.getITargetsFor( nf ).iterator(); it2.hasNext(); ) {
		Node n2 = (Node) it2.next();
		NodeXField nf2 = new NodeXField( n, nf.field );
		m.addIEdges( nf2, mu.get( n2 ) );
		delta.put( n2, nf2 );
	    }
	}

	// W_E = W_E \cup ( {n} \times edgesFrom( O_R, n1 ) )
	for( Iterator it = r.getONodeXFields( n1 ).iterator(); it.hasNext(); ) {
	    NodeXField nf = (NodeXField) it.next();;
	    for( Iterator it2 = r.getOTargetsFor( nf ).iterator(); it2.hasNext(); ) {
		we.add( new WorkListEntry( n, nf, (Node) it2.next() ) );
	    }
	}

	// if( n1 == n ) then
	//    W_I = W_I \cup ( {n} \times edgesFrom( I_R, n1 ) )
	//    W_O = W_O \cup ( {n} \times edgesFrom( O_R, n1 ) )
	if( n1.equals( n ) ) {
	    for( Iterator it = r.getINodeXFields( n1 ).iterator(); it.hasNext(); ) {
		NodeXField nf = (NodeXField) it.next();
		for( Iterator it2 = r.getITargetsFor( nf ).iterator(); it2.hasNext(); ) {
		    wi.add( new WorkListEntry( n, nf, (Node) it2.next() ) );
		}
	    }
	    for( Iterator it = r.getONodeXFields( n1 ).iterator(); it.hasNext(); ) {
		NodeXField nf = (NodeXField) it.next();
		for( Iterator it2 = r.getOTargetsFor( nf ).iterator(); it2.hasNext(); ) {
		    wo.add( new WorkListEntry( n, nf, (Node) it2.next() ) );
		}
	    }
	}
    }
    public void go( Local ret, InvokeExpr ie, SootMethod callee, Graph orig,
	Graph calleeGraph ) {
	r = calleeGraph;
	m = new Graph();
	orig.copyTo( m );
	Iterator it = calleeGraph.nm.nodeMap.entrySet().iterator();
	while( it.hasNext() ) {
	    Map.Entry e = (Map.Entry) it.next();
	    if( e.getKey() instanceof ParameterRef ) {
		Value v = ie.getArg( ((ParameterRef) e.getKey()).getIndex() );
		if( v instanceof Local ) 
		    mapNode( (Node) e.getValue(), 
			orig.getITargetsFor( (Local) v ) );
	    } else if( e.getKey() instanceof ThisRef ) {
		InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
		Value v = iie.getBase();
		mapNode( (Node) e.getValue(),
		    orig.getITargetsFor( (Local) v ) );
	    } else if( e.getKey() instanceof SootClass ) {
		mapNode( (Node) e.getValue(), (Node) e.getValue() );
	    }
	    if( e.getValue() instanceof InsideNode
	    || e.getValue() instanceof ReturnNode ) {
		if( r.isReturned( (Node) e.getValue() ) ) {
		    mapNode( (Node) e.getValue(), (Node) e.getValue() );
		}
	    }
	}
	while( true ) {
	    WorkListEntry wle;
	    if( !we.isEmpty() ) {
		wle = (WorkListEntry) we.iterator().next();
		we.remove( wle );
		if( wle.nf.node instanceof InsideNode ) continue;
		mapNode( wle.n2, orig.getITargetsFor(
		    new NodeXField( wle.n3, wle.nf.field ) ) );
	    } else if( !wi.isEmpty() ) {
		wle = (WorkListEntry) wi.iterator().next();
		wi.remove( wle );
		if( ! (wle.n2 instanceof InsideNode )
		&&  ! (wle.n2 instanceof ReturnNode ) ) continue;
		mapNode( wle.n2, wle.n2 );
	    } else if( !wo.isEmpty() ) {
		wle = (WorkListEntry) wo.iterator().next();
		wo.remove( wle );
		if( !m.hasEscaped( wle.n3 ) ) continue;
		m.addOEdge( new NodeXField( wle.n3, wle.nf.field ), wle.n2 );
		mapNode( wle.n2, wle.n2 );
	    } else break;
	}
	for( it = r.getReturned().iterator(); it.hasNext(); ) {
	    m.addIEdges( ret, mu.get( (Node) it.next() ) );
	}


	if( RinardTransformer.v().optionMuDotFiles ) {
	    try {
		Body b = callee.getActiveBody();
	    PrintStream outSt = new PrintStream(
		new FileOutputStream( b.getMethod().getDeclaringClass().getName() + "."
					+b.getMethod().getName()+
		    "graph"+(graphNum++)+".dot" ) );
	    outSt.println( "digraph G {\nrankdir=LR;\nrotate=90;\n" );
	    outSt.println( r.dotGraphGuts(" color=red fontcolor=red ","callee_", " color=red fontcolor=red "," color=red fontcolor=red ") );
	    outSt.println( m.dotGraphGuts("","","","") );
	    for( it = mu.keySet().iterator(); it.hasNext(); ) {
		Node n1 = (Node) it.next();
		for( Iterator it2 = mu.get(n1).iterator(); it2.hasNext(); ) {
		    Node n2 = (Node) it2.next();
		    outSt.println( n1.getId() + " -> " + n2.getId() + " [ color=green , weight=0 ] ;\n" );
		}
	    }
	    outSt.println( "}\n" );
	    } catch( FileNotFoundException e ) { }
	}
    }
}

