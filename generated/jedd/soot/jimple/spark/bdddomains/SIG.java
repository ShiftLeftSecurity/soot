package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.jimple.spark.pag.*;
import soot.*;

public class SIG extends Domain {
    public Numberer numberer() { return Scene.v().getSubSigNumberer(); }
    
    public final int bits = 20;
    
    public static Domain v() { return instance; }
    
    private static Domain instance = new SIG();
    
    public SIG() { super(); }
}
