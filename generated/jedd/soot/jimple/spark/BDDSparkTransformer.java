package soot.jimple.spark;

import soot.*;
import soot.jimple.spark.pag.*;
import soot.jimple.toolkits.callgraph.*;
import soot.jimple.toolkits.pointer.*;
import soot.jimple.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import soot.util.*;
import soot.options.*;
import soot.tagkit.*;
import jedd.*;
import soot.jimple.spark.bdddomains.*;

public class BDDSparkTransformer extends AbstractSparkTransformer {
    public BDDSparkTransformer(Singletons.Global g) { super(); }
    
    public static BDDSparkTransformer v() { return G.v().soot_jimple_spark_BDDSparkTransformer(); }
    
    protected void internalTransform(String phaseName, Map options) {  }
}
