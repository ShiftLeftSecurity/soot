/*
 * Created on Sep 13, 2006
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis;

import java.util.IdentityHashMap;

/**
 * StateMachineFoldingCache
 *
 * @author Eric Bodden
 */
public class StateMachineFoldingCache {
	
	protected final IdentityHashMap smToFoldedSm;
	
	protected static StateMachineFoldingCache instance;
	
	public static StateMachineFoldingCache v() {
		if(instance==null) {
			instance = new StateMachineFoldingCache();
		}
		return instance;
	}
	
	
	/**
	 * Frees the singleton object. 
	 */
	public static void reset() {
		instance = null;
	}
	
	/**
	 * 
	 */
	private StateMachineFoldingCache() {
		this.smToFoldedSm = new IdentityHashMap();
	}
	
	/**
	 * @param machine
	 * @return 
	 */
	public UGStateMachine getCachedFoldedInstance(UGStateMachine machine) {
		return (UGStateMachine) smToFoldedSm.get(machine);
	}
	
	public void registerCachedFoldedInstance(UGStateMachine machine, UGStateMachine folded) {
		assert !smToFoldedSm.containsKey(machine);
		smToFoldedSm.put(machine, folded);
	}

}
