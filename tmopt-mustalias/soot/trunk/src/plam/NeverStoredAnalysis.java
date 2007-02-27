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

public class NeverStoredAnalysis extends ForwardFlowAnalysis {
    public final static Object NEVER_STORED = new Object() 
        { public String toString() { return "NEVER_STORED"; } };
    public final static Object POSSIBLY_STORED = new Object()
        { public String toString() { return "POSSIBLY_STORED"; } };
    public final static Object PARAMETER = new Object()
        { public String toString() { return "PARAMETER"; } };

    private SootMethod meth; private UnitGraph g;
    
    private List refLocals;
    private Map pointsToSetToLocals;
    private PointsToAnalysis pa;
    private CallGraph cg;

    private static Set methodsVisiting = 
        Collections.synchronizedSet(new HashSet());
    private HashMap previousNSAs;

    private HashMap localsUponExit;
    private Object thisResult; 
    private Object[] argsResults;
    private Object rvResult = NEVER_STORED;

    public NeverStoredAnalysis(SootMethod m, UnitGraph g, 
                               HashMap previousNSAs) {
        super(g);
        methodsVisiting.add(m);

        this.g = g; this.meth = m;
        this.previousNSAs = previousNSAs;
        this.pa = Scene.v().getPointsToAnalysis();
        if (pa instanceof soot.jimple.toolkits.pointer.DumbPointerAnalysis)
            throw new RuntimeException ("NeverStoredAnalysis requires SPARK: -p cg.spark on");
        this.cg = Scene.v().getCallGraph();
        
        this.refLocals = new LinkedList();
        this.pointsToSetToLocals = new HashMap();
        this.localsUponExit = new HashMap();

        Iterator it = g.getBody().getLocals().iterator();
        while (it.hasNext()) {
            Local l = (Local) it.next();
            if (l.getType() instanceof RefType) {
                refLocals.add(l);
                PointsToSet pt = pa.reachingObjects(l);
                List lp = (List)pointsToSetToLocals.get(pt);
                if (lp == null) {
                    lp = new LinkedList();
                    pointsToSetToLocals.put(pt, lp);
                }
                lp.add(l);
            }
        }
        
        doAnalysis();

        // Collect never-stored information for each local.
        Iterator tailsIt = g.getTails().iterator();
        localsUponExit = (HashMap)entryInitialFlow();

        while (tailsIt.hasNext()) {
            Unit t = (Unit)tailsIt.next();
            Map tm = (Map)(unitToAfterFlow.get(t));

            merge (localsUponExit, tm, localsUponExit);
            if (t instanceof ReturnStmt) {
                Value rv = ((ReturnStmt)t).getOp();
                assert (rv instanceof Local);
                rvResult = mergeOne(rvResult, tm.get(rv));
            }
        }

        // But that's still not what we want to know.
        // What we want to know is per-parameter.

        int paramCount = m.getParameterTypes().size();

        PointsToSet initialThis = null; 
        PointsToSet[] initialParams = new PointsToSet[paramCount];

        // Record initial assignments of parameters.
        Iterator unitsIt = g.getBody().getUnits().iterator();
        while (unitsIt.hasNext()) {
            Unit u = (Unit)unitsIt.next();
            if (u instanceof IdentityStmt) {
                IdentityStmt is = (IdentityStmt)u;
                if (is.getRightOp() instanceof ThisRef)
                    initialThis = pa.reachingObjects((Local)is.getLeftOp());
                else if (is.getRightOp() instanceof ParameterRef) {
                    ParameterRef pr = (ParameterRef) is.getRightOp();
                    initialParams[pr.getIndex()] = 
                        pa.reachingObjects((Local)is.getLeftOp());
                }
            }
        }

        this.thisResult = NEVER_STORED; 
        this.argsResults = new Object[paramCount];
        for (int i = 0; i < paramCount; i++) argsResults[i] = NEVER_STORED;

        // Okay, now figure out which locals are aliased to parameters.
        Iterator localsIt = refLocals.iterator();
        while (localsIt.hasNext()) {
            Local l = (Local) localsIt.next();
            PointsToSet lp = pa.reachingObjects(l);
            if (initialThis != null && lp.hasNonEmptyIntersection(initialThis))
                thisResult = mergeOne (thisResult, localsUponExit.get(l));

            for (int i = 0; i < paramCount; i++) {
                if (lp.hasNonEmptyIntersection(initialParams[i])) {
                    argsResults[i] = 
                        mergeOne (argsResults[i], localsUponExit.get(l));
                }
            }
        }

        methodsVisiting.remove(m);
    }

    public List getRefLocals() { return Collections.unmodifiableList(refLocals); }

    public Object queryLocal(Unit u, Local l) {
        return ((Map)(unitToAfterFlow.get(u))).get(l);
    }

    public Object queryThisParam() {
        return thisResult;
    }

    public Object queryParameter(int i) {
        return argsResults[i];
    }

    public Object queryRetVal() {
        return rvResult;
    }

    private Object mergeOne(Object o1, Object o2) {
        assert (o1 == POSSIBLY_STORED || o1 == NEVER_STORED || o1 == PARAMETER);
        if (o1 == o2) 
            return o1;
        else if (o1 == POSSIBLY_STORED || o2 == POSSIBLY_STORED)
            return POSSIBLY_STORED;
        else 
            return PARAMETER;
    }

    public void merge(Object i1, Object i2, Object o) {
        Map in1 = (Map) i1;
        Map in2 = (Map) i2;
        Map out = (Map) o;
        
        out.clear();
        Iterator it = refLocals.iterator();
        while (it.hasNext()) {
            Local l = (Local) it.next();
            Object o1 = in1.get(l), o2 = in2.get(l);
            out.put (l, mergeOne(o1, o2));
        }
    }
    
