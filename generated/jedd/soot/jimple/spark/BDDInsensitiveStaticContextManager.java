package soot.jimple.spark;

import soot.jimple.spark.queue.*;
import soot.jimple.spark.bdddomains.*;

public class BDDInsensitiveStaticContextManager extends AbsStaticContextManager {
    BDDInsensitiveStaticContextManager(Rsrcc_srcm_stmt_kind_tgtc_tgtm in, Qsrcc_srcm_stmt_kind_tgtc_tgtm out) {
        super(in, out);
    }
    
    public void update() {
        out.add(new jedd.internal.RelationContainer(new jedd.Attribute[] { srcc.v(), srcm.v(), stmt.v(), kind.v(), tgtc.v(), tgtm.v() },
                                                    new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), FD.v(), V2.v(), T2.v() },
                                                    ("out.add(in.get()) at /home/olhotak/soot-2-jedd/src/soot/jimp" +
                                                     "le/spark/BDDInsensitiveStaticContextManager.jedd:34,8"),
                                                    in.get()));
    }
}
