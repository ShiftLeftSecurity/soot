/**
 * This class is a piece of test infrastructure. It compares various
 * varieties of control flow graphs.
 */

package soot.toolkits.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import soot.Body;
import soot.util.Chain;
import soot.BriefUnitPrinter;
import soot.CompilationDeathException;
import soot.LabeledUnitPrinter;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.toolkits.graph.CompleteUnitGraph.ExceptionDest;
import soot.util.ArraySet;

public class GraphComparer {

    /**
     * A utility class for keeping tracks of which nodes in the
     * graphs being compared are the same.  For {@link Block} graphs
     * we have to map from <tt>Block</tt>s containing a set of {@link Unit}s 
     * in one 

    /**
     * Checks if a graph's edge set is consistent.
     *
     * @param g the {@link DirectedGraph} whose edge set is to be 
     * checked.
     *
     * @return <tt>true</tt> if <tt>g</tt>'s edge set is consistent
     * (i.e., when <tt>x</tt> is recorded as a predecessor of <tt>y</tt> 
     * if and only if <tt>y</tt> is recorded as a successor of <tt>x</tt>).
     */
    public static boolean consistentGraph(DirectedGraph g) {
	for (Iterator nodeIt = g.iterator(); nodeIt.hasNext(); ) {
	    Object node = nodeIt.next();
	    for (Iterator predIt = g.getPredsOf(node).iterator(); 
		 predIt.hasNext(); ) {
		Object pred = predIt.next();
		if (! g.getSuccsOf(pred).contains(node)) {
		    return false;
		}
	    }
	    for (Iterator succIt = g.getSuccsOf(node).iterator(); 
		 succIt.hasNext(); ) {
		Object succ = succIt.next();
		if (! g.getPredsOf(succ).contains(node)) {
		    return false;
		}
	    }
	}
	return true;
    }


    /**
     * Compare two {@link DirectedGraph}s.
     *
     * @param g1 the first graph to compare.
     *
     * @param g2 the second graph to compare.
     *
     * @return <tt>true</tt> if <tt>g1</tt> and <tt>g2</tt> have
     * the same nodes, the same lists of heads and tails, and the
     * same sets of edges.
     */
    public static boolean equal(DirectedGraph g1, DirectedGraph g2) {
	if ((! consistentGraph(g1)) || (! consistentGraph(g2))) {
	    throw new CompilationDeathException(
		CompilationDeathException.COMPILATION_ABORTED,
		"Cannot compare inconsistent graphs");
	}
	if (g1.size() != g2.size()) {
	    return false;
	}
	if (! (g1.getHeads().containsAll(g2.getHeads())
	       && g2.getHeads().containsAll(g1.getHeads())) ) {
	    return false;
	}
	if (! (g1.getTails().containsAll(g2.getTails())
	       && g2.getTails().containsAll(g1.getTails())) ) {
	    return false;
	}
	for (Iterator g1it = g1.iterator(); g1it.hasNext(); ) {
	    Unit g1node = (Unit) g1it.next();
	    try {
		if (! (g1.getSuccsOf(g1node).containsAll(g2.getSuccsOf(g1node)) &&
		       g2.getSuccsOf(g1node).containsAll(g1.getSuccsOf(g1node)))) {
		    return false;
		}
		if (! (g1.getPredsOf(g1node).containsAll(g2.getPredsOf(g1node)) &&
		       g2.getPredsOf(g1node).containsAll(g1.getPredsOf(g1node)))) {
		    return false;
		}
	    } catch (RuntimeException e) {
		if (e.getMessage() != null && 
		    e.getMessage().startsWith("Invalid unit ")) {
		    return false;
		} else {
		    throw e;
		}
	    }
	}
	return true;
    }


