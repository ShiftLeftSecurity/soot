/*
 * Created on 12-Feb-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.stages;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.SootMethod;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;

/**
 * IntraproceduralFlowAnalysis
 *
 * @author Eric Bodden
 */
public class IntraproceduralFlowAnalysis extends AbstractAnalysisStage {

	/**
	 * {@inheritDoc}
	 */
	protected void doAnalysis() {
		
		Set shadowGroups = FlowInsensitiveAnalysis.v().getAllConsistentShadowGroups();
		
		Set singleMethodShadowGroups = new HashSet();
		
		for (Iterator groupIter = shadowGroups.iterator(); groupIter.hasNext();) {
			ShadowGroup group = (ShadowGroup) groupIter.next();
			
			boolean allSameMethod = true;
			SootMethod declaringMethod = null;
			for (Iterator ShadowIter = group.getLabelShadows().iterator(); ShadowIter.hasNext();) {
				Shadow shadow = (Shadow) ShadowIter.next();
				if(declaringMethod==null) {
					declaringMethod = shadow.getContainer();
				} else {
					if(declaringMethod!=shadow.getContainer()) {
						//found that there are at least two different container methods
						allSameMethod = false;
						break;
					}
				}				
			}
			
			if(allSameMethod) {
				singleMethodShadowGroups.add(group);
			}			
		}
	}
	
	//singleton pattern

	protected static IntraproceduralFlowAnalysis instance;

	private IntraproceduralFlowAnalysis() {}
	
	public static IntraproceduralFlowAnalysis v() {
		if(instance==null) {
			instance = new IntraproceduralFlowAnalysis();
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
