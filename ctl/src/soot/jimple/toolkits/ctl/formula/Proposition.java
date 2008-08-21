package soot.jimple.toolkits.ctl.formula;

import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.jimple.toolkits.ctl.patterns.IPattern;
import soot.tagkit.Host;

public abstract class Proposition<N extends Host> extends Formula implements IFormula {

	public abstract Map<IPattern, Set<Object>> holdsIn(N n);
	
	public void closure(List<IFormula> result) {
		result.add(this);
	}
	
	@Override
	public String toString() {
		return "p";
	}
	
	@Override
	public int depth() {
		return 1;
	}
	
	public boolean label(Host n, List<Host> succs) {
		if(holdsIn((N) n)!=null) return addFormula(this, n);
		return false;
	}

}
