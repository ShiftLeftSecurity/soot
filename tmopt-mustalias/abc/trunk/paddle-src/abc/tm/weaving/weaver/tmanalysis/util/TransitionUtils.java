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
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.stages.CallGraphAbstraction;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowMatchTag;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolFinder.SymbolShadowMatch;
import abc.tm.weaving.weaver.tmanalysis.mustalias.LocalMustAliasAnalysis;

public class TransitionUtils {
	
	/**
	 * For any state of a {@link TMStateMachine} of {@link TraceMatch}
	 * tm, returns the successor states under the statement stmt
	 * (itself; this method does not handle any callees of stmt). Note
	 * that <i>skip</i>-loops are treated the following way: If we are
	 * in state <i>s</i> , read symbol <i>a</i> and <i>s</i> has a
	 * <i>skip</i>-loop labeled <i>skip<a></i> then we return the set
	 * of all initial states ant <b>not</b> <i>s</i>.
	 * @param currentStates possible current states of the tracematch automaton; empty set indicates all states possible
	 * @param tm the tracematch owning that state
	 * @param stmt any statement; tagged with a tracematch shadow
	 * @param adviceActualToTmVar 
	 * @param tmFormalToTmVar 
	 * @param initial states of <code>tm</code>
	 * @return a collection of possible successor states given known set of current states
	 */
	public static Collection<SMNode> getSuccessorStatesFor(Collection<SMNode> currentStates, TraceMatch tm, Shadow ps, Stmt stmt, LocalMustAliasAnalysis lma) {
		//if the current statement is not tagged, we don't switch states
		if(!stmt.hasTag(SymbolShadowMatchTag.NAME)) {
			return currentStates;
		}

		Set<SMNode> res = new HashSet<SMNode>();		
		SymbolShadowMatchTag tag = (SymbolShadowMatchTag) stmt.getTag(SymbolShadowMatchTag.NAME);
		
		boolean atLeastOneShadowActive = false;
		for (SymbolShadowMatch match : tag.getMatchesForTracematch(tm)) {
			if(match.isEnabled()) {
				//check composition of tmFormalToAdviceLocal and adviceActualToTmVar
				//against tmFormalToTmVar
				boolean sameVariableMapping = true;
                Map<String, Local> m = match.getTmFormalToAdviceLocal();
                for (Entry<String,Local> tmFormalAndAdviceActual : m.entrySet()) {
					String tmFormal = tmFormalAndAdviceActual.getKey();
					Local adviceActual = tmFormalAndAdviceActual.getValue();
                    Local supposedAdviceActual = ps.getLocalForVarName(tmFormal);
					if(!lma.mustAlias(adviceActual, supposedAdviceActual, stmt)) {
						sameVariableMapping = false;
						break;
					}
				}
				
				//add all states which we can reach directly via this symbol
				String symbolName = match.getSymbolName();
                System.out.println("symbol name "+symbolName);

				Iterator<SMNode> it = currentStates.iterator();
				if (currentStates.isEmpty())
					it = tm.getStateMachine().getStateIterator();

				for (; it.hasNext(); ) {
					SMNode cs = it.next();
					for (Iterator edgeIter = cs.getOutEdgeIterator(); edgeIter.hasNext();) {
						SMEdge edge = (SMEdge) edgeIter.next();
						//if we have a skip edge, we get back to the initial configuration
						if(edge.getLabel().equals(symbolName)) {
                            System.out.println("found edge; skip "+edge.isSkipEdge()+" target "+edge.getLabel()+" with sameVariableMapping "+sameVariableMapping);
							if(edge.isSkipEdge() && sameVariableMapping) {
                                System.out.println("initial states: "+tm.getStateMachine().getInitialStates());
								//TODO if sameVariableMapping is false, we could add edge only if it gets us closer to the final state
								res.addAll(tm.getStateMachine().getInitialStates());
							} else {
                                System.out.println("target: "+edge.getTarget());
								res.add(edge.getTarget());
							}
						}
					}
				}
				//weak update; also remain in the current state
				if(!sameVariableMapping) {
					res.addAll(currentStates);
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
