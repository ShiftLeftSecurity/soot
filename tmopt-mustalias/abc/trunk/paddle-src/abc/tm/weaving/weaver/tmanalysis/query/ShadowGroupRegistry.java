/*
 * Created on 2-Mar-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import abc.main.Debug;

/**
 * ShadowGroupRegistry
 *
 * @author Eric Bodden
 */
public class ShadowGroupRegistry {
	
	protected Set shadowGroups;
	
	/**
	 * Registers a collection of {@link ShadowGroup}s with this registry.
	 * @param groups
	 */
	public void registerShadowGroups(Collection groups) {
		if(shadowGroups==null) {
			shadowGroups = new HashSet();
		}		
		shadowGroups.addAll(groups);
	}
	
	/**
	 * Returns all shadow groups currently registered.
	 * @return a set of all {@link ShadowGroup}s currently registered
	 */
	public Set getAllShadowGroups() {
		if(shadowGroups==null) {
			throw new RuntimeException("Shadow groups not yet available. Apply FlowInsensitiveAnalysis first.");
		}		
		return Collections.unmodifiableSet(shadowGroups);
	}
	
	/**
	 * Prunes all shadow groups which have a label-shadow equal to one one of the shadows in deadShadows.
	 * @param deadShadows a set of shadows which is known to be dead, i.e. unnecessary
	 * @return <code>true</code> if any shadow group was actually removed
	 * @see ShadowGroup#getLabelShadows()
	 */
	public boolean pruneShadowsAndIncompleteDependentGroups(Collection deadShadows) {
		boolean removedSomething = true;
		for (Iterator groupIter = shadowGroups.iterator(); groupIter.hasNext();) {
			ShadowGroup group = (ShadowGroup) groupIter.next();
			Set labelShadows = group.getLabelShadows();
			for (Iterator shadowIter = deadShadows.iterator(); shadowIter.hasNext();) {
				Shadow deadShadow = (Shadow) shadowIter.next();
				if(labelShadows.contains(deadShadow)) {
					if(Debug.v().tmShadowGroupDump) {
						System.err.println("Removed shadow group #"+group.getNumber()+
								" because it contains label-shadow "+deadShadow.getUniqueShadowId()+
								", which is dead.");
					}
					groupIter.remove();
					removedSomething = true;					
				}
			}
		}			
		return removedSomething;
	}

	//singleton pattern
	
	protected static ShadowGroupRegistry instance;
	
	private ShadowGroupRegistry() {}
	
	public static ShadowGroupRegistry v() {
		if(instance==null) {
			instance = new ShadowGroupRegistry();
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
