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

package edu.berkeley.pa.csdemand;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import manu.util.Averager;
import manu.util.HashSetMultiMap;
import manu.util.Util;
import soot.AnySubType;
import soot.FastHierarchy;
import soot.Local;
import soot.PointsToSet;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.Stmt;
import soot.jimple.spark.pag.LocalVarNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.VarNode;
import soot.jimple.spark.sets.HybridPointsToSet;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;
import edu.berkeley.pa.util.ContextSensitiveInfo;
import edu.berkeley.pa.util.SootUtil;

/**
 * @author manu
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class TestCastsPAG {

    private static final int DEBUG_NUM = -1; // 28640; //29126;

    private static final boolean PRINT_EXTRA = false;

    private static final boolean PERFORMANCE = true;

    private static final boolean RUN_DEMAND = true;

    private static HashSetMultiMap<SootMethod, String> methodToFileCastedVars = null;

    public static void testAllCasts(final PAG pag) {
        int totalCasts = 0;
        int totalPointerCasts = 0;
//        Set<VarNode> hasInstanceOf = new HashSet<VarNode>();
//        int numInstanceOf = 0;
        int fsSuccess = 0;
        readCastFile();
        final FastHierarchy fh = Scene.v().getOrMakeFastHierarchy();
        EnumMap<HeuristicType, Integer> numRefined = new EnumMap<HeuristicType, Integer>(
                HeuristicType.class);
        ContextSensitiveInfo csInfo = new ContextSensitiveInfo(pag);
        final DemandCSPointsTo refiner = new DemandCSPointsTo(csInfo, pag);
        // final DemandPAG alg = new DemandCSPathEdge(pag, csInfo);
        Map<VarNode, PointsToSetInternal> toCheck = new HashMap<VarNode, PointsToSetInternal>();
        Map<VarNode, Integer> varToNumCasts = new HashMap<VarNode, Integer>();
        RefType throwableType = Scene.v().getRefType("java.lang.Throwable");
        for (Iterator mIt = Scene.v().getReachableMethods().listener(); mIt
                .hasNext();) {
            final SootMethod m = (SootMethod) mIt.next();
            if (SootUtil.inLibrary(m.getDeclaringClass().getName()))
                continue;
            for (Iterator sIt = m.getActiveBody().getUnits().iterator(); sIt
                    .hasNext();) {
                final Stmt s = (Stmt) sIt.next();
                if (!(s instanceof AssignStmt))
                    continue;
                AssignStmt as = (AssignStmt) s;
                Value rhs = as.getRightOp();
                if (!(rhs instanceof CastExpr))
                    continue;
                final CastExpr ce = (CastExpr) rhs;
                final Value opv = ce.getOp();
                if (!(opv instanceof Local))
                    continue;
                final Local op = (Local) opv;
                if (methodToFileCastedVars != null) {
                    Set<String> castedVars = methodToFileCastedVars.get(m);
                    if (!castedVars.contains(op.toString())) {
                        System.out.println("cast not in file: method " + m
                                + " local " + op);
                        // System.out.println(castedVars);
                        continue;
                    }
                }
                totalCasts++;
                if (!(op.getType() instanceof RefLikeType)) {
                    continue;
                }
                totalPointerCasts++;
                final Type castType = ce.getCastType();
                if (fh.canStoreType(castType, throwableType)) {
                    continue;
                }
                PointsToSet pt = pag.reachingObjects(op);
                boolean canFail = false;
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
                        canFail = true;
                        break;
                    }
                }

                LocalVarNode varNode = pag
                        .makeLocalVarNode(op, op.getType(), m);
                varToNumCasts.put(varNode, Util.getInt(varToNumCasts
                        .get(varNode)) + 1);
                if (!canFail) {
                    // System.out.println("cast is " + ce + " in method " + m);
                    // System.out.println(varNode.getP2Set());
                    fsSuccess++;
                    for (HeuristicType type : EnumSet
                            .allOf(HeuristicType.class)) {
                        numRefined.put(type,
                                Util.getInt(numRefined.get(type)) + 1);
                    }
                    continue;
                } else {
                }
                if (toCheck.containsKey(varNode))
                    continue;
                PointsToSetInternal p2set = varNode.getP2Set();
                final HybridPointsToSet badLocs = new HybridPointsToSet(varNode
                        .getType(), pag);
                P2SetVisitor setVisitor = new P2SetVisitor() {
                    @Override
                    public void visit(Node n) {
                        Type type = n.getType();
                        if (!fh.canStoreType(type, ce.getCastType())) {
                            // inspector.dumpPathForBadLoc(node, (AllocNode) n);
                            // returnValue = true;
                            badLocs.add(n);
                        } else if (!returnValue) {
                            returnValue = true;
                        }
                    }
                };
                p2set.forall(setVisitor);
                // assert badLocs.size() > 0;
                assert !toCheck.containsKey(varNode);
                toCheck.put(varNode, badLocs);
            }
        }
        Averager av = new Averager();
        long startTime = System.currentTimeMillis();
        if (RUN_DEMAND) {
            if (!PERFORMANCE) {
                System.out.println("STARTING CAST CHECKS");
            }
            for (VarNode node : toCheck.keySet()) {
                PointsToSetInternal badLocs = toCheck.get(node);
                if (!PERFORMANCE) {
                    if (DEBUG_NUM != -1 && node.getNumber() != DEBUG_NUM) {
                        continue;
                    }
                    System.err.println(node + " failed");
                }
                EnumSet<HeuristicType> heuristics;
                if (PERFORMANCE) {
                    heuristics = EnumSet.range(HeuristicType.INCR,
                            HeuristicType.INCR);
                } else {
//                     heuristics = EnumSet.range(HeuristicType.EVERY,
//                     HeuristicType.EVERY);
                    heuristics = EnumSet.of(HeuristicType.INCR,
                            HeuristicType.EVERY);
                    // heuristics = EnumSet.allOf(HeuristicType.class);
                }
                for (HeuristicType type : heuristics) {
                    if (!PERFORMANCE) {
                        System.err.println("using " + type);
                    }
                    long queryStart = System.currentTimeMillis();
                    boolean refined = refiner.refineP2Set(node, badLocs, type);
                    long queryTime = System.currentTimeMillis() - queryStart;
                    av.addSample(queryTime);
                    if (refined) {
                        if (!PERFORMANCE) {
                            System.err.println("refined!");
                        }
                        if (PRINT_EXTRA) {
                            System.err.println(refiner.numPasses + " passes");
                            System.err.println(refiner.numNodesTraversed
                                    + " nodes traversed");
                        }
                        numRefined.put(type, Util.getInt(numRefined.get(type))
                                + varToNumCasts.get(node));
                    } else {
                        if (!PERFORMANCE) {
                            System.err.println("couldn't refine");
                        }
                    }

                }
                if (!PERFORMANCE) {
                    System.out.println("+++++=====+++++");
                }

            }
        }
        long totalTime = System.currentTimeMillis() - startTime;
        if (!PERFORMANCE) {
            System.out.println("DONE CASTS");
            System.out.println(totalCasts + " total cast queries");
            System.out.println(totalPointerCasts + " pointer cast queries");
            System.out.println(totalCasts - fsSuccess
                    + " failed with field-sensitive");
            for (HeuristicType heuristic : EnumSet.allOf(HeuristicType.class)) {
                System.out.println((totalCasts - Util.getInt(numRefined
                        .get(heuristic)))
                        + " failed with " + heuristic);
            }
        } else {
            System.out.println(totalCasts + " total cast queries");
            System.out.println("AVERAGE QUERY TIME " + av.getCurAverage());
            System.out.println("TOTAL TIME " + totalTime);
        }
    }

    private static void readCastFile() {
        String fileName = Scene.v().getMainClass().getName() + ".casts.out";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            methodToFileCastedVars = new HashSetMultiMap<SootMethod, String>();
            String readLine = null;
            while ((readLine = reader.readLine()) != null) {
                Pattern p1 = Pattern.compile("(.*) ::::: method (.*)");
                Matcher m1 = p1.matcher(readLine);
                String methodName = null;
                String varStr = null;
                while (m1.find()) {
                    methodName = m1.group(2);
                    varStr = m1.group(1);
                }
                // System.out.println("method " + methodName + " var " +
                // varStr);
                SootMethod method = Scene.v().getMethod(methodName);
                methodToFileCastedVars.put(method, varStr);
            }

        } catch (FileNotFoundException e) {
            System.out.println("couldn't find casts file!!");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException();
        }

    }
}
