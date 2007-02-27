/*
 * Created on 17-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis;

import abc.main.Debug;

public class Timer {
	
	private long stamp = -1;
	private long acc = 0;
	private String name; 
	private boolean running = false;		

	public Timer(String name) {
		this.name=name;
	}
	
	public synchronized void startOrResume() {
		if(running) {
			new RuntimeException("Timer already running!").printStackTrace();								
		} else {
			if(Debug.v().tmTimerTrace) {
				System.err.println(name+"...");
			}
			running = true;
			stamp = System.currentTimeMillis();
		}
	}
	
	public synchronized void stop() {
		if(!running) {
			new RuntimeException("Timer not running!").printStackTrace();								
		} else {
			running = false;
			acc += System.currentTimeMillis()-stamp;
			if(Debug.v().tmTimerTrace) {
				System.err.println(name+" : "+value()+"ms");
			}
		}
	}
	
	public synchronized long value() {
		return acc;		
	}
	
	public String getName() {
		return name;
	}

	public synchronized String toString() {
		if(running) {
			return "running";								
		} else if(stamp==-1) {
			return "did not run";
		} else {
			return Long.toString(value());
		}
	}
	
}