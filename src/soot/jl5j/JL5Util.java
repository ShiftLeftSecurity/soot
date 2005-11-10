package soot.jl5j;

import soot.javaToJimple.Util;

public class JL5Util extends Util{

    public static int getModifier(polyglot.types.Flags flags){
        int res = Util.getModifier(flags);
        if (polyglot.ext.jl5.types.JL5Flags.isEnumModifier(flags)){
            res = res | soot.Modifier.ENUM;
        }
        if (polyglot.ext.jl5.types.JL5Flags.isAnnotationModifier(flags)){
            res = res | soot.Modifier.ANNOTATION;
        }
        return res;
    }
}
