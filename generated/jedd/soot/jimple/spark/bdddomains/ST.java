package soot.jimple.spark.bdddomains;

import jedd.*;

public class ST extends PhysicalDomain {
    public int bits() { return 15; }
    
    public static PhysicalDomain v() { return ST.instance; }
    
    private static PhysicalDomain instance = new ST();
    
    public ST() { super(); }
}
