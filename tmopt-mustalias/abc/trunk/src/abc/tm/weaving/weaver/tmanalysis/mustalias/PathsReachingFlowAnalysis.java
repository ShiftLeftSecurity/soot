/** Approximates the number of paths that reach any given statement;
 * i.e. detects whether a statement belongs to a loop or not.  */

package abc.tm.weaving.weaver.tmanalysis.mustalias;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import soot.Unit;
import soot.toolkits.graph.UnitGraph;

public class PathsReachingFlowAnalysis {
    public static final Object NONE = new Object();
    public static final Object ONE = new Object();
    public static final Object MANY = new Object();

    HashMap<Unit, Object> visitCount = new HashMap();
    
	public PathsReachingFlowAnalysis(UnitGraph g) {
        for (Unit u : (Collection<Unit>)g.getBody().getUnits())
            visitCount.put(u, NONE);

        LinkedList<Unit> visitQueue = new LinkedList();
        visitQueue.addAll(g.getHeads());

        while (!visitQueue.isEmpty()) {
            Unit u = visitQueue.removeFirst();
            Object o = visitCount.get(u);
            if (o == NONE)
                visitCount.put(u, ONE);
            else if (o == ONE)
                visitCount.put(u, MANY);
            visitQueue.addAll((Collection<Unit>)g.getSuccsOf(u));
        }
    }

    public Object getVisitCount(Unit u) {
        return visitCount.get(u);
    }
}
