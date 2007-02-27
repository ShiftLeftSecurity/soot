/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Eric Bodden
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

import static abc.tm.weaving.weaver.tmanalysis.ds.Disjunct.FALSE;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.PointsToSet;
import soot.jimple.Stmt;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.weaver.tmanalysis.TMFlowAnalysis;

/**
 * A disjunct represents a mapping of variables (type {@link String}) to
 * creation sites (type {@link PointsToSet}).
 * Also it holds a history, which is the set of shadow-ids of the shadows
 * of all edges in the program-graph that drove this disjunct into its current state.
 * 
 * This class is <i>not</i> threadsafe due to {@link #COMPARE_ON_HISTORY}.
 * 
 *  DDisjuncts are produced using the prototype pattern, i.e. via cloning. The prototype is
 * {@link #FALSE}. Other Disjuncts can then be created by calling
 * {@link #addBindingsForSymbol(Collection, Map, String)} and  
 * {@link #addNegativeBindingsForSymbol(Collection, Map, String)}.
 *
 * @author Eric Bodden
 */
public class Disjunct implements Cloneable {
	
	/**
	 * a most-recently used cache to cache equal disjuncts; the idea is that equality checks
	 * are faster if performed on "interned" instances
	 * @see #intern()
	 * @see String#intern()
	 */
//	protected static Map disjunctToUniqueDisjunct = new HashMap();//new MemoryStableMRUCache("disjunct-intern",10*1024*1024,false);
	
	public static void reset() {
//		disjunctToUniqueDisjunct.clear();
	}
	
	/** The unique FALSE disjunct. It holds no mapping and no history. */
	protected static Disjunct FALSE = new Disjunct() {
		public String toString() {
			return "FALSE";
		}
		
		/**
		 * @return a fresh, empty disjunct
		 */
		protected Disjunct copy() {
			//we do not clone here, because then the clone would
			//always show "FALSE" in toString()
			return new Disjunct(); 
		}
		
		/**
		 * Returns the empty set.
		 */
		public Set addNegativeBindingsForSymbol(Collection allVariables, Map bindings, String shadowId, TMFlowAnalysis analysis) {
			//return no disjuncts, because there is nothing to remove
			return Collections.EMPTY_SET;
		}
	};

	/** The history holds the edges we came from.
	 *  If we reach a final state, all those edges have be to marked
	 *  as "this edge may be used for reaching a final state". */
	protected HashSet history;
		
	/** The variable mapping of the form {@link String} to {@link PointsToSet}. */
	protected HashMap varBinding;
	
//	/** The negative variable mapping of the form {@link String} to {@link PointsToSet}. */
//	protected HashMap negBinding;

	/**
	 * Creates a new disjunct with empty bindings and history.
	 * Only to be called form this class (prototype pattern.) 
	 * Other disjuncts can be created using
	 * {@link #addBindingsForSymbol(Collection, Map, String)} and 
	 * {@link #addNegativeBindingsForSymbol(Collection, Map, String)}.
	 */
	private Disjunct() {
		this.varBinding = new HashMap();
//		this.negBinding = new HashMap();
		this.history = new HashSet();
	}
	
	/**
	 * Adds bindings to this disjunct, adding the shadowId to the history of the new disjunct.
	 * @param allVariables the set of all variables bound by the symbol that is read
	 * @param bindings the bindings of that edge in form of a mapping {@link String} to {@link PointsToSet}
	 * @param shadowId the shadow-id of the shadow that triggered this edge
	 * @return the updated disjunct; this is a fresh instance, 
	 * the disjuncts of this copy hold the history of the disjuncts of this constraint plus
	 * the shadowId that is passed in
	 */
	public Disjunct addBindingsForSymbol(Collection allVariables, Map bindings, String shadowId) {
		
		//make a copy
		Disjunct result = copy();
		
		//for each tracematch variable
		for (Iterator varIter = allVariables.iterator(); varIter.hasNext();) {
			String variableName = (String) varIter.next();
			PointsToSet toBind = (PointsToSet) bindings.get(variableName);
			
			//we want to bind this value, so really its points-to set should not be empty
			assert !toBind.isEmpty();
			
			//get the current binding
			PointsToSet curBinding = (PointsToSet) varBinding.get(variableName);
			
			//if there is an old binding intersect with the new one;
			//if there is no binding yet, simply set the new binding
			PointsToSet intersection =
				(curBinding==null) ? toBind : Intersection.intersect(toBind, curBinding);
			
			if(intersection.isEmpty()) {
				//if there is no intersection, the new binding is incompatible with the current one;
				//hence return false
				return FALSE;
			} else {
				//else, store the intersection				
				result.varBinding.put(variableName, intersection);
			}
		}
		
		//if we get here, we have a non-false constraint
		assert result!=FALSE;
		
		//keep track of that this edge was taken
		result.history.add(shadowId);

		
		return result;
		//return an interned version of the result
//		return result.intern(); 
	}
	
