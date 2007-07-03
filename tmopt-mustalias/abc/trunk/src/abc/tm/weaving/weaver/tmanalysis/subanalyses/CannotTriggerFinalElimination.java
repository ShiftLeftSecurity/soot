/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package abc.tm.weaving.weaver.tmanalysis.subanalyses;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.jimple.toolkits.pointer.LocalNotMayAliasAnalysis;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.weaver.tmanalysis.Util;
import abc.tm.weaving.weaver.tmanalysis.ds.FinalConfigsUnitGraph;
import abc.tm.weaving.weaver.tmanalysis.ds.InitialConfigsUnitGraph;
import abc.tm.weaving.weaver.tmanalysis.ds.MustMayNotAliasDisjunct;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;

/**
 * CannotTriggerFinalElimination
 *
 * @author Eric Bodden
 */
public class CannotTriggerFinalElimination {
    
    public static boolean apply(TraceMatch tm, UnitGraph g, Map<Local, Stmt> tmLocalsToDefStatements, LocalMustAliasAnalysis localMustAliasAnalysis, LocalNotMayAliasAnalysis localNotMayAliasAnalysis) {
        
        System.err.println("Running optimization 'cannot trigger final'...");

        //get all active shadows in the method
        Set<ISymbolShadow> allMethodShadows = Util.getAllActiveShadows(g.getBody().getUnits());
        
        //generate an augmented unit graph modelling all possible incoming executions to the method and all possible outgoing executions from the method
        DirectedGraph<Unit> augmentedGraph = new FinalConfigsUnitGraph(new InitialConfigsUnitGraph(g,allMethodShadows,tm),allMethodShadows,tm);
        
        Collection<Stmt> allStmts = new HashSet<Stmt>();
        for (Iterator<Unit> unitIter = augmentedGraph.iterator(); unitIter.hasNext();) {
            Stmt st = (Stmt)unitIter.next();
            allStmts.add(st);
        }
    
        IntraProceduralTMFlowAnalysis flowAnalysis = new IntraProceduralTMFlowAnalysis(
                tm,
                augmentedGraph,
                g.getBody().getMethod(),
                tmLocalsToDefStatements,
                new MustMayNotAliasDisjunct(),
                new HashSet<State>(),
                allStmts,
                localMustAliasAnalysis,
                localNotMayAliasAnalysis
        );
        
        Status status = flowAnalysis.getStatus();
        System.err.println("Analysis done with status: "+status);

        if(status.isAborted() || status.hitFinal()) {
            return false;
        }
        
        //method and everything that follows it can never hit the final state, hence disable all shadows in the method
        for (ISymbolShadow shadow : allMethodShadows) {
            ShadowRegistry.v().disableShadow(shadow.getUniqueShadowId());
        }
        System.err.println("Optimization 'cannot trigger final' removed all shadows.");
        
        return true;
    }


}