    /**
     * Compare a {@link ClassicCompleteUnitGraph} to a {@link
     * CompleteUnitGraph}.
     *
     * @param classic the {@link ClassicCompleteUnitGraph} to compare.
     *
     * @param complete the {@link CompleteUnitGraph} to compare.
     *
     * @return <tt>true</tt> if <tt>classic</tt> and <tt>complete</tt>
     * differ only in the ways that we would expect a {@link
     * ClassicCompleteUnitGraph} to differ from a {@link
     * CompleteUnitGraph} for the same {@link Body}.
     */
    public static boolean onlyExpectedDiffs(ClassicCompleteUnitGraph classic, 
					    CompleteUnitGraph complete) {
System.err.println("onlyExpectedDiffs(): called");
	if (classic.size() != complete.size()) {
System.err.println("onlyExpectedDiffs(): sizes differ" + classic.size() + " " + complete.size());
	    return false;
	}
	// Could amend heads comparison to allow ClassicCompleteUnitGraph
	// to have unreachable handlers in its heads, but let's see if 
	// there are any.
	Set onlyClassic = new ArraySet();
	
	if (! (classic.getHeads().containsAll(complete.getHeads())
	       && complete.getHeads().containsAll(classic.getHeads())) ) {
System.err.println("onlyExpectedDiffs(): heads differ");
	    return false;
	}
	if (! (complete.getTails().containsAll(classic.getTails()))) {
	    return false;
	}
	for (Iterator tailIt = complete.getTails().iterator(); 
	     tailIt.hasNext(); ) {
	    Object tail = tailIt.next();
	    if ((! classic.getTails().contains(tail)) &&
		(! trappedReturn(complete, classic, tail))) {
System.err.println("onlyExpectedDiffs(): " + tail.toString() + " is not a tail in classic, but not a trapped Return either");
		return false;
	    }
	}

	// Since we've already confirmed that the two graphs have
	// the same number of nodes, we only need to iterate through
	// the nodes of one of them --- if they don't have exactly the same
	// set of nodes, this single iteration will reveal some
	// node in classic that is not in complete.
	for (Iterator nodeIt = classic.iterator(); nodeIt.hasNext(); ) {
	    Unit node = (Unit) nodeIt.next();
	    try {
		List classicSuccs = classic.getSuccsOf(node);
		List completeSuccs = complete.getSuccsOf(node);
		for (Iterator it = classicSuccs.iterator(); it.hasNext(); ) {
		    Unit classicSucc = (Unit) it.next();
		    if ((! completeSuccs.contains(classicSucc)) &&
			(! cannotReallyThrowTo(complete, node, classicSucc))) {
System.err.println("onlyExpectedDiffs(): " + classicSucc.toString() + " is not CompletueUnitGraph successor of " + node.toString() + " even though " + node.toString() + " can throw to it");
			return false;
		    }
		}
		for (Iterator it = completeSuccs.iterator(); it.hasNext(); ) {
		    Unit completeSucc = (Unit) it.next();
		    if ((! classicSuccs.contains(completeSucc)) &&
			(! predOfMidTrapThrower(complete, node, completeSucc))) {
System.err.println("onlyExpectedDiffs(): " + completeSucc.toString() + " is not ClassicUnitGraph successor of " + node.toString() + " even though " + node.toString() + " is not a predOfMidTrapThrower");
			return false;
		    }
		}
		List classicPreds = classic.getPredsOf(node);
		List completePreds = complete.getPredsOf(node);
		for (Iterator it = classicPreds.iterator(); it.hasNext(); ) {
		    Unit classicPred = (Unit) it.next();
		    if ((! completePreds.contains(classicPred)) &&
			(! cannotReallyThrowTo(complete, classicPred, node))) {
System.err.println("onlyExpectedDiffs(): " + classicPred.toString() + " is not CompletueUnitGraph predecessor of " + node.toString() + " even though " + classicPred.toString() + " can throw to " + node.toString());
			return false;
		    }
		}
		for (Iterator it = completePreds.iterator(); it.hasNext(); ) {
		    Unit completePred = (Unit) it.next();
		    if ((! classicPreds.contains(completePred)) &&
			(! predOfMidTrapThrower(complete, completePred, node))) {
System.err.println("onlyExpectedDiffs(): " + completePred.toString() + " is not ClassicUnitGraph predecessor of " + node.toString() + " even though " + completePred.toString() + " is not a predOfMidTrapThrower");
			return false;
		    }
		}
			
	    } catch (RuntimeException e) {
e.printStackTrace(System.err);
		if (e.getMessage() != null && 
		    e.getMessage().startsWith("Invalid unit ")) {
System.err.println("onlyExpectedDiffs(): " + node.toString() + " is not in CompleteUnitGraph at all");
		    // node is not in complete graph.
		    return false;
		} else {
		    throw e;
		}
	    }
	}
	return true;
    }

