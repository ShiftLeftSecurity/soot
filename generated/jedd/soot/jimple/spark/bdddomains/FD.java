package soot.jimple.spark.bdddomains;

import jedd.*;

public class FD extends PhysicalDomain {
    public int bits() { return 10; }
    
    public static PhysicalDomain v() { return FD.instance; }
    
    private static PhysicalDomain instance = new FD();
    
    public FD() { super(); }
}
