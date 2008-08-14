package soot.jimple.toolkits.ctl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.jimple.toolkits.ctl.formula.IFormula;
import soot.tagkit.Host;
import soot.toolkits.graph.DirectedGraph;

public class CTLAnalysis<N extends Host>  {
	
	public CTLAnalysis(DirectedGraph<N> g, IFormula formula) {
		List<IFormula> closure = formula.closure();
		Collections.sort(closure);
		
		for(IFormula f: closure) {		
			f.preprocess(g);
			Set<N> worklist = new HashSet<N>();
			//TODO do not always need to add all nodes, do we? does it make sense to iterate in a civilized way?
			for(N n: g) {
				worklist.add(n);
			}
			while(!worklist.isEmpty()) {
				Iterator<N> iter = worklist.iterator();
				N n = iter.next();
				iter.remove();

				List<Host> succs = (List<Host>) g.getSuccsOf(n);
				if(f.label(n,succs))
					worklist.addAll(g.getPredsOf(n));
			}
		}
		System.err.println();
		
		for (N n : g) {
			n.removeTag(FormulaTag.class.getName());
		}
	}


	
}
