package soot.jimple.spark;

import soot.*;
import soot.jimple.spark.queue.*;
import soot.jimple.spark.bdddomains.*;
import java.util.*;
import jedd.*;

public class BDDPAG extends AbsPAG {
    BDDPAG(Rsrc_dst simple,
           Rsrc_fld_dst load,
           Rsrc_fld_dst store,
           Robj_var alloc,
           Qsrc_dst simpleout,
           Qsrc_fld_dst loadout,
           Qsrc_fld_dst storeout,
           Qobj_var allocout) {
        super(simple, load, store, alloc, simpleout, loadout, storeout, allocout);
    }
    
    public void update() {
        final jedd.internal.RelationContainer newSimple =
          new jedd.internal.RelationContainer(new Attribute[] { src.v(), dst.v() },
                                              new PhysicalDomain[] { V1.v(), V2.v() },
                                              ("<soot.jimple.spark.bdddomains.src:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.dst:soot.jimple.spark.bd" +
                                               "ddomains.V2> newSimple = jedd.internal.Jedd.v().minus(jedd.i" +
                                               "nternal.Jedd.v().read(simple.get()), simpleBDD); at /home/ol" +
                                               "hotak/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:44,19-28"),
                                              jedd.internal.Jedd.v().minus(jedd.internal.Jedd.v().read(simple.get()),
                                                                           simpleBDD));
        simpleout.add(new jedd.internal.RelationContainer(new Attribute[] { dst.v(), src.v() },
                                                          new PhysicalDomain[] { V2.v(), V1.v() },
                                                          ("simpleout.add(newSimple) at /home/olhotak/soot-2-jedd/src/so" +
                                                           "ot/jimple/spark/BDDPAG.jedd:45,8-17"),
                                                          newSimple));
        simpleBDD.eqUnion(newSimple);
        final jedd.internal.RelationContainer newAlloc =
          new jedd.internal.RelationContainer(new Attribute[] { obj.v(), var.v() },
                                              new PhysicalDomain[] { H1.v(), V1.v() },
                                              ("<soot.jimple.spark.bdddomains.obj:soot.jimple.spark.bdddomai" +
                                               "ns.H1, soot.jimple.spark.bdddomains.var:soot.jimple.spark.bd" +
                                               "ddomains.V1> newAlloc = jedd.internal.Jedd.v().minus(jedd.in" +
                                               "ternal.Jedd.v().read(alloc.get()), allocBDD); at /home/olhot" +
                                               "ak/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:48,19-27"),
                                              jedd.internal.Jedd.v().minus(jedd.internal.Jedd.v().read(alloc.get()),
                                                                           allocBDD));
        allocout.add(new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v() },
                                                         new PhysicalDomain[] { V1.v(), H1.v() },
                                                         ("allocout.add(newAlloc) at /home/olhotak/soot-2-jedd/src/soot" +
                                                          "/jimple/spark/BDDPAG.jedd:49,8-16"),
                                                         newAlloc));
        allocBDD.eqUnion(newAlloc);
        final jedd.internal.RelationContainer newLoad =
          new jedd.internal.RelationContainer(new Attribute[] { src.v(), fld.v(), dst.v() },
                                              new PhysicalDomain[] { V1.v(), FD.v(), V2.v() },
                                              ("<soot.jimple.spark.bdddomains.src:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.fld:soot.jimple.spark.bd" +
                                               "ddomains.FD, soot.jimple.spark.bdddomains.dst:soot.jimple.sp" +
                                               "ark.bdddomains.V2> newLoad = jedd.internal.Jedd.v().minus(je" +
                                               "dd.internal.Jedd.v().read(load.get()), loadBDD); at /home/ol" +
                                               "hotak/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:52,24-31"),
                                              jedd.internal.Jedd.v().minus(jedd.internal.Jedd.v().read(load.get()),
                                                                           loadBDD));
        loadout.add(new jedd.internal.RelationContainer(new Attribute[] { fld.v(), dst.v(), src.v() },
                                                        new PhysicalDomain[] { FD.v(), V2.v(), V1.v() },
                                                        ("loadout.add(newLoad) at /home/olhotak/soot-2-jedd/src/soot/j" +
                                                         "imple/spark/BDDPAG.jedd:53,8-15"),
                                                        newLoad));
        loadBDD.eqUnion(newLoad);
        final jedd.internal.RelationContainer newStore =
          new jedd.internal.RelationContainer(new Attribute[] { src.v(), fld.v(), dst.v() },
                                              new PhysicalDomain[] { V1.v(), FD.v(), V2.v() },
                                              ("<soot.jimple.spark.bdddomains.src:soot.jimple.spark.bdddomai" +
                                               "ns.V1, soot.jimple.spark.bdddomains.fld:soot.jimple.spark.bd" +
                                               "ddomains.FD, soot.jimple.spark.bdddomains.dst:soot.jimple.sp" +
                                               "ark.bdddomains.V2> newStore = jedd.internal.Jedd.v().minus(j" +
                                               "edd.internal.Jedd.v().read(store.get()), storeBDD); at /home" +
                                               "/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:56,24" +
                                               "-32"),
                                              jedd.internal.Jedd.v().minus(jedd.internal.Jedd.v().read(store.get()),
                                                                           storeBDD));
        storeout.add(new jedd.internal.RelationContainer(new Attribute[] { fld.v(), dst.v(), src.v() },
                                                         new PhysicalDomain[] { FD.v(), V2.v(), V1.v() },
                                                         ("storeout.add(newStore) at /home/olhotak/soot-2-jedd/src/soot" +
                                                          "/jimple/spark/BDDPAG.jedd:57,8-16"),
                                                         newStore));
        storeBDD.eqUnion(newStore);
    }
    
    public Iterator simpleSources() {
        return new jedd.internal.RelationContainer(new Attribute[] { src.v() },
                                                   new PhysicalDomain[] { V1.v() },
                                                   ("jedd.internal.Jedd.v().project(simpleBDD, new jedd.PhysicalD" +
                                                    "omain[...]).iterator() at /home/olhotak/soot-2-jedd/src/soot" +
                                                    "/jimple/spark/BDDPAG.jedd:62,35-43"),
                                                   jedd.internal.Jedd.v().project(simpleBDD,
                                                                                  new PhysicalDomain[] { V2.v() })).iterator();
    }
    
    public Iterator loadSources() {
        return new FieldRefIterator(new jedd.internal.RelationContainer(new Attribute[] { fld.v(), var.v() },
                                                                        new PhysicalDomain[] { FD.v(), V1.v() },
                                                                        ("new soot.jimple.spark.BDDPAG.FieldRefIterator(...) at /home/" +
                                                                         "olhotak/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:65,15-" +
                                                                         "18"),
                                                                        jedd.internal.Jedd.v().project(loadBDD,
                                                                                                       new PhysicalDomain[] { V2.v() })));
    }
    
    public Iterator storeSources() {
        return new jedd.internal.RelationContainer(new Attribute[] { src.v() },
                                                   new PhysicalDomain[] { V1.v() },
                                                   ("jedd.internal.Jedd.v().project(storeBDD, new jedd.PhysicalDo" +
                                                    "main[...]).iterator() at /home/olhotak/soot-2-jedd/src/soot/" +
                                                    "jimple/spark/BDDPAG.jedd:68,42-50"),
                                                   jedd.internal.Jedd.v().project(storeBDD,
                                                                                  new PhysicalDomain[] { FD.v(), V2.v() })).iterator();
    }
    
    public Iterator allocSources() {
        return new jedd.internal.RelationContainer(new Attribute[] { obj.v() },
                                                   new PhysicalDomain[] { H1.v() },
                                                   ("jedd.internal.Jedd.v().project(allocBDD, new jedd.PhysicalDo" +
                                                    "main[...]).iterator() at /home/olhotak/soot-2-jedd/src/soot/" +
                                                    "jimple/spark/BDDPAG.jedd:71,34-42"),
                                                   jedd.internal.Jedd.v().project(allocBDD,
                                                                                  new PhysicalDomain[] { V1.v() })).iterator();
    }
    
    public Iterator simpleInvSources() {
        return new jedd.internal.RelationContainer(new Attribute[] { dst.v() },
                                                   new PhysicalDomain[] { V2.v() },
                                                   ("jedd.internal.Jedd.v().project(simpleBDD, new jedd.PhysicalD" +
                                                    "omain[...]).iterator() at /home/olhotak/soot-2-jedd/src/soot" +
                                                    "/jimple/spark/BDDPAG.jedd:74,35-43"),
                                                   jedd.internal.Jedd.v().project(simpleBDD,
                                                                                  new PhysicalDomain[] { V1.v() })).iterator();
    }
    
    public Iterator loadInvSources() {
        return new jedd.internal.RelationContainer(new Attribute[] { dst.v() },
                                                   new PhysicalDomain[] { V2.v() },
                                                   ("jedd.internal.Jedd.v().project(loadBDD, new jedd.PhysicalDom" +
                                                    "ain[...]).iterator() at /home/olhotak/soot-2-jedd/src/soot/j" +
                                                    "imple/spark/BDDPAG.jedd:77,40-48"),
                                                   jedd.internal.Jedd.v().project(loadBDD,
                                                                                  new PhysicalDomain[] { FD.v(), V1.v() })).iterator();
    }
    
    public Iterator storeInvSources() {
        return new FieldRefIterator(new jedd.internal.RelationContainer(new Attribute[] { fld.v(), var.v() },
                                                                        new PhysicalDomain[] { FD.v(), V1.v() },
                                                                        ("new soot.jimple.spark.BDDPAG.FieldRefIterator(...) at /home/" +
                                                                         "olhotak/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:80,15-" +
                                                                         "18"),
                                                                        jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().project(storeBDD,
                                                                                                                                      new PhysicalDomain[] { V1.v() }),
                                                                                                       new PhysicalDomain[] { V2.v() },
                                                                                                       new PhysicalDomain[] { V1.v() })));
    }
    
    public Iterator allocInvSources() {
        return new jedd.internal.RelationContainer(new Attribute[] { var.v() },
                                                   new PhysicalDomain[] { V1.v() },
                                                   ("jedd.internal.Jedd.v().project(allocBDD, new jedd.PhysicalDo" +
                                                    "main[...]).iterator() at /home/olhotak/soot-2-jedd/src/soot/" +
                                                    "jimple/spark/BDDPAG.jedd:83,34-42"),
                                                   jedd.internal.Jedd.v().project(allocBDD,
                                                                                  new PhysicalDomain[] { H1.v() })).iterator();
    }
    
    public Iterator simpleLookup(VarNode key) {
        return new jedd.internal.RelationContainer(new Attribute[] { dst.v() },
                                                   new PhysicalDomain[] { V2.v() },
                                                   ("jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(s" +
                                                    "impleBDD), jedd.internal.Jedd.v().literal(new java.lang.Obje" +
                                                    "ct[...], new jedd.Attribute[...], new jedd.PhysicalDomain[.." +
                                                    ".]), new jedd.PhysicalDomain[...]).iterator() at /home/olhot" +
                                                    "ak/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:87,55-63"),
                                                   jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(simpleBDD),
                                                                                  jedd.internal.Jedd.v().literal(new Object[] { key },
                                                                                                                 new Attribute[] { src.v() },
                                                                                                                 new PhysicalDomain[] { V1.v() }),
                                                                                  new PhysicalDomain[] { V1.v() })).iterator();
    }
    
    public Iterator loadLookup(FieldRefNode key) {
        return new jedd.internal.RelationContainer(new Attribute[] { dst.v() },
                                                   new PhysicalDomain[] { V2.v() },
                                                   ("jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(l" +
                                                    "oadBDD), jedd.internal.Jedd.v().literal(new java.lang.Object" +
                                                    "[...], new jedd.Attribute[...], new jedd.PhysicalDomain[...]" +
                                                    "), new jedd.PhysicalDomain[...]).iterator() at /home/olhotak" +
                                                    "/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:91,69-77"),
                                                   jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(loadBDD),
                                                                                  jedd.internal.Jedd.v().literal(new Object[] { key.getBase(), key.getField() },
                                                                                                                 new Attribute[] { src.v(), fld.v() },
                                                                                                                 new PhysicalDomain[] { V1.v(), FD.v() }),
                                                                                  new PhysicalDomain[] { V1.v(), FD.v() })).iterator();
    }
    
    public Iterator storeLookup(VarNode key) {
        return new FieldRefIterator(new jedd.internal.RelationContainer(new Attribute[] { fld.v(), var.v() },
                                                                        new PhysicalDomain[] { FD.v(), V1.v() },
                                                                        ("new soot.jimple.spark.BDDPAG.FieldRefIterator(...) at /home/" +
                                                                         "olhotak/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:94,15-" +
                                                                         "18"),
                                                                        jedd.internal.Jedd.v().replace(jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(storeBDD),
                                                                                                                                      jedd.internal.Jedd.v().literal(new Object[] { key },
                                                                                                                                                                     new Attribute[] { src.v() },
                                                                                                                                                                     new PhysicalDomain[] { V1.v() }),
                                                                                                                                      new PhysicalDomain[] { V1.v() }),
                                                                                                       new PhysicalDomain[] { V2.v() },
                                                                                                       new PhysicalDomain[] { V1.v() })));
    }
    
    public Iterator allocLookup(AllocNode key) {
        return new jedd.internal.RelationContainer(new Attribute[] { var.v() },
                                                   new PhysicalDomain[] { V1.v() },
                                                   ("jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(a" +
                                                    "llocBDD), jedd.internal.Jedd.v().literal(new java.lang.Objec" +
                                                    "t[...], new jedd.Attribute[...], new jedd.PhysicalDomain[..." +
                                                    "]), new jedd.PhysicalDomain[...]).iterator() at /home/olhota" +
                                                    "k/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:98,54-62"),
                                                   jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(allocBDD),
                                                                                  jedd.internal.Jedd.v().literal(new Object[] { key },
                                                                                                                 new Attribute[] { obj.v() },
                                                                                                                 new PhysicalDomain[] { H1.v() }),
                                                                                  new PhysicalDomain[] { H1.v() })).iterator();
    }
    
    public Iterator simpleInvLookup(VarNode key) {
        return new jedd.internal.RelationContainer(new Attribute[] { src.v() },
                                                   new PhysicalDomain[] { V1.v() },
                                                   ("jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(s" +
                                                    "impleBDD), jedd.internal.Jedd.v().literal(new java.lang.Obje" +
                                                    "ct[...], new jedd.Attribute[...], new jedd.PhysicalDomain[.." +
                                                    ".]), new jedd.PhysicalDomain[...]).iterator() at /home/olhot" +
                                                    "ak/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:101,55-63"),
                                                   jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(simpleBDD),
                                                                                  jedd.internal.Jedd.v().literal(new Object[] { key },
                                                                                                                 new Attribute[] { dst.v() },
                                                                                                                 new PhysicalDomain[] { V2.v() }),
                                                                                  new PhysicalDomain[] { V2.v() })).iterator();
    }
    
    public Iterator loadInvLookup(VarNode key) {
        return new FieldRefIterator(new jedd.internal.RelationContainer(new Attribute[] { fld.v(), var.v() },
                                                                        new PhysicalDomain[] { FD.v(), V1.v() },
                                                                        ("new soot.jimple.spark.BDDPAG.FieldRefIterator(...) at /home/" +
                                                                         "olhotak/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:104,15" +
                                                                         "-18"),
                                                                        jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(loadBDD),
                                                                                                       jedd.internal.Jedd.v().literal(new Object[] { key },
                                                                                                                                      new Attribute[] { dst.v() },
                                                                                                                                      new PhysicalDomain[] { V2.v() }),
                                                                                                       new PhysicalDomain[] { V2.v() })));
    }
    
    public Iterator storeInvLookup(FieldRefNode key) {
        return new jedd.internal.RelationContainer(new Attribute[] { src.v() },
                                                   new PhysicalDomain[] { V1.v() },
                                                   ("jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(s" +
                                                    "toreBDD), jedd.internal.Jedd.v().literal(new java.lang.Objec" +
                                                    "t[...], new jedd.Attribute[...], new jedd.PhysicalDomain[..." +
                                                    "]), new jedd.PhysicalDomain[...]).iterator() at /home/olhota" +
                                                    "k/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:109,69-77"),
                                                   jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(storeBDD),
                                                                                  jedd.internal.Jedd.v().literal(new Object[] { key.getBase(), key.getField() },
                                                                                                                 new Attribute[] { dst.v(), fld.v() },
                                                                                                                 new PhysicalDomain[] { V2.v(), FD.v() }),
                                                                                  new PhysicalDomain[] { V2.v(), FD.v() })).iterator();
    }
    
    public Iterator allocInvLookup(VarNode key) {
        return new jedd.internal.RelationContainer(new Attribute[] { obj.v() },
                                                   new PhysicalDomain[] { H1.v() },
                                                   ("jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(a" +
                                                    "llocBDD), jedd.internal.Jedd.v().literal(new java.lang.Objec" +
                                                    "t[...], new jedd.Attribute[...], new jedd.PhysicalDomain[..." +
                                                    "]), new jedd.PhysicalDomain[...]).iterator() at /home/olhota" +
                                                    "k/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:112,54-62"),
                                                   jedd.internal.Jedd.v().compose(jedd.internal.Jedd.v().read(allocBDD),
                                                                                  jedd.internal.Jedd.v().literal(new Object[] { key },
                                                                                                                 new Attribute[] { var.v() },
                                                                                                                 new PhysicalDomain[] { V1.v() }),
                                                                                  new PhysicalDomain[] { V1.v() })).iterator();
    }
    
    public Rsrc_dst allSimple() {
        return new Rsrc_dstBDD(new jedd.internal.RelationContainer(new Attribute[] { dst.v(), src.v() },
                                                                   new PhysicalDomain[] { V2.v(), V1.v() },
                                                                   ("new soot.jimple.spark.queue.Rsrc_dstBDD(...) at /home/olhota" +
                                                                    "k/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:115,41-44"),
                                                                   simpleBDD));
    }
    
    public Rsrc_fld_dst allLoad() {
        return new Rsrc_fld_dstBDD(new jedd.internal.RelationContainer(new Attribute[] { fld.v(), dst.v(), src.v() },
                                                                       new PhysicalDomain[] { FD.v(), V2.v(), V1.v() },
                                                                       ("new soot.jimple.spark.queue.Rsrc_fld_dstBDD(...) at /home/ol" +
                                                                        "hotak/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:116,43-4" +
                                                                        "6"),
                                                                       loadBDD));
    }
    
    public Rsrc_fld_dst allStore() {
        return new Rsrc_fld_dstBDD(new jedd.internal.RelationContainer(new Attribute[] { fld.v(), dst.v(), src.v() },
                                                                       new PhysicalDomain[] { FD.v(), V2.v(), V1.v() },
                                                                       ("new soot.jimple.spark.queue.Rsrc_fld_dstBDD(...) at /home/ol" +
                                                                        "hotak/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:117,44-4" +
                                                                        "7"),
                                                                       storeBDD));
    }
    
    public Robj_var allAlloc() {
        return new Robj_varBDD(new jedd.internal.RelationContainer(new Attribute[] { var.v(), obj.v() },
                                                                   new PhysicalDomain[] { V1.v(), H1.v() },
                                                                   ("new soot.jimple.spark.queue.Robj_varBDD(...) at /home/olhota" +
                                                                    "k/soot-2-jedd/src/soot/jimple/spark/BDDPAG.jedd:118,40-43"),
                                                                   allocBDD));
    }
    
    private static class FieldRefIterator implements Iterator {
        FieldRefIterator(final jedd.internal.RelationContainer bdd) {
            super();
            this.it =
              new jedd.internal.RelationContainer(new Attribute[] { var.v(), fld.v() },
                                                  new PhysicalDomain[] { V1.v(), FD.v() },
                                                  ("bdd.iterator(new jedd.Attribute[...]) at /home/olhotak/soot-" +
                                                   "2-jedd/src/soot/jimple/spark/BDDPAG.jedd:122,22-25"),
                                                  bdd).iterator(new Attribute[] { var.v(), fld.v() });
        }
        
        private Iterator it;
        
        public boolean hasNext() { return it.hasNext(); }
        
        public Object next() {
            Object[] ret = (Object[]) it.next();
            return ((VarNode) ret[0]).dot((SparkField) ret[1]);
        }
        
        public void remove() { throw new UnsupportedOperationException(); }
    }
    
    
    private final jedd.internal.RelationContainer simpleBDD =
      new jedd.internal.RelationContainer(new Attribute[] { src.v(), dst.v() },
                                          new PhysicalDomain[] { V1.v(), V2.v() },
                                          ("private <soot.jimple.spark.bdddomains.src, soot.jimple.spark" +
                                           ".bdddomains.dst> simpleBDD = jedd.internal.Jedd.v().falseBDD" +
                                           "() at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDPAG" +
                                           ".jedd:133,12-22"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final jedd.internal.RelationContainer loadBDD =
      new jedd.internal.RelationContainer(new Attribute[] { src.v(), fld.v(), dst.v() },
                                          new PhysicalDomain[] { V1.v(), FD.v(), V2.v() },
                                          ("private <soot.jimple.spark.bdddomains.src, soot.jimple.spark" +
                                           ".bdddomains.fld, soot.jimple.spark.bdddomains.dst> loadBDD =" +
                                           " jedd.internal.Jedd.v().falseBDD() at /home/olhotak/soot-2-j" +
                                           "edd/src/soot/jimple/spark/BDDPAG.jedd:134,12-27"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final jedd.internal.RelationContainer storeBDD =
      new jedd.internal.RelationContainer(new Attribute[] { src.v(), fld.v(), dst.v() },
                                          new PhysicalDomain[] { V1.v(), FD.v(), V2.v() },
                                          ("private <soot.jimple.spark.bdddomains.src, soot.jimple.spark" +
                                           ".bdddomains.fld, soot.jimple.spark.bdddomains.dst> storeBDD " +
                                           "= jedd.internal.Jedd.v().falseBDD() at /home/olhotak/soot-2-" +
                                           "jedd/src/soot/jimple/spark/BDDPAG.jedd:135,12-27"),
                                          jedd.internal.Jedd.v().falseBDD());
    
    private final jedd.internal.RelationContainer allocBDD =
      new jedd.internal.RelationContainer(new Attribute[] { obj.v(), var.v() },
                                          new PhysicalDomain[] { H1.v(), V1.v() },
                                          ("private <soot.jimple.spark.bdddomains.obj, soot.jimple.spark" +
                                           ".bdddomains.var> allocBDD = jedd.internal.Jedd.v().falseBDD(" +
                                           ") at /home/olhotak/soot-2-jedd/src/soot/jimple/spark/BDDPAG." +
                                           "jedd:136,12-22"),
                                          jedd.internal.Jedd.v().falseBDD());
}
