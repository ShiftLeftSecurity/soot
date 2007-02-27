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

package edu.berkeley.pa.driver;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.Local;
import soot.PackManager;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Printer;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import edu.berkeley.pa.csdemand.DemandCSPointsTo;
import edu.berkeley.pa.util.SootUtil;

public class DumpPToSets extends SceneTransformer {

  public static void main(String[] args) {
    PackManager.v().getPack("wjtp").add(new Transform("wjtp.dpto", new DumpPToSets()));

    soot.Main.main(args);

  }

  @Override
  protected void internalTransform(String phaseName, Map options) {
    System.out.println("dumping points-to sets");
    PointsToAnalysis myAnalysis = DemandCSPointsTo.makeDefault();
    for (Iterator mIt = Scene.v().getReachableMethods().listener(); mIt.hasNext();) {
      final SootMethod m = (SootMethod) mIt.next();
      if (!m.hasActiveBody()) continue;
      if (SootUtil.inLibrary(m.getDeclaringClass().getType())) continue;
      System.out.println("method " + m + ", body:");
      Body activeBody = m.getActiveBody();
      Printer.v().printTo(activeBody, new PrintWriter(System.out, true));
      for (Iterator iter = activeBody.getLocals().iterator(); iter.hasNext();) {
        Local l = (Local) iter.next();
        PointsToSet p2set = myAnalysis.reachingObjects(l);
        System.out.println("points-to set for " + l + ": " + p2set);
      }
    }
  }

}
