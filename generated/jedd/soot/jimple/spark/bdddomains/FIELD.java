package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class FIELD extends Domain {
    public Numberer numberer() { return Scene.v().getFieldNumberer(); }
    
    public final int bits = 20;
    
    public static Domain v() { return instance; }
    
    private static Domain instance = new FIELD();
    
    public FIELD() { super(); }
}
