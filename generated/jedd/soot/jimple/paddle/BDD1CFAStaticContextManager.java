package soot.jimple.paddle;

import soot.jimple.paddle.queue.*;
import soot.jimple.paddle.bdddomains.*;

public class BDD1CFAStaticContextManager extends AbsStaticContextManager {
    BDD1CFAStaticContextManager(Rsrcc_srcm_stmt_kind_tgtc_tgtm in, Qsrcc_srcm_stmt_kind_tgtc_tgtm out) {
        super(in, out);
    }
    
    public void update() {
        out.add(new jedd.internal.RelationContainer(new jedd.Attribute[] { srcc.v(), srcm.v(), stmt.v(), tgtc.v(), kind.v(), tgtm.v() },
                                                    new jedd.PhysicalDomain[] { V1.v(), T1.v(), ST.v(), V2.v(), FD.v(), T2.v() },
                                                    ("out.add(jedd.internal.Jedd.v().copy(jedd.internal.Jedd.v().p" +
                                                     "roject(in.get(), new jedd.PhysicalDomain[...]), new jedd.Phy" +
                                                     "sicalDomain[...], new jedd.PhysicalDomain[...])) at /home/ol" +
                                                     "hotak/soot-2-jedd/src/soot/jimple/paddle/BDD1CFAStaticContex" +
                                                     "tManager.jedd:35,8-11"),
                                                    jedd.internal.Jedd.v().copy(jedd.internal.Jedd.v().project(in.get(),
                                                                                                               new jedd.PhysicalDomain[] { V2.v() }),
                                                                                new jedd.PhysicalDomain[] { ST.v() },
                                                                                new jedd.PhysicalDomain[] { V2.v() })));
    }
}
