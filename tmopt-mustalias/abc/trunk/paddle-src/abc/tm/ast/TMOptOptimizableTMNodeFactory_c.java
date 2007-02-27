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

import java.util.List;

import polyglot.ast.Block;
import polyglot.types.Flags;
import polyglot.util.Position;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.Pointcut;

/**
 * This class creates customized tracematch declarations and per-symbol advice declarations,
 * which in turn generate customized aspect-infos.
 *
 * @author Eric Bodden
 */
public class TMOptOptimizableTMNodeFactory_c extends TMNodeFactory_c {
	
	/**
	 * {@inheritDoc}
	 */
	public TMDecl TMDecl(Position pos, Position body_pos, TMModsAndType mods_and_type, String tracematch_name, List formals, List throwTypes, List symbols, List freqent_symbols, Regex regex, Block body) {
		return new TMOptTMDecl_c(pos, body_pos, mods_and_type, tracematch_name, formals,
				throwTypes, symbols, freqent_symbols, regex, body);
	}

	/**
	 * {@inheritDoc}
	 */
	public TMAdviceDecl PerSymbolAdviceDecl(Position pos, Flags flags, AdviceSpec spec, List throwTypes, Pointcut pc, Block body, String tm_id, SymbolDecl sym, Position tm_pos) {
		// TODO Auto-generated method stub
		return new TMOptPerSymbolAdviceDecl_c(pos, flags, spec, throwTypes, pc, body, tm_id,
				sym, tm_pos);
	}
	
}
