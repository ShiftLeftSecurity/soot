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

    public static soot.Type getSootType(polyglot.types.Type type){
        if (type instanceof polyglot.ext.jl5.types.ParameterizedType){
            return Util.getSootType(((polyglot.ext.jl5.types.ParameterizedType)type).baseType());
        }
        else if (type instanceof polyglot.ext.jl5.types.IntersectionType){
            return Util.getSootType(((polyglot.ext.jl5.types.IntersectionType)type).erasureType());
        }
        else if (type instanceof polyglot.ext.jl5.types.JL5ArrayType){
            polyglot.types.Type polyglotBase = ((polyglot.types.ArrayType)type).base();
            while (polyglotBase instanceof polyglot.types.ArrayType) {
                polyglotBase = ((polyglot.types.ArrayType)polyglotBase).base();
            }
            soot.Type baseType = getSootType(polyglotBase);
            int dims = ((polyglot.types.ArrayType)type).dims();
            // do something here if baseType is still an array
            return soot.ArrayType.v(baseType, dims);
        }
        else if (type instanceof polyglot.ext.jl5.types.AnyType){
            return Util.getSootType(((polyglot.ext.jl5.types.AnyType)type).upperBound());
        }
        else if (type instanceof polyglot.ext.jl5.types.AnySuperType){
            return Util.getSootType(((polyglot.ext.jl5.types.AnySuperType)type).upperBound());
        }
        else if (type instanceof polyglot.ext.jl5.types.AnySubType){
            return Util.getSootType(((polyglot.ext.jl5.types.AnySubType)type).bound());
        }
        return Util.getSootType(type);
    }
}
