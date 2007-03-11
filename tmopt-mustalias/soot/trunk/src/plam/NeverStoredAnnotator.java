package plam;
/* Soot - a J*va Optimization Framework
 * Copyright (C) 2007 Patrick Lam
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import soot.Body;
import soot.G;
import soot.Local;
import soot.Scene;
import soot.SceneTransformer;
import soot.Singletons;
import soot.SootClass;
import soot.SootMethod;
import soot.VoidType;
import soot.jimple.Stmt;
import soot.tagkit.StringTag;
import soot.toolkits.graph.BriefUnitGraph;

public class NeverStoredAnnotator extends SceneTransformer {
    public NeverStoredAnnotator (Singletons.Global g) {}
    public static NeverStoredAnnotator v() { return G.v().plam_NeverStoredAnnotator(); }

    protected void internalTransform(String phaseName, Map options) {
        HashMap nsaResults = new HashMap();

        for(Iterator clIt = Scene.v().getApplicationClasses().iterator(); clIt.hasNext(); ) 
        {
            final SootClass cl = (SootClass) clIt.next();
            for(Iterator mIt = cl.methodIterator(); mIt.hasNext(); ) 
            {
                SootMethod m = (SootMethod)mIt.next();

                // Abstract methods are harmless.  Natives aren't, but they're too hard; let's assume they're harmless.
                if (m.isAbstract() || m.isNative())
                    continue;

                Body b = m.retrieveActiveBody();

                NeverStoredAnalysis a = (NeverStoredAnalysis)nsaResults.get(m);
                if (a == null) {
                    a = new NeverStoredAnalysis(m, 
                                                new BriefUnitGraph(b), 
                                                nsaResults);
                    nsaResults.put(m, a);
                }

                StringBuffer summary = new StringBuffer();
                if (m.getReturnType().equals(VoidType.v()))
                    summary.append("(void) ");
                else
                    summary.append("("+a.queryRetVal() + ") ");

                if (!m.isStatic()){
                    summary.append("[this:"+a.queryThisParam().toString()+"]");
                }
                summary.append("(");
                for (int i = 0; i < m.getParameterTypes().size(); i++) {
                    if (i > 0) summary.append(", ");
                    summary.append(a.queryParameter(i));
                }
                summary.append(")");

                m.addTag(new StringTag(summary.toString(), "NeverStored Summary"));

                List locals = a.getRefLocals();

                Iterator it = b.getUnits().iterator();
                while (it.hasNext())
                {
                    Stmt s = (Stmt)it.next();
                    StringBuffer bb = new StringBuffer("[");
                    Iterator localsIt = locals.iterator();
                    while (localsIt.hasNext()) 
                    {
                        Local l = (Local) localsIt.next();
                        Object q = a.queryLocal(s, l);
                        if (q == NeverStoredAnalysis.NEVER_STORED)
                            bb.append(l.toString() + ":NS ");
                        if (q == NeverStoredAnalysis.POSSIBLY_STORED)
                            bb.append(l.toString() + ":PS ");
                        if (q == NeverStoredAnalysis.PARAMETER)
                            bb.append(l.toString() + ":PM ");
                    }
                            
                    s.addTag(new StringTag(bb.toString()+"]", "Never Stored"));
                }
            }
        }
    }
}
