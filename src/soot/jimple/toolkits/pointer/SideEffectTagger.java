package soot.jimple.toolkits.pointer;
import soot.*;
import java.util.*;
import soot.toolkits.graph.*;
import soot.jimple.toolkits.invoke.*;
import soot.jimple.*;
import java.io.*;

public class SideEffectTagger extends BodyTransformer
{ 
    private static SideEffectTagger instance = 
	new SideEffectTagger();
    private SideEffectTagger() {}
    private Map bodyToGraph = new HashMap(0);
    private Set analysesInProgress = new HashSet(0);
    private Set preAnalysesInProgress = new HashSet(0);
    private InvokeGraph ig;
    boolean optionDontTag = false;

    public static SideEffectTagger v() { return instance; }

    public String getDeclaredOptions() { return super.getDeclaredOptions() +
	" dont-tag "; }

    public String getDefaultOptions() { return ""; }

    protected void internalTransform(Body b, String phaseName, Map options)
    {
	SideEffectAnalysis sea = Scene.v().getActiveSideEffectAnalysis();
	sea.findNTRWSets( b.getMethod() );
	HashMap stmtToReadSet = new HashMap();
	HashMap stmtToWriteSet = new HashMap();
	optionDontTag = Options.getBoolean( options, "dont-tag" );
	for( Iterator stmtIt = b.getUnits().iterator(); stmtIt.hasNext(); ) {
	    Stmt stmt = (Stmt) stmtIt.next();
	    stmtToReadSet.put( stmt, sea.readSet( b.getMethod(), stmt ) );
	    stmtToWriteSet.put( stmt, sea.writeSet( b.getMethod(), stmt ) );
	}
	for( Iterator outerIt = b.getUnits().iterator(); outerIt.hasNext(); ) {
	    Stmt outer = (Stmt) outerIt.next();
	    RWSet outerRead = (RWSet) stmtToReadSet.get( outer );
	    RWSet outerWrite = (RWSet) stmtToWriteSet.get( outer );
	    DependenceTag tag = null;

	    for( Iterator innerIt = b.getUnits().iterator(); innerIt.hasNext(); ) {
		Stmt inner = (Stmt) innerIt.next();
		RWSet innerRead = (RWSet) stmtToReadSet.get( inner );
		RWSet innerWrite = (RWSet) stmtToWriteSet.get( inner );

		if( outerRead != null && innerWrite != null &&
			outerRead.hasNonEmptyIntersection( innerWrite ) ) {
		    if( tag == null ) tag = new DependenceTag();
		    tag.addStmtRW( inner );
		}
		if( outerWrite != null && innerRead != null &&
			outerWrite.hasNonEmptyIntersection( innerRead ) ) {
		    if( tag == null ) tag = new DependenceTag();
		    tag.addStmtWR( inner );
		}
		if( outer != inner ) {
		    if( outerRead != null && innerRead != null &&
			    outerRead.hasNonEmptyIntersection( innerRead ) ) {
			if( tag == null ) tag = new DependenceTag();
			tag.addStmtRR( inner );
		    }
		    if( outerWrite != null && innerWrite != null &&
			    outerWrite.hasNonEmptyIntersection( innerWrite ) ) {
			if( tag == null ) tag = new DependenceTag();
			tag.addStmtWW( inner );
		    }
		}
	    }
	    if( ( outerRead != null && outerRead.getCallsNative() ) 
		    || ( outerWrite != null && outerWrite.getCallsNative() ) ) {
		if( tag == null ) tag = new DependenceTag();
		tag.setCallsNative();
	    }
	    if( tag != null ) {
		if( !optionDontTag )
		    outer.addTag( tag );
	    }
	}
    }
}