    /**
     * A utility method for confirming that a node which is considered
     * a tail by a {@link CompleteUnitGraph} but not by the 
     * corresponding {@link ClassicCompleteUnitGraph} is a return instruction
     * with a {@link Trap} handler as its successor in the
     * {@link ClassicCompleteUnitGraph}.
     *
     * @param complete the {@link CompleteUnitGraph}
     *
     * @param classic the {@link ClassicCompleteUnitGraph}
     *
     * @param node the node which is considered a tail by <tt>complete</tt>
     * but not by <tt>classic</tt>.
     *
     * @return <tt>true</tt> if <tt>node</tt> is a return instruction
     * which has a trap handler as its successor in <tt>classic</tt>.\
     */
    private static boolean trappedReturn(CompleteUnitGraph complete, 
					 ClassicCompleteUnitGraph classic, 
					 Object node) {
	if (! ((node instanceof soot.jimple.ReturnStmt) ||
	       (node instanceof soot.jimple.ReturnVoidStmt) ||
	       (node instanceof soot.baf.ReturnInst) ||
	       (node instanceof soot.baf.ReturnVoidInst))) {
	    return false;
	}
	List succsUnaccountedFor = new ArrayList(classic.getSuccsOf(node));
	if (succsUnaccountedFor.size() <= 0) {
	    return false;
	}
	for (Iterator trapIt = classic.getBody().getTraps().iterator();
	     trapIt.hasNext(); ) {
	    Trap trap = (Trap) trapIt.next();
	    succsUnaccountedFor.remove(trap.getHandlerUnit());
	}
	return (succsUnaccountedFor.size() == 0);
    }


    /**
     * A utility method for confirming that an edge which is in a 
     * {@link ClassicCompleteUnitGraph} but not the corresponding
     * {@link CompleteUnitGraph} satisfies the criterion we would
     * expect of such an edge: that it leads from a unit to a handler, 
     * that the unit might be expected to trap to the handler based
     * solely on the range of Units trapped by the handler, but that there
     * is no way for the particular exception that the handler catches
     * to be thrown at this point.
     *
     * @param g the CompleteUnitGraph associated with the units.
     * @param head the graph node that the edge leaves.
     * @param tail the graph node that the edge leads to.
     * @return true if <tt>head</tt> cannot really be associated with
     * an exception that <tt>tail</tt> catches, false otherwise.
     */
    private static boolean cannotReallyThrowTo(CompleteUnitGraph g,
					       Unit head, Unit tail) {
	// First, ensure that tail is a handler.
	Trap tailsTrap = null;
	for (Iterator it = g.getBody().getTraps().iterator(); it.hasNext(); ) {
	    Trap trap = (Trap) it.next();
	    if (trap.getHandlerUnit() == tail) {
		tailsTrap = trap;
		break;
	    }
	}
	if (tailsTrap == null) {
System.err.println("cannotReallyThrowTo(" +
		   head.toString() + "," +
		   tail.toString() + ") tail is not a handler");
	    return false;
	}

	// Next check if head is in the trap's protected area, but
	// either cannot throw an exception that the trap catches, or
	// is not included in CompleteUnitGraph because it has no
	// side-effects:
	Collection headsDests = g.getExceptionDests(head);
	if (amongTrappedUnits(head, tailsTrap, g.getBody()) &&
	    ((! destCollectionIncludes(headsDests, tailsTrap)) ||
	     (! CompleteUnitGraph.mightHaveSideEffects(head)))) {
	    return true;
	}

	// Finally, check if one of head's successors is the
	// first unit in tail's protected area, but that 
	// unit does not throw an exception to tail.
	List headsSuccs = g.getSuccsOf(head);
	Unit tailsFirstTrappedUnit = tailsTrap.getBeginUnit();
	if (headsSuccs.contains(tailsFirstTrappedUnit)) {
	    Collection succsDests = g.getExceptionDests(tailsFirstTrappedUnit);
	    if (! destCollectionIncludes(succsDests, tailsTrap)) {
		return true;
	    }
	}
System.err.println("cannotReallyThrowTo(" +
		   head.toString() + "," +
		   tail.toString() + ") returns false");
	return false;
    }


    /**
     * A utility method for determining if a {@link Unit} is among
     * those protected by a particular {@link Trap}..
     *
     * @param unit the {@link Unit} being asked about.
     *
     * @param trap the {@link Trap} being asked about.
     *
     * @param body the {@link Body} containing <tt>unit</tt> and
     * <tt>trap</tt>
     *
     * @return <tt>true</tt> if <tt>unit</tt> is protected by
     * <tt>trap</tt>, false otherwise.
     */
    private static boolean amongTrappedUnits(Unit unit, Trap trap, Body body) {
	Chain units = body.getUnits();
	for (Iterator it = units.iterator(trap.getBeginUnit(),
					  units.getPredOf(trap.getEndUnit()));
	     it.hasNext(); ) {
	    Unit u = (Unit) it.next();
	    if (u == unit) {
		return true;
	    }
	}
	return false;
    }

