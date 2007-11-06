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

import soot.Local;
import soot.PointsToSet;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.toolkits.pointer.FullObjectSet;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.jimple.toolkits.pointer.LocalNotMayAliasAnalysis;

/**
 * InstanceKeyNonRefLikeType
 * TODO extract interface so that this does not have to be a subclass of {@link InstanceKey}
 *
 * @author Eric Bodden
 */
public class InstanceKeyNonRefLikeType extends InstanceKey {

    public InstanceKeyNonRefLikeType(Local assignedLocal,
            Stmt stmtAfterAssignStmt, SootMethod owner,
            LocalMustAliasAnalysis lmaa, LocalNotMayAliasAnalysis lnma) {
        super(assignedLocal, stmtAfterAssignStmt, owner, lmaa, lnma);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mustAlias(InstanceKey otherKey) {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mayNotAlias(InstanceKey otherKey) {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PointsToSet getPointsToSet() {
        return FullObjectSet.v();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "NonRefLikeType";
    }
    
}
