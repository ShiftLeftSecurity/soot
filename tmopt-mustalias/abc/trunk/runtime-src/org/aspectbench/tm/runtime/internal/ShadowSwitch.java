package org.aspectbench.tm.runtime.internal;

import java.util.StringTokenizer;

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
		LabelShadowSwitchPrompt.start();
	}

	private static void parse(String argString) {
		if(argString.length()==0) {
			return;
		}
		
		
		String[] split = argString.split(":");
		String format = split[0];
		String arg = split[1];		
		if(format.equals("enum")) {
			byNumber(arg);
		} else if(format.equals("upto")) {
			upToNumber(arg);
		}
	}
	
	private static void upToNumber(String arg) {
		int bound = Integer.parseInt(arg);
		if(bound<0) {
			throw new IllegalArgumentException("bound must be >=0 !");
		}
		for(int i=0;i<bound;i++) {
			enableShadowGroup(i);
		}
	}

	private static void byNumber(String enumeration) {
		StringTokenizer toki = new StringTokenizer(enumeration,",");
	    while(toki.hasMoreTokens()) {
	    	String token = toki.nextToken();
	    	int groupId = Integer.parseInt(token);
	    	enableShadowGroup(groupId);
	    }
	}

	public static void enableShadowGroup(int groupNumber) {
    	System.err.println("enabled shadow group #"+groupNumber);
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
