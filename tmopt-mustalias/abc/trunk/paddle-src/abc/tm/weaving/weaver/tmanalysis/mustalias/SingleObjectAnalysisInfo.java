/*
 * Created on 20-Feb-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.mustalias;

import java.util.Collection;
import java.util.HashSet;

import soot.Local;
import soot.PointsToSet;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.StmtSwitch;
import soot.jimple.internal.JNewExpr;
import soot.jimple.paddle.AllocNode;

/**
 * AnalysisInfo
 *
 * @author Eric Bodden
 */
public class SingleObjectAnalysisInfo implements Cloneable {
	
	private JNewExpr object;
	
	private boolean unique;
	
	private HashSet must;
	
	private boolean may;
	
	private HashSet mustNot;
	
	private transient Collection result;
	
	/**
	 * @param stmt
	 */
	public SingleObjectAnalysisInfo(AssignStmt stmt) {
		Value leftOp = stmt.getLeftOp();
		Value rightOp = stmt.getRightOp();
		
		assert rightOp instanceof JNewExpr;

		this.object = (JNewExpr) rightOp;
		this.unique = true;
		this.must = new HashSet();
		this.must.add(leftOp);
		this.may = false;
		this.mustNot = new HashSet();		
	}


	public void update(Stmt s) {
		result = new HashSet();
		//s.apply()
	}
	
	private final StmtSwitch SWITCH = new AbstractStmtSwitch() {
		
		public void caseAssignStmt(AssignStmt stmt) {
			Value leftOp = stmt.getLeftOp();
			Value rightOp = stmt.getRightOp();
			
			if(rightOp instanceof JNewExpr) {
				//v = new O();
				if(object.equals(rightOp)) {
					//new O() is the allocation site we care about
					
					//we revisit the original allocation site...
					
					SingleObjectAnalysisInfo copy = copy();
					copy.unique = false;
					copy.must.remove(leftOp);//FIXME incomplete: need to remove all access paths over v
					copy.mustNot.add(leftOp);
					result.add(copy);
					
					SingleObjectAnalysisInfo init = new SingleObjectAnalysisInfo(stmt);
					result.add(init);					
				}
			} else if(rightOp==NullConstant.v()) {
				if(leftOp instanceof Local) {
					SingleObjectAnalysisInfo copy = copy();
					copy.must.remove(leftOp);//FIXME incomplete: need to remove all access paths over v
					copy.mustNot.add(leftOp);
					result.add(copy);
				} else {
					System.err.println("Lefthandside not handled:"+leftOp);
				}
			}
		}
		
	};
	
	protected SingleObjectAnalysisInfo copy() {
		try {
			return (SingleObjectAnalysisInfo) clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected Object clone() throws CloneNotSupportedException {
		SingleObjectAnalysisInfo clone = (SingleObjectAnalysisInfo) super.clone();
		clone.must = (HashSet) must.clone();
		clone.mustNot = (HashSet) mustNot.clone();
		return clone;
	}
	

}
