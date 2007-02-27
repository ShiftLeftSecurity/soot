/*
 * Created on 1-Dec-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.ds;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import soot.Kind;
import soot.PointsToSet;
import soot.Scene;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.pointer.MemoryEfficientRasUnion;
import soot.util.IdentityHashSet;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.weaver.tmanalysis.VariableSMEdgeFactory.SMVariableEdge;

/**
 * Stores the {@link SMVariableEdge}s of a thread.
 *
 * @author Eric Bodden
 */
public class ThreadContext {

	public final static Edge MAIN = new Edge(null,null,Scene.v().getMainClass().getMethod("void main(java.lang.String[])"),Kind.THREAD);
	
	protected static Map threadStartEdgeToContext = new HashMap();
	
	protected Edge threadStartEdge;
	
	protected IdentityHashSet edges;

	protected Map varToUnion;
	
	/**
	 * @param threadStartEdge
	 * @param edges
	 */
	protected ThreadContext(Edge threadStartEdge) {
		this.threadStartEdge = threadStartEdge;
		this.edges = new IdentityHashSet();
	}
	
	public void notifyEdge(SMEdge edge) {
		if(varToUnion!=null) {
			throw new IllegalStateException("nothing may not be added no more once getThreadSummaryMapping() was called");
		}
		if(edge instanceof SMVariableEdge && edge.getLabel()!=null) {
			edges.add(edge);
		}
	}
	
	protected Map getThreadSummaryMapping() {
		if(varToUnion==null) {
			varToUnion = new HashMap();
			for (Iterator edgeIter = edges.iterator(); edgeIter.hasNext();) {
				SMVariableEdge varEdge = (SMVariableEdge) edgeIter.next();
				
				for (Iterator varIter = varEdge.getBoundVariables().iterator(); varIter.hasNext();) {
					String var = (String) varIter.next();
					PointsToSet pts = varEdge.getPointsToSet(var);
					
					MemoryEfficientRasUnion union = (MemoryEfficientRasUnion) varToUnion.get(var);
					if(union==null) {
						union = new MemoryEfficientRasUnion();
						varToUnion.put(var, union);
					}
					union.addAll(pts);
				}
			}
			varToUnion = Collections.unmodifiableMap(varToUnion);
		}
		return varToUnion;
	}
	
	protected static Map resultCache = new HashMap();
	
	public static boolean pointsToSharedObject(Map variableMapping) {
		
		Boolean result = (Boolean) resultCache.get(variableMapping);
		if(result==null) {
			result = Boolean.FALSE;			
			//for all variable->pts mappings
			outer:
			for (Iterator mappingEntryIter = variableMapping.entrySet().iterator(); mappingEntryIter.hasNext();) {
				Entry varMappingEntry = (Entry) mappingEntryIter.next();
				String var = (String) varMappingEntry.getKey();
				PointsToSet pts = (PointsToSet) varMappingEntry.getValue();
				
				boolean foundOne = false;

				//for all thread contexts
				for (Iterator contextIter = threadStartEdgeToContext.values().iterator(); contextIter.hasNext();) {
					ThreadContext tc = (ThreadContext) contextIter.next();
					Map summaryMapping = tc.getThreadSummaryMapping();
					
					//get the points-to set of the given variable in the summary
					PointsToSet summaryPts = (PointsToSet) summaryMapping.get(var);
					
					//if this thread affects the mapping, ths points-to-set cannot be null
					if(summaryPts!=null) {
						//intersect the given pts with the one in the summary
						PointsToSet intersection = Intersection.intersect(pts, summaryPts);
						if(!intersection.isEmpty()) {
							//the intersection is not empty so this thread could affect the points-to set
							
							if(foundOne) {
								//if we had already found another thread affecting the same variable
								//this means we now found two threads affecting the same variable; return true
								result = Boolean.TRUE;
								break outer;
							} else {
								//we found thwe first thread affecting the variable
								foundOne = true;
							}
						}
					}
				}
				
				//there should always be at least one thread affecting each variable
				//assert foundOne;
				assert foundOne;				
			}		
			resultCache.put(variableMapping, result);
		}
		
		return result.booleanValue();
	}
	
	
	public static ThreadContext contextOf(Edge threadStartEdge) {
		assert threadStartEdge.kind().equals(Kind.THREAD);
		
		ThreadContext tc = (ThreadContext) threadStartEdgeToContext.get(threadStartEdge);
		if(tc==null) {
			tc = new ThreadContext(threadStartEdge);
			threadStartEdgeToContext.put(threadStartEdge, tc);
		}
		
		return tc;
	}
	
	public String toString() {
		return "ThreadContext\nthread start edge: " + ((threadStartEdge==MAIN) ? "MAIN" : threadStartEdge.toString()) +
		  "\nedges:\n"+edges;
		
	}
}
