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

package soot.jimple.toolkits.callgraph;
import soot.*;
import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import java.util.*;
import jedd.*;

/** A Numberer for kinds of edges.
 * @author Ondrej Lhotak
 */

public final class KindNumberer implements Numberer {
    public KindNumberer( Singletons.Global g ) {}
    public static KindNumberer v() { return G.v().KindNumberer(); }

    public void add( Object o ) {
        throw new RuntimeException( "Can't add to KindNumberer" );
    }
    private Integer[] kind = {
        new Integer(0),
        new Integer(1),
        new Integer(2),
        new Integer(3),
        new Integer(4),
        new Integer(5),
        new Integer(6),
        new Integer(7),
        new Integer(8),
        new Integer(9),
        new Integer(10)
    };
    public Object get( int number ) {
        return kind[number];
    }
    public int get( Object o ) {
        return ((Integer)o).intValue();
    }
    public int size() {
        return kind.length;
    }
}

