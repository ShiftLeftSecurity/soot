package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class STMT extends Domain {
    public Numberer numberer() { return Scene.v().getUnitNumberer(); }
    
    public final int bits = 20;
    
    public static Domain v() { return instance; }
    
    private static Domain instance = new STMT();
    
    public STMT() { super(); }
}
