/* Soot - a J*va Optimization Framework
 * Copyright (C) 2006 Eric Bodden
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package abc.tm.weaving.weaver.tmanalysis.ds;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import soot.PointsToSet;
import soot.jimple.paddle.PointsToSetInternal;
import soot.jimple.paddle.PointsToSetReadOnly;
import soot.jimple.toolkits.pointer.Union;

/**
 * This class wraps a paddle points-to set in order to achive compatibility with
 * points-to sets of a type different from {@link PointsToSetInternal}.
 * The main difference lies in {@link #hasNonEmptyIntersection(PointsToSet)}.
 *
 * @author Eric Bodden
 */
public class PaddlePointsToSetCompatibilityWrapper implements PointsToSet {

	/** The wrapped points-to set. */
	protected final PointsToSet paddlePts;
	
	/** The cached hashcode for {@link #paddlePts}. */
	protected int hashCode = -1;
	
	/** The cached equality comparison results for comparison of {@link #paddlePts} to other sets. */
	protected Map/*PointsToSet->Boolean*/ equals = new IdentityHashMap();

	/**
	 * @param paddlePts A paddle points-to set to wrap. This set must not change over time!
	 */
	public PaddlePointsToSetCompatibilityWrapper(PointsToSet paddlePts) {
		this.paddlePts = paddlePts;
	}

	/**
	 * Delegates so that <code>other</code> compares itself to
	 * <code>this</code>, respectively the wrapped object.
	 * That way, the object itself can do the comparison, which
	 * the paddle points-to set would not be capable of.  
	 */
	public boolean hasNonEmptyIntersection(PointsToSet other) {
	    if(other instanceof PaddlePointsToSetCompatibilityWrapper) {
			PaddlePointsToSetCompatibilityWrapper wrapper = (PaddlePointsToSetCompatibilityWrapper) other;
			return paddlePts.hasNonEmptyIntersection(wrapper.paddlePts);
		} else if(other instanceof PointsToSetReadOnly)
	    	return paddlePts.hasNonEmptyIntersection(other);
		else if(other instanceof Union)
	        return other.hasNonEmptyIntersection(this);
	    else if(other instanceof Intersection)
	        return other.hasNonEmptyIntersection(this);
	    else throw new RuntimeException("unexpected set type: "+other.getClass().getName());
	}

	/**
	 * @return
	 * @see soot.PointsToSet#isEmpty()
	 */
	public boolean isEmpty() {
		return paddlePts.isEmpty();
	}

	/**
	 * @return
	 * @see soot.jimple.paddle.PointsToSetReadOnly#possibleClassConstants()
	 */
	public Set possibleClassConstants() {
		return paddlePts.possibleClassConstants();
	}

	/**
	 * @return
	 * @see soot.jimple.paddle.PointsToSetReadOnly#possibleStringConstants()
	 */
	public Set possibleStringConstants() {
		return paddlePts.possibleStringConstants();
	}

	/**
	 * @return
	 * @see soot.jimple.paddle.PointsToSetReadOnly#possibleTypes()
	 */
	public Set possibleTypes() {
		return paddlePts.possibleTypes();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if(obj==null) {
			return false;
		}
		if(this==obj) {
			return true;
		}		
		
		//have to get around the tyranny of reference losing equality
		if(obj instanceof PaddlePointsToSetCompatibilityWrapper) {
			PaddlePointsToSetCompatibilityWrapper wrapper = (PaddlePointsToSetCompatibilityWrapper) obj;
			
			//we cache equality values;
			//this is valid because we know that points-to sets do not change at this point in time
			Boolean doesEqual = (Boolean) equals.get(wrapper.paddlePts);
			if(doesEqual==null) {
				doesEqual = new Boolean(paddlePts.equals(wrapper.paddlePts));
				equals.put(wrapper.paddlePts, doesEqual);
			}
			
			return doesEqual.booleanValue();
		}
		
		return obj.equals(paddlePts);
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
	    //we cache the hash code;
	    //this is valid because we know that the points-to set does not change at this stage
		if(hashCode==-1) {
			hashCode = paddlePts.hashCode();
		}
		return hashCode;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return paddlePts.toString();
	}
}
