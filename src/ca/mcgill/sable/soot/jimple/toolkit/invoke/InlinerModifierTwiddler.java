package ca.mcgill.sable.soot.jimple.toolkit.invoke;

import java.util.*;
import ca.mcgill.sable.soot.*;
import ca.mcgill.sable.soot.jimple.*;

class InlinerModifierTwiddler
{
    static Integer priv = new Integer ( 0 );
    static Integer def = new Integer ( 1 );
    static Integer prot = new Integer ( 2 );
    static Integer pub = new Integer ( 3 );

    // must initialize these.
    Resolver res;
    Scene scm = res.getManager();
    ClassGraphBuilder clgb = res.getClassGraphBuilder();

    // must resolve from Inliner.
    private List classesToProcess = null;
    private boolean includeLibraries = false;

    // must merge back into Inliner.
    Collection changedClasses;

    // ???
    int change;

    static Integer getCorrectModifier (int modifiers) 
    {
        if (Modifier.isPublic (modifiers))
            return pub;
        else if (Modifier.isProtected (modifiers))
            return prot;
        else if (Modifier.isPrivate (modifiers))
            return priv;
        else
            return def;
    }

    int getChangedClassModifiers (String s, int modifiers, HashMap classesHT) 
    {
        int changedmodifiers = modifiers;
        changedmodifiers = changedmodifiers & 0xFFFB;
        changedmodifiers = changedmodifiers & 0xFFFD;
        changedmodifiers = changedmodifiers & 0xFFFE;
        if (((Integer) classesHT.get (s)).intValue() == 3)
            changedmodifiers = changedmodifiers | 0x0001;
        return changedmodifiers;
    }

