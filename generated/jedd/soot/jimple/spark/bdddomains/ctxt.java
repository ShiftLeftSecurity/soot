package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class ctxt extends Domain {
    public Numberer numberer() { return Scene.v().getContextNumberer(); }
    
    public static Domain v() { return ctxt.instance; }
    
    private static Domain instance = new ctxt();
    
    public ctxt() { super(); }
}
