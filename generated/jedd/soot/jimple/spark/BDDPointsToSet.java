package soot.jimple.spark;

import soot.*;
import java.util.*;
import soot.jimple.spark.bdddomains.*;

public class BDDPointsToSet implements PointsToSet {
    private final jedd.internal.RelationContainer bdd =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v() },
                                          new jedd.PhysicalDomain[] { H1.v() },
                                          ("private final <soot.jimple.spark.bdddomains.obj:soot.jimple." +
                                           "spark.bdddomains.H1> bdd at /home/olhotak/soot-2-jedd/src/so" +
                                           "ot/jimple/spark/BDDPointsToSet.jedd:29,18"));
    
    public BDDPointsToSet(final jedd.internal.RelationContainer bdd) {
        super();
        this.bdd.eq(bdd);
    }
    
    public boolean isEmpty() {
        return jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(bdd), jedd.internal.Jedd.v().falseBDD());
    }
    
    public boolean hasNonEmptyIntersection(PointsToSet other) {
        BDDPointsToSet o = (BDDPointsToSet) other;
        return !jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().intersect(jedd.internal.Jedd.v().read(bdd),
                                                                                                           o.bdd)),
                                              jedd.internal.Jedd.v().falseBDD());
    }
    
    public Set possibleTypes() {
        final HashSet ret = new HashSet();
        Iterator it =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v() },
                                              new jedd.PhysicalDomain[] { H1.v() },
                                              ("bdd.iterator() at /home/olhotak/soot-2-jedd/src/soot/jimple/" +
                                               "spark/BDDPointsToSet.jedd:43,22"),
                                              bdd).iterator();
        while (it.hasNext()) {
            AllocNode an = (AllocNode) it.next();
            ret.add(an.getType());
        }
        return ret;
    }
    
    public Set possibleStringConstants() {
        final HashSet ret = new HashSet();
        Iterator it =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { obj.v() },
                                              new jedd.PhysicalDomain[] { H1.v() },
                                              ("bdd.iterator() at /home/olhotak/soot-2-jedd/src/soot/jimple/" +
                                               "spark/BDDPointsToSet.jedd:57,22"),
                                              bdd).iterator();
        while (it.hasNext()) {
            AllocNode an = (AllocNode) it.next();
            if (!(an instanceof StringConstantNode)) return null;
            StringConstantNode scn = (StringConstantNode) an;
            ret.add(scn.getString());
        }
        return ret;
    }
    
    public Set possibleClassConstants() { return Collections.EMPTY_SET; }
}
