package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class stmt extends Domain {
    public Numberer numberer() { return Scene.v().getUnitNumberer(); }
    
    public static Domain v() { return stmt.instance; }
    
    private static Domain instance = new stmt();
    
    public stmt() { super(); }
}
