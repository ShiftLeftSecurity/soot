package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class QobjBDD extends Qobj {
    private LinkedList readers = new LinkedList();
    
    public void add(AllocNode _obj) {
        add(new jedd.internal.RelationContainer(new Attribute[] { obj.v() },
                                                new PhysicalDomain[] { H1.v() },
                                                ("add(jedd.internal.Jedd.v().literal(new java.lang.Object[...]" +
                                                 ", new jedd.Attribute[...], new jedd.PhysicalDomain[...])) at" +
                                                 " /home/olhotak/soot-2-jedd/src/soot/jimple/spark/queue/QobjB" +
                                                 "DD.jedd:33,8-11"),
                                                jedd.internal.Jedd.v().literal(new Object[] { _obj },
                                                                               new Attribute[] { obj.v() },
                                                                               new PhysicalDomain[] { H1.v() })));
    }
    
    public void add(final jedd.internal.RelationContainer in) {
        for (Iterator it = readers.iterator(); it.hasNext(); ) {
            RobjBDD reader = (RobjBDD) it.next();
            reader.add(new jedd.internal.RelationContainer(new Attribute[] { obj.v() },
                                                           new PhysicalDomain[] { H1.v() },
                                                           ("reader.add(in) at /home/olhotak/soot-2-jedd/src/soot/jimple/" +
                                                            "spark/queue/QobjBDD.jedd:38,12-18"),
                                                           in));
        }
    }
    
    public Robj reader() {
        Robj ret = new RobjBDD();
        readers.add(ret);
        return ret;
    }
    
    public QobjBDD() { super(); }
}
