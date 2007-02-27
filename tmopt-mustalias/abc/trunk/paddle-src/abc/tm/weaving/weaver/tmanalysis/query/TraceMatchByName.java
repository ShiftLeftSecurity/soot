/*
 * Created on 13-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;

/**
 * TraceMatchByName
 *
 * @author Eric Bodden
 */
public class TraceMatchByName {
	
	protected TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo(); 
	
	protected Map nameToTm;
	
	public TraceMatch get(String tmName) {
		assert nameToTm.containsKey(tmName);
		return (TraceMatch) nameToTm.get(tmName);
	}

	private TraceMatchByName() {
	
		nameToTm = new HashMap();
		
		for (Iterator tmIter = gai.getTraceMatches().iterator(); tmIter.hasNext();) {
			TraceMatch tm = (TraceMatch) tmIter.next();
		
			Object old = nameToTm.put(tm.getName(), tm);
			assert old==null; //names should be unique
		}
		
	}
	

	
	protected static TraceMatchByName instance;

	public static TraceMatchByName v() {
		if(instance==null) {
			instance = new TraceMatchByName();
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
