/*
 * Created on 12-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.SootMethod;
import soot.tagkit.Host;
import abc.main.Debug;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TMOptPerSymbolAdviceDecl;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.dynamicinstr.DynamicInstrumentationResidue;
import abc.tm.weaving.weaver.tmanalysis.util.AdviceApplicationVisitor;
import abc.tm.weaving.weaver.tmanalysis.util.AdviceApplicationVisitor.AdviceApplicationHandler;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowMatchTag;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolFinder.SymbolShadowMatch;

/**
 * ShadowRegistry
 *
 * @author Eric Bodden
 */
public class ShadowRegistry {
	
	protected static TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
	
	protected Map allShadowsToAdviceApplications;
	
	protected Map tmNameToUniqueShadowIds;
	
	protected Map shadowIdToNumber;
	
	protected Set enabledShadows;  
	
	protected Set disabledShadows;  
	
	protected Set shadowsToBeRetained;
	
	protected ShadowRegistry() {
	
		allShadowsToAdviceApplications= new HashMap();
		tmNameToUniqueShadowIds = new HashMap();
		shadowIdToNumber = new HashMap();
		
		for (Iterator tmIter = gai.getTraceMatches().iterator(); tmIter.hasNext();) {
			TraceMatch tm = (TraceMatch) tmIter.next();
			tmNameToUniqueShadowIds.put(tm.getName(), new HashSet());			
		}
		
		//traverse all advice applications
		AdviceApplicationVisitor.v().traverse(
				new AdviceApplicationHandler() {

					public void adviceApplication(AdviceApplication aa,SootMethod context) {

						//if we have a tracematch advice
						if(aa.advice instanceof TMOptPerSymbolAdviceDecl) {
							
							//count the shadow
							TMOptPerSymbolAdviceDecl decl = (TMOptPerSymbolAdviceDecl) aa.advice;

							//get the tracematch for that advice application
							String traceMatchID = decl.getTraceMatchID();

							String qualifiedShadowId = Naming.uniqueShadowID(traceMatchID, decl.getSymbolId(),aa.shadowmatch.shadowId).intern();
							Object old = allShadowsToAdviceApplications.put(qualifiedShadowId, aa);
							assert old==null; //IDs should be unique
							
							Set shadowIds = (Set) tmNameToUniqueShadowIds.get(traceMatchID);
							boolean added = shadowIds.add(qualifiedShadowId);
							assert added; //IDs should be unique
						}
						
					}

				}
		);
		
		//right now, all shadows are enabled, none are disabled, none to be retained
		enabledShadows = new HashSet(allShadowsToAdviceApplications.keySet());
		disabledShadows = new HashSet();
		shadowsToBeRetained = new HashSet();
		
		//instantly remove tracematches from the system which have no matches at all
		removeTracematchesWithNoRemainingShadows();
	}
	
	public Set traceMatchesWithMatchingShadows() {
		return new HashSet(tmNameToUniqueShadowIds.keySet());
	}
	
	public boolean hasMatchingShadows(TraceMatch tm) {
		return traceMatchesWithMatchingShadows().contains(tm.getName());
	}
	
	public Set allShadowIDsForTraceMatch(String traceMatchName) {
		if(!tmNameToUniqueShadowIds.containsKey(traceMatchName)) {
			return Collections.EMPTY_SET;
		} else {
			return new HashSet((Set) tmNameToUniqueShadowIds.get(traceMatchName));
		}
	}
		
	public void disableShadow(String uniqueShadowId) {
		
		//remove the tag from this registry
		assert !shadowsToBeRetained.contains(uniqueShadowId);
		assert allShadowsToAdviceApplications.containsKey(uniqueShadowId);
		AdviceApplication aa = (AdviceApplication) allShadowsToAdviceApplications.get(uniqueShadowId);
		aa.setResidue(NeverMatch.v());
		boolean removed = enabledShadows.remove(uniqueShadowId);
		assert removed;
		boolean added = disabledShadows.add(uniqueShadowId);
		assert added;
		
		if(Debug.v().tmShadowDump) {
			System.err.println("disabled shadow: "+uniqueShadowId);
		}

	}
	
	public void removeTracematchesWithNoRemainingShadows() {
		for (Iterator tmIter = gai.getTraceMatches().iterator(); tmIter.hasNext();) {
			TraceMatch tm = (TraceMatch) tmIter.next();
			
			//get all the enabled shadows for this TM
			Set thisTMsShadowIDs = new HashSet((Set) tmNameToUniqueShadowIds.get(tm.getName()));
			thisTMsShadowIDs.retainAll(enabledShadows());
			
			if(thisTMsShadowIDs.isEmpty()) {
				boolean removed = gai.removeTraceMatch(tm);
				assert removed;
				tmNameToUniqueShadowIds.remove(tm.getName());
			}
		}
	}
	
	public void retainShadow(String uniqueShadowId) {
		shadowsToBeRetained.add(uniqueShadowId);
		assert !disabledShadows.contains(uniqueShadowId);
	}
	
	public void disableAllInactiveShadows() {
		Set copyEnabled = new HashSet(enabledShadows); 
		for (Iterator enabledIter = copyEnabled.iterator(); enabledIter.hasNext();) {
			String uniqueShadowId = (String) enabledIter.next();
			if(!shadowsToBeRetained.contains(uniqueShadowId)) {
				disableShadow(uniqueShadowId);
			}
		}
		
		//validation code
		assert sanityCheck();
	}
	
	public Set enabledShadows() {
		return new HashSet(enabledShadows);
	}
	
	public boolean enabledShadowsLeft() {
		return !enabledShadows.isEmpty();
	}

