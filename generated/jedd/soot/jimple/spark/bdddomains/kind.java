package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class kind extends Attribute {
    public final KIND domain = (KIND) KIND.v();
    
    public Domain domain() { return domain; }
    
    public static Attribute v() { return instance; }
    
    private static Attribute instance = new kind();
    
    public kind() { super(); }
}
