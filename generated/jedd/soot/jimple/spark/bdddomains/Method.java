package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class Method extends Domain {
    public Numberer numberer() { return Scene.v().getMethodNumberer(); }
    
    public final int bits = 20;
    
    public static Domain v() { return instance; }
    
    private static Domain instance = new Method();
    
    public Method() { super(); }
}
