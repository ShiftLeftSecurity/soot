package soot.jimple.toolkits.ctl.formula;

import java.util.List;

import soot.tagkit.Host;

public class EX extends UnaryFormula {
	
	public EX(IFormula child) {
		super(child);
	}
	
	@Override
	protected String unop() {
		return "EX";
	}
	
	public boolean label(Host n, List<Host> succs) {
		for (Host succ : succs) {
			if(taggedWith(child, succ)) {
				return addFormula(this, n);
			}
		}		
		return false;
	}

}
