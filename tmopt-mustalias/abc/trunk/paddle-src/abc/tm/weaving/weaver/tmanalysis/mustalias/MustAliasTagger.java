package abc.tm.weaving.weaver.tmanalysis.mustalias;
import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.jimple.Stmt;
import soot.tagkit.StringTag;
import soot.toolkits.graph.BriefUnitGraph;

/** A body transformer that records mustalias 
 * information in tags. */
public class MustAliasTagger extends BodyTransformer
{ 
    protected void internalTransform(
            Body b, String phaseName, Map options)
    {
        LocalMustAliasAnalysis a = new LocalMustAliasAnalysis(
		new BriefUnitGraph( b ) );

        Iterator sIt = b.getUnits().iterator();
        while( sIt.hasNext() ) {

            Stmt s = (Stmt) sIt.next();

            UnionFind mustAlias;
            StringTag t;

            mustAlias = (UnionFind) a.getFlowBefore( s );
            t = new StringTag( "MustAliasBefore: "+mustAlias.allSets().toString() );
            s.addTag( t );

            mustAlias = (UnionFind) a.getFlowAfter( s );
            t = new StringTag( "MustAliasAfter: "+mustAlias.allSets().toString() );
            s.addTag( t );

            if(b.getMethod().getSignature().indexOf("main")>-1) {
            
	            System.err.println();
	            System.err.println(s);
	            for (Iterator localIter = b.getLocals().iterator(); localIter.hasNext();) {
					Local l = (Local) localIter.next();
					for (Iterator localIter2 = b.getLocals().iterator(); localIter2
							.hasNext();) {
						Local l2 = (Local) localIter2.next();
						System.err.println("mustalias("+l+","+l2+") = "+a.mustAlias(l,s,l2,s));
					}
					
				}
            }
            
        }
    }
}

