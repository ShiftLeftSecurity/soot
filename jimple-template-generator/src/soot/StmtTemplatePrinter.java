package soot;

import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StmtSwitch;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;

class StmtTemplatePrinter implements StmtSwitch {
	private final TemplatePrinter p;
	
	private final ValueTemplatePrinter vtp;

	/**
	 * @param templatePrinter
	 */
	StmtTemplatePrinter(TemplatePrinter templatePrinter) {
		this.p = templatePrinter;
		this.vtp = new ValueTemplatePrinter(p);
	}

	public void defaultCase(Object obj) {
		// TODO Auto-generated method stub
		
	}

	public void caseThrowStmt(ThrowStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	public void caseTableSwitchStmt(TableSwitchStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	public void caseReturnStmt(ReturnStmt stmt) {
		Value value = stmt.getOp();
		value.apply(vtp);
		p.println("units.add(Jimple.v().newReturnStmt(retVal));");		
	}

	public void caseRetStmt(RetStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	public void caseNopStmt(NopStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	public void caseInvokeStmt(InvokeStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	public void caseIfStmt(IfStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	public void caseIdentityStmt(IdentityStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	public void caseGotoStmt(GotoStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	public void caseBreakpointStmt(BreakpointStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	public void caseAssignStmt(AssignStmt stmt) {
		// TODO Auto-generated method stub
		
	}
}