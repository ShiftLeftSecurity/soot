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

/** Propagates points-to sets along pointer assignment graph using iteration.
 * @author Ondrej Lhotak
 */

public final class PropIter extends AbsPropagator {
    public PropIter(  Rsrc_dst simple,
            Rsrc_fld_dst load,
            Rsrc_fld_dst store,
            Robj_var alloc,
            Qvar_obj propout,
            AbsPAG pag
 ) {
        super( simple, load, store, alloc, propout, pag );
    }
    /** Actually does the propagation. */
    public final void update() {
        new TopoSorter( pag, false ).sort();
        for( Iterator it = pag.allocSources(); it.hasNext(); ) {
            handleAllocNode( (AllocNode) it.next() );
        }
        int iteration = 1;
	boolean change;
	do {
	    change = false;
            TreeSet simpleSources = new TreeSet();
            for( Iterator sourceIt = pag.simpleSources(); sourceIt.hasNext(); ) {
            	simpleSources.add(sourceIt.next());
            }
            if( SparkScene.v().options().verbose() ) {
                G.v().out.println( "Iteration "+(iteration++) );
            }
            for( Iterator it = simpleSources.iterator(); it.hasNext(); ) {
                change = handleSimples( (VarNode) it.next() ) | change;
            }
            
            for( Iterator srcIt = SparkNumberers.v().varNodeNumberer().iterator(); srcIt.hasNext(); ) {
            
                final VarNode src = (VarNode) srcIt.next();
                src.getP2Set().getNewSet().forall( new P2SetVisitor() {
                public final void visit( Node n ) {
                    ptout.add( src, (AllocNode) n );
                }} );
            }
            SparkScene.v().updateCallGraph();

            for( Iterator tIt = newSimple.iterator(); tIt.hasNext(); ) {

                final Rsrc_dst.Tuple t = (Rsrc_dst.Tuple) tIt.next();
                change = true;
                PointsToSetInternal p2set = t.src().getP2Set();
                if( p2set != null ) p2set.unFlushNew();
            }
            for( Iterator tIt = newLoad.iterator(); tIt.hasNext(); ) {
                final Rsrc_fld_dst.Tuple t = (Rsrc_fld_dst.Tuple) tIt.next();
                change = true;
            }
            for( Iterator tIt = newStore.iterator(); tIt.hasNext(); ) {
                final Rsrc_fld_dst.Tuple t = (Rsrc_fld_dst.Tuple) tIt.next();
                change = true;
                PointsToSetInternal p2set = t.src().getP2Set();
                if( p2set != null ) p2set.unFlushNew();
            }
            for( Iterator tIt = newAlloc.iterator(); tIt.hasNext(); ) {
                final Robj_var.Tuple t = (Robj_var.Tuple) tIt.next();
                change = true;
                t.var().makeP2Set().add( t.obj() );
            }
            if( change ) {
                new TopoSorter( pag, false ).sort();
            }
	    for( Iterator it = pag.loadSources(); it.hasNext(); ) {
                change = handleLoads( (FieldRefNode) it.next() ) | change;
	    }
	    for( Iterator it = pag.storeSources(); it.hasNext(); ) {
                change = handleStores( (VarNode) it.next() ) | change;
	    }
	} while( change );
    }

    /* End of public methods. */
    /* End of package methods. */

    /** Propagates new points-to information of node src to all its
     * successors. */
    protected final boolean handleAllocNode( AllocNode src ) {
	boolean ret = false;
        for( Iterator targetIt = pag.allocLookup(src); targetIt.hasNext(); ) {
            final VarNode target = (VarNode) targetIt.next();
	    ret = target.makeP2Set().add( src ) | ret;
	}
	return ret;
    }

    protected final boolean handleSimples( VarNode src ) {
	boolean ret = false;
	PointsToSetInternal srcSet = src.getP2Set();
	if( srcSet.isEmpty() ) return false;
        for( Iterator simpleTargetIt = pag.simpleLookup(src); simpleTargetIt.hasNext(); ) {
            final VarNode simpleTarget = (VarNode) simpleTargetIt.next();
	    ret = simpleTarget.makeP2Set().addAll( srcSet, null ) | ret;
	}
        return ret;
    }

    protected final boolean handleStores( VarNode src ) {
	boolean ret = false;
	final PointsToSetInternal srcSet = src.getP2Set();
	if( srcSet.isEmpty() ) return false;
        for( Iterator storeTargetIt = pag.storeLookup(src); storeTargetIt.hasNext(); ) {
            final FieldRefNode storeTarget = (FieldRefNode) storeTargetIt.next();
            final SparkField f = storeTarget.getField();
            ret = storeTarget.getBase().getP2Set().forall( new P2SetVisitor() {
            public final void visit( Node n ) {
                    AllocDotField nDotF = SparkScene.v().nodeManager().makeAllocDotField( 
                        (AllocNode) n, f );
                    if( nDotF.makeP2Set().addAll( srcSet, null ) ) {
                        returnValue = true;
                    }
                }
            } ) | ret;
	}
        return ret;
    }

    protected final boolean handleLoads( final FieldRefNode src ) {
	boolean ret = false;
        final SparkField f = src.getField();
        ret = src.getBase().getP2Set().forall( new P2SetVisitor() {
        public final void visit( Node n ) {
                AllocDotField nDotF = ((AllocNode)n).dot( f );
                if( nDotF == null ) return;
                PointsToSetInternal set = nDotF.getP2Set();
                if( set.isEmpty() ) return;
                for( Iterator loadTargetIt = pag.loadLookup(src); loadTargetIt.hasNext(); ) {
                    final VarNode loadTarget = (VarNode) loadTargetIt.next();
                    if( loadTarget.makeP2Set().addAll( set, null ) ) {
                        returnValue = true;
                    }
                }
            }
        } ) | ret;
        return ret;
    }
}



