package soot.jimple.toolkits.callgraph;

import soot.*;
import soot.util.*;
import soot.jimple.spark.bdddomains.*;
import soot.jimple.spark.internal.*;
import java.util.*;

public final class BDDVirtualCalls {
    public BDDVirtualCalls(BDDHierarchy hier) {
        super();
        this.hier = hier;
        this.initialize();
    }
    
    public void addTypes(final jedd.Relation newTypes) {
        this.hier.update();
        newTypes.eqMinus(jedd.Jedd.v().project(jedd.Jedd.v().replace(this.answer,
                                                                     new jedd.PhysicalDomain[] { T1.v() },
                                                                     new jedd.PhysicalDomain[] { T2.v() }),
                                               new jedd.PhysicalDomain[] { V2.v() }));
        final jedd.Relation toResolve =
          new jedd.Relation(new jedd.Attribute[] { subt.v(), signature.v(), supt.v() },
                            new jedd.PhysicalDomain[] { T1.v(), H1.v(), T2.v() },
                            jedd.Jedd.v().join(jedd.Jedd.v().read(jedd.Jedd.v().replace(newTypes,
                                                                                        new jedd.PhysicalDomain[] { T2.v() },
                                                                                        new jedd.PhysicalDomain[] { T1.v() })),
                                               newTypes,
                                               new jedd.PhysicalDomain[] { H1.v() }));
        toResolve.eqUnion(jedd.Jedd.v().replace(jedd.Jedd.v().compose(jedd.Jedd.v().read(toResolve),
                                                                      jedd.Jedd.v().replace(this.hier.anySub(),
                                                                                            new jedd.PhysicalDomain[] { T2.v() },
                                                                                            new jedd.PhysicalDomain[] { T3.v() }),
                                                                      new jedd.PhysicalDomain[] { T1.v() }),
                                                new jedd.PhysicalDomain[] { T3.v() },
                                                new jedd.PhysicalDomain[] { T1.v() }));
        do  {
            final jedd.Relation resolved =
              new jedd.Relation(new jedd.Attribute[] { subt.v(), signature.v(), supt.v(), method.v() },
                                new jedd.PhysicalDomain[] { T1.v(), H1.v(), T2.v(), V2.v() },
                                jedd.Jedd.v().join(jedd.Jedd.v().read(toResolve),
                                                   this.declaresMethod,
                                                   new jedd.PhysicalDomain[] { T2.v(), H1.v() }));
            toResolve.eqMinus(jedd.Jedd.v().project(resolved, new jedd.PhysicalDomain[] { V2.v() }));
            this.answer.eqUnion(jedd.Jedd.v().project(resolved, new jedd.PhysicalDomain[] { T2.v() }));
            toResolve.eq(jedd.Jedd.v().replace(jedd.Jedd.v().compose(jedd.Jedd.v().read(toResolve),
                                                                     jedd.Jedd.v().replace(this.hier.extend(),
                                                                                           new jedd.PhysicalDomain[] { T1.v(), T2.v() },
                                                                                           new jedd.PhysicalDomain[] { T2.v(), T3.v() }),
                                                                     new jedd.PhysicalDomain[] { T2.v() }),
                                               new jedd.PhysicalDomain[] { T3.v() },
                                               new jedd.PhysicalDomain[] { T2.v() }));
        }while(!jedd.Jedd.v().equals(jedd.Jedd.v().read(toResolve), jedd.Jedd.v().falseBDD())); 
    }
    
    public jedd.Relation answer() {
        return new jedd.Relation(new jedd.Attribute[] { method.v(), signature.v(), type.v() },
                                 new jedd.PhysicalDomain[] { V2.v(), H1.v(), T1.v() },
                                 this.answer);
    }
    
    private final jedd.Relation declaresMethod =
      new jedd.Relation(new jedd.Attribute[] { type.v(), signature.v(), method.v() },
                        new jedd.PhysicalDomain[] { T2.v(), H1.v(), V2.v() },
                        jedd.Jedd.v().falseBDD());
    
    private final jedd.Relation answer =
      new jedd.Relation(new jedd.Attribute[] { type.v(), signature.v(), method.v() },
                        new jedd.PhysicalDomain[] { T1.v(), H1.v(), V2.v() },
                        jedd.Jedd.v().falseBDD());
    
    private BDDHierarchy hier;
    
    private void initialize() {
        for (Iterator clIt = Scene.v().getClasses().iterator(); clIt.hasNext(); ) {
            final SootClass cl = (SootClass) clIt.next();
            for (Iterator mIt = cl.getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                if (m.isAbstract()) continue;
                this.declaresMethod.eqUnion(jedd.Jedd.v().literal(new Object[] { m.getDeclaringClass().getType(), m.getNumberedSubSignature(), m },
                                                                  new jedd.Attribute[] { type.v(), signature.v(), method.v() },
                                                                  new jedd.PhysicalDomain[] { T2.v(), H1.v(), V2.v() }));
            }
        }
    }
}
