package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class base extends Attribute {
    public Numberer numberer() { return ((BDDPAG) Scene.v().getPointsToAnalysis()).getVarNodeNumberer(); }
    
    public static Attribute v() { return base.instance; }
    
    private static Attribute instance = new base();
    
    public base() { super(); }
}
