package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public class Rvar_objIter extends Rvar_obj {
    protected Iterator r;
    
    public Rvar_objIter(Iterator r) {
        super();
        this.r = r;
    }
    
    public Iterator iterator() {
        ;
        return new Iterator() {
            public boolean hasNext() { return r.hasNext(); }
            
            public Object next() { return Rvar_objIter.this.new Tuple((VarNode) r.next(), (AllocNode) r.next()); }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v() },
                                              new PhysicalDomain[] { V1.v(), H1.v() },
                                              ("<soot.jimple.spark.bdddomains.var:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bd" +
                                               "ddomains.H1> ret = jedd.internal.Jedd.v().falseBDD(); at /ho" +
                                               "me/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/Rvar_objI" +
                                               "ter.jedd:43,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        while (r.hasNext()) {
            ret.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { r.next(), r.next() },
                                                       new Attribute[] { var.v(), obj.v() },
                                                       new PhysicalDomain[] { V1.v(), H1.v() }));
        }
        return new jedd.internal.RelationContainer(new Attribute[] { obj.v(), var.v() },
                                                   new PhysicalDomain[] { H1.v(), V1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rvar_objIter.jedd:47,8"),
                                                   ret);
    }
    
    public boolean hasNext() { return r.hasNext(); }
    
    private final class Tuple extends soot.jimple.spark.queue.Rvar_obj.Tuple {
        private VarNode _var;
        
        public VarNode var() { return _var; }
        
        private AllocNode _obj;
        
        public AllocNode obj() { return _obj; }
        
        public Tuple(VarNode _var, AllocNode _obj) {
            super();
            this._var = _var;
            this._obj = _obj;
        }
    }
    
}
