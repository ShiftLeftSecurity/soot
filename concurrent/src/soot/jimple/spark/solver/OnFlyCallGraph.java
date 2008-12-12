/* Soot - a J*va Optimization Framework
 * Copyright (C) 2002,2003 Ondrej Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package soot.jimple.spark.solver;
import soot.jimple.spark.sets.*;
import soot.jimple.spark.pag.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import soot.util.queue.*;


/** The interface between the pointer analysis engine and the on-the-fly
 * call graph builder.
 * @author Ondrej Lhotak
 */

public class OnFlyCallGraph {
    private final OnFlyCallGraphBuilder ofcgb;
    private final ReachableMethods reachableMethods;
    private final QueueReader reachablesReader;
    private final QueueReader callEdges;
    private final CallGraph callGraph;

    public ReachableMethods reachableMethods() { return reachableMethods; }
    public CallGraph callGraph() { return callGraph; }

    public OnFlyCallGraph( PAG pag ) {
        this.pag = pag;
        callGraph = new CallGraph();
        Scene.v().setCallGraph( callGraph );
        ContextManager cm = CallGraphBuilder.makeContextManager(callGraph);
        reachableMethods = Scene.v().getReachableMethods();
        ofcgb = new OnFlyCallGraphBuilder( cm, reachableMethods );
        reachablesReader = reachableMethods.listener();
        callEdges = cm.callGraph().listener();
    }
    public void build() {
        ofcgb.processReachables();
        processReachables();
        processCallEdges();
    }
    private ConcurrentLinkedQueue<MethodOrMethodContext> momcToReprocessQ = new ConcurrentLinkedQueue<MethodOrMethodContext>();
    private void processReachables() {
        reachableMethods.update();
	ExecutorService pool = Executors.newFixedThreadPool(4);
        while(reachablesReader.hasNext()) {
            MethodOrMethodContext m = (MethodOrMethodContext) reachablesReader.next();
	    ProcessMethodPAG pm = new ProcessMethodPAG(m);
	    pool.execute(pm);
        }
	pool.shutdown();
	try { while (!pool.awaitTermination(50L, TimeUnit.SECONDS)) ; }
	catch (InterruptedException e) {}

	while (!momcToReprocessQ.isEmpty()) {
	    MethodOrMethodContext m = momcToReprocessQ.poll();
	    if (m == null) break;

	    MethodPAG mpag = MethodPAG.v( pag, m.method() );
	    mpag.build();
	    mpag.addToPAG(m.context());
	}

	if (reachablesReader.hasNext()) throw new RuntimeException("need loop");
    }

    private java.util.Set s = java.util.Collections.synchronizedSet(new java.util.HashSet());
    private class ProcessMethodPAG implements Runnable {
	private MethodOrMethodContext m;
	public ProcessMethodPAG(MethodOrMethodContext m) {
	    this.m = m;
	}

	public void run() {
	    SootMethod mm = (SootMethod) m.method();
	    if (mm.isConcrete())
		mm.retrieveActiveBody();
	    if (s.contains(mm)) throw new RuntimeException(mm.toString());
	    s.add(mm);
	    momcToReprocessQ.add(m);
	}
    }

    private void processCallEdges() {
        while(callEdges.hasNext()) {
            Edge e = (Edge) callEdges.next();
            MethodPAG amp = MethodPAG.v( pag, e.tgt() );
            amp.build();
            amp.addToPAG( e.tgtCtxt() );
            pag.addCallTarget( e );
        }
    }

    public OnFlyCallGraphBuilder ofcgb() { return ofcgb; }

    public void updatedNode( VarNode vn ) {
        Object r = vn.getVariable();
        if( !(r instanceof Local) ) return;
        final Local receiver = (Local) r;
        final Context context = vn.context();

        PointsToSetInternal p2set = vn.getP2Set().getNewSet();
        if( ofcgb.wantTypes( receiver ) ) {
            p2set.forall( new P2SetVisitor() {
            public final void visit( Node n ) { 
                ofcgb.addType( receiver, context, n.getType(), (AllocNode) n );
            }} );
        }
        if( ofcgb.wantStringConstants( receiver ) ) {
            p2set.forall( new P2SetVisitor() {
            public final void visit( Node n ) {
                if( n instanceof StringConstantNode ) {
                    String constant = ((StringConstantNode)n).getString();
                    ofcgb.addStringConstant( receiver, context, constant );
                } else {
                    ofcgb.addStringConstant( receiver, context, null );
                }
            }} );
        }
    }

    /** Node uses this to notify PAG that n2 has been merged into n1. */
    public void mergedWith( Node n1, Node n2 ) {
    }

    /* End of public methods. */
    /* End of package methods. */

    private PAG pag;
}



