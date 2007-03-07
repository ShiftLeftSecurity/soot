/*
 * Created on 12-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import polyglot.util.ErrorInfo;
import soot.Local;
import soot.PointsToSet;
import soot.PrimType;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.pointer.FullObjectSet;
import soot.tagkit.Host;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.ds.PaddlePointsToSetCompatibilityWrapper;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowMatchTag;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolFinder.SymbolShadowMatch;

/**
 * Shadow
 *
 * @author Eric Bodden
 */
public class Shadow {
	
	protected Map varToPointsToSet;		

	protected Map varToSootLocal;		

	protected String uniqueShadowId;

	protected boolean hasEmptyMapping;

	protected final SootMethod container;
	
//	/**
//	 * @param variableMapping
//	 */
//	public Shadow(String uniqueShadowId, Map variableMapping, SootMethod container) {
//		this.container = container;
//		assert uniqueShadowId!=null && variableMapping!=null && container!=null;
//		this.uniqueShadowId = uniqueShadowId;
////		this.hasEmptyMapping = false;
//		importVariableMapping(variableMapping, container);
//	}
	
	/**
	 * @param match
	 * @param container
	 */
	public Shadow(SymbolShadowMatch match, SootMethod container) {
		this.hasEmptyMapping = false;
		this.container = container;
		this.uniqueShadowId = match.getUniqueShadowId();
		importVariableMapping(match.getTmVarToAdviceLocal(), container);
	}

	public static Set allActiveShadowsForTag(SymbolShadowMatchTag tag, SootMethod container) {
		Set result = new HashSet();

		for (SymbolShadowMatch match : tag.getAllMatches()) {
			if(match.isEnabled()) {
				result.add(new Shadow(match,container));
			}
		}
//		for (Iterator symbolIdIter = tag.getMatchingSymbolIDs().iterator(); symbolIdIter.hasNext();) {
//			String symbolId = (String) symbolIdIter.next();
//			
//			Map mapping = tag.getVariableMappingForSymbol(symbolId);
//			String uniqueShadowId = tag.getMatchingUniqueShadowID(symbolId);
//			
//			result.add(new Shadow(uniqueShadowId,mapping,container));
//		}
		return result;
	}
	
	public static Set allActiveShadowsForHost(Host h, SootMethod container) {
		if(h.hasTag(SymbolShadowMatchTag.NAME)) {
			SymbolShadowMatchTag tag = (SymbolShadowMatchTag) h.getTag(SymbolShadowMatchTag.NAME);
			return allActiveShadowsForTag(tag, container);
		} else {
			return Collections.EMPTY_SET; 
		}
	}

	public static Set allActiveShadowsForHostAndTM(Host h, SootMethod container, TraceMatch tm) {
		Set result = new HashSet();
		Set allShadowsForHost = allActiveShadowsForHost(h, container);
		for (Iterator shadowIter = allShadowsForHost.iterator(); shadowIter.hasNext();) {
			Shadow shadow = (Shadow) shadowIter.next();
			String shadowId = shadow.getUniqueShadowId();
			String tracematchName = Naming.getTracematchName(shadowId);
			if(tracematchName.equals(tm.getName())) {
				result.add(shadow);
			}
		}
		return result;
	}
	
