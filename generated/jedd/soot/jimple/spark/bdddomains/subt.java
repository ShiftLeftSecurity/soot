package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class subt extends Attribute {
    public Numberer numberer() { return Scene.v().getTypeNumberer(); }
    
    public static Attribute v() { return subt.instance; }
    
    private static Attribute instance = new subt();
    
    public subt() { super(); }
}
