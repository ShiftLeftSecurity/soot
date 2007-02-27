package abc.tm.weaving.weaver.tmanalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;

import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.StronglyConnectedComponents;
import soot.util.IdentityHashSet;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.query.Naming;

/**
 * Computes the labels of all edges that dominate a final state in the state machine.
 * This means that all those events must occur in some order to lead to a match.
 * 
 * @author Eric Bodden
 */
public class DominatorEdgeLabels {

	protected transient Set allPaths;
	
	protected Set setsOfDominatingEdges;
	
	/**
	 * Constructs a new analysis for the given state machine.
	 * @param sm a tracematch state machine.
	 */
	public DominatorEdgeLabels(TMStateMachine sm) {
		setsOfDominatingEdges = new HashSet();
		
		//get a minimized copy
		sm = sm.getMinimized(true);
		DirectedGraph graph = new TMStateMachineAsGraph(sm);
		StronglyConnectedComponents scc = new StronglyConnectedComponents(graph);
		Set edgesToRemove = new IdentityHashSet();
		for (Iterator compIter = scc.getComponents().iterator(); compIter.hasNext();) {
			Collection comp = (Collection) compIter.next();
			if(comp.size()==1) {
				SMEdge edge = (SMEdge) comp.iterator().next();
				if(edge.getLabel()!=null) {
					if(edge.getSource()==edge.getTarget() && !edge.isSkipEdge()) {
						edgesToRemove.add(edge);
					}
				}
			} else {
				SMNode firstNode = ((SMEdge)comp.iterator().next()).getSource();
				boolean hasMultipleNodes = false;
				for (Iterator edgeIter = comp.iterator(); edgeIter.hasNext();) {
					SMEdge edge = (SMEdge) edgeIter.next();
					if(edge.getSource()!=firstNode || edge.getTarget()!=firstNode) {
						hasMultipleNodes = true;
					}
				}
				if(hasMultipleNodes) {
					for (Iterator edgeIter = comp.iterator(); edgeIter.hasNext();) {
						SMEdge edge = (SMEdge) edgeIter.next();
						if(edge.getLabel()!=null) {
							edgesToRemove.add(edge);
						}
					}
				} else {
					for (Iterator edgeIter = comp.iterator(); edgeIter.hasNext();) {
						SMEdge edge = (SMEdge) edgeIter.next();
						if(!edge.isSkipEdge()) {
							edgesToRemove.add(edge);
						}
					}
				}
			}
		}
		sm.removeEdges(edgesToRemove.iterator());
		//sm is now a DAG
		
		sm.cleanup();
		//sm.minimizeIfSmaller();
		
		//find all paths in SM
		allPaths = new HashSet();		
		for (Iterator nodeITer = sm.getStateIterator(); nodeITer.hasNext();) {
			SMNode node = (SMNode) nodeITer.next();
			if(node.isInitialNode()) {
				recurse(node,new ArrayList());
			}
		}
		
		for (Iterator pathIter = allPaths.iterator(); pathIter.hasNext();) {
			Collection path = (Collection) pathIter.next();
			Bag labSet = new HashBag();
			Set skipLoopLabelSet = new HashSet();
			for (Iterator edgeIter = path.iterator(); edgeIter.hasNext();) {
				SMEdge edge = (SMEdge) edgeIter.next();
				String lab = Naming.getSymbolShortName(edge.getLabel());
				labSet.add(lab);
				SMNode tgt = edge.getTarget();
				for (Iterator outIter = tgt.getOutEdgeIterator(); outIter.hasNext();) {
					SMEdge outEdge = (SMEdge) outIter.next();
					if(outEdge.isSkipEdge()) {
						skipLoopLabelSet.add(outEdge.getLabel());
					}
				}
			}
			skipLoopLabelSet = Collections.unmodifiableSet(skipLoopLabelSet);			
			setsOfDominatingEdges.add(new PathInfo(labSet,skipLoopLabelSet));
		}

		setsOfDominatingEdges = Collections.unmodifiableSet(setsOfDominatingEdges);
		
		allPaths = null;
	}

	/**
	 * @param node 
	 * @param path
	 */
	private void recurse(SMNode node, List path) {
		boolean reachedEnd = true;
		for (Iterator outIter = node.getOutEdgeIterator(); outIter.hasNext();) {
			reachedEnd = false;
			SMEdge edge = (SMEdge) outIter.next();
			if(!edge.isSkipEdge()) {
				SMNode next = edge.getTarget();
				List subpath = new ArrayList(path);
				subpath.add(edge);
				recurse(next,subpath);
			}
		}
		if(reachedEnd) {
			allPaths.add(path);
		}
	}

	/**
	 * @return the labels of all edges dominating a final state
	 */
	public Set getPathInfos() {
		return setsOfDominatingEdges;
	}
	
	public static class PathInfo {
		
		protected Bag dominatingLabels;
		
		protected Set skipLoopLabels;

		public PathInfo(Bag dominatingLabels, Set skipLoopLabels) {
			this.dominatingLabels = dominatingLabels;
			this.skipLoopLabels = skipLoopLabels;
		}

		/**
		 * @return the dominatingLabels
		 */
		public Bag getDominatingLabels() {
			return new HashBag(dominatingLabels);
		}

		/**
		 * @return the skipLoops
		 */
		public Set getSkipLoopLabels() {
			return new HashSet(skipLoopLabels);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public String toString() {
			return "<dom-labels="+dominatingLabels+",skip-labels="+skipLoopLabels+">";
		}

		/**
		 * {@inheritDoc}
		 */
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((dominatingLabels == null) ? 0 : dominatingLabels
							.hashCode());
			result = prime
					* result
					+ ((skipLoopLabels == null) ? 0 : skipLoopLabels.hashCode());
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
			final PathInfo other = (PathInfo) obj;
			if (dominatingLabels == null) {
				if (other.dominatingLabels != null)
					return false;
			} else if (!dominatingLabels.equals(other.dominatingLabels))
				return false;
			if (skipLoopLabels == null) {
				if (other.skipLoopLabels != null)
					return false;
			} else if (!skipLoopLabels.equals(other.skipLoopLabels))
				return false;
			return true;
		}
		
		
		
	}

}
 