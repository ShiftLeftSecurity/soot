package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class method extends Domain {
    public Numberer numberer() { return Scene.v().getMethodNumberer(); }
    
    public static Domain v() { return method.instance; }
    
    private static Domain instance = new method();
    
    public method() { super(); }
}
