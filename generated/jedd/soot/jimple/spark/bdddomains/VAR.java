package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.jimple.spark.*;
import soot.*;

public class VAR extends Domain {
    public Numberer numberer() { return SparkNumberers.v().varNodeNumberer(); }
    
    public final int bits = 20;
    
    public static Domain v() { return instance; }
    
    private static Domain instance = new VAR();
    
    public VAR() { super(); }
}
