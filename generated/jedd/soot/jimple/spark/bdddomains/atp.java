package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class atp extends Attribute {
    public Numberer numberer() { return Scene.v().getTypeNumberer(); }
    
    public static Attribute v() { return atp.instance; }
    
    private static Attribute instance = new atp();
    
    public atp() { super(); }
}