    public void copy(Object src, Object dest) {
        Map in = (Map) src;
        Map out = (Map) dest;
        
        out.clear();
        Iterator it = refLocals.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            out.put (o, in.get(o));
        }
    }

    private void weakUpdate(Map out, Local v, Object newVal) {
        // this should be implicit:
        // out.put (v, newVal);

        Iterator it = ((List)pointsToSetToLocals.get
                       (pa.reachingObjects(v))).iterator();
        while (it.hasNext()) {
            out.put (it.next(), newVal);
        }
    }

    private void handleParameter(Map out, Value v, Object newValue) {
        assert (newValue != NEVER_STORED);
        
        // If t == PARAMETER, don't change anything.
        if (v instanceof Local && newValue == POSSIBLY_STORED) 
            weakUpdate (out, (Local)v, POSSIBLY_STORED);
    }
    
    protected void flowThrough(Object inVal, Object stmt, Object outVal) {
        Map in = (Map) inVal;
        Map out = (Map) outVal;
        Stmt s = (Stmt) stmt;
        
        copy (in, out);
        if (s instanceof DefinitionStmt) {
            Value lhs = ((DefinitionStmt) s).getLeftOp();
            Value rhs = ((DefinitionStmt) s).getRightOp();
            
            if (rhs instanceof IdentityRef || rhs instanceof ThisRef)
            {
                // lhs should be a local; won't have any aliases.
                out.put (lhs, PARAMETER);
            }
            else if (rhs instanceof NewExpr)
            {
                // no aliases to contend with here.
                out.put (lhs, NEVER_STORED);
            }
            else if (lhs instanceof Local && rhs instanceof Local)
            {
                // strong update, copy the value over.
                out.put (lhs, in.get(rhs));
            }

            if (lhs instanceof FieldRef && rhs instanceof Local &&
                rhs.getType() instanceof RefType)
            {
                weakUpdate (out, (Local)rhs, POSSIBLY_STORED);
            }
            if (rhs instanceof FieldRef && lhs instanceof Local &&
                lhs.getType() instanceof RefType) {
                weakUpdate (out, (Local)lhs, POSSIBLY_STORED);
            }

            // TODO catch-all rule
        }
        
        if (s.containsInvokeExpr()) {
            // Let's not handle recursion yet.
            InvokeExpr e = s.getInvokeExpr();
            Iterator targIt = new Targets(cg.edgesOutOf(s));

            while (targIt.hasNext()) {
                SootMethod targ = (SootMethod)targIt.next();
                NeverStoredAnalysis tNSA;

                tNSA = (NeverStoredAnalysis)previousNSAs.get(targ);
                if (tNSA == null) {
                    // TODO:
                    // Actually, we don't have to be so terrible for non-concrete methods,
                    // because sometimes they're just interface methods.
                    // But let's see if this works for now.
                    if (methodsVisiting.contains(targ) || !targ.isConcrete()) {
                        // Recursion; make everything POSSIBLY_STORED...
                        Iterator it = s.getUseBoxes().iterator();
                        while (it.hasNext()) {
                            Value v = (Value)((ValueBox)(it.next())).getValue();
                            if (v instanceof Local && v.getType() instanceof RefType)
                                weakUpdate (out, (Local)v, POSSIBLY_STORED);
                        }
                        // ... and bail.
                        continue;
                    }
                    else {
                        tNSA = new NeverStoredAnalysis(targ, 
                                                       new BriefUnitGraph(targ.retrieveActiveBody()), 
                                                       previousNSAs);
                        previousNSAs.put(targ, tNSA);
                    }
                }

                // Now check 'this' and all of the parameters.
                if (e instanceof InstanceInvokeExpr) {
                    Value v = ((InstanceInvokeExpr)e).getBase();
                    if (v.getType() instanceof RefType) {
                        Object t = tNSA.queryThisParam();
                        if (t == POSSIBLY_STORED) {
                            // Second chance: do a flow-insensitive
                            // class-based never-stored analysis
                            System.out.println("second chance on "+targ+" with this="+v);
                            boolean b = FlowInsensitiveNeverStoredAnalysis.possiblyExposes(targ, (Local)v);
                            if (b)
                                handleParameter(out, v, t);
                            System.out.println("done: "+b);
                        }
                    }
                }
                for (int i = 0; i < targ.getParameterTypes().size(); i++) {
                    Value v = e.getArg(i);
                    if (v.getType() instanceof RefType) {
                        Object t = tNSA.queryParameter(i);
                        handleParameter(out, v, t);
                    }
                }
                
                // Take advantage of Jimple form here: only have x = o.foo()
                if (s instanceof DefinitionStmt) {
                    Value rv = ((DefinitionStmt)s).getLeftOp();
                    Object t = tNSA.queryRetVal();
                    
                    if (rv instanceof Local) {
                        if (t == NEVER_STORED)
                            out.put(rv, t);
                        else if (t == PARAMETER) {
                            // could do better if we knew which parameter 
                            // we were returning.  Then we could query
                            // that parameter's state before the call.
                            out.put(rv, POSSIBLY_STORED); 
                        }
                        else
                            out.put(rv, POSSIBLY_STORED);
                    }
                }
            }
        }
    }
    
    protected Object newInitialFlow() {
        Iterator it = refLocals.iterator();
        Map m = new HashMap();
        
        while (it.hasNext()) {
            Object o = it.next();
            m.put (o, NEVER_STORED);
        }
        
        return m;
    }
    
    protected Object entryInitialFlow() {
        Iterator it = refLocals.iterator();
        Map m = new HashMap();
        
        while (it.hasNext()) {
            Object o = it.next();
            m.put (o, NEVER_STORED);
        }
        
        return m;
    }
}
