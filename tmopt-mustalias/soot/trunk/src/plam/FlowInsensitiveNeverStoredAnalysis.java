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

import soot.*;
import soot.util.*;
import java.util.*;
import soot.jimple.*;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.jimple.toolkits.callgraph.TransitiveTargets;

/** Given a SootClass c and a Local l, determines whether or not l is
 * ever stored in c such that l is accessible from the outside.
 *
 * Typical use case: inner classes, e.g. LinkedList's iterator.
 *
 * class java.util.LinkedList$2 {
 *  private LinkedList this$0;
 *  <init> (LinkedList outerThis) { this.this$0 = outerThis; }
 *  // no other stores or returns of this$0
 * }
 *
 * Check: 1) no returns of objects aliased to outerThis;
 *        2) no transitive stores of aliases of outerThis to non-private
 *              fields.
 *
 * Threads should be okay, because they're in separate classes and
 * hence cannot read the private field.
 */
 
public class FlowInsensitiveNeverStoredAnalysis {
    /** Returns true if <code>c</code> potentially exposes <code>l</code>.
     * Note that <code>l</code> would usually be a parameter to <code>c</code>'s
     * constructor, and is typically not in scope in <code>c</code> (but its
     * aliases are!). */
    private static boolean classPossiblyExposes(SootClass c, Local l, 
                                           LinkedList visited, HashMap cachedResults) {
        Iterator it = c.getMethods().iterator();
        while (it.hasNext()) {
            SootMethod m = (SootMethod) it.next();

            if (!m.isConcrete())
                return true;
            if (!visited.contains(m) && possiblyExposes(m, l, true, 
                                                        visited, cachedResults))
                return true;
        }
        return false;
    }

    static boolean possiblyExposes(SootMethod m, Local l) {
        return possiblyExposes(m, l, false, new LinkedList(), new HashMap());
    }

    static private boolean bail(SootMethod m, LinkedList visited, String reason, HashMap cachedResults) {
        System.out.println("bailing at "+m+" due to "+reason);
        visited.remove(m);
        cachedResults.put(m, new Boolean(true));
        return true;
    }

    /* analyzingClass: if true, conservatively bail on calls to non-concrete methods. */
    static private boolean possiblyExposes(SootMethod m, Local l, boolean analyzingClass, 
                                           LinkedList visited, HashMap cachedResults) {
        System.out.println("query on "+m);

        visited.add(m);
        PointsToAnalysis pa = Scene.v().getPointsToAnalysis();
        CallGraph cg = Scene.v().getCallGraph();
        PointsToSet lp = pa.reachingObjects(l);

        Iterator unitsIt = m.retrieveActiveBody().getUnits().iterator();
        while (unitsIt.hasNext()) {
            Stmt u = (Stmt) unitsIt.next();
            if (u instanceof ReturnStmt) {
                // Check for returns
                // In principle, returns of l from sub-calls should be okay.
                Value rv = ((ReturnStmt)u).getOp();
                if (rv instanceof Local) {
                    PointsToSet rvp = pa.reachingObjects((Local)rv);
                    if (rvp.hasNonEmptyIntersection(lp))
                        return bail(m, visited, "return", cachedResults);
                }
            }
            if (u instanceof AssignStmt) {
                // Check for stores (condition 2)
                Value lhs = ((AssignStmt)u).getLeftOp(),
                    rhs = ((AssignStmt)u).getRightOp();

                if (lhs instanceof FieldRef && rhs instanceof Local) {
                    // (rhs could also be a NullConstant, which is safe)
                    // check to see if we're writing object of interest.
                    if (pa.reachingObjects
                        ((Local)rhs).hasNonEmptyIntersection(lp)) {
                        SootField f = ((FieldRef)lhs).getField();
                        if (((SootField)f).isPrivate()) {
                            // We've got a store of l to a private field.
                            // Check the whole class for possibly-exposedness.
                            if (classPossiblyExposes(m.getDeclaringClass(), l, visited,
                                                cachedResults))
                                return bail(m, visited, "field write to private (and class exposes)", cachedResults);
                        } 
                        else if (!analyzingClass)
                            return bail(m, visited, "field write of "+rhs+" to field "+lhs+" and analyzingClass off", cachedResults);
                    }
                }
            }
            if (u.containsInvokeExpr()) {
                // Transitively check on callees, don't recurse.
                SootMethod initialMethod = (SootMethod)visited.getFirst();
                Iterator targIt = new TransitiveTargets(cg).iterator(u);
                while (targIt.hasNext()) {
                    SootMethod targ = (SootMethod)targIt.next();
                    boolean leavingInitialClass = targ.getDeclaringClass()
                        .equals(initialMethod.getDeclaringClass());
                    boolean leavingCurrentClass = targ.getDeclaringClass()
                        .equals(m.getDeclaringClass());

                    // It's okay to return 'false' for recursive calls.
                    if (targ.equals(m))
                        continue;
                    if (cachedResults.containsKey(targ)) {
                        boolean b = ((Boolean)cachedResults.get(targ)).booleanValue();
                        if (b)
                            continue;
                        else
                            return false;
                    }

                    if (targ.isConcrete()) {
                        // If we're leaving current class, then switch analyzingClass off.
                        boolean newAnalyzingClass = analyzingClass;
                        if (leavingCurrentClass) newAnalyzingClass = false;

                        if (!visited.contains(targ) &&
                            possiblyExposes
                            (targ, l, newAnalyzingClass, visited,
                             cachedResults))
                            return bail(m, visited, "subcall", cachedResults);
                    } else {
                        // Let's see if non-concrete callee can expose
                        // the private field somehow.  Say if 1) callee
                        // belongs to same class (can then expose privates), or...
                        if (leavingInitialClass)
                            return bail(m, visited, "non-concrete, intra, from "+m+" to "+targ, cachedResults);
                        // of course, case 1) takes care of 'this' as well.
                        // 2) if any of its parameters can intersect l.
                        InvokeExpr ie = (InvokeExpr)u.getInvokeExpr();
                        for (int i = 0; i < ie.getArgCount(); i++) {
                            if (ie.getArg(i) instanceof Local) {
                                PointsToSet p = pa.reachingObjects((Local)ie.getArg(i));
                                if (p.hasNonEmptyIntersection(lp))
                                    return bail(m, visited, "non-concrete call to "+targ+", param" +i, cachedResults);
                            }
                            // else arg was a constant.
                        }
                    }
                }
            }
        }
        visited.remove(m);
        cachedResults.put(m, new Boolean(false));
        return false;
    }
}

