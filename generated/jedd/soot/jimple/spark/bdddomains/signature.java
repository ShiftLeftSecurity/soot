package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class signature extends Domain {
    public Numberer numberer() { return Scene.v().getSubSigNumberer(); }
    
    public static Domain v() { return signature.instance; }
    
    private static Domain instance = new signature();
    
    public signature() { super(); }
}
