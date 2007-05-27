/**
 * TransitionUtils
 *
 * @author Eric Bodden
 */
package abc.tm.weaving.weaver.tmanalysis.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.Unit;
import soot.Local;
import soot.Value;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;
import abc.tm.weaving.weaver.tmanalysis.stages.CallGraphAbstraction;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowMatchTag;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolFinder.SymbolShadowMatch;
import abc.tm.weaving.weaver.tmanalysis.mustalias.LocalMustAliasAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.LocalNotMayAliasAnalysis;

public class TransitionUtils {
	
	/**
	 * For any state of a {@link TMStateMachine} of {@link TraceMatch}
	 * tm, returns the successor states under the statement stmt
	 * (itself; this method does not handle any callees of stmt).
     *
     * The rules: 
     *
     *  1) The initial state is always a successor state. (due to
     *     suffix property);
     * 2) If we have a nonskip edge matching the given symbol, add its
     *     target as successor;
     * 3) If we have a skip edge matching the given symbol, remove the
     *     current state as successor.
     *
	 * @param currentStates possible current states of the tracematch automaton; empty set indicates all states possible
	 * @param tm the tracematch owning that state
	 * @param stmt any statement; tagged with a tracematch shadow
     * @param psm links shadow actuals to their defining stmts
	 * @param adviceActualToTmVar 
	 * @param tmFormalToTmVar 
	 * @param initial states of <code>tm</code>
	 * @return a collection of possible successor states given known set of current states
	 */
	public static Collection<SMNode> getSuccessorStatesFor(Collection<SMNode> currentStates, TraceMatch tm, Shadow ps, 
                                                           Map<Value,Stmt> psm, Stmt stmt, LocalMustAliasAnalysis lma,
                                                           LocalNotMayAliasAnalysis lnma) {

//         Set<Shadow> shadowsIntersectingPs = new HashSet();

//         Set allShadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
//         for (ShadowGroup ss : (Set<ShadowGroup>)allShadowGroups) {
//             if (ss.getAllShadows().contains(ps)) {
//                 shadowsIntersectingPs.addAll(ss.getAllShadows());
//             }
//         }

		//if the current statement is not tagged, we don't switch states
		if(!stmt.hasTag(SymbolShadowMatchTag.NAME)) {
			return currentStates;
		}

		Set<SMNode> res = new HashSet<SMNode>();		
		SymbolShadowMatchTag tag = (SymbolShadowMatchTag) stmt.getTag(SymbolShadowMatchTag.NAME);
		
		boolean atLeastOneShadowActive = false;
		for (SymbolShadowMatch match : tag.getMatchesForTracematch(tm)) {
			if(match.isEnabled()) {
                // Check to see if match belongs to the same shadow group (may-alias check)
//                 String shadowId = match.getUniqueShadowId();
//                 boolean found = false;
//                 for (Shadow s : shadowsIntersectingPs) {
//                     if (s.getUniqueShadowId().equals(shadowId))
//                         found = true;
//                 }
//                 if (!found)
//                     continue;

				boolean sameVariableMapping = true, notMaySameVariableMapping = false;

				String symbolName = match.getSymbolName();
                System.out.println("doing shadow ID "+match.getUniqueShadowId());

                /* Figure out if we have the exact same bindings as the
                 * tracematch we're tracking.
                 * If so, then we can carry out rule 3. */
                Map<String, Local> m = match.getTmFormalToAdviceLocal();
                for (Entry<String,Local> tmFormalAndAdviceActual : m.entrySet()) {
					String tmFormal = tmFormalAndAdviceActual.getKey();
					Local adviceActual = tmFormalAndAdviceActual.getValue();
                    Local supposedAdviceActual = ps.getLocalForVarName(tmFormal);
                    System.out.println("tmFormal "+tmFormal+" aa "+adviceActual+" supposedAdviceActual "+supposedAdviceActual+" psm "+psm);
                    System.out.println();
                    if (psm.get(supposedAdviceActual) != null) {
                        Stmt saaDef = psm.get(supposedAdviceActual);
                        if(!lma.mustAlias(adviceActual, stmt, supposedAdviceActual, saaDef))
                            sameVariableMapping = false;
                        if (lnma.notMayAlias(adviceActual, stmt, supposedAdviceActual, saaDef))
                            notMaySameVariableMapping = true;
                    }
				}
				
                res.addAll(tm.getStateMachine().getInitialStates());

				Iterator<SMNode> it = currentStates.iterator();
				if (currentStates.isEmpty())
					it = tm.getStateMachine().getStateIterator();

				for (; it.hasNext(); ) {
					SMNode cs = it.next();

                    // Final states are always sinks, don't treat them
                    // as sources here.
                    if (cs.isFinalNode())
                        continue;

                    System.out.println("from node "+cs);

                    // Rule 1: always add initial
                    boolean mustAddCurrent = true;

					for (Iterator edgeIter = cs.getOutEdgeIterator(); edgeIter.hasNext();) {
						SMEdge edge = (SMEdge) edgeIter.next();

						if(edge.getLabel().equals(symbolName)) {
                            System.out.println("found edge; skip "+edge.isSkipEdge()+" label "+edge.getLabel()+" with svm "+sameVariableMapping+" and msvm "+notMaySameVariableMapping);
                            // Rule 3: if skip & same vars, don't add current
							if(edge.isSkipEdge()) {
                                if (sameVariableMapping) 
                                    mustAddCurrent = false;
                            }
                            else {
                                // Rule 2: non-skip => add target
                                // (unless not-may)
                                if (!notMaySameVariableMapping) {
                                    System.out.println("Adding transition");
                                    res.add(edge.getTarget());
                                }
                            }
						}
					}
                    if (mustAddCurrent)
                        res.add(cs);
				}
				
				atLeastOneShadowActive = true;
			}
		}

		//if we actually made a transition, return the result, otherwise treat as a no-op
		if(atLeastOneShadowActive)
			return res;
		else
			return currentStates;
	}
	
	/**
	 * Returns <code>true</code> if the given unit might transitively call another shadow.
	 * This requires the {@link CallGraphAbstraction} to have run already.
	 * @param unit any unit
	 * @return <code>true</code> if there is an outgoing edge for this node in the abstracted call graph
	 * @see CallGraphAbstraction#apply()
	 */
	public static boolean mayTransitivelyCallOtherShadow(Unit unit) {
		CallGraph abstractedCallGraph = CallGraphAbstraction.v().abstractedCallGraph();
		return abstractedCallGraph.edgesOutOf(unit).hasNext();
	}
	
//	public static void testcase() {
//		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
//		
//		for (AbcClass abcClass : (Set<AbcClass>)gai.getWeavableClasses()) {
//			SootClass sootClass = abcClass.getSootClass();
//			for (final SootMethod method : (List<SootMethod>)sootClass.getMethods()) {
//				if(method.hasActiveBody()) {
//					new BodyTransformer() {
//
//						protected void internalTransform(Body b,
//								String phaseName, Map options) {
//							for (Stmt s : (Collection<Stmt>)b.getUnits()) {
//								System.out.println(mayTransitivelyCallOtherShadow(s));
//							}
//							
//						}
//						
//					}.transform(method.getActiveBody());
//				}				
//			}
//		}
//		
//	}
	
}
