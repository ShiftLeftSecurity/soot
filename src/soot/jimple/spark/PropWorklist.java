/* Soot - a J*va Optimization Framework
 * Copyright (C) 2002 Ondrej Lhotak
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

package soot.jimple.spark;
import soot.jimple.spark.queue.*;
import soot.*;
import soot.util.queue.*;
import java.util.*;
import soot.options.SparkOptions;

/** Propagates points-to sets along pointer assignment graph using a worklist.
 * @author Ondrej Lhotak
 */

public final class PropWorklist extends AbsPropagator {
    protected final Set varNodeWorkList = new TreeSet();
    private NodeManager nm = SparkScene.v().nodeManager();

    public PropWorklist( Rsrc_dst simple,
            Rsrc_fld_dst load,
            Rsrc_fld_dst store,
            Robj_var alloc,
            Qvar_obj propout,
            AbsPAG pag ) {
        super( simple, load, store, alloc, propout, pag );
    }
    /** Actually does the propagation. */
    public final void update() {
        new TopoSorter( pag, false ).sort();
	for( Iterator it = pag.allocSources(); it.hasNext(); ) {
	    handleAllocNode( (AllocNode) it.next() );
	}

        boolean verbose = SparkScene.v().options().verbose();
	do {
            if( verbose ) {
                G.v().out.println( "Worklist has "+varNodeWorkList.size()+
                        " nodes." );
            }
            while( !varNodeWorkList.isEmpty() ) {
                VarNode src = (VarNode) varNodeWorkList.iterator().next();
                varNodeWorkList.remove( src );
                handleVarNode( src );
            }
            if( verbose ) {
                G.v().out.println( "Now handling field references" );
            }
            for( Iterator srcIt = pag.storeSources(); srcIt.hasNext(); ) {
                final VarNode src = (VarNode) srcIt.next();
                for( Iterator targetIt = pag.storeLookup(src); targetIt.hasNext(); ) {
                    final FieldRefNode target = (FieldRefNode) targetIt.next();
                    target.getBase().makeP2Set().forall( new P2SetVisitor() {
                    public final void visit( Node n ) {
                            AllocDotField nDotF = nm.makeAllocDotField( 
                                (AllocNode) n, target.getField() );
                            nDotF.makeP2Set().addAll( src.getP2Set(), null );
                        }
                    } );
                }
            }
            HashSet edgesToPropagate = new HashSet();
	    for( Iterator it = pag.loadSources(); it.hasNext(); ) {
                handleFieldRefNode( (FieldRefNode) it.next(), edgesToPropagate );
	    }
            HashSet nodesToFlush = new HashSet();
            for( Iterator pairIt = edgesToPropagate.iterator(); pairIt.hasNext(); ) {
                final Object[] pair = (Object[]) pairIt.next();
                PointsToSetInternal nDotF = (PointsToSetInternal) pair[0];
		PointsToSetInternal newP2Set = nDotF.getNewSet();
                VarNode loadTarget = (VarNode) pair[1];
                if( loadTarget.makeP2Set().addAll( newP2Set, null ) ) {
                    varNodeWorkList.add( loadTarget );
                }
                nodesToFlush.add( nDotF );
            }
            for( Iterator nDotFIt = nodesToFlush.iterator(); nDotFIt.hasNext(); ) {
                final PointsToSetInternal nDotF = (PointsToSetInternal) nDotFIt.next();
                nDotF.flushNew();
            }
	} while( !varNodeWorkList.isEmpty() );
    }

    /* End of public methods. */
    /* End of package methods. */

