package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.jimple.spark.*;
import soot.*;

public class OBJ extends Domain {
    public Numberer numberer() { return SparkNumberers.v().allocNodeNumberer(); }
    
    public final int bits = 20;
    
    public static Domain v() { return instance; }
    
    private static Domain instance = new OBJ();
    
    public OBJ() { super(); }
}
