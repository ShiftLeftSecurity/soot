/*
 * Created on 29-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.paddleext;

import java.util.Collection;

import soot.SootMethod;
import soot.Type;
import soot.jimple.paddle.NodeManager;
import soot.jimple.paddle.VarNode;
import soot.jimple.paddle.queue.Qobj_method_type;
import soot.jimple.paddle.queue.Qobj_type;
import soot.jimple.paddle.queue.Qvar_method_type;
import soot.jimple.paddle.queue.Qvar_type;
import abc.tm.weaving.weaver.tmanalysis.query.TaggedHosts;

/**
 * A node manager that stores context information for certain variables only.
 * Those variables are read from {@link TaggedHosts#findVariablesNeedingContextSensitivity()}.
 *
 * @author Eric Bodden
 */
public class CustomizedContextSensitivityNodeManager extends NodeManager {

	/** the collection of variables needing context-sensitivity */
	protected static Collection variablesNeedingContextSensitivity;
	
	/**
	 * Creates a new node manager.
	 * @see NodeManager#NodeManager(Qvar_method_type, Qvar_type, Qobj_method_type, Qobj_type)
	 */
	public CustomizedContextSensitivityNodeManager(Qvar_method_type locals,
			Qvar_type globals, Qobj_method_type localallocs,
			Qobj_type globalallocs) {
		super(locals, globals, localallocs, globalallocs);
		if(variablesNeedingContextSensitivity==null) {
			variablesNeedingContextSensitivity = TaggedHosts.v().findVariablesNeedingContextSensitivity();
		}
	}

	/**
	 * Overrides {@link NodeManager#makeLocalVarNode(Object, Type, SootMethod)} so that
	 * local var nodes are only created for variables in {@link #variablesNeedingContextSensitivity}.
	 * This is done by delegating to {@link NodeManager#makeGlobalVarNode(Object, Type)} if value
	 * is ot contained in the set.
	 * @see NodeManager#makeLocalVarNode(Object, Type, SootMethod)
	 */
	public VarNode makeLocalVarNode(Object value, Type type, SootMethod method) {
		if(variablesNeedingContextSensitivity.contains(value)) {
			return super.makeLocalVarNode(value, type, method);
		} else {
			return super.makeGlobalVarNode(value, type);
		}
	}
	
	/**
	 * Overrides {@link NodeManager#findLocalVarNode(Object)} so that var nodes for which no
	 * context is to be created are looked up using {@link NodeManager#findGlobalVarNode(Object)}.
	 */
	public VarNode findLocalVarNode(Object value) {
		if(variablesNeedingContextSensitivity.contains(value)) {
			return super.findLocalVarNode(value);
		} else {
			return super.findGlobalVarNode(value);
		}
	}
	
	

	
}