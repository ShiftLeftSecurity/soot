/*
 * Created on 30-Apr-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.aspectbench.tm.runtime.internal.labelshadows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;


/**
 * AbstractLabelShadowSwitch
 *
 * @author Eric Bodden
 */
public abstract class AbstractLabelShadowSwitch implements Runnable {

	protected Set disabled = new HashSet();
	
	public abstract void run();

	/**
	 * @param className
	 * @return true if the tracematch is now enabled or false if it is not
	 */
	protected boolean switchTraceMatch(String className) {
		boolean isEnabled = !disabled.contains(className);
		try {
			Class cl = Class.forName(className);
			
			String methodName;
			if(isEnabled) {
				methodName = "disableLabelShadows";
				disabled.add(className);
			} else {
				methodName = "enableLabelShadows";
				disabled.remove(className);
			}

			Method aspectOfMethod = cl.getMethod("aspectOf",new Class[0]);
			Object aspectInstance = aspectOfMethod.invoke(null,new Object[0]);
			
			Method method = cl.getMethod(methodName,new Class[0]);
			method.invoke(aspectInstance,new Object[0]);
						
			isEnabled = !isEnabled;
			
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found: "+e.getMessage());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} 
		
		return isEnabled;
	}

}
