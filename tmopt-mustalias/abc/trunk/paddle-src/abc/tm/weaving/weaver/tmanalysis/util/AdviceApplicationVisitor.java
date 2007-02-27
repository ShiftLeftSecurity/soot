/*
 * Created on 17-Aug-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.util;

import java.util.Iterator;

import soot.SootClass;
import soot.SootMethod;
import abc.main.Main;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;

public class AdviceApplicationVisitor {
	
	protected static AdviceApplicationVisitor instance;
	
	private AdviceApplicationVisitor() {}
	
	protected GlobalAspectInfo gai = Main.v().getAbcExtension().getGlobalAspectInfo();
	
	public void traverse(AdviceApplicationHandler aah) {
	
		for (Iterator classIter = gai.getWeavableClasses().iterator(); classIter.hasNext();) {
			AbcClass abcClass = (AbcClass) classIter.next();
			SootClass c = abcClass.getSootClass();
			
			for (Iterator methodIter = c.methodIterator(); methodIter.hasNext();) {
				SootMethod m = (SootMethod) methodIter.next();
				
				MethodAdviceList adviceList = gai.getAdviceList(m);
				
				if(adviceList!=null) {
					for (Iterator aaIter = adviceList.allAdvice().iterator(); aaIter.hasNext();) {
						AdviceApplication aa = (AdviceApplication) aaIter.next();
						
						aah.adviceApplication(aa,m);
						
					}
				}
				
			}
			
		}
		
	}
	
	public static AdviceApplicationVisitor v() {
		if(instance==null) {
			instance = new AdviceApplicationVisitor();
		}
		return instance;
	}
	
	public interface AdviceApplicationHandler {
		
		public void adviceApplication(AdviceApplication aa, SootMethod m);
		
	}
}