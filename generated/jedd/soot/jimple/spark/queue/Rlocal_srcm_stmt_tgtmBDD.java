package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Rlocal_srcm_stmt_tgtmBDD extends Rlocal_srcm_stmt_tgtm {
    private final jedd.internal.RelationContainer bdd =
      new jedd.internal.RelationContainer(new Attribute[] { local.v(), srcm.v(), stmt.v(), tgtm.v() },
                                          new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), T2.v() },
                                          ("private <soot.jimple.spark.bdddomains.local:soot.jimple.spar" +
                                           "k.bdddomains.V1, soot.jimple.spark.bdddomains.srcm:soot.jimp" +
                                           "le.spark.bdddomains.T1, soot.jimple.spark.bdddomains.stmt:so" +
                                           "ot.jimple.spark.bdddomains.ST, soot.jimple.spark.bdddomains." +
                                           "tgtm:soot.jimple.spark.bdddomains.T2> bdd = jedd.internal.Je" +
                                           "dd.v().falseBDD() at /home/olhotak/soot-2-jedd/src/soot/jimp" +
                                           "le/spark/queue/Rlocal_srcm_stmt_tgtmBDD.jedd:31,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    void add(final jedd.internal.RelationContainer tuple) { bdd.eqUnion(tuple); }
    
    public Rlocal_srcm_stmt_tgtmBDD(final jedd.internal.RelationContainer bdd) {
        super();
        this.bdd.eq(bdd);
    }
    
    Rlocal_srcm_stmt_tgtmBDD() { super(); }
    
    public Iterator iterator() {
        ;
        final Iterator it =
          new jedd.internal.RelationContainer(new Attribute[] { tgtm.v(), local.v(), stmt.v(), srcm.v() },
                                              new PhysicalDomain[] { T2.v(), V1.v(), ST.v(), T1.v() },
                                              ("bdd.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-" +
                                               "2-jedd/src/soot/jimple/spark/queue/Rlocal_srcm_stmt_tgtmBDD." +
                                               "jedd:36,28"),
                                              bdd).iterator(new Attribute[] { local.v(), srcm.v(), stmt.v(), tgtm.v() });
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new Iterator() {
            public boolean hasNext() { return it.hasNext(); }
            
            public Object next() { return Rlocal_srcm_stmt_tgtmBDD.this.new Tuple((Object[]) it.next()); }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { local.v(), srcm.v(), stmt.v(), tgtm.v() },
                                              new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.local:soot.jimple.spark.bdddom" +
                                               "ains.V1, soot.jimple.spark.bdddomains.srcm:soot.jimple.spark" +
                                               ".bdddomains.T1, soot.jimple.spark.bdddomains.stmt:soot.jimpl" +
                                               "e.spark.bdddomains.ST, soot.jimple.spark.bdddomains.tgtm:soo" +
                                               "t.jimple.spark.bdddomains.T2> ret = bdd; at /home/olhotak/so" +
                                               "ot-2-jedd/src/soot/jimple/spark/queue/Rlocal_srcm_stmt_tgtmB" +
                                               "DD.jedd:47,8"),
                                              bdd);
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new jedd.internal.RelationContainer(new Attribute[] { tgtm.v(), local.v(), stmt.v(), srcm.v() },
                                                   new PhysicalDomain[] { T2.v(), V1.v(), ST.v(), T1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rlocal_srcm_stmt_tgtmBDD.jedd:49,8"),
                                                   ret);
    }
    
    public boolean hasNext() {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD());
    }
    
    private final class Tuple extends soot.jimple.spark.queue.Rlocal_srcm_stmt_tgtm.Tuple {
        private Object[] tuple;
        
        public Tuple(Object[] tuple) {
            super();
            this.tuple = tuple;
        }
        
        public Local local() { return (Local) tuple[0]; }
        
        public SootMethod srcm() { return (SootMethod) tuple[1]; }
        
        public Unit stmt() { return (Unit) tuple[2]; }
        
        public SootMethod tgtm() { return (SootMethod) tuple[3]; }
    }
    
}
