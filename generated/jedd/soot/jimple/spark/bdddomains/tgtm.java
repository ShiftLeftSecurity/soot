package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class tgtm extends Attribute {
    public Numberer numberer() { return Scene.v().getMethodNumberer(); }
    
    public static Attribute v() { return tgtm.instance; }
    
    private static Attribute instance = new tgtm();
    
    public tgtm() { super(); }
}
