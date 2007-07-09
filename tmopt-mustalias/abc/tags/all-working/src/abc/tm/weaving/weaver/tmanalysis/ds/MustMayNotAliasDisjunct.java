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
import java.util.Map;
import java.util.Set;

import abc.tm.weaving.weaver.tmanalysis.mustalias.InstanceKey;

/**
 * A disjuncts making use of must and not-may alias information.
 *
 * @author Eric Bodden
 */
public class MustMayNotAliasDisjunct extends Disjunct<InstanceKey> {
	

    /**
	 * Constructs a new disjunct.
	 */
	public MustMayNotAliasDisjunct() {
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Disjunct addBindingsForSymbol(Collection allVariables, Map bindings, String shadowId) {
		Disjunct clone = clone();
		//for each tracematch variable
		for (String tmVar : (Collection<String>)allVariables) {
			InstanceKey toBind = (InstanceKey) bindings.get(tmVar);

			//clash with negative binding?
			if(clashWithNegativeBinding(tmVar,toBind)) {
				return FALSE;
			}
			
//			//TODO comment
//			if(contradictsNegativeBinding(tmVar,toBind)) {
//				return FALSE;
//			}

			//get the current binding
			InstanceKey curBinding = (InstanceKey) varBinding.get(tmVar);
			
			if(curBinding==null) {
				//set the new binding
				clone.varBinding.put(tmVar, toBind);
				//keep track of that this edge was taken
				//clone.history.add(shadowId);
			} else if(curBinding.mayNotAlias((InstanceKey) bindings.get(tmVar))) {
				return FALSE;			
			}
		}
		
		return clone.intern();
	}

//	/**
//	 * @param tmVar
//	 * @param toBind
//	 * @return
//	 */
//	private boolean contradictsNegativeBinding(String tmVar, InstanceKey toBind) {
//		for (Map.Entry<String,Set<Local>>  entry : negVarBinding.entrySet()) {
//			String negVar = entry.getKey();
//			Set<Local> negBindings = entry.getValue();
//			for (Local negBinding : negBindings) {
//				if(leadsToContradiction(tmVar, toBind, negVar, negBinding)) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}

//	/**
//	 * Assume we have a negative binding <code>x!=o</code> and we want to combine it with a positive
//	 * binding <code>y=p</code>. If we can prove that <code>y=p</code> can only ever occur with
//	 * <code>x=o</code>, this contradicts the negative binding. In this case, we return <code>true</code>.
//	 * @param tmVar the tracematch variable we bind
//	 * @param toBind an incoming positive binding for some variable
//	 * @param negVar the variable for an existing negative binding
//	 * @param negBinding the negative binding we have for negVar
//	 */
//	protected boolean leadsToContradiction(String tmVar, Local toBind, String negVar, Local negBinding) {
//		return ShadowSideEffectsAnalysis.v().leadsToContradiction(tmVar, toBind, negVar, negBinding, container, tm);
//	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override      
	protected Disjunct addNegativeBindingsForVariable(String tmVar, InstanceKey newBinding, String shadowId) {
		InstanceKey curBinding = varBinding.get(tmVar);
		if(curBinding!=null && curBinding.mustAlias(newBinding)) {
			return FALSE;
		} else {
			return addNegativeBinding(tmVar, newBinding);
		}
	}
	
	protected Disjunct addNegativeBinding(String tmVar, InstanceKey negBinding) {
		//check if we need to add...
		//we do *not* need to add a mapping v->l is there is alreade a mapping
		//v->m with mustAlias(l,m)
		Set<InstanceKey> thisNegBindingsForVariable = negVarBinding.get(tmVar);
		if(thisNegBindingsForVariable!=null) {
			for (InstanceKey instanceKey : thisNegBindingsForVariable) {
				if(instanceKey.mustAlias(negBinding)) {
					return this;
				}
			}
		}
        
		//else clone and actually add the binding...
		
		MustMayNotAliasDisjunct clone = (MustMayNotAliasDisjunct) clone();
		Set<InstanceKey> negBindingsForVariable = clone.negVarBinding.get(tmVar);
		//initialize if necessary
		if(negBindingsForVariable==null) {
			negBindingsForVariable = new HashSet<InstanceKey>();
			clone.negVarBinding.put(tmVar, negBindingsForVariable);
		}
		negBindingsForVariable.add(negBinding);
		return clone.intern();
	}

	
	protected boolean clashWithNegativeBinding(String tmVar, InstanceKey toBind) {
		Set<InstanceKey> negBindingsForVar = negVarBinding.get(tmVar);
		if(negBindingsForVar!=null) {
			for (InstanceKey negBinding : negBindingsForVar) {
				if(negBinding.mustAlias(toBind)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[pos(");
        sb.append(varBinding.toString());
		sb.append(")-neg(");			
        sb.append(negVarBinding.toString());
		sb.append(")]");			
		return sb.toString();
	}
}
