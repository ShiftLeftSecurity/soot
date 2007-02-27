/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Eric Bodden
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
package abc.tm.weaving.weaver.tmanalysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.query.Naming;
import abc.tm.weaving.weaver.tmanalysis.query.TaggedHosts;
import abc.weaving.aspectinfo.Var;
import abc.weaving.residues.WeavingVar;

/**
 * TODO comment
 * @author Eric Bodden
 */
public class MatchingTMSymbolTag implements Tag {

	public static String NAME = "abc.tm.weaving.weaver.tmanalysis.MatchingTMSymbolWithVarsTag";
	
	protected final Map /*<String,Map<Var,WeavingVar>>*/ symbolToVarToWeavingVar;
	
	protected final Map /*<String,Set<Integer>>*/ symbolToShadowIds;
	
	/** Indicates whether unwoven were removed. Clients should only access this
	 *  object if it is clean. Call {@link #removeUnwovenMappings()} to clean the object. */
	private boolean clean;

	/**
	 * @param matchingSymbolIDs
	 */
	public MatchingTMSymbolTag() {
		if(TaggedHosts.v().isTaggingCompleted()) {
			throw new IllegalStateException("Tagging is completed (see class TaggedHosts).");
		}
		symbolToVarToWeavingVar = new HashMap();
		symbolToShadowIds = new HashMap();
		clean = true;
	}

	/** 
	 * {@inheritDoc}
	 */
	public String toString() {
		return symbolToVarToWeavingVar.toString();
	}
	
