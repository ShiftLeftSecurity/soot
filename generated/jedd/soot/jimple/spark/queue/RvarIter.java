package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public class RvarIter extends Rvar {
    protected Iterator r;
    
    public RvarIter(Iterator r) {
        super();
        this.r = r;
    }
    
    public Iterator iterator() {
        ;
        return new Iterator() {
            public boolean hasNext() { return r.hasNext(); }
            
            public Object next() { return RvarIter.this.new Tuple((VarNode) r.next()); }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { var.v() },
                                              new PhysicalDomain[] { V1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1> ret = jedd.internal.Jedd.v().falseBDD(); at /home/olh" +
                                               "otak/soot-2-jedd/src/soot/jimple/spark/queue/RvarIter.jedd:4" +
                                               "3,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        while (r.hasNext()) {
            ret.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { r.next() },
                                                       new Attribute[] { var.v() },
                                                       new PhysicalDomain[] { V1.v() }));
        }
        return new jedd.internal.RelationContainer(new Attribute[] { var.v() },
                                                   new PhysicalDomain[] { V1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/RvarIter.jedd:47,8"),
                                                   ret);
    }
    
    public boolean hasNext() { return r.hasNext(); }
    
    private final class Tuple extends soot.jimple.spark.queue.Rvar.Tuple {
        private VarNode _var;
        
        public VarNode var() { return _var; }
        
        public Tuple(VarNode _var) {
            super();
            this._var = _var;
        }
    }
    
}
