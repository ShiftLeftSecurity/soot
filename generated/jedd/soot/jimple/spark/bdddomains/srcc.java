package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class srcc extends Domain {
    public Numberer numberer() { return Scene.v().getContextNumberer(); }
    
    public static Domain v() { return srcc.instance; }
    
    private static Domain instance = new srcc();
    
    public srcc() { super(); }
}
