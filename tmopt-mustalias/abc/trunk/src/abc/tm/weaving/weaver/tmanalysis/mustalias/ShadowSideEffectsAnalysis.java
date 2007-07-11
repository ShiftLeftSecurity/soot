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
	 * Returns <code>true</code> if all shadows with overlapping bindings for the given variable are in the given method. 
	 */
	public boolean allShadowsWithOverLappingBindingInSameMethod(String tmVar, Local toBind, SootMethod container, TraceMatch tm) {
		PointsToSet toBindPts = getPointsToSetOf(toBind);
		
		Set<SymbolShadowWithPTS> overlaps = new HashSet<SymbolShadowWithPTS>();
		
		Set<ShadowGroup> allShadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
		for (ShadowGroup shadowGroup : allShadowGroups) {
			if(shadowGroup.getTraceMatch().equals(tm)) {
				if(shadowGroup.hasCompatibleBinding(tmVar, toBindPts)) {
					overlaps.addAll(shadowGroup.getAllShadows());
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
