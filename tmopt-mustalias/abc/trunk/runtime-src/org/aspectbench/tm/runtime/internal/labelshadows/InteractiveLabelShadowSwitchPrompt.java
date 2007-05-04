/*
 * Created on 1-May-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.aspectbench.tm.runtime.internal.labelshadows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * InteractiveLabelShadowSwitchPrompt
 *
 * @author Eric Bodden
 */
public class InteractiveLabelShadowSwitchPrompt extends	AbstractLabelShadowSwitch {

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
				boolean enabled = switchTraceMatch(line);
				
				System.err.println("new label-edge status: "+(enabled?"enabled":"disabled"));
				
			} catch (IOException e) {
				break;
			}
		}		

	}
	
}
