package soot.jimple.spark;

import soot.*;
import soot.util.*;
import soot.jimple.spark.queue.*;
import soot.jimple.spark.bdddomains.*;
import java.util.*;

public class TradTypeManager extends AbsTypeManager {
    TradTypeManager(Rvar vars, Robj allocs, FastHierarchy fh) {
        super(vars, allocs);
        this.fh = fh;
    }
    
    public void update() {
        for (Iterator tIt = vars.iterator(); tIt.hasNext(); ) {
            final soot.jimple.spark.queue.Rvar.Tuple t = (soot.jimple.spark.queue.Rvar.Tuple) tIt.next();
            VarNode vn = t.var();
            Type type = vn.getType();
            if (typeMask.get(type) != null) continue;
            if (type.toString().indexOf("Entry") >= 0)
                System.out.println("updating typemasks for " + type.getNumber() + ":" + type);
            BitVector bv = new BitVector(allocNodes.size());
            typeMask.put(type, bv);
            for (Iterator anIt = allocNodes.iterator(); anIt.hasNext(); ) {
                final AllocNode an = (AllocNode) anIt.next();
                if (type.toString().indexOf("Entry") >= 0)
                    System.out.println("updating with an for " + an.getType().getNumber() + ":" + an);
                if (this.castNeverFails(an.getType(), type)) { bv.set(an.getNumber()); }
            }
            if (type.toString().indexOf("Entry") >= 0)
                System.out.println("updating typemasks for " + type.getNumber() + ":" + type);
        }
        for (Iterator tIt = allocs.iterator(); tIt.hasNext(); ) {
            final soot.jimple.spark.queue.Robj.Tuple t = (soot.jimple.spark.queue.Robj.Tuple) tIt.next();
            AllocNode an = t.obj();
            allocNodes.add(an);
            if (an.getType().toString().indexOf("Entry") >= 0)
                System.out.println("updating typemasks for allocnode " + an);
            for (Iterator typeIt = Scene.v().getTypeNumberer().iterator(); typeIt.hasNext(); ) {
                final Type type = (Type) typeIt.next();
                if (!(type instanceof RefLikeType)) continue;
                if (type instanceof AnySubType) continue;
                BitVector bv = (BitVector) typeMask.get(type);
                if (bv == null) continue;
                if (an.getType().toString().indexOf("Entry") >= 0)
                    System.out.println("updating typemasks for allocnode with type " + type.getNumber() + ":" + type);
                if (this.castNeverFails(an.getType(), type)) { bv.set(an.getNumber()); }
            }
        }
    }
    
    public BitVector get(Type type) {
        if (type == null) return null;
        this.update();
        BitVector ret = (BitVector) typeMask.get(type);
        if (ret == null && fh != null) throw new RuntimeException("oops" + type);
        System.out.println("getting typemask for " + type);
        return ret;
    }
    
    public boolean castNeverFails(Type from, Type to) {
        boolean ret = this.castNeverFailsGuts(from, to);
        if (from != null && from.toString().indexOf("Entry") >= 0) {
            System.out.println("castNeverFails from=" + from + " to=" + to + ":" + ret);
        }
        return ret;
    }
    
    private boolean castNeverFailsGuts(Type from, Type to) {
        if (fh == null) return true;
        if (to == null) return true;
        if (to == from) return true;
        if (from == null) return false;
        if (to.equals(from)) return true;
        if (from instanceof NullType) return true;
        if (from instanceof AnySubType) return true;
        if (to instanceof NullType) return false;
        if (to instanceof AnySubType) throw new RuntimeException("oops from=" + from + " to=" + to);
        return fh.canStoreType(from, to);
    }
    
    public jedd.internal.RelationContainer get() { throw new RuntimeException("NYI"); }
    
    private LargeNumberedMap typeMask = new LargeNumberedMap(Scene.v().getTypeNumberer());
    
    private NumberedSet allocNodes = new NumberedSet(SparkNumberers.v().allocNodeNumberer());
    
    private FastHierarchy fh;
}
