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

import soot.SootMethod;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.weaver.tmanalysis.mustalias.InstanceKey;

/**
 * A disjuncts making use of must and not-may alias information.
 *
 * @author Eric Bodden
 */
public class MustMayNotAliasDisjunct extends Disjunct<InstanceKey> {
	

//    private final SootMethod container;
//    private final TraceMatch tm;
//
    /**
	 * Constructs a new disjunct.
	 */
	public MustMayNotAliasDisjunct(SootMethod container, TraceMatch tm) {
//        this.container = container;
//        this.tm = tm;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Disjunct addBindingsForSymbol(Collection allVariables, Map bindings, String shadowId, SMNode from) {
		MustMayNotAliasDisjunct clone = clone();
		//for each tracematch variable
		for (String tmVar : (Collection<String>)allVariables) {
			InstanceKey toBind = (InstanceKey) bindings.get(tmVar);

			/*
			 * Rule 1: If we want to bind a value x=o1 but we already have a positive binding
			 *         x=o2, and we know that o1!=o2 by the not-may-alias analysis, then we can
			 *         safely reduce to FALSE.
			 */
			if(notMayAliasedPositiveBinding(tmVar, toBind)) {
				return FALSE;
			}
			
			/*
			 * Rule 2: If we want to bind a value x=o1 but we already have a negative binding
			 *         x=o2, and we know that o1==o2 by the must-alias analysis, then we can
			 *         safely reduce to FALSE.
			 */
			if(mustAliasedNegativeBinding(tmVar, toBind)) {
				return FALSE;
			}
			
			/*
			 * Rule 3: If we want to bind a value x=o1 but we already have a positive binding
			 *         x=o2, and we know that o1==o2 by the must-alias analysis, then we  
			 *         can just leave the constraint unchanged, because x=o1 && x=o2 is the same as just
			 *         x=o1.
			 */
			if(mustAliasedPositiveBinding(tmVar, toBind)) {
				continue;
			}
			
			/*
			 * At this point we know that the positive binding is necessary because it does not clash with any
			 * of our other constraints and also it is not superfluous (rule 3). Hence, store it.
			 */
			clone.addPositiveBinding(tmVar, toBind);
			
			/*
			 * Rule 4: We just stored the positive binding x=o1. Now if there is a negative binding that says
			 *         x!=o2 and we know that o1!=o2 by our not-may-alias analysis, then we do not need to store that
			 *         negative binding. This is because x=o1 and o1!=o2 already implies x!=o2. 
			 */
			clone.pruneSuperflousNegativeBinding(tmVar,toBind);
		}
		
		return clone.intern();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override      
	protected Disjunct addNegativeBindingsForVariable(String tmVar, InstanceKey toBind, String shadowId) {
		/*
		 * Rule 1: If we want to bind a value x!=o1 but we already have a positive binding
		 *         x=o2, and we know that o1!=o2 by the not-may-alias analysis, then we can
		 *         just leave the constraint unchanged, as x=o2 and o1!=o2 already implies x!=o1. 
		 */
		if(notMayAliasedPositiveBinding(tmVar, toBind)) {
			return this;
		}
		
		/*
		 * Rule 2: If we want to bind a value x!=o1 but we already have a negative binding
		 *         x!=o2, and we know that o1==o2 by the must-alias analysis, then we 
		 *         can just leave the constraint unchanged, because x!=o1 && x!=o2 is the same
		 *         as just x=o1 in that case.
		 */
		if(mustAliasedNegativeBinding(tmVar, toBind)) {
			return this;
		}
		
		/*
		 * Rule 3: If we want to bind a value x!=o1 but we already have a positive binding
		 *         x=o2, and we know that o1==o2 by the must-alias analysis, then we can
		 *         safely reduce to FALSE.
		 */
		if(mustAliasedPositiveBinding(tmVar, toBind)) {
			return FALSE;
		}

		/*
		 * At this point we know that the negative binding is necessary because it does not clash with any
		 * of our other constraints and also it is not superfluous (rule 2). Hence, store it.
		 * 
		 * HOWEVER: We only need to store negative bindings which originate from within the current methods.
		 * Other bindings (1) can never lead to clashes, because they can never must-alias a binding (we don't have
		 * inter-procedural must-alias information), neither do we need it for looking up which shadows contribute to
		 * reaching a final state.
		 */
	    if(toBind.haveLocalInformation()) {
	    	return addNegativeBinding(tmVar, toBind);
	    } else {
	    	//do not need to store negative bindings from other methods
	    	return this;
	    }
	}
	
	protected Disjunct addNegativeBinding(String tmVar, InstanceKey negBinding) {
		//TODO: is this check still necessary? After all, equality on instance keys is defined via must-alias!
		
		//check if we need to add...
		//we do *not* need to add a mapping v->l is there is already a mapping
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
		//initialise if necessary
		if(negBindingsForVariable==null) {
			negBindingsForVariable = new HashSet<InstanceKey>();
			clone.negVarBinding.put(tmVar, negBindingsForVariable);
		}
		negBindingsForVariable.add(negBinding);
		return clone.intern();
	}
	
	protected void addPositiveBinding(String tmVar, InstanceKey toBind) {
		Set<InstanceKey> posBindingForVar = posVarBinding.get(tmVar);
		if(posBindingForVar==null){
			posBindingForVar = new HashSet<InstanceKey>();
			posVarBinding.put(tmVar, posBindingForVar);
		}
		posBindingForVar.add(toBind);
	}
	
	
	/**
	 * Returns <code>true</code> if there is a positive binding stored in this disjunct
	 * that must-aliases the given binding for the given variable.
	 */
	protected boolean mustAliasedPositiveBinding(String tmVar, InstanceKey toBind) {
		Set<InstanceKey> posBindingsForVar = posVarBinding.get(tmVar);
		if(posBindingsForVar!=null) {
			for (InstanceKey posBinding : posBindingsForVar) {
				if(posBinding.mustAlias(toBind)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if there is a positive binding stored in this disjunct
	 * that not-may-aliases the given binding for the given variable.
	 */
	protected boolean notMayAliasedPositiveBinding(String tmVar, InstanceKey toBind) {
		Set<InstanceKey> posBindingsForVar = posVarBinding.get(tmVar);
		if(posBindingsForVar!=null) {
			for (InstanceKey posBinding : posBindingsForVar) {
				if(posBinding.mayNotAlias(toBind)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if there is a negative binding stored in this disjunct
	 * that must-aliases the given binding for the given variable.
	 */
	protected boolean mustAliasedNegativeBinding(String tmVar, InstanceKey toBind) {
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
	 * Prunes any negative bindings for the variable tmVar that not may-alias the
	 * positive binding that is passed in for that variable.
	 * This is because thos enegative bindings are superflous. 
	 */
	protected void pruneSuperflousNegativeBinding(String tmVar, InstanceKey toBind) {
		Set<InstanceKey> negBindingsForVar = negVarBinding.get(tmVar);
		if(negBindingsForVar!=null) {
			for (Iterator<InstanceKey> iterator = negBindingsForVar.iterator(); iterator.hasNext();) {
				InstanceKey negBinding = iterator.next();
				if(negBinding.mayNotAlias(toBind)) {
					iterator.remove();
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[pos(");
        sb.append(posVarBinding.toString());
		sb.append(")-neg(");			
        sb.append(negVarBinding.toString());
		sb.append(")]");			
		return sb.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected MustMayNotAliasDisjunct clone() {
		return (MustMayNotAliasDisjunct) super.clone();
	}	

//	/**
//	 * Expects a map from variables ({@link String}s) to {@link InstanceKey}s.
//	 * Returns <code>false</code> if there exists a variable in the binding
//	 * stored in this disjunct and the binding passed in for which both instance keys
//	 * for this variable may not be aliased (and <code>true</code> otherwise).
//	 * @param b a map of bindings ({@link String} to {@link InstanceKey}) 
//	 */
//	@Override
//	public boolean compatibleBinding(Map b) {
//		Map<String,InstanceKey> binding = (Map<String,InstanceKey>)b;
//		for (String v : binding.keySet()) {
//			if(this.posVarBinding.containsKey(v)) {
//				InstanceKey storedKey = this.posVarBinding.get(v);
//				InstanceKey argKey = binding.get(v);
//				if(storedKey.mayNotAlias(argKey)) {
//					return false;
//				}
//			}
//		}		
//		return true;
//	}
}