	/**
	 * Adds negative bindings for the case where the given symbol is read by taking a <i>skip</i> edge in the program graph.
	 * Effectively this deletes all bindings which adhere to the binding which is passed in.
	 * Note that unlike in {@link #addBindingsForSymbol(Collection, Map, int)}
	 * here we do not need to update the history of the disjuncts, because we know that no skip-loop
	 * can ever possibly lead to a final node.
	 * @param allVariables the set of all variables bound by the symbol that is read
	 * @param bindings the bindings of that skip-edge in form of a mapping {@link String} to {@link PointsToSet}
	 * @param shadowId the shadow-id of the shadow that triggered this edge
	 * @param analysis 
	 * @param analysis 
	 * @param stmt the statement the current shadow applies to
	 * @return the updated constraint; this is a fresh instance or {@link #FALSE} 
	 */
	public Set addNegativeBindingsForSymbol(Collection allVariables, Map bindings, String shadowId, TMFlowAnalysis analysis, Stmt stmt) {		
		
		/*
		 * TODO (Eric)
		 * when fully implementing negative bindings in the future, take care of the following issue:
		 * currently it can be the case that references to tags are copied in Soot (e.g. onto traps), which
		 * might lead to "stuttering"; possible solution: only always "recognize" the first reference to any tag in each method  
		 */
		
		//if there are no variables, there is nothing to do
		if(allVariables.isEmpty()) {
			return Collections.EMPTY_SET;
		}
		
		Set resultSet = new HashSet();
		
		//for each tracematch variable, add the negative bindings for that variable
		for (Iterator varIter = allVariables.iterator(); varIter.hasNext();) {
			String varName = (String) varIter.next();

			resultSet.add( addNegativeBindingsForVariable(varName, (PointsToSet) bindings.get(varName), shadowId, analysis, stmt) );
		}
		
		return resultSet;
	}

	/**
	 * Currently this just returns a clone of <code>this</code>. We need a must-alias and must--flow analysis
	 * in order to do anything more clever.
	 * @param varName the name of the variable for which the binding is to be updated
	 * @param negativeBinding the negative binding this variable should be updated with
	 * @param shadowId the shadow-id of the shadow that triggered this edge
	 * @param analysis 
	 * @param stmt the statement the current shadow applies to
	 * @return
	 */
	protected Disjunct addNegativeBindingsForVariable(String varName, PointsToSet negativeBinding, String shadowId, TMFlowAnalysis analysis, Stmt stmt) {
		
		//TODO
		
//		if(analysis.mustAlias(l1, l2, stmt)) {
//			
//		}
//		
		
		
		PointsToSet curBinding = (PointsToSet) varBinding.get(varName);
		Disjunct result = copy();
		if(curBinding == null || curBinding.hasNonEmptyIntersection(negativeBinding)) {
			result.history.add(shadowId);
		} 
		//return an interned version of the result
		return result;//.intern();
	}
	
	/**
	 * Returns <code>true</code> if there are still objects possibly being strongly referenced at this
	 * point in the program.
	 * @param state the state machine state which we moved to
	 * @return <code>true</code> if there are still objects possibly being strongly referenced at this
	 * point in the program, <code>false</code> otherwise
	 */
	public boolean validate(SMNode state) {
		//this should return false only when it is CERTAIN that the object bound by this disjunct
		//cannot be strongly referenced any more, i.e. is available for GC
		return true;
	}
	
	/**
	 * Returns the history for this disjunct.
	 * @return A set containing IDs of all shadows that triggered a transition
	 * which drove this disjunct into its current state.
	 */
	public Set getHistory() {
		return Collections.unmodifiableSet(history);
	}

	/**
	 * Creates a copy of this disjunct. The method assures that this copy can
	 * be altered.
	 * @return this default implementation returns {@link #clone()}
	 */
	protected Disjunct copy() {		
		return (Disjunct) clone();
	}
	
//	/**
//	 * Interns the disjunct, i.e. returns a (usually) unique equal instance for it.
//	 * @return a unique instance that is equal to this 
//	 */
//	protected Disjunct intern() {
//		Disjunct cached = (Disjunct) disjunctToUniqueDisjunct.get(this);
//		if(cached==null) {
//			cached = this;
//			disjunctToUniqueDisjunct.put(this, this);
//		}
//		return cached;
//	}	
	
	/**
	 * {@inheritDoc}
	 */
	protected Object clone() {		
		try {
			Disjunct clone = (Disjunct) super.clone();
			clone.varBinding = (HashMap) varBinding.clone();
//			clone.negBinding = (HashMap) negBinding.clone();
			clone.history = (HashSet) history.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return varBinding.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((history == null) ? 0 : history.hashCode());
		result = PRIME * result + ((varBinding == null) ? 0 : varBinding.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Disjunct other = (Disjunct) obj;
		if (history == null) {
			if (other.history != null)
				return false;
		} else if (!history.equals(other.history))
			return false;
		if (varBinding == null) {
			if (other.varBinding != null)
				return false;
		} else if (!varBinding.equals(other.varBinding))
			return false;
		return true;
	}
	
	public boolean isSmallerThan(Disjunct other) {
		return other.history.containsAll(history) && (other.history.size()>history.size()) && other.varBinding.equals(varBinding); 
	}

	public boolean hasSameBindings(Disjunct other) {
		return varBinding.hashCode()==other.varBinding.hashCode() && varBinding.equals(other.varBinding);
	}
	
//	/**
//	 * WARNING: This comparator is NOT (!!!) consistent with equals()!
//	 *
//	 * @author Eric Bodden
//	 */
//	protected static class BindingsOnlyComparator implements Comparator {
//
//		public int compare(Object o1, Object o2) {
//			Disjunct d1 = (Disjunct) o1;
//			Disjunct d2 = (Disjunct) o2;
//			return d1.varBinding.hashCode() - d2.varBinding.hashCode();
//		}
//	}
//	
//	public final static Comparator BINDINGS_ONLY = new BindingsOnlyComparator();

	public Disjunct mergeWith(Disjunct other) {
		assert hasSameBindings(other);
		Disjunct merged = copy();
		merged.history.addAll(other.history);
		return merged;
	}

}