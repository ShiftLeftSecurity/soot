package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class method extends Attribute {
    public Numberer numberer() { return Scene.v().getMethodNumberer(); }
    
    public static Attribute v() { return method.instance; }
    
    private static Attribute instance = new method();
    
    public method() { super(); }
}
