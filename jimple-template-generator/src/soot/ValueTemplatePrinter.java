package soot;

import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.ArrayRef;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ClassConstant;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.DivExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.EqExpr;
import soot.jimple.FloatConstant;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.JimpleValueSwitch;
import soot.jimple.LeExpr;
import soot.jimple.LengthExpr;
import soot.jimple.LongConstant;
import soot.jimple.LtExpr;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.OrExpr;
import soot.jimple.ParameterRef;
import soot.jimple.RemExpr;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.SubExpr;
import soot.jimple.ThisRef;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;

public class ValueTemplatePrinter implements JimpleValueSwitch {

	private final TemplatePrinter p;

	public ValueTemplatePrinter(TemplatePrinter p) {
		this.p = p;
		// TODO Auto-generated constructor stub
	}

	public void caseDoubleConstant(DoubleConstant v) {
		p.print("Value retVal = DoubleConstant.v("+v.value+");");
	}

	public void caseFloatConstant(FloatConstant v) {
		// TODO Auto-generated method stub

	}

	public void caseIntConstant(IntConstant v) {
		// TODO Auto-generated method stub

	}

	public void caseLongConstant(LongConstant v) {
		// TODO Auto-generated method stub

	}

	public void caseNullConstant(NullConstant v) {
		// TODO Auto-generated method stub

	}

	public void caseStringConstant(StringConstant v) {
		// TODO Auto-generated method stub

	}

	public void caseClassConstant(ClassConstant v) {
		// TODO Auto-generated method stub

	}

	public void defaultCase(Object object) {
		// TODO Auto-generated method stub

	}

	public void caseAddExpr(AddExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseAndExpr(AndExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseCmpExpr(CmpExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseCmpgExpr(CmpgExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseCmplExpr(CmplExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseDivExpr(DivExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseEqExpr(EqExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseNeExpr(NeExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseGeExpr(GeExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseGtExpr(GtExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseLeExpr(LeExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseLtExpr(LtExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseMulExpr(MulExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseOrExpr(OrExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseRemExpr(RemExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseShlExpr(ShlExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseShrExpr(ShrExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseUshrExpr(UshrExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseSubExpr(SubExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseXorExpr(XorExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseStaticInvokeExpr(StaticInvokeExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseCastExpr(CastExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseInstanceOfExpr(InstanceOfExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseNewArrayExpr(NewArrayExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseNewExpr(NewExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseLengthExpr(LengthExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseNegExpr(NegExpr v) {
		// TODO Auto-generated method stub

	}

	public void caseArrayRef(ArrayRef v) {
		// TODO Auto-generated method stub

	}

	public void caseStaticFieldRef(StaticFieldRef v) {
		// TODO Auto-generated method stub

	}

	public void caseInstanceFieldRef(InstanceFieldRef v) {
		// TODO Auto-generated method stub

	}

	public void caseParameterRef(ParameterRef v) {
		// TODO Auto-generated method stub

	}

	public void caseCaughtExceptionRef(CaughtExceptionRef v) {
		// TODO Auto-generated method stub

	}

	public void caseThisRef(ThisRef v) {
		// TODO Auto-generated method stub

	}

	public void caseLocal(Local l) {
		// TODO Auto-generated method stub

	}

}
