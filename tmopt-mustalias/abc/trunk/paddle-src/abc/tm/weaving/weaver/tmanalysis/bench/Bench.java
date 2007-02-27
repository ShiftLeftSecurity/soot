/*
 * Created on 17-Oct-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.bench;

import java.io.File;
import java.util.Iterator;

import soot.PhaseOptions;
import soot.Scene;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.Timer;

/**
 * Bench
 *
 * @author Eric Bodden
 */
public class Bench {
	
	//global timers
	public final static Timer T1_COMPLETE = new Timer("T1:Complete");
	public final static Timer T2_CG = new Timer("T2:CG-Phase");
	public final static Timer T3_CG_ABST = new Timer("T3:CG-Abstraction");
	public final static Timer T4_UG_SM = new Timer("T4:UGStateMachines");
	public final static Timer T5_INTER_ABSTR = new Timer("T5:InterproceduralAbstraction");
	public final static Timer T6_TM_MINIMIZATION = new Timer("T6:TMMinimization");
	public final static Timer T7_PG_MINIMIZATION = new Timer("T7:PGMinimization");
	public final static Timer T8_FLOWINS_PART = new Timer("T8:FlowInsensitiveAnalysis-Partitioning");
	public final static Timer T9_FLOWINS_REMOVE = new Timer("T8:FlowInsensitiveAnalysis-EdgeRemoval");

	//timers for fixed-point iteration
	public final static Timer FPT1_FP_FULL_DEFAULT = new Timer("FPT1:FPFullDefOrder");
	public final static Timer FPT2_FP_FULL_OPT = new Timer("FPT2:FPFullOptOrder");
	public final static Timer FPT3_FP_OPT_DEFAULT = new Timer("FPT3:FPOptDefOrder");
	public final static Timer FPT4_FP_OPT_OPT = new Timer("FPT4:FPOptOptOrder");
	public final static Timer FPT5_FP_OO_MIN_TM = new Timer("FPT5:FPOptOptOrderMinTM");
	public final static Timer FPT6_FP_OO_MIN_PG = new Timer("FPT6:FPOptOptOrderMinPG");

	//shadow counts
	public static int S1_SHADOWS_ALL = -1;
	public static int S2_SHADOWS_OPT = -1;
	public static int S3_SHADOWS_INIT_REACHABLE = -1;
	
	//call graph size
	public static int C1_CG_SIZE_FULL = -1;
	public static int C2_CG_SIZE_OPT = -1;
	
	//UG state machine number and sizes
	public static int U1_UG_COUNT = -1;
	public static double U2_UG_SIZE_AVG = -1;
	public static int U3_UG_SIZE_MAX = -1;
	public static int U4_SHARING_COUNT = -1;
	public static String U5_LARGEST_SM = "not set"; 
	
	//TM state machine sizes
	public static int TM1_TM_SIZE_FULL = -1; 
	public static int TM2_TM_SIZE_MIN = -1;
	
	//program graph sizes
	public static int PG1_PG_SIZE_FULL = -1; 
	public static int PG2_PG_SIZE_MIN = -1;
	
	//number of thread contexts
	public static int TC_NUM = -1;
	
	//program graph SCC count and size
	public static int SCC1_PG_FULL_SCC_NUM = -1;
	public static double SCC2_PG_FULL_SCC_AVG_SIZE = -1;
	public static int SCC3_PG_MIN_SCC_NUM = -1;
	public static double SCC4_PG_MIN_SCC_AVG_SIZE = -1;
	
	//times for constructing the reverse pseudo-topological order
	public static long RPTO2_FULL = -1;
	public static long RPTO4_OPT = -1;
	public static long RPTO5_OPT_MIN_TM = -1;
	public static long RPTO6_OPT_MIN_PG = -1;
	
	//flow computation counts
	public static int FPFC1_FP_FULL_DEFAULT = -1;
	public static int FPFC2_FP_FULL_OPT = -1;
	public static int FPFC3_FP_OPT_DEFAULT = -1;
	public static int FPFC4_FP_OPT_OPT = -1;
	public static int FPFC5_FP_OO_MIN_TM = -1;
	public static int FPFC6_FP_OO_MIN_PG = -1;
	
