/*
 * Created on 1-May-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.aspectbench.tm.runtime.internal.labelshadows;

/**
 * TimedShadowSwitch
 *
 * @author Eric Bodden
 */
public class TimedShadowSwitch extends AbstractLabelShadowSwitch {

	/** Initial time for which the non-skip loops are disabled. */
	protected volatile long tDisabled; 
	
	/** Initial time for which the non-skip loops are enabled. */
	protected volatile long tEnabled;
	
	/** Fraction of increase (or decrease) of the time for which the non-skip loops are disabled.
	 * This increase or decrease will be applied every 'periods' iterations. */
	protected volatile double incDisabled;
	
	/** Fraction of increase (or decrease) of the time for which the non-skip loops are enabled.
	 * This increase or decrease will be applied every 'periods' iterations. */
	protected volatile double incEnabled;

	/** Number of periods after which to apply increases or decreases. */
	protected volatile int periods;
	
	/** Name of the class whose tracematch should be triggered. */
	protected volatile String className;
	
	/**
	 */
	public TimedShadowSwitch() {
		tDisabled = Integer.parseInt(System.getProperty("tDisabled", "100"));
		tEnabled = Integer.parseInt(System.getProperty("tEnabled", "50"));
		incDisabled = Double.parseDouble(System.getProperty("incDisabled", "2"));
		incEnabled = Double.parseDouble(System.getProperty("incEnabled", "2"));
		periods = Integer.parseInt(System.getProperty("periods", "10"));
		className = System.getProperty("className", "");
		if(tDisabled<1) {
			throw new IllegalArgumentException("Illegal argument tDisabled="+tDisabled);
		}
		if(tEnabled<1) {
			throw new IllegalArgumentException("Illegal argument tEnabled="+tEnabled);
		}
		if(periods<1) {
			throw new IllegalArgumentException("Illegal argument periods="+periods);
		}
		if(className.length()==0) {
			throw new IllegalArgumentException("No className given! Use: java -DclassName=<ClassName>");
		}
		System.err.println("Instantiated timed shadow switch.");
		System.err.println("Will start with disabling for "+tDisabled+"ms then enabling for "+tEnabled+"ms.");
		System.err.println("'disabled' time will increase by factor "+incDisabled+" and 'enabled' by factor "+incEnabled);
		System.err.println("after every "+periods+" periods.");	
		System.err.println(System.getProperty("periods"));
	}


	/**
	 * {@inheritDoc}
	 */
	public void run() {
		int per = 0;
		while(true) {
			//switch label shadows off
			switchTraceMatch(className);
			//wait for <breakLength> ms
			synchronized (this) {
				try {
					wait(tDisabled);
				} catch (InterruptedException e) {
				}
			}
			//switch label shadows on
			switchTraceMatch(className);
			//wait for <breakLength> ms
			synchronized (this) {
				try {
					wait(tEnabled);
				} catch (InterruptedException e) {
				}
			}			
			per = (per++) % periods;
			if(per==0) {
				tDisabled = Math.round(tDisabled * incDisabled);
				tEnabled = Math.round(tEnabled * incEnabled);
			}
		}
	}

}
