/*
 * Created on 12-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.stages;

import java.util.Iterator;
import java.util.Set;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.tagkit.Host;
import abc.main.Main;
import abc.tm.weaving.weaver.tmanalysis.MatchingTMSymbolTag;
import abc.tm.weaving.weaver.tmanalysis.query.TaggedHosts;
import abc.tm.weaving.weaver.tmanalysis.query.WeavableMethods;
import abc.weaving.residues.WeavingVar;
import abc.weaving.weaver.Weaver;

/**
 * For the static tracematch optimization, we initially create tags in {@link VariableTrackingAdviceFormals}.
 * This class in invoked on <i>any</i> pointcut evaluation, even on ones which never actually lead to a match and
 * hence are never woven.
 * When this class is applied, it removes all bindings from tags which refer to {@link WeavingVar}s that have no associated 
 * {@link Local} (which is an indication for the fact that this {@link WeavingVar} was never woven).
 * A tag is removed entirely, if it becomes empty due to the removal process. 
 * 
 * Also, this class registers all tags that are remaining to {@link TaggedHosts}. 
 * 
 * @author Eric Bodden
 */
public class UnwovenShadowTagRemover implements Stage {

	protected static final Weaver weaver = Main.v().getAbcExtension().getWeaver();

	/**
	 * {@inheritDoc}
	 */
	public void apply() {
		Set weavableMethods = WeavableMethods.v().getAll();
		
        for (Iterator methodIter = weavableMethods.iterator(); methodIter.hasNext();) {
			SootMethod method = (SootMethod) methodIter.next();
			removeTagsForUnwovenShadows(method);
		}
        
    	//notify the tag manager that tagging was completed; now it can do some internal cleanup and caching
    	TaggedHosts.v().taggingCompleted();
	}

	
	/**
	 * Removes all tags within the given method for shadows which were not actually woven.
	 * @param method the method to remove the tags for
	 */
	protected void removeTagsForUnwovenShadows(SootMethod method) {
		removeTagIfNotWoven(method, method);
		
		for (Iterator unitIter = method.getActiveBody().getUnits().iterator(); unitIter.hasNext();) {
			Unit u = (Unit) unitIter.next();
			removeTagIfNotWoven(u, method);
		}
	}
	

	/**
	 * If the host has a {@link MatchingTMSymbolTag} for shadows of which none was woven,
	 * this methods removed the tag.
	 * @param h any host
	 * @param container the method containing the host
	 */
	protected void removeTagIfNotWoven(Host h, SootMethod container) {
		//we have to rebind here so that we remove the tag from the sam eunig which TaggedHosts would be considering
		if(h instanceof Unit) {
			h = weaver.reverseRebind((Unit)h);				
		}	
		if(h.hasTag(MatchingTMSymbolTag.NAME)) {
			MatchingTMSymbolTag tag = (MatchingTMSymbolTag) h.getTag(MatchingTMSymbolTag.NAME);
			tag.removeUnwovenMappings();
			if(tag.getMatchingSymbolIDs().size()==0) {
				//if the tag became empty, remove it
				h.removeTag(MatchingTMSymbolTag.NAME);
			} else {
				//otherwise register it 
				TaggedHosts.v().registerHostAsTagged(h, container);
			}
		}
	}

	//singleton pattern
	
	protected static UnwovenShadowTagRemover instance;
	
	private UnwovenShadowTagRemover() {}
	
	public static UnwovenShadowTagRemover v() {
		if(instance==null) {
			instance = new UnwovenShadowTagRemover();
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
