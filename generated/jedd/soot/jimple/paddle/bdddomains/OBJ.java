package soot.jimple.paddle.bdddomains;

import jedd.*;
import soot.jimple.paddle.*;
import soot.*;

public class OBJ extends Domain {
    public Numberer numberer() { return PaddleNumberers.v().allocNodeNumberer(); }
    
    public final int bits = 20;
    
    public static Domain v() { return instance; }
    
    private static Domain instance = new OBJ();
    
    public OBJ() { super(); }
}
