package abc.tm.weaving.weaver.tmanalysis.mustalias;

import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.InitKind;

/**
 * Generic interface for a flow-sensitive analysis for static tracematch optimizations.
 *
 * @author Eric Bodden
 */
public interface TMFlowAnalysis {
	
	/**
	 * @return returns the associated tracematch
	 */
	public TraceMatch getTracematch();

	/**
	 * @return the initializationKind
	 */
	public InitKind getInitializationKind();

}
