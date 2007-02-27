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

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import soot.AnySubType;
import soot.FastHierarchy;
import soot.Local;
import soot.PackManager;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.RefLikeType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import soot.Type;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.Stmt;

public class FailCast extends SceneTransformer {

    public static void main(String[] args) {
        PackManager.v().getPack("wjtp").add(
                new Transform("wjtp.fc", new FailCast()));

        soot.Main.main(args);

    }

    @Override
    protected void internalTransform(String phaseName, Map options) {
        System.out.println("running wjtp.failcast");
        FastHierarchy fh = Scene.v().getOrMakeFastHierarchy();
        PointsToAnalysis pa = Scene.v().getPointsToAnalysis();
        int totalCasts = 0;
        int safeCasts = 0;
        String outFilename = Scene.v().getMainClass().getName() + ".casts.out";
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outFilename)), true);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (Iterator mIt = Scene.v().getReachableMethods().listener(); mIt
                .hasNext();) {
            final SootMethod m = (SootMethod) mIt.next();
            if (SootUtil.inLibrary(m.getDeclaringClass().getType())) continue;
            stmt: for (Iterator sIt = m.getActiveBody().getUnits().iterator(); sIt
                    .hasNext();) {
                final Stmt s = (Stmt) sIt.next();
                if (!(s instanceof AssignStmt))
                    continue;
                AssignStmt as = (AssignStmt) s;
                Value rhs = as.getRightOp();
                if (!(rhs instanceof CastExpr))
                    continue;
                CastExpr ce = (CastExpr) rhs;
                totalCasts++;
                Value opv = ce.getOp();
                writer.println(opv + " ::::: method " + m);
                if (!(opv instanceof Local))
                    continue;
                Local op = (Local) opv;
                if (!(op.getType() instanceof RefLikeType)) {
                    continue;
                }
                PointsToSet pt = pa.reachingObjects(op);
                for (Iterator tIt = pt.possibleTypes().iterator(); tIt
                        .hasNext();) {
                    final Type t = (Type) tIt.next();
                    Type child;
                    if (t instanceof AnySubType) {
                        child = ((AnySubType) t).getBase();
                    } else {
                        child = t;
                    }
                    if (!fh.canStoreType(child, ce.getCastType())) {
                        // System.out.println( "added "+probeStmt(m,s)+"
                        // because ptset of "+op+" has possible type "+t);
//                        failingCasts++;
                        continue stmt;
                    }
                }
                System.out.println("SAFE " + ce + " in method " + m);
                safeCasts++;
            }
        }
        System.out.println("Total casts: " + totalCasts);
        System.out.println("Safe casts: " + safeCasts);
        writer.close();
    }
}
