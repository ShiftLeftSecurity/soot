package soot.jimple.spark.bdddomains;

import jedd.*;

public class T3 extends PhysicalDomain {
    public int bits() { return 15; }
    
    public static PhysicalDomain v() { return T3.instance; }
    
    private static PhysicalDomain instance = new T3();
    
    public T3() { super(); }
}
