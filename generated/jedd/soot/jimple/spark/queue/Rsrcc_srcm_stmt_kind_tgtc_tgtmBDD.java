package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Rsrcc_srcm_stmt_kind_tgtc_tgtmBDD extends Rsrcc_srcm_stmt_kind_tgtc_tgtm {
    private final jedd.internal.RelationContainer bdd =
      new jedd.internal.RelationContainer(new Attribute[] { srcc.v(), srcm.v(), stmt.v(), kind.v(), tgtc.v(), tgtm.v() },
                                          new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), FD.v(), V2.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.srcc:soot.jimple.spark" +
                                           ".bdddomains.V1, soot.jimple.spark.bdddomains.srcm:soot.jimpl" +
                                           "e.spark.bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soo" +
                                           "t.jimple.spark.bdddomains.ST, soot.jimple.spark.bdddomains.k" +
                                           "ind:soot.jimple.spark.bdddomains.FD, soot.jimple.spark.bdddo" +
                                           "mains.tgtc:soot.jimple.spark.bdddomains.V2, soot.jimple.spar" +
                                           "k.bdddomains.tgtm:soot.jimple.spark.bdddomains.T2> bdd = jed" +
                                           "d.internal.Jedd.v().falseBDD() at /home/olhotak/soot-2-jedd/" +
                                           "src/soot/jimple/spark/queue/Rsrcc_srcm_stmt_kind_tgtc_tgtmBD" +
                                           "D.jedd:31,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    void add(final jedd.internal.RelationContainer tuple) { bdd.eqUnion(tuple); }
    
    public Rsrcc_srcm_stmt_kind_tgtc_tgtmBDD(final jedd.internal.RelationContainer bdd) {
        super();
        this.bdd.eq(bdd);
    }
    
    Rsrcc_srcm_stmt_kind_tgtc_tgtmBDD() { super(); }
    
    public Iterator iterator() {
        ;
        final Iterator it =
          new jedd.internal.RelationContainer(new Attribute[] { tgtc.v(), tgtm.v(), kind.v(), stmt.v(), srcm.v(), srcc.v() },
                                              new PhysicalDomain[] { V2.v(), T2.v(), FD.v(), ST.v(), T1.v(), V1.v() },
                                              ("bdd.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-" +
                                               "2-jedd/src/soot/jimple/spark/queue/Rsrcc_srcm_stmt_kind_tgtc" +
                                               "_tgtmBDD.jedd:36,28"),
                                              bdd).iterator(new Attribute[] { srcc.v(), srcm.v(), stmt.v(), kind.v(), tgtc.v(), tgtm.v() });
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new Iterator() {
            public boolean hasNext() { return it.hasNext(); }
            
            public Object next() { return Rsrcc_srcm_stmt_kind_tgtc_tgtmBDD.this.new Tuple((Object[]) it.next()); }
            
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
                                               "ains.tgtm:soot.jimple.spark.bdddomains.T2> ret = bdd; at /ho" +
                                               "me/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/Rsrcc_src" +
                                               "m_stmt_kind_tgtc_tgtmBDD.jedd:47,8"),
                                              bdd);
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new jedd.internal.RelationContainer(new Attribute[] { tgtc.v(), tgtm.v(), kind.v(), stmt.v(), srcm.v(), srcc.v() },
                                                   new PhysicalDomain[] { V2.v(), T2.v(), FD.v(), ST.v(), T1.v(), V1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rsrcc_srcm_stmt_kind_tgtc_tgtmBDD.jedd:49,8"),
                                                   ret);
    }
    
    public boolean hasNext() {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD());
    }
    
    private final class Tuple extends soot.jimple.spark.queue.Rsrcc_srcm_stmt_kind_tgtc_tgtm.Tuple {
        private Object[] tuple;
        
        public Tuple(Object[] tuple) {
            super();
            this.tuple = tuple;
        }
        
        public Context srcc() { return (Context) tuple[0]; }
        
        public SootMethod srcm() { return (SootMethod) tuple[1]; }
        
        public Unit stmt() { return (Unit) tuple[2]; }
        
        public Kind kind() { return (Kind) tuple[3]; }
        
        public Context tgtc() { return (Context) tuple[4]; }
        
        public SootMethod tgtm() { return (SootMethod) tuple[5]; }
    }
    
}
