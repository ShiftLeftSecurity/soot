package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class fld extends Attribute {
    public Numberer numberer() { return Scene.v().getFieldNumberer(); }
    
    public static Attribute v() { return fld.instance; }
    
    private static Attribute instance = new fld();
    
    public fld() { super(); }
}
