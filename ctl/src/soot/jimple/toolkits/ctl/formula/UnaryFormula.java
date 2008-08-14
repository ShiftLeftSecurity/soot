package soot.jimple.toolkits.ctl.formula;

import java.util.List;

public abstract class UnaryFormula extends Formula {

	protected final IFormula child;

	public UnaryFormula(IFormula child) {
		this.child = child;		
	}
	
	public IFormula child() {
		return child;
	}
	
	@Override
	public void closure(List<IFormula> result) {
		child.closure(result);
		result.add(this);
	}
	
	@Override
	public String toString() {
		return unop()+"("+child().toString()+")";
	}

	protected abstract String unop();
	
	@Override
	public int depth() {
		return child.depth()+1;
	}

}
