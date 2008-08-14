package soot.jimple.toolkits.ctl.formula;

import java.util.ArrayList;
import java.util.List;

import soot.jimple.toolkits.ctl.FormulaTag;
import soot.tagkit.Host;
import soot.toolkits.graph.DirectedGraph;

public abstract class Formula implements IFormula {

	public List<IFormula> closure() {
		ArrayList<IFormula> result = new ArrayList<IFormula>();
		closure(result);
		return result;
	}

	public abstract void closure(List<IFormula> result);
	
	public abstract int depth();
	
	public int compareTo(Object o) {
		Formula other = (Formula)o;
		return depth() - other.depth();
	}
	
	public static <N extends Host> boolean addFormula(IFormula f, N node) {
		FormulaTag tag = (FormulaTag) node.getTag(FormulaTag.class.getName());
		if(tag==null) {
			tag = new FormulaTag();
			node.addTag(tag);
		}
		return tag.addFormula(f);		
	}
	
	public static <N extends Host> boolean taggedWith(IFormula f, N node) {
		FormulaTag tag = (FormulaTag) node.getTag(FormulaTag.class.getName());
		if(tag==null) return false;
		return tag.getFormulas().contains(f);
	}
	
	public <N extends Host> void preprocess(DirectedGraph<N> g) {
		//do nothing
	}
	
}
