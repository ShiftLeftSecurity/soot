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

import soot.jimple.spark.pag.VarNode;

/**
 * @author manu
 */
public final class AssignEdge {

	private static final int PARAM_MASK = 0x00000001;
	
	private static final int RETURN_MASK = 0x00000002;

	private static final int CALL_MASK = PARAM_MASK | RETURN_MASK;
    
    private Integer callSite = null;
	
    private final VarNode src;

    private int scratch;
    
    private final VarNode dst;

    /**
     * @param from
     * @param to
     */
    public AssignEdge(final VarNode from, final VarNode to) {
        this.src = from;
        this.dst = to;
    }
    
	public boolean isParamEdge() {
		return (scratch & PARAM_MASK) != 0;
	}
	
	public void setParamEdge() {
		scratch |= PARAM_MASK;
	}
	
	public boolean isReturnEdge() {
		return (scratch & RETURN_MASK) != 0;
	}
	
	public void setReturnEdge() {
		scratch |= RETURN_MASK;
	}
	
	public boolean isCallEdge() {
		return (scratch & CALL_MASK) != 0;
	}
	
	public void clearCallEdge() {
		scratch = 0;
	}
    
    /**
     * @return
     */
    public Integer getCallSite() {
        assert callSite != null : this + " is not a call edge!";
        return callSite;
    }

    /**
     * @param i
     */
    public void setCallSite(Integer i) {
        callSite = i;
    }

    public String toString() {
    	String ret = src + " -> " + dst;
    	if (isReturnEdge()) {
    		ret += "(* return" + callSite + " *)";
    	} else if (isParamEdge()) {
			ret += "(* param" + callSite + " *)";
    		
    	}
    	return ret;
    }
    
    public VarNode getSrc() {
        return src;
    }
    public VarNode getDst() {
        return dst;
    }
}
