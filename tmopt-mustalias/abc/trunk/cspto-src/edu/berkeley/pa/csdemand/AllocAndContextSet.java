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

import manu.util.ArraySet;
import manu.util.ImmutableStack;
import soot.PointsToSet;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;

public final class AllocAndContextSet extends ArraySet<AllocAndContext> implements PointsToSet {

  public boolean hasNonEmptyIntersection(PointsToSet other) {
    if (other instanceof AllocAndContextSet) {
      return nonEmptyHelper((AllocAndContextSet) other);
    } else if (other instanceof WrappedPointsToSet) {
      return hasNonEmptyIntersection(((WrappedPointsToSet) other).getWrapped());
    } else if (other instanceof PointsToSetInternal) {
      return ((PointsToSetInternal) other).forall(new P2SetVisitor() {

        @Override
        public void visit(Node n) {
          if (!returnValue) {
            for (AllocAndContext allocAndContext : AllocAndContextSet.this) {
              if (n.equals(allocAndContext.alloc)) {
                returnValue = true;
                break;
              }
            }
          }
        }

      });
    }
    throw new UnsupportedOperationException("can't check intersection with set of type " + other.getClass());
  }

  private boolean nonEmptyHelper(AllocAndContextSet other) {
    for (AllocAndContext otherAllocAndContext : other) {
      for (AllocAndContext myAllocAndContext : this) {
        if (otherAllocAndContext.alloc.equals(myAllocAndContext.alloc)) {
          ImmutableStack<Integer> myContext = myAllocAndContext.context;
          ImmutableStack<Integer> otherContext = otherAllocAndContext.context;
          if (myContext.topMatches(otherContext) || otherContext.topMatches(myContext)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public Set possibleClassConstants() {
    throw new UnsupportedOperationException();
  }

  public Set possibleStringConstants() {
    throw new UnsupportedOperationException();
  }

  public Set possibleTypes() {
    throw new UnsupportedOperationException();
  }
}