    /**
     * A utility method for determining if a {@link Collection}
     * of {@link CompleteUnitGraph#ExceptionDest} contains
     * one which leads to a specified {@link Trap}.
     *
     * @param dests the {@link Collection} of {@link
     * CompleteUnitGraph#ExceptionDest}s to search.
     *
     * @param trap the {@link Trap} to search for.
     *
     * @return <tt>true</tt> if <tt>dests</tt> contains 
     * <tt>trap</tt> as a destination, false otherwise.
     */
    private static boolean destCollectionIncludes(Collection dests, Trap trap) {
	for (Iterator destIt = dests.iterator(); destIt.hasNext(); ) {
	    ExceptionDest dest = (ExceptionDest) destIt.next();
	    if (dest.trap() == trap) {
		return true;
	    }
	}
	return false;
    }
	    

    /**
     * A utility method for confirming that an edge which is in a
     * {@link CompleteUnitGraph} but not the corresponding {@link
     * ClassicCompleteUnitGraph} satisfies the criterion we would
     * expect of such an edge: that tail of the edge is a {@link Trap}
     * handler, and that the head of the edge is not itself in the
     * <tt>Trap</tt>'s protected area, but is the predecessor of a
     * {@link Unit}, call it <tt>u</tt>, such that <tt>u</tt> is a
     * <tt>Unit</tt> in the <tt>Trap</tt>'s protected area, but not
     * the first one, and <tt>u</tt> might throw an exception that the
     * <tt>Trap</tt> catches.
     *
     * @param g the CompleteUnitGraph associated with the units.
     * @param head the graph node that the edge leaves.
     * @param tail the graph node that the edge leads to.
     * @return true if <tt>head</tt> is an untrapped predecessor of
     * a unit protected by <tt>tail</tt>, other than the first one,
     * which might throw an exception caught by <tt>tail</tt>
     */
    private static boolean predOfMidTrapThrower(CompleteUnitGraph g,
						Unit head, Unit tail) {
	// First, ensure that tail is a handler.
	Trap tailsTrap = null;
	for (Iterator it = g.getBody().getTraps().iterator(); it.hasNext(); ) {
	    Trap trap = (Trap) it.next();
	    if (trap.getHandlerUnit() == tail) {
		tailsTrap = trap;
		break;
	    }
	}
	if (tailsTrap == null) {
System.err.println("predOfMidTrapThrower(): " + tail.toString() + " is not a trap handler");
	    return false;
	}

	// Build a list of Units, other than head itself, which, if
	// they threw a caught exception, would cause
	// CompleteUnitGraph to add an edge from head to the catcher:
	List possibleThrowers = new ArrayList(g.getSuccsOf(head));
	possibleThrowers.remove(tail); // This method wouldn't have been
	possibleThrowers.remove(head); // called if tail was not in
				       // g.getSuccsOf(head).
	
	// Now ensure that one of those possibleThrowers might throw
	// an exception caught by tailsTrap.
	for (Iterator throwerIt = possibleThrowers.iterator(); 
	     throwerIt.hasNext(); ) {
	    Unit thrower = (Unit) throwerIt.next();
	    Collection dests = g.getExceptionDests(thrower);
	    for (Iterator destIt = dests.iterator(); destIt.hasNext(); ) {
		ExceptionDest dest = (ExceptionDest) destIt.next();
		if (dest.trap() == tailsTrap) {
		    return true;
		}
	    }
	}
System.err.println("predOfMidTrapThrower(): " + head.toString() + "'s successors do not trap to " + tail.toString());
	return false;
    }
	

    private final static String diffMarker = " ***";


    /**
     * Utility method to return the {@link Body} associated with a 
     * {@link DirectedGraph}, if there is one.
     *
     * @param g the graph for which to return a {@link Body}.
     *
     * @return the {@link Body} represented by <tt>g</tt>, if <tt>g</tt>
     * is a control flow graph, or <tt>null</tt> if <tt>g</tt> is not a
     * control flow graph.
     */
    private static Body getGraphsBody(DirectedGraph g) {
	Body result = null;
	if (g instanceof UnitGraph) {
	    result = ((UnitGraph) g).getBody();
	} else if (g instanceof BlockGraph) {
	    result = ((BlockGraph) g).getBody();
	}
	return result;
    }


