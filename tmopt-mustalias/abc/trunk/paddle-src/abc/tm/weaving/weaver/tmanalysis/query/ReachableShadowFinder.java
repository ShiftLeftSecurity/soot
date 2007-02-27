/*
 * Created on 12-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.tagkit.Host;

/**
 * Finds all reachable shadows in a callgraph. It uses result caching, i.e. assumes that the call graph does not change over time.
 *
 * @author Eric Bodden
 */
public class ReachableShadowFinder {
	
	/** the current set of reachable shadows */
	protected Map cgToReachableShadows;
	
	/** current working copy for the call graph that is passed in */
	protected transient Collection reachableShadows;
	
	/**
	 * Computes the reachable shadows of cg if it has not been computed before or
	 * returns a cached result otherwise.
	 * @param cg a call graph
	 * @return a set of {@link Shadow}s rechable in cg
	 */
	public Set reachableShadows(CallGraph cg) {
		
		reachableShadows = (Collection) cgToReachableShadows.get(cg);
		if(reachableShadows==null) {
			reachableShadows = new LinkedList();

			//get all weavable reachable methods in the callgraph
			Collection methods = WeavableMethods.v().getReachable(cg);

			for (Iterator methodIter = methods.iterator(); methodIter.hasNext();) {
				SootMethod method = (SootMethod) methodIter.next();
				findShadowsIn(method);
			}
			
			cgToReachableShadows.put(cg, reachableShadows);
		}		
		
		return new HashSet(reachableShadows);
	}
	
	/**
	 * Computes the reachable shadows of cg if it has not been computed before or
	 * returns a cached result otherwise.
	 * @param cg a call graph
	 * @return a set of unique shadow IDs of {@link Shadow}s rechable in cg
	 * @see Naming#uniqueShadowID(String, int)
	 * @see Naming#uniqueShadowID(String, String, int)
	 */
	public Set reachableShadowIDs(CallGraph cg) {
		return Shadow.uniqueShadowIDsOf(reachableShadows(cg));
	}
	
	/**
	 * adds all shadows in the method to {@link #reachableShadows}
	 * @param method any reachable method
	 */
	protected void findShadowsIn(SootMethod method) {
		addShadowIfPresent(method);
		Body b = method.getActiveBody();

		for (Iterator unitIter = b.getUnits().iterator(); unitIter.hasNext();) {
			Unit u = (Unit) unitIter.next();
			addShadowIfPresent(u);
		}
	}


	/**
	 * if h has a shadows attached, those are added to {@link #reachableShadows}
	 * @param a host
	 */
	protected void addShadowIfPresent(Host h) {
		if(TaggedHosts.v().hasTag(h)) {
			reachableShadows.addAll(TaggedHosts.v().getShadowsOf(h));
		}
	}

	//singleton pattern
	
	protected static ReachableShadowFinder instance;

	private ReachableShadowFinder() {
		cgToReachableShadows = new IdentityHashMap();		
	}
	
	public static ReachableShadowFinder v() {
		if(instance==null) {
			instance = new ReachableShadowFinder();
		}
		return instance;		
	}
	
	/**
	 * Frees the singleton object. 
	 */
	public static void reset() {
		instance = null;
	}


}
