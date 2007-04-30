package abc.tm.weaving.weaver;

import java.util.Collections;

import soot.Body;
import soot.BooleanType;
import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.VoidType;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NopStmt;
import soot.jimple.StaticFieldRef;
import soot.util.Chain;
import abc.tm.weaving.aspectinfo.TraceMatch;

/**
 * DynamicSwitchIndexedCodeGenHelper
 *
 * @author Eric Bodden
 */
public class DynamicSwitchIndexedCodeGenHelper extends IndexedCodeGenHelper {

	/**  */
	private static final String BOOLEAN_FIELD_NAME = "labelShadowsEnabled";
	private static final String ENABLE_METHOD_NAME = "enableLabelShadows";
	private static final String DISABLE_METHOD_NAME = "disableLabelShadows";

	public DynamicSwitchIndexedCodeGenHelper(TraceMatch tm) {
		super(tm);
	}
	
    /**
     * Generate code to update a label with the constraint
     * for performing a "from --->[symbol] to" transition.
     */
    public void genLabelUpdate(int from, int to, String symbol,
                                        SootMethod method)
    {
        
        Body body = method.getActiveBody();
        Chain units = body.getUnits();
        LocalGenerator localGen = new LocalGenerator(body);
        
        //create new nop-statement as jump target
        NopStmt jumpTarget = Jimple.v().newNopStmt();
        
        //generate field, if not already present
        genSwitchField(method.getDeclaringClass());
        //method to set field to true, if not already present
        genEnableMethod(method.getDeclaringClass());
        //method to set field to false, if not already present
        genDisableMethod(method.getDeclaringClass());
        
        //create branch if(!labelShadowsEnabled) goto jumpTarget
        Chain branchUnits = newChain();
        //lse = labelShadowsEnabled;
        InstanceFieldRef booleanFieldRef = Jimple.v().newInstanceFieldRef(
        		body.getThisLocal(),
        		Scene.v().makeFieldRef(method.getDeclaringClass(), BOOLEAN_FIELD_NAME, BooleanType.v(), false)
        );
        Local booleanLocal = localGen.generateLocal(BooleanType.v());
        AssignStmt fieldAssignStmt = Jimple.v().newAssignStmt(booleanLocal, booleanFieldRef);
        branchUnits.add(fieldAssignStmt);
        //if(lse==0) goto jumpTarget
        IfStmt ifStmt = Jimple.v().newIfStmt(Jimple.v().newEqExpr(booleanLocal, IntConstant.v(0)), jumpTarget);
        branchUnits.add(ifStmt);
		insertBeforeReturn(branchUnits, units);        
        
        super.genLabelUpdate(from, to, symbol, method);

        //insert new nop-statement right before the return statement
        Chain nopUnitChain = newChain();
		nopUnitChain.add(jumpTarget);
		insertBeforeReturn(nopUnitChain, units);
		
    }
    
    protected void genSwitchField(SootClass container) {
    	if(!container.declaresFieldByName(BOOLEAN_FIELD_NAME)) {
    		//add field
    		container.addField(new SootField(BOOLEAN_FIELD_NAME, BooleanType.v(), Modifier.PRIVATE));

    		//initialize to "true" in constructor
    		SootMethod initializer = container.getMethodByName(SootMethod.constructorName);
    		Body initBody = initializer.getActiveBody();    		    		
	        InstanceFieldRef booleanFieldRef = Jimple.v().newInstanceFieldRef(
	        		initBody.getThisLocal(),
	        		Scene.v().makeFieldRef(container, BOOLEAN_FIELD_NAME, BooleanType.v(), false)
	        );
	        AssignStmt fieldAssignStmt = Jimple.v().newAssignStmt(booleanFieldRef,IntConstant.v(1));
	        initBody.getUnits().addFirst(fieldAssignStmt);
    	}    	
    }
    
    protected void genEnableMethod(SootClass container) {
    	genFieldSwitchMethod(ENABLE_METHOD_NAME, true, container);
    }

    protected void genDisableMethod(SootClass container) {
    	genFieldSwitchMethod(DISABLE_METHOD_NAME, false, container);
    }

    /**
     * FIXME: This only works if there is at most one tracematch per Class.
     * Actually we need to generate 'enable/disable$tracematch$i'-methods
	 * @param methodName
	 * @param enable 
     * @param container 
	 */
	private void genFieldSwitchMethod(String methodName, boolean enable, SootClass container) {
		if(!container.declaresMethodByName(methodName)) {
    		SootMethod method = new SootMethod(methodName,Collections.EMPTY_LIST,VoidType.v(),Modifier.PUBLIC);
			container.addMethod(method);			
			JimpleBody body = Jimple.v().newBody(method);
			method.setActiveBody(body);
			Chain units = body.getUnits();
			
			
			//add identity statement; necessary for locking
			Local thisLocal = Jimple.v().newLocal("this", container.getType());			
			body.getLocals().add(thisLocal);
			units.add(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(container.getType())));			
			//acquire tracematch lock
			getLock(body, units);
			
			//set value
	        InstanceFieldRef booleanFieldRef = Jimple.v().newInstanceFieldRef(
	        		body.getThisLocal(),
	        		Scene.v().makeFieldRef(method.getDeclaringClass(), BOOLEAN_FIELD_NAME, BooleanType.v(), false)
	        );
	        AssignStmt fieldAssignStmt = Jimple.v().newAssignStmt(booleanFieldRef,IntConstant.v(enable?1:0));
	        units.add(fieldAssignStmt);

	        //release lock
	        releaseLock(body, units);

	        units.add(Jimple.v().newReturnVoidStmt());
		}
	}
	
}