    /**
     * Utility method to return a short string label identifying a graph.
     *
     * @param g the graph for which to return a label.
     *
     * @return the method signature associated with <tt>g</tt>, if <tt>g</tt>
     * is a control flow graph, or an arbitrary identifying string if 
     * <tt>g</tt> is not a control flow graph.
     */
    private static String graphToStringLabel(DirectedGraph g) {
	StringBuffer result = new StringBuffer(g.getClass().getName());
	Body b = getGraphsBody(g);
	if (b != null) {
	    result.append('(');
	    result.append(b.getMethod().getSignature());
	    result.append(')');
	}
	return b.toString();
    }


    /**
     * Utility method that returns a {@link LabeledUnitPrinter} for printing
     * the {@link Unit}s in graph.
     *
     * @param g the graph for which to return a {@link LabeledUnitPrinter}.
     * @return A {@link LabeledUnitPrinter} for printing the {@link Unit}s in
     * <tt>g</tt> if <tt>g</tt> is a control flow graph. Returns 
     * <tt>null</tt> if <tt>g</tt> is not a control flow graph.
     */
    private static LabeledUnitPrinter makeUnitPrinter(DirectedGraph g) {
	Body b = getGraphsBody(g);
	if (b == null) {
	    return null;
	} else {
	    BriefUnitPrinter printer = new BriefUnitPrinter(b);
	    printer.noIndent();
	    return printer;
	}
    }


    /**
     * Utility method to return a {@link String} representation of a
     * graph node.
     *
     * @param node an {@link Object} representing a node in a
     *             {@link DirectedGraph}.
     *
     * @param printer either a {@link LabeledUnitPrinter} for printing the
     * {@link Unit}s in the graph represented by <tt>node</tt>'s
     * graph, if node is part of a control flow graph, or <tt>null</tt>, if
     * <tt>node</tt> is not part of a control flow graph.
     *
     * @return a {@link String} representation of <tt>node</tt>.
     */
    private static String nodeToString(Object node, 
				       LabeledUnitPrinter printer) {
	String result = null;
	if (printer == null) {
	    result = node.toString();
	} else if (node instanceof Unit) {
	    ((Unit) node).toString(printer);
	    result = printer.toString();
	} else if (node instanceof Block) {
	    Block b = (Block) node;
	    StringBuffer buffer = new StringBuffer();
	    Iterator units = ((Block) node).iterator();
	    while (units.hasNext()) {
		Unit unit = (Unit) units.next();
		String targetLabel = (String) printer.labels().get(unit);
		if (targetLabel != null) {
		    buffer.append(targetLabel)
			.append(": ");
		}
		unit.toString(printer);
		buffer.append(printer.toString()).append("; ");
	  }
	  result = buffer.toString();
	}
	return result;
    }


