package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class tgtc extends Attribute {
    public Numberer numberer() { return Scene.v().getContextNumberer(); }
    
    public static Attribute v() { return tgtc.instance; }
    
    private static Attribute instance = new tgtc();
    
    public tgtc() { super(); }
}
