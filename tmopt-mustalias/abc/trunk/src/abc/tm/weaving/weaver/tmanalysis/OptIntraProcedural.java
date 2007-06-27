/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 * Copyright (C) 2007 Patrick Lam
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
package abc.tm.weaving.weaver.tmanalysis;

import java.util.Iterator;

import soot.Kind;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import abc.main.AbcTimer;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.weaver.tmanalysis.stages.CallGraphAbstraction;
import abc.tm.weaving.weaver.tmanalysis.stages.IntraproceduralAnalysis;
import abc.tm.weaving.weaver.tmanalysis.util.Statistics;
import abc.weaving.weaver.AbstractReweavingAnalysis;

/**
 * A reweaving analysis that executes the intra-procedural analysis
 * as described in our upcoming POPL 2008 paper.
 * @author Eric Bodden
 * @author Patrick Lam
 */
public class OptIntraProcedural extends AbstractReweavingAnalysis {

	protected TMGlobalAspectInfo gai;
	
    public boolean analyze() {
    	gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();

		//nothing to do?
    	if(gai.getTraceMatches().size()==0) {
    		return false;
    	}
        
        //if any thread may be started, abort
        CallGraph callGraph = CallGraphAbstraction.v().abstractedCallGraph();
        for (Iterator iterator = callGraph.listener(); iterator.hasNext();) {
            Edge edge = (Edge) iterator.next();
            if(edge.kind().equals(Kind.THREAD)) {
                System.err.println("#####################################################");
                System.err.println(" - WARNING - WARNING - WARNING - WARNING - WARNING - ");
                System.err.println(" Application may start threads that execute shadows! ");
                System.err.println("#####################################################");
                break;
            }
        }
    	
    	try {
    		doAnalyze();
    	} catch (Error e) {
    		Statistics.errorOccured = true;
    		throw e;
    	} catch (RuntimeException e) {
    		Statistics.errorOccured = true;
    		throw e;
    	}
    	
		//we do not need to reweave right away
        return false;
    }

    /**
	 * Performs the actual analysis.
	 */
	protected void doAnalyze() {

		//take into account effects of the earlier stages
		CallGraphAbstraction.v().rebuildAbstractedCallGraph();

		AbcTimer.mark("Reabstraction of call graph");

		IntraproceduralAnalysis.v().apply();
		
		AbcTimer.mark("Intra-procedural analysis (POPL'08)");
    	
	}

}
