package ca.mcgill.sable.soot.jimple.toolkit.invoke;

import ca.mcgill.sable.soot.*;

public class WholeProgramUtil
{
    static boolean isStrictSuperClass (SootClass sc1, SootClass sc2) 
    {
        boolean result = false;
        SootClass parent = sc1;
        while (parent.hasSuperClass())
        {
            parent = parent.getSuperClass();
            if (parent.getName().equals(sc2.getName()))
                return true;
        }
        
        return false;
    }

    public static String getPackageName (String classname) 
    { 
        int index = classname.lastIndexOf ('.');
        String packagename = null;

        if ( index > -1 )
            packagename = classname.substring (0, index);

        return packagename;
    }

    public static boolean isSamePackage (String s1, String s2) 
    {
        if ((s1 == null) && (s2 == null))
            return true;
        else if ((s1 != null) && (s2 != null))
        {
            if ((s1.compareTo (s2)) == 0)
                return true;
        }

        return false;
    }
}
