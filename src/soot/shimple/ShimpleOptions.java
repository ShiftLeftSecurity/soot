/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Navindra Umanee
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

/* THIS FILE IS AUTO-GENERATED FROM options. DO NOT MODIFY */



package soot.shimple;
import java.util.*;
import soot.*;

/**
 * Various options regulating the functioning of Shimple.
 *
 * @author Navindra Umanee
 **/
public class ShimpleOptions
{
    protected Map options;

    public ShimpleOptions(Map options) {
        this.options = options;
        checkOptions();
    }

    public ShimpleOptions() {
        this.options = Scene.v().computePhaseOptions("shimple", getDefaultOptions());
        checkOptions();
    }


/*********************************************************************
*** General Options
*********************************************************************/

    /**
     * When this option is set to true, Shimple-based optimizations are applied.
     * Default value is false.
     **/
    public boolean optimize() {
        return Options.getBoolean( options, "optimize" );
    }


/*********************************************************************
*** Options for Building Shimple
*********************************************************************/

    /**
     * If set to true, SSA will be computed for scalars only (PrimType).  Otherwise,
     * SSA is computed for both scalars (PrimType) and objects (RefType. Note: 
     * object aliasing is not taken into account).  Arrays are not handled either
     * way.
     * Default value is false.
     **/
    public boolean scalars_only() {
        return Options.getBoolean( options, "scalars-only" );
    }


/*********************************************************************
*** Options for Leaving Shimple
*********************************************************************/

    /**
     * If set to true, neither pre-optimization nor post-optimization will be applied
     * to the Phi elimination process.  Note that setting this option to true takes
     * precedence over other Phi elimination options.
     * Default value is false.
     **/
    public boolean naive_phi_elimination() {
        return Options.getBoolean( options, "naive-phi-elimination" );
    }

    /**
     * If set to true, some optimizations are applied before Phi nodes are eliminated.
     * Default value is false.
     **/
    public boolean pre_optimize_phi_elimination() {
        return Options.getBoolean( options, "pre-optimize-phi-elimination" );
    }

    /**
     * If set to false, optimizations are not applied after eliminating Phi nodes.
     * Default value is true.
     **/
    public boolean post_optimize_phi_elimination() {
        return Options.getBoolean( options, "post-optimize-phi-elimination" );
    }

    public void checkOptions() {
        Options.checkOptions(options, "shimple", getDeclaredOptions());
    }
    public static String getDeclaredOptions() {
        return
        " optimize scalars-only naive-phi-elimination pre-optimize-phi-elimination post-optimize-phi-elimination";
    }
    public static String getDefaultOptions() {
        return
        " optimize:false scalars-only:false naive-phi-elimination:false pre-optimize-phi-elimination:false post-optimize-phi-elimination:true";
    }
}

