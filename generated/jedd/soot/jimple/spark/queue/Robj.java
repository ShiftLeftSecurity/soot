package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public abstract class Robj {
    public abstract Iterator iterator();
    
    public abstract jedd.internal.RelationContainer get();
    
    public abstract boolean hasNext();
    
    public abstract class Tuple {
        public abstract AllocNode obj();
        
        public String toString() {
            StringBuffer ret = new StringBuffer();
            ret.append(this.obj());
            ret.append(", ");
            return ret.toString();
        }
        
        public Tuple() { super(); }
    }
    
    
    public Robj() { super(); }
}
