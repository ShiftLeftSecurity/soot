package soot.jimple.spark.sets;

import soot.jimple.spark.*;
import soot.jimple.spark.pag.*;
import soot.*;
import java.util.*;
import soot.jimple.spark.bdddomains.*;

public class BDDPointsToSet implements PointsToSet {
    private final jedd.Relation bdd =
      new jedd.Relation(new jedd.Domain[] { obj.v() }, new jedd.PhysicalDomain[] { H1.v() });
    
    public BDDPointsToSet(final jedd.Relation bdd) {
        super();
        this.bdd.eq(bdd);
    }
    
    public boolean isEmpty() { return jedd.Jedd.v().equals(jedd.Jedd.v().read(this.bdd), jedd.Jedd.v().falseBDD()); }
    
    public boolean hasNonEmptyIntersection(PointsToSet other) {
        BDDPointsToSet o = (BDDPointsToSet) other;
        return !jedd.Jedd.v().equals(jedd.Jedd.v().read(jedd.Jedd.v().intersect(jedd.Jedd.v().read(this.bdd), o.bdd)),
                                     jedd.Jedd.v().falseBDD());
    }
    
    public Set possibleTypes() {
        final HashSet ret = new HashSet();
        Iterator it =
          new jedd.Relation(new jedd.Domain[] { obj.v() }, new jedd.PhysicalDomain[] { H1.v() }, this.bdd).iterator();
        while (it.hasNext()) {
            AllocNode an = (AllocNode) it.next();
            ret.add(an.getType());
        }
        return ret;
    }
    
    public Set possibleStringConstants() {
        final HashSet ret = new HashSet();
        Iterator it =
          new jedd.Relation(new jedd.Domain[] { obj.v() }, new jedd.PhysicalDomain[] { H1.v() }, this.bdd).iterator();
        while (it.hasNext()) {
            AllocNode an = (AllocNode) it.next();
            if (!(an instanceof StringConstantNode)) return null;
            StringConstantNode scn = (StringConstantNode) an;
            ret.add(scn.getString());
        }
        return ret;
    }
    
    public Set possibleClassConstants() {
        final HashSet ret = new HashSet();
        Iterator it =
          new jedd.Relation(new jedd.Domain[] { obj.v() }, new jedd.PhysicalDomain[] { H1.v() }, this.bdd).iterator();
        while (it.hasNext()) {
            AllocNode an = (AllocNode) it.next();
            if (!(an instanceof ClassConstantNode)) return null;
            ClassConstantNode scn = (ClassConstantNode) an;
            ret.add(scn.getString());
        }
        return ret;
    }
}
