package ca.mcgill.sable.soot;

import java.util.*;

class Dummy
{
    String className;
    String methodName;
    List parameterTypes;
    Type returnType;
    
    Dummy o;

    int hashCode;

    public Dummy(String className, String methodName, 
        List parameterTypes, Type returnType)
    {
        this.className = className;
        this.methodName = methodName;

        this.parameterTypes = new ArrayList();
        this.parameterTypes.addAll(parameterTypes);
        this.returnType = returnType;
    }
    
}