    boolean changeModifiersOfAccessesFrom (SootMethod method,
                                           HashMap classesHT,
                                           HashMap methodsHT,
                                           HashMap fieldsHT) 
    {
        boolean haveRef = false;
        SootClass sc = null;
        JimpleBody body = Jimplifier.getJimpleBody(method);
        Iterator localsIt = body.getLocals().iterator();

        // Process all of the non-base types used as local types.
        while (localsIt.hasNext())
        {
            Local loc = (Local) localsIt.next();

            if (loc.getType() instanceof RefType)
            {
                haveRef = true;
                String locName = loc.getType().toString();
                sc = scm.getClass (locName);
            }
            else if (loc.getType() instanceof ArrayType)
            {
               Type t = ((ArrayType) loc.getType()).baseType;
               if (t instanceof RefType)
               {
                  haveRef = true;
                  String locName = t.toString();
                  sc = scm.getClass(locName);
               }
            }

            if (haveRef)
            {
               if (((Integer)classesHT.get (sc.getName())).intValue() 
                   > (getCorrectModifier (sc.getModifiers())).intValue())
               {
                   if (!allowedToChange(sc.getName()))
                       return false;
                   sc.setModifiers (getChangedClassModifiers
                                    (sc.getName(), sc.getModifiers(), 
                                     classesHT));
                   changedClasses.add(sc);
               }
            }
         }

         Iterator it = body.getUnits().iterator();
         while (it.hasNext())
         {
            Stmt s = (Stmt) it.next();
            List boxes = s.getUseAndDefBoxes();
            Iterator boxit = boxes.iterator();
            while (boxit.hasNext())
            {
               ValueBox vb = (ValueBox) boxit.next();
               Value v = vb.getValue();
               SootClass dec = null;
               SootField field = null;
               Value im = null;
               if (v instanceof InstanceFieldRef)
               {
                  InstanceFieldRef ifr = (InstanceFieldRef) v;
                  field = ifr.getField();
                  dec = scm.getClass (field.getDeclaringClass().getName());
                  field = dec.getFieldByName(field.getName());
                  if (((Integer) classesHT.get (dec.getName())).intValue() > (getCorrectModifier (dec.getModifiers())).intValue())
                  {

                     if (! allowedToChange (dec.getName()))
                     return false;
                     dec.setModifiers (getChangedClassModifiers (dec.getName(), dec.getModifiers(), classesHT));
                     changedClasses.add (dec);
                  }

                  if (((Integer) fieldsHT.get (field.getSignature())).intValue() > (getCorrectModifier (field.getModifiers())).intValue())
                  {
                     if (! allowedToChange (dec.getName()))
                     return false;
                     field.setModifiers (getChangedFieldModifiers (field.getSignature(), field.getModifiers(), fieldsHT));
                     changedClasses.add (dec);
                  }

               }

               else if (v instanceof StaticFieldRef)
               {
                  StaticFieldRef sfr = (StaticFieldRef) v;
                  field = sfr.getField();
                  dec = scm.getClass (field.getDeclaringClass().getName());
                  field = dec.getFieldByName(field.getName());
                  if (((Integer) classesHT.get (dec.getName())).intValue() > (getCorrectModifier (dec.getModifiers())).intValue())
                  {
                     if (! allowedToChange (dec.getName()))
                     return false;
                     dec.setModifiers (getChangedClassModifiers (dec.getName(), dec.getModifiers(), classesHT));
                     changedClasses.add (dec);
                  }

                  if (((Integer) fieldsHT.get (field.getSignature())).intValue() > (getCorrectModifier (field.getModifiers())).intValue())
                  {
                     if (! allowedToChange (dec.getName()))
                     return false;
                     field.setModifiers (getChangedFieldModifiers (field.getSignature(), field.getModifiers(), fieldsHT));
                     changedClasses.add(dec);
                  }

               }

               else if (v instanceof CastExpr)
               {
                  haveRef = false;
                  CastExpr ce = (CastExpr) v;
                  Type t = ce.getType();
                  if (t instanceof RefType)
                  {
                     haveRef = true;
                     String locName = t.toString();
                     sc = scm.getClass (locName);
                  }

                  else if (t instanceof ArrayType)
                  {
                     Type ty = ((ArrayType) t).baseType;
                     if (ty instanceof RefType)
                     {
                        haveRef = true;
                        String locName = ty.toString();
                        sc = scm.getClass (locName);
                     }

                  }

                  if (haveRef)
                  {
                     if (((Integer) classesHT.get (sc.getName())).intValue() > (getCorrectModifier (sc.getModifiers())).intValue())
                     {
                        if (! allowedToChange (sc.getName()))
                        return false;
                        sc.setModifiers (getChangedClassModifiers (sc.getName(), sc.getModifiers(), classesHT));
                        changedClasses.add (sc);
                     }

                  }

               }

               else if (v instanceof InstanceOfExpr)
               {
                  haveRef = false;
                  InstanceOfExpr ce = (InstanceOfExpr) v;
                  Type t = ce.getCheckType();
                  if (t instanceof RefType)
                  {
                     haveRef = true;
                     String locName = t.toString();
                     sc = scm.getClass (locName);
                  }

                  else if (t instanceof ArrayType)
                  {
                     Type ty = ((ArrayType) t).baseType;
                     if (ty instanceof RefType)
                     {
                        haveRef = true;
                        String locName = ty.toString();
                        sc = scm.getClass (locName);
                     }

                  }

                  if (haveRef)
                  {
                     if (((Integer) classesHT.get (sc.getName())).intValue() > (getCorrectModifier (sc.getModifiers())).intValue())
                     {
                        if (!allowedToChange(sc.getName()))
                            return false;
                        sc.setModifiers(getChangedClassModifiers 
                                        (sc.getName(), sc.getModifiers(), 
                                         classesHT));
                        changedClasses.add (sc);
                     }

                  }

               }

               else if (v instanceof NewExpr)
               {
                  NewExpr newexpr = (NewExpr) v;
                  RefType t = newexpr.getBaseType();
                  String locName = t.toString();
                  sc = scm.getClass (locName);
                  if (((Integer) classesHT.get (sc.getName())).intValue() > (getCorrectModifier (sc.getModifiers())).intValue())
                  {
                     if (! allowedToChange (sc.getName()))
                     return false;
                     sc.setModifiers (getChangedClassModifiers (sc.getName(), sc.getModifiers(), classesHT));
                     changedClasses.add (sc);
                  }

               }

               else if (v instanceof NewArrayExpr)
               {
                  haveRef = false;
                  NewArrayExpr newarrayexpr = (NewArrayExpr) v;
                  Type t = newarrayexpr.getBaseType();
                  if (t instanceof RefType)
                  {
                     haveRef = true;
                     String locName = t.toString();
                     sc = scm.getClass(locName);
                  }

                  else if (t instanceof ArrayType)
                  {
                     Type ty = ((ArrayType) t).baseType;
                     if (ty instanceof RefType)
                     {
                        haveRef = true;
                        String locName = ty.toString();
                        sc = scm.getClass(locName);
                     }

                  }

                  if (haveRef)
                  {
                     if (((Integer) classesHT.get (sc.getName())).intValue() > (getCorrectModifier (sc.getModifiers())).intValue())
                     {
                        if (! allowedToChange (sc.getName()))
                        return false;
                        sc.setModifiers (getChangedClassModifiers (sc.getName(), sc.getModifiers(), classesHT));
                        changedClasses.add (sc);
                     }

                  }

               }

               else if (v instanceof NewMultiArrayExpr)
               {
                  haveRef = false;
                  NewMultiArrayExpr newmultiarrayexpr = (NewMultiArrayExpr) v;
                  Type t = newmultiarrayexpr.getBaseType();
                  if (t instanceof RefType)
                  {
                     haveRef = true;
                     String locName = t.toString();
                     sc = scm.getClass (locName);
                  }

                  else if (t instanceof ArrayType)
                  {
                     Type ty = ((ArrayType) t).baseType;
                     if (ty instanceof RefType)
                     {
                        haveRef = true;
                        String locName = ty.toString();
                        sc = scm.getClass (locName);
                     }

                  }

                  if (haveRef)
                  {
                     if (((Integer) classesHT.get (sc.getName())).intValue() > (getCorrectModifier (sc.getModifiers())).intValue())
                     {
                        if (! allowedToChange (sc.getName()))
                        return false;
                        sc.setModifiers (getChangedClassModifiers (sc.getName(), sc.getModifiers(), classesHT));
                        changedClasses.add (sc);
                     }

                  }

               }

               else if (v instanceof InvokeExpr)
               {
                  InvokeExpr stinvexpr = (InvokeExpr) v;
                  int argcount = stinvexpr.getArgCount();
                  int counter = 0;
                  while (counter < argcount)
                  {
                     haveRef = false;
                     if (stinvexpr.getMethod().getParameterType(counter) instanceof RefType)
                     {
                        haveRef = true;
                        String argtype = stinvexpr.getMethod().getParameterType(counter).toString();
                        sc = scm.getClass (argtype);
                     }

                     else if (stinvexpr.getMethod().getParameterType(counter) instanceof ArrayType)
                     {
                        Type t = ((ArrayType) stinvexpr.getMethod().getParameterType(counter)).baseType;
                        if (t instanceof RefType)
                        {
                           haveRef = true;
                           String argtype = t.toString();
                           sc = scm.getClass (argtype);
                        }

                     }

                     if (haveRef)
                     {
                        if (((Integer) classesHT.get (sc.getName())).intValue() > (getCorrectModifier (sc.getModifiers())).intValue())
                        {
                           if (! allowedToChange (sc.getName()))
                           return false;
                           sc.setModifiers (getChangedClassModifiers (sc.getName(), sc.getModifiers(), classesHT));
                           changedClasses.add (sc);
                        }

                     }

                     counter++;
                  }

                  SootMethod meth = stinvexpr.getMethod();
                  dec = scm.getClass (meth.getDeclaringClass().getName());
                  meth = dec.getMethod (meth.getName(), meth.getParameterTypes());
                  if (((Integer) classesHT.get (dec.getName())).intValue() > (getCorrectModifier (dec.getModifiers())).intValue())
                  {
                     if (! allowedToChange (dec.getName()))
                     return false;
                     dec.setModifiers (getChangedClassModifiers (dec.getName(), dec.getModifiers(), classesHT));
                     changedClasses.add (dec);
                  }

                  if (((Integer) methodsHT.get (meth.getSignature())).intValue() > (getCorrectModifier (meth.getModifiers())).intValue())
                  {
                     if (!allowedToChange (dec.getName()))
                         return false;
                     int newmodifiers = getChangedMethodModifiers 
                         (meth.getModifiers(), ((Integer)methodsHT.get (s)).intValue(), false);
                     adjustSubMethods (meth, newmodifiers);

                     meth.setModifiers (newmodifiers);
                     changedClasses.add (dec);
                  }

               }

            }

         }

      return true;
   }


