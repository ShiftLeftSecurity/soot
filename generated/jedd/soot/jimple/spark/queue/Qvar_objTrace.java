package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Qvar_objTrace extends Qvar_objTrad {
    public Qvar_objTrace(String name) {
        super();
        this.name = name;
    }
    
    private String name;
    
    public void add(VarNode _var, AllocNode _obj) {
        System.out.print(name + ": ");
        System.out.print(_var + ", ");
        System.out.print(_obj + ", ");
        System.out.println();
        super.add(_var, _obj);
    }
}
