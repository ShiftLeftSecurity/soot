package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public class RobjIter extends Robj {
    protected Iterator r;
    
    public RobjIter(Iterator r) {
        super();
        this.r = r;
    }
    
    public Iterator iterator() {
        return new Iterator() {
            public boolean hasNext() {
                boolean ret = r.hasNext();
                return ret;
            }
            
            public Object next() { return new Tuple((AllocNode) r.next()); }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { obj.v() },
                                              new PhysicalDomain[] { H1.v() },
                                              ("<soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bdddomai" +
                                               "ns.H1> ret = jedd.internal.Jedd.v().falseBDD(); at /home/olh" +
                                               "otak/soot-2-jedd/src/soot/jimple/spark/queue/RobjIter.jedd:4" +
                                               "6,17-20"),
                                              jedd.internal.Jedd.v().falseBDD());
        while (r.hasNext()) {
            ret.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { r.next() },
                                                       new Attribute[] { obj.v() },
                                                       new PhysicalDomain[] { H1.v() }));
        }
        return new jedd.internal.RelationContainer(new Attribute[] { obj.v() },
                                                   new PhysicalDomain[] { H1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/RobjIter.jedd:50,8-14"),
                                                   ret);
    }
    
    public boolean hasNext() { return r.hasNext(); }
}
