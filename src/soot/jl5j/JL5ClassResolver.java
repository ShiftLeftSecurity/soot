package soot.jl5j;

import polyglot.ext.jl5.ast.*;
import soot.tagkit.*;
import java.util.*;
import polyglot.ast.*;

public class JL5ClassResolver extends AbstractClassResolver {


    private VisibilityAnnotationTag createRuntimeVisibleAnnotationTag(List annots){
        VisibilityAnnotationTag tag = new VisibilityAnnotationTag(AnnotationConstants.RUNTIME_VISIBLE);
        addAnnotations(tag, annots);
        return tag;
    }

    private VisibilityAnnotationTag createRuntimeInvisibleAnnotationTag(List annots){
        VisibilityAnnotationTag tag = new VisibilityAnnotationTag(AnnotationConstants.RUNTIME_INVISIBLE);
        addAnnotations(tag, annots);
        return tag;
    }

    private void addAnnotations(VisibilityAnnotationTag tag, List annots){
        for (Iterator it = annots.iterator(); it.hasNext(); ){
            NormalAnnotationElem ae = (NormalAnnotationElem)it.next();
            String annotName = ae.typeName().type().toClass().fullName();
            annotName = "L" + annotName + ";";
            AnnotationTag aTag = new AnnotationTag(annotName, ae.elements().size());
            aTag.setElems(createElemTags(ae.elements()));
            tag.addAnnotation(aTag);
        }
    }
    
    private ArrayList createElemTags(List elements){
        ArrayList list = new ArrayList();
        for (Iterator it = elements.iterator(); it.hasNext(); ){
            ElementValuePair elemValue = (ElementValuePair)it.next();
            String name = elemValue.name();
            Expr value = elemValue.value();
            list.add(createAnnotationElem(name, value));
        }
        return list;
    }
   
