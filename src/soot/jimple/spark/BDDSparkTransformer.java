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
import soot.*;
import soot.jimple.spark.builder.*;
import soot.jimple.spark.pag.*;
import soot.jimple.spark.solver.*;
import soot.jimple.spark.sets.*;
import soot.jimple.toolkits.callgraph.*;
import soot.jimple.toolkits.pointer.*;
import soot.jimple.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import soot.util.*;
import soot.options.*;
import soot.tagkit.*;
import jedd.*;
import soot.jimple.spark.bdddomains.*;

/** Main entry point for Spark.
 * @author Ondrej Lhotak
 */
public class BDDSparkTransformer extends AbstractSparkTransformer
{ 
    public BDDSparkTransformer( Singletons.Global g ) {}
    public static BDDSparkTransformer v() { return G.v().BDDSparkTransformer(); }

    protected void internalTransform( String phaseName, Map options )
    {
        BDDSparkOptions opts = new BDDSparkOptions( options );

        System.loadLibrary("jeddbuddy");
        Jedd.v();
        if( opts.profile() ) {
            Jedd.v().enableProfiling();
        }
        PhysicalDomain[] v1v2 = { V1.v(), V2.v() };
        Object[] order = { T1.v(), T2.v(), V3.v(), FD.v(), v1v2, H1.v(), H2.v() };
        Jedd.v().setOrder( order, true );

        // Build pointer assignment graph
        BDDContextInsensitiveBuilder b = new BDDContextInsensitiveBuilder();
        if( opts.pre_jimplify() ) b.preJimplify();
        if( opts.force_gc() ) doGC();
        Date startBuild = new Date();
        final BDDPAG pag = (BDDPAG) b.setup( opts );
        Scene.v().setPointsToAnalysis( pag );
        b.build();
        Date endBuild = new Date();
        reportTime( "Pointer Assignment Graph", startBuild, endBuild );
        if( opts.force_gc() ) doGC();

        // Build type masks
        Date startTM = new Date();
        pag.getTypeManager().makeTypeMask();
        Date endTM = new Date();
        reportTime( "Type masks", startTM, endTM );
        if( opts.force_gc() ) doGC();

        if( opts.verbose() ) {
            G.v().out.println( "VarNodes: "+pag.getVarNodeNumberer().size() );
            G.v().out.println( "FieldRefNodes: "+pag.getFieldRefNodeNumberer().size() );
            G.v().out.println( "AllocNodes: "+pag.getAllocNodeNumberer().size() );
        }

        // Propagate
        Date startProp = new Date();
        BDDPropagator propagator = new BDDPropagator( pag );
        propagator.propagate();
        Date endProp = new Date();
        reportTime( "Propagation", startProp, endProp );
        if( opts.force_gc() ) doGC();
        
        if( !opts.on_fly_cg() || opts.vta() ) {
            BDDCallGraphBuilder cgb = new BDDCallGraphBuilder( pag );
            throw new RuntimeException("NYI");
            // cgb.build();
        }

        /*
        if( opts.verbose() ) {
            G.v().out.println( "[Spark] Number of reachable methods: "
                    +Scene.v().getReachableMethods().size() );
        }
        */

        if( opts.set_mass() ) findSetMass( pag );
        if( opts.add_tags() ) {
            addTags( pag );
        }
        if( opts.profile() ) {
            try {
                JeddProfiler.v().printInfo( new PrintStream( new GZIPOutputStream(
                    new FileOutputStream( new File( "profile.sql.gz")))));
            } catch( IOException e ) {
                throw new RuntimeException( "Couldn't output Jedd profile "+e );
            }
        }
        new BDDSideEffectAnalysis( pag, pag.ofcg().callGraph(), pag.ofcg().reachableMethods() ).analyze();
        System.out.println("points-to set is:\n"+ pag.pointsTo.toString() );
        System.out.println("call graph is:\n"+ pag.ofcg().callGraph().edges.toString() );
    }

    private void addTags( BDDPAG pag ) {
        throw new RuntimeException( "NYI" );
    }

    protected void findSetMass( AbstractPAG pg ) {
        BDDPAG pag = (BDDPAG) pg;
        G.v().out.println( "PointsTo mass: " + pag.pointsTo.size() );
        G.v().out.println( "FieldPt mass: " + pag.fieldPt.size() );
        G.v().out.println( "Set mass: " + (pag.fieldPt.size()+pag.pointsTo.size()) );
    }
}


