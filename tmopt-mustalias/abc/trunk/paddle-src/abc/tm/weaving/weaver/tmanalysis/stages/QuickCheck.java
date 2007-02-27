/*
 * Created on 11-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.stages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.util.IdentityHashSet;
import abc.main.AbcTimer;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TMOptTraceMatch;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.MatchingTMSymbolTag;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;

/**
 * This check determines if a tracematch in the system can ever match, solely on the number of advice applications
 * that occured during weaving.
 *
 * @author Eric Bodden
 */
public class QuickCheck extends AbstractAnalysisStage {

	/** the set of tracematches that were determined to be non-matching and can hence be disabled */
	protected transient Set traceMatchesToDisable;
	
	/**
	 * {@inheritDoc}
	 */
	public void doAnalysis() {
		traceMatchesToDisable = new IdentityHashSet();
		removeNonMatchingSymbols();
		pruneTagsAndDisableShadowsForRemovedTracematches();
		traceMatchesToDisable = null;

		AbcTimer.mark("TMAnalysis: remove non-matching symbols");
	}
	
	/**
	 * For all tracematches, checks if all their symbols actually applied anywhere.
	 * If not, edges with that symbol are removed and if by doing so the final states
	 * become unreachable, we remove the tracematch entirely.
	 */
	protected void removeNonMatchingSymbols() {	
		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) abc.main.Main.v().getAbcExtension().getGlobalAspectInfo();

		//make a copy of this list, because we alter it while iterating over it
		List traceMatches = new ArrayList(gai.getTraceMatches());
		//for all tracematches
		for (Iterator iter = traceMatches.iterator(); iter.hasNext();) {
			TMOptTraceMatch tm = (TMOptTraceMatch) iter.next();

			//if the tracematch has no matching shadows at all
			//or after removing the ones which don't match, we get an empty automaton...
			if(!ShadowRegistry.v().hasMatchingShadows(tm)  || tm.removeNonMatchingSymbols()) {
				//mark this tm as to be disabled
				traceMatchesToDisable.add(tm);
			}
		}
	}
	
	/**
	 * For all tracematches to remove, remove all information related to those tracematches from the
	 * {@link MatchingTMSymbolTag}s. Delete a tag entirely if it becomes empty.
	 * Also, remove all shadows of a disabled tracematch.
	 * The {@link ShadowRegistry} takes care of all of this, including removing the
	 * tracematch itself if there are no shadows left for it.
	 */
	protected void pruneTagsAndDisableShadowsForRemovedTracematches() {
		if(!traceMatchesToDisable.isEmpty()) {
			
			for (Iterator tmIter = traceMatchesToDisable.iterator(); tmIter.hasNext();) {
				TraceMatch tm = (TraceMatch) tmIter.next();
				
				//get all shadows for this tracematch
				Set shadowsForDisabledTm = ShadowRegistry.v().allShadowIDsForTraceMatch(tm.getName());
				//disable them all
				disableAll(shadowsForDisabledTm);
			}
		}
	}
	
	//singleton pattern

	protected static QuickCheck instance;

	private QuickCheck() {}
	
	public static QuickCheck v() {
		if(instance==null) {
			instance = new QuickCheck();
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