    private soot.tagkit.AnnotationElem createAnnotationElem(String name, polyglot.ast.Expr value){
        if (value instanceof IntLit && ((IntLit)value).kind() == polyglot.ast.IntLit.INT){
            int constVal = ((Integer)((IntLit)value).constantValue()).intValue();
            return new AnnotationIntElem(constVal, 'I', name);
        }
        else if (value instanceof IntLit && ((IntLit)value).kind() == polyglot.ast.IntLit.LONG){
            long constVal = ((Long)((IntLit)value).constantValue()).longValue();
            return new AnnotationLongElem(constVal, 'J', name);
        }
        else if (value instanceof CharLit){
            char constVal = ((Character)((CharLit)value).constantValue()).charValue();
            return new AnnotationIntElem(constVal, 'C', name);
        }
        else if (value instanceof BooleanLit){
            int constVal = ((Boolean)((BooleanLit)value).constantValue()).booleanValue() ? 1 : 0;
            return new AnnotationIntElem(constVal, 'Z', name); 
        }
        else if (value instanceof FloatLit && ((FloatLit)value).kind() == polyglot.ast.FloatLit.FLOAT){
            float constVal = ((Float)((FloatLit)value).constantValue()).floatValue();
            return new AnnotationFloatElem(constVal, 'F', name);
        }
        else if (value instanceof FloatLit && ((FloatLit)value).kind() == polyglot.ast.FloatLit.DOUBLE){
            double constVal = ((Double)((FloatLit)value).constantValue()).doubleValue();
            return new AnnotationDoubleElem(constVal, 'D', name);
        }
        else if (value instanceof StringLit){
            String constVal = (String)((StringLit)value).constantValue();
            return new AnnotationStringElem(constVal, 's', name);
        }
        else if (value instanceof EnumConstant || value instanceof JL5Field){
            String enumName = ((polyglot.types.Named) ((JL5Field)value).target().type()).fullName();
            String simpleName = ((JL5Field)value).name();
            return new AnnotationEnumElem(enumName, simpleName, 'e', name);
        }
        else if (value instanceof ClassLit){
            String constVal = ((polyglot.types.Named) ((ClassLit)value).typeNode().type()).fullName();
            return new AnnotationClassElem(constVal, 'c', name);
        }
        else if (value instanceof ArrayInit){
            ArrayList l = new ArrayList();
            for (Iterator it = ((ArrayInit)value).elements().iterator(); it.hasNext(); ){
                Expr expr = (Expr)it.next();
                l.add(createAnnotationElem(name, expr));
            }
            return new AnnotationArrayElem(l, '[', name);
        }
        else if (value instanceof polyglot.ext.jl5.ast.AnnotationElem){
            String annotName = ((polyglot.types.Named)((polyglot.ext.jl5.ast.AnnotationElem)value).typeName().type()).fullName();
            annotName = "L" + annotName + ";";
            AnnotationTag aTag = new AnnotationTag(annotName, ((NormalAnnotationElem)value).elements().size());
            aTag.setElems(createElemTags(((NormalAnnotationElem)value).elements()));
            return new AnnotationAnnotationElem(aTag, '@', name);
        }
        else if (value instanceof Unary){
            if (((Unary)value).operator() == polyglot.ast.Unary.POS){
                return createAnnotationElem(name, ((Unary)value).expr());
            }
            else if (((Unary)value).operator() == polyglot.ast.Unary.NEG){
                if (((Unary)value).expr() instanceof FloatLit && ((FloatLit)((Unary)value).expr()).kind() == polyglot.ast.FloatLit.FLOAT){
                    float constVal = -((Float)((FloatLit)((Unary)value).expr()).constantValue()).floatValue();
                    return new AnnotationFloatElem(constVal, 'F', name);
                }
                else if (((Unary)value).expr() instanceof FloatLit && ((FloatLit)((Unary)value).expr()).kind() == polyglot.ast.FloatLit.DOUBLE){
                    double constVal = -((Double)((FloatLit)((Unary)value).expr()).constantValue()).doubleValue();
                    return new AnnotationDoubleElem(constVal, 'D', name);
                }
                else if (((Unary)value).expr() instanceof IntLit && ((IntLit)((Unary)value).expr()).kind() == polyglot.ast.IntLit.INT){
                    int constVal = -((Integer)((IntLit)((Unary)value).expr()).constantValue()).intValue();
                    return new AnnotationIntElem(constVal, 'I', name);
                }
                else if (((Unary)value).expr() instanceof IntLit && ((IntLit)((Unary)value).expr()).kind() == polyglot.ast.IntLit.LONG){
                    long constVal = -((Long)((IntLit)((Unary)value).expr()).constantValue()).longValue();
                    return new AnnotationLongElem(constVal, 'J', name);
                }
                else {
                    throw new RuntimeException("Unexpected unary neg annotation elem: "+value);
                }
            }
            else {
                throw new RuntimeException("Unexpected unary annotation elem: "+value);
            }
        }
        else if (value instanceof Cast){
            if (((Cast)value).castType().type().isByte()){
                int constVal = ((Integer)((Cast)value).expr().constantValue()).intValue();
                return new AnnotationIntElem(constVal, 'B', name);
            }
            else if (((Cast)value).castType().type().isShort()){
                int constVal = ((Integer)((Cast)value).expr().constantValue()).intValue();
                return new AnnotationIntElem(constVal, 'S', name);
            }
            else if (((Cast)value).castType().type().isInt()){
                int constVal = ((Integer)((Cast)value).expr().constantValue()).intValue();
                return new AnnotationIntElem(constVal, 'I', name);
            }
            else {
                throw new RuntimeException("Unexpected cast annotation elem: "+value);
            }
        }
        else {
            throw new RuntimeException("Unexpected annotation elem: "+value);
        }
    }
    
    public void createClassDecl(polyglot.ast.ClassDecl cDecl){
        ext().createClassDecl(cDecl);
        List runtimeAnnots = ((polyglot.ext.jl5.ast.JL5ClassDecl)cDecl).runtimeAnnotations();
        List classAnnots = ((polyglot.ext.jl5.ast.JL5ClassDecl)cDecl).classAnnotations();
        if (!runtimeAnnots.isEmpty()){
            sootClass.addTag(createRuntimeVisibleAnnotationTag(runtimeAnnots));
        }
        if (!classAnnots.isEmpty()){
            sootClass.addTag(createRuntimeInvisibleAnnotationTag(classAnnots));
        }

        if (needsClassSignature((JL5ClassDecl)cDecl)){
            StringBuffer sig = new StringBuffer();
            sig.append(createParamTypesSig(((JL5ClassDecl)cDecl).paramTypes()));
            sig.append(((polyglot.ext.jl5.types.SignatureType)cDecl.type().superType()).signature());
            for (Iterator it = cDecl.interfaces().iterator(); it.hasNext(); ){
                sig.append(((polyglot.ext.jl5.types.SignatureType)((TypeNode)it.next()).type()).signature());
            }
            sootClass.addTag(new SignatureTag(sig.toString()));
        }
    }

