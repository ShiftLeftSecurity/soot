package abc.tm.weaving.weaver.tmanalysis;

import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.weaver.tmanalysis.partitioning.ThreadSummary;

/**
 * This is a statemachine edge which marks that a thread
 * was spawned (by calling {@link Thread#start()}).
 * It has a {@link ThreadSummary} attached which gives
 * analysis summary information for this thread.
 *
 * @author Eric Bodden
 */
public class SMThreadSpawnEdge extends SMEdge {

	protected ThreadSummary ts;

	/**
	 * @param from the source node
	 * @param to the target node
	 * @param ts the thread summary to attach
	 */
	public SMThreadSpawnEdge(SMNode from, SMNode to, ThreadSummary ts) {
		super(from, to, "threadSummary");
		this.ts = ts;
	}

	/**
	 * @return the attached thread summary
	 */
	public ThreadSummary getThreadSummary() {
		return ts;
	}
	
	/**
	 * Returns hash code based on the associated thread summary only.
	 */
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + ((ts == null) ? 0 : ts.hashCode());
		return result;
	}

	/**
	 * The edges are equal if the associated thread summaries are the same.
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SMThreadSpawnEdge other = (SMThreadSpawnEdge) obj;
		if (ts == null) {
			if (other.ts != null)
				return false;
		} else if (!ts.equals(other.ts))
			return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return ts.toString();
	}

}