package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public abstract class Rlocal_srcm_stmt_tgtm {
    public abstract Iterator iterator();
    
    public abstract jedd.internal.RelationContainer get();
    
    public abstract boolean hasNext();
    
    public abstract class Tuple {
        public abstract Local local();
        
        public abstract SootMethod srcm();
        
        public abstract Unit stmt();
        
        public abstract SootMethod tgtm();
        
        public String toString() {
            StringBuffer ret = new StringBuffer();
            ret.append(this.local());
            ret.append(", ");
            ret.append(this.srcm());
            ret.append(", ");
            ret.append(this.stmt());
            ret.append(", ");
            ret.append(this.tgtm());
            ret.append(", ");
            return ret.toString();
        }
        
        public Tuple() { super(); }
    }
    
    
    public Rlocal_srcm_stmt_tgtm() { super(); }
}