	public Set allShadows() {
		return new HashSet(allShadowsToAdviceApplications.keySet());
	}
	
	public AdviceApplication getAdviceApplicationForShadow(String uniqueShadowId) {
		return (AdviceApplication) allShadowsToAdviceApplications.get(uniqueShadowId);
	}
	
	public void dumpShadows() {
		if(Debug.v().tmShadowDump) {
			System.err.println("===============================================================================");
			System.err.println("===============================================================================");
			System.err.println("DISABLED SHADOWS");
			System.err.println("===============================================================================");
			System.err.println("===============================================================================");
			for (Iterator entryIter = allShadowsToAdviceApplications.entrySet().iterator(); entryIter.hasNext();) {
				Entry entry = (Entry) entryIter.next();
				String shadowId = (String) entry.getKey();
				if(!enabledShadows.contains(shadowId)) {
					AdviceApplication aa = (AdviceApplication) entry.getValue();
					StringBuffer sb = new StringBuffer();
					aa.debugInfo("  ", sb);
					System.err.println("unique-shadow-id: "+shadowId);
					System.err.println("status: disabled");
					System.err.println("applied in method: "+aa.shadowmatch.getContainer());					
					System.err.println(sb.toString());
					System.err.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
				}
			}
			System.err.println("===============================================================================");
			System.err.println("===============================================================================");
			System.err.println("REMAINING SHADOWS");
			System.err.println("===============================================================================");
			System.err.println("===============================================================================");
			for (Iterator entryIter = allShadowsToAdviceApplications.entrySet().iterator(); entryIter.hasNext();) {
				Entry entry = (Entry) entryIter.next();
				String shadowId = (String) entry.getKey();
				
				if(enabledShadows.contains(shadowId)) {
					AdviceApplication aa = (AdviceApplication) entry.getValue();
					StringBuffer sb = new StringBuffer();
					aa.debugInfo("  ", sb);
					System.err.println("unique-shadow-id: "+shadowId);
					System.err.println("status: enabled");
					System.err.println("applied in method: "+aa.shadowmatch.getContainer());					
					System.err.println(sb.toString());
					System.err.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
				}
			}
		}
	}
	
	
	/**
	 * @return
	 */
	private boolean sanityCheck() {
		assert enabledShadows.containsAll(shadowsToBeRetained);
		
		Set enabled = new HashSet(enabledShadows);
		assert !enabled.removeAll(disabledShadows);
		
		Set all = new HashSet();
		all.addAll(enabledShadows);
		all.addAll(disabledShadows);
		assert all.equals(allShadowsToAdviceApplications.keySet());
		return true;
	}

	/**
	 * Conjoins the residues of all enabled shadows with a {@link DynamicInstrumentationResidue}.
	 */
	public void setDynamicResidues() {
		
		for (Iterator enabledIter = enabledShadows.iterator(); enabledIter.hasNext();) {
			String uniqueShadowId = (String) enabledIter.next();
			AdviceApplication aa = (AdviceApplication) allShadowsToAdviceApplications.get(uniqueShadowId);
			Residue originalResidue = aa.getResidue();
			aa.setResidue(
					AndResidue.construct(					
							new DynamicInstrumentationResidue(numberOf(uniqueShadowId)),
							originalResidue
					)
			);			
		}
		
	}
	
	/**
	 * Returns a unique int number for the given shadow id.
	 * The numbers start with 0 and then increase by 1.
	 * @param uniqueShadowId
	 * @return
	 */
	public int numberOf(String uniqueShadowId) {
		Integer number = (Integer) shadowIdToNumber.get(uniqueShadowId);
		if(number==null) {
			number = new Integer(shadowIdToNumber.size());
			shadowIdToNumber.put(uniqueShadowId, number);
		}
		return number.intValue();
	}
	
	/**
     * Returns <code>true</code> if the shadow with this unique id is still enabled.
	 */
	public boolean isEnabled(String uniqueShadowId) {
		assert allShadowsToAdviceApplications.keySet().contains(uniqueShadowId);
		return enabledShadows.contains(uniqueShadowId);
	}

	public Set allActiveShadowsForTag(SymbolShadowMatchTag tag, SootMethod container) {
		Set result = new HashSet();

		for (SymbolShadowMatch match : tag.getAllMatches()) {
			if(match.isEnabled()) {
				result.add(new Shadow(match,container));
			}
		}
		return result;
	}
	
	public Set<Shadow> allActiveShadowsForHost(Host h, SootMethod container) {
		if(h.hasTag(SymbolShadowMatchTag.NAME)) {
			SymbolShadowMatchTag tag = (SymbolShadowMatchTag) h.getTag(SymbolShadowMatchTag.NAME);
			return allActiveShadowsForTag(tag, container);
		} else {
			return Collections.EMPTY_SET; 
		}
	}

	public Set allActiveShadowsForHostAndTM(Host h, SootMethod container, TraceMatch tm) {
		Set result = new HashSet();
		Set allShadowsForHost = allActiveShadowsForHost(h, container);
		for (Iterator shadowIter = allShadowsForHost.iterator(); shadowIter.hasNext();) {
			Shadow shadow = (Shadow) shadowIter.next();
			String shadowId = shadow.getUniqueShadowId();
			String tracematchName = Naming.getTracematchName(shadowId);
			if(tracematchName.equals(tm.getName())) {
				result.add(shadow);
			}
		}
		return result;
	}
	
	//singleton pattern
	
	public static void initialize() {
		v();
	}

	protected static ShadowRegistry instance;
	
	public static ShadowRegistry v() {
		if(instance==null) {
			instance = new ShadowRegistry();
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
