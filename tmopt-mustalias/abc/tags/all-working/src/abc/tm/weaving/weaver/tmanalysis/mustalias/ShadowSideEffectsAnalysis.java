/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package abc.tm.weaving.weaver.tmanalysis.mustalias;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootMethod;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.query.SymbolShadowWithPTS;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;


/**
 * IntraproceduralAnalysis: This analysis propagates tracematch
 * automaton states through the method.
 *
 * @author Eric Bodden
 */
public class ShadowSideEffectsAnalysis  {
	
	protected Map<Local,PointsToSet> localToPts = new HashMap<Local, PointsToSet>();
	
	/**
	 * Assume we have a negative binding <code>x!=o</code> and we want to combine it with a positive
	 * binding <code>y=p</code>. If we can prove that <code>y=p</code> can only ever occur with
	 * <code>x=o</code>, this contradicts the negative binding. In this case, we return <code>true</code>.
	 * @param tmVar the tracematch variable we bind
	 * @param toBind an incoming positive binding for some variable
	 * @param negVar the variable for an existing negative binding
	 * @param negBinding the negative binding we have for negVar
	 * @param container the method hokding toBind and negBinding
	 * @param tm tracematch we focus on 
	 */
	public boolean leadsToContradiction(String tmVar, Local toBind, String negVar, Local negBinding, SootMethod container, TraceMatch tm) {
		PointsToSet toBindPts = getPointsToSetOf(toBind);
		PointsToSet negBindingPts = getPointsToSetOf(negBinding);
		
		Set<SymbolShadowWithPTS> overlaps = new HashSet<SymbolShadowWithPTS>();
		
		Set<ShadowGroup> allShadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
		for (ShadowGroup shadowGroup : allShadowGroups) {
			if(shadowGroup.getTraceMatch().equals(tm)) {
				if(shadowGroup.hasCompatibleBinding(negVar,negBindingPts)) {
					if(shadowGroup.hasCompatibleBinding(tmVar, toBindPts)) {
						overlaps.addAll(shadowGroup.getAllShadows());
					}
				}
			}
		}
		
		for (SymbolShadowWithPTS shadow : overlaps) {
			if(!shadow.getContainer().equals(container)) {
				return false;
			}
		}

		return true;
	}

	protected PointsToSet getPointsToSetOf(Local toBind) {
		PointsToSet pointsToSet = localToPts.get(toBind);
		if(pointsToSet==null) {
			PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
			pointsToSet = pta.reachingObjects(toBind);
			localToPts.put(toBind, pointsToSet);
		}
		return pointsToSet;
	}
	

	//Singleton pattern
	
	protected static ShadowSideEffectsAnalysis instance;

	private ShadowSideEffectsAnalysis() {}
	
	public static ShadowSideEffectsAnalysis v() {
		if(instance==null) {
			instance = new ShadowSideEffectsAnalysis();
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
