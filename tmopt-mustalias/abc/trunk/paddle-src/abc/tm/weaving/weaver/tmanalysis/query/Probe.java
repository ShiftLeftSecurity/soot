/*
 * Created on 27-Feb-07
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

import abc.tm.weaving.weaver.tmanalysis.stages.FlowInsensitiveAnalysis;

/**
 * Probe
 *
 * @author Eric Bodden
 */
public class Probe {
	
	private static int nextProbeNumber;
	
	protected Set shadows; 
	
	protected int number;
	
	private Probe(Collection shadowSet) {
		shadows = new HashSet();
		shadows.addAll(shadowSet);
		shadows = Collections.unmodifiableSet(shadows);
		number = nextProbeNumber++;
	}
	
	public int getNumber() {
		return number;
	}

	public Set getShadows() {
		return shadows;
	}
	
	protected static Set allSoundProbes = null;
	
	public static Set generateAllSoundProbes() {
		if(allSoundProbes!=null) {
			return allSoundProbes;
		}		
		
		nextProbeNumber = 0;
		
		Set shadowSets = new HashSet();
		
		Set allConsistentShadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
		for (Iterator groupIter = allConsistentShadowGroups.iterator(); groupIter
				.hasNext();) {
			ShadowGroup sg = (ShadowGroup) groupIter.next();
			shadowSets.add(sg.getAllShadows());
		}
		
		Set probes = new HashSet();
		for (Iterator shadowSetsIter = shadowSets.iterator(); shadowSetsIter.hasNext();) {
			Collection shadowSet = (Collection) shadowSetsIter.next();
			probes.add(new Probe(shadowSet));
		}
		
		allSoundProbes = probes;
		
		return probes;
	}

}
