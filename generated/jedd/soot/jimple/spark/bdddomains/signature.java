package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class signature extends Attribute {
    public Numberer numberer() { return Scene.v().getSubSigNumberer(); }
    
    public static Attribute v() { return signature.instance; }
    
    private static Attribute instance = new signature();
    
    public signature() { super(); }
}
