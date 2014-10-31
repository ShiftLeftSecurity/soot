/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003-2014 Eric Bodden and others
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package soot.jimple.toolkits.invoke;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Singletons.Global;
import soot.SootMethodRef;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.MethodHandle;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;

/**
 * This class replaces such {@link DynamicInvokeExpr} by ordinary {@link StaticInvokeExpr} where the invokedynamic calls
 * are simply the result of Java 8 lambda expressions. This is frequently the case when a special bootstrap method
 * within LambdaMetafactory is called. Replacing such calls benefits static call-graph resolution.
 * 
 * @author Eric Bodden
 */
public class InvokeDynamicForLambdaInliner extends BodyTransformer {

	private static final String LAMBDA_METAFACTORY_SIG = "<java.lang.invoke.LambdaMetafactory: java.lang.invoke.CallSite metafactory(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.invoke.MethodType,java.lang.invoke.MethodHandle,java.lang.invoke.MethodType)>";

	public InvokeDynamicForLambdaInliner(Global g) {
	}

	@Override
	protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
		for(Unit u: b.getUnits()) {
			if(u instanceof Stmt) {
				Stmt s = (Stmt) u;
				if(s.containsInvokeExpr()) {
					ValueBox ieBox = s.getInvokeExprBox();
					InvokeExpr ie = s.getInvokeExpr();
					if(ie instanceof DynamicInvokeExpr) {
						DynamicInvokeExpr indy = (DynamicInvokeExpr) ie;
						SootMethodRef bsmRef = indy.getBootstrapMethodRef();
						//TODO also handle altMetafactory
						if(bsmRef.getSignature().equals(LAMBDA_METAFACTORY_SIG)) {
							MethodHandle handle = (MethodHandle) indy.getBootstrapArg(1);
							if(handle.getMethodRef().isStatic()) {
								InvokeExpr newIe = Jimple.v().newStaticInvokeExpr(handle.getMethodRef(),indy.getArgs());
								ieBox.setValue(newIe);								
								//TODO do we need to fabricate a return value for void methods?
							} else {
								G.v().out.println("InvokeDynamicInliner: Do not know how to inline non-static closures...");
								G.v().out.println(indy);
							}
						}
					}
				}
			}
		}
	}

	public static InvokeDynamicForLambdaInliner v() {
		return G.v().soot_jimple_toolkits_invoke_InvokeDynamicInliner();
	}

}
