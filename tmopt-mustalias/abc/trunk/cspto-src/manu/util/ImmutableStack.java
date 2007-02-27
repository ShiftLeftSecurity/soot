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

public class ImmutableStack<T> {

    private static final ImmutableStack<Object> EMPTY = new ImmutableStack<Object>(
            new Object[0]);

    private static final int MAX_SIZE = Integer.MAX_VALUE;

    public static int getMaxSize() {
        return MAX_SIZE;
    }
    @SuppressWarnings("unchecked")
    public static final <T> ImmutableStack<T> emptyStack() {
        return (ImmutableStack<T>) EMPTY;
    }

    final private T[] entries;

    private ImmutableStack(T[] entries) {
        this.entries = entries;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o != null && o instanceof ImmutableStack) {
            ImmutableStack other = (ImmutableStack) o;
            return Arrays.equals(entries, other.entries);
        }
        return false;
    }

    public int hashCode() {
        return Util.hashArray(this.entries);
    }

    @SuppressWarnings("unchecked")
    public ImmutableStack<T> push(T entry) {
        assert entry != null;
        if (MAX_SIZE == 0) {
            return emptyStack();
        }
        int size = entries.length + 1;
        T[] tmpEntries = null;
        if (size <= MAX_SIZE) {
            tmpEntries = (T[]) new Object[size];
            System.arraycopy(entries, 0, tmpEntries, 0, entries.length);
            tmpEntries[size - 1] = entry;
        } else {
            tmpEntries = (T[]) new Object[MAX_SIZE];
            System.arraycopy(entries, 1, tmpEntries, 0, entries.length - 1);
            tmpEntries[MAX_SIZE - 1] = entry;

        }
        return new ImmutableStack<T>(tmpEntries);
    }

    public T peek() {
        assert entries.length != 0;
        return entries[entries.length - 1];
    }

    @SuppressWarnings("unchecked")
    public ImmutableStack<T> pop() {
        assert entries.length != 0;
        int size = entries.length - 1;
        T[] tmpEntries = (T[]) new Object[size];
        System.arraycopy(entries, 0, tmpEntries, 0, size);
        return new ImmutableStack<T>(tmpEntries);
    }

    public boolean isEmpty() {
        return entries.length == 0;
    }

    public int size() {
        return entries.length;
    }

    public T get(int i) {
        return entries[i];
    }

    public String toString() {
        String objArrayToString = Util.objArrayToString(entries);
        assert entries.length <= MAX_SIZE : objArrayToString;
        return objArrayToString;
    }

    public boolean contains(T entry) {
        return Util.arrayContains(entries, entry, entries.length);
    }

    public boolean topMatches(ImmutableStack<T> other) {
        if (other.size() > size())
            return false;
        for (int i = other.size() - 1, j = this.size() - 1; i >= 0; i--, j--) {
            if (!other.get(i).equals(get(j)))
                return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public ImmutableStack<T> reverse() {
        T[] tmpEntries = (T[]) new Object[entries.length];
        for (int i = entries.length - 1, j = 0; i >= 0; i--, j++) {
            tmpEntries[j] = entries[i];
        }
        return new ImmutableStack<T>(tmpEntries);
    }

    @SuppressWarnings("unchecked")
    public ImmutableStack<T> popAll(ImmutableStack<T> other) {
        // TODO Auto-generated method stub
        assert topMatches(other);
        int size = entries.length - other.entries.length;
        T[] tmpEntries = (T[]) new Object[size];
        System.arraycopy(entries, 0, tmpEntries, 0, size);
        return new ImmutableStack<T>(tmpEntries);
    }

    @SuppressWarnings("unchecked")
    public ImmutableStack<T> pushAll(ImmutableStack<T> other) {
        // TODO Auto-generated method stub
        int size = entries.length + other.entries.length;
        T[] tmpEntries = null;
        if (size <= MAX_SIZE) {
            tmpEntries = (T[]) new Object[size];
            System.arraycopy(entries, 0, tmpEntries, 0, entries.length);
            System.arraycopy(other.entries, 0, tmpEntries, entries.length,
                    other.entries.length);
        } else {
            tmpEntries = (T[]) new Object[MAX_SIZE];
            // other has size at most MAX_SIZE
            // must keep all in other
            // top MAX_SIZE - other.size from this
            int numFromThis = MAX_SIZE - other.entries.length;
            System.arraycopy(entries, entries.length - numFromThis, tmpEntries, 0, numFromThis);
            System.arraycopy(other.entries, 0, tmpEntries, numFromThis, other.entries.length);            
        }
        return new ImmutableStack<T>(tmpEntries);
    }
}
