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

package edu.berkeley.pa.util;

import java.util.Iterator;
import java.util.Set;

import manu.util.ArraySet;
import manu.util.HashSetMultiMap;
import manu.util.MultiMap;
import manu.util.Pair;
import soot.jimple.spark.pag.FieldRefNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.SparkField;
import soot.jimple.spark.pag.VarNode;
import edu.berkeley.pa.util.SootUtil.FieldToEdgesMap;

public class ValidMatches {

	// edges are in same direction as PAG, in the direction of value flow
	private final MultiMap<VarNode, VarNode> vMatchEdges = new HashSetMultiMap<VarNode, VarNode>();
	
	private final MultiMap<VarNode, VarNode> vMatchBarEdges = new HashSetMultiMap<VarNode, VarNode>();
	
	public ValidMatches(PAG pag, FieldToEdgesMap fieldToStores) {
		System.out.println("constructing vmatches");
		for (Iterator iter = pag.loadSources().iterator(); iter.hasNext();) {
			FieldRefNode loadSource = (FieldRefNode) iter.next();
			SparkField field = loadSource.getField();
			VarNode loadBase = loadSource.getBase();
			ArraySet<Pair<VarNode, VarNode>> storesOnField = fieldToStores.get(field);
			for (Pair<VarNode, VarNode> store : storesOnField) {
				VarNode storeBase = store.getO2();				
				if (loadBase.getP2Set().hasNonEmptyIntersection(storeBase.getP2Set())) {
					VarNode matchSrc = store.getO1();
					Node[] loadTargets = pag.loadLookup(loadSource);
					for (int i = 0; i < loadTargets.length; i++) {
						VarNode matchTgt = (VarNode) loadTargets[i];
						vMatchEdges.put(matchSrc, matchTgt);
						vMatchBarEdges.put(matchTgt, matchSrc);
					}
				}				
			}
		}
		System.out.println("done with vmatches");
	}
	
	public Set<VarNode> vMatchLookup(VarNode src) {
		return vMatchEdges.get(src);
	}

	public Set<VarNode> vMatchInvLookup(VarNode src) {
		return vMatchBarEdges.get(src);
	}
}
