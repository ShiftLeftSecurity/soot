package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class signature extends Attribute {
    public final SIG domain = (SIG) SIG.v();
    
    public Domain domain() { return domain; }
    
    public static Attribute v() { return instance; }
    
    private static Attribute instance = new signature();
    
    public signature() { super(); }
}
