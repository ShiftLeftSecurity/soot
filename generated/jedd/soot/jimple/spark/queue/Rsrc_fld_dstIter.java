package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public class Rsrc_fld_dstIter extends Rsrc_fld_dst {
    protected Iterator r;
    
    public Rsrc_fld_dstIter(Iterator r) {
        super();
        this.r = r;
    }
    
    public Iterator iterator() {
        return new Iterator() {
            public boolean hasNext() {
                boolean ret = r.hasNext();
                return ret;
            }
            
            public Object next() { return new Tuple((VarNode) r.next(), (SparkField) r.next(), (VarNode) r.next()); }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { src.v(), fld.v(), dst.v() },
                                              new PhysicalDomain[] { V1.v(), FD.v(), V2.v() },
                                              ("<soot.jimple.spark.bdddomains.src:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.fld:soot.jimple.spark.bd" +
                                               "ddomains.FD, soot.jimple.spark.bdddomains.dst:soot.jimple.sp" +
                                               "ark.bdddomains.V2> ret = jedd.internal.Jedd.v().falseBDD(); " +
                                               "at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/Rsr" +
                                               "c_fld_dstIter.jedd:46,33-36"),
                                              jedd.internal.Jedd.v().falseBDD());
        while (r.hasNext()) {
            ret.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { r.next(), r.next(), r.next() },
                                                       new Attribute[] { src.v(), fld.v(), dst.v() },
                                                       new PhysicalDomain[] { V1.v(), FD.v(), V2.v() }));
        }
        return new jedd.internal.RelationContainer(new Attribute[] { dst.v(), fld.v(), src.v() },
                                                   new PhysicalDomain[] { V2.v(), FD.v(), V1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rsrc_fld_dstIter.jedd:50,8-14"),
                                                   ret);
    }
    
    public boolean hasNext() { return r.hasNext(); }
}