    /**
     * Report the differences between two {@link DirectedGraph}s.
     *
     * @param g1 the first graph to compare.
     *
     * @param label1 a string to be used to identify <tt>g1</tt> in the
     * report of differences.
     *
     * @param g2 the second graph to compare.
     *
     * @param label2 a string to be used to identify <tt>g2</tt> in the
     * report of differences.
     *
     * @return a {@link String} summarizing the differences 
     * between the two graphs.
     */
    public static String diff(DirectedGraph g1, String label1,
			      DirectedGraph g2, String label2) {
	StringBuffer report = new StringBuffer();
	LabeledUnitPrinter printer1 = makeUnitPrinter(g1);
	LabeledUnitPrinter printer2 = makeUnitPrinter(g2);
	diffList(report, printer1, printer2, "HEADS", 
		 g1.getHeads(), g2.getHeads());
	diffList(report, printer1, printer2, "TAILS", 
		 g1.getTails(), g2.getTails());
	       
	for (Iterator g1it = g1.iterator(); g1it.hasNext(); ) {
	    Object g1node = g1it.next();
	    String g1string = nodeToString(g1node, printer1);
	    List g1succs = g1.getSuccsOf(g1node);
	    List g1preds = g1.getPredsOf(g1node);
	    List g2succs = null;
	    List g2preds = null;

	    try {
		g2preds = g2.getPredsOf(g1node);
	    } catch (RuntimeException e) {
		if (e.getMessage() != null && 
		    e.getMessage().startsWith("Invalid unit ")) {
		    // No preds entry for g1node in g2
		} else {
		    throw e;
		}
	    }
	    diffList(report, printer1, printer2, g1string + " PREDS", 
		     g1preds, g2preds);

	    try {
		g2succs = g2.getSuccsOf(g1node);
	    } catch (RuntimeException e) {
		if (e.getMessage() != null && 
		    e.getMessage().startsWith("Invalid unit ")) {
		    // No succs entry for g1node in g2
		} else {
		    throw e;
		}
	    }
	    diffList(report, printer1, printer2, g1string + " SUCCS", 
		     g1succs, g2succs);

	    
	}

	for (Iterator g2it = g2.iterator(); g2it.hasNext(); ) {
	    // In this loop we Only need to report the cases where
	    // there is no g1 entry at all, cases where there is an
	    // entry in both graphs but with differing lists of preds
	    // or succs will have already been reported by the loop over
	    // g1's nodes.
	    Object g2node = g2it.next();
	    String g2string = nodeToString(g2node, printer2);
	    List g2succs = g2.getSuccsOf(g2node);
	    List g2preds = g2.getPredsOf(g2node);

	    try {
		g1.getPredsOf(g2node);
	    } catch (RuntimeException e) {
		if (e.getMessage() != null && 
		    e.getMessage().startsWith("Invalid unit ")) {
		    diffList(report, printer1, printer2, 
			     g2string  + " PREDS",
			     null, g2preds);
		} else {
		    throw e;
		}
	    }

	    try {
		g1.getSuccsOf(g2node);
	    } catch (RuntimeException e) {
		if (e.getMessage() != null && 
		    e.getMessage().startsWith("Invalid unit ")) {
		    diffList(report, printer1, printer2, 
			     g2string + " SUCCS" ,
			     null, g2succs);
		} else {
		    throw e;
		}
	    }
	}

	if (report.length() > 0) {
	    String leader1 = "*** ";
	    String leader2 = "\n--- ";
	    String leader3 = "\n";
	    StringBuffer result = new StringBuffer(leader1.length() + 
						   label1.length() + 
						   leader2.length() +
						   label2.length() +
						   leader3.length() +
						   report.length());
	    result.append(leader1)
		.append(label1)
		.append(leader2)
		.append(label2)
		.append(leader3)
		.append(report);
	    return result.toString();
	} else {
	    return "";
	}
    }


    /**
     * A utility method for reporting the differences between two lists 
     * of graph nodes. The lists are usually the result of calling
     * <tt>getHeads()</tt>, <tt>getTails()</tt>, <tt>getSuccsOf()</tt>
     * or <tt>getPredsOf()</tt> on each of two graphs being compared.
     *
     * @param buffer a {@link StringBuffer} to which to append the 
     * description of differences.
     *
     * @param printer1 a {@link LabeledUnitPrinter} to be used to format
     * any {@link Unit}s found in <tt>list1</tt>.
     *
     * @param printer2 a {@link LabeledUnitPrinter} to be used to format
     * any {@link Unit}s found in <tt>list2</tt>.
     * 
     * @param label a string characterizing these lists.
     *
     * @param list1 the list from the first graph, or <tt>null</tt> if
     * this list is missing in the first graph.
     *
     * @param list2 the list from the second graph, or <tt>null</tt> if
     * this list is missing in the second graph.
     */
    private static void diffList(StringBuffer buffer, 
				 LabeledUnitPrinter printer1, 
				 LabeledUnitPrinter printer2,
				 String label, List list1, List list2) {
	if (! (list1.containsAll(list2) && list2.containsAll(list1))) {
	    buffer.append("*********\n");
	    if (list1 == null) {
		buffer.append("+ ");
		list1 = Collections.EMPTY_LIST;
	    } else if (list2 == null) {
		buffer.append("- ");
		list2 = Collections.EMPTY_LIST;
	    } else {
		buffer.append("  ");
	    }
	    buffer.append(label)
		.append(":\n");
	    for (Iterator it = list1.iterator(); it.hasNext(); ) {
		Object node = it.next();
		if (list2.contains(node)) {
		    buffer.append("      ");
		} else {
		    buffer.append("-     ");
		}
		buffer.append(nodeToString(node,printer1)).append("\n");
	    }
	    for (Iterator it = list2.iterator(); it.hasNext(); ) {
		Object node = it.next();
		if (! list1.contains(node)) {
		    buffer.append("+     ")
			.append(nodeToString(node,printer2))
			.append("\n");
		}
	    }
	    buffer.append("---------\n");
	}

    }
}
