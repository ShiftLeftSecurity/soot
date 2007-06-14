/** Propagates states and locals through a method. 
 * Assumes that everything involved is thread-local. */

package abc.tm.weaving.weaver.tmanalysis.mustalias;

import static abc.tm.weaving.weaver.tmanalysis.mustalias.StatePropagatorFlowAnalysis.AliasInfo.MAY;
import static abc.tm.weaving.weaver.tmanalysis.mustalias.StatePropagatorFlowAnalysis.AliasInfo.MAY_NOT;
import static abc.tm.weaving.weaver.tmanalysis.mustalias.StatePropagatorFlowAnalysis.AliasInfo.MUST;
import static abc.tm.weaving.weaver.tmanalysis.mustalias.StatePropagatorFlowAnalysis.AliasInfo.UNKNOWN;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.thread.IThreadLocalObjectsAnalysis;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.StateMachine;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.stages.CallGraphAbstraction;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowTag;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolShadow;

/** StatePropagatorFlowAnalysis: Propagates sets of SMNodes.
 * 
 * When a statement has multiple SMNodes associated with it, this abstractly
 * represents the fact that the automaton we're tracking may have multiple states
 * at that program point.
 *
 * FIXME This implementation is still not 100% sound, at least for the following reasons:
 * 
 *  2.) Thread safety: Right now, we do not take threads into account.
 *
 * @author Eric Bodden
 * @author Patrick Lam
 */
public class StatePropagatorFlowAnalysis extends ForwardFlowAnalysis {
	
	/**
	 * NotThreadLocalException
	 *
	 * @author Eric Bodden
	 */
	protected static class NotThreadLocalException extends Exception {
	
		private final Local l;
	
		public NotThreadLocalException(Local local) {
			this.l = local;
		}
	
		/**
		 * @return the adviceLocal
		 */
		public Local getVariableThatIsNotThreadLocal() {
			return l;
		}
	
	}

	/**
	 * Info telling whether something may, may not or must be aliased.
	 * @author Eric
	 */
	protected static enum AliasInfo {
		UNKNOWN {
			public boolean mayAlias() {
				throw new RuntimeException("unknown");
			}
			public boolean mustAlias() {
				throw new RuntimeException("unknown");
			}
			public String toString() {
				return "UNKNOWN";
			}
			public AliasInfo join(AliasInfo other) {
				return other;
			}
		},
		MAY {
			public boolean mayAlias() {
				return true;
			}
			public boolean mustAlias() {
				return false;
			}
			public String toString() {
				return "may alias (not must alias)";
			}
		},
		MUST {
			public boolean mayAlias() {
				return true;
			}
			public boolean mustAlias() {
				return true;
			}
			public String toString() {
				return "must alias";
			}
		},
		MAY_NOT {
			public boolean mayAlias() {
				return false;
			}
			public boolean mustAlias() {
				return false;
			}
			public String toString() {
				return "may not alias";
			}
		};
		public abstract boolean mayAlias();
		public abstract boolean mustAlias();
		public AliasInfo join(AliasInfo other) {
			if(other==this) {
				return this;
			} else {
				return MAY;
			}
		}
	}

	protected SootMethod meth;
	protected StateMachine sm;
	private TraceMatch traceMatch;
    /** True if we ever hit final state (or have to give up for some reason) */
	private boolean gaveUp;
	private final CallGraph abstractedCallGraph;
    private LocalMustAliasAnalysis lma;
    private LocalNotMayAliasAnalysis lnma;
    private PathsReachingFlowAnalysis prf;

    /* Pick out a shadow associated with the given tracematch and in the appropriate method.
     * Use it to determine the bindings that we're tracking. */
    /* In principle, we ought to try all possible principalShadows. */
    private Shadow principalShadow;
    private HashMap<Value, Stmt> principalShadowDefMap;
	private IThreadLocalObjectsAnalysis tloa;
	
	private Set<SymbolShadow> mustAliasedShadows;
	
