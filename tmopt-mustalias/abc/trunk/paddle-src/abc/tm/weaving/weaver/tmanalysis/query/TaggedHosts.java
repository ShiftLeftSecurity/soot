/*
 * Created on 11-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.tagkit.Host;
import abc.main.Main;
import abc.tm.weaving.weaver.tmanalysis.MatchingTMSymbolTag;
import abc.tm.weaving.weaver.tmanalysis.stages.Stage;
import abc.tm.weaving.weaver.tmanalysis.stages.UnwovenShadowTagRemover;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.aspectinfo.AfterReturningArgAdvice;
import abc.weaving.aspectinfo.Formal;
import abc.weaving.aspectinfo.Var;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.BodyShadowMatch;
import abc.weaving.matching.StmtShadowMatch;
import abc.weaving.residues.WeavingVar;
import abc.weaving.weaver.Weaver;


/**
 * This class holds all hosts that were tagged with a {@link MatchingTMSymbolTag} and provides different mappings between hosts,
 * tags and containers of hosts. The aim is to provide a means to efficiently remove tags from hosts for subsequent analysis stages. 
 * This class must be informed when the initial tagging is finished and unwoven tags have been removed (see {@link UnwovenShadowTagRemover}),
 * by calling {@link #taggingCompleted()}.
 * 
 * This class also rebinds hosts which are units (see {@link Weaver#reverseRebind(Unit)}) in order to get a consistent version of the them.
 * 
 * @author Eric Bodden
 */
public class TaggedHosts {
	
	/** shortcut to the weaver */
	protected final static Weaver weaver = Main.v().getAbcExtension().getWeaver();

	/** the set of all {@link Host}s currently tagged  */
	protected Set taggedHosts = new HashSet();
	
	/** a mapping from a {@link Host}s to its container (a {@link SootMethod}) */
	protected Map hostToContainer = new HashMap();
	
	/** a mapping from a unique shadow id ({@link String}) to all {@link Host}s referring to this shadow */
	protected Map uniqueShadowIdToHosts = new HashMap();
	
	/** internal state variable to keep track of allowed operations */
	protected boolean registrationAllowed = true;

	/**
	 * Register a host as tagged with a {@link MatchingTMSymbolTag}, storing a reference to its container.
	 * The container is the {@link SootMethod} containing h or h itself if h is a {@link SootMethod}.
	 * @param h some host containing a {@link MatchingTMSymbolTag}
	 * @param container the container of h
	 * @throws IllegalStateException thrown if this method is called after {@link #taggingCompleted()} was called
	 */
	public void registerHostAsTagged(Host h, SootMethod container) {
		h = rebind(h);
		if(!registrationAllowed) {
			throw new IllegalStateException("Registration not allowed at this stage.");
		}
		assert h.hasTag(MatchingTMSymbolTag.NAME);
		taggedHosts.add(h);
		hostToContainer.put(h,container);		
	}

	/**
	 * This method must be called after applying {@link UnwovenShadowTagRemover} and before
	 * applying any other {@link Stage}.
	 * It builds up internal mappings between hosts and shadow IDs. It is hence mandatory
	 * that that no tags are added after calling this method.
	 */
	public void taggingCompleted() {
		registrationAllowed = false;
		
		for (Iterator hostIter = taggedHosts.iterator(); hostIter.hasNext();) {
			Host h = (Host) hostIter.next();
			
			MatchingTMSymbolTag tag = getTag(h);
			
			Set matchingUniqueShadowIDs = tag.getMatchingUniqueShadowIDs();
			assert !matchingUniqueShadowIDs.isEmpty();
			for (Iterator shadowIdIter = matchingUniqueShadowIDs.iterator(); shadowIdIter.hasNext();) {
				String uniqueShadowId = (String) shadowIdIter.next();
				
				Set hostsTaggedWithThisShadow = (Set) uniqueShadowIdToHosts.get(uniqueShadowId);			
				if(hostsTaggedWithThisShadow==null) {
					hostsTaggedWithThisShadow = new HashSet();
					uniqueShadowIdToHosts.put(uniqueShadowId, hostsTaggedWithThisShadow);
				}
				
				hostsTaggedWithThisShadow.add(h);
			}
		}
	}
	
	/**
	 * Returns true if {@link #taggingCompleted()} was called before.
	 * @return true if {@link #taggingCompleted()} was called before
	 */
	public boolean isTaggingCompleted() {
		return !registrationAllowed;
	}
	
	/**
	 * Returns a (snapshot) iterator over all tagged hosts.
	 */
	public Iterator taggedHosts() {
		return new HashSet(taggedHosts).iterator();
	}
	
	/**
	 * Retrieves the {@link MatchingTMSymbolTag} from h.
	 * The client must make sure that h has such a tag!
	 */
	public MatchingTMSymbolTag getTag(Host h) {
		h = rebind(h);
		assert taggedHosts.contains(h);
		assert h.hasTag(MatchingTMSymbolTag.NAME);		
		return (MatchingTMSymbolTag) h.getTag(MatchingTMSymbolTag.NAME);		
	}
	
	/**
	 * Removes a tag soundly. {@link MatchingTMSymbolTag}s should only be removed from a host using this method!
	 * @param h any host tagged with a {@link MatchingTMSymbolTag} or whose rebound version does
	 */
	public void removeTag(Host h) {
		h = rebind(h);
		assert taggedHosts.contains(h);
		assert h.hasTag(MatchingTMSymbolTag.NAME);		
		h.removeTag(MatchingTMSymbolTag.NAME);
		taggedHosts.remove(h);
	}
	
	/**
	 * Returns true if h (or the rebound version of it) has a {@link MatchingTMSymbolTag}.
	 * @param h any host
	 * @return true if h (or the rebound version of it) has a {@link MatchingTMSymbolTag}
	 */
	public boolean hasTag(Host h) {
		h = rebind(h);
		assert taggedHosts.contains(h) == h.hasTag(MatchingTMSymbolTag.NAME);
		return taggedHosts.contains(h);
	}
	
	/**
	 * Returns all shadows attached to (the rebound version of) h. 
	 * @param h any host
	 * @return the set of all shadows attached to (the rebound version of) h or the empty set if there are none
	 */
	public Set getShadowsOf(Host h) {
		h = rebind(h);
		assert hasTag(h);
		return Shadow.allShadowsForTag(getTag(h),(SootMethod) hostToContainer.get(h));
	}
	
	/**
	 * Removes the given shadow from all tags that refer to it.
	 * @param uniqueShadowId a unique shadow ID
	 * @see Naming#uniqueShadowID(String, int)
	 * @see Naming#uniqueShadowID(String, String, int)
	 */
	public void removeShadowFromTags(String uniqueShadowId) {
		assert !ShadowRegistry.v().enabledShadows().contains(uniqueShadowId);
		
		Set hosts = (Set) uniqueShadowIdToHosts.get(uniqueShadowId);
		if(hosts==null) {
			throw new IllegalArgumentException("No hosts registered for shadow "+uniqueShadowId+"!");
		}
		
		for (Iterator hostIter = hosts.iterator(); hostIter.hasNext();) {
			Host h = (Host) hostIter.next();
			
			MatchingTMSymbolTag tag = getTag(h);
			if(tag.removeMappingsForShadow(uniqueShadowId)) {
				TaggedHosts.v().removeTag(h);
			}
		}
		
	}
	
	/**
	 * If h is a {@link Unit}, rebinds h to the equivalent version right before the first weaving (if there is such a version). 
	 * @param h any host
	 * @return the rebound version of h or h itself, if there is no rebound version
	 */
	protected Host rebind(Host h) {
		if(h instanceof Unit) {
			Unit u = (Unit) h;
			h = weaver.reverseRebind(u);
		}
		return h;
	}
	
