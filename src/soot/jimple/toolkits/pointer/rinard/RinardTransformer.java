package soot.jimple.toolkits.pointer.rinard;
import soot.*;
import java.util.*;
import soot.toolkits.graph.*;
import soot.jimple.toolkits.invoke.*;
import java.io.*;

public class RinardTransformer extends BodyTransformer
{ 
    private static RinardTransformer instance = 
	new RinardTransformer();
    private RinardTransformer() {}
    private Map bodyToGraph = new HashMap(0);
    private Set analysesInProgress = new HashSet(0);
    private Set preAnalysesInProgress = new HashSet(0);
    private InvokeGraph ig;
    boolean optionMuDotFiles = false;

    public static RinardTransformer v() { return instance; }

    public String getDeclaredOptions() { return super.getDeclaredOptions() +
	" dot-files interproc mu-dot-files"; }

    public String getDefaultOptions() { return ""; }

    public Graph getGraphForBody( Body b )
    {
	Graph ret = (Graph) bodyToGraph.get( b );
	if( ret == null ) {
	    if( analysesInProgress.contains( b ) ) return null;
	    analysesInProgress.add( b );
	    Rinard r = new Rinard( new CompleteUnitGraph( b ), ig );
	    ret = r.endGraph;
	    ret.nm = r.nm;
	    bodyToGraph.put( b, ret );
	    analysesInProgress.remove( b );
	}
	return ret;
    }

    protected void preAnalyze( Body b ) {
	String name = b.getMethod().getDeclaringClass().getName()+"."+
	    b.getMethod().getName();
	if( preAnalysesInProgress.contains(name) ) return;
	preAnalysesInProgress.add( name );
	for( Iterator it = ig.getTargetsOf( b.getMethod() ).iterator(); it.hasNext(); ) {
	    SootMethod m = (SootMethod) it.next();
	    if( m.hasActiveBody() ) {
		preAnalyze( m.getActiveBody() );
	    }
	}
	getGraphForBody( b );
    }
    protected void internalTransform(Body b, String phaseName, Map options)
    {
	if( Options.getBoolean( options, "interproc" ) && ig == null ) {
	    if( Options.getBoolean( options, "mu-dot-files" ) ) optionMuDotFiles = true;
	    InvokeGraphBuilder.v().transform( phaseName + ".igb" );
	    ig = Scene.v().getActiveInvokeGraph();
	}
	if( ig != null ) preAnalyze( b );
	String methodName = b.getMethod().getDeclaringClass().getName() + "."
	            +b.getMethod().getName();
	System.out.println( "Rinard analysis for "+methodName );
	Graph g = getGraphForBody( b );
	System.out.println( g.toString() );
	if( Options.getBoolean(options, "dot-files") ) {
	    try {
		PrintStream out = new PrintStream( 
		    new FileOutputStream( methodName+".dot" ));
		out.println( g.toDotGraph() );
	    } catch( FileNotFoundException e ) {
		System.out.println( "Could not write to file "+methodName+".dot" );
	    }
	}	
    }
}