	//transition iteration counts
	public static int FPTC1_FP_FULL_DEFAULT = -1;
	public static int FPTC2_FP_FULL_OPT = -1;
	public static int FPTC3_FP_OPT_DEFAULT = -1;
	public static int FPTC4_FP_OPT_OPT = -1;
	public static int FPTC5_FP_OO_MIN_TM = -1;
	public static int FPTC6_FP_OO_MIN_PG = -1;
	
	//maximal depth of intersection
	public static int IM_INTERS_DEPTH_MAX = -1;
	
	//error bit; is set to false on graceful termination
	public static boolean HAS_ERROR = true;
	
	//flow-insensitive analysis
	public static int FI1_NUM_COMPONENTS = -1;
	public static int FI2_NUM_COMPLETE_COMPONENTS = -1;
	public static int FI3_NUM_PARTITIONING_ITERATIONS = -1;
	public static int FI4_NUM_SHADOWS = -1;
	public static int FI5_NUM_REMOVED = -1;
	//enabling those can actually slow down the program a lot
//	public static long FI6_MERGE_CACHE_HITS = -1;
//	public static long FI7_MERGE_CACHE_MISSES = -1;
//	public static long FI8_TRANS_CACHE_HITS = -1;
//	public static long FI9_TRANS_CACHE_MISSES = -1;
	
	public static int THR_THREAD_ITERATIONS = 0;
	
	public static String OUTCOME = "error";

	private static String res;
	
	private static volatile boolean alreadyPrinted = false;
	
