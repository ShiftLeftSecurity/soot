/*
 * Created on 13-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class splits a given set of shadows into multiple sets so that each set holds only shadows of one tracematch. 
 *
 * @author Eric Bodden
 */
public class ShadowsPerTMSplitter {
	
	/**
	 * Splits the shadows in the given set per tracematch.
	 * @param shadows a set of {@link Shadow}s
	 * @return a mapping from tracematch name ({@link String}) to a {@link Set} of {@link Shadow}s of that tracematch
	 */
	public Map splitShadows(Collection shadows) {
		Map tmNameToShadows = new HashMap();
		
		for (Iterator shadowIter = shadows.iterator(); shadowIter.hasNext();) {
			Shadow shadow = (Shadow) shadowIter.next();
			
			String uniqueShadowId = shadow.getUniqueShadowId();
			String tracematchName = Naming.getTracematchName(uniqueShadowId);
			
			Set shadowsForTm = (Set) tmNameToShadows.get(tracematchName);
			if(shadowsForTm==null) {
				shadowsForTm = new HashSet();
				tmNameToShadows.put(tracematchName, shadowsForTm);
			}
			
			shadowsForTm.add(shadow);
		}

		return tmNameToShadows;		
	}
	
	//singleton pattern
	
	protected static ShadowsPerTMSplitter instance;

	private ShadowsPerTMSplitter() {}
	
	public static ShadowsPerTMSplitter v() {
		if(instance==null) {
			instance = new ShadowsPerTMSplitter();
		}
		return instance;		
	}

}
