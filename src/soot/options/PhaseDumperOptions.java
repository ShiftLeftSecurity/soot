
/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Ondrej Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/* THIS FILE IS AUTO-GENERATED FROM soot_options.xml. DO NOT MODIFY. */

package soot.options;
import java.util.*;

/** Option parser for Phase Dumper. */
public class PhaseDumperOptions
{
    private Map options;

    public PhaseDumperOptions( Map options ) {
        this.options = options;
    }
    
    /** Enabled --
    
     * .
    
     * 
     */
    public boolean enabled() {
        return soot.PhaseOptions.getBoolean( options, "enabled" );
    }
    
    /** Dump CFGs along with phase --
    
     * Dumps visualizations of CFGs constructed during dumped phases..
    
     * If enabled (and if the CFG constructing classes were compiled 
     * with debugging enabled) any control flow graph constructed 
     * during a dumped phase will also be dumped, in the form of a file 
     * containing input to dot, the graphviz tool. Output dot files 
     * are stored beneath the soot output directory, in files with 
     * names like: 
     * className/methodSignature/phasename-graphType-number.dot, where 
     * number serves to distinguish graphs in phases that produce more 
     * than one (for example, the Aggregator may produce multiple 
     * CompleteUnitGraphs).
     */
    public boolean dumpcfgs() {
        return soot.PhaseOptions.getBoolean( options, "dumpcfgs" );
    }
    
}
        