package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class RobjBDD extends Robj {
    private final jedd.internal.RelationContainer bdd =
      new jedd.internal.RelationContainer(new Attribute[] { obj.v() },
                                          new PhysicalDomain[] { H1.v() },
                                          ("private <soot.jimple.spark.bdddomains.obj:soot.jimple.spark." +
                                           "bdddomains.H1> bdd at /home/olhotak/soot-2-jedd/src/soot/jim" +
                                           "ple/spark/queue/RobjBDD.jedd:31,12-20"));
    
    void add(final jedd.internal.RelationContainer tuple) { bdd.eqUnion(tuple); }
    
    public RobjBDD(final jedd.internal.RelationContainer bdd) {
        this();
        add(new jedd.internal.RelationContainer(new Attribute[] { obj.v() },
                                                new PhysicalDomain[] { H1.v() },
                                                ("add(bdd) at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/" +
                                                 "queue/RobjBDD.jedd:33,45-48"),
                                                bdd));
    }
    
    RobjBDD() {
        super();
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
    }
    
    public Iterator iterator() {
        ;
        return new Iterator() {
            private Iterator it;
            
            public boolean hasNext() {
                if (it != null && it.hasNext()) return true;
                if (!jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD()))
                    return true;
                return false;
            }
            
            public Object next() {
                if (it == null || !it.hasNext()) {
                    it =
                      new jedd.internal.RelationContainer(new Attribute[] { obj.v() },
                                                          new PhysicalDomain[] { H1.v() },
                                                          ("bdd.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-" +
                                                           "2-jedd/src/soot/jimple/spark/queue/RobjBDD.jedd:45,25-28"),
                                                          bdd).iterator(new Attribute[] { obj.v() });
                    bdd.eq(jedd.internal.Jedd.v().falseBDD());
                }
                Object[] components = (Object[]) it.next();
                return new Tuple((AllocNode) components[0]);
            }
            
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
    
    public jedd.internal.RelationContainer get() {
        final jedd.internal.RelationContainer ret =
          new jedd.internal.RelationContainer(new Attribute[] { obj.v() },
                                              new PhysicalDomain[] { H1.v() },
                                              ("<soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bdddomai" +
                                               "ns.H1> ret = bdd; at /home/olhotak/soot-2-jedd/src/soot/jimp" +
                                               "le/spark/queue/RobjBDD.jedd:55,17-20"),
                                              bdd);
        bdd.eq(jedd.internal.Jedd.v().falseBDD());
        return new jedd.internal.RelationContainer(new Attribute[] { obj.v() },
                                                   new PhysicalDomain[] { H1.v() },
                                                   ("return ret; at /home/olhotak/soot-2-jedd/src/soot/jimple/spa" +
                                                    "rk/queue/RobjBDD.jedd:57,8-14"),
                                                   ret);
    }
    
    public boolean hasNext() {
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD());
    }
}
