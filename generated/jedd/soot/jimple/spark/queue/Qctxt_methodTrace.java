package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Qctxt_methodTrace extends Qctxt_methodTrad {
    public Qctxt_methodTrace(String name) {
        super();
        this.name = name;
    }
    
    private String name;
    
    public void add(Context _ctxt, SootMethod _method) {
        System.out.print(name + ": ");
        System.out.print(_ctxt + ", ");
        System.out.print(_method + ", ");
        System.out.println();
        super.add(_ctxt, _method);
    }
}
