package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class TYPE extends Domain {
    public Numberer numberer() { return Scene.v().getTypeNumberer(); }
    
    public final int bits = 20;
    
    public static Domain v() { return instance; }
    
    private static Domain instance = new TYPE();
    
    public TYPE() { super(); }
}