    /** Propagates new points-to information of node src to all its
     * successors. */
    protected final boolean handleAllocNode( AllocNode src ) {
	boolean ret = false;
        for( Iterator targetIt = pag.allocLookup(src); targetIt.hasNext(); ) {
            final VarNode target = (VarNode) targetIt.next();
	    if( target.makeP2Set().add( src ) ) {
                varNodeWorkList.add( target );
                ret = true;
            }
	}
	return ret;
    }
    /** Propagates new points-to information of node src to all its
     * successors. */
    protected final boolean handleVarNode( final VarNode src ) {
	boolean ret = false;

        
	final PointsToSetInternal newP2Set = src.getP2Set().getNewSet();
	if( newP2Set.isEmpty() ) return false;

        newP2Set.forall( new P2SetVisitor() {

        public final void visit( Node n ) {
            ptout.add( src, (AllocNode) n );
        }} );
        SparkScene.v().updateCallGraph();
        for( Iterator tIt = newSimple.iterator(); tIt.hasNext(); ) {
            final Rsrc_dst.Tuple t = (Rsrc_dst.Tuple) tIt.next();
            ret = true;
            if( t.dst().makeP2Set().addAll( t.src().getP2Set(), null ) )
                varNodeWorkList.add( t.dst() );
        }
        for( Iterator tIt = newAlloc.iterator(); tIt.hasNext(); ) {
            final Robj_var.Tuple t = (Robj_var.Tuple) tIt.next();
            ret = true;
            if( t.var().makeP2Set().add( t.obj() ) )
                varNodeWorkList.add( t.var() );
        }

        for( Iterator simpleTargetIt = pag.simpleLookup(src); simpleTargetIt.hasNext(); ) {

            final VarNode simpleTarget = (VarNode) simpleTargetIt.next();
	    if( simpleTarget.makeP2Set().addAll( newP2Set, null ) ) {
                varNodeWorkList.add( simpleTarget );
                ret = true;
            }
	}

        for( Iterator storeTargetIt = pag.storeLookup(src); storeTargetIt.hasNext(); ) {

            final FieldRefNode storeTarget = (FieldRefNode) storeTargetIt.next();
            final SparkField f = storeTarget.getField();
            ret = storeTarget.getBase().getP2Set().forall( new P2SetVisitor() {
            public final void visit( Node n ) {
                    AllocDotField nDotF = nm.makeAllocDotField( 
                        (AllocNode) n, f );
                    if( nDotF.makeP2Set().addAll( newP2Set, null ) ) {
                        returnValue = true;
                    }
		}
	    } ) | ret;
        }

        final HashSet storesToPropagate = new HashSet();
        final HashSet loadsToPropagate = new HashSet();
	Collection fieldRefs = src.getAllFieldRefs();
	for( Iterator frIt = fieldRefs.iterator(); frIt.hasNext(); ) {
	    final FieldRefNode fr = (FieldRefNode) frIt.next();
	    final SparkField field = fr.getField();
            for( Iterator storeSourceIt = pag.storeInvLookup(fr); storeSourceIt.hasNext(); ) {
                final VarNode storeSource = (VarNode) storeSourceIt.next();
                newP2Set.forall( new P2SetVisitor() {
                public final void visit( Node n ) {
                        AllocDotField nDotF = nm.makeAllocDotField(
                            (AllocNode) n, field );
                        Node[] pair = { storeSource, nDotF };
                        storesToPropagate.add( pair );
                    }
                } );
            }

            for( Iterator loadTargetIt = pag.loadLookup(fr); loadTargetIt.hasNext(); ) {

                final VarNode loadTarget = (VarNode) loadTargetIt.next();
                newP2Set.forall( new P2SetVisitor() {
                public final void visit( Node n ) {
                        AllocDotField nDotF = nm.findAllocDotField(
                            (AllocNode) n, field );
                        if( nDotF != null ) {
                            Node[] pair = { nDotF, loadTarget };
                            loadsToPropagate.add( pair );
                        }
                    }
                } );
            }
	}
	src.getP2Set().flushNew();
        for( Iterator pIt = storesToPropagate.iterator(); pIt.hasNext(); ) {
            final Node[] p = (Node[]) pIt.next();
            VarNode storeSource = (VarNode) p[0];
            AllocDotField nDotF = (AllocDotField) p[1];
            if( nDotF.makeP2Set().addAll( storeSource.getP2Set(), null ) ) {
                ret = true;
            }
        }
        for( Iterator pIt = loadsToPropagate.iterator(); pIt.hasNext(); ) {
            final Node[] p = (Node[]) pIt.next();
            AllocDotField nDotF = (AllocDotField) p[0];
            VarNode loadTarget = (VarNode) p[1];
            if( loadTarget.makeP2Set().
                addAll( nDotF.getP2Set(), null ) ) {
                varNodeWorkList.add( loadTarget );
                ret = true;
            }
        }
	return ret;
    }

    /** Propagates new points-to information of node src to all its
     * successors. */
    protected final void handleFieldRefNode( final FieldRefNode src, 
            final HashSet edgesToPropagate ) {
        final SparkField field = src.getField();

        for( Iterator loadTargetIt = pag.loadLookup(src); loadTargetIt.hasNext(); ) {

            final VarNode loadTarget = (VarNode) loadTargetIt.next();
            src.getBase().getP2Set().forall( new P2SetVisitor() {
            public final void visit( Node n ) {
                    AllocDotField nDotF = nm.findAllocDotField( 
                        (AllocNode) n, field );
                    if( nDotF != null ) {
                        PointsToSetInternal p2Set = nDotF.getP2Set();
                        if( !p2Set.getNewSet().isEmpty() ) {
                            Object[] pair = { p2Set, loadTarget };
                            edgesToPropagate.add( pair );
                        }
                    }
                }
            } );
        }
    }
    
}



