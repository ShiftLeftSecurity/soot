package abc.tm.weaving.weaver.tmanalysis.mustalias;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Local;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;


public class LocalMustAliasAnalysis extends ForwardFlowAnalysis
{
    private Map objectMap = new HashMap();

    public LocalMustAliasAnalysis(UnitGraph g)
    {
        super(g);

        int nextNum = 0;
        for( Iterator lIt = g.getBody().getLocals().iterator(); lIt.hasNext(); ) {
            final Local l = (Local) lIt.next();
            objectMap.put( l, new Integer( nextNum++ ) );
        }

        doAnalysis();
    }

    protected void merge(Object in1, Object in2, Object out)
    {
        UnionFind inMap1 = (UnionFind) in1;
        UnionFind inMap2 = (UnionFind) in2;
        UnionFind outMap = (UnionFind) out;

        outMap.clear();
        for( Iterator lIt = inMap1.allSets().iterator(); lIt.hasNext(); ) {
            final List l = (List) lIt.next();
            for( Iterator localIt = l.iterator(); localIt.hasNext(); ) {
                final Local local = (Local) localIt.next();
                for( Iterator local2It = l.iterator(); local2It.hasNext(); ) {
                    final Local local2 = (Local) local2It.next();
                    if( local2 == local ) break;
                    if( inMap2.lookup( local ) == inMap2.lookup( local2 ) ) {
                        outMap.merge( local, local2 );
                    }
                }
            }
        }
    }
    

    protected void flowThrough(Object inValue, Object unit,
            Object outValue)
    {
        UnionFind     in  = (UnionFind) inValue;
        UnionFind     out = (UnionFind) outValue;
        Stmt    s   = (Stmt)    unit;

        out.clear();

        List preserve = new ArrayList();
outer:
        for( Iterator lIt = objectMap.keySet().iterator(); lIt.hasNext(); ) {
            final Local l = (Local) lIt.next();
            for( Iterator vbIt = s.getDefBoxes().iterator(); vbIt.hasNext(); ) {
                final ValueBox vb = (ValueBox) vbIt.next();
                if( vb.getValue() == l ) continue outer;
            }
            preserve.add( l );
        }

        for( Iterator lIt = preserve.iterator(); lIt.hasNext(); ) {

            final Local l = (Local) lIt.next();
            for( Iterator l2It = in.findSet( l ).iterator(); l2It.hasNext(); ) {
                final Local l2 = (Local) l2It.next();
                out.merge( l, l2 );
            }
        }

        if( s instanceof DefinitionStmt ) {
            DefinitionStmt ds = (DefinitionStmt) s;
            Value lhs = ds.getLeftOp();
            Value rhs = ds.getRightOp();
            if( rhs instanceof Local && lhs instanceof Local ) out.merge( lhs, rhs );
        }
    }

    protected void copy(Object source, Object dest)
    {
        UnionFind sourceMap = (UnionFind) source;
        UnionFind destMap   = (UnionFind) dest;
            
        sourceMap.copy( destMap );
    }

    protected Object entryInitialFlow()
    {
        return new UnionFind(objectMap);
    }
        
    protected Object newInitialFlow()
    {
        UnionFind u = new UnionFind(objectMap);
        u.mergeAll();
        return u;
    }

	/**
	 * @param l1
	 * @param l2
	 * @param s 
	 * @return
	 */
	public boolean mustAlias(Local l1, Local l2, Stmt s) {
		UnionFind uf = (UnionFind) getFlowBefore(s);
		return uf.findSet(l1).contains(l2);
	}
        
}