    boolean allowedToChange (String cname) 
    {
        boolean result;
        result = (!clgb.getIncorrectlyJimplifiedClasses().contains (cname));

        if (!includeLibraries)
            result = result && (! isLibraryNode (cname));

        if (classesToProcess != null)
            result = result && (classesToProcess.contains (cname));

        return result;
    }


   boolean isLibraryNode (String cname) {
      boolean isJava = ClassGraphBuilder.isLibraryNode("java.",cname);
      boolean isSun = ClassGraphBuilder.isLibraryNode("sun.",cname);
      boolean isSunw = ClassGraphBuilder.isLibraryNode("sunw.",cname);
      boolean isJavax = ClassGraphBuilder.isLibraryNode("javax.",cname);
      boolean isOrg = ClassGraphBuilder.isLibraryNode("org.",cname);
      boolean isCom = ClassGraphBuilder.isLibraryNode("com.",cname);
      return (isJava || isSun || isSunw || isJavax || isOrg || isCom);
   }


   void adjustSubMethods (SootMethod m, int newmodifiers) {
      ClassNode cn = clgb.getNode (m.getDeclaringClass().getName());
      Set subclassnodes = clgb.getAllSubClassesOf (cn);
      Iterator subclassnodesit = subclassnodes.iterator();

      while (subclassnodesit.hasNext())
      {
         ClassNode subcn = (ClassNode) subclassnodesit.next();
         SootClass subclass = scm.getClass (subcn.getSootClass().getName());
         if (subclass.declaresMethod (m.getName(), m.getParameterTypes()))
         {
            SootMethod submeth = subclass.getMethod (m.getName(), m.getParameterTypes());
            if (getCorrectModifier (submeth.getModifiers()).intValue() < getCorrectModifier (newmodifiers).intValue())
            {
               if (!allowedToChange (subclass.getName()))
                   throw new NotAllowedToChangeException();
               submeth.setModifiers (getChangedMethodModifiers (submeth.getModifiers(), 0, true));
               changedClasses.add (subclass);
            }
         }

      }
   }

