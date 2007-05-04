package org.aspectbench.tm.runtime.internal;

import java.util.StringTokenizer;

import org.aspectbench.tm.runtime.internal.labelshadows.LabelShadowSwitchFactory;

/**
 * ShadowSwitch
 *
 * @author Eric Bodden
 */
public class ShadowSwitch {
	
	public static boolean groupTable[][];
	
	public static boolean enabled[];
	
	static {
		initialize();
	}

	private static void initialize() {
		try {
			IShadowSwitchInitializer initializer = (IShadowSwitchInitializer) Class.forName("org.aspectbench.tm.runtime.internal.ShadowSwitchInitializer").newInstance();
			initializer.initialize();
		} catch (InstantiationException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}				
		
		System.out.println(groupTable.length + " shadow groups present");
		
		String argString = System.getProperty("SHADOWGROUPS","");
		parse(argString);
		LabelShadowSwitchFactory.start();
	}

	private static void parse(String argString) {
		if(argString.length()>0) {
			if(argString.equals("all")) {
				all();
				return;
			}
			
			String[] split = argString.split(":");
			String format = split[0];
			String arg = split[1];		
			if(format.equals("enum")) {
				byNumber(arg);
			} else if(format.equals("upto")) {
				upToNumber(arg);
			} else {
				System.err.println("No shadow groups enabled.");
			}
		} else {
			System.err.println("No shadow groups enabled.");
		}
	}
	
	private static void all() {
		for(int i=0;i<groupTable.length;i++) {
			enableShadowGroup(i);
		}
		System.err.println("Enabled all shadow groups.");
	}

	private static void upToNumber(String arg) {
		int bound = Integer.parseInt(arg);
		if(bound<0) {
			throw new IllegalArgumentException("bound must be >=0 !");
		}
		for(int i=0;i<bound;i++) {
			enableShadowGroup(i);
		}
		System.err.println("Enabled all shadow groups up to #"+bound);
	}

	private static void byNumber(String enumeration) {
		StringTokenizer toki = new StringTokenizer(enumeration,",");
	    while(toki.hasMoreTokens()) {
	    	String token = toki.nextToken();
	    	int groupId = Integer.parseInt(token);
	    	enableShadowGroup(groupId);
	    }
		System.err.println("Enabled shadow groups: "+enumeration);
	}

	public static void enableShadowGroup(int groupNumber) {
		for (int i = 0; i < groupTable[groupNumber].length; i++) {
			boolean toEnable = groupTable[groupNumber][i];
			enabled[i] = enabled[i] | toEnable;
		}		
	}


	public static void disableAllGroups() {
		for (int i = 0; i < enabled.length; i++) {
			enabled[i] = false;			
		}
	}
	
}
