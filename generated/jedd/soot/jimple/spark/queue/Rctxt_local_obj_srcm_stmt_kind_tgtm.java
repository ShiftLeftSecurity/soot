package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public abstract class Rctxt_local_obj_srcm_stmt_kind_tgtm {
    public abstract Iterator iterator();
    
    public abstract jedd.internal.RelationContainer get();
    
    public abstract boolean hasNext();
    
    public abstract class Tuple {
        public abstract Context ctxt();
        
        public abstract Local local();
        
        public abstract AllocNode obj();
        
        public abstract SootMethod srcm();
        
        public abstract Unit stmt();
        
        public abstract Kind kind();
        
        public abstract SootMethod tgtm();
        
        public String toString() {
            StringBuffer ret = new StringBuffer();
            ret.append(this.ctxt());
            ret.append(", ");
            ret.append(this.local());
            ret.append(", ");
            ret.append(this.obj());
            ret.append(", ");
            ret.append(this.srcm());
            ret.append(", ");
            ret.append(this.stmt());
            ret.append(", ");
            ret.append(this.kind());
            ret.append(", ");
            ret.append(this.tgtm());
            ret.append(", ");
            return ret.toString();
        }
        
        public Tuple() { super(); }
    }
    
    
    public Rctxt_local_obj_srcm_stmt_kind_tgtm() { super(); }
}
