package soot.util.queue;

class BDDChunk {
    final jedd.Relation bdd =
      new jedd.Relation(new jedd.Attribute[] {  }, new jedd.PhysicalDomain[] {  }, jedd.Jedd.v().falseBDD());
    
    BDDChunk next;
    
    public BDDChunk() { super(); }
}
