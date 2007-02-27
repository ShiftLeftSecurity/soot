/*
 * Created on 18-Aug-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.util;

import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;
import abc.weaving.residues.WeavingVar;

/**
 * WeavingVarContext
 *
 * @author Eric Bodden
 */
public class WeavingVarContext {
	
	protected WeavingVar wv;
	
	protected SootClass cl;
	
	protected SootMethod m;
	
	protected Stmt stmt;

	/**
	 * @param wv
	 * @param cl
	 * @param m
	 * @param stmt
	 */
	public WeavingVarContext(WeavingVar wv, SootClass cl, SootMethod m, Stmt stmt) {
		super();
		this.wv = wv;
		this.cl = cl;
		this.m = m;
		this.stmt = stmt;
	}

	/**
	 * @return the stmt
	 */
	public Stmt getStmt() {
		return stmt;
	}

	/**
	 * @return the cl
	 */
	public SootClass getSootClass() {
		return cl;
	}

	/**
	 * @return the m
	 */
	public SootMethod getSootMethod() {
		return m;
	}

	/**
	 * @return the wv
	 */
	public WeavingVar getWeavingVar() {
		return wv;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "'"+wv+"' in "+getSootClass()+"."+getSootMethod()+
			(getStmt()==null ? "" : "at Stmt "+getStmt()); 
	}
	
	/**
	 * Returns <code>true</code> if the context of this object is equal
	 * to the one that is given as a parameter, i.e. the objects are equal
	 * up to possibly different WeavingVars.
	 */
	public boolean equalContext(WeavingVarContext obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final WeavingVarContext other = (WeavingVarContext) obj;
		if (cl == null) {
			if (other.cl != null)
				return false;
		} else if (!cl.equals(other.cl))
			return false;
		if (m == null) {
			if (other.m != null)
				return false;
		} else if (!m.equals(other.m))
			return false;
		if (stmt == null) {
			if (other.stmt != null)
				return false;
		} else if (!stmt.equals(other.stmt))
			return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((cl == null) ? 0 : cl.hashCode());
		result = PRIME * result + ((m == null) ? 0 : m.hashCode());
		result = PRIME * result + ((stmt == null) ? 0 : stmt.hashCode());
		result = PRIME * result + ((wv == null) ? 0 : wv.hashCode());
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
		final WeavingVarContext other = (WeavingVarContext) obj;
		if (cl == null) {
			if (other.cl != null)
				return false;
		} else if (!cl.equals(other.cl))
			return false;
		if (m == null) {
			if (other.m != null)
				return false;
		} else if (!m.equals(other.m))
			return false;
		if (stmt == null) {
			if (other.stmt != null)
				return false;
		} else if (!stmt.equals(other.stmt))
			return false;
		if (wv == null) {
			if (other.wv != null)
				return false;
		} else if (!wv.equals(other.wv))
			return false;
		return true;
	}
	

}
