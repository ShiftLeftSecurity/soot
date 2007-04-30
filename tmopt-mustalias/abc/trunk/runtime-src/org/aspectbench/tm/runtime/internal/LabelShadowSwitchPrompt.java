/*
 * Created on 30-Apr-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.aspectbench.tm.runtime.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * LabelShadowSwitchPrompt
 *
 * @author Eric Bodden
 */
public class LabelShadowSwitchPrompt implements Runnable {

	protected Set disabled = new HashSet();
	
	/**
	 * {@inheritDoc}
	 */
	public void run() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("At the moment, label shadows for all shadows are enabled.");
		
		while(true) {
			System.out.println("Please type the name of any class containing a tracematch to disable/enable its label shadows.\n" +
					"Type 'x' to exit this console.");
			try {
				String line = in.readLine();
				if(line.toLowerCase().equals("x")) {
					break;
				}
				switchTraceMatch(line);
				
			} catch (IOException e) {
				break;
			}
		}
		

	}

	/**
	 * @param className
	 */
	private void switchTraceMatch(String className) {
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

			System.err.println("Label shadows for tracematch in class "+className+" successfully "+(isEnabled?"disabled":"enabled")+".");
			
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
				
	}

	public static void start() {
		new Thread(new LabelShadowSwitchPrompt()).start();
	}

}
