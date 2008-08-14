package soot.jimple.toolkits.ctl.formula;

import java.util.List;

import soot.tagkit.Host;

public class EU extends BinaryFormula {

	public EU(IFormula left, IFormula right) {
		super(left, right);
	}
	
	@Override
	public String toString() {
		return "E("+left.toString()+" U "+right.toString()+")";
	}
	
	public boolean label(Host n, List<Host> succs) {
		if(taggedWith(right, n)){
			return addFormula(this, n);
		} else if(taggedWith(left,n)) {
			for (Host succ : succs) {
				if(taggedWith(this,succ)) {
					return addFormula(this, n);
				}
			}
		}
		return false;
	}
}
