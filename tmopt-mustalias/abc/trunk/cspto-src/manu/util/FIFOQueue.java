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

/**
 * A FIFO queue of objects, implemented as a
 * circular buffer.  NOTE: elements stored in the
 * buffer should be non-null; this is not checked
 * for performance reasons.
 */
public final class FIFOQueue {

    /**
     * the buffer.
     */
    private Object[] _buf;

    /**
     * pointer to current top of buffer
     */
    private int _top;

    /**
     * point to current bottom of buffer, where
     * things will be added
     * invariant: after call to add / remove,
     * should always point to an empty slot in
     * the buffer
     */
    private int _bottom;

    /**
     * @param initialSize_ the initial size of the queue
     */
    public FIFOQueue(int initialSize_) {
        _buf = new Object[initialSize_];
    }

    public FIFOQueue() {
        this(10);
    }

    public boolean push(Object obj_) {
        return add(obj_);
    }
    /**
     * add an element to the bottom of the queue
     */
    public boolean add(Object obj_) {
        //        Assert.chk(obj_ != null);
        // add the element
        _buf[_bottom] = obj_;
        // increment bottom, wrapping around if necessary
        _bottom = (_bottom == _buf.length - 1) ? 0 : _bottom + 1;
        // see if we need to increase the queue size
        if (_bottom == _top) {
            // allocate a new array and copy
            int oldLen = _buf.length;
            int newLen = oldLen * 2;
            //            System.out.println("growing buffer to size " + newLen);
            Object[] newBuf = new Object[newLen];
            int topToEnd = oldLen - _top;
            int newTop = newLen - topToEnd;
            // copy from 0 to _top to beginning of new buffer,
            // _top to _buf.length to the end of the new buffer
            System.arraycopy(_buf, 0, newBuf, 0, _top);
            System.arraycopy(_buf, _top, newBuf, newTop, topToEnd);
            _buf = newBuf;
            _top = newTop;
            return true;
        }
        return false;
    }

    public Object pop() {
        return remove();
    }
    
    /**
     * remove the top element from the buffer
     */
    public Object remove() {
        // check if buffer is empty
        if (_bottom == _top) return null;
        Object ret = _buf[_top];
        // increment top, wrapping if necessary
        _top = (_top == _buf.length - 1) ? 0 : _top + 1;
        return ret;
    }

    public boolean isEmpty() {
        return _bottom == _top;
    }

    public String toString() {
        return _bottom + " " + _top;
    }
    
    public void clear() {
    	_bottom = 0;
    	_top = 0;
    }
}
   
