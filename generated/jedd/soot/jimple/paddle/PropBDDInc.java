package soot.jimple.paddle;

import soot.*;
import soot.util.queue.*;
import java.util.*;
import soot.options.PaddleOptions;
import soot.jimple.paddle.bdddomains.*;
import soot.jimple.paddle.queue.*;
import jedd.*;

public final class PropBDDInc extends PropBDD {
    public PropBDDInc(Rsrcc_src_dstc_dst simple,
                      Rsrcc_src_fld_dstc_dst load,
                      Rsrcc_src_dstc_dst_fld store,
                      Robjc_obj_varc_var alloc,
                      Qvarc_var_objc_obj propout,
                      AbsPAG pag) {
        super(simple, load, store, alloc, propout, pag);
    }
    
    public final void update() {
        final jedd.internal.RelationContainer ptFromLoad =
          new jedd.internal.RelationContainer(new Attribute[] { varc.v(), var.v(), objc.v(), obj.v() },
                                              new PhysicalDomain[] { C3.v(), V2.v(), C2.v(), H1.v() },
                                              ("<soot.jimple.paddle.bdddomains.varc:soot.jimple.paddle.bdddo" +
                                               "mains.C3, soot.jimple.paddle.bdddomains.var:soot.jimple.padd" +
                                               "le.bdddomains.V2, soot.jimple.paddle.bdddomains.objc:soot.ji" +
                                               "mple.paddle.bdddomains.C2, soot.jimple.paddle.bdddomains.obj" +
                                               ":soot.jimple.paddle.bdddomains.H1> ptFromLoad = jedd.interna" +
                                               "l.Jedd.v().falseBDD(); at /tmp/fixing-paddle/src/soot/jimple" +
                                               "/paddle/PropBDDInc.jedd:40,31-41"),
                                              jedd.internal.Jedd.v().falseBDD());
        while (true) {
            final jedd.internal.RelationContainer veryOldPt =
              new jedd.internal.RelationContainer(new Attribute[] { varc.v(), var.v(), objc.v(), obj.v() },
                                                  new PhysicalDomain[] { C1.v(), V1.v(), C2.v(), H2.v() },
                                                  ("<soot.jimple.paddle.bdddomains.varc:soot.jimple.paddle.bdddo" +
                                                   "mains.C1, soot.jimple.paddle.bdddomains.var:soot.jimple.padd" +
                                                   "le.bdddomains.V1, soot.jimple.paddle.bdddomains.objc:soot.ji" +
                                                   "mple.paddle.bdddomains.C2, soot.jimple.paddle.bdddomains.obj" +
                                                   ":soot.jimple.paddle.bdddomains.H2> veryOldPt = pt; at /tmp/f" +
                                                   "ixing-paddle/src/soot/jimple/paddle/PropBDDInc.jedd:43,35-44"),
                                                  pt);
            final jedd.internal.RelationContainer ptFromAlloc =
              new jedd.internal.RelationContainer(new Attribute[] { varc.v(), var.v(), objc.v(), obj.v() },
                                                  new PhysicalDomain[] { C3.v(), V2.v(), C2.v(), H1.v() },
                                                  ("<soot.jimple.paddle.bdddomains.varc:soot.jimple.paddle.bdddo" +
                                                   "mains.C3, soot.jimple.paddle.bdddomains.var:soot.jimple.padd" +
                                                   "le.bdddomains.V2, soot.jimple.paddle.bdddomains.objc:soot.ji" +
                                                   "mple.paddle.bdddomains.C2, soot.jimple.paddle.bdddomains.obj" +
                                                   ":soot.jimple.paddle.bdddomains.H1> ptFromAlloc = jedd.intern" +
                                                   "al.Jedd.v().intersect(jedd.internal.Jedd.v().read(jedd.inter" +
                                                   "nal.Jedd.v().replace(newAlloc.get(), new jedd.PhysicalDomain" +
                                                   "[...], new jedd.PhysicalDomain[...])), jedd.internal.Jedd.v(" +
                                                   ").replace(typeFilter(), new jedd.PhysicalDomain[...], new je" +
                                                   "dd.PhysicalDomain[...])); at /tmp/fixing-paddle/src/soot/jim" +
                                                   "ple/paddle/PropBDDInc.jedd:46,16-27"),
                                                  jedd.internal.Jedd.v().intersect(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(newAlloc.get(),
                                                                                                                                              new PhysicalDomain[] { C1.v(), V1.v() },
                                                                                                                                              new PhysicalDomain[] { C3.v(), V2.v() })),
                                                                                   jedd.internal.Jedd.v().replace(typeFilter(),
                                                                                                                  new PhysicalDomain[] { V1.v() },
                                                                                                                  new PhysicalDomain[] { V2.v() })));
            final jedd.internal.RelationContainer ptFromSimple1 =
              new jedd.internal.RelationContainer(new Attribute[] { varc.v(), var.v(), objc.v(), obj.v() },
                                                  new PhysicalDomain[] { C3.v(), V2.v(), C2.v(), H1.v() },
                                                  ("<soot.jimple.paddle.bdddomains.varc:soot.jimple.paddle.bdddo" +
                                                   "mains.C3, soot.jimple.paddle.bdddomains.var:soot.jimple.padd" +
                                                   "le.bdddomains.V2, soot.jimple.paddle.bdddomains.objc:soot.ji" +
                                                   "mple.paddle.bdddomains.C2, soot.jimple.paddle.bdddomains.obj" +
                                                   ":soot.jimple.paddle.bdddomains.H1> ptFromSimple1 = jedd.inte" +
                                                   "rnal.Jedd.v().replace(jedd.internal.Jedd.v().intersect(jedd." +
                                                   "internal.Jedd.v().read(propSimple(new jedd.internal.Relation" +
                                                   "Container(...), new jedd.internal.RelationContainer(...))), " +
                                                   "jedd.internal.Jedd.v().replace(typeFilter(), new jedd.Physic" +
                                                   "alDomain[...], new jedd.PhysicalDomain[...])), new jedd.Phys" +
                                                   "icalDomain[...], new jedd.PhysicalDomain[...]); at /tmp/fixi" +
                                                   "ng-paddle/src/soot/jimple/paddle/PropBDDInc.jedd:48,16-29"),
                                                  jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().intersect(jedd.internal.Jedd.v().read(propSimple(new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v(), objc.v(), varc.v() },
                                                                                                                                                                                             new PhysicalDomain[] { V1.v(), H1.v(), C2.v(), C1.v() },
                                                                                                                                                                                             ("propSimple(jedd.internal.Jedd.v().replace(pt, new jedd.Physi" +
                                                                                                                                                                                              "calDomain[...], new jedd.PhysicalDomain[...]), jedd.internal" +
                                                                                                                                                                                              ".Jedd.v().replace(newSimple.get(), new jedd.PhysicalDomain[." +
                                                                                                                                                                                              "..], new jedd.PhysicalDomain[...])) at /tmp/fixing-paddle/sr" +
                                                                                                                                                                                              "c/soot/jimple/paddle/PropBDDInc.jedd:48,32-42"),
                                                                                                                                                                                             jedd.internal.Jedd.v().replace(pt,
                                                                                                                                                                                                                            new PhysicalDomain[] { H2.v() },
                                                                                                                                                                                                                            new PhysicalDomain[] { H1.v() })),
                                                                                                                                                         new jedd.internal.RelationContainer(new Attribute[] { srcc.v(), src.v(), dstc.v(), dst.v() },
                                                                                                                                                                                             new PhysicalDomain[] { C3.v(), V1.v(), C2.v(), V2.v() },
                                                                                                                                                                                             ("propSimple(jedd.internal.Jedd.v().replace(pt, new jedd.Physi" +
                                                                                                                                                                                              "calDomain[...], new jedd.PhysicalDomain[...]), jedd.internal" +
                                                                                                                                                                                              ".Jedd.v().replace(newSimple.get(), new jedd.PhysicalDomain[." +
                                                                                                                                                                                              "..], new jedd.PhysicalDomain[...])) at /tmp/fixing-paddle/sr" +
                                                                                                                                                                                              "c/soot/jimple/paddle/PropBDDInc.jedd:48,32-42"),
                                                                                                                                                                                             jedd.internal.Jedd.v().replace(newSimple.get(),
                                                                                                                                                                                                                            new PhysicalDomain[] { C1.v() },
                                                                                                                                                                                                                            new PhysicalDomain[] { C3.v() })))),
                                                                                                                  jedd.internal.Jedd.v().replace(typeFilter(),
                                                                                                                                                 new PhysicalDomain[] { C3.v(), V1.v() },
                                                                                                                                                 new PhysicalDomain[] { C1.v(), V2.v() })),
                                                                                 new PhysicalDomain[] { C1.v() },
                                                                                 new PhysicalDomain[] { C3.v() }));
            final jedd.internal.RelationContainer ptFromAllocAndSimple1 =
              new jedd.internal.RelationContainer(new Attribute[] { varc.v(), var.v(), objc.v(), obj.v() },
                                                  new PhysicalDomain[] { C3.v(), V2.v(), C2.v(), H1.v() },
                                                  ("<soot.jimple.paddle.bdddomains.varc:soot.jimple.paddle.bdddo" +
                                                   "mains.C3, soot.jimple.paddle.bdddomains.var:soot.jimple.padd" +
                                                   "le.bdddomains.V2, soot.jimple.paddle.bdddomains.objc:soot.ji" +
                                                   "mple.paddle.bdddomains.C2, soot.jimple.paddle.bdddomains.obj" +
                                                   ":soot.jimple.paddle.bdddomains.H1> ptFromAllocAndSimple1 = j" +
                                                   "edd.internal.Jedd.v().union(jedd.internal.Jedd.v().read(ptFr" +
                                                   "omAlloc), ptFromSimple1); at /tmp/fixing-paddle/src/soot/jim" +
                                                   "ple/paddle/PropBDDInc.jedd:50,16-37"),
                                                  jedd.internal.Jedd.v().union(jedd.internal.Jedd.v().read(ptFromAlloc),
                                                                               ptFromSimple1));
            final jedd.internal.RelationContainer ptFromSimple2 =
              new jedd.internal.RelationContainer(new Attribute[] { varc.v(), var.v(), objc.v(), obj.v() },
                                                  new PhysicalDomain[] { C3.v(), V2.v(), C2.v(), H1.v() },
                                                  ("<soot.jimple.paddle.bdddomains.varc:soot.jimple.paddle.bdddo" +
                                                   "mains.C3, soot.jimple.paddle.bdddomains.var:soot.jimple.padd" +
                                                   "le.bdddomains.V2, soot.jimple.paddle.bdddomains.objc:soot.ji" +
                                                   "mple.paddle.bdddomains.C2, soot.jimple.paddle.bdddomains.obj" +
                                                   ":soot.jimple.paddle.bdddomains.H1> ptFromSimple2 = jedd.inte" +
                                                   "rnal.Jedd.v().intersect(jedd.internal.Jedd.v().read(jedd.int" +
                                                   "ernal.Jedd.v().replace(propSimple(new jedd.internal.Relation" +
                                                   "Container(...), new jedd.internal.RelationContainer(...)), n" +
                                                   "ew jedd.PhysicalDomain[...], new jedd.PhysicalDomain[...]))," +
                                                   " jedd.internal.Jedd.v().replace(typeFilter(), new jedd.Physi" +
                                                   "calDomain[...], new jedd.PhysicalDomain[...])); at /tmp/fixi" +
                                                   "ng-paddle/src/soot/jimple/paddle/PropBDDInc.jedd:52,16-29"),
                                                  jedd.internal.Jedd.v().intersect(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(propSimple(new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v(), varc.v(), objc.v() },
                                                                                                                                                                                             new PhysicalDomain[] { V1.v(), H1.v(), C1.v(), C2.v() },
                                                                                                                                                                                             ("propSimple(jedd.internal.Jedd.v().replace(jedd.internal.Jedd" +
                                                                                                                                                                                              ".v().union(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v(" +
                                                                                                                                                                                              ").replace(ptFromAllocAndSimple1, new jedd.PhysicalDomain[..." +
                                                                                                                                                                                              "], new jedd.PhysicalDomain[...])), jedd.internal.Jedd.v().re" +
                                                                                                                                                                                              "place(ptFromLoad, new jedd.PhysicalDomain[...], new jedd.Phy" +
                                                                                                                                                                                              "sicalDomain[...])), new jedd.PhysicalDomain[...], new jedd.P" +
                                                                                                                                                                                              "hysicalDomain[...]), jedd.internal.Jedd.v().replace(pag.allS" +
                                                                                                                                                                                              "imple().get(), new jedd.PhysicalDomain[...], new jedd.Physic" +
                                                                                                                                                                                              "alDomain[...])) at /tmp/fixing-paddle/src/soot/jimple/paddle" +
                                                                                                                                                                                              "/PropBDDInc.jedd:52,32-42"),
                                                                                                                                                                                             jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().union(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(ptFromAllocAndSimple1,
                                                                                                                                                                                                                                                                                                                    new PhysicalDomain[] { V2.v() },
                                                                                                                                                                                                                                                                                                                    new PhysicalDomain[] { V1.v() })),
                                                                                                                                                                                                                                                         jedd.internal.Jedd.v().replace(ptFromLoad,
                                                                                                                                                                                                                                                                                        new PhysicalDomain[] { V2.v() },
                                                                                                                                                                                                                                                                                        new PhysicalDomain[] { V1.v() })),
                                                                                                                                                                                                                            new PhysicalDomain[] { C3.v() },
                                                                                                                                                                                                                            new PhysicalDomain[] { C1.v() })),
                                                                                                                                                         new jedd.internal.RelationContainer(new Attribute[] { srcc.v(), src.v(), dstc.v(), dst.v() },
                                                                                                                                                                                             new PhysicalDomain[] { C3.v(), V1.v(), C2.v(), V2.v() },
                                                                                                                                                                                             ("propSimple(jedd.internal.Jedd.v().replace(jedd.internal.Jedd" +
                                                                                                                                                                                              ".v().union(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v(" +
                                                                                                                                                                                              ").replace(ptFromAllocAndSimple1, new jedd.PhysicalDomain[..." +
                                                                                                                                                                                              "], new jedd.PhysicalDomain[...])), jedd.internal.Jedd.v().re" +
                                                                                                                                                                                              "place(ptFromLoad, new jedd.PhysicalDomain[...], new jedd.Phy" +
                                                                                                                                                                                              "sicalDomain[...])), new jedd.PhysicalDomain[...], new jedd.P" +
                                                                                                                                                                                              "hysicalDomain[...]), jedd.internal.Jedd.v().replace(pag.allS" +
                                                                                                                                                                                              "imple().get(), new jedd.PhysicalDomain[...], new jedd.Physic" +
                                                                                                                                                                                              "alDomain[...])) at /tmp/fixing-paddle/src/soot/jimple/paddle" +
                                                                                                                                                                                              "/PropBDDInc.jedd:52,32-42"),
                                                                                                                                                                                             jedd.internal.Jedd.v().replace(pag.allSimple().get(),
                                                                                                                                                                                                                            new PhysicalDomain[] { C1.v() },
                                                                                                                                                                                                                            new PhysicalDomain[] { C3.v() }))),
                                                                                                                                              new PhysicalDomain[] { C1.v() },
                                                                                                                                              new PhysicalDomain[] { C3.v() })),
                                                                                   jedd.internal.Jedd.v().replace(typeFilter(),
                                                                                                                  new PhysicalDomain[] { V1.v() },
                                                                                                                  new PhysicalDomain[] { V2.v() })));
            final jedd.internal.RelationContainer ptFromAllocAndSimple =
              new jedd.internal.RelationContainer(new Attribute[] { varc.v(), var.v(), objc.v(), obj.v() },
                                                  new PhysicalDomain[] { C3.v(), V2.v(), C2.v(), H2.v() },
                                                  ("<soot.jimple.paddle.bdddomains.varc:soot.jimple.paddle.bdddo" +
                                                   "mains.C3, soot.jimple.paddle.bdddomains.var:soot.jimple.padd" +
                                                   "le.bdddomains.V2, soot.jimple.paddle.bdddomains.objc:soot.ji" +
                                                   "mple.paddle.bdddomains.C2, soot.jimple.paddle.bdddomains.obj" +
                                                   ":soot.jimple.paddle.bdddomains.H2> ptFromAllocAndSimple = je" +
                                                   "dd.internal.Jedd.v().union(jedd.internal.Jedd.v().read(jedd." +
                                                   "internal.Jedd.v().replace(ptFromAllocAndSimple1, new jedd.Ph" +
                                                   "ysicalDomain[...], new jedd.PhysicalDomain[...])), jedd.inte" +
                                                   "rnal.Jedd.v().replace(ptFromSimple2, new jedd.PhysicalDomain" +
                                                   "[...], new jedd.PhysicalDomain[...])); at /tmp/fixing-paddle" +
                                                   "/src/soot/jimple/paddle/PropBDDInc.jedd:55,16-36"),
                                                  jedd.internal.Jedd.v().union(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(ptFromAllocAndSimple1,
                                                                                                                                          new PhysicalDomain[] { H1.v() },
                                                                                                                                          new PhysicalDomain[] { H2.v() })),
                                                                               jedd.internal.Jedd.v().replace(ptFromSimple2,
                                                                                                              new PhysicalDomain[] { H1.v() },
                                                                                                              new PhysicalDomain[] { H2.v() })));
            pt.eqUnion(jedd.internal.Jedd.v().replace(ptFromAllocAndSimple,
                                                      new PhysicalDomain[] { V2.v(), C3.v() },
                                                      new PhysicalDomain[] { V1.v(), C1.v() }));
            outputPt(new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v(), objc.v(), varc.v() },
                                                         new PhysicalDomain[] { V2.v(), H1.v(), C2.v(), C1.v() },
                                                         ("outputPt(jedd.internal.Jedd.v().replace(ptFromAllocAndSimple" +
                                                          ", new jedd.PhysicalDomain[...], new jedd.PhysicalDomain[...]" +
                                                          ")) at /tmp/fixing-paddle/src/soot/jimple/paddle/PropBDDInc.j" +
                                                          "edd:57,12-20"),
                                                         jedd.internal.Jedd.v().replace(ptFromAllocAndSimple,
                                                                                        new PhysicalDomain[] { H2.v(), C3.v() },
                                                                                        new PhysicalDomain[] { H1.v(), C1.v() })));
            fieldPt.eqUnion(propStore(new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v(), objc.v(), varc.v() },
                                                                          new PhysicalDomain[] { V1.v(), H2.v(), C2.v(), C1.v() },
                                                                          ("propStore(pt, pag.allStore().get(), jedd.internal.Jedd.v().r" +
                                                                           "eplace(pt, new jedd.PhysicalDomain[...], new jedd.PhysicalDo" +
                                                                           "main[...])) at /tmp/fixing-paddle/src/soot/jimple/paddle/Pro" +
                                                                           "pBDDInc.jedd:59,23-32"),
                                                                          pt),
                                      new jedd.internal.RelationContainer(new Attribute[] { srcc.v(), src.v(), dstc.v(), dst.v(), fld.v() },
                                                                          new PhysicalDomain[] { C1.v(), V1.v(), C2.v(), V2.v(), FD.v() },
                                                                          ("propStore(pt, pag.allStore().get(), jedd.internal.Jedd.v().r" +
                                                                           "eplace(pt, new jedd.PhysicalDomain[...], new jedd.PhysicalDo" +
                                                                           "main[...])) at /tmp/fixing-paddle/src/soot/jimple/paddle/Pro" +
                                                                           "pBDDInc.jedd:59,23-32"),
                                                                          pag.allStore().get()),
                                      new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v(), objc.v(), varc.v() },
                                                                          new PhysicalDomain[] { V1.v(), H1.v(), C2.v(), C1.v() },
                                                                          ("propStore(pt, pag.allStore().get(), jedd.internal.Jedd.v().r" +
                                                                           "eplace(pt, new jedd.PhysicalDomain[...], new jedd.PhysicalDo" +
                                                                           "main[...])) at /tmp/fixing-paddle/src/soot/jimple/paddle/Pro" +
                                                                           "pBDDInc.jedd:59,23-32"),
                                                                          jedd.internal.Jedd.v().replace(pt,
                                                                                                         new PhysicalDomain[] { H2.v() },
                                                                                                         new PhysicalDomain[] { H1.v() }))));
            ptFromLoad.eq(jedd.internal.Jedd.v().intersect(jedd.internal.Jedd.v().read(jedd.internal.Jedd.v().replace(propLoad(new jedd.internal.RelationContainer(new Attribute[] { fld.v(), base.v(), obj.v(), objc.v(), basec.v() },
                                                                                                                                                                   new PhysicalDomain[] { FD.v(), H1.v(), H2.v(), C2.v(), C1.v() },
                                                                                                                                                                   ("propLoad(fieldPt, jedd.internal.Jedd.v().replace(pag.allLoad" +
                                                                                                                                                                    "().get(), new jedd.PhysicalDomain[...], new jedd.PhysicalDom" +
                                                                                                                                                                    "ain[...]), pt) at /tmp/fixing-paddle/src/soot/jimple/paddle/" +
                                                                                                                                                                    "PropBDDInc.jedd:60,25-33"),
                                                                                                                                                                   fieldPt),
                                                                                                                               new jedd.internal.RelationContainer(new Attribute[] { srcc.v(), src.v(), fld.v(), dstc.v(), dst.v() },
                                                                                                                                                                   new PhysicalDomain[] { C1.v(), V1.v(), FD.v(), C3.v(), V2.v() },
                                                                                                                                                                   ("propLoad(fieldPt, jedd.internal.Jedd.v().replace(pag.allLoad" +
                                                                                                                                                                    "().get(), new jedd.PhysicalDomain[...], new jedd.PhysicalDom" +
                                                                                                                                                                    "ain[...]), pt) at /tmp/fixing-paddle/src/soot/jimple/paddle/" +
                                                                                                                                                                    "PropBDDInc.jedd:60,25-33"),
                                                                                                                                                                   jedd.internal.Jedd.v().replace(pag.allLoad().get(),
                                                                                                                                                                                                  new PhysicalDomain[] { C2.v() },
                                                                                                                                                                                                  new PhysicalDomain[] { C3.v() })),
                                                                                                                               new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v(), objc.v(), varc.v() },
                                                                                                                                                                   new PhysicalDomain[] { V1.v(), H2.v(), C2.v(), C1.v() },
                                                                                                                                                                   ("propLoad(fieldPt, jedd.internal.Jedd.v().replace(pag.allLoad" +
                                                                                                                                                                    "().get(), new jedd.PhysicalDomain[...], new jedd.PhysicalDom" +
                                                                                                                                                                    "ain[...]), pt) at /tmp/fixing-paddle/src/soot/jimple/paddle/" +
                                                                                                                                                                    "PropBDDInc.jedd:60,25-33"),
                                                                                                                                                                   pt)),
                                                                                                                      new PhysicalDomain[] { H2.v() },
                                                                                                                      new PhysicalDomain[] { H1.v() })),
                                                           jedd.internal.Jedd.v().replace(typeFilter(),
                                                                                          new PhysicalDomain[] { V1.v() },
                                                                                          new PhysicalDomain[] { V2.v() })));
            pt.eqUnion(jedd.internal.Jedd.v().replace(ptFromLoad,
                                                      new PhysicalDomain[] { V2.v(), H1.v(), C3.v() },
                                                      new PhysicalDomain[] { V1.v(), H2.v(), C1.v() }));
            outputPt(new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v(), objc.v(), varc.v() },
                                                         new PhysicalDomain[] { V2.v(), H1.v(), C2.v(), C1.v() },
                                                         ("outputPt(jedd.internal.Jedd.v().replace(ptFromLoad, new jedd" +
                                                          ".PhysicalDomain[...], new jedd.PhysicalDomain[...])) at /tmp" +
                                                          "/fixing-paddle/src/soot/jimple/paddle/PropBDDInc.jedd:62,12-" +
                                                          "20"),
                                                         jedd.internal.Jedd.v().replace(ptFromLoad,
                                                                                        new PhysicalDomain[] { C3.v() },
                                                                                        new PhysicalDomain[] { C1.v() })));
            if (PaddleScene.v().options().verbose()) { G.v().out.println("Major iteration: "); }
            if (jedd.internal.Jedd.v().equals(jedd.internal.Jedd.v().read(pt), veryOldPt)) break;
        }
    }
}
