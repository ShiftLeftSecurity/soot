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
package abc.tm.weaving.weaver.tmanalysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.Local;
import soot.PointsToSet;
import soot.PrimType;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.pointer.FullObjectSet;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMEdgeFactory;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.weaver.tmanalysis.ShadowSMEdgeFactory.SMShadowEdge;
import abc.tm.weaving.weaver.tmanalysis.ds.PaddlePointsToSetCompatibilityWrapper;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.weaving.aspectinfo.Var;
import abc.weaving.residues.WeavingVar;

/**
 * This factory creates edges which can hold variable bindings. 
 * @author Eric Bodden
 */
public class VariableSMEdgeFactory implements SMEdgeFactory {

	private static SMEdgeFactory instance;
	
	private VariableSMEdgeFactory() {
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public SMEdge createTransition(State from, State to, String label) {
		return new SMVariableEdge((SMNode)from, (SMNode)to, label);
	}
	

	/** 
	 * {@inheritDoc}
	 */
	public SMEdge createSkipTransition(SMNode node, String label) {
		return new SkipVariableLoop(node,label);
	}
	
	/**
	 * Returns the singleton instance of this factory.
	 * @return a {@link VariableSMEdgeFactory}
	 */
	public static SMEdgeFactory v() {
		if(instance == null) {
			instance = new VariableSMEdgeFactory();
		}
		return instance;
	}


	/**
	 * A {@link SMEdge} that can hold free variable bindings.
	 * <b>Note that by purpose we do <i>not</i> implement equals
	 * and hashcode for this (based on those bindings) because
	 * the bindings can change over time.</b>
	 * @author Eric Bodden
	 */
	public class SMVariableEdge extends SMShadowEdge {

		protected Shadow shadow;
		
		//FIXME it would be more consistent if this edge
		//would hold an instance of Shadow
		
		/**
		 * @param from
		 * @param to
		 * @param symbolName
		 */
		public SMVariableEdge(SMNode from, SMNode to, String symbolName) {
			super(from, to, symbolName);
		}
		
		/**
		 * Tells whether this edge has a variable mapping bound. 
		 * @return <code>true</code> if a variable mapping is bound to this
		 * edge
		 */
		public boolean hasVariableMapping() {
			return shadow!=null && shadow.hasVariableMapping();
		}

//		/**
//		 * TODO comment properly
//		 * Sets the free variable mapping for this edge. 
//		 * This may only be called if {@link #hasVariableMapping()} returns false.
//		 * @param mapping a map of type &lt;Var,&lt;Set&lt;WeavingVar&gt;&gt;&gt; 
//		 * @param method the method this edge was created for
//		 * @throws EmptyVariableMappingException thrown when there is an empty points-to set for one of
//		 * the weaving variables. This means most possibly that it would be null during runtime.  
//		 */
//		public void importVariableMapping(Map mapping, SootMethod method) throws EmptyVariableMappingException {
//			if(varToPointsToSet!=null) {
//				throw new RuntimeException("Mapping set already!");
//			}
//			varToPointsToSet = new HashMap();
//			//for each WeavingVar, get its local l and for that l get and store
//			//the points-to set
//			for (Iterator iter = mapping.entrySet().iterator(); iter.hasNext();) {
//				Entry entry = (Entry) iter.next();
//				Collection weavingVars = (Collection) entry.getValue();
//
//				assert weavingVars.size()<=1;
//
//				if(weavingVars.size()>0) {
//					WeavingVar wv = (WeavingVar) weavingVars.iterator().next();
//					Local l = wv.get();
//					
//					PointsToSet pts;
//					if(l.getType() instanceof PrimType) {
//						//if the type of the variable is a primitive type, then we assume it could "point to anything", i.e. could have any value
//						pts = FullObjectSet.v();
//					} else {
//						//if l is null this probably means that the WeavingVar was never woven
//						assert l!=null;
//						PointsToSet paddlePts = (PointsToSet) Scene.v().getPointsToAnalysis().reachingObjects(l);
//						if(paddlePts.isEmpty()) {
//							//abc.main.Main.v().error_queue.enqueue(ErrorInfo.WARNING, "Empty points-to set for variable "+l+" in "+method);
//							throw new EmptyVariableMappingException(wv.toString(),method);
//							/*  FOR CAUSES OF  POSSIBLE CAUSES FOR EMPTY VARIABLE MAPPINGS see class Shadow */
//						}		
//						
//						pts = new PaddlePointsToSetCompatibilityWrapper(paddlePts);
//					}
//					varToPointsToSet.put(((Var)entry.getKey()).getName(),pts);
//				}
//			}
//		}
		
		/**
		 * TODO comment
		 * @param v
		 * @return
		 */
		public PointsToSet getPointsToSet(String v) {
			return shadow.getPointsToSet(v);
		}
		
		public Set getBoundVariables() {
			return shadow.getBoundVariables();
		}
		
		public boolean hasShadow() {
			return shadow!=null;
		}

		/**
		 * @return
		 */
		public Map getVariableMapping() {
			return shadow.getVariableMapping();
		}
		
		/**
		 * {@inheritDoc}
		 */
		public String toString() {
			return super.toString() + shadow;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result
					+ ((shadow == null) ? 0 : shadow.hashCode());
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			final SMVariableEdge other = (SMVariableEdge) obj;
			if (shadow == null) {
				if (other.shadow != null)
					return false;
			} else if (!shadow.equals(other.shadow))
				return false;
			return true;
		}

		/**
		 * @param shadow the shadow to set
		 */
		public void setShadow(Shadow shadow) {
			if(this.shadow!=null) {
				throw new IllegalStateException("shadow already set");
			}			
			this.shadow = shadow;
		}

		/**
		 * @return
		 */
		public Shadow getShadow() {
			return shadow;
		}
		
//		/**
//		 * TODO document
//		 * @param other
//		 * @return
//		 */
//		public SMVariableEdge intersectWith(SMVariableEdge other) {
//			if(equals(other)) {
//				return this;
//			}
//			if(!other.getLabel().equals(getLabel())) {
//				throw new RuntimeException("Edges have different labels.");
//			}
//			if(varToPointsToSet==null) {
//				//if this mapping is null, the intersection should also be empty;
//				//so just return this
//				return this;
//			} else {
//				SMVariableEdge copy = new SMVariableEdge(getSource(),getTarget(),getLabel());
//				HashMap intersectedMap = new HashMap();
//				for (Iterator iter = varToPointsToSet.keySet().iterator(); iter.hasNext();) {
//					Var v = (Var) iter.next();
//					PointsToSet p1 = (PointsToSet) this.varToPointsToSet.get(v);
//					PointsToSet p2 = (PointsToSet) other.varToPointsToSet.get(v);
//					PointsToSet inter = Intersection.intersect(p1, p2);
//					if(!inter.isEmpty()) {
//						intersectedMap.put(v,inter);
//					}
//				}
//				copy.varToPointsToSet = intersectedMap;
//				return copy;
//			}
//		}
//
	}
	
	/**
	 * A skip loop which can hold free variable bindings.
	 * @author Eric Bodden
	 */
	public class SkipVariableLoop extends SMVariableEdge {

		/**
		 * @param state the state to loop over
		 * @param symbolName the label of the symbol to be skipped
		 */
		public SkipVariableLoop(SMNode state, String symbolName) {
			super(state, state, symbolName);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		public boolean isSkipEdge() {
			return true;
		}		
		
		/**
		 * {@inheritDoc}
		 */
		public String toString() {
			return "skip-" + super.toString();
		}
		
	}	
	

}