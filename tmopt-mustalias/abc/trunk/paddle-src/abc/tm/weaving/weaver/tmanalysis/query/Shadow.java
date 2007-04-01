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
import java.util.List;
import java.util.LinkedList;
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
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
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

	protected Map sootLocalToVar;		

	protected String uniqueShadowId;

	protected boolean hasEmptyMapping;

	protected final SootMethod container;

	protected List<Local> boundLocals;
	
	/**
	 * @param match
	 * @param container
	 */
	public Shadow(SymbolShadowMatch match, SootMethod container) {
		this.hasEmptyMapping = false;
		this.container = container;
		this.uniqueShadowId = match.getUniqueShadowId();
		this.boundLocals = null;
		importVariableMapping(match.getTmFormalToAdviceLocal(), container);
	}
	
	public void importVariableMapping(Map<String,Local> mapping, SootMethod container)  {
		assert boundLocals == null;
		
		varToPointsToSet = new HashMap();
		varToSootLocal = new HashMap();
		sootLocalToVar = new HashMap();
		
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
			assert !sootLocalToVar.containsKey(l);
			sootLocalToVar.put(l, varName);
		}
			
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
	
	public String getVarNameForLocal(Local l) {
		return (String) sootLocalToVar.get(l);
	}

	
	public Set getBoundVariables() {
		return Collections.unmodifiableSet(new HashSet(varToPointsToSet.keySet()));
	}

	// TODO generalize for must-alias info, or factor it back out to callers or something.
    public List<Local> getBoundLocals() {
		if (this.boundLocals != null)
			return this.boundLocals;

		boundLocals = new LinkedList<Local>();
		for (Object var : getBoundVariables()) {
			boundLocals.add(getLocalForVarName((String)var));
		}
		boundLocals = Collections.unmodifiableList(boundLocals);
		return boundLocals;
	}
	
	public boolean hasVariableMapping() {
		return varToPointsToSet!=null;
	}

	
	/**
	 * Returns the points-to set corresponding to shadow bound variable v.
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
	
	public TraceMatch getTraceMatch() {
		//TODO optimize by storing reference at construction time
		String tmName = Naming.getTracematchName(getUniqueShadowId());
		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
		return gai.traceMatchByName(tmName);		
	}
	
	

}
