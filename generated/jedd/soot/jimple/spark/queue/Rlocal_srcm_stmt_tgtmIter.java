package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public class Rlocal_srcm_stmt_tgtmIter extends Rlocal_srcm_stmt_tgtm {
    protected Iterator r;
    
    public Rlocal_srcm_stmt_tgtmIter(Iterator r) {
        super();
        this.r = r;
    }
    
    public Iterator iterator() {
        ;
        return new Iterator() {
            public boolean hasNext() { return r.hasNext(); }
            
            public Object next() {
                return Rlocal_srcm_stmt_tgtmIter.this.new Tuple((Local) r.next(),
                                                                (SootMethod) r.next(),
                                                                (Unit) r.next(),
                                                                (SootMethod) r.next());
            }
            
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
                                               "t.jimple.spark.bdddomains.T2> ret = jedd.internal.Jedd.v().f" +
                                               "alseBDD(); at /home/olhotak/soot-2-jedd/src/soot/jimple/spar" +
                                               "k/queue/Rlocal_srcm_stmt_tgtmIter.jedd:43,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        while (r.hasNext()) {
            ret.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { r.next(), r.next(), r.next(), r.next() },
                                                       new Attribute[] { local.v(), srcm.v(), stmt.v(), tgtm.v() },
                                                       new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), T2.v() }));
        }
        return new jedd.internal.RelationContainer(new Attribute[] { tgtm.v(), local.v(), stmt.v(), srcm.v() },
                                                   new PhysicalDomain[] { T2.v(), V1.v(), ST.v(), T1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rlocal_srcm_stmt_tgtmIter.jedd:47,8"),
                                                   ret);
    }
    
    public boolean hasNext() { return r.hasNext(); }
    
    private final class Tuple extends soot.jimple.spark.queue.Rlocal_srcm_stmt_tgtm.Tuple {
        private Local _local;
        
        public Local local() { return _local; }
        
        private SootMethod _srcm;
        
        public SootMethod srcm() { return _srcm; }
        
        private Unit _stmt;
        
        public Unit stmt() { return _stmt; }
        
        private SootMethod _tgtm;
        
        public SootMethod tgtm() { return _tgtm; }
        
        public Tuple(Local _local, SootMethod _srcm, Unit _stmt, SootMethod _tgtm) {
            super();
            this._local = _local;
            this._srcm = _srcm;
            this._stmt = _stmt;
            this._tgtm = _tgtm;
        }
    }
    
}
