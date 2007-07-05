/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
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
package abc.tm.weaving.weaver.tmanalysis.mustalias;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.toolkits.graph.UnitGraph;

/**
 * 
 *
 * @author Eric Bodden
 */
public class LoopAwareLocalMustAliasAnalysis extends LocalMustAliasAnalysis {

    protected Set<Integer> invalidInstanceKeys;
    
    public LoopAwareLocalMustAliasAnalysis(UnitGraph g) {
        super(g);
        invalidInstanceKeys = new HashSet<Integer>();
    }
    
    public void addLocalAssignedExpressionTwice(Local l, Unit stmtAfterAssignStmt) {
        Object key = ((HashMap)getFlowBefore(stmtAfterAssignStmt)).get(l);
        if(key!=UNKNOWN) {
            invalidInstanceKeys.add((Integer) key);
        }        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mustAlias(Local l1, Stmt s1, Local l2, Stmt s2) {
        Object l1n = ((HashMap)getFlowBefore(s1)).get(l1);
        Object l2n = ((HashMap)getFlowBefore(s2)).get(l2);

        if (l1n == UNKNOWN || l2n == UNKNOWN ||
            invalidInstanceKeys.contains(l2n) || invalidInstanceKeys.contains(l2n))
            return false;

        return l1n == l2n;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String instanceKeyString(Local l, Stmt s) {
        Object ln = ((HashMap)getFlowBefore(s)).get(l);
        if(invalidInstanceKeys.contains(ln)) {
            return UNKNOWN_LABEL;
        }
        return super.instanceKeyString(l, s);
    }

}