	/**
	 * TODO comment
	 * @param symbolId
	 * @return
	 */
	public Map getVariableMappingForSymbol(String symbolId) {
		assert symbolToVarToWeavingVar.containsKey(symbolId);
		assert clean;
		return (Map) symbolToVarToWeavingVar.get(symbolId);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Set getMatchingSymbolIDs() {
		assert clean;
		return symbolToVarToWeavingVar.keySet();
	}
	
	public String getMatchingUniqueShadowID(String symbolId) {
		return Naming.uniqueShadowID(symbolId, getShadowId(symbolId));			
	}

	public Set getMatchingUniqueShadowIDs() {
		Set symbolIDs = getMatchingSymbolIDs();
		Set result = new HashSet();
		for (Iterator symIdIter = symbolIDs.iterator(); symIdIter.hasNext();) {
			String symbolId = (String) symIdIter.next();
			result.add(Naming.uniqueShadowID(symbolId, getShadowId(symbolId)));			
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + ((symbolToVarToWeavingVar == null) ? 0 : symbolToVarToWeavingVar.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MatchingTMSymbolTag other = (MatchingTMSymbolTag) obj;
		if (symbolToVarToWeavingVar == null) {
			if (other.symbolToVarToWeavingVar != null)
				return false;
		} else if (!symbolToVarToWeavingVar.equals(other.symbolToVarToWeavingVar))
			return false;
		return true;
	}

	/**
	 * @param matchingSymbolId
	 */
	public void addMatchingSymbolWithoutVariables(String matchingSymbolId, int shadowId) {
		//assert that if we already have a mapping for this symbol,
		//it is also empty
		assert symbolToVarToWeavingVar.get(matchingSymbolId)==null
		    || ((Map)symbolToVarToWeavingVar.get(matchingSymbolId)).isEmpty();
		
		symbolToVarToWeavingVar.put(matchingSymbolId, new HashMap());
		
		registerShadowId(shadowId,matchingSymbolId);
		clean = false;
	}
	
	/**
	 * @param matchingSymbolId
	 */
	public void addMapping(String matchingSymbolId, Var v, WeavingVar wv, int shadowId) {
		Map varToWeavingVars = (Map) symbolToVarToWeavingVar.get(matchingSymbolId);
		if(varToWeavingVars==null) {
			varToWeavingVars = new HashMap();
			symbolToVarToWeavingVar.put(matchingSymbolId, varToWeavingVars);
		}
		
		Set weavingVars = (Set) varToWeavingVars.get(v);
		if(weavingVars==null) {
			weavingVars = new HashSet();
			varToWeavingVars.put(v, weavingVars);
		}
		weavingVars.add(wv);
		
		registerShadowId(shadowId,matchingSymbolId);
		clean = false;
	}

	/**
	 * @param shadowId
	 * @param matchingSymbolId 
	 */
	protected void registerShadowId(int shadowId, String matchingSymbolId) {
		Set shadowIDs = (Set) symbolToShadowIds.get(matchingSymbolId);
		if(shadowIDs==null) {
			shadowIDs = new HashSet();
			symbolToShadowIds.put(matchingSymbolId, shadowIDs);
		}
		
		shadowIDs.add(new Integer(shadowId));
	}

	/**
	 * 
	 */
	public void removeUnwovenMappings() {

		for (Iterator entryIter = symbolToVarToWeavingVar.entrySet().iterator(); entryIter.hasNext();) {
			Entry entry = (Entry) entryIter.next();
			Map varToWeavingVars = (Map) entry.getValue();
			
			boolean hadEmptyMapping = varToWeavingVars.isEmpty();
			
			//for each mapping
			for (Iterator iterator = varToWeavingVars.entrySet().iterator(); iterator.hasNext();) {
				Entry innerEntry = (Entry) iterator.next();
				Set weavingVars = (Set) innerEntry.getValue();
				//for each weaving var
				for (Iterator wvIter = weavingVars.iterator(); wvIter.hasNext();) {
					WeavingVar wv = (WeavingVar) wvIter.next();
					try {
						//see if it was woven
						wv.get();
					} catch(RuntimeException e) {
						//if an exception occurs, this means that no local has been
						//associated with this WeavingVar, most likely, cause
						//the WeavingVar was never actually woven;
						//we are not interested in such entries
						wvIter.remove();
					}					
				}
				if(weavingVars.isEmpty()) {
					iterator.remove();
				}
			}
			
			if(!hadEmptyMapping && varToWeavingVars.isEmpty()) {
				symbolToShadowIds.remove(entry.getKey());
				entryIter.remove();				
			}
		}	

		clean = true;		
	}
	
	public int getShadowId(String qualifiedSymbolId) {
		assert clean;
		Set shadowIDs = (Set) symbolToShadowIds.get(qualifiedSymbolId);
		assert shadowIDs!=null;
		assert shadowIDs.size()==1;
		
		return ((Integer)shadowIDs.iterator().next()).intValue();		
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] getValue() throws AttributeValueException {
		throw new UnsupportedOperationException("This tag is not meant to be written to a class file.");
	}

	/**
	 * Prunes all information from this tag related to the tracematch tm.
	 * @param tm some tracematch
	 * @return true, if this tag is empty after the deletion
	 */
	public boolean pruneTracematch(TraceMatch tm) {
		String tmNameToRemove = tm.getName();
		for (Iterator iterator = symbolToShadowIds.keySet().iterator(); iterator.hasNext();) {
			String qualifiedSymbolID = (String) iterator.next();
			String tmName = Naming.getTracematchName(qualifiedSymbolID);
			if(tmName.equals(tmNameToRemove)) {
				iterator.remove();
			}
		}
		for (Iterator iterator = symbolToVarToWeavingVar.keySet().iterator(); iterator.hasNext();) {
			String qualifiedSymbolID = (String) iterator.next();
			String tmName = Naming.getTracematchName(qualifiedSymbolID);
			if(tmName.equals(tmNameToRemove)) {
				iterator.remove();
			}
		}
		return symbolToShadowIds.isEmpty() && symbolToVarToWeavingVar.isEmpty();
	}

	/**
	 * @param uniqueShadowId
	 * @return 
	 */
	public boolean removeMappingsForShadow(String uniqueShadowId) {

		assert symbolToShadowIds.keySet().equals(symbolToVarToWeavingVar.keySet());

		String symbolName = Naming.getSymbolShortName(uniqueShadowId);
		String tmName = Naming.getTracematchName(uniqueShadowId);
		String fullSymbolName = Naming.uniqueSymbolID(tmName, symbolName);
		int shadowId = Naming.getShadowIdFromUniqueShadowId(uniqueShadowId);
		
		assert getShadowId(fullSymbolName) == shadowId;
		
		{
			Object old = symbolToShadowIds.remove(fullSymbolName);
			assert old!=null;
		}

		{
			Object old = symbolToVarToWeavingVar.remove(fullSymbolName);
			assert old!=null;
		}

		assert symbolToShadowIds.keySet().equals(symbolToVarToWeavingVar.keySet());
		
		return symbolToShadowIds.isEmpty();
	}
	
}
