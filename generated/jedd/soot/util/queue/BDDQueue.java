package soot.util.queue;

public class BDDQueue {
    BDDChunk chunk = new BDDChunk();
    
    public void add(final jedd.Relation r) { this.chunk.bdd.eqUnion(r); }
    
    public BDDReader reader() { return new BDDReader(this); }
    
    BDDChunk newChunk() {
        this.chunk = (this.chunk.next = new BDDChunk());
        return this.chunk;
    }
    
    public BDDQueue() { super(); }
}
