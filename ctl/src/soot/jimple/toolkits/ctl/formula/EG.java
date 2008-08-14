package soot.jimple.toolkits.ctl.formula;

import java.util.List;

import soot.jimple.toolkits.ctl.FilteredDirectedGraph;
import soot.tagkit.Host;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.StronglyConnectedComponents;
import soot.toolkits.graph.StronglyConnectedComponentsFast;

public class EG extends UnaryFormula {
	
	public EG(IFormula child) {
		super(child);
	}
	
	@Override
	protected String unop() {
		return "EG";
	}
	
	public boolean label(Host n, List<Host> succs) {
		for (Host succ : succs) {
			if(taggedWith(this, succ)) {
				return addFormula(this, n);
			}
		}
		return false;
	}
	
	@Override
	public <N extends Host> void preprocess(DirectedGraph<N> g) {
		//TODO do we have to notify the analysis here that we updated the tagging (in order to not end the fp iteration too early)?
		
		//for tail units t we assume that EG(phi) at t if just phi holds at t 
		for(N tail: g.getTails()) {
			if(taggedWith(child, tail))
				addFormula(this, tail);
		}
		
		//filter the unit graph so that it only contains nodes at which "child" holds
		FilteredDirectedGraph<N> filtered = new FilteredDirectedGraph<N>(g,child);
		//compute this graph's SCCs
		StronglyConnectedComponentsFast<N> sccAnalysis = new StronglyConnectedComponentsFast<N>(filtered);
		List<List<N>> sccs = sccAnalysis.getTrueComponents();
		//label the nodes in all real SCCs with "this"
		for (List<N> scc : sccs) {
			if(scc.size()>1) {
				for (N n : scc) {
					addFormula(this, n);
				}				
			}
		}
	}
}
