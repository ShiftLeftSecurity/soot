package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class var extends Attribute {
    public Numberer numberer() { return ((BDDPAG) Scene.v().getPointsToAnalysis()).getVarNodeNumberer(); }
    
    public static Attribute v() { return var.instance; }
    
    private static Attribute instance = new var();
    
    public var() { super(); }
}
