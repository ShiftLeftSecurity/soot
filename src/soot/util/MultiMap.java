package soot.util;
import java.util.*;

public interface MultiMap {
    public boolean isEmpty();
    public int numKeys();
    public boolean containsKey( Object key );
    public boolean containsValue( Object value );
    public boolean put( Object key, Object value );
    public boolean putAll( Object key, Set values );
    public void putAll( MultiMap m );
//    public boolean putAll( Map m );
    public boolean remove( Object key, Object value );
    public boolean remove( Object key );
    public boolean removeAll( Object key, Set values );
    public Set get( Object o );
    public Set keySet();
    public Set values();
    public boolean equals( Object o );
    public int hashCode();
}
