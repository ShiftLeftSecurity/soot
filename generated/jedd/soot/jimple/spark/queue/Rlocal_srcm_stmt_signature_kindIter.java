package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public class Rlocal_srcm_stmt_signature_kindIter extends Rlocal_srcm_stmt_signature_kind {
    protected Iterator r;
    
    public Rlocal_srcm_stmt_signature_kindIter(Iterator r) {
        super();
        this.r = r;
    }
    
    public Iterator iterator() {
        return new Iterator() {
            public boolean hasNext() {
                boolean ret = r.hasNext();
                return ret;
            }
            
            public Object next() {
                return new Tuple((Local) r.next(),
                                 (SootMethod) r.next(),
                                 (Unit) r.next(),
                                 (NumberedString) r.next(),
                                 (Kind) r.next());
            }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { local.v(), srcm.v(), stmt.v(), signature.v(), kind.v() },
                                              new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), FD.v() },
                                              ("<soot.jimple.spark.bdddomains.local:soot.jimple.spark.bdddom" +
                                               "ains.V1, soot.jimple.spark.bdddomains.srcm:soot.jimple.spark" +
                                               ".bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimpl" +
                                               "e.spark.bdddomains.ST, soot.jimple.spark.bdddomains.signatur" +
                                               "e:soot.jimple.spark.bdddomains.H2, soot.jimple.spark.bdddoma" +
                                               "ins.kind:soot.jimple.spark.bdddomains.FD> ret = jedd.interna" +
                                               "l.Jedd.v().falseBDD(); at /home/olhotak/soot-2-jedd/src/soot" +
                                               "/jimple/spark/queue/Rlocal_srcm_stmt_signature_kindIter.jedd" +
                                               ":46,60-63"),
                                              jedd.internal.Jedd.v().falseBDD());
        while (r.hasNext()) {
            ret.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { r.next(), r.next(), r.next(), r.next(), r.next() },
                                                       new Attribute[] { local.v(), srcm.v(), stmt.v(), signature.v(), kind.v() },
                                                       new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), FD.v() }));
        }
        return new jedd.internal.RelationContainer(new Attribute[] { stmt.v(), signature.v(), local.v(), kind.v(), srcm.v() },
                                                   new PhysicalDomain[] { ST.v(), H2.v(), V1.v(), FD.v(), T1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rlocal_srcm_stmt_signature_kindIter.jedd:50,8-14"),
                                                   ret);
    }
    
    public boolean hasNext() { return r.hasNext(); }
}