	/**
	 * Computes possible states of <code>tm</code> in the given procedure, based on the bindings for shadow <code>s</code>.
	 */
	public StatePropagatorFlowAnalysis(TraceMatch tm, Shadow s, UnitGraph g, CallGraph abstractedCallGraph, IThreadLocalObjectsAnalysis tloa) {
		super(g);
		this.abstractedCallGraph = abstractedCallGraph;
		this.meth = g.getBody().getMethod();
		this.gaveUp = false; 

		this.traceMatch = tm;
		this.sm = tm.getStateMachine();
        this.principalShadow = s;
		this.lma = new LocalMustAliasAnalysis(g);
        this.lnma = new LocalNotMayAliasAnalysis(g);
        this.prf = new PathsReachingFlowAnalysis(g);
        this.tloa = tloa;
        this.mustAliasedShadows = new HashSet<SymbolShadow>();

        // dump the woven body...
//           java.io.PrintWriter pw = new java.io.PrintWriter(System.out);
//           soot.Printer.v().printTo(g.getBody(), pw);
//           pw.close();

			// for after-flow-ins analysis
        // 	Set<ShadowGroup> shadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
        // 	for (ShadowGroup group : shadowGroups) {
        //	Set<Shadow> allShadows = group.getAllShadows();


        /*
        int maxBindingCount = -1;
        for (Shadow ss : allShadows) {
            if (ss.getTraceMatch() != this.traceMatch || !ss.getContainer().equals(meth))
                continue;
            
            if (ss.getBoundLocals().size() > maxBindingCount) {
                principalShadow = ss;
                maxBindingCount = ss.getBoundLocals().size();
        */
        principalShadowDefMap = new java.util.HashMap();

        Collection seenBoundLocals = new java.util.LinkedList();
                
        for (Stmt u : (Collection<Stmt>)g.getBody().getUnits()) {
            for (soot.ValueBox vb : (Collection<soot.ValueBox>)u.getDefBoxes()) {
                soot.Value v = vb.getValue();
                if (principalShadow.getBoundLocals().contains(v)) {
                    if (prf.getVisitCount(u) == PathsReachingFlowAnalysis.MANY) {
                        System.out.println("gave up: many defs");
                        gaveUp = true;
                    }
                    if (seenBoundLocals.contains(v)) {
                        gaveUp = true;
                    }
                    seenBoundLocals.add(v);
                    //TODO what's this good for? why "0"?
                    principalShadowDefMap.put(v, (Stmt)(g.getSuccsOf(u).get(0)));
                }
            }
        }

        /*
            }
        }
        */

        // quick-state-prop
        // 		}

        System.out.println("principalShadow's bound variables: "+principalShadow.getBoundLocals());
		
		doAnalysis();
    }

	protected void flowThrough(Object inVal, Object stmt, Object outVal) {
		if(gaveUp) {
			return;
		}
		
		Collection<SMNode> in = (Collection<SMNode>) inVal, out = (Collection<SMNode>) outVal;
		Stmt s = (Stmt) stmt;

		out.clear();
		Collection<SMNode> successorStates;
		try {
			successorStates = getSuccessorStatesFor
				(in, traceMatch, principalShadow, principalShadowDefMap, s, lma, lnma, tloa);
		} catch (NotThreadLocalException e) {
			System.err.println("Giving up. "+e.getVariableThatIsNotThreadLocal()+" is not thread-local!");
			gaveUp = true;
			return;
		}
		
		for (SMNode succ : successorStates) {
			if(succ.isFinalNode()) {
                System.out.println("gave up: final");
				gaveUp = true;
				return;
			}
		}
		out.addAll(successorStates);
	}

	protected Object newInitialFlow() {
		return new HashSet<SMNode>();
	}

	protected Object entryInitialFlow() {
		HashSet<SMNode> initial = new HashSet<SMNode>();
		initial.addAll(sm.getInitialStates());
		return initial;
	}

	protected void copy(Object src, Object dest) {
		Collection s = (Collection) src, d = (Collection) dest;
		d.clear();
		d.addAll(s);
	}

	protected void merge(Object i1, Object i2, Object o) {
		Collection in1 = (Collection) i1, in2 = (Collection) i2;
		Collection out = (Collection) o;

		out.clear();
		out.addAll(in1);
		out.addAll(in2);

        if (!in1.equals(in2)) {
            // actually we should do something at flowThrough
            // when we have a set of output nodes or something.
            // Except when we have an initial flow. Then it's fine.
//             System.out.println("unequal merges");
            //            gaveUp = true;
        }
	}

