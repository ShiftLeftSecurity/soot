package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public class Rctxt_local_obj_srcm_stmt_kind_tgtmIter extends Rctxt_local_obj_srcm_stmt_kind_tgtm {
    protected Iterator r;
    
    public Rctxt_local_obj_srcm_stmt_kind_tgtmIter(Iterator r) {
        super();
        this.r = r;
    }
    
    public Iterator iterator() {
        ;
        return new Iterator() {
            public boolean hasNext() { return r.hasNext(); }
            
            public Object next() {
                return Rctxt_local_obj_srcm_stmt_kind_tgtmIter.this.new Tuple((Context) r.next(),
                                                                              (Local) r.next(),
                                                                              (AllocNode) r.next(),
                                                                              (SootMethod) r.next(),
                                                                              (Unit) r.next(),
                                                                              (Kind) r.next(),
                                                                              (SootMethod) r.next());
            }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { ctxt.v(), local.v(), obj.v(), srcm.v(), stmt.v(), kind.v(), tgtm.v() },
                                              new PhysicalDomain[] { V2.v(), V1.v(), H1.v(), T1.v(), ST.v(), FD.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.ctxt:soot.jimple.spark.bdddoma" +
                                               "ins.V2, soot.jimple.spark.bdddomains.local:soot.jimple.spark" +
                                               ".bdddomains.V1, soot.jimple.spark.bdddomains.obj:soot.jimple" +
                                               ".spark.bdddomains.H1, soot.jimple.spark.bdddomains.srcm:soot" +
                                               ".jimple.spark.bdddomains.T1, soot.jimple.spark.bdddomains.st" +
                                               "mt:soot.jimple.spark.bdddomains.ST, soot.jimple.spark.bdddom" +
                                               "ains.kind:soot.jimple.spark.bdddomains.FD, soot.jimple.spark" +
                                               ".bdddomains.tgtm:soot.jimple.spark.bdddomains.T2> ret = jedd" +
                                               ".internal.Jedd.v().falseBDD(); at /home/olhotak/soot-2-jedd/" +
                                               "src/soot/jimple/spark/queue/Rctxt_local_obj_srcm_stmt_kind_t" +
                                               "gtmIter.jedd:43,8"),
                                              jedd.internal.Jedd.v().falseBDD());
        while (r.hasNext()) {
            ret.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { r.next(), r.next(), r.next(), r.next(), r.next(), r.next(), r.next() },
                                                       new Attribute[] { ctxt.v(), local.v(), obj.v(), srcm.v(), stmt.v(), kind.v(), tgtm.v() },
                                                       new PhysicalDomain[] { V2.v(), V1.v(), H1.v(), T1.v(), ST.v(), FD.v(), T2.v() }));
        }
        return new jedd.internal.RelationContainer(new Attribute[] { obj.v(), ctxt.v(), tgtm.v(), local.v(), kind.v(), stmt.v(), srcm.v() },
                                                   new PhysicalDomain[] { H1.v(), V2.v(), T2.v(), V1.v(), FD.v(), ST.v(), T1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/Rctxt_local_obj_srcm_stmt_kind_tgtmIter.jedd:47,8"),
                                                   ret);
    }
    
    public boolean hasNext() { return r.hasNext(); }
    
    private final class Tuple extends soot.jimple.spark.queue.Rctxt_local_obj_srcm_stmt_kind_tgtm.Tuple {
        private Context _ctxt;
        
        public Context ctxt() { return _ctxt; }
        
        private Local _local;
        
        public Local local() { return _local; }
        
        private AllocNode _obj;
        
        public AllocNode obj() { return _obj; }
        
        private SootMethod _srcm;
        
        public SootMethod srcm() { return _srcm; }
        
        private Unit _stmt;
        
        public Unit stmt() { return _stmt; }
        
        private Kind _kind;
        
        public Kind kind() { return _kind; }
        
        private SootMethod _tgtm;
        
        public SootMethod tgtm() { return _tgtm; }
        
        public Tuple(Context _ctxt,
                     Local _local,
                     AllocNode _obj,
                     SootMethod _srcm,
                     Unit _stmt,
                     Kind _kind,
                     SootMethod _tgtm) {
            super();
            this._ctxt = _ctxt;
            this._local = _local;
            this._obj = _obj;
            this._srcm = _srcm;
            this._stmt = _stmt;
            this._kind = _kind;
            this._tgtm = _tgtm;
        }
    }
    
}
