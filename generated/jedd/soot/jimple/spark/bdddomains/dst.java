package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class dst extends Attribute {
    public Numberer numberer() { return ((BDDPAG) Scene.v().getPointsToAnalysis()).getVarNodeNumberer(); }
    
    public static Attribute v() { return dst.instance; }
    
    private static Attribute instance = new dst();
    
    public dst() { super(); }
}
