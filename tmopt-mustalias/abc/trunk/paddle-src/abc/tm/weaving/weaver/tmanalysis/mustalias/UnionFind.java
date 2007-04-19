package abc.tm.weaving.weaver.tmanalysis.mustalias;
import java.util.*;

// Implementation of union-find. See Cormen, Leiserson, Rivest, 
// _Introduction to Algorithms_, pp 448-449 in 1st edition
public class UnionFind
{
    private Map objectNumberMap;
    private int[] preds;
    private int[] rank;

    public UnionFind(Map objectNumberMap) {
        this.objectNumberMap = objectNumberMap;
        clear();
    }

    public void clear() {
        preds = new int[objectNumberMap.size()];
        for( int i = 0; i < preds.length; i++ ) preds[i] = i;
        rank = new int[objectNumberMap.size()];
    }

    /** Opposite of clear: mutates this object so that everything is in the same class. */
    public void mergeAll() {
        if (objectNumberMap.keySet().size() == 0)
            return;

        Object first = objectNumberMap.keySet().iterator().next();

        for( Iterator oIt = objectNumberMap.keySet().iterator(); oIt.hasNext(); ) 
            merge (first, oIt.next());
    }

    public int lookup( Object o ) {
        Integer i = (Integer) objectNumberMap.get(o);
        if( i == null ) throw new RuntimeException( "Object "+o+" not in objectNumberMap = "+objectNumberMap );
        int x = i.intValue();
        return lookup( x );
    }

    private int lookup( int x ) {
        if( x != preds[x] ) preds[x] = lookup(preds[x]);
        return preds[x];
    }

    public void merge( Object o1, Object o2 ) {
        link( lookup(o1), lookup(o2) );
    }

    private void link( int x, int y ) {
        if( rank[x] > rank[y] ) preds[y] = x;
        else {
            preds[x] = y;
            if( rank[x] == rank[y] ) rank[y]++;
        }
    }

    public List findSet( Object o ) {
        List ret = new ArrayList();
        int i = lookup( o );
        for( Iterator otherIt = objectNumberMap.keySet().iterator(); otherIt.hasNext(); ) {
            final Object other = (Object) otherIt.next();
            if( lookup( other ) == i ) ret.add( other );
        }
        return ret;
    }

    public List allSets() {
        Set seen = new HashSet();
        List ret = new ArrayList();

        for( Iterator oIt = objectNumberMap.keySet().iterator(); oIt.hasNext(); ) {

            final Object o = (Object) oIt.next();
            if( seen.contains( o ) ) continue;
            List curlist = new ArrayList();
            ret.add( curlist );
            for( Iterator o2It = findSet( o ).iterator(); o2It.hasNext(); ) {
                final Object o2 = (Object) o2It.next();
                seen.add( o2 );
                curlist.add( o2 );
            }
        }

        return ret;
    }

    public void copy( UnionFind other ) {
        if( objectNumberMap != other.objectNumberMap )
            throw new RuntimeException( "Different objectNumberMaps" );

        for( int i = 0; i < preds.length; i++ ) other.preds[i] = preds[i];
        for( int i = 0; i < rank.length; i++ ) other.rank[i] = rank[i];
    }

    public boolean equals( Object o ) {
        UnionFind other = (UnionFind)o;
        if( objectNumberMap != other.objectNumberMap )
            throw new RuntimeException( "Different objectNumberMaps" );

        for( int i = 0; i < preds.length; i++ ) if (other.preds[i] != preds[i]) return false;
        for( int i = 0; i < rank.length; i++ ) if (other.rank[i] != rank[i]) return false;
        return true;
    }
}
