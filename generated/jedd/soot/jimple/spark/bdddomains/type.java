package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class type extends Domain {
    public Numberer numberer() { return Scene.v().getTypeNumberer(); }
    
    public static Domain v() { return type.instance; }
    
    private static Domain instance = new type();
    
    public type() { super(); }
}
