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
	 * Disables all shadows in deadShadows.
	 * Then disables all (other) shadows which are not member of a shadow group any more.
	 * @param deadShadows a set of shadows which is known to be dead, i.e. unnecessary
	 * @return <code>true</code> if a shadow could actually be disabled
	 * @see ShadowGroup#getLabelShadows()
	 * @see {@link ShadowRegistry#disableShadow(String)}
	 */
	public boolean pruneShadowsAndIncompleteDependentGroups(Collection deadShadows) {
		boolean removedAGroup = true;
		Set allShadowsInAllShadowGroups = new HashSet();
		
		//disable all dead shadows
		for (Iterator deadShadowIter = deadShadows.iterator(); deadShadowIter.hasNext();) {
			Shadow shadow = (Shadow) deadShadowIter.next();
			if(Debug.v().tmShadowDump) {
				System.err.println("Disabling shadow "+shadow.getUniqueShadowId()+
						" because it was marked as dead.");
			}
			ShadowRegistry.v().disableShadow(shadow.getUniqueShadowId());
		}
		
		//prune all groups which have a dead shadow as a label-shadow
		for (Iterator groupIter = shadowGroups.iterator(); groupIter.hasNext();) {
			ShadowGroup group = (ShadowGroup) groupIter.next();

			//collect all shadows
			allShadowsInAllShadowGroups.addAll(group.getAllShadows());
			
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
					removedAGroup = true;
					break;
				}
			}
		}
		
		if(removedAGroup) {
			//collect all shadows which are still active, i.e. still contained in a remaining shadow group
			Set allShadowsStillActive = new HashSet();
			for (Iterator groupIter = shadowGroups.iterator(); groupIter.hasNext();) {
				ShadowGroup group = (ShadowGroup) groupIter.next();
				allShadowsStillActive.addAll(group.getAllShadows());
			}
			
			//we can disable all shadows which were in a shadow group before but now are not any more
			Set shadowsToDisable = new HashSet(allShadowsInAllShadowGroups);
			shadowsToDisable.removeAll(allShadowsStillActive);			
			for (Iterator shadowIter = shadowsToDisable.iterator(); shadowIter.hasNext();) {
				Shadow shadow = (Shadow) shadowIter.next();
				ShadowRegistry.v().disableShadow(shadow.getUniqueShadowId());
				if(Debug.v().tmShadowDump) {
					System.err.println("Removed shadow "+shadow.getUniqueShadowId()+
							" because it is no longer part of any active shadow group.");
				}
			}			
			return !shadowsToDisable.isEmpty();
		} else {
			return false;
		}
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
