package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class var extends Domain {
    public Numberer numberer() { return ((BDDPAG) Scene.v().getPointsToAnalysis()).getVarNodeNumberer(); }
    
    public static Domain v() { return var.instance; }
    
    private static Domain instance = new var();
    
    public var() { super(); }
}
