package soot.jimple.toolkits.ctl.formula;

import java.util.List;

import soot.tagkit.Host;

public class Not extends UnaryFormula {
	
	public Not(IFormula child) {
		super(child);
	}

	@Override
	protected String unop() {
		return "!";
	}
	
	public boolean label(Host n, List<Host> succs) {
		if(!taggedWith(child, n)) return addFormula(this, n);
		return false;
	}
}
