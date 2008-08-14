package soot.jimple.toolkits.ctl.formula;

import java.util.List;

import soot.tagkit.Host;
import soot.toolkits.graph.DirectedGraph;

public interface IFormula extends Comparable {
	
	List<IFormula> closure();

	public void closure(List<IFormula> result);

	public int depth();
	
	public boolean label(Host node, List<Host> succs);
	
	public <N extends Host> void preprocess(DirectedGraph<N> g);
}
