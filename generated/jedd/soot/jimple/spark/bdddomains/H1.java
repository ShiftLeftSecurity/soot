package soot.jimple.spark.bdddomains;

import jedd.*;

public class H1 extends PhysicalDomain {
    public int bits() { return 12; }
    
    public static PhysicalDomain v() { return H1.instance; }
    
    private static PhysicalDomain instance = new H1();
    
    public H1() { super(); }
}
