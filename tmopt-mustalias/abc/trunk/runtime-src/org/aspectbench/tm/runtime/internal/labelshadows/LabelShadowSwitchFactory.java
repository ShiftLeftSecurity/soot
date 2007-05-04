package org.aspectbench.tm.runtime.internal.labelshadows;

/**
 * LabelShadowSwitchFactory
 *
 * @author Eric Bodden
 */
public class LabelShadowSwitchFactory {
	
	/** Priority value higher than {@link Thread#NORM_PRIORITY}.  */
	private static final int ABOVE_NORMAL = 7;

	public static void start() {
		String params = System.getProperty("LABELSHADOWSWITCH");
		
		if(params==null) {
			return;
		}
		
		Runnable runnable = null;		
		if(params.equals("interactive")) {
			runnable = new InteractiveLabelShadowSwitchPrompt();
		} else if(params.equals("timed")){
			runnable = new TimedShadowSwitch();
		} else {
			System.err.println("#######################################");
			System.err.println("Illegal argument for LABELSHADOWSWITCH: "+params);
			System.err.println("USAGE:");
			System.err.println("LABELSHADOWSWITCH=interactive");
			System.err.println("LABELSHADOWSWITCH=timed");
			System.err.println("#######################################");
		}
		if(runnable!=null) {
			Thread thread = new Thread(runnable);
			thread.setPriority(ABOVE_NORMAL);
			thread.start();
		}
	}

}
