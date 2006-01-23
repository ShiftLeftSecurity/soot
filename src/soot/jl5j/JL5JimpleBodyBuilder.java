package soot.jl5j;

public class JL5JimpleBodyBuilder extends soot.javaToJimple.AbstractJimpleBodyBuilder {

    public JL5JimpleBodyBuilder(){
    
    }

    public boolean needsOuterClassRef(polyglot.types.ClassType typeToInvoke){
        if (polyglot.ext.jl5.types.JL5Flags.isEnumModifier(typeToInvoke.flags())) return false;
        return ext().needsOuterClassRef(typeToInvoke);
    }

    public soot.Value createExpr(polyglot.ast.Expr expr){
        if (expr instanceof polyglot.ext.jl5.ast.JL5Let){
            return createLet((polyglot.ext.jl5.ast.JL5Let)expr);
        }
        return ext().createExpr(expr);
    }

    public soot.Value createLet(polyglot.ext.jl5.ast.JL5Let let){
        createStmt(let.localDecl());
        //createExpr(let.alpha());
        return createExpr(let.beta());
    }

    public soot.Type getSootType(polyglot.types.Type polyglotType){
        return JL5Util.getSootType(polyglotType);
    }
}