	public static void print() {		
		alreadyPrinted = true;
		res = "";
		
		add("GENERAL","");
		String name;
		try {
			name = Scene.v().getMainClass().getName();
		} catch (RuntimeException e) {
			name = "NO MAIN CLASS AVAILABLE!";
		}
		add("d1",name+","+new File(".").getAbsolutePath());		
		add("d2",traceMatchContainerClassNames());
		add("d3","cg:"+PhaseOptions.v().getPhaseOptions("cg")+",cg.paddle:"+PhaseOptions.v().getPhaseOptions("cg.paddle"));		
		add("SHADOW COUNTS","");
		add("s1",""+S1_SHADOWS_ALL);
		add("s2",""+S2_SHADOWS_OPT);
		add("s3",""+S3_SHADOWS_INIT_REACHABLE);
		add("CALLGRAPH SIZES","");
		add("c1",""+C1_CG_SIZE_FULL);
		add("c2",""+C2_CG_SIZE_OPT);
		add("UG-SM COUNT/SIZES","");
		add("u1",""+U1_UG_COUNT);
		add("u2",""+U2_UG_SIZE_AVG);
		add("u3",""+U3_UG_SIZE_MAX);
		add("u4",""+U4_SHARING_COUNT);
		add("u5",""+U5_LARGEST_SM);
		add("TM-SM SIZES","");
		add("tm1",""+TM1_TM_SIZE_FULL);
		add("tm2",""+TM2_TM_SIZE_MIN);
		add("PROGRAM GRAPH SIZES","");
		add("pg1",""+PG1_PG_SIZE_FULL);
		add("pg2",""+PG2_PG_SIZE_MIN);
		add("NUM THREAD CONTEXTS","");
		add("tc",""+TC_NUM);
		add("PROGRAM SCC SIZES AND COUNTS","");
		add("scc1",""+SCC1_PG_FULL_SCC_NUM);
		add("scc2",""+SCC2_PG_FULL_SCC_AVG_SIZE);
		add("scc3",""+SCC3_PG_MIN_SCC_NUM);
		add("scc4",""+SCC4_PG_MIN_SCC_AVG_SIZE);
		add("GENERAL TIMERS","");
		add("t2",T2_CG);
		add("t3",T3_CG_ABST);
		add("t4",T4_UG_SM);
		add("t5",T5_INTER_ABSTR);
		add("t6",T6_TM_MINIMIZATION);
		add("t7",T7_PG_MINIMIZATION);
		add("t8",T8_FLOWINS_PART);
		add("t9",T9_FLOWINS_REMOVE);
		add("TIMES FOR ORDER CONSTRUCTION","");
		add("rpto2",""+RPTO2_FULL);
		add("rpto4",""+RPTO4_OPT);
		add("rpto5",""+RPTO5_OPT_MIN_TM);
		add("rpto6",""+RPTO6_OPT_MIN_PG);
		add("FP-ITERATION TIMERS","");
		add("fpt1",FPT1_FP_FULL_DEFAULT);
		add("fpt2",FPT2_FP_FULL_OPT);
		add("fpt3",FPT3_FP_OPT_DEFAULT);
		add("fpt4",FPT4_FP_OPT_OPT);
		add("fpt5",FPT5_FP_OO_MIN_TM);
		add("fpt6",FPT6_FP_OO_MIN_PG);
		add("FP FLOW COMPUTATION COUNTS","");
		add("fpfc1",""+FPFC1_FP_FULL_DEFAULT);
		add("fpfc2",""+FPFC2_FP_FULL_OPT);
		add("fpfc3",""+FPFC3_FP_OPT_DEFAULT);
		add("fpfc4",""+FPFC4_FP_OPT_OPT);
		add("fpfc5",""+FPFC5_FP_OO_MIN_TM);
		add("fpfc6",""+FPFC6_FP_OO_MIN_PG);
		add("FP TRANSITION COMPUTATION COUNTS","");
		add("fptc1",""+FPTC1_FP_FULL_DEFAULT);
		add("fptc2",""+FPTC2_FP_FULL_OPT);
		add("fptc3",""+FPTC3_FP_OPT_DEFAULT);
		add("fptc4",""+FPTC4_FP_OPT_OPT);
		add("fptc5",""+FPTC5_FP_OO_MIN_TM);
		add("fptc6",""+FPTC6_FP_OO_MIN_PG);
		add("MAX UNION/INTERS. DEPTH","");
		add("idm",""+IM_INTERS_DEPTH_MAX);
		add("FLOWINSENSITIVE ANALYSIS","");
		add("fi1",""+FI1_NUM_COMPONENTS);
		add("fi2",""+FI2_NUM_COMPLETE_COMPONENTS);
		add("fi3",""+FI3_NUM_PARTITIONING_ITERATIONS);
		add("fi4",""+FI4_NUM_SHADOWS);
		add("fi5",""+FI5_NUM_REMOVED);
//		add("fi6",""+FI6_MERGE_CACHE_HITS);
//		add("fi7",""+FI7_MERGE_CACHE_MISSES);
//		add("fi8",""+FI8_TRANS_CACHE_HITS);
//		add("fi9",""+FI9_TRANS_CACHE_MISSES);
		add("THREADING","");
		add("thr",""+THR_THREAD_ITERATIONS);
		add("OUTCOME","");
		add("outcome",""+OUTCOME);
		add("ERROR FLAG","");
		add("error",""+HAS_ERROR);
		
		System.err.println(res);
	}
	
	/**
	 * Timer for complete runtime has to be run separately from the shutdown hook.
	 */
	private static void printCompleteTime() {
		res = "";
		add("COMPLETE RUNTIME","");		
		add("t1",T1_COMPLETE);		
		System.err.println(res);
	}

	/**
	 * @return
	 */
	private static String traceMatchContainerClassNames() {
		String tmNames = "";
		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
		for (Iterator iter = gai.getTraceMatches().iterator(); iter.hasNext();) {
			TraceMatch tm = (TraceMatch) iter.next();
			tmNames += tm.getContainerClass().getName() +",";
		}
		return tmNames;
	}

	private static void add(Object val1, Object val2) {
		add(":::::"); add(val1); add(val2); nl();
	}
	
	private static void add(Object val) {
		res += "\""+ val.toString() + "\",";
	}
	
	private static void nl() {
		res+= "\n";
	}
	
	static {
		
		try{
			Runtime.getRuntime().addShutdownHook( new Thread() {
				public void run() {
					//stop timer for complete run
					T1_COMPLETE.stop();
					synchronized(Bench.class) {
						if(!alreadyPrinted) {
							print();
						}
					}					
					printCompleteTime();
				}
				
			});
		} catch(IllegalStateException e) {
			//if this is thrown, shutdown was externally initiated;
			//just do nothing then
		}
		
	}

}
