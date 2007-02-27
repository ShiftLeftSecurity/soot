/****************************************
 * 
 * Copyright (c) 2006, University of California, Berkeley.
 * All rights reserved.
 *
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 * - Redistributions of source code must retain the above copyright 
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright 
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the 
 *   distribution.
 * - Neither the name of the University of California, Berkeley nor the 
 *   names of its contributors may be used to endorse or promote 
 *   products derived from this software without specific prior written 
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ***************************************/ 

package edu.berkeley.pa.csdemand;

import java.util.Set;

import soot.PointsToSet;
import soot.jimple.spark.sets.PointsToSetInternal;

public class WrappedPointsToSet implements PointsToSet {
  
  final PointsToSetInternal wrapped;

  public PointsToSetInternal getWrapped() {
    return wrapped;
  }

  public WrappedPointsToSet(final PointsToSetInternal wrapped) {
    super();
    this.wrapped = wrapped;
  }

  public boolean hasNonEmptyIntersection(PointsToSet other) {
    if (other instanceof AllocAndContextSet) {
      return other.hasNonEmptyIntersection(this);
    } else if (other instanceof WrappedPointsToSet) {
      return hasNonEmptyIntersection(((WrappedPointsToSet) other).getWrapped());
    } else {
      return wrapped.hasNonEmptyIntersection(other);
    }
  }

  public boolean isEmpty() {
    return wrapped.isEmpty();
  }

  public Set possibleClassConstants() {
    return wrapped.possibleClassConstants();
  }

  public Set possibleStringConstants() {
    return wrapped.possibleStringConstants();
  }

  public Set possibleTypes() {
    return wrapped.possibleTypes();
  }

  public String toString() {
    return wrapped.toString();
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
		if(obj instanceof WrappedPointsToSet) {
			WrappedPointsToSet wrapper = (WrappedPointsToSet) obj;
			
			return wrapped.equals(wrapper.wrapped);
		}
		
		return obj.equals(wrapped);
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return wrapped.hashCode();
	}


}
