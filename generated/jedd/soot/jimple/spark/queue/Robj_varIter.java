package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public class Robj_varIter extends Robj_var {
    protected Iterator r;
    
    public Robj_varIter(Iterator r) {
        super();
        this.r = r;
    }
    
    public Iterator iterator() {
        ;
        return new Iterator() {
            public boolean hasNext() { return r.hasNext(); }
            
            public Object next() { return Robj_varIter.this.new Tuple((AllocNode) r.next(), (VarNode) r.next()); }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { obj.v(), var.v() },
                                              new PhysicalDomain[] { H1.v(), V1.v() },
                                              ("<soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bdddomai" +
                                               "ns.H1, soot.jimple.spark.bdddomains.var:soot.jimple.spark.bd" +
                                               "ddomains.V1> ret = jedd.internal.Jedd.v().falseBDD(); at /ho" +
                                               "me/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/Robj_varI" +
                                               "ter.jedd:43,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        while (r.hasNext()) {
            ret.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { r.next(), r.next() },
                                                       new Attribute[] { obj.v(), var.v() },
                                                       new PhysicalDomain[] { H1.v(), V1.v() }));
        }
        return new jedd.internal.RelationContainer(new Attribute[] { obj.v(), var.v() },
                                                   new PhysicalDomain[] { H1.v(), V1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Robj_varIter.jedd:47,8"),
                                                   ret);
    }
    
    public boolean hasNext() { return r.hasNext(); }
    
    private final class Tuple extends soot.jimple.spark.queue.Robj_var.Tuple {
        private AllocNode _obj;
        
        public AllocNode obj() { return _obj; }
        
        private VarNode _var;
        
        public VarNode var() { return _var; }
        
        public Tuple(AllocNode _obj, VarNode _var) {
            super();
            this._obj = _obj;
            this._var = _var;
        }
    }
    
}
