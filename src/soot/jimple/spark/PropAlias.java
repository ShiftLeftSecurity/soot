/* Soot - a J*va Optimization Framework
 * Copyright (C) 2002, 2003 Ondrej Lhotak
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
import soot.util.*;
import soot.util.queue.*;
import java.util.*;
import soot.options.SparkOptions;
/** Propagates points-to sets along pointer assignment graph using an
 * alias edge analysis.
 * @author Ondrej Lhotak
 */

public final class PropAlias extends AbsPropagator {
    protected final Set varNodeWorkList = new TreeSet();
    protected Set aliasWorkList;
    protected Set fieldRefWorkList = new HashSet();
    protected Set outFieldRefWorkList = new HashSet();

    public PropAlias( Rsrc_dst simple, Rsrc_fld_dst load, Rsrc_fld_dst store,
            Robj_var alloc, Qvar_obj propout, AbsPAG pag ) {
        super( simple, load, store, alloc, propout, pag );
        loadSets = new LargeNumberedMap( SparkNumberers.v().fieldRefNodeNumberer() );
    }

    /** Actually does the propagation. */
    public final void update() {
        new TopoSorter( pag, false ).sort();
        for( Iterator frIt = pag.loadSources(); frIt.hasNext(); ) {
            final FieldRefNode fr = (FieldRefNode) frIt.next();
            fieldToBase.put( fr.getField(), fr.getBase() );
        }
        for( Iterator frIt = pag.storeInvSources(); frIt.hasNext(); ) {
            final FieldRefNode fr = (FieldRefNode) frIt.next();
            fieldToBase.put( fr.getField(), fr.getBase() );
        }
	for( Iterator it = pag.allocSources(); it.hasNext(); ) {
	    handleAllocNode( (AllocNode) it.next() );
	}

        boolean verbose = SparkScene.v().options().verbose();
	do {
            if( verbose ) {
                G.v().out.println( "Worklist has "+varNodeWorkList.size()+
                        " nodes." );
            }
            aliasWorkList = new HashSet();
            while( !varNodeWorkList.isEmpty() ) {
                VarNode src = (VarNode) varNodeWorkList.iterator().next();
                varNodeWorkList.remove( src );
                aliasWorkList.add( src );
                handleVarNode( src );
            }
            if( verbose ) {
                G.v().out.println( "Now handling field references" );
            }

            for( Iterator srcIt = aliasWorkList.iterator(); srcIt.hasNext(); ) {

                final VarNode src = (VarNode) srcIt.next();
                for( Iterator srcFrIt = src.getAllFieldRefs().iterator(); srcFrIt.hasNext(); ) {
                    final FieldRefNode srcFr = (FieldRefNode) srcFrIt.next();
                    SparkField field = srcFr.getField();
                    for( Iterator dstIt = fieldToBase.get( field ).iterator(); dstIt.hasNext(); ) {
                        final VarNode dst = (VarNode) dstIt.next();
                        if( src.getP2Set().hasNonEmptyIntersection(
                                    dst.getP2Set() ) ) {
                            FieldRefNode dstFr = dst.dot( field );
                            aliasEdges.put( srcFr, dstFr );
                            aliasEdges.put( dstFr, srcFr );
                            fieldRefWorkList.add( srcFr );
                            fieldRefWorkList.add( dstFr );
                            if( makeP2Set( dstFr ).addAll( 
                                    srcFr.getP2Set().getOldSet(), null ) ) {
                                outFieldRefWorkList.add( dstFr );
                            }
                            if( makeP2Set( srcFr ).addAll( 
                                    dstFr.getP2Set().getOldSet(), null ) ) {
                                outFieldRefWorkList.add( srcFr );
                            }
                        }
                    }
                }
            }
            for( Iterator srcIt = fieldRefWorkList.iterator(); srcIt.hasNext(); ) {
                final FieldRefNode src = (FieldRefNode) srcIt.next();
                for( Iterator dstIt = aliasEdges.get( src ).iterator(); dstIt.hasNext(); ) {
                    final FieldRefNode dst = (FieldRefNode) dstIt.next();
                    if( makeP2Set( dst ).addAll( src.getP2Set().getNewSet(), null ) ) {
                        outFieldRefWorkList.add( dst );
                    }
                }
                src.getP2Set().flushNew();
            }
            fieldRefWorkList = new HashSet();
            for( Iterator srcIt = outFieldRefWorkList.iterator(); srcIt.hasNext(); ) {
                final FieldRefNode src = (FieldRefNode) srcIt.next();
                PointsToSetInternal set = getP2Set( src ).getNewSet();
                if( set.isEmpty() ) continue;
                for( Iterator targetIt = pag.loadLookup(src); targetIt.hasNext(); ) {
                    final VarNode target = (VarNode) targetIt.next();
                    if( target.makeP2Set().addAll( set, null ) ) {
                        addToWorklist( target );
                    }
                }
                getP2Set( src ).flushNew();
            }
            outFieldRefWorkList = new HashSet();
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
                addToWorklist( target );
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
                addToWorklist( t.dst() );
        }
        for( Iterator tIt = newAlloc.iterator(); tIt.hasNext(); ) {
            final Robj_var.Tuple t = (Robj_var.Tuple) tIt.next();
            ret = true;
            if( t.var().makeP2Set().add( t.obj() ) )
                addToWorklist( t.var() );
        }
        for( Iterator tIt = newLoad.iterator(); tIt.hasNext(); ) {
            final Rsrc_fld_dst.Tuple t = (Rsrc_fld_dst.Tuple) tIt.next();
            ret = true;
            if( fieldToBase.put( t.fld(), t.src() ) ) {
                aliasWorkList.add( t.src() );
            }
        }
        for( Iterator tIt = newStore.iterator(); tIt.hasNext(); ) {
            final Rsrc_fld_dst.Tuple t = (Rsrc_fld_dst.Tuple) tIt.next();
            ret = true;
            if( fieldToBase.put( t.fld(), t.dst() ) ) {
                aliasWorkList.add( t.dst() );
            }
        }

        for( Iterator simpleTargetIt = pag.simpleLookup(src); simpleTargetIt.hasNext(); ) {

            final VarNode simpleTarget = (VarNode) simpleTargetIt.next();
	    if( simpleTarget.makeP2Set().addAll( newP2Set, null ) ) {
                addToWorklist( simpleTarget );
                ret = true;
            }
	}

        for( Iterator storeTargetIt = pag.storeLookup(src); storeTargetIt.hasNext(); ) {

            final FieldRefNode storeTarget = (FieldRefNode) storeTargetIt.next();
            if( storeTarget.makeP2Set().addAll( newP2Set, null ) ) {
                fieldRefWorkList.add( storeTarget );
                ret = true;
            }
        }

	src.getP2Set().flushNew();
	return ret;
    }

    protected final PointsToSetInternal makeP2Set( FieldRefNode n ) {
        PointsToSetInternal ret = (PointsToSetInternal) loadSets.get(n);
        if( ret == null ) {
            ret = SparkScene.v().setFactory.newSet( null );
            loadSets.put( n, ret );
        }
        return ret;
    }

    protected final PointsToSetInternal getP2Set( FieldRefNode n ) {
        PointsToSetInternal ret = (PointsToSetInternal) loadSets.get(n);
        if( ret == null ) {
            return EmptyPointsToSet.v();
        }
        return ret;
    }

    private boolean addToWorklist( VarNode n ) {
        return varNodeWorkList.add( n );
    }

    protected MultiMap fieldToBase = new HashMultiMap();
    protected MultiMap aliasEdges = new HashMultiMap();
    protected LargeNumberedMap loadSets;
}



