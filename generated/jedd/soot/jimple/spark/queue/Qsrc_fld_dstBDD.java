package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Qsrc_fld_dstBDD extends Qsrc_fld_dst {
    private LinkedList readers = new LinkedList();
    
    public void add(VarNode _src, SparkField _fld, VarNode _dst) {
        this.add(new jedd.internal.RelationContainer(new Attribute[] { src.v(), fld.v(), dst.v() },
                                                     new PhysicalDomain[] { V1.v(), FD.v(), V2.v() },
                                                     ("this.add(jedd.internal.Jedd.v().literal(new java.lang.Object" +
                                                      "[...], new jedd.Attribute[...], new jedd.PhysicalDomain[...]" +
                                                      ")) at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/" +
                                                      "Qsrc_fld_dstBDD.jedd:33,8"),
                                                     jedd.internal.Jedd.v().literal(new Object[] { _src, _fld, _dst },
                                                                                    new Attribute[] { src.v(), fld.v(), dst.v() },
                                                                                    new PhysicalDomain[] { V1.v(), FD.v(), V2.v() })));
    }
    
    public void add(final jedd.internal.RelationContainer in) {
        for (Iterator it = readers.iterator(); it.hasNext(); ) {
            Rsrc_fld_dstBDD reader = (Rsrc_fld_dstBDD) it.next();
            reader.add(new jedd.internal.RelationContainer(new Attribute[] { dst.v(), src.v(), fld.v() },
                                                           new PhysicalDomain[] { V2.v(), V1.v(), FD.v() },
                                                           ("reader.add(in) at /home/olhotak/soot-2-jedd/src/soot/jimple/" +
                                                            "spark/queue/Qsrc_fld_dstBDD.jedd:38,12"),
                                                           in));
        }
    }
    
    public Rsrc_fld_dst reader() {
        Rsrc_fld_dst ret = new Rsrc_fld_dstBDD();
        readers.add(ret);
        return ret;
    }
    
    public Qsrc_fld_dstBDD() { super(); }
}
