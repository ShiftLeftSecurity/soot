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

import soot.SootField;
import soot.jimple.spark.pag.ArrayElement;
import soot.jimple.spark.pag.SparkField;

/**
 * for hacking around with stuff
 * 
 * @author manu
 * 
 */
public class ManualFieldCheckHeuristic implements FieldCheckHeuristic {

    private boolean allNotBothEnds = false;

    public boolean runNewPass() {
        if (!allNotBothEnds) {
            allNotBothEnds = true;
            return true;
        }
        return false;
    }

    private static final String[] importantTypes = new String[] {
//            "ca.mcgill.sable.util.ArrayList",
//            "ca.mcgill.sable.util.ArrayList$ArrayIterator",
//            "ca.mcgill.sable.util.AbstractList$AbstractListIterator",
            /*"ca.mcgill.sable.util.VectorList",*/ "java.util.Vector",
            "java.util.Hashtable", "java.util.Hashtable$Entry",
            "java.util.Hashtable$Enumerator", "java.util.LinkedList",
            "java.util.LinkedList$Entry", "java.util.AbstractList$Itr",
//            "ca.mcgill.sable.util.HashMap", "ca.mcgill.sable.util.LinkedList",
//            "ca.mcgill.sable.util.LinkedList$LinkedListIterator",
//            "ca.mcgill.sable.util.LinkedList$Node",
            /*"ca.mcgill.sable.soot.TrustingMonotonicArraySet",*/ "java.util.Vector$1",
            "java.util.ArrayList", };

    private static final String[] notBothEndsTypes = new String[] {
            "java.util.Hashtable$Entry", "java.util.LinkedList$Entry", /*"ca.mcgill.sable.util.LinkedList$Node"*/ };

    public boolean validateMatchesForField(SparkField field) {
        if (field instanceof ArrayElement) {
            return true;
        }
        SootField sootField = (SootField) field;
        String fieldTypeStr = sootField.getDeclaringClass().getType()
                .toString();
        for (String typeName : importantTypes) {
            if (fieldTypeStr.equals(typeName)) {
                return true;
            }
        }
        return false;
    }

    public boolean validFromBothEnds(SparkField field) {
        if (allNotBothEnds) {
            return false;
        }
        if (field instanceof SootField) {
            SootField sootField = (SootField) field;
            String fieldTypeStr = sootField.getDeclaringClass().getType()
                    .toString();
            for (String typeName : notBothEndsTypes) {
                if (fieldTypeStr.equals(typeName)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Manual annotations";
    }

}
