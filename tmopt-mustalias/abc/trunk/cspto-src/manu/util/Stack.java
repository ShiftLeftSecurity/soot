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

/**
 * @author manu_s
 *
 */
public final class Stack<T> implements Cloneable {

    private T[] elems;

    private int size = 0;

    @SuppressWarnings("unchecked")
    public Stack(int numElems_) {
        elems = (T[])new Object[numElems_];
    }

    public Stack() {
        this(4);
    }

    @SuppressWarnings("unchecked")
    public void push(T obj_) {
        assert obj_ != null;
        if (size == elems.length) {
            // lengthen array
            Object[] tmp = elems;
            elems = (T[])new Object[tmp.length * 2];
            System.arraycopy(tmp, 0, elems, 0, tmp.length);
        }
        elems[size] = obj_;
        size++;
    }
    
    public void pushAll(Collection<T> c) {
    	for (T t : c) {
			push(t);
		}
    }
    public T pop() {
    	if (size == 0) return null;
    	size--;
    	T ret =  elems[size];
    	elems[size] = null;
    	return ret;
    }

    public T peek() {
    	if (size == 0) return null;
    	return elems[size - 1];
    }
    
    public int size() {
    	return size;
    }
    
    public boolean isEmpty() {
    	return size == 0;
    }
    
    public void clear() {
    	size = 0;
    }

    @SuppressWarnings("unchecked")
    public Stack<T> clone() {
    	Stack<T> ret = null;
		try {
			ret = (Stack<T>)super.clone();
			ret.elems = (T[])new Object[elems.length];
			System.arraycopy(elems, 0, ret.elems, 0, size);
			return ret;
		} catch (CloneNotSupportedException e) {
			// should not happen
			throw new InternalError();
		}
    }
    
    public Object get(int i) {
    	return elems[i];
    }
    
    public boolean contains(Object o) {
        return Util.arrayContains(elems, o, size);
    }
    
    /**
     * returns first index
     * @param o
     * @return
     */
    public int indexOf(T o) {
        for (int i = 0; i < size && elems[i] != null; i++) {
            if (elems[i].equals(o)) return i;
        }
        return -1;
    }
    public String toString() {
    	StringBuffer s = new StringBuffer();
		s.append("[");
		for (int i=0; i<size && elems[i] != null; i++) {
		    if (i>0) s.append(", ");
		    s.append(elems[i].toString());
		}
		s.append("]");
		return s.toString();
    }
}
