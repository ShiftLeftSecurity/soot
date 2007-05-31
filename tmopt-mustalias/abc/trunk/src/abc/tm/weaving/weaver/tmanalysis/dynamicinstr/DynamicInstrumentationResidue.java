/*
 * Created on 16-Jan-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.dynamicinstr;

import soot.ArrayType;
import soot.BooleanType;
import soot.Local;
import soot.Scene;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.Expr;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.ConstructorInliningMap;
import abc.weaving.weaver.WeavingContext;

/**
 * This is a special residue used to dynamically enable or disable a shadow based on a boolean
 * value in an array. The residue passes if the boolean value at position {@link #shadowNumber}
 * is <code>true</code> and fails otherwise.
 *
 * @author Eric Bodden
 */
public class DynamicInstrumentationResidue extends Residue {

	protected int shadowNumber;

	/**
	 * Creates a new residue for the given shadow number.
	 * The residue passes if that shadow is enabled via its boolean flag.
	 * @param shadowNumber a valid shadow number; this must be smaller than
	 * the size of {@link ShadowRegistry#enabledShadows()} and greater or equal to 0.
	 */
	public DynamicInstrumentationResidue(int shadowNumber) {
		assert shadowNumber>=0 && shadowNumber<ShadowRegistry.v().enabledShadows().size();
		
		this.shadowNumber = shadowNumber;
	}

	/**
	 * {@inheritDoc}
	 */
	public Stmt codeGen(SootMethod method, LocalGeneratorEx localgen,
			Chain units, Stmt begin, Stmt fail, boolean sense, WeavingContext wc) {
		if(!sense) {
			throw new RuntimeException("This residue should not be used under negation.");
		}
		
		
		//fetch the boolean array to a local variable
		//boolean[] enabled_array = ShadowSwitch.enabled;
		Local array = localgen.generateLocal(ArrayType.v(BooleanType.v(),1),"enabled_array");
		SootFieldRef fieldRef = Scene.v().makeFieldRef(
			Scene.v().getSootClass(DynamicInstrumenter.SHADOW_SWITCH_CLASS_NAME),
			"enabled",
			ArrayType.v(BooleanType.v(),1),
			true
		);		
		StaticFieldRef staticFieldRef = Jimple.v().newStaticFieldRef(fieldRef);
		AssignStmt assignStmt = Jimple.v().newAssignStmt(array, staticFieldRef);
		units.insertAfter(assignStmt, begin);

		//boolean isEnabled = enabled_array[shadowNumber]
		Local ifresult=localgen.generateLocal(BooleanType.v(),"isEnabled");
		ArrayRef arrayRef = Jimple.v().newArrayRef(array, IntConstant.v(shadowNumber));
		AssignStmt resAssignStmt = Jimple.v().newAssignStmt(ifresult, arrayRef);
		units.insertAfter(resAssignStmt, assignStmt);
				
		//we want the residue to succeed if the field value is true, which means that we jump to
		//fail if it is false
		Expr test = Jimple.v().newEqExpr(ifresult,IntConstant.v(0));
		IfStmt ifStmt = Jimple.v().newIfStmt(test, fail);
		units.insertAfter(ifStmt, resAssignStmt);

		//return the last statement that was added
		return ifStmt;
	}

	/**
	 * {@inheritDoc}
	 */
	public Residue inline(ConstructorInliningMap cim) {
		return new DynamicInstrumentationResidue(shadowNumber);
	}

	/**
	 * {@inheritDoc}
	 */
	public Residue optimize() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "if(enabled["+shadowNumber+"])";
	}

}
