package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.jimple.spark.pag.*;
import soot.*;

public class KIND extends Domain {
    public Numberer numberer() { return Scene.v().kindNumberer(); }
    
    public final int bits = 4;
    
    public static Domain v() { return instance; }
    
    private static Domain instance = new KIND();
    
    public KIND() { super(); }
}