	/**
	 * Returns <code>true</code> if <code>s</code> may have any sideeffects on a tracematch automaton, i.e.
	 * if the statement could transitively cause any shadow to be triggered.
	 * This information is computed using the abstractedCallGraph.
	 * @param s any statement
	 */
	private boolean mayHaveSideEffects(Stmt s) {
		return abstractedCallGraph.edgesOutOf(s).hasNext();
	}

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
	 * @param tloa 
	 * @param adviceActualToTmVar 
	 * @param tmFormalToTmVar 
	 * @param initial states of <code>tm</code>
	 * @return a collection of possible successor states given known set of current states
	 * @throws NotThreadLocalException thrown if a variable under investigation is not thread local
	 */
	protected Collection<SMNode> getSuccessorStatesFor(Collection<SMNode> currentStates, TraceMatch tm, Shadow ps, 
                                                           Map<Value,Stmt> psm, Stmt stmt, LocalMustAliasAnalysis lma,
                                                           LocalNotMayAliasAnalysis lnma, IThreadLocalObjectsAnalysis tloa) throws NotThreadLocalException {

//         Set<Shadow> shadowsIntersectingPs = new HashSet();

//         Set allShadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
//         for (ShadowGroup ss : (Set<ShadowGroup>)allShadowGroups) {
//             if (ss.getAllShadows().contains(ps)) {
//                 shadowsIntersectingPs.addAll(ss.getAllShadows());
//             }
//         }

		//if the current statement is not tagged, we don't switch states
		if(!stmt.hasTag(SymbolShadowTag.NAME)) {
			return currentStates;
		}

		Set<SMNode> res = new HashSet<SMNode>();		
		SymbolShadowTag tag = (SymbolShadowTag) stmt.getTag(SymbolShadowTag.NAME);
		
		boolean atLeastOneShadowActive = false;
		for (SymbolShadow shadow : tag.getMatchesForTracematch(tm)) {
			if(shadow.isEnabled()) {
				for (Local adviceLocal : shadow.getTmFormalToAdviceLocal().values()) {
					if(!tloa.isObjectThreadLocal(adviceLocal, ps.getContainer())) {
						throw new NotThreadLocalException(adviceLocal);
					}
				}
				
                // Check to see if match belongs to the same shadow group (may-alias check)
//                 String shadowId = match.getUniqueShadowId();
//                 boolean found = false;
//                 for (Shadow s : shadowsIntersectingPs) {
//                     if (s.getUniqueShadowId().equals(shadowId))
//                         found = true;
//                 }
//                 if (!found)
//                     continue;

				AliasInfo aliasInfo = UNKNOWN;

				String symbolName = shadow.getSymbolName();
                System.out.println("doing shadow ID "+shadow.getUniqueShadowId());

                /* Figure out if we have the exact same bindings as the
                 * tracematch we're tracking.
                 * If so, then we can carry out rule 3. */
                Map<String, Local> m = shadow.getTmFormalToAdviceLocal();
                for (Map.Entry<String,Local> tmFormalAndAdviceActual : m.entrySet()) {
					String tmFormal = tmFormalAndAdviceActual.getKey();
					Local adviceActual = tmFormalAndAdviceActual.getValue();
                    Local supposedAdviceActual = ps.getLocalForVarName(tmFormal);
                    System.out.println("tmFormal "+tmFormal+" aa "+adviceActual+" supposedAdviceActual "+supposedAdviceActual+" psm "+psm);
                    System.out.println();
                    if (psm.get(supposedAdviceActual) != null) {
                        Stmt saaDef = psm.get(supposedAdviceActual);
                        if(lma.mustAlias(adviceActual, stmt, supposedAdviceActual, saaDef))
                            aliasInfo = aliasInfo.join(MUST);
                        else if (lnma.notMayAlias(adviceActual, stmt, supposedAdviceActual, saaDef))
                            aliasInfo = aliasInfo.join(MAY_NOT);
                    }
				}

                if(aliasInfo==UNKNOWN) {
                	aliasInfo = MAY;
                } 
                
				//register, if must-aliased               
                if (aliasInfo.mustAlias()) {
					mustAliasedShadows.add(shadow);
				}

				//by definition of the tracematch semantics, we always remain in all initial states
                res.addAll(tm.getStateMachine().getInitialStates());

				Iterator it = currentStates.iterator();
//                //TODO why is this correct?
//				if (currentStates.isEmpty())
//					it = tm.getStateMachine().getStateIterator();

				for (; it.hasNext(); ) {
					SMNode cs = (SMNode) it.next();

                    // Final states are always sinks, don't treat them
                    // as sources here.
                    if (cs.isFinalNode())
                        continue;

                    System.out.println("from node "+cs);

                    // Rule 1: always add initial, (unless rule 3 applies - see below))
                    boolean mustAddCurrent = true;

					for (Iterator edgeIter = cs.getOutEdgeIterator(); edgeIter.hasNext();) {
						SMEdge edge = (SMEdge) edgeIter.next();

						if(edge.getLabel().equals(symbolName)) {
                            System.out.println("found edge; skip "+edge.isSkipEdge()+" label "+edge.getLabel()+" with alias info "+aliasInfo);
                            // Rule 3: if skip & same vars, don't add current
							if(edge.isSkipEdge()) {
                                if (aliasInfo.mustAlias()) 
                                    mustAddCurrent = false;
                            }
                            else {
                                // Rule 2: non-skip => add target
                                // (unless not-may)
                                if (aliasInfo.mayAlias()) {
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
	protected static boolean mayTransitivelyCallOtherShadow(Unit unit) {
		CallGraph abstractedCallGraph = CallGraphAbstraction.v().abstractedCallGraph();
		return abstractedCallGraph.edgesOutOf(unit).hasNext();
	}

	/**
	 * @return the mustAliasedShadows
	 */
	public Set<SymbolShadow> getMustAliasedShadows() {
		return mustAliasedShadows;
	}

	/**
	 * @return the gaveUp
	 */
	public boolean gaveUp() {
		return gaveUp;
	}
}