    private boolean needsClassSignature(JL5ClassDecl classDecl){
        if (!classDecl.paramTypes().isEmpty()) return true;
        // not sure that a super class can be an intersection type
        if (classDecl.type().superType() instanceof polyglot.ext.jl5.types.IntersectionType || classDecl.type().superType() instanceof polyglot.ext.jl5.types.ParameterizedType) return true;
        if (typeNodeListNeedsSignature(classDecl.interfaces())) return true;
        return false;
    }

    public void createClassBody(polyglot.ast.ClassBody classBody){
        staticFieldInits = null;
        fieldInits = null;
        initializerBlocks = null;
        staticInitializerBlocks = null;

        for (Iterator it = classBody.members().iterator(); it.hasNext(); ){
            ClassMember next = (ClassMember)it.next();
            if (next instanceof MethodDecl){
                base().createMethodDecl((MethodDecl)next);
            }
            else if (next instanceof FieldDecl){
                base().createFieldDecl((FieldDecl)next);
            }
            else if (next instanceof ConstructorDecl){
                base().createConstructorDecl((ConstructorDecl)next);
            }
            else if (next instanceof ClassDecl){
                JL5Util.addInnerClassTag(sootClass, JL5Util.getSootType(((ClassDecl)next).type()).toString(), sootClass.getName(), ((ClassDecl)next).name().toString(), getModifiers(((ClassDecl)next).flags()));
            }
            else if (next instanceof Initializer){
                createInitializer((Initializer)next);
            }
            else if (next instanceof AnnotationElemDecl){
                createAnnotationElemDecl((AnnotationElemDecl)next);
            }
            else {
                throw new RuntimeException("Unexpected class body member: "+next);
            }
        }

        handleInnerClassTags(classBody);
        handleClassLiteral(classBody);
        handleAssert(classBody);
    }

    public void createMethodDecl(MethodDecl method){
        String name = createName(method);
        ArrayList formalTypes = new ArrayList();
        VisibilityParameterAnnotationTag rtag = null;
        VisibilityParameterAnnotationTag ctag = null;

        for (Iterator it = method.formals().iterator(); it.hasNext(); ){
            JL5Formal f = (JL5Formal)it.next();
            if (!f.runtimeAnnotations().isEmpty()){
                if (rtag == null){
                    rtag = new VisibilityParameterAnnotationTag(method.formals().size(), AnnotationConstants.RUNTIME_VISIBLE);
                }
                rtag.addVisibilityAnnotation(createRuntimeVisibleAnnotationTag(f.runtimeAnnotations()));
            }
            if (!f.classAnnotations().isEmpty()){
                if (ctag == null){
                    ctag = new VisibilityParameterAnnotationTag(method.formals().size(), AnnotationConstants.RUNTIME_INVISIBLE);
                }
                ctag.addVisibilityAnnotation(createRuntimeInvisibleAnnotationTag(f.classAnnotations()));
            }
            formalTypes.add(JL5Util.getSootType(f.type().type()));
        }
        ArrayList exceptions = createExceptions(method);
        soot.SootMethod sm = createSootMethod(name, method.flags(), method.returnType().type(), formalTypes, exceptions);

        finishProcedure(method, sm);
        if (rtag != null){
            sm.addTag(rtag);
        }
        if (ctag != null){
            sm.addTag(ctag);
        }

        if (!((JL5MethodDecl)method).runtimeAnnotations().isEmpty()){
            sm.addTag(createRuntimeVisibleAnnotationTag(((JL5MethodDecl)method).runtimeAnnotations()));
        }
        if (!((JL5MethodDecl)method).classAnnotations().isEmpty()){
            sm.addTag(createRuntimeInvisibleAnnotationTag(((JL5MethodDecl)method).classAnnotations()));
        }
    
        if (needsMethodSignature((JL5MethodDecl)method)){
            StringBuffer sig = new StringBuffer();
            sig.append(createParamTypesSig(((JL5MethodDecl)method).paramTypes()));
            sig.append(createFormalsSig(method.formals()));
            sig.append(((polyglot.ext.jl5.types.SignatureType)method.returnType().type()).signature());
            sm.addTag(new SignatureTag(sig.toString()));
        }
    }

