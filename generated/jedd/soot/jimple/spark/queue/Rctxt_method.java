package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public abstract class Rctxt_method {
    public abstract Iterator iterator();
    
    public abstract jedd.internal.RelationContainer get();
    
    public abstract boolean hasNext();
    
    public abstract class Tuple {
        public abstract Context ctxt();
        
        public abstract SootMethod method();
        
        public String toString() {
            StringBuffer ret = new StringBuffer();
            ret.append(this.ctxt());
            ret.append(", ");
            ret.append(this.method());
            ret.append(", ");
            return ret.toString();
        }
        
        public Tuple() { super(); }
    }
    
    
    public Rctxt_method() { super(); }
}
