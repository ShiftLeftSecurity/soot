package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class tgtm extends Attribute {
    public final METHOD domain = (METHOD) METHOD.v();
    
    public Domain domain() { return domain; }
    
    public static Attribute v() { return instance; }
    
    private static Attribute instance = new tgtm();
    
    public tgtm() { super(); }
}
