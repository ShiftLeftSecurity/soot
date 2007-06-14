/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.tm.weaving.weaver.tmanalysis.ds;

import java.util.Collection;
import java.util.Map;

import soot.Local;
import soot.jimple.Stmt;
import abc.tm.weaving.weaver.tmanalysis.mustalias.LocalMustAliasAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.LocalNotMayAliasAnalysis;

/**
 * A disjuncts making use of must and not-may alias information.
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
				//clone.history.add(shadowId);
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
			//clone.history.add(shadowId);
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
