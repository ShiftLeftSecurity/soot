package soot.jimple.paddle.queue;

import soot.util.*;
import soot.jimple.paddle.bdddomains.*;
import soot.jimple.paddle.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Qlocal_srcm_stmt_signature_kindBDD extends Qlocal_srcm_stmt_signature_kind {
    private LinkedList readers = new LinkedList();
    
    public void add(Local _local, SootMethod _srcm, Unit _stmt, NumberedString _signature, Kind _kind) {
        add(new jedd.internal.RelationContainer(new Attribute[] { local.v(), srcm.v(), stmt.v(), signature.v(), kind.v() },
                                                new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), FD.v() },
                                                ("add(jedd.internal.Jedd.v().literal(new java.lang.Object[...]" +
                                                 ", new jedd.Attribute[...], new jedd.PhysicalDomain[...])) at" +
                                                 " /home/olhotak/soot-2-jedd/src/soot/jimple/paddle/queue/Qloc" +
                                                 "al_srcm_stmt_signature_kindBDD.jedd:33,8-11"),
                                                jedd.internal.Jedd.v().literal(new Object[] { _local, _srcm, _stmt, _signature, _kind },
                                                                               new Attribute[] { local.v(), srcm.v(), stmt.v(), signature.v(), kind.v() },
                                                                               new PhysicalDomain[] { V1.v(), T1.v(), ST.v(), H2.v(), FD.v() })));
    }
    
    public void add(final jedd.internal.RelationContainer in) {
        for (Iterator it = readers.iterator(); it.hasNext(); ) {
            Rlocal_srcm_stmt_signature_kindBDD reader = (Rlocal_srcm_stmt_signature_kindBDD) it.next();
            reader.add(new jedd.internal.RelationContainer(new Attribute[] { srcm.v(), local.v(), kind.v(), signature.v(), stmt.v() },
                                                           new PhysicalDomain[] { T1.v(), V1.v(), FD.v(), H2.v(), ST.v() },
                                                           ("reader.add(in) at /home/olhotak/soot-2-jedd/src/soot/jimple/" +
                                                            "paddle/queue/Qlocal_srcm_stmt_signature_kindBDD.jedd:38,12-1" +
                                                            "8"),
                                                           in));
        }
    }
    
    public Rlocal_srcm_stmt_signature_kind reader() {
        Rlocal_srcm_stmt_signature_kind ret = new Rlocal_srcm_stmt_signature_kindBDD();
        readers.add(ret);
        return ret;
    }
    
    public Qlocal_srcm_stmt_signature_kindBDD() { super(); }
}
