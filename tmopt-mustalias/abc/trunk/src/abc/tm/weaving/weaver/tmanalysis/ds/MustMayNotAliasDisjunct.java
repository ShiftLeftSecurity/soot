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

import soot.SootMethod;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.weaver.tmanalysis.mustalias.InstanceKey;
import abc.tm.weaving.weaver.tmanalysis.mustalias.ShadowSideEffectsAnalysis;

/**
 * A disjuncts making use of must and not-may alias information.
 *
 * @author Eric Bodden
 */
public class MustMayNotAliasDisjunct extends Disjunct<InstanceKey> {
	

    private final SootMethod container;
    private final TraceMatch tm;

    /**
	 * Constructs a new disjunct.
	 */
	public MustMayNotAliasDisjunct(SootMethod container, TraceMatch tm) {
        this.container = container;
        this.tm = tm;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Disjunct addBindingsForSymbol(Collection allVariables, Map bindings, String shadowId, SMNode from) {
		Disjunct clone = clone();
		//for each tracematch variable
		for (String tmVar : (Collection<String>)allVariables) {
			InstanceKey toBind = (InstanceKey) bindings.get(tmVar);

			//clash with negative binding?
			if(clashWithNegativeBinding(tmVar,toBind)) {
				return FALSE;
			}
			
			//get the current binding
			InstanceKey curBinding = (InstanceKey) varBinding.get(tmVar);
			
			if(curBinding==null) {
			    //if we have no binding, we only generate a new binding if
			    //either we do a transition out of an initial state or we have shadows with an overlapping binding in other methods			    
			    if(from.isInitialNode() || !allShadowsWithOverLappingBindingInSameMethod(tmVar,toBind,from)) {
    				//set the new binding
    				clone.varBinding.put(tmVar, toBind);
    				//keep track of that this edge was taken
    				//clone.history.add(shadowId);
			    } else {
	                /* Optimization: if toBind.haveLocalInformation() is false, it means that we have an instance key from another method.
	                 * Such instance keys should only propagate existing bindings to new states (potentially a final one). They should not, however,
	                 * generate new bindings. Hence in the case where no binding exists yet, return FALSE.  
	                 */             
			        return FALSE;
			    }
			} else if(curBinding.mayNotAlias(toBind)) {
			    //FIXME which positive value do we have to bind here? do we have to bind both, even (at least of they don't must-alias)?
				return FALSE;			
			}
		}
		
		return clone.intern();
	}

    //TODO RENAME AND COMMENT
	protected boolean allShadowsWithOverLappingBindingInSameMethod(String tmVar, InstanceKey toBind, SMNode from) {
		return ShadowSideEffectsAnalysis.v().allShadowsWithOverLappingBindingInSameMethod(tmVar, toBind.getLocal(), container, tm, from);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override      
	protected Disjunct addNegativeBindingsForVariable(String tmVar, InstanceKey newBinding, String shadowId) {
		InstanceKey curBinding = varBinding.get(tmVar);
		if(curBinding!=null && curBinding.mustAlias(newBinding)) {
			return FALSE;
		} else {
		    if(newBinding.haveLocalInformation()) {
		        return addNegativeBinding(tmVar, newBinding);
		    } else {
		        //do not need to store negative bindings from other methods
		        return this;
		    }
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

	/**
	 * Expects a map from variables ({@link String}s) to {@link InstanceKey}s.
	 * Returns <code>false</code> if there exists a variable in the binding
	 * stored in this disjunct and the binding passed in for which both instance keys
	 * for this variable may not be aliased (and <code>true</code> otherwise).
	 * @param b a map of bindings ({@link String} to {@link InstanceKey}) 
	 */
	@Override
	public boolean compatibleBinding(Map b) {
		Map<String,InstanceKey> binding = (Map<String,InstanceKey>)b;
		for (String v : binding.keySet()) {
			if(this.varBinding.containsKey(v)) {
				InstanceKey storedKey = this.varBinding.get(v);
				InstanceKey argKey = binding.get(v);
				if(storedKey.mayNotAlias(argKey)) {
					return false;
				}
			}
		}		
		return true;
	}
}
