package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public class Qsrc_fld_dstTrad extends Qsrc_fld_dst {
    private ChunkedQueue q = new ChunkedQueue();
    
    public void add(VarNode _src, SparkField _fld, VarNode _dst) {
        q.add(_src);
        q.add(_fld);
        q.add(_dst);
    }
    
    public void add(final jedd.internal.RelationContainer in) {
        Iterator it =
          new jedd.internal.RelationContainer(new Attribute[] { dst.v(), fld.v(), src.v() },
                                              new PhysicalDomain[] { V2.v(), FD.v(), V1.v() },
                                              ("in.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-2" +
                                               "-jedd/src/soot/jimple/spark/queue/Qsrc_fld_dstTrad.jedd:38,2" +
                                               "2-24"),
                                              in).iterator(new Attribute[] { src.v(), fld.v(), dst.v() });
        while (it.hasNext()) {
            Object[] tuple = (Object[]) it.next();
            for (int i = 0; i < 3; i++) { add((VarNode) tuple[0], (SparkField) tuple[1], (VarNode) tuple[2]); }
        }
    }
    
    public Rsrc_fld_dst reader() { return new Rsrc_fld_dstTrad(q.reader()); }
    
    public Qsrc_fld_dstTrad() { super(); }
}
