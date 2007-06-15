/** Approximates the number of paths that reach any given statement;
 * i.e. detects whether a statement belongs to a loop or not.  */

package abc.tm.weaving.weaver.tmanalysis.mustalias;

import soot.Unit;
import soot.toolkits.graph.StronglyConnectedComponents;
import soot.toolkits.graph.UnitGraph;

/**
 * PathsReachingFlowAnalysis
 *
 * @author Eric Bodden
 */
@Deprecated()
public class PathsReachingFlowAnalysis {

	protected StronglyConnectedComponents sccAnalysis;

	public PathsReachingFlowAnalysis(UnitGraph g) {
		sccAnalysis = new StronglyConnectedComponents(g);		
    }

    public boolean visitedPotentiallyManyTimes(Unit u) {
    	return sccAnalysis.getComponentOf(u).size()>1;
    }
}
