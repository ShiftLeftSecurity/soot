/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Eric Bodden
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package abc.tm.weaving.weaver.tmanalysis;

import java.util.Iterator;
import java.util.List;

import abc.main.AbcTimer;
import abc.main.Debug;
import abc.main.Main;
import abc.main.options.OptionsParser;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.dynamicinstr.DynamicInstrumenter;
import abc.tm.weaving.weaver.tmanalysis.query.ReachableShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupStatistics;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.TraceMatchByName;
import abc.tm.weaving.weaver.tmanalysis.query.WeavableMethods;
import abc.tm.weaving.weaver.tmanalysis.stages.CallGraphAbstraction;
import abc.tm.weaving.weaver.tmanalysis.stages.FlowInsensitiveAnalysis;
import abc.tm.weaving.weaver.tmanalysis.stages.IntraproceduralAnalysis;
import abc.tm.weaving.weaver.tmanalysis.stages.QuickCheck;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger;
import abc.weaving.weaver.AbstractReweavingAnalysis;

/**
 * @author Eric Bodden
 */
public class TracematchAnalysis extends AbstractReweavingAnalysis {

	protected boolean originalState_cleanupAfterAdviceWeave;
	
	protected TMGlobalAspectInfo gai;
	
    /** 
     * Performs the static analysis for tracematches. This currently consists of the following
     * steps:
     * <ol>
     * <li> Tag each first unit that is matched by a tracematch symbol at each shadow with
     *      the symbols that match it.
     * <li> Build an abstracted call graph. This graph holds only nodes (i.e. methods) which
     *      hold at least one unit that was tagged in the previous step. Edges are over-
     *      approximated in a sound way.
     * <li> For each method in the abstracted call graph and for each entry point build
     *      a finite state machine reflecting its transition structure w.r.t. all tracematches.
     *      This means that in the state machine there exists an edge <i>(q,l,p)</i> if
     *      the program can move from its global state <i>q</i> to <i>p</i> by causing
     *      an event that is matched by the symbol <i>l</i>.
     *      Further they hold a special edge <i>(q,i,p)</i> for each invoke statment <i>i</i>
     *      that leads from a program state <i>q</i> before the invocation to a state
     *      <i>p</i> after the invocation.
     *      Those state machines further have the special form that they have a unique
     *      starting and end state. (by means of epsilon transitions)
     * <li> Using the abstracted call graph, interprocedurally combine those automata by
     *      inlining the invoke edges: An edge <i>(q,i,p)</i> is replaced by epsilon edges
     *      leading from <i>q</i> to the unique starting nodes of the automata of 
     *      all possible callees of <i>i</i> plus epsilon transitions leading from all
     *      final states in those callee automata to <i>p</i>.
     * </ol>
     * Epsilon transitions and unreachable states are removed immediately whenever appropriate.
     */
    public boolean analyze() {
//    	PackManager.v().getPack("cg").apply();
//    	CallGraph callGraph = Scene.v().getCallGraph();
//    	
//    	Set reachable = WeavableMethods.v().getReachable(callGraph);
//    	for (Iterator methodIter = reachable.iterator(); methodIter.hasNext();) {
//			SootMethod m = (SootMethod) methodIter.next();
//			System.err.println(m);
//			for (Iterator localIter = m.getActiveBody().getLocals().iterator(); localIter.hasNext();) {
//				Local l= (Local) localIter.next();
//				String s = (Scene.v().getPointsToAnalysis().reachingObjects(l).isEmpty() ? "empty ": "full  ") + l.toString();
//				System.err.println(s);
//			}
//			
//		}
//    	System.exit(0);
    	gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();

		//nothing to do?
    	if(gai.getTraceMatches().size()==0) {
    		return false;
    	}
    	
    	for (Iterator tmIter = gai.getTraceMatches().iterator(); tmIter.hasNext();) {
			TraceMatch tm = (TraceMatch) tmIter.next();
			if(tm.isPerThread()) {
				System.err.println("Cannot currently handle perthread tracematches. Skipping analysis.");
				return false;
			}			
		}
    	
    	try {
    		doAnalyze();
    	} catch (Error e) {
    		Statistics.errorOccured = true;
    		throw e;
    	} catch (RuntimeException e) {
    		Statistics.errorOccured = true;
    		throw e;
    	} finally {
            //reeneable optimizations on straightlinecode (if they were previously enabled)
            reenableStraightlineCodeOptimizations();
    		Statistics.printFinalStatistics();
    		ShadowRegistry.v().dumpShadows();
    		//free all data structures
    		reset();
    	}
    	
		//we do not need to reweave right away
        return false;
    }

    /**
	 * Performs the actual analysis.
	 */
	protected void doAnalyze() {
		//see what was selected as last stage
		String laststage = OptionsParser.v().laststage();
		
		AbcTimer.mark("TMAnalysis (prelude)");

		//add tags to all statements matches by a shadow
		TMShadowTagger.v().apply();

		//this performs a quick test that can always be applied:
    	//we see if actually all of the per-symbol advice matched at some point;
    	//if one of them did not match, we remove all edges that
    	//hold this symbol; also, if then the final state becomes unreachable,
    	//we remove the tracematch entirely
    	QuickCheck.v().apply();
    	
    	if(!ShadowRegistry.v().enabledShadowsLeft() || laststage.equals("quick")) {
    		return;
    	}

        //build the abstracted call graph
        CallGraphAbstraction.v().apply();
        
    	if(!ShadowRegistry.v().enabledShadowsLeft()) {
    		return;
    	}

    	FlowInsensitiveAnalysis.v().apply();
    	//prune edges from the abstracted callgraph which become superflous due to shadows
    	//removed by the flow-insensitive analysis
    	CallGraphAbstraction.v().rebuildAbstractedCallGraph();
    	
//        if(Debug.v().tmShadowGroupDump) {
//        	shadowGroupDump();
//        }

    	IntraproceduralAnalysis.v().apply();
    	
    	
    	if(!ShadowRegistry.v().enabledShadowsLeft() || laststage.equals("flowins")) {
    		return;
    	}

    	if(Debug.v().dynaInstr) {
    		
    		DynamicInstrumenter.v().createClassesAndSetDynamicResidues();
    		
    	} else {

    		assert laststage.equals("flowsens");
        	
            //build a state machine reflecting the transition
            //structure of a method for each method
            //PerMethodStateMachines.v().apply();

            //aaply the flow-sensitive analysis
            //FlowSensitiveAnalysis.v().apply();

            //disable all shadows not having been marked as active
            //ShadowRegistry.v().disableAllInactiveShadows();
            
    	}
    	
        if(Debug.v().tmShadowGroupDump) {
        	ShadowGroupStatistics.v().computeAndDumpStatistics();
        }
	}

