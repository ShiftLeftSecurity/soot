package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public abstract class Rsrc_fld_dst {
    public abstract Iterator iterator();
    
    public abstract jedd.internal.RelationContainer get();
    
    public abstract boolean hasNext();
    
    public abstract class Tuple {
        public abstract VarNode src();
        
        public abstract SparkField fld();
        
        public abstract VarNode dst();
        
        public String toString() {
            StringBuffer ret = new StringBuffer();
            ret.append(this.src());
            ret.append(", ");
            ret.append(this.fld());
            ret.append(", ");
            ret.append(this.dst());
            ret.append(", ");
            return ret.toString();
        }
        
        public Tuple() { super(); }
    }
    
    
    public Rsrc_fld_dst() { super(); }
}