	/**
	 * Computes the set of all {@link Local}s in all {@link MatchingTMSymbolTag}s which should be treated
	 * context-sensitively by the points-to analysis. At the moment those are all locals for all return-variables
	 * of after-returning advice which refer to a method (but not a constructor) call or execution.
	 * (e.g. <code>after returning(e): execution(A makeA())</code> but not <code>after returning(e): call(A.new())</code>)
	 */
	public Set findVariablesNeedingContextSensitivity() {
		Set enabledShadows = ShadowRegistry.v().enabledShadows();
		
		HashSet result = new HashSet();
		
		//for all shadow-to-hosts mappings
		for (Iterator entryIter = uniqueShadowIdToHosts.entrySet().iterator(); entryIter.hasNext();) {
			Entry shadowIdToHost = (Entry) entryIter.next();
			String uniqueShadowId = (String) shadowIdToHost.getKey();
			
			//if the shadow is still active
			if(enabledShadows.contains(uniqueShadowId)) {
				Set hosts = (Set) shadowIdToHost.getValue();
				
				//for all hosts of that shadow
				for (Iterator hostIter = hosts.iterator(); hostIter.hasNext();) {
					Host h = (Host) hostIter.next();
					
					//if the host is still tagged
					if(hasTag(h)) {
						//get its tag
						MatchingTMSymbolTag tag = getTag(h);
						
						//get the advice application for the shadow
						AdviceApplication aa = ShadowRegistry.v().getAdviceApplicationForShadow(uniqueShadowId);

						AdviceSpec adviceSpec = aa.advice.getAdviceSpec();
						if(adviceSpec instanceof AfterReturningArgAdvice) {
							//we do have an after-returning advice

							//see if this advice references a method (which is not a constructor)
							SootMethod nonConstructorMethod = null;
							if(aa.shadowmatch instanceof StmtShadowMatch) {
								//we have a statement shadow match, so this could be call, set, get, ...
								StmtShadowMatch stmtShadowMatch = (StmtShadowMatch) aa.shadowmatch;
								Stmt stmt = stmtShadowMatch.getStmt();
								if(stmt.containsInvokeExpr()) {
									//if we have an invoke expression; so this means we have an after-returning-call shadow
									
									InvokeExpr invokeExpr = stmt.getInvokeExpr();
									if(!invokeExpr.getMethod().getName().equals("<init>")) {
										//if do *not* have a call to a constructor
										nonConstructorMethod = invokeExpr.getMethod(); 
									}
								}
							} else if(aa.shadowmatch instanceof BodyShadowMatch) {
								//we have a body shadow match
								BodyShadowMatch bodyShadowMatch = (BodyShadowMatch) aa.shadowmatch;
								if(!bodyShadowMatch.getContainer().getName().equals("<init>")) {
									//if do *not* have a constructor body
									nonConstructorMethod = bodyShadowMatch.getContainer(); 
								}
							} else {
								throw new IllegalStateException("unexcpected shadow match type: "+aa.shadowmatch.getClass());
							}
							
							if(nonConstructorMethod!=null) {
								//this is an after-returning advice shadow which returns from a non-constructor call or
								//execution, initialization, etc. ...
								
								//get the return-variable name of the after-returning advice
								AfterReturningArgAdvice afterReturningArgAdvice = (AfterReturningArgAdvice) adviceSpec;
								Formal boundFormal = afterReturningArgAdvice.getFormal();
								String variableName = boundFormal.getName();
								
								//convert the shadow id to a symbol id 
								String symbolId = Naming.uniqueSymbolID(Naming.getTracematchName(uniqueShadowId), Naming.getSymbolShortName(uniqueShadowId));
								
								//find the right Var for the return variable name
								Map varToWeavingVars = tag.getVariableMappingForSymbol(symbolId);
								Var variable = null;
								for (Iterator varIter = varToWeavingVars.keySet().iterator(); varIter.hasNext();) {
									Var v = (Var) varIter.next();
									if(v.getName().equals(variableName)) {
										variable = v;
										break;
									}
								}							
								assert variable!=null;
								
								//get all WeavingVars for that Var
								Collection wvs = (Collection) varToWeavingVars.get(variable);
								assert wvs!=null;
								
								//for all those weaving vars
								for (Iterator wvIter = wvs.iterator(); wvIter.hasNext();) {
									WeavingVar wv = (WeavingVar) wvIter.next();
									assert wv!=null;
									
									Local local = wv.get();
									assert local!=null;
									
									//add the associated local to the reasult set
									result.add(local);
								}
							}
						}
					}
				}
			}
		}
		
		return result;
	}
	
	
	//singleton pattern
	
	protected static TaggedHosts instance;
	
	private TaggedHosts() {}
	
	public static TaggedHosts v() {
		if(instance==null) {
			instance = new TaggedHosts();
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
