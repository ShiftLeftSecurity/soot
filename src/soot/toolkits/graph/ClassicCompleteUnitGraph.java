/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 John Jorgensen
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


package soot.toolkits.graph;

import soot.*;
import soot.util.*;
import java.util.*;
import soot.options.Options;


/**
 *  <p> Represents a CFG for a Body instance where the nodes are
 *  {@link Unit} instances, and where edges are a conservative
 *  indication of unexceptional and exceptional control
 *  flow.</p>
 *
 *  <p><tt>ClassicCompleteUnitGraph</tt> duplicates the results
 *  that would have been produced by Soot's {@link CompleteUnitGraph}
 *  in releases up to Soot 2.0.1. It is included solely for testing
 *  purposes, and should not be used in actual analyses.</p>
 *
 *  <p> There are two distinctions between the graphs produced by the
 *  <tt>ClassicCompleteUnitGraph</tt> and 
 *  <tt>PrunedUnitGraph</tt>:
 *  <ol>
 *
 *  <li><tt>PrunedUnitGraph</tt> only creates edges to a <tt>Trap</tt>
 *  handler for trapped <tt>Unit</tt>s that have the potential to
 *  throw the particular exception type caught by the handler,
 *  according to the {@link ThrowAnalysis} used to estimate which
 *  exceptions each {@link Unit} may throw..
 *  <tt>ClassicCompleteUnitGraph</tt> creates edges for all trapped
 *  <tt>Unit</tt>s, regardless of what exceptions they may throw.</li>
 *
 *  <li> When <tt>PrunedUnitGraph</tt> creates edges for a trapped
 *  <tt>Unit</tt> that may throw a caught exception, it adds edges
 *  from each predecessor of the excepting <tt>Unit</tt> to the
 *  handler. Only if the excepting <tt>Unit</tt> may have side effects
 *  does it also add an edge from the excepting <tt>Unit</tt> itself
 *  to the handler.  <tt>ClassicCompleteUnitGraph</tt>, on the other
 *  hand, always adds an edge from the excepting <tt>Unit</tt> itself
 *  to the handler, and adds edges from the predecessor only of the
 *  first <tt>Unit</tt> covered by a <tt>Trap</tt> (in this one aspect
 *  <tt>ClassicCompleteUnitGraph</tt> is less conservative than
 *  <tt>PrunedUnitGraph</tt>, since it ignores the possibility of a
 *  branch into the middle of a protected area.</li>
 *
 * </ol></p>
 */
public class ClassicCompleteUnitGraph extends TrapUnitGraph
{
    /**
     *  Constructs the graph from a given Body instance.
     *  @param the Body instance from which the graph is built.
     */
    public ClassicCompleteUnitGraph(Body body)
    {
	// The TrapUnitGraph constructor will use our buildExceptionalEdges:
        super(body);
    }


    /**
     * Method to compute the edges corresponding to exceptional
     * control flow. 
     *
     * @param unitToSuccs A {@link Map} from {@link Unit}s to {@link
     *                    List}s of {@link Unit}s. This is * an ``out
     *                    parameter''; <tt>buildExceptionalEdges</tt>
     *                    will add a mapping for every <tt>Unit</tt>
     *                    within the scope of one or more {@link
     *                    Trap}s to a <tt>List</tt> of the handler
     *                    units of those <tt>Trap</tt>s.
     *
     * @param unitToPreds A {@link Map} from {@link Unit}s to 
     *                    {@link List}s of {@link Unit}s. This is an
     *                    ``out parameter'';
     *                    <tt>buildExceptionalEdges</tt> will add a
     *                    mapping for every {@link Trap} handler to
     *                    all the <tt>Unit</tt>s within the scope of
     *                    that <tt>Trap</tt>.
     */
    protected void buildExceptionalEdges(Map unitToSuccs, Map unitToPreds) {
	// First, add the same edges as TrapUnitGraph.
	super.buildExceptionalEdges(unitToSuccs, unitToPreds);
	// Then add edges from the predecessors of the first
	// trapped Unit for each Trap.
	for (Iterator trapIt = body.getTraps().iterator(); 
	     trapIt.hasNext(); ) {
	    Trap trap = (Trap) trapIt.next();
	    Unit firstTrapped = trap.getBeginUnit();
	    Unit catcher = trap.getHandlerUnit();
	    // Make a copy of firstTrapped's predecessors to iterate over,
	    // just in case we're about to add new predecessors to this 
	    // very list, though that can only happen if the handler traps
	    // itself.  And to really allow for that
	    // possibility, we should iterate here until we reach a fixed
	    // point; but the old UnitGraph that we are attempting to
	    // duplicate did not do that, so we won't either.
	    List origPredsOfTrapped = new ArrayList(getPredsOf(firstTrapped));
	    for (Iterator unitIt = origPredsOfTrapped.iterator(); 
		 unitIt.hasNext(); ) {
		Unit pred = (Unit) unitIt.next();
		addEdge(unitToSuccs, unitToPreds, pred, catcher);
	    }
	}
    }
}
