package abc.tm.weaving.weaver.tmanalysis;

import java.util.HashSet;
import java.util.Set;

import soot.Kind;
import soot.jimple.toolkits.callgraph.Edge;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;

/**
 * SMThreadCallEdge
 *
 * @author Eric Bodden
 */
public class SMThreadCallEdge extends SMEdge {

	private final Edge threadStartEdge;
	private final boolean type;
	
	public final static boolean OUTGOING = true;
	public final static boolean RETURNING = false;
	
	private static Set startEdges = new HashSet();  

	/**
	 * @param from
	 * @param to
	 * @param l
	 */
	public SMThreadCallEdge(SMNode from, SMNode to, Edge threadstartEdge, boolean outGoing) {
		super(from, to, "SMThreadCallEdge<"+(outGoing?"outgoing":"returning")+","+threadstartEdge+">");
		this.threadStartEdge = threadstartEdge;
		this.type = outGoing;
		assert threadstartEdge.kind()==Kind.THREAD;
		startEdges.add(threadstartEdge);
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + (type ? 1231 : 1237);
		result = PRIME * result + ((threadStartEdge == null) ? 0 : threadStartEdge.hashCode());
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
		final SMThreadCallEdge other = (SMThreadCallEdge) obj;
		if (type != other.type)
			return false;
		if (threadStartEdge == null) {
			if (other.threadStartEdge != null)
				return false;
		} else if (!threadStartEdge.equals(other.threadStartEdge))
			return false;
		return true;
	}
	
	public static Set allThreadStartEdges() {
		return new HashSet(startEdges);
	}
	
	public boolean isOutgoing() {
		return type==OUTGOING;
	}

	/**
	 * @return the threadCallEdge
	 */
	public Edge getThreadStartEdge() {
		return threadStartEdge;
	}
	
	public static void reset() {
		startEdges = new HashSet();  
	}
	
}
