package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class anyst extends Domain {
    public Numberer numberer() { return Scene.v().getTypeNumberer(); }
    
    public static Domain v() { return anyst.instance; }
    
    private static Domain instance = new anyst();
    
    public anyst() { super(); }
}
