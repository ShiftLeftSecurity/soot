package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.jimple.spark.*;
import soot.*;

public class LOCAL extends Domain {
    public Numberer numberer() { return Scene.v().getLocalNumberer(); }
    
    public final int bits = 20;
    
    public static Domain v() { return instance; }
    
    private static Domain instance = new LOCAL();
    
    public LOCAL() { super(); }
}
