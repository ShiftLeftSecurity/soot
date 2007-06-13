/*
 * Created on 13-Jun-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.ds;

import java.util.Collection;
import java.util.Map;

import soot.Local;
import soot.jimple.Stmt;
import abc.tm.weaving.weaver.tmanalysis.mustalias.LocalMustAliasAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.LocalNotMayAliasAnalysis;

/**
 * MustMayNotAliasDisjunct
 *
 * @author Eric Bodden
 */
public class MustMayNotAliasDisjunct extends Disjunct {
	
	protected final LocalMustAliasAnalysis lmaa;
	protected final LocalNotMayAliasAnalysis lmna;
	protected final Map<Local, Stmt> tmLocalsToDefStatements;

	public MustMayNotAliasDisjunct(LocalMustAliasAnalysis lmaa, LocalNotMayAliasAnalysis lmna, Map<Local, Stmt> tmLocalsToDefStatements) {
		this.lmaa = lmaa;
		this.lmna = lmna;
		this.tmLocalsToDefStatements = tmLocalsToDefStatements;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Disjunct addBindingsForSymbol(Collection allVariables, Map bindings, String shadowId) {
		Disjunct clone = copy();
		//for each tracematch variable
		for (String tmVar : (Collection<String>)allVariables) {
			Local toBind = (Local) bindings.get(tmVar);
			
			//get the current binding
			Local curBinding = (Local) varBinding.get(tmVar);
			
			if(curBinding==null || !notMayAliased(tmVar, bindings)) {
				//make a copy
				clone.varBinding.put(tmVar, toBind);
				//keep track of that this edge was taken
				clone.history.add(shadowId);
			} 			
		}
		
		return clone.intern();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Disjunct addNegativeBindingsForVariable(String varName, Object negativeBinding, String shadowId) {
		if(mustAliased(varName, varBinding)) {
			return FALSE;
		} else {
			Disjunct clone = copy();
			clone.history.add(shadowId);
			return clone.intern();
		}
	}

	protected boolean notMayAliased(String s, Map binding) {
		Local currBinding = (Local) varBinding.get(s);
		Local newBinding = (Local) binding.get(s);
		
		return lmna.notMayAlias(currBinding, tmLocalsToDefStatements.get(currBinding), newBinding, tmLocalsToDefStatements.get(newBinding));
	}
	
	protected boolean mustAliased(String s, Map binding) {
		Local currBinding = (Local) varBinding.get(s);
		Local newBinding = (Local) binding.get(s);

		return lmaa.mustAlias(currBinding, tmLocalsToDefStatements.get(currBinding), newBinding, tmLocalsToDefStatements.get(newBinding));
	}
	
	
}
