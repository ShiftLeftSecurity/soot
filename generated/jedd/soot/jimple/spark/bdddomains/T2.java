package soot.jimple.spark.bdddomains;

import jedd.*;

public class T2 extends PhysicalDomain {
    public int bits() { return 15; }
    
    public static PhysicalDomain v() { return T2.instance; }
    
    private static PhysicalDomain instance = new T2();
    
    public T2() { super(); }
}