	/** 
     * {@inheritDoc}
     */
    public void defaultSootArgs(List sootArgs) {
    	//check command line arguments
    	checkArguments();    	
    	
    	if(!Debug.v().treatVariables) {
    		throw new RuntimeException("treatVariables always should be TRUE for the moment");
    	}
    	
        //if we shall treat free variables, make sure that we create edges which are capable of
        //holding free variable bindings
        if(Debug.v().treatVariables) {
        	TMStateMachine.setEdgeFactory(VariableSMEdgeFactory.v());
        } else {
        	TMStateMachine.setEdgeFactory(ShadowSMEdgeFactory.v());
        }

        //keep line numbers
        sootArgs.add("-keep-line-number");
    	//enable whole program mode
        sootArgs.add("-w");
        //disable all packs we do not need
        sootArgs.add("-p");
        sootArgs.add("wjtp");
        sootArgs.add("enabled:false");
        sootArgs.add("-p");
        sootArgs.add("wjop");
        sootArgs.add("enabled:false");
        sootArgs.add("-p");
        sootArgs.add("wjap");
        sootArgs.add("enabled:false");
        
    	//enable paddle points-to analysis
        sootArgs.add("-p");
        sootArgs.add("cg");
        sootArgs.add("enabled:true");
        
        if(Debug.v().onDemand) {
            configureSpark(sootArgs);        	
        } else {
            configurePaddle(sootArgs);
        }
        
		//in order to generate points-to sets for weaving variables, we have to
        //disable the straightlinecode optimizations which take place right
        //after weaving;
        //we store the old state and reset it after the analysis
        disableStraightlineCodeOptimizations();
    }
    
	/**
	 * @param sootArgs
	 */
	private void configureSpark(List sootArgs) {
        sootArgs.add("-p");
        sootArgs.add("cg.spark");
        sootArgs.add("enabled:true");
	}

	/**
	 * @param sootArgs
	 */
	protected void configurePaddle(List sootArgs) {
		//paddle
        sootArgs.add("-p");
        sootArgs.add("cg.paddle");
        sootArgs.add("enabled:true");
        sootArgs.add("-p");
        sootArgs.add("cg.paddle");
        sootArgs.add("bdd:enabled");
        sootArgs.add("-p");
        sootArgs.add("cg.paddle");
        sootArgs.add("backend:javabdd");
        
//        //context-options
//        sootArgs.add("-p");
//        sootArgs.add("cg.paddle");
//        sootArgs.add("context:objsens");        
//        sootArgs.add("-p");
//        sootArgs.add("cg.paddle");
//        sootArgs.add("context-heap");
//
//        //set factory in paddle so that we only produce context for certain variables
//        PaddleScene.v().overrideNodeManagerFactory(new NodeManagerFactory() {
//        	
//        	/**
//        	 * {@inheritDoc}
//        	 */
//        	public NodeManager createNodeManager(Qvar_method_type locals,
//        			Qvar_type globals, Qobj_method_type localallocs,
//        			Qobj_type globalallocs) {
//        		return new CustomizedContextSensitivityNodeManager(locals,globals,localallocs,globalallocs); 
//        	}
//        	
//        });
	}

	/**
	 * Checks for correct command line arguments.
	 */
	private void checkArguments() {
		assert OptionsParser.v().tmopt();
		String laststage = OptionsParser.v().laststage();
		if(!laststage.equals("quick") && !laststage.equals("flowsens") && !laststage.equals("flowins")) {
			throw new IllegalArgumentException("laststage is '"+laststage+"' but must be one of 'quick', 'flowins' or 'flowsens'");			
		}
	}

	/**
	 * Disables optimizations on straightline code after weaving.
	 */
	protected void disableStraightlineCodeOptimizations() {
		//store the old state
        originalState_cleanupAfterAdviceWeave = Debug.v().cleanupAfterAdviceWeave;
        //disable
        Debug.v().cleanupAfterAdviceWeave = false;
	}
    
	/**
	 * Reenables optimizations of straightline code (if they were enabled originally).
	 */
	protected void reenableStraightlineCodeOptimizations() {
		Debug.v().cleanupAfterAdviceWeave = originalState_cleanupAfterAdviceWeave;
	}
	
	/**
	 * Frees all singleton objects. 
	 */
	public static void reset() {
		ReachableShadowFinder.reset();
		ShadowRegistry.reset();
		TraceMatchByName.reset();
		WeavableMethods.reset();
		CallGraphAbstraction.reset();
		FlowInsensitiveAnalysis.reset();
		QuickCheck.reset();
		ShadowGroupRegistry.reset();
		TMShadowTagger.reset();
	}


}
