/* Soot - a J*va Optimization Framework
 * Copyright (C) 2002 Sable Research Group
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

/*
 * Modified by the Sable Research Group and others 2002-2003.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */


package soot.tools;

import soot.util.dot.*;
import soot.util.cfgcmd.CFGOptionMatcher;
import soot.util.cfgcmd.CFGGraphType;
import soot.util.cfgcmd.CFGIntermediateRep;
import soot.util.cfgcmd.AltClassLoader;
import soot.*;
import soot.toolkits.graph.*;
import soot.util.*;
import soot.exceptions.*;
import java.util.*;
import soot.jimple.JimpleBody;
import soot.baf.Baf;
import soot.grimp.Grimp;
import soot.shimple.Shimple;
import java.lang.reflect.Method;

/**
 * A utility class for generating dot graph file for a control flow graph
 *
 * @author Feng Qian
 */
public class CFGViewer extends BodyTransformer 
  implements CFGGraphType.Viewer {

  /**
   * An enumeration type for representing the ThrowAnalysis to use. 
   */
  abstract class ThrowAnalysisOption extends CFGOptionMatcher.CFGOption {
    ThrowAnalysisOption(String name) {
      super(name);
    }
    abstract ThrowAnalysis getAnalysis();
  }

  private final ThrowAnalysisOption PEDANTIC_THROW_ANALYSIS = 
    new ThrowAnalysisOption("pedantic") {
    ThrowAnalysis getAnalysis() { 
      return new PedanticThrowAnalysis(); 
    }
  };

  private final ThrowAnalysisOption UNIT_THROW_ANALYSIS = new ThrowAnalysisOption("unit") {
    ThrowAnalysis getAnalysis() { 
      return new UnitThrowAnalysis(); 
    }
  };

  private final CFGOptionMatcher throwAnalysisOptions = 
    new CFGOptionMatcher(new ThrowAnalysisOption[] {    
      PEDANTIC_THROW_ANALYSIS,
      UNIT_THROW_ANALYSIS,
    });
  private ThrowAnalysis getThrowAnalysis(String option) {
    return ((ThrowAnalysisOption) 
	    throwAnalysisOptions.match(option)).getAnalysis();
  }

  private CFGGraphType graphtype = CFGGraphType.BRIEF_UNIT_GRAPH;
  private CFGIntermediateRep ir = CFGIntermediateRep.JIMPLE_IR;
  private ThrowAnalysis throwAnalysis = UNIT_THROW_ANALYSIS.getAnalysis();

  private boolean isBrief = false;
 
  private boolean onepage = true;  /* in one page or several 8.5x11 pages  */

  private boolean showExceptions = false;

  private Map methodsToPrint = null; // If the user specifies particular
				     // methods to print, this is a map
				     // from method name to the class
				     // name declaring the method.


  protected void internalTransform(Body b, String phaseName, Map options) {
    SootMethod meth = b.getMethod();

    if ((methodsToPrint == null) ||
	(meth.getDeclaringClass().getName() ==
	 methodsToPrint.get(meth.getName()))) {
      Body body = ir.getBody((JimpleBody) b);
      print_cfg(body);
    }
  }


  public static void main(String[] args) {
      new CFGViewer().run( args );
  }


  public void run(String[] args) {

    /* process options */
    args = parse_options(args);
for (int i = 0; i < args.length; i++) System.err.println(args[i]);
    if (args.length == 0) {
      usage();
      return;
    }

    AltClassLoader.v().setAltClasses(new String[] {
      "soot.toolkits.graph.ArrayRefBlockGraph",
      "soot.toolkits.graph.Block",
      "soot.toolkits.graph.Block$AllMapTo",
      "soot.toolkits.graph.BlockGraph",
      "soot.toolkits.graph.BriefBlockGraph",
      "soot.toolkits.graph.BriefUnitGraph",
      "soot.toolkits.graph.CompleteBlockGraph",
      "soot.toolkits.graph.CompleteUnitGraph",
      "soot.toolkits.graph.TrapUnitGraph",
      "soot.toolkits.graph.UnitGraph",
      "soot.toolkits.graph.ZonedBlockGraph",
    });

    Pack jtp = PackManager.v().getPack("jtp");
    jtp.add(new Transform("jtp.printcfg", this));

    soot.Main.main(args);
  }


  private void usage(){
      G.v().out.println(
"Usage:\n" +
"   java soot.util.CFGViewer [soot options] [CFGViewer options] [class[:method]]...\n\n" +
"   CFGViewer options:\n" +
"      (When specifying the value for an '=' option, you only\n" +
"       need to type enough characters to specify the choice\n" +
"       unambiguously, and case is ignored.)\n" +
"\n" +
"       --alt-classpath PATH :\n" +
"                specifies the class path from which to load classes.\n" +
"                that implement graph types whose names begin with 'Alt'.\n" +
"       --graph={" +
CFGGraphType.help(0, 70, 
"                ".length()) + "} :\n" +
"                show the specified type of graph.\n" +
"                Defaults to BriefUnitGraph.\n" +
"       --ir={" +
CFGIntermediateRep.help(0, 70, 
"                ".length()) + "} :\n" +
"                create the CFG from the specified intermediate\n" +
"                representation. The default is jimple.\n" +
"       --brief :\n" +
"                label nodes with the unit or block index,\n" +
"                instead of the text of their statements.\n" +
"       --throwAnalysis={" +
throwAnalysisOptions.help(0, 70,
"              ".length()) + "} :\n" +
"                use the specified throw analysis when creating complete\n" +
"                graphs (UnitThrowAnalysis is the default).\n" +
"       --showExceptions :\n" +
"                in complete graphs, include edges showing the path of\n" +
"                exceptions from thrower to catcher, labeled with the\n" +
"                possible exception types.\n" +
"       --multipages :\n" +
"                produce dot file output for multiple 8.5x11\" pages.\n" +
"                By default, a single page is produced.\n" +
"       --help :\n" +
"                print this message.\n"
);
  }

  /**
   * Parse the command line arguments specific to CFGViewer,
   * @return an array of arguments to pass on to Soot.Main.main().
   */
  private String[] parse_options(String[] args){
    List sootArgs = new ArrayList(args.length);

    for (int i=0, n=args.length; i<n; i++) {
      if (args[i].equals("--soot-classpath") ||
	  args[i].equals("--soot-class-path")) {
	Scene.v().setSootClassPath(args[++i]);
      } else if (args[i].equals("--alt-classpath") ||
	  args[i].equals("--alt-class-path")) {
	AltClassLoader.v().setAltClassPath(args[++i]);
      } else if (args[i].startsWith("--graph=")) {
	graphtype = 
	  CFGGraphType.getGraphType(args[i].substring("--graph=".length()));
      } else if (args[i].startsWith("--ir=")) {
	ir = 
	  CFGIntermediateRep.getIR(args[i].substring("--ir=".length()));
      } else if (args[i].equals("--brief")) {
	isBrief = true;
      } else if (args[i].startsWith("--throwAnalysis=")) {
	throwAnalysis = 
	  getThrowAnalysis(args[i].substring("--throwAnalysis=".length()));
      } else if (args[i].equals("--showExceptions")) {
	showExceptions = true;
      } else if (args[i].equals("--multipages")) {
	onepage = false;
      } else if (args[i].equals("--help")) {
	return new String[0];	// This is a cheesy method to inveigle
				// our caller into printing the help
				// and exiting.
      } else if (args[i].equals("-p") ||
		 args[i].equals("--phase-option") ||
		 args[i].equals("-phase-option")) {
	// Pass the phase option right away, so the colon doesn't look
	// like a method specifier.
	sootArgs.add(args[i]);
	sootArgs.add(args[++i]);
	sootArgs.add(args[++i]);
      } else {
	int smpos = args[i].indexOf(':');
	if (smpos == -1) {
	  sootArgs.add(args[i]); 
	} else {
	  String clsname  = args[i].substring(0, smpos);
	  sootArgs.add(clsname);
	  String methname = args[i].substring(smpos+1);
	  if (methodsToPrint == null) {
	    methodsToPrint = new HashMap();
	  }
	  methodsToPrint.put(methname, clsname);
	}
      }
    }
    String[] sootArgsArray = new String[sootArgs.size()];
    return (String[]) sootArgs.toArray(sootArgsArray);
  }

    
  protected void print_cfg(Body body) {
    SootMethod method = body.getMethod();
    String graphname = method.getSubSignature();

    DotGraph canvas = new DotGraph(graphname);
    
    if (!onepage) {
      canvas.setPageSize(8.5, 11.0);
    }

    if (isBrief) {
      canvas.setNodeShape(DotGraphConstants.NODE_SHAPE_CIRCLE);
    } else {
      canvas.setNodeShape(DotGraphConstants.NODE_SHAPE_BOX);
    }
    canvas.setGraphLabel(graphname);

    DirectedGraph graph = graphtype.buildGraph(body);
    graphtype.drawNodesAndEdges(this, canvas, graph);
    canvas.plot();
  }


  /**
   * Add to a {@link DotGraph} the nodes and edges depicting the
   * control flow in a control flow graph without distinguished
   * exceptional edges.
   * 
   * @param canvas the {@link DotGraph} to which to add the nodes and edges.
   * @param graph a directed control flow graph (UnitGraph, BlockGraph ...)
   */
  public void drawUnexceptionalNodesAndEdges(DotGraph canvas,
					     DirectedGraph graph) {
    DotLabeller labeller = new DotLabeller((int)(graph.size()/0.7f), 0.7f);

    Iterator nodesIt = graph.iterator();
    while (nodesIt.hasNext()) {
      Object node = nodesIt.next();

      canvas.drawNode(labeller.getLabel(node));
      Iterator succsIt = graph.getSuccsOf(node).iterator();
      while (succsIt.hasNext()) {
        Object succ = succsIt.next();	
        canvas.drawEdge(labeller.getLabel(node), 
			labeller.getLabel(succ));
      }
    }
    setStyle(graph.getHeads(), canvas, labeller,
	     DotGraphConstants.NODE_STYLE_FILLED);
    setStyle(graph.getTails(), canvas, labeller, 
	     DotGraphConstants.NODE_STYLE_FILLED);
    if (! isBrief) {
      Body body = null;
      if (graph instanceof UnitGraph) {
	body = ((UnitGraph) graph).getBody();
      } else if (graph instanceof BlockGraph) {
	body = ((BlockGraph) graph).getBody();
      }
      formatNodeText(body, canvas, labeller);
    }
  } 


  /**
   * Add to a {@link DotGraph} the nodes and edges depicting the
   * control flow in a {@link CompleteUnitGraph}, which has
   * distinguished edges for exceptional control flow.
   * 
   * @param canvas the {@link DotGraph} to which to add the nodes and edges.
   * @param graph the control flow graph
   */
  public void drawExceptionalNodesAndEdges(DotGraph canvas,
					   CompleteUnitGraph graph) {

    DotLabeller labeller = new DotLabeller((int)(graph.size()/0.7f), 0.7f);

    for (Iterator nodesIt = graph.iterator(); nodesIt.hasNext(); ) {
      Unit node = (Unit) nodesIt.next();

      canvas.drawNode(labeller.getLabel(node));

      for (Iterator succsIt = graph.getUnexceptionalSuccsOf(node).iterator();
	   succsIt.hasNext(); ) {
        Object succ = succsIt.next();	
        DotGraphEdge edge = canvas.drawEdge(labeller.getLabel(node), 
					    labeller.getLabel(succ));
	// edge.setStyle("solid");
	edge.setAttribute("color", "black");
      }

      for (Iterator succsIt = graph.getExceptionalSuccsOf(node).iterator();
	   succsIt.hasNext(); ) {
	Object succ = succsIt.next();
	DotGraphEdge edge = canvas.drawEdge(labeller.getLabel(node),
					    labeller.getLabel(succ));
	// edge.setStyle("dashed");
	edge.setAttribute("color", "red");
      }

      if (showExceptions) {
	for (Iterator destsIt = graph.getExceptionDests(node).iterator();
	     destsIt.hasNext(); ) {
	  CompleteUnitGraph.ExceptionDest dest = 
	    (CompleteUnitGraph.ExceptionDest) destsIt.next();
	  Object handlerStart = null;
	  if (dest.trap() == null) {
	    // Giving each escaping exception its own, invisible
	    // exceptional exit node produces a less cluttered
	    // graph.
	    handlerStart = new Object() {
	      public String toString() {
		return "Esc";
	      }
	    };
	    DotGraphNode escapeNode = 
	      canvas.drawNode(labeller.getLabel(handlerStart));
	    escapeNode.setStyle(DotGraphConstants.NODE_STYLE_INVISIBLE);

	  } else {
	    handlerStart = dest.trap().getHandlerUnit();
	  }
	  DotGraphEdge edge = canvas.drawEdge(labeller.getLabel(node),
					      labeller.getLabel(handlerStart));
	  //edge.setStyle("dotted");
	  edge.setAttribute("color", "lightgray");
	  String exceptionsLabel = 
	    ThrowableSetAbbreviator.abbreviate(dest.throwables());
	  edge.setLabel(exceptionsLabel);
	}
      }
    }
    setStyle(graph.getHeads(), canvas, labeller,
	     DotGraphConstants.NODE_STYLE_FILLED);
    setStyle(graph.getTails(), canvas, labeller, 
	     DotGraphConstants.NODE_STYLE_FILLED);
    if (! isBrief) {
      formatNodeText(graph.getBody(), canvas, labeller);
    }
  }


  // A utility class for assigning unique string labels to DotGraph
  // entities.
  public static class DotLabeller extends HashMap {
    private int nodecount = 0;

    DotLabeller(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
    }

    DotLabeller() {
      super();
    }

    void resetCount() {
      nodecount = 0;
    }

    String getLabel(Object node) {
      Integer index = (Integer)this.get(node);
      if (index == null) {
	index = new Integer(nodecount++);
	this.put(node, index);
      }
      return index.toString();
    }
  }


  private void formatNodeText(Body body, DotGraph canvas, 
			      DotLabeller labeller) {

    LabeledUnitPrinter printer = null;
    if (body != null) {
      printer = new BriefUnitPrinter(body);
      printer.noIndent();
    }

    for (Iterator nodesIt = labeller.keySet().iterator();
	 nodesIt.hasNext(); ) {
      Object node = nodesIt.next();
      DotGraphNode dotnode = canvas.getNode(labeller.getLabel(node));
      String nodeLabel = null;

      if (printer == null) {
	nodeLabel = node.toString();
      } else {
	if (node instanceof Unit) {
	  ((Unit) node).toString(printer);
	  String targetLabel = (String) printer.labels().get(node);
	  if (targetLabel == null) {
	    nodeLabel = printer.toString();
	  } else {
	    nodeLabel = targetLabel + ": " + printer.toString();
	  }

	} else if (node instanceof Block) {
	  Iterator units = ((Block) node).iterator();
	  StringBuffer buffer = new StringBuffer();
	  while (units.hasNext()) {
	    Unit unit = (Unit) units.next();
	    String targetLabel = (String) printer.labels().get(unit);
	    if (targetLabel != null) {
	      buffer.append(targetLabel)
		.append(":\\n");
	    }
	    unit.toString(printer);
	    buffer.append(printer.toString())
	      .append("\\l");
	  }
	  nodeLabel = buffer.toString();
	} else {
	  nodeLabel = node.toString();
	}
      }
      dotnode.setLabel(nodeLabel);
    }
  }


  /**
   * Utility routine for setting some common formatting style for the
   * {@link DotGraphNode}s corresponding to some collection of objects.
   * 
   * @param objects is the collection of {@link Object}s whose
   *        nodes are to be styled.
   * @param canvas the {@link DotGraph} containing nodes corresponding
   *        to the collection.
   * @param labeller maps from {@link Object} to the strings used
   *        to identify corresponding {@link DotGraphNode}s.
   * @param style the style to set for each of the nodes.
   */
  private void setStyle(Collection objects, DotGraph canvas, 
			DotLabeller labeller, String style) {
    // Fill the entry and exit nodes.
    for (Iterator it = objects.iterator(); it.hasNext(); ) {
      Object object = it.next();
      DotGraphNode objectNode = canvas.getNode(labeller.getLabel(object));
      objectNode.setStyle(style);
    }
  }


  // A kludge for shortening the names of exceptions.
  private static class ThrowableSetAbbreviator {
    static final private String JAVA_LANG = "java.lang.";
    static final private int JAVA_LANG_LENGTH = JAVA_LANG.length();
    static final private String EXCEPTION = "Exception";
    static final private int EXCEPTION_LENGTH = EXCEPTION.length();

    static String abbreviate(ThrowableSet set) {
      Collection setsThrowables = set.types();
      Collection asyncThrowables = ThrowableSet.Manager.v().ASYNC_ERRORS.types();
      boolean containsAllAsync = setsThrowables.containsAll(asyncThrowables);
      StringBuffer buf = new StringBuffer();

      if (containsAllAsync) {
	buf.append("+async");
      }

      for (Iterator it = set.types().iterator(); it.hasNext(); ) {
	RefLikeType reflikeType = (RefLikeType) it.next();
	RefType baseType = null;
	if (reflikeType instanceof RefType) {
	  baseType = (RefType)reflikeType;
	  if (asyncThrowables.contains(baseType) && containsAllAsync) {
	    continue;		// This is already accounted for in "+async".
	  } else {
	    if (buf.length() > 0) {
	      buf.append("\\l");
	    }
	    buf.append('+');
	  }
	} else if (reflikeType instanceof AnySubType) {
	  if (buf.length() > 0) {
	    buf.append("\\l");
	  }
	  buf.append("+(");
	  baseType = ((AnySubType)reflikeType).getBase();
	}
	String typeName = baseType.toString();
	if (typeName.startsWith(JAVA_LANG)) {
	  typeName = typeName.substring(JAVA_LANG_LENGTH);
	}
	if (typeName.length() > EXCEPTION_LENGTH && typeName.endsWith(EXCEPTION)) {
	  typeName = typeName.substring(0, typeName.length()-EXCEPTION_LENGTH);
	}
	buf.append(typeName);
	if (reflikeType instanceof AnySubType) {
	  buf.append(')');
	}
      }
      return buf.toString();
    }
  }
}
