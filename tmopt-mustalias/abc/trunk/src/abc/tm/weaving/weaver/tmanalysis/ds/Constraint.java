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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.PointsToSet;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.weaver.tmanalysis.mustalias.TMFlowAnalysis;

/**
 * Implements a single constraint. A constraint is normally associated with a state
 * and represents under which constraint a run may be in this state.
 * The easiest constraints are {@link #TRUE} and {@link #FALSE}, which mean that we
 * simply are in the state (or not).
 * 
 * We store constraints in disjunctive normal form (DNF), which means each constraint
 * is a set of disjuncts. So a constraint of the form {{v=a,w=b},{v=c}} means that
 * either the variable v points to creation side a and w to b or v points to c (and w
 * is unbound).
 * 
 * Constraints are produced using the prototype pattern, i.e. via cloning. The two prototypes are
 * {@link #TRUE} and {@link #FALSE}. Other Constraints can then be created by calling
 * {@link #addBindingsForSymbol(Collection, SMNode, Map, String, TMFlowAnalysis)} and  
 * {@link #addNegativeBindingsForSymbol(Collection, SMNode, Map, String, TMFlowAnalysis)}.
 *
 * @author Eric Bodden
 */
public class Constraint implements Cloneable {
	
	/**
	 * a most-recently used cache to cache equal constraints; the idea is that equality checks
	 * are faster if performed on "interned" instances
	 * @see #intern()
	 * @see String#intern()
	 */
	protected static Map constraintToUniqueConstraint = new HashMap();//new MemoryStableMRUCache("constraint-intern",10*1024*1024,false);
	
	public static void reset() {
		constraintToUniqueConstraint.clear();
	}


	/** The set of disjuncts for this constraint (DNF). */
	protected HashSet disjuncts;
	
	/** The unique false constraint. */
	public final static Constraint FALSE;
	
	/** The unique true constraint. */
	public final static Constraint TRUE;
	
	static {
		//initialize FALSE
		FALSE = new Constraint(new HashSet()) {			
			public String toString() {
				return "FALSE";
			}
			
			/**
			 * Returns this (FALSE).
			 */
			public Constraint addBindingsForSymbol(Collection allVariables, SMNode to, Map bindings, String shadowId, TMFlowAnalysis flowAnalysis) {
				//FALSE stays FALSE
				return this;
			}
			
			/**
			 * Returns this (FALSE).
			 */
			public Constraint addNegativeBindingsForSymbol(Collection allVariables, SMNode state, Map bindings, String shadowId) {
				//FALSE stays FALSE
				return this;
			}
			
			protected Object clone() {
				return new Constraint(disjuncts);
			}
		};
		
		//initialize TRUE; this holds a single empty disjunct
		HashSet set = new HashSet();
		set.add(Disjunct.FALSE);		
		TRUE = new Constraint(set) {
			public String toString() {
				return "TRUE";
			}
			
			protected Object clone() {
				return new Constraint(disjuncts);
			}
		};
	}
	
	/**
	 * Constructs a new constraint holding a reference to the given disjuncts. 
	 * Only to be called from inside this class (prototype pattern). Other disjuncts can be created
	 * via {@link #addBindingsForSymbol(Collection, SMNode, Map, String, TMFlowAnalysis)} and
	 * {@link #addNegativeBindingsForSymbol(Collection, SMNode, Map, String, TMFlowAnalysis)}.
	 * @param disjuncts a set of {@link Disjunct}s
	 */
	private Constraint(HashSet disjuncts) {
		this.disjuncts = (HashSet) disjuncts.clone();
	}