    private String createFormalsSig(List formals){
        StringBuffer sig = new StringBuffer();
        sig.append("(");
        for (Iterator it = formals.iterator(); it.hasNext(); ){
            sig.append(((polyglot.ext.jl5.types.SignatureType)((Formal)it.next()).type().type()).signature());
        }
        sig.append(")");
        return sig.toString();
    }
    
    private String createParamTypesSig(List paramTypes){
        StringBuffer sig = new StringBuffer();
        if (!paramTypes.isEmpty()){
            sig.append("<");
            for (Iterator it = paramTypes.iterator(); it.hasNext(); ){
                polyglot.ext.jl5.types.IntersectionType next = (polyglot.ext.jl5.types.IntersectionType)((TypeNode)it.next()).type();
                sig.append(next.name());
                if (next.bounds().isEmpty()){
                    sig.append(":"+((polyglot.ext.jl5.types.SignatureType)next.erasureType()).signature());
                }
                else {
                    for (Iterator nt = next.bounds().iterator(); nt.hasNext(); ){
                        sig.append(":"+((polyglot.ext.jl5.types.SignatureType)nt.next()).signature());
                    }
                }
            }
            sig.append(">");
        }
        return sig.toString();
    }
    
    private boolean needsMethodSignature(JL5MethodDecl md){
        if (!md.paramTypes().isEmpty()) return true;
        if (md.returnType().type() instanceof polyglot.ext.jl5.types.IntersectionType || md.returnType().type() instanceof polyglot.ext.jl5.types.ParameterizedType) return true;
        if (formalListNeedsSignature(md.formals())){
            return true;
        }
        return false;
    }

    private boolean needsConstructorSignature(JL5ConstructorDecl cd){
        if (!cd.paramTypes().isEmpty()) return true;
        if (formalListNeedsSignature(cd.formals())){
            return true;
        }
        return false; 
    }

    private boolean typeNodeListNeedsSignature(List l){
        for (Iterator it = l.iterator(); it.hasNext(); ){
            TypeNode tn = (TypeNode)it.next();
            if (tn.type() instanceof polyglot.ext.jl5.types.IntersectionType || tn.type() instanceof polyglot.ext.jl5.types.ParameterizedType){
                return true;
            }
        }
        return false;
    }
    
    private boolean formalListNeedsSignature(List l){
        for (Iterator it = l.iterator(); it.hasNext(); ){
            Formal f = (Formal)it.next();
            if (f.type().type() instanceof polyglot.ext.jl5.types.IntersectionType || f.type().type() instanceof polyglot.ext.jl5.types.ParameterizedType){
                return true;
            }
        }
        return false;
    }
    

