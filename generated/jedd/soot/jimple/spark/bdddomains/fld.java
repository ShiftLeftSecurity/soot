package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class fld extends Domain {
    public Numberer numberer() { return Scene.v().getFieldNumberer(); }
    
    public static Domain v() { return fld.instance; }
    
    private static Domain instance = new fld();
    
    public fld() { super(); }
}
