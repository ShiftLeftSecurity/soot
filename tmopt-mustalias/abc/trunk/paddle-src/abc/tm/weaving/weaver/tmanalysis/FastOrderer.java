package abc.tm.weaving.weaver.tmanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.Orderer;
import soot.util.IdentityHashSet;

/**
 * FastOrderer
 *
 * @author Eric Bodden
 */
public class FastOrderer implements Orderer {

	/**
	 * {@inheritDoc}
	 */
	public List newList(DirectedGraph g, boolean reverse) {
		List ordering = new ArrayList(g.size());
		IdentityHashSet heads = new IdentityHashSet();
		heads.addAll(g.getHeads());
		//add heads first
		ordering.addAll(heads);
		//add everything else last
		for (Iterator allIter = g.iterator(); allIter.hasNext();) {
			Object curr = allIter.next();
			if(!heads.contains(curr)) {
				ordering.add(curr);
			}
		}		
		if(reverse) {
			Collections.reverse(ordering);
		}
		
		return ordering;
	}

}
