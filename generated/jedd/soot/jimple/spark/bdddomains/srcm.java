package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class srcm extends Domain {
    public Numberer numberer() { return Scene.v().getMethodNumberer(); }
    
    public static Domain v() { return srcm.instance; }
    
    private static Domain instance = new srcm();
    
    public srcm() { super(); }
}
