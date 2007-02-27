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
import soot.SootField;
import soot.jimple.spark.internal.TypeManager;
import soot.jimple.spark.pag.ArrayElement;
import soot.jimple.spark.pag.SparkField;
import edu.berkeley.pa.util.SootUtil;
import edu.berkeley.pa.util.SootUtil.CallSiteAndContext;

public class IncrementalTypesHeuristic implements FieldCheckHeuristic {

    private final TypeManager manager;

    private static final boolean EXCLUDE_TYPES = false;

    private static final String[] EXCLUDED_NAMES = new String[] { "ca.mcgill.sable.soot.SootMethod" };

    private Set<RefType> typesToCheck = new HashSet<RefType>();

    private Set<RefType> notBothEndsTypes = new HashSet<RefType>();

    private RefType newTypeOnQuery = null;


    /*
     * (non-Javadoc)
     * 
     * @see AAA.algs.Heuristic#newQuery()
     */
    public boolean runNewPass() {
//        if (!aggressive && reachedAggressive) {
//            aggressive = true;
//            return true;
//        }
        if (newTypeOnQuery != null) {
            boolean added = typesToCheck.add(newTypeOnQuery);
            if (SootUtil.hasRecursiveField(newTypeOnQuery.getSootClass())) {
                notBothEndsTypes.add(newTypeOnQuery);
            }
            newTypeOnQuery = null;
            return added;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see AAA.algs.Heuristic#validateMatchesForField(soot.jimple.spark.pag.SparkField)
     */
    public boolean validateMatchesForField(SparkField field) {
        // if (true) return true;
        if (field instanceof ArrayElement) {
            return true;
        }
        SootField sootField = (SootField) field;
        RefType declaringType = sootField.getDeclaringClass().getType();
        if (EXCLUDE_TYPES) {
            for (String typeName : EXCLUDED_NAMES) {
                if (Util.stringContains(declaringType.toString(), typeName)) {
                    return false;
                }
            }
        }
        for (RefType typeToCheck : typesToCheck) {
            if (manager.castNeverFails(declaringType, typeToCheck)) {
                return true;
            }
        }
        if (newTypeOnQuery == null) {
            newTypeOnQuery = declaringType;
            // System.err.println("adding type " + declaringType);
        }
        // System.err.println("false for " + field);
        return false;
    }

    public IncrementalTypesHeuristic(TypeManager manager) {
        super();
        this.manager = manager;
    }

    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append("types ");
        ret.append(typesToCheck.toString());
        if (!notBothEndsTypes.isEmpty()) {
            ret.append(" not both ");
            ret.append(notBothEndsTypes.toString());
        }
        return ret.toString();
    }

    public boolean validFromBothEnds(SparkField field) {
        if (field instanceof SootField) {
            SootField sootField = (SootField) field;
            RefType declaringType = sootField.getDeclaringClass().getType();
            for (RefType type : notBothEndsTypes) {
                if (manager.castNeverFails(declaringType, type)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean refineVirtualCall(CallSiteAndContext callSiteAndContext) {
        // TODO make real heuristic
        return true;
    }

}
