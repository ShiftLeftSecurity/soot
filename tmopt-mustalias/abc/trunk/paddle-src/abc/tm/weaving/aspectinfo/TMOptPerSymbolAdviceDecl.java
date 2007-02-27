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
package abc.tm.weaving.aspectinfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.util.Position;
import soot.SootMethod;
import soot.Unit;
import soot.tagkit.Host;
import soot.util.Chain;
import abc.main.Debug;
import abc.main.Main;
import abc.soot.util.LocalGeneratorEx;
import abc.tm.weaving.weaver.tmanalysis.MatchingTMSymbolTag;
import abc.tm.weaving.weaver.tmanalysis.query.Naming;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.MethodSig;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.aspectinfo.Var;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.AdviceFormals;
import abc.weaving.matching.MatchingContext;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.WeavingVar;
import abc.weaving.weaver.WeavingContext;

/**
 * This is an extended {@link TMAdviceDecl} which tags hosts which it is applied to
 * (woven into). At the same time it keeps track of {@link WeavingVar}s which are
 * generated so that later on we can refer to those variables in the static
 * points-to analysis.
 * 
 * @author Eric Bodden
 */
public class TMOptPerSymbolAdviceDecl extends TMAdviceDecl {

    /**
     * A unique id for this symbol (unique per TM decl.).
     */
    protected String symbolId;

	/**
	 * A mapping from pointcut variables to the set of weaving variables
	 * that has been used to implement those pointcut variables.
	 */
	protected Map varToWeavingVars;

    /**
     * @param symId the name of the associated symbol
     * @see TMAdviceDecl#TMAdviceDecl(AdviceSpec, Pointcut, MethodSig, Aspect, int, int, int, List, Position, String, Position, int)
     */
    public TMOptPerSymbolAdviceDecl(AdviceSpec spec, Pointcut pc, MethodSig impl, Aspect aspct, int jp, int jpsp, int ejp, List methods, Position pos, String tm_id, Position tm_pos, String symId, int kind) {
        super(spec, pc, impl, aspct, jp, jpsp, ejp, methods, pos, tm_id, tm_pos, kind);
        symbolId = symId;
		varToWeavingVars = new HashMap();
    }
    
    /**
     * Returns the symbol id/name for which this advice is generated.
	 * @return the symbol id
	 */
	public String getSymbolId() {
		return symbolId;
	}
    
    /**
     * Returns a unique id for this symbol. IT is qualified by the name of
     * the declaring tracematch.
     */
    public String getQualifiedSymbolId() {
    	return Naming.uniqueSymbolID(tm_id, symbolId);
    }
    
    /** 
     * If we treat free variables in the static tracematch analysis, we have to
     * keep track on the variables that were woven.
     * So this methods returns a weaving environment which tracks woven variables
     * if treatment of free variables is enabled.
     */
    public WeavingEnv getWeavingEnv() {
    	if(Debug.v().treatVariables) {
    		//create a special instance that keeps track of weaving variables
    		return new VariableTrackingAdviceFormals(this);
    	} else {
    		return new AdviceFormals(this);
    	}
    }

	/**
	 * A special weaving environment which keeps track of the
	 * weaving variables that were used.
	 * @author Eric Bodden
	 */
	protected class VariableTrackingAdviceFormals extends AdviceFormals {
    	
		/**
		 * Creates a new instance.
		 * @param ad the surrounding advice declaration
		 */
		public VariableTrackingAdviceFormals(AdviceDecl ad) {
			super(ad);
		}
    	
		/** 
		 * Keeps track of the weaving var that was used.
		 * @param the variable for which we need a {@link WeavingVar}
		 * @param mc the surrounding matching context; this is necessary
		 * to have context information for the static tracematch analysis
		 * (of the form<i>This {@link WeavingVar} that we just produced for
		 * <code>this</code> symbol, where was that woven into?</i>)
		 * The mapping is stored in the host of the shadow match associated
		 * with that context.
		 */
		public WeavingVar getWeavingVar(Var v, MatchingContext mc) {
			//create the weaving var
			WeavingVar weavingVar = super.getWeavingVar(v, mc);
			//get the current mapping
			Set weavingVars = (Set) varToWeavingVars.get(v);
			//initialize if necessary
			if(weavingVars==null) {
				weavingVars = new HashSet();
				varToWeavingVars.put(v, weavingVars);
			}
			
			//at least those three parameters should not be null;
			//mc.getStatement() may be null, e.g. for body-shadows
			assert weavingVar!=null;
			assert mc.getSootClass()!=null;
			assert mc.getSootMethod()!=null;
			//we need to tag this host so it better be not null 
			assert mc.getShadowMatch().getHost()!=null;
			
			//tags the host the current shadow is associated with
			//with the information that this symbol matched
			//this shadow under the given variable mapping
			tagHost(mc.getShadowMatch().getHost(),v,weavingVar,mc.getShadowMatch().shadowId,mc.getSootMethod());
						
			//add the mapping
			weavingVars.add(weavingVar);
			
			//return as required by the contract for this method
			return weavingVar;			
		}