	/**
	 * Adds bindings for the case where the given symbol is read by taking an edge in the program graph.
	 * Also, this adds the shadow-ids of any edges that are on the path to a final state to the
	 * may-flow analysis. (see {@link TMFlowAnalysis#registerActiveShadows(Set)})
	 * @param allVariables the set of all variables bound by the symbol that is read
	 * @param to the state the state machine is driven into by taking this transition
	 * @param bindings the bindings of that edge in form of a mapping {@link String} to {@link PointsToSet}
	 * @param shadowId the shadow-id of the shadow that triggered this edge
	 * @param flowAnalysis the flow analysis; used as call-back to register active edges
	 * @return the updated constraint; this is a fresh instance, 
	 * the disjuncts of this copy hold the history of the disjuncts of this constraint plus
	 * the shadowId that is passed in
	 */
	public Constraint addBindingsForSymbol(Collection allVariables, SMNode to, Map bindings, String shadowId, TMFlowAnalysis flowAnalysis) {
		//create a set for the resulting disjuncts
		HashSet resultDisjuncts = new HashSet();
		//for all current disjuncts
		for (Iterator iter = disjuncts.iterator(); iter.hasNext();) {
			Disjunct disjunct = (Disjunct) iter.next();

			//delegate to the disjunct
			Disjunct newDisjunct = disjunct.addBindingsForSymbol(allVariables,bindings,shadowId);
			resultDisjuncts.add(newDisjunct);
			
			//if we just hit a final node
			if(to.isFinalNode()) {
				//TODO do we want to do something in this case?
			}
		}
		
		//references to FALSE are useless in DNF
		resultDisjuncts.remove(Disjunct.FALSE);
		if(resultDisjuncts.isEmpty()) {
			//if no disjunts are left, this means nothing else but FALSE
			return FALSE;	

		} else {
			//return an interned version of the the updated copy;
			//the disjuncts of this copy hold clones of the history of the original disjuncts
			//(plus the id of the shadow that triggered the current edge)
			return new Constraint(resultDisjuncts).intern();
		}		
	}
	
	/**
	 * Adds negative bindings for the case where the given symbol is read by taking a <i>skip</i> edge in the program graph.
	 * Effectively this deletes all bindings which adhere to the binding which is passed in.
	 * Note that unlike in {@link #addBindingsForSymbol(Collection, SMNode, Map, int, TMFlowAnalysis)}
	 * here we do not need to update the history of the disjuncts, because we know that no skip-loop
	 * can ever possibly lead to a final node.
	 * @param allVariables the set of all variables bound by the symbol that is read
	 * @param state the state in the state-machine which the skip-loop which is taken is connected to 
	 * @param bindings the bindings of that skip-edge in form of a mapping {@link String} to {@link PointsToSet}
	 * @param shadowId the shadow-id of the shadow that triggered this edge
	 * @param analysis the may-flow analysis; used as call-back to register active edges
	 * @return the updated constraint; this is a fresh instance or {@link #FALSE} 
	 */
	public Constraint addNegativeBindingsForSymbol(Collection allVariables, SMNode state, Map bindings, String shadowId) {
		HashSet resultDisjuncts = new HashSet();
		//for each disjunct
		for (Iterator iter = disjuncts.iterator(); iter.hasNext();) {
			Disjunct disjunct = (Disjunct) iter.next();
			
			resultDisjuncts.addAll(disjunct.addNegativeBindingsForSymbol(allVariables,bindings,shadowId));
		}
		
		//references to FALSE are useless in DNF
		resultDisjuncts.remove(Disjunct.FALSE);
		if(resultDisjuncts.isEmpty()) {
			//if no disjunts are left, this means nothing else but FALSE
			return FALSE;
		} else {
			//return an interned version of the updated copy;
			//the disjuncts of this copy hold clones of the history of the original disjuncts
			//(plus the id of the shadow that triggered the current edge)
			return new Constraint(resultDisjuncts).intern();
		}		
	}

	/**
	 * Constructs and returns a constraint representing <code>this</code>
	 * <i>or</i> <code>other</code> by adding all disjuncts from other to a clone of this constraint.
	 * @param other some other constraint
	 * @return the disjoint constraint
	 */
	public Constraint or(Constraint other) {
		Constraint copy = (Constraint) clone();
		copy.disjuncts.addAll(other.disjuncts);
		return copy;
	}
	
	/**
	 * Interns the constraint, i.e. returns a (usually) unique equal instance for it.
	 * @return a unique instance that is equal to this 
	 */
	protected Constraint intern() {
		Constraint cached = (Constraint) constraintToUniqueConstraint.get(this);
		if(cached==null) {
			cached = this;
			constraintToUniqueConstraint.put(this, this);
		}
		return cached;
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected Object clone() {
		try {
			Constraint clone = (Constraint) super.clone();
			clone.disjuncts = (HashSet) disjuncts.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @returnthe number of disjuncts in this constraint
	 */
	public int size() {		
		return disjuncts.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((disjuncts == null) ? 0 : disjuncts.hashCode());
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
		final Constraint other = (Constraint) obj;
		if (disjuncts == null) {
			if (other.disjuncts != null)
				return false;
		} else if (!disjuncts.equals(other.disjuncts))
			return false;
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return disjuncts.toString();
	}	
	
}
