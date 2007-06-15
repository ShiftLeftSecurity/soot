/*
 * Created on 15-Jun-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.mustalias;

import soot.jimple.Expr;

/**
 * InstanceKey
 *
 * @author Eric Bodden
 */
public interface InstanceKey {
	
	final static InstanceKey UNKNOWN = new UnknownInstanceKey();  
	
	class UnknownInstanceKey implements InstanceKey {

		public boolean isSameAs(InstanceKey other) {
			//always false
			return false;
		}
		
		public boolean isCertainlyNotSameAs(InstanceKey other) {
			//always false
			return false;
		}

		public String toString() {
			return "<UNKNOWN>";
		}

		public boolean isUnknown() {
			return true;
		}
		
		public boolean equals(Object obj) {
			return obj instanceof UnknownInstanceKey;
		}

	}
	
	class ExprBasedInstanceKey implements InstanceKey {

		private final Expr e;

		public ExprBasedInstanceKey(Expr e) {
			this.e = e;
		}
		
		public boolean isSameAs(InstanceKey otherKey) {
			if (this == otherKey)
				return true;
			if (otherKey == null)
				return false;
			if (getClass() != otherKey.getClass())
				return false;
			final ExprBasedInstanceKey other = (ExprBasedInstanceKey) otherKey;
			if (e!=other.e)
				return false;
			return true;
		}
		
		public boolean isCertainlyNotSameAs(InstanceKey otherKey) {
			if (this == otherKey)
				return false;
			if (otherKey == null)
				return false;
			if (getClass() != otherKey.getClass())
				return false;
			final ExprBasedInstanceKey other = (ExprBasedInstanceKey) otherKey;
			if (e!=other.e)
				return true;
			else
				return false;
		}

		public String toString() {
			return "InstanceKey< "+System.identityHashCode(e)+": "+e+" >";
		}
	
		public boolean isUnknown() {
			return false;
		}

		public Expr getExpr() {
			return e;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((e == null) ? 0 : e.hashCode());
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ExprBasedInstanceKey other = (ExprBasedInstanceKey) obj;
			return e == other.e;
		}
	}

	public boolean isSameAs(InstanceKey other); 
	
	public boolean isCertainlyNotSameAs(InstanceKey other); 

	public boolean isUnknown();

}
