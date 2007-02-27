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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

abstract class AbstractMultiMap<K, V> implements MultiMap<K, V> {

	protected final Map<K, Set<V>> map = new HashMap<K, Set<V>>();
	
	protected final boolean create;
	
	protected AbstractMultiMap(boolean create) {
		this.create = create;
	}
	
	protected abstract Set<V> createSet();
	
	protected Set<V> emptySet() {
		return Collections.<V>emptySet();
	}
    /* (non-Javadoc)
	 * @see AAA.util.MultiMap#get(K)
	 */
    public Set<V> get(K key) {
		Set<V> ret = map.get(key);
		if (ret == null) {
		    if (create) {
		        ret = createSet();
		        map.put(key, ret);
		    } else {
		        ret = emptySet();
		    }
		}
		return ret;
    }
    
    /* (non-Javadoc)
	 * @see AAA.util.MultiMap#put(K, V)
	 */
    public boolean put(K key, V val) {
		Set<V> vals = map.get(key);
		if (vals == null) {
			vals = createSet();
			map.put(key, vals);
		}		
		return vals.add(val);        
    }
    
    /* (non-Javadoc)
	 * @see AAA.util.MultiMap#remove(K, V)
	 */
    public boolean remove(K key, V val) {
	    Set<V> elems = map.get(key);
	    if (elems == null) return false;
	    boolean ret = elems.remove(val);
	    if (elems.isEmpty()) {
	        map.remove(key);
	    }
        return ret;
    }
    
    public Set<V> removeAll(K key) {
      return map.remove(key);
    }
    /* (non-Javadoc)
	 * @see AAA.util.MultiMap#keys()
	 */
    public Set<K> keySet() {
    	return map.keySet();
    }
    
    /* (non-Javadoc)
	 * @see AAA.util.MultiMap#containsKey(java.lang.Object)
	 */
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }
    
    /* (non-Javadoc)
	 * @see AAA.util.MultiMap#size()
	 */
    public int size() {
        int ret = 0;
        for (K key : keySet()) {
			ret += get(key).size();
		}
        return ret;
    }
    
    /* (non-Javadoc)
	 * @see AAA.util.MultiMap#toString()
	 */
    public String toString() {
        return map.toString();
    }

	/* (non-Javadoc)
	 * @see AAA.util.MultiMap#putAll(K, java.util.Set)
	 */
	public boolean putAll(K key, Collection<? extends V> vals) {
		Set<V> edges = map.get(key);
		if (edges == null) {
			edges = createSet();
			map.put(key, edges);
		}		
		return edges.addAll(vals);        
	}

	public void clear() {
		map.clear();
	}
    
    public boolean isEmpty() {
      return map.isEmpty();
    }
}
