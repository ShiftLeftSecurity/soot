package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public class Rsrcc_srcm_stmt_kind_tgtc_tgtmIter extends Rsrcc_srcm_stmt_kind_tgtc_tgtm {
    protected Iterator r;
    
    public Rsrcc_srcm_stmt_kind_tgtc_tgtmIter(Iterator r) {
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
                return new Tuple((Context) r.next(),
                                 (SootMethod) r.next(),
                                 (Unit) r.next(),
                                 (Kind) r.next(),
                                 (Context) r.next(),
                                 (SootMethod) r.next());
            }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { srcc.v(), srcm.v(), stmt.v(), kind.v(), tgtc.v(), tgtm.v() },
                                              new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), FD.v(), V2.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.srcc:soot.jimple.spark.bdddoma" +
                                               "ins.V1, soot.jimple.spark.bdddomains.srcm:soot.jimple.spark." +
                                               "bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimple" +
                                               ".spark.bdddomains.ST, soot.jimple.spark.bdddomains.kind:soot" +
                                               ".jimple.spark.bdddomains.FD, soot.jimple.spark.bdddomains.tg" +
                                               "tc:soot.jimple.spark.bdddomains.V2, soot.jimple.spark.bdddom" +
                                               "ains.tgtm:soot.jimple.spark.bdddomains.T2> ret = jedd.intern" +
                                               "al.Jedd.v().falseBDD(); at /home/olhotak/soot-2-jedd/src/soo" +
                                               "t/jimple/spark/queue/Rsrcc_srcm_stmt_kind_tgtc_tgtmIter.jedd" +
                                               ":46,63-66"),
                                              jedd.internal.Jedd.v().falseBDD());
        while (r.hasNext()) {
            ret.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { r.next(), r.next(), r.next(), r.next(), r.next(), r.next() },
                                                       new Attribute[] { srcc.v(), srcm.v(), stmt.v(), kind.v(), tgtc.v(), tgtm.v() },
                                                       new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), FD.v(), V2.v(), T2.v() }));
        }
        return new jedd.internal.RelationContainer(new Attribute[] { tgtc.v(), stmt.v(), srcm.v(), tgtm.v(), kind.v(), srcc.v() },
                                                   new PhysicalDomain[] { V2.v(), ST.v(), T1.v(), T2.v(), FD.v(), V1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rsrcc_srcm_stmt_kind_tgtc_tgtmIter.jedd:50,8-14"),
                                                   ret);
    }
    
    public boolean hasNext() { return r.hasNext(); }
}
