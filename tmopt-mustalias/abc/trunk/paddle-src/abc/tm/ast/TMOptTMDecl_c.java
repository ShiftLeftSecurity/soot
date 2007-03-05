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
package abc.tm.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.MethodDecl;
import polyglot.ast.TypeNode;
import polyglot.types.CodeInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.MethodInstance;
import polyglot.util.Position;
import abc.aspectj.ast.Around;
import abc.aspectj.ast.Pointcut;
import abc.tm.visit.MoveTraceMatchMembers;
import abc.tm.weaving.aspectinfo.TMOptTraceMatch;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.aspectinfo.MethodSig;

/**
 * A customized tracematch declaration which instantiates a customized instance
 * of {@link TraceMatch}, which we need for added functionality.
 * 
 * @author Eric Bodden
 */
public class TMOptTMDecl_c extends TMDecl_c {

	public TMOptTMDecl_c(Position pos, Position body_pos,
			TMModsAndType mods_and_type, String tracematch_name, List formals,
			List throwTypes, List symbols, List frequent_symbols, Regex regex,
			Block body) {
		super(pos, body_pos, mods_and_type, tracematch_name, formals,
				throwTypes, symbols, frequent_symbols, regex, body);
	}

	/**
	 * {@inheritDoc}
	 */
	public List generateImplementationAdvice(TMNodeFactory nf, TypeNode voidn,
			MoveTraceMatchMembers visitor) {
		List advice = new LinkedList();
		Collection final_syms = regex.finalSymbols();
		Pointcut before = null;
		Pointcut after = null;

		Iterator j = symbols.iterator();

		while (j.hasNext()) {
			SymbolDecl sd = (SymbolDecl) j.next();

			makeSymbolAdvice(nf, advice, sd, voidn);
			Pointcut pc = sd.generateClosedPointcut(nf, formals);

			if (sd.kind() == SymbolKind.AFTER) {
				after = orPC(nf, after, pc);

				if (final_syms.contains(sd.name()))
					after_pc = orPC(nf, after_pc, pc);
			} else {
				before = orPC(nf, before, pc);

				if (final_syms.contains(sd.name())) {
					if (isAround)
						before_around_pc = sd.getPointcut();
					else
						before_around_pc = orPC(nf, before_around_pc, pc);
				}
			}
		}

		makeEventAdvice(nf, advice, before, after, voidn, "synch()",
				TMAdviceDecl.SYNCH);
		makeEventAdvice(nf, advice, before, after, voidn, "some()",
				TMAdviceDecl.SOME);

		return advice;
	}

	/**
	 * This method is just copied over from the super class. The only difference
	 * is that we create another advice info.
	 */
	public void update(GlobalAspectInfo gai, Aspect current_aspect) {
		//
		// create aspectinfo advice declarations
		//

		int jp_vars = thisJoinPointVariables();

		// list of what the formals will be for the body-advice
		// after the tracematch formals are removed.
		List transformed_formals = bodyAdviceFormals();
		for (int i = formals.size() - jp_vars; i < formals.size(); i++)
			transformed_formals.add(formals.get(i));

		int lastpos = transformed_formals.size();
		int jp = -1, jpsp = -1, ejp = -1;

		if (hasEnclosingJoinPointStaticPart)
			ejp = --lastpos;
		if (hasJoinPoint)
			jp = --lastpos;
		if (hasJoinPointStaticPart)
			jpsp = --lastpos;

		before_around_spec.setReturnType(returnType());
		if (after_spec != null)
			after_spec.setReturnType(returnType());

		List methods = new ArrayList();
		for (Iterator procs = methodsInAdvice.iterator(); procs.hasNext();) {
			CodeInstance ci = (CodeInstance) procs.next();

			if (ci instanceof MethodInstance)
				methods.add(AbcFactory.MethodSig((MethodInstance) ci));
			if (ci instanceof ConstructorInstance)
				methods.add(AbcFactory.MethodSig((ConstructorInstance) ci));
		}

		// create a signature for this method after transformation
		// in the backend (i.e. with only around tracematch formals)
		MethodSig sig = AbcFactory.MethodSig(this.formals(transformed_formals));

		if (before_around_pc != null) {
			abc.weaving.aspectinfo.AdviceDecl before_ad = new abc.tm.weaving.aspectinfo.TMAdviceDecl(
					before_around_spec.makeAIAdviceSpec(), before_around_pc
							.makeAIPointcut(), sig, current_aspect, jp, jpsp,
					ejp, methods, position(), name(), position(),
					TMAdviceDecl.BODY);

			gai.addAdviceDecl(before_ad);
		}

		if (after_pc != null) {
			abc.weaving.aspectinfo.AdviceDecl after_ad = new abc.tm.weaving.aspectinfo.TMAdviceDecl(
					after_spec.makeAIAdviceSpec(), after_pc.makeAIPointcut(),
					sig, current_aspect, jp, jpsp, ejp, methods, position(),
					tracematch_name, position(), TMAdviceDecl.BODY);

			gai.addAdviceDecl(after_ad);
		}

		MethodCategory.register(sig, MethodCategory.ADVICE_BODY);

		String proceed_name = null;

		if (isAround) {
			MethodDecl proceed = ((Around) before_around_spec).proceed();
			proceed_name = proceed.name();
			MethodCategory.register(proceed, MethodCategory.PROCEED);
		}

		//
		// Create aspectinfo tracematch
		//
		List tm_formals = weavingFormals(formals, true);
		List body_formals = weavingFormals(transformed_formals, false);

		// create TraceMatch
		TraceMatch tm = new TMOptTraceMatch(tracematch_name, tm_formals,
				body_formals, regex.makeSM(), isPerThread, orderedSymToVars(),
				frequent_symbols, sym_to_advice_name, synch_advice,
				some_advice, proceed_name, current_aspect, position());

		((TMGlobalAspectInfo) gai).addTraceMatch(tm);
	}

}
