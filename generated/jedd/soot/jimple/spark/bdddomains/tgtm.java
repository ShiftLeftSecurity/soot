package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class tgtm extends Domain {
    public Numberer numberer() { return Scene.v().getMethodNumberer(); }
    
    public static Domain v() { return tgtm.instance; }
    
    private static Domain instance = new tgtm();
    
    public tgtm() { super(); }
}
