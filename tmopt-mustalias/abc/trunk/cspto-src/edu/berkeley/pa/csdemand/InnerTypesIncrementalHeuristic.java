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

import java.util.HashSet;
import java.util.Set;

import manu.util.Util;

import soot.RefType;
import soot.Scene;
import soot.SootField;
import soot.jimple.spark.internal.TypeManager;
import soot.jimple.spark.pag.ArrayElement;
import soot.jimple.spark.pag.SparkField;
import edu.berkeley.pa.util.SootUtil;

public class InnerTypesIncrementalHeuristic implements FieldCheckHeuristic {

    private final TypeManager manager;

    private final Set<RefType> typesToCheck = new HashSet<RefType>();

    private String newTypeOnQuery = null;

    private final Set<RefType> bothEndsTypes = new HashSet<RefType>();

    private final Set<RefType> notBothEndsTypes = new HashSet<RefType>();

    private int numPasses = 0;

    private final int passesInDirection;

    private boolean allNotBothEnds = false;

    public InnerTypesIncrementalHeuristic(TypeManager manager, int maxPasses) {
        this.manager = manager;
        this.passesInDirection = maxPasses / 2;
    }

    public boolean runNewPass() {
        numPasses++;
        if (numPasses == passesInDirection) {
            return switchToNotBothEnds();
        } else {
            if (newTypeOnQuery != null) {
                String topLevelTypeStr = Util.topLevelTypeString(newTypeOnQuery);
                RefType refType = Scene.v().getRefType(topLevelTypeStr);
                boolean added = typesToCheck.add(refType);
                newTypeOnQuery = null;
                return added;
            } else {
                return switchToNotBothEnds();
            }
        }
    }

    private boolean switchToNotBothEnds() {
        if (!allNotBothEnds) {
            numPasses = 0;
            allNotBothEnds = true;
            newTypeOnQuery = null;
            typesToCheck.clear();
            return true;
        } else {
            return false;
        }
    }

    public boolean validateMatchesForField(SparkField field) {
        if (field instanceof ArrayElement) {
            return true;
        }
        SootField sootField = (SootField) field;
        RefType declaringType = sootField.getDeclaringClass().getType();
        String declaringTypeStr = declaringType.toString();
        String topLevel = Util.topLevelTypeString(declaringTypeStr);
        RefType refType = Scene.v().getRefType(topLevel);
        for (RefType checkedType : typesToCheck) {
            if (manager.castNeverFails(checkedType, refType)) {
                // System.err.println("validate " + declaringTypeStr);
                return true;
            }
        }
        if (newTypeOnQuery == null) {
            newTypeOnQuery = declaringTypeStr;
        }
        return false;
    }

    public boolean validFromBothEnds(SparkField field) {
        if (allNotBothEnds) {
            return false;
        }
        if (field instanceof ArrayElement) {
            return true;
        }
        SootField sootField = (SootField) field;
        RefType declaringType = sootField.getDeclaringClass().getType();
        if (bothEndsTypes.contains(declaringType)) {
            return true;
        } else if (notBothEndsTypes.contains(declaringType)) {
            return false;
        } else {
            if (SootUtil.hasRecursiveField(declaringType.getSootClass())) {
                notBothEndsTypes.add(declaringType);
                return false;
            } else {
                bothEndsTypes.add(declaringType);
                return true;
            }
        }
    }

    @Override
    public String toString() {
        return typesToCheck.toString();
    }
}
