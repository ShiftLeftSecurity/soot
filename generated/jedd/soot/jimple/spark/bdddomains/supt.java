package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class supt extends Attribute {
    public Numberer numberer() { return Scene.v().getTypeNumberer(); }
    
    public static Attribute v() { return supt.instance; }
    
    private static Attribute instance = new supt();
    
    public supt() { super(); }
}
