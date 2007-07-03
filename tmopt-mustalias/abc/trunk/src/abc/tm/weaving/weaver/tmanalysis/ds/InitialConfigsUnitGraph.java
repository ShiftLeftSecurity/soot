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
package abc.tm.weaving.weaver.tmanalysis.ds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.toolkits.callgraph.Units;
import soot.toolkits.graph.DirectedGraph;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.Util;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.stages.FlowInsensitiveAnalysis;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowTag;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolShadow;

/**
 * Augments a directed graph over {@link Units} with a graph prefix that models all
 * possible executions preceding the execution of the associated method, based on
 * interprocedural shadow information.
 * 
 * First we gather the set of all shadows S that share {@link ShadowGroup}s with {@link SymbolShadow}s
 * in this method. Then we compute the length L of the longest path info for the given
 * {@link TraceMatch}.
 * We then add a prefix of length L+1 to the graph:
 * <ul>
 * <li>one initial nop-node that has no shadows annotated and has all following initial
 *     nop-nodes as successors, as well as all head units
 * <li>a chain of L initial nop-nodes that are annotated with all shadows computed above 
 * <ul> 
 * @author Eric Bodden
 */
public class InitialConfigsUnitGraph implements DirectedGraph<Unit> {
    
    protected final DirectedGraph<Unit> originalGraph;
    protected final List<Unit> initialUnits;

    public InitialConfigsUnitGraph(DirectedGraph<Unit> originalGraph, Set<ISymbolShadow> shadowsInMethod, TraceMatch owner) {
        this.originalGraph = originalGraph;
        
        Set<ISymbolShadow> overlappingShadows = Util.sameShadowGroup(shadowsInMethod);        
        int lengthOfLongestPath = FlowInsensitiveAnalysis.v().lengthOfLongestPathFor(owner);
        
        Map<TraceMatch,Set<ISymbolShadow>> tmToShadows = new HashMap<TraceMatch, Set<ISymbolShadow>>();
        tmToShadows.put(owner, overlappingShadows);
                
        final List<Unit> units = new ArrayList<Unit>();
        //add head-nop
        units.add(Jimple.v().newNopStmt());
        //add as many nops as length of longets path, each one with all shadows attached
        for(int i=0; i <lengthOfLongestPath; i++) {
            NopStmt nop = Jimple.v().newNopStmt();
            nop.addTag( new SymbolShadowTag(tmToShadows) );
            units.add(nop);
        }
        
        this.initialUnits = units;
    }

    /**
     * {@inheritDoc}
     */
    public List<Unit> getHeads() {
        //return first unit
        return Collections.singletonList(initialUnits.get(0));
    }

    /**
     * {@inheritDoc}
     */
    public List<Unit> getPredsOf(Unit s) {
        int index = initialUnits.indexOf(s);
        if(index>-1) {
            //unit is an initial unit
            if(index==0) {
                //first element; has no predecessor
                return Collections.emptyList();
            } else {
                //subsequent element; has the natural predecessor as predecessor
                //and the first initial nop-unit
                List<Unit> list = new ArrayList<Unit>();
                list.add(initialUnits.get(0));
                list.add(initialUnits.get(index-1));
                return list;
            }               
        } else if(originalGraph.getHeads().contains(s)) {
            //head unit, has initial nop-unit and last initial unit as predecessors 
            List<Unit> list = new ArrayList<Unit>();
            list.add(initialUnits.get(0));
            list.add(initialUnits.get(initialUnits.size()-1));
            return list;
        } else {
            //else delegate
            return originalGraph.getPredsOf(s);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Unit> getSuccsOf(Unit s) {
        int index = initialUnits.indexOf(s);
        if(index==0) {
            //initial nop-statements has all other initial nops and all
            //head statements as successors
            List<Unit> list = new ArrayList<Unit>();
            //all other initial nops
            list.addAll(initialUnits);
            list.remove(s);
            //all heads
            list.addAll(originalGraph.getHeads());
            return list;
        } else if(index>-1 && index<initialUnits.size()-1) {
            //other initial unit but not the last one:
            //has next natural successor as successor
            return Collections.singletonList(initialUnits.get(index+1));
        } else if(index>-1) {
            //last initial not: has all heads as sucessors
            assert index==initialUnits.size()-1;
            return originalGraph.getHeads();
        } else {
            //else delegate
            return originalGraph.getSuccsOf(s);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Unit> getTails() {
        //return tails of the original graph
        return originalGraph.getTails();
    }

    /**
     * {@inheritDoc}
     */
    public Iterator iterator() {
        List<Unit> list = new ArrayList<Unit>();
        //add all units form original graph
        for (Iterator<Unit> iterator = originalGraph.iterator(); iterator.hasNext();) {
            Unit u = iterator.next();
            list.add(u);
        }
        //and all initial units
        list.addAll(initialUnits);
        return list.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return originalGraph.size()+initialUnits.size();
    }

}
