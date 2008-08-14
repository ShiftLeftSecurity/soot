package soot.jimple.toolkits.ctl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import soot.jimple.toolkits.ctl.formula.IFormula;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

public class FormulaTag implements Tag {
	
	protected Set<IFormula> formulas = new HashSet<IFormula>();
	
	public boolean addFormula(IFormula f) {
		return formulas.add(f);
	}

	public String getName() {
		return FormulaTag.class.getName();
	}

	public byte[] getValue() throws AttributeValueException {
		throw new UnsupportedOperationException();
	}

	public Set<IFormula> getFormulas() {
		return Collections.unmodifiableSet(formulas);
	}
	
	@Override
	public String toString() {
		return formulas.toString();
	}

}
