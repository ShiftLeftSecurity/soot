package soot.jimple.spark.queue;

import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.util.queue.*;
import jedd.*;
import java.util.*;

public final class Qsrc_dstTrace extends Qsrc_dstTrad {
    public Qsrc_dstTrace(String name) {
        super();
        this.name = name;
    }
    
    private String name;
    
    public void add(VarNode _src, VarNode _dst) {
        System.out.print(name + ": ");
        System.out.print(_src + ", ");
        System.out.print(_dst + ", ");
        System.out.println();
        super.add(_src, _dst);
    }
}
