package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class tgtc extends Domain {
    public Numberer numberer() { return Scene.v().getContextNumberer(); }
    
    public static Domain v() { return tgtc.instance; }
    
    private static Domain instance = new tgtc();
    
    public tgtc() { super(); }
}