	public void importVariableMapping(Map<String,Local> mapping, SootMethod container)  {
		
		varToPointsToSet = new HashMap();
		varToSootLocal = new HashMap();
		
		for (Entry<String,Local> entry : mapping.entrySet()) {

			Local l = entry.getValue();
			PointsToSet pts;
			if(l.getType() instanceof PrimType) {
				//if the type of the variable is a primitive type, then we assume it could "point to anything", i.e. could have any value
				pts = FullObjectSet.v();
			} else {
				//if l is null this probably means that the WeavingVar was never woven
				assert l!=null;
				PointsToSet paddlePts = (PointsToSet) Scene.v().getPointsToAnalysis().reachingObjects(l);
				if(paddlePts.isEmpty()) {
					hasEmptyMapping = true;
					abc.main.Main.v().error_queue.enqueue(ErrorInfo.WARNING, "Empty points-to set for variable "+l+" in "+container);
					/*   POSSIBLE CAUSES FOR EMPTY VARIABLE MAPPINGS:
					 *   1.) a shadow is created for an invoke statement (or similar) of the form o.foo() where o has type NullType, i.e. is certainly null.
					 *   2.) if object-sensitivity is enabled:
					 *       if dynamic type checks for advice aplication can be ruled out statically; 
					 *        example: point cut is call(* Set.add(..)) && args(Collection)
					 *                 shadow is s.add("someString")
					 *                 in this case, paddle sees automatically that the instanceof check in the bytecode
					 *                 can never succeed; hence the statement is rendered unreachable and the points-to set becomes empty
					 */
				}							
				pts = new PaddlePointsToSetCompatibilityWrapper(paddlePts);
			}
			String varName = entry.getKey();
			varToPointsToSet.put(varName,pts);
			varToSootLocal.put(varName, l);
			
		}
		
//		if(varToPointsToSet!=null) {
//			throw new IllegalStateException("mapping already set");
//		}
//		varToPointsToSet = new HashMap();
//		varToSootLocal = new HashMap();
//		//for each WeavingVar, get its local l and for that l get and store
//		//the points-to set
//		for (Iterator iter = mapping.entrySet().iterator(); iter.hasNext();) {
//			Entry entry = (Entry) iter.next();
//			Collection weavingVars = (Collection) entry.getValue();
//
//			assert weavingVars.size()<=1;
//
//			if(weavingVars.size()>0) {
//				WeavingVar wv = (WeavingVar) weavingVars.iterator().next();
//				Local l = wv.get();
//				
//				PointsToSet pts;
//				if(l.getType() instanceof PrimType) {
//					//if the type of the variable is a primitive type, then we assume it could "point to anything", i.e. could have any value
//					pts = FullObjectSet.v();
//				} else {
//					//if l is null this probably means that the WeavingVar was never woven
//					assert l!=null;
//					PointsToSet paddlePts = (PointsToSet) Scene.v().getPointsToAnalysis().reachingObjects(l);
//					if(paddlePts.isEmpty()) {
//						hasEmptyMapping = true;
//						abc.main.Main.v().error_queue.enqueue(ErrorInfo.WARNING, "Empty points-to set for variable "+l+" in "+container);
//						/*   POSSIBLE CAUSES FOR EMPTY VARIABLE MAPPINGS:
//						 *   1.) a shadow is created for an invoke statement (or similar) of the form o.foo() where o has type NullType, i.e. is certainly null.
//						 *   2.) if object-sensitivity is enabled:
//						 *       if dynamic type checks for advice aplication can be ruled out statically; 
//						 *        example: point cut is call(* Set.add(..)) && args(Collection)
//						 *                 shadow is s.add("someString")
//						 *                 in this case, paddle sees automatically that the instanceof check in the bytecode
//						 *                 can never succeed; hence the statement is rendered unreachable and the points-to set becomes empty
//						 */
//					}							
//					pts = new PaddlePointsToSetCompatibilityWrapper(paddlePts);
//				}
//				String varName = ((Var)entry.getKey()).getName();
//				varToPointsToSet.put(varName,pts);
//				varToSootLocal.put(varName, l);
//			}
//		}
	}
	
	/**
	 * @return the varToPointsToSet
	 */
	public Map getVariableMapping() {
		return Collections.unmodifiableMap(new HashMap(varToPointsToSet));
	}
	
	public Local getLocalForVarName(String varName) {
		assert varToSootLocal.containsKey(varName);
		return (Local) varToSootLocal.get(varName);
	}

	
	public Set getBoundVariables() {
		return Collections.unmodifiableSet(new HashSet(varToPointsToSet.keySet()));
	}
	;
	public boolean hasVariableMapping() {
		return varToPointsToSet!=null;
	}

	
	/**
	 * TODO comment
	 * @param v
	 * @return
	 */
	public PointsToSet getPointsToSet(String v) {
		return (PointsToSet) varToPointsToSet.get(v);
	}
	
	/**
	 * @return the uniqueShadowId
	 */
	public String getUniqueShadowId() {
		return uniqueShadowId;
	}
	
	/**
	 * For a given set of {@link Shadow}s, returns the set of their
	 * unique shadow IDs.
	 * @param shadows a set of {@link Shadow}s
	 * @return the set of their shadow IDs
	 * @see Naming#uniqueShadowID(String, int)
	 * @see Naming#uniqueShadowID(String, String, int)
	 */
	public static Set uniqueShadowIDsOf(Set shadows) {
		Set ids = new HashSet();
		for (Iterator shadowIter = shadows.iterator(); shadowIter.hasNext();) {
			Shadow shadow = (Shadow) shadowIter.next();
			ids.add(shadow.getUniqueShadowId());
		}
		return Collections.unmodifiableSet(ids);
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((uniqueShadowId == null) ? 0 : uniqueShadowId.hashCode());
		result = prime
				* result
				+ ((varToPointsToSet == null) ? 0 : varToPointsToSet.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Shadow other = (Shadow) obj;
		if (uniqueShadowId == null) {
			if (other.uniqueShadowId != null)
				return false;
		} else if (!uniqueShadowId.equals(other.uniqueShadowId))
			return false;
		if (varToPointsToSet == null) {
			if (other.varToPointsToSet != null)
				return false;
		} else if (!varToPointsToSet.equals(other.varToPointsToSet))
			return false;
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return uniqueShadowId + "(" + varToPointsToSet + ")";
	}

	/**
	 * @return the hasEmptyMapping
	 */
	public boolean hasEmptyMapping() {
		return hasEmptyMapping;
	}

	/**
	 * @return the container
	 */
	public SootMethod getContainer() {
		return container;
	}
	
	

}