    public void createConstructorDecl(ConstructorDecl constructor){
        String name = "<init>";
        ArrayList formalTypes = new ArrayList();
        VisibilityParameterAnnotationTag rtag = null;
        VisibilityParameterAnnotationTag ctag = null;

        for (Iterator it = constructor.formals().iterator(); it.hasNext(); ){
            JL5Formal f = (JL5Formal)it.next();
            if (f.runtimeAnnotations() != null && !f.runtimeAnnotations().isEmpty()){
                if (rtag == null){
                    rtag = new VisibilityParameterAnnotationTag(constructor.formals().size(), AnnotationConstants.RUNTIME_VISIBLE);
                }
                rtag.addVisibilityAnnotation(createRuntimeVisibleAnnotationTag(f.runtimeAnnotations()));
            }
            if (f.classAnnotations() != null && !f.classAnnotations().isEmpty()){
                if (ctag == null){
                    ctag = new VisibilityParameterAnnotationTag(constructor.formals().size(), AnnotationConstants.RUNTIME_INVISIBLE);
                }
                ctag.addVisibilityAnnotation(createRuntimeInvisibleAnnotationTag(f.classAnnotations()));
            }
            formalTypes.add(JL5Util.getSootType(f.type().type()));
        }
        ArrayList exceptions = createExceptions(constructor);
        soot.SootMethod sm = createSootConstructor(name, constructor.flags(), formalTypes, exceptions);
        
        finishProcedure(constructor, sm);
        if (rtag != null){
            sm.addTag(rtag);
        }
        if (ctag != null){
            sm.addTag(ctag);
        }

        if (!((JL5ConstructorDecl)constructor).runtimeAnnotations().isEmpty()){
            sm.addTag(createRuntimeVisibleAnnotationTag(((JL5ConstructorDecl)constructor).runtimeAnnotations()));
        }
        if (!((JL5ConstructorDecl)constructor).classAnnotations().isEmpty()){
            sm.addTag(createRuntimeInvisibleAnnotationTag(((JL5ConstructorDecl)constructor).classAnnotations()));
        }
        if (needsConstructorSignature((JL5ConstructorDecl)constructor)){
            StringBuffer sig = new StringBuffer();
            sig.append(createParamTypesSig(((JL5ConstructorDecl)constructor).paramTypes()));
            sig.append(createFormalsSig(constructor.formals()));
            sig.append("V");
            sm.addTag(new SignatureTag(sig.toString()));
        }
        
    }

    public void createFieldDecl(FieldDecl field){
        ext().createFieldDecl(field);

        soot.SootField sootField = sootClass.getField(field.fieldInstance().name(), JL5Util.getSootType(field.fieldInstance().type()));
        
        if (!((JL5FieldDecl)field).runtimeAnnotations().isEmpty()){
            sootField.addTag(createRuntimeVisibleAnnotationTag(((JL5FieldDecl)field).runtimeAnnotations()));
        }
        if (!((JL5FieldDecl)field).classAnnotations().isEmpty()){
            sootField.addTag(createRuntimeInvisibleAnnotationTag(((JL5FieldDecl)field).classAnnotations()));
        }

        if (field.type().type() instanceof polyglot.ext.jl5.types.IntersectionType){
            sootField.addTag(new SignatureTag(((polyglot.ext.jl5.types.SignatureType)field.type().type()).signature()));
        }
        else if (field.type().type() instanceof polyglot.ext.jl5.types.ParameterizedType){
            sootField.addTag(new SignatureTag(((polyglot.ext.jl5.types.SignatureType)field.type().type()).signature()));
        }
    }

    public soot.Type getSootType(polyglot.types.Type polyglotType){
        return JL5Util.getSootType(polyglotType);
    }
    
    public int getModifiers(polyglot.types.Flags flags){
        return JL5Util.getModifier(flags);
    }

    public void createAnnotationElemDecl(polyglot.ext.jl5.ast.AnnotationElemDecl annotDecl){
        soot.SootMethod sm = createSootMethod(annotDecl.name(), annotDecl.flags(), annotDecl.type().type(), new ArrayList(), new ArrayList());
        addProcedureToClass(sm);

        if (annotDecl.position() != null){
            JL5Util.addLnPosTags(sm, annotDecl.position());
        }

        if (annotDecl.defaultVal() != null){
            sm.addTag(new AnnotationDefaultTag(createAnnotationElem("default", annotDecl.defaultVal())));
        }
    }

    public JL5ClassResolver(soot.SootClass sootClass, List refs){
        this.sootClass = sootClass;
        this.references = refs;
    }
    
    // this is overridden to prevent Soot from trying to 
    // load intersection types
    public void findReferences(polyglot.ast.Node node) {
        soot.javaToJimple.TypeListBuilder typeListBuilder = new soot.javaToJimple.TypeListBuilder();
        
        node.visit(typeListBuilder);

        for( Iterator typeIt = typeListBuilder.getList().iterator(); typeIt.hasNext(); ) {

            final polyglot.types.Type type = (polyglot.types.Type) typeIt.next();
            if (type.isPrimitive()) continue;
            if (type instanceof polyglot.ext.jl5.types.IntersectionType) continue;
            if (!type.isClass()) continue;
            polyglot.types.ClassType classType = (polyglot.types.ClassType)type;
            soot.Type sootClassType = base().getSootType(classType);
            System.out.println("adding type ref: "+sootClassType);
            references.add(sootClassType);
        }
    }

}
