package soot.jimple.toolkits.callgraph;

import soot.*;
import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.*;
import java.util.*;

public final class BDDVirtualCalls {
    public BDDVirtualCalls(BDDHierarchy hier) {
        super();
        this.hier = hier;
        this.initialize();
    }
    
    public void addTypes(final jedd.internal.RelationContainer newTypes) {
        hier.update();
        newTypes.eqMinus(jedd.internal.Jedd.v().project(answer, new jedd.PhysicalDomain[] { V2.v() }));
        final jedd.internal.RelationContainer toResolve =
          new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), signature.v(), supt.v() },
                                              new jedd.PhysicalDomain[] { T1.v(), H1.v(), T2.v() },
                                              ("<soot.jimple.spark.bdddomains.subt:soot.jimple.spark.bdddoma" +
                                               "ins.T1, soot.jimple.spark.bdddomains.signature:soot.jimple.s" +
                                               "park.bdddomains.H1, soot.jimple.spark.bdddomains.supt:soot.j" +
                                               "imple.spark.bdddomains.T2> toResolve = jedd.internal.Jedd.v(" +
                                               ").copy(newTypes, new jedd.PhysicalDomain[...], new jedd.Phys" +
                                               "icalDomain[...]); at /home/olhotak/soot-2-jedd/src/soot/jimp" +
                                               "le/toolkits/callgraph/BDDVirtualCalls.jedd:47,8"),
                                              jedd.internal.Jedd.v().copy(newTypes,
                                                                          new jedd.PhysicalDomain[] { T1.v() },
                                                                          new jedd.PhysicalDomain[] { T2.v() }));
        toResolve.eqUnion(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(toResolve),
                                                                                        jedd.internal.Jedd.v().replace(hier.anySub(),
                                                                                                                       new jedd.PhysicalDomain[] { T2.v() },
                                                                                                                       new jedd.PhysicalDomain[] { T3.v() }),
                                                                                        new jedd.PhysicalDomain[] { T1.v() }),
                                                         new jedd.PhysicalDomain[] { T3.v() },
                                                         new jedd.PhysicalDomain[] { T1.v() }));
        do  {
            final jedd.internal.RelationContainer resolved =
              new jedd.internal.RelationContainer(new jedd.Attribute[] { subt.v(), signature.v(), supt.v(), method.v() },
                                                  new jedd.PhysicalDomain[] { T1.v(), H1.v(), T2.v(), V2.v() },
                                                  ("<soot.jimple.spark.bdddomains.subt:soot.jimple.spark.bdddoma" +
                                                   "ins.T1, soot.jimple.spark.bdddomains.signature:soot.jimple.s" +
                                                   "park.bdddomains.H1, soot.jimple.spark.bdddomains.supt:soot.j" +
                                                   "imple.spark.bdddomains.T2, soot.jimple.spark.bdddomains.meth" +
                                                   "od:soot.jimple.spark.bdddomains.V2> resolved = jedd.internal" +
                                                   ".Jedd.v().join(jedd.internal.Jedd.v().read(toResolve), decla" +
                                                   "resMethod, new jedd.PhysicalDomain[...]); at /home/olhotak/s" +
                                                   "oot-2-jedd/src/soot/jimple/toolkits/callgraph/BDDVirtualCall" +
                                                   "s.jedd:58,12"),
                                                  jedd.internal.Jedd.v().join(jedd.internal.Jedd.v().read(toResolve),
                                                                              declaresMethod,
                                                                              new jedd.PhysicalDomain[] { T2.v(), H1.v() }));
            toResolve.eqMinus(jedd.internal.Jedd.v().project(resolved, new jedd.PhysicalDomain[] { V2.v() }));
            answer.eqUnion(jedd.internal.Jedd.v().project(resolved, new jedd.PhysicalDomain[] { T2.v() }));
            toResolve.eq(jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(toResolve),
                                                                                       jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().replace(hier.extend(),
                                                                                                                                                     new jedd.PhysicalDomain[] { T2.v() },
                                                                                                                                                     new jedd.PhysicalDomain[] { T3.v() }),
                                                                                                                      new jedd.PhysicalDomain[] { T1.v() },
                                                                                                                      new jedd.PhysicalDomain[] { T2.v() }),
                                                                                       new jedd.PhysicalDomain[] { T2.v() }),
                                                        new jedd.PhysicalDomain[] { T3.v() },
                                                        new jedd.PhysicalDomain[] { T2.v() }));
        }while(!jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(toResolve),
                                              jedd.internal.Jedd.v().falseBDD())); 
    }
    
    public jedd.internal.RelationContainer answer() {
        return new jedd.internal.RelationContainer(new jedd.Attribute[] { signature.v(), type.v(), method.v() },
                                                   new jedd.PhysicalDomain[] { H1.v(), T1.v(), V2.v() },
                                                   ("return answer; at /home/olhotak/soot-2-jedd/src/soot/jimple/" +
                                                    "toolkits/callgraph/BDDVirtualCalls.jedd:73,8"),
                                                   answer);
    }
    
    private final jedd.internal.RelationContainer declaresMethod =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { type.v(), signature.v(), method.v() },
                                          new jedd.PhysicalDomain[] { T2.v(), H1.v(), V2.v() },
                                          ("private <soot.jimple.spark.bdddomains.type:soot.jimple.spark" +
                                           ".bdddomains.T2, soot.jimple.spark.bdddomains.signature:soot." +
                                           "jimple.spark.bdddomains.H1, soot.jimple.spark.bdddomains.met" +
                                           "hod:soot.jimple.spark.bdddomains.V2> declaresMethod = jedd.i" +
                                           "nternal.Jedd.v().falseBDD() at /home/olhotak/soot-2-jedd/src" +
                                           "/soot/jimple/toolkits/callgraph/BDDVirtualCalls.jedd:77,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final jedd.internal.RelationContainer answer =
      new jedd.internal.RelationContainer(new jedd.Attribute[] { type.v(), signature.v(), method.v() },
                                          new jedd.PhysicalDomain[] { T1.v(), H1.v(), V2.v() },
                                          ("private <soot.jimple.spark.bdddomains.type:soot.jimple.spark" +
                                           ".bdddomains.T1, soot.jimple.spark.bdddomains.signature:soot." +
                                           "jimple.spark.bdddomains.H1, soot.jimple.spark.bdddomains.met" +
                                           "hod:soot.jimple.spark.bdddomains.V2> answer = jedd.internal." +
                                           "Jedd.v().falseBDD() at /home/olhotak/soot-2-jedd/src/soot/ji" +
                                           "mple/toolkits/callgraph/BDDVirtualCalls.jedd:78,12"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private BDDHierarchy hier;
    
    private void initialize() {
        for (Iterator clIt = Scene.v().getClasses().iterator(); clIt.hasNext(); ) {
            final SootClass cl = (SootClass) clIt.next();
            for (Iterator mIt = cl.getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                if (m.isAbstract()) continue;
                declaresMethod.eqUnion(jedd.internal.Jedd.v().literal(new Object[] { m.getDeclaringClass().getType(), m.getNumberedSubSignature(), m },
                                                                      new jedd.Attribute[] { type.v(), signature.v(), method.v() },
                                                                      new jedd.PhysicalDomain[] { T2.v(), H1.v(), V2.v() }));
            }
        }
    }
}
