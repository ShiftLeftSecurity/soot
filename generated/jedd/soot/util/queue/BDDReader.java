package soot.util.queue;

public class BDDReader implements Cloneable {
    BDDChunk chunk;
    
    BDDQueue q;
    
    BDDReader(BDDQueue q) {
        super();
        this.q = q;
        this.chunk = q.newChunk();
    }
    
    public jedd.Relation next() {
        final jedd.Relation ret = new jedd.Relation(new jedd.Attribute[] {  }, new jedd.PhysicalDomain[] {  });
        do  {
            ret.eq(this.chunk.bdd);
            this.chunk = this.chunk.next;
            if (this.chunk == null) this.chunk = this.q.newChunk();
        }while(jedd.Jedd.v().equals(jedd.Jedd.v().read(ret), jedd.Jedd.v().falseBDD()) && this.chunk.next != null); 
        return new jedd.Relation(new jedd.Attribute[] {  }, new jedd.PhysicalDomain[] {  }, ret);
    }
    
    public boolean hasNext() {
        while (jedd.Jedd.v().equals(jedd.Jedd.v().read(this.chunk.bdd), jedd.Jedd.v().falseBDD()) &&
                 this.chunk.next != null)
            this.chunk = this.chunk.next;
        return !jedd.Jedd.v().equals(jedd.Jedd.v().read(this.chunk.bdd), jedd.Jedd.v().falseBDD());
    }
    
    private BDDReader(BDDQueue q, BDDChunk chunk) {
        super();
        this.q = q;
        this.chunk = chunk;
    }
    
    public Object clone() { return new BDDReader(this.q, this.chunk); }
}