		/**
		 * Tags the host with the information that the given {@link WeavingVar} was created
		 * for the given {@link Var} for the shadow with the given id and for this symbol.
		 * @param h the host to tag
		 * @param v the variable we create the {@link WeavingVar} for
		 * @param weavingVar the {@link WeavingVar} that was created
		 * @param shadowId the id of the shadow where we are weaving into
		 * @param method 
		 */
		protected void tagHost(Host h, Var v, WeavingVar weavingVar, int shadowId, SootMethod method) {
			//currently we only support units and methods as host for those tags
			//(which does make sense because we have statement and body shadows)
			//to losen this requirement, extend UGStateMachine
			assert h instanceof Unit || h instanceof SootMethod;
			
			//rebind unit 
			if(h instanceof Unit) {
				h = Main.v().getAbcExtension().getWeaver().rebind((Unit)h);
			}
			
			//get the fully qualified, unique ID of this symbol
			String qualifiedSymbolId = getQualifiedSymbolId();
			
			//retrieve the tag if it already exists on this unit, otherwise
			//create a new one
			MatchingTMSymbolTag tag;
			if(h.hasTag(MatchingTMSymbolTag.NAME)) {
				tag = (MatchingTMSymbolTag) h.getTag(MatchingTMSymbolTag.NAME);
			} else {
				tag = new MatchingTMSymbolTag();
				h.addTag(tag);
			}
			//add the mapping to the tag
			tag.addMapping(qualifiedSymbolId,v,weavingVar,shadowId);
		}
		
    }

	/**
	 * The above ({@link VariableTrackingAdviceFormals}) mechanism of tagging hosts which were matched
	 * only works if this symbol actually binds variables. If not, the {@link VariableTrackingAdviceFormals}
	 * object is never called. Hence we have to tag here, as this method is called at each shadow
	 * which is woven into. 
	 */
	public Chain makeAdviceExecutionStmts(AdviceApplication adviceappl, LocalGeneratorEx localgen, WeavingContext wc) {
		Chain res = super.makeAdviceExecutionStmts(adviceappl, localgen, wc);
		
		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
		
		//determine the number of free variables for this symbol
		List freeVariables = null;
		for (Iterator tmIter = gai.getTraceMatches().iterator(); tmIter.hasNext();) {
			TraceMatch tm = (TraceMatch) tmIter.next();
			
			if(tm.getName().equals(getTraceMatchID())) {
				freeVariables = tm.getVariableOrder(getSymbolId());
				break;
			}
		}		
		assert freeVariables!=null;
		
		//if there are none, we have to add an appropriate tag here, because
		//the weaving environment above will never become active
		if(freeVariables.size()==0) {
			//get the host to tag
			Host h = adviceappl.shadowmatch.getHost();
			
			//currently we only support units and methods as host for those tags
			//(which does make sense because we have statement and body shadows)
			//to losen this requirement, extend UGStateMachine
			assert h instanceof Unit || h instanceof SootMethod;

			//retrieve the tag if it already exists on this unit, otherwise
			//create a new one
			MatchingTMSymbolTag tag;
			if(h.hasTag(MatchingTMSymbolTag.NAME)) {
				tag = (MatchingTMSymbolTag) h.getTag(MatchingTMSymbolTag.NAME);
			} else {
				tag = new MatchingTMSymbolTag();
				h.addTag(tag);
			}

			//add the tag
			tag.addMatchingSymbolWithoutVariables(getQualifiedSymbolId(),adviceappl.shadowmatch.shadowId);
		}
		
		//call super to do the actual work this method was originally intended for
		return res;
	}
}
