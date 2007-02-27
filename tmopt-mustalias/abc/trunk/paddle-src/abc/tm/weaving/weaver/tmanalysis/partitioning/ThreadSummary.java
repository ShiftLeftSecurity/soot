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

package abc.tm.weaving.weaver.tmanalysis.partitioning;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import abc.tm.weaving.matching.SMEdge;

/**
 * CachedThreadSummary
 *
 * @author Eric Bodden
 */
public class ThreadSummary {

	protected final Set edges;

	public ThreadSummary(Set edges) {
		Set temp = new HashSet();
		SMEdge.setEqualsDespiteState(true);
		temp.addAll(edges);
		SMEdge.setEqualsDespiteState(false);		
		this.edges = Collections.unmodifiableSet(temp);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Set edgesMaybeTriggeredByThread() {
		return edges;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "ThreadSummary(" + edgesMaybeTriggeredByThread().toString()+")";
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((edges == null) ? 0 : edges.hashCode());
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ThreadSummary other = (ThreadSummary) obj;
		if (edges == null) {
			if (other.edges != null)
				return false;
		} else if (!edges.equals(other.edges))
			return false;
		return true;
	}	

}
