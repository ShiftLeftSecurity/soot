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

package manu.util;

import java.util.Arrays;

public final class DisjointSets {

	private int[] array;

	/**
	 * Construct a disjoint sets object.
	 * 
	 * @param numElements
	 *            the initial number of elements--also the initial number of
	 *            disjoint sets, since every element is initially in its own
	 *            set.
	 */
	public DisjointSets(int numElements) {
		array = new int[numElements];
        Arrays.fill(array, -1);
	}

	/**
	 * union() unites two disjoint sets into a single set. A union-by-size
	 * heuristic is used to choose the new root. This method will corrupt the
	 * data structure if root1 and root2 are not roots of their respective sets,
	 * or if they're identical.
	 * 
	 * @param root1
	 *            the root of the first set.
	 * @param root2
	 *            the root of the other set.
	 */
	public void union(int root1, int root2) {
		assert array[root1] < 0;
		assert array[root2] < 0;
		assert root1 != root2;
		if (array[root2] < array[root1]) { // root2 has larger tree
			array[root2] += array[root1]; // update # of items in root2's tree
			array[root1] = root2; // make root2 new root
		} else { // root1 has equal or larger tree
			array[root1] += array[root2]; // update # of items in root1's tree
			array[root2] = root1; // make root1 new root
		}
	}

	/**
	 * find() finds the (int) name of the set containing a given element.
	 * Performs path compression along the way.
	 * 
	 * @param x
	 *            the element sought.
	 * @return the set containing x.
	 */
	public int find(int x) {
		if (array[x] < 0) {
			return x; // x is the root of the tree; return it
		} else {
			// Find out who the root is; compress path by making the root x's
			// parent.
			array[x] = find(array[x]);
			return array[x]; // Return the root
		}
	}
}