package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class kind extends Domain {
    public Numberer numberer() { return soot.jimple.toolkits.callgraph.KindNumberer.v(); }
    
    public static Domain v() { return kind.instance; }
    
    private static Domain instance = new kind();
    
    public kind() { super(); }
}
