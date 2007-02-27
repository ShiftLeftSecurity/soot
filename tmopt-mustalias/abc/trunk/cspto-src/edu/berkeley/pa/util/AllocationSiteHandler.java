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

import java.util.HashSet;
import java.util.Set;

import manu.util.ImmutableStack;

import soot.AnySubType;
import soot.ArrayType;
import soot.RefType;
import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.jimple.spark.internal.TypeManager;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.VarNode;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.jimple.toolkits.callgraph.VirtualCalls;
import soot.util.NumberedString;

/**
 * Interface for handler for when an allocation site is encountered in a pointer
 * analysis query.
 * 
 * @author manu
 * 
 * 
 */
public interface AllocationSiteHandler {

  /**
   * handle a particular allocation site
   * 
   * @param allocNode
   *          the abstract location node
   * @param callStack
   *          for context-sensitive analysis, the call site; might be null
   * @return true if analysis should be terminated; false otherwise
   */
  public boolean handleAllocationSite(AllocNode allocNode, ImmutableStack<Integer> callStack);

  public void resetState();

  public static class PointsToSetHandler implements AllocationSiteHandler {

    private PointsToSetInternal p2set;

    /*
     * (non-Javadoc)
     * 
     * @see AAA.algs.AllocationSiteHandler#handleAllocationSite(soot.jimple.spark.pag.AllocNode,
     *      java.lang.Integer)
     */
    public boolean handleAllocationSite(AllocNode allocNode, ImmutableStack<Integer> callStack) {
      p2set.add(allocNode);
      return false;
    }

    public PointsToSetInternal getP2set() {
      return p2set;
    }

    public void setP2set(PointsToSetInternal p2set) {
      this.p2set = p2set;
    }

    public void resetState() {
      // TODO support this
      throw new RuntimeException();
    }

    public boolean shouldHandle(VarNode dst) {
      // TODO Auto-generated method stub
      return false;
    }
  }

  public static class CastCheckHandler implements AllocationSiteHandler {

    private Type type;

    private TypeManager manager;

    private boolean castFailed = false;

    /*
     * (non-Javadoc)
     * 
     * @see AAA.algs.AllocationSiteHandler#handleAllocationSite(soot.jimple.spark.pag.AllocNode,
     *      java.lang.Integer)
     */
    public boolean handleAllocationSite(AllocNode allocNode, ImmutableStack<Integer> callStack) {
      castFailed = !manager.castNeverFails(allocNode.getType(), type);
      return castFailed;
    }

    public void setManager(TypeManager manager) {
      this.manager = manager;
    }

    public void setType(Type type) {
      this.type = type;
    }

    public void resetState() {
      throw new RuntimeException();
    }

    public boolean shouldHandle(VarNode dst) {
      // TODO Auto-generated method stub
      P2SetVisitor v = new P2SetVisitor() {

        @Override
        public void visit(Node n) {
          if (!returnValue) {
            returnValue = !manager.castNeverFails(n.getType(), type);
          }
        }

      };
      dst.getP2Set().forall(v);
      return v.getReturnValue();
    }
  }

  public static class VirtualCallHandler implements AllocationSiteHandler {

    public PAG pag;

    public Type receiverType;

    public NumberedString methodStr;

    public Set<SootMethod> possibleMethods = new HashSet<SootMethod>();

    /**
     * @param pag
     * @param receiverType
     * @param methodName
     * @param parameterTypes
     * @param returnType
     */
    public VirtualCallHandler(PAG pag, Type receiverType, NumberedString methodStr) {
      super();
      this.pag = pag;
      this.receiverType = receiverType;
      this.methodStr = methodStr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see AAA.algs.AllocationSiteHandler#handleAllocationSite(soot.jimple.spark.pag.AllocNode,
     *      AAA.algs.MethodContext)
     */
    public boolean handleAllocationSite(AllocNode allocNode, ImmutableStack<Integer> callStack) {
      Type type = allocNode.getType();
      if (!pag.getTypeManager().castNeverFails(type, receiverType))
        return false;
      if (type instanceof AnySubType) {
        AnySubType any = (AnySubType) type;
        RefType refType = any.getBase();
        if (pag.getTypeManager().getFastHierarchy().canStoreType(receiverType, refType)
            || pag.getTypeManager().getFastHierarchy().canStoreType(refType, receiverType)) {
          return true;
        }
        return false;
      }
      if (type instanceof ArrayType) {
        // we'll invoke the java.lang.Object method in this
        // case
        // Assert.chk(varNodeType.toString().equals("java.lang.Object"));
        type = Scene.v().getSootClass("java.lang.Object").getType();
      }
      RefType refType = (RefType) type;
      SootMethod targetMethod = null;
      targetMethod = VirtualCalls.v().resolveNonSpecial(refType, methodStr);
      if (!possibleMethods.contains(targetMethod)) {
        possibleMethods.add(targetMethod);
        if (possibleMethods.size() > 1)
          return true;
      }
      return false;
    }

    public void resetState() {
      possibleMethods.clear();
    }

    public boolean shouldHandle(VarNode dst) {
      // TODO Auto-generated method stub
      return false;
    }
  }

  public boolean shouldHandle(VarNode dst);
}
