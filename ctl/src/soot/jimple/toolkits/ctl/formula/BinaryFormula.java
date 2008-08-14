package soot.jimple.toolkits.ctl.formula;

import java.util.List;

public abstract class BinaryFormula extends Formula {

	IFormula left;
	IFormula right;
	
	public BinaryFormula(IFormula left, IFormula right) {
		this.left = left;
		this.right = right;
	}
	
	public IFormula left() {
		return left;
	}
	
	public IFormula right() {
		return right;
	}
	
	@Override
	public void closure(List<IFormula> result) {
		left.closure(result);
		right.closure(result);
		result.add(this);
	}
	
	@Override
	public int depth() {
		return Math.max(left.depth(), right.depth())+1;
	}
}
