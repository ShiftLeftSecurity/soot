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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.jimple.toolkits.pointer.LocalNotMayAliasAnalysis;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.mustalias.ShadowSideEffectsAnalysis;

/**
 * A disjuncts making use of must and not-may alias information.
 *
 * @author Eric Bodden
 */
public class MustMayNotAliasDisjunct extends Disjunct<Local> {
	
	protected final LocalMustAliasAnalysis lmaa;
	protected final LocalNotMayAliasAnalysis lmna;
	protected final Map<Local, Stmt> tmLocalsToDefStatements;
	protected final SootMethod container;
	protected final TraceMatch tm;

	public MustMayNotAliasDisjunct(LocalMustAliasAnalysis lmaa, LocalNotMayAliasAnalysis lmna, Map<Local, Stmt> tmLocalsToDefStatements, SootMethod container, TraceMatch tm) {
		this.lmaa = lmaa;
		this.lmna = lmna;
		this.tmLocalsToDefStatements = tmLocalsToDefStatements;
		this.container = container;
		this.tm = tm;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Disjunct addBindingsForSymbol(Collection allVariables, Map bindings, String shadowId) {
		Disjunct clone = clone();
		//for each tracematch variable
		for (String tmVar : (Collection<String>)allVariables) {
			Local toBind = (Local) bindings.get(tmVar);

			//clash with negative binding?
			if(clashWithNegativeBinding(tmVar,toBind)) {
				return FALSE;
			}
			
			//TODO comment
			if(contradictsNegativeBinding(tmVar,toBind)) {
				return FALSE;
			}

			//get the current binding
			Local curBinding = (Local) varBinding.get(tmVar);
			
			if(curBinding==null) {
				//set the new binding
				clone.varBinding.put(tmVar, toBind);
				//keep track of that this edge was taken
				//clone.history.add(shadowId);
			} else if(notMayAliased(tmVar, bindings)) {
				return FALSE;			
			}
		}
		
		return clone.intern();
	}

	/**
	 * @param tmVar
	 * @param toBind
	 * @return
	 */
	private boolean contradictsNegativeBinding(String tmVar, Local toBind) {
		for (Map.Entry<String,Set<Local>>  entry : negVarBinding.entrySet()) {
			String negVar = entry.getKey();
			Set<Local> negBindings = entry.getValue();
			for (Local negBinding : negBindings) {
				if(leadsToContradiction(tmVar, toBind, negVar, negBinding)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Assume we have a negative binding <code>x!=o</code> and we want to combine it with a positive
	 * binding <code>y=p</code>. If we can prove that <code>y=p</code> can only ever occur with
	 * <code>x=o</code>, this contradicts the negative binding. In this case, we return <code>true</code>.
	 * @param tmVar the tracematch variable we bind
	 * @param toBind an incoming positive binding for some variable
	 * @param negVar the variable for an existing negative binding
	 * @param negBinding the negative binding we have for negVar
	 */
	protected boolean leadsToContradiction(String tmVar, Local toBind, String negVar, Local negBinding) {
		return ShadowSideEffectsAnalysis.v().leadsToContradiction(tmVar, toBind, negVar, negBinding, container, tm);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Disjunct addNegativeBindingsForVariable(String tmVar, Local newBinding, String shadowId) {
		Local curBinding = (Local) varBinding.get(tmVar);
		if(curBinding!=null && mustAliased(tmVar, newBinding)) {
			return FALSE;
		} else {
			return addNegativeBinding(tmVar, newBinding);
		}
	}
	
	protected Disjunct addNegativeBinding(String tmVar, Local negBinding) {
		//check if we need to add...
		//we do *not* need to add a mapping v->l is there is alreade a mapping
		//v->m with mustAlias(l,m)
		Set<Local> thisNegBindingsForVariable = negVarBinding.get(tmVar);
		if(thisNegBindingsForVariable!=null) {
			for (Local local : thisNegBindingsForVariable) {
				if(mustAliased(negBinding, local)) {
					return this;
				}
			}
		}
		//else clone and actually add the binding...
		
		MustMayNotAliasDisjunct clone = (MustMayNotAliasDisjunct) clone();
		Set<Local> negBindingsForVariable = clone.negVarBinding.get(tmVar);
		//initialize if necessary
		if(negBindingsForVariable==null) {
			negBindingsForVariable = new HashSet<Local>();
			clone.negVarBinding.put(tmVar, negBindingsForVariable);
		}
		negBindingsForVariable.add(negBinding);
		return clone.intern();
	}

	
	protected boolean clashWithNegativeBinding(String tmVar, Local toBind) {
		Set<Local> negBindingsForVar = negVarBinding.get(tmVar);
		if(negBindingsForVar!=null) {
			for (Local negBinding : negBindingsForVar) {
				if(mustAliased(negBinding,toBind)) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean notMayAliased(String s, Map binding) {
		Local currBinding = (Local) varBinding.get(s);
		Local newBinding = (Local) binding.get(s);
		
		return lmna.notMayAlias(currBinding, tmLocalsToDefStatements.get(currBinding), newBinding, tmLocalsToDefStatements.get(newBinding));
	}
	
	protected boolean mustAliased(String s, Local newBinding) {
		Local currBinding = (Local) varBinding.get(s);

		return lmaa.mustAlias(currBinding, tmLocalsToDefStatements.get(currBinding), newBinding, tmLocalsToDefStatements.get(newBinding));
	}
	
	protected boolean mustAliased(Local l1, Local l2) {
		return lmaa.mustAlias(l1, tmLocalsToDefStatements.get(l1), l2, tmLocalsToDefStatements.get(l2));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[pos(");
		for (Iterator<Map.Entry<String,Local>> iterator = varBinding.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String,Local> entry = iterator.next();
			String tmVariable = entry.getKey();
			sb.append(tmVariable);
			sb.append("->");
			Local l = entry.getValue();
			String stringRepresentation = lmaa.instanceKeyString(l, tmLocalsToDefStatements.get(l));
			sb.append(stringRepresentation);
			if(iterator.hasNext())
				sb.append(", ");
		}
		sb.append(")-neg(");			
		for (Iterator<Map.Entry<String,Set<Local>>> iterator = negVarBinding.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String,Set<Local>> entry = iterator.next();
			String tmVariable = entry.getKey();
			sb.append(tmVariable);
			sb.append("->");
			Set<Local> locals = entry.getValue();
			sb.append("{");			
			for (Iterator localIter = locals.iterator(); localIter.hasNext();) {
				Local l = (Local) localIter.next();
				String stringRepresentation = lmaa.instanceKeyString(l, tmLocalsToDefStatements.get(l));
				sb.append(stringRepresentation);
				if(localIter.hasNext())
					sb.append(", ");
			}
			sb.append("}");			
			if(iterator.hasNext())
				sb.append(", ");
		}
		sb.append(")]");			
		return sb.toString();
	}
	
	
}
