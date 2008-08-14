package soot.jimple.toolkits.ctl.formula;

import java.util.List;

import soot.tagkit.Host;

public class Or extends BinaryFormula {

	public Or(IFormula left, IFormula right) {
		super(left, right);
	}
	
	@Override
	public String toString() {
		return "("+left.toString()+" || "+right.toString()+")";
	}
	
	public boolean label(Host n, List<Host> succs) {
		if(taggedWith(left, n) || taggedWith(right, n)) return addFormula(this, n);
		return false;
	}

}
