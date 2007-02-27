/*
 * Created on 17-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis;

import abc.main.Debug;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;

/**
 * Statistics
 *
 * @author Eric Bodden
 */
public class Statistics {
	
	public static volatile String lastStageCompleted = "not set";
	
	public static volatile boolean errorOccured = false;
	
	public static void print(String label, Object value) {
		if(Debug.v().tmShadowStatistics) {
			if(Debug.v().csv) {
				System.err.println("\":::::\",\""+label+"\",\""+value+"\",");
			} else {
				System.err.println(label+" = "+value);
			}
		}
	}

	/**
	 * 
	 */
	public static void printFinalStatistics() {
		print("last-stage-completed",lastStageCompleted);
		print("full-shadow-count",ShadowRegistry.v().allShadows().size()+"");
		print("remaining-shadow-count",ShadowRegistry.v().enabledShadows().size()+"");
		print("error",errorOccured+"");
	}
	

}
