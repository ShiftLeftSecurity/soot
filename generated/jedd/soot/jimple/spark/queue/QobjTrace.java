package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class QobjTrace extends QobjTrad {
    public QobjTrace(String name) {
        super();
        this.name = name;
    }
    
    private String name;
    
    public void add(AllocNode _obj) {
        System.out.print(name + ": ");
        System.out.print(_obj + ", ");
        System.out.println();
        super.add(_obj);
    }
}