    int getChangedMethodModifiers (int modifiers, int htVal,
                                   boolean adjustingSubMethods) 
    {
        int changedmodifiers = modifiers & 0xfff8;

        if (adjustingSubMethods)
        {
            if (change == 3)
                changedmodifiers = changedmodifiers | 0x0001;
            else if (change == 2)
                changedmodifiers = changedmodifiers | 0x0004;
            else if (change == 0)
                changedmodifiers = changedmodifiers | 0x0002;
        }
        else
        {
            if (htVal == 3)
            {
                changedmodifiers = changedmodifiers | 0x0001;
                change = 3;
            }
            else if (htVal == 2)
            {
                changedmodifiers = changedmodifiers | 0x0004;
                change = 2;
            }
            else if (htVal == 0)
            {
                changedmodifiers = changedmodifiers | 0x0002;
                change = 0;
            }
      }

      return changedmodifiers;
   }


   int getChangedFieldModifiers (String s, int modifiers, HashMap fieldsHT) {
      int changedmodifiers = modifiers;
      changedmodifiers = changedmodifiers & 0xFFFB;
      changedmodifiers = changedmodifiers & 0xFFFD;
      changedmodifiers = changedmodifiers & 0xFFFE;

      if (((Integer) fieldsHT.get (s)).intValue() == 3)
          changedmodifiers = changedmodifiers | 0x0001;
      else if (((Integer) fieldsHT.get (s)).intValue() == 2)
          changedmodifiers = changedmodifiers | 0x0004;
      else if (((Integer) fieldsHT.get (s)).intValue() == 0)
          changedmodifiers = changedmodifiers | 0x0002;

      return changedmodifiers;
   }

   String getPackageName (String classname) {
      int index = classname.lastIndexOf ('.');
      String packagename = null;
      if (index > -1)
      packagename = classname.substring (0, index);
      return packagename;
   }


   boolean isSameProtected (SootClass declaringclass, SootClass currclass, String baseType, String currpackage) {
      boolean answer = false;
      if (WholeProgramUtil.isSamePackage (WholeProgramUtil.getPackageName (declaringclass.getName()), currpackage))
      return true;
      else
      {
         boolean searching = true;
         SootClass sclass = currclass;
         if (sclass.getName().equals (declaringclass.getName()))
         searching = false;
         while ((sclass.hasSuperClass()) && (searching == true))
         {
            sclass = sclass.getSuperClass();
            if (sclass.getName().equals (declaringclass.getName()))
            searching = false;
         }

         if (searching == true)
         return false;
         else
         {
            searching = true;
            sclass = scm.getClass (baseType);
            if (sclass.getName().equals (currclass.getName()))
            searching = false;
            while ((sclass.hasSuperClass()) && (searching == true))
            {
               sclass = sclass.getSuperClass();
               if (sclass.getName().equals (currclass.getName()))
               searching = false;
            }

            if (searching == true)
            return false;
            else
            return true;
         }

      }

   }


   boolean isSameStaticProtected (SootClass declaringclass, SootClass currclass, String currpackage) {
      boolean answer = false;
      if (WholeProgramUtil.isSamePackage (WholeProgramUtil.getPackageName (declaringclass.getName()), currpackage))
      return true;
      else
      {
         boolean searching = true;
         SootClass sclass = currclass;
         if (sclass.getName().equals (declaringclass.getName()))
         searching = false;
         while ((sclass.hasSuperClass()) && (searching == true))
         {
            sclass = sclass.getSuperClass();
            if (sclass.getName().equals (declaringclass.getName()))
            searching = false;
         }

         if (searching == true)
         return false;
         else
         return true;
      }

   }
}
