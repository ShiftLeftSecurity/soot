package abc.tm.weaving.weaver.tmanalysis.mustalias;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import soot.Local;
import soot.RefLikeType;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

/** LocalMustAliasAnalysis attempts to determine if two local
 * variables (at two potentially different program points) must point
 * to the same object.
 *
 * The underlying abstraction is that of definition expressions.
 * When a local variable is assigned to, the analysis tracks the source
 * of the value (a NewExpr, InvokeExpr, or ParameterRef). If two
 * variables have the same source, then they are equal.
 *
 * This is like constant propagation on abstract objects. */
public class LocalMustAliasAnalysis extends ForwardFlowAnalysis
{
    private static final Object UNKNOWN = new Object() {
    	public String toString() { return "UNKNOWN"; }
    };
    private static final Object NOTHING = new Object() {
    	public String toString() { return "NOTHING"; }
    };
    
    private List<Local> locals;

    public LocalMustAliasAnalysis(UnitGraph g)
    {
        super(g);
        this.locals = new LinkedList<Local>(); 

        for (Local l : (Collection<Local>) g.getBody().getLocals()) {
            if (l.getType() instanceof RefLikeType)
                this.locals.add(l);
        }

        doAnalysis();
    }

    protected void merge(Object in1, Object in2, Object o)
    {
        HashMap inMap1 = (HashMap) in1;
        HashMap inMap2 = (HashMap) in2;
        HashMap outMap = (HashMap) o;

        for (Local l : locals) {
            Object i1 = inMap1.get(l), i2 = inMap2.get(l);
            if (i1 == i2) 
                outMap.put(l, i1);
            else if (i1 == NOTHING)
            	outMap.put(l, i2);
            else if (i2 == NOTHING)
            	outMap.put(l, i1);
            else
                outMap.put(l, UNKNOWN);
        }
    }
    

    protected void flowThrough(Object inValue, Object unit,
            Object outValue)
    {
        HashMap     in  = (HashMap) inValue;
        HashMap     out = (HashMap) outValue;
        Stmt    s   = (Stmt)    unit;

        out.clear();

        List<Local> preserve = new ArrayList();
        preserve.addAll(locals);
        for (ValueBox vb : (Collection<ValueBox>)s.getDefBoxes()) {
            preserve.remove(vb.getValue());
        }

        for (Local l : preserve) {
            out.put(l, in.get(l));
        }

        if (s instanceof DefinitionStmt) {
            DefinitionStmt ds = (DefinitionStmt) s;
            Value lhs = ds.getLeftOp();
            Value rhs = ds.getRightOp();

            if (lhs instanceof Local && lhs.getType() instanceof RefLikeType) {
                if (rhs instanceof NewExpr ||
                    rhs instanceof InvokeExpr || 
                    rhs instanceof ParameterRef || 
                    rhs instanceof ThisRef) {
                    // use the newexpr, invokeexpr, parameterref,
                    // or thisref as an ID; this should be OK for
                    // must-alias analysis.
                    out.put(lhs, rhs);
                } else if (rhs instanceof Local) {
                    out.put(lhs, in.get(rhs));
                } else out.put(lhs, UNKNOWN);
            }
        }
    }

    protected void copy(Object source, Object dest)
    {
        HashMap sourceMap = (HashMap) source;
        HashMap destMap   = (HashMap) dest;
            
        for (Local l : (Collection<Local>) locals) {
            destMap.put (l, sourceMap.get(l));
        }
    }

    /** Initial conservative value: objects have unknown definition. */
    protected Object entryInitialFlow()
    {
        HashMap m = new HashMap();
        for (Local l : (Collection<Local>) locals) {
            m.put(l, UNKNOWN);
        }
        return m;
    }

    /** Initial aggressive value: objects have no definitions. */
    protected Object newInitialFlow()
    {
        HashMap m = new HashMap();
        for (Local l : (Collection<Local>) locals) {
            m.put(l, NOTHING);
        }
        return m;
    }

    /**
     * @return true if values of l1 (at s1) and l2 (at s2) have the
     * exact same object IDs
     */
    public boolean mustAlias(Local l1, Stmt s1, Local l2, Stmt s2) {
        Object l1n = ((HashMap)getFlowBefore(s1)).get(l1);
        Object l2n = ((HashMap)getFlowBefore(s2)).get(l2);

        if (l1n == UNKNOWN || l2n == UNKNOWN)
            return false;

        return l1n == l2n;
    }
}
