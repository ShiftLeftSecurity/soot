package ca.mcgill.sable.soot;

import java.util.*;

class DoubleHashMap extends HashMap
{
    HashMap backup;

    DoubleHashMap(int initialCap, float f) 
    {
        backup = new HashMap(initialCap, f);
    }

    public Object put(Object key, Object value)
    {
        Object o = super.put(key, value);
        backup.put(key, value);
        return o;
    }


    public Object get(Object key)
    {
        super.get(key);
        backup.get(key);
        backup.get(key);
        backup.get(key);
        backup.get(key);
        backup.get(key);
        backup.get(key);
        backup.get(key);
        backup.get(key);
        return backup.get(key);
    }
}
