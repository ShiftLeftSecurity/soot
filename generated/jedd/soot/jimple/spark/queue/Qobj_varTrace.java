package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Qobj_varTrace extends Qobj_varTrad {
    public Qobj_varTrace(String name) {
        super();
        this.name = name;
    }
    
    private String name;
    
    public void add(AllocNode _obj, VarNode _var) {
        System.out.print(name + ": ");
        System.out.print(_obj + ", ");
        System.out.print(_var + ", ");
        System.out.println();
        super.add(_obj, _var);
    }
}
