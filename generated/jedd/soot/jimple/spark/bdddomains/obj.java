package soot.jimple.spark.bdddomains;

import jedd.*;
import soot.*;
import soot.jimple.spark.pag.*;

public class obj extends Attribute {
    public final OBJ domain = (OBJ) OBJ.v();
    
    public Domain domain() { return domain; }
    
    public static Attribute v() { return instance; }
    
    private static Attribute instance = new obj();
    
    public obj() { super(); }
}
