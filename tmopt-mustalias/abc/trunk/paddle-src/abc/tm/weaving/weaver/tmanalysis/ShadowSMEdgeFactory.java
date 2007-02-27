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

import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMEdgeFactory;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.State;

/**
 * This factory creates edges which can hold a shadow id. 
 * @author Eric Bodden
 */
public class ShadowSMEdgeFactory implements SMEdgeFactory {

	private static SMEdgeFactory instance;
	
	private ShadowSMEdgeFactory() {
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public SMEdge createTransition(State from, State to, String label) {
		return new SMShadowEdge((SMNode)from, (SMNode)to, label);
	}
	

	/** 
	 * {@inheritDoc}
	 */
	public SMEdge createSkipTransition(SMNode node, String label) {
		return new SkipShadowLoop(node,label);
	}
	
	/**
	 * Returns the singleton instance of this factory.
	 * @return a {@link ShadowSMEdgeFactory}
	 */
	public static SMEdgeFactory v() {
		if(instance == null) {
			instance = new ShadowSMEdgeFactory();
		}
		return instance;
	}


	/**
	 * A {@link SMEdge} that can hold a shadow id.
	 * @author Eric Bodden
	 */
	public static class SMShadowEdge extends SMEdge {

		protected int id;
		
		protected final int UNSET = -1;
		
		protected String qsid;
		
		/**
		 * @param from
		 * @param to
		 * @param symbolName
		 */
		public SMShadowEdge(SMNode from, SMNode to, String symbolName) {
			super(from, to, symbolName);
			id = UNSET;
		}
		
		/**
		 * @param shadowId
		 */
		public void setShadowId(int shadowId) {
			if(id!=UNSET && id!=shadowId) {
				throw new RuntimeException("Shadow id already set!");				
			}
			if(shadowId<0) {
				throw new RuntimeException("Shadow id should be >=0!");
			}
			id = shadowId;
			qsid = (getLabel()+"@"+id).intern();
		}
		
		/**
		 * @return the shadow id, if set
		 */
		public String getQualifiedShadowId() {
			if(qsid==null) {
				throw new RuntimeException("Shadow id not yet set!");				
			}
			return qsid;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public String toString() {
			return super.toString() + (id==UNSET?"":"(shadow-id:"+id+")");
		}

		/**
		 * Returns hash code based on the associated label and shadow id.
		 */
		public int hashCode() {
			final int PRIME = 31;
			int result = super.hashCode();
			result = PRIME * result + id;
			return result;
		}

		/**
		 * Equality based on the on the associated label and shadow id.
		 */
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			final SMShadowEdge other = (SMShadowEdge) obj;
			if (id != other.id)
				return false;
			return true;
		}
		
		
	}
	
	/**
	 * A skip loop which can hold a shadow id.
	 * @author Eric Bodden
	 */
	public static class SkipShadowLoop extends SMShadowEdge {

		/**
		 * @param state the state to loop over
		 * @param symbolName the label of the symbol to be skipped
		 */
		public SkipShadowLoop(SMNode state, String symbolName) {
			super(state, state, symbolName);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		public boolean isSkipEdge() {
			return true;
		}		
		
	}	
	

}