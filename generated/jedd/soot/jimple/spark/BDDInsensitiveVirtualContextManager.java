package soot.jimple.spark;

import soot.jimple.spark.queue.*;
import soot.jimple.spark.bdddomains.*;
import soot.*;

public class BDDInsensitiveVirtualContextManager extends AbsVirtualContextManager {
    BDDInsensitiveVirtualContextManager(Rctxt_local_obj_srcm_stmt_kind_tgtm in, Qsrcc_srcm_stmt_kind_tgtc_tgtm out) {
        super(in, out);
    }
    
    public void update() {
        out.add(new jedd.internal.RelationContainer(new jedd.Attribute[] { srcm.v(), tgtm.v(), stmt.v(), kind.v(), srcc.v(), tgtc.v() },
                                                    new jedd.PhysicalDomain[] { T1.v(), T2.v(), ST.v(), FD.v(), V1.v(), V2.v() },
                                                    ("out.add(jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().r" +
                                                     "ead(jedd.internal.Jedd.v().project(in.get(), new jedd.Physic" +
                                                     "alDomain[...])), jedd.internal.Jedd.v().literal(new java.lan" +
                                                     "g.Object[...], new jedd.Attribute[...], new jedd.PhysicalDom" +
                                                     "ain[...]), new jedd.PhysicalDomain[...])) at /home/olhotak/s" +
                                                     "oot-2-jedd/src/soot/jimple/spark/BDDInsensitiveVirtualContex" +
                                                     "tManager.jedd:35,8-11"),
                                                    jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().project(in.get(),
                                                                                                                                           new jedd.PhysicalDomain[] { V1.v(), V2.v(), H1.v() })),
                                                                                jedd.internal.Jedd.v().literal(new Object[] { null, null },
                                                                                                               new jedd.Attribute[] { srcc.v(), tgtc.v() },
                                                                                                               new jedd.PhysicalDomain[] { V1.v(), V2.v() }),
                                                                                new jedd.PhysicalDomain[] {  })));
    }
}
