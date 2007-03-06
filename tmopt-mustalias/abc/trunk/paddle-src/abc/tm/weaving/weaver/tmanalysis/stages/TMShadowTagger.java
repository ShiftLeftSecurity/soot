/*
 * Created on 5-Mar-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.stages;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
import soot.toolkits.graph.BriefUnitGraph;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolFinder;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolFinder.SymbolShadowMatch;
import abc.weaving.aspectinfo.AbcClass;

/**
 * TMShadowTagger
 *
 * @author Eric Bodden
 */
public class TMShadowTagger extends BodyTransformer implements Stage {

	protected Map<SootMethod,TraceMatch> syncMethodToTraceMatch;
	
	public static class SymbolShadowMatchTag implements Tag {
		
		public final static String NAME = SymbolShadowMatchTag.class.getName();

		private final Map<TraceMatch, Set<SymbolShadowMatch>> tmToMatches;		

		private SymbolShadowMatchTag(Map<TraceMatch, Set<SymbolShadowMatch>> matches) {
			this.tmToMatches = matches;
		}

		public String getName() {
			return NAME;
		}

		public byte[] getValue() throws AttributeValueException {
			throw new UnsupportedOperationException();
		}
		
		public Set<SymbolShadowMatch> getMatchesForTracematch(TraceMatch tm) {
			Set<SymbolShadowMatch> symbolShadowMatch = tmToMatches.get(tm); 
			if(symbolShadowMatch==null) {
				return Collections.emptySet();
			} else {
				return symbolShadowMatch;
			}
		}
		
		public Set<SymbolShadowMatch> getAllMatches() {
			Set<SymbolShadowMatch> res = new HashSet<SymbolShadowMatch>();
			for (Set<SymbolShadowMatch> matches : tmToMatches.values()) {
				res.addAll(matches);
			}
			return res;
		}
		
		
	}
	
	protected void internalTransform(Body b, String phaseName, Map options) {

		SymbolFinder symbolFinder = new SymbolFinder(new BriefUnitGraph(b));
		
		for (Stmt call : symbolFinder.getSomeAdviceMethodCalls()) {
			Map<TraceMatch,Set<SymbolShadowMatch>> matches = symbolFinder.getSymbolsAtSomeAdviceMethodCall(call);
			call.addTag(new SymbolShadowMatchTag(matches));
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void apply() {
		
		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
		
		syncMethodToTraceMatch = new HashMap<SootMethod,TraceMatch>();
		
		for (TraceMatch tm : (Collection<TraceMatch>) gai.getTraceMatches()) {
			SootMethod syncMethod = tm.getSynchAdviceMethod();
			syncMethodToTraceMatch.put(syncMethod, tm);
		}		
		
		for (AbcClass abcClass : (Set<AbcClass>)gai.getWeavableClasses()) {
			SootClass sootClass = abcClass.getSootClass();
			for (SootMethod method : (List<SootMethod>)sootClass.getMethods()) {
				if(method.hasActiveBody()) {
					transform(method.getActiveBody());
				}				
			}
		}
	}
	
	//singleton pattern
	
	protected static TMShadowTagger instance;

	private TMShadowTagger() {}
	
	public static TMShadowTagger v() {
		if(instance==null) {
			instance = new TMShadowTagger();
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
