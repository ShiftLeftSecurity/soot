package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class obj extends Domain {
    public Numberer numberer() { return ((BDDPAG) Scene.v().getPointsToAnalysis()).getAllocNodeNumberer(); }
    
    public static Domain v() { return obj.instance; }
    
    private static Domain instance = new obj();
    
    public obj() { super(); }
}
