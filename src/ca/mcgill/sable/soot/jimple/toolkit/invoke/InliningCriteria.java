package ca.mcgill.sable.soot.jimple.toolkit.invoke;

import java.util.*;

import ca.mcgill.sable.soot.*;
import ca.mcgill.sable.soot.jimple.*;
import ca.mcgill.sable.soot.jimple.toolkit.invoke.*;

public class InliningCriteria
{
    int criteria0 = 0, criteria1 = 0, criteria2 = 0, criteria3 = 0;
    int criteria4 = 0, criteria5 = 0, criteria6 = 0, criteria7 = 0;
    int criteria8 = 0;

    boolean satisfiesCriteria (SootMethod currmethod, SootMethod m, InvokeExpr ie,
                               InlinerState st) 
    {
        if (Inliner.NOLIB)
        {
            String cm = m.getDeclaringClass().getName();
            if (cm.startsWith ("java.") || cm.startsWith ("sun.") ||
                cm.startsWith ("sunw.") || cm.startsWith ("javax.") ||
                cm.startsWith ("org.")  || cm.startsWith ("com."))
                return false;
        }

        if (Inliner.classesToAnalyze != null)
        {
            if (!Inliner.classesToAnalyze.contains
                (m.getDeclaringClass().getName()))
                return false;
        }

       if (st.syncFlag && st.staticInvoked)
       {
           criteria1++;
           return false;
       }

       if ((Jimplifier.getJimpleBody(m).getUnits().size() 
                                > Inliner.averageSize))
       {
            criteria2++;
            return false;
       }

       if ((Modifier.isNative(m.getModifiers())) || 
           (Modifier.isAbstract (m.getModifiers())))
       {
          criteria3++;
          return false;
       }

       if ((st.res.getErrorInvokeExprs().contains(ie)))
       {
           criteria4++;
           return false;
       }

       if ((st.res.getErrorMethods().contains(m.getSignature())))
       {
           criteria5++;
           return false;
       }

       if (!satisfiesInvokeSpecialSafety(currmethod, m, st))
       {
           criteria6++;
           return false;
       }

       HashMap classesHT = new HashMap();

//         if (!satisfiesResolverCriteria(m, st, classesHT))
//         {
//             criteria7++;
//             return false;
//         }

       // we need a resolverclassesHT here.
       if (!satisfiesExceptionsAccess(currmethod, m, st, classesHT, null))
       {
           criteria7++;
           return false;
       }

       if ((currmethod.getSignature().equals(m.getSignature())))
       {
           criteria8++;
           return false;
       }
      return true;
   }



    boolean satisfiesInvokeSpecialSafety (SootMethod inlineIntoM, SootMethod inlineeM,
                                          InlinerState st) 
    {
      SootClass inlinableclass = inlineeM.getDeclaringClass();
      SootClass targetclass = inlineIntoM.getDeclaringClass();

      JimpleBody body = Jimplifier.getJimpleBody (inlineeM);
      Iterator unitIt = body.getUnits().iterator();

      while (unitIt.hasNext())
      {
          Stmt s = (Stmt) unitIt.next();
          List boxes = s.getUseAndDefBoxes();
          Iterator boxit = boxes.iterator();

          while (boxit.hasNext())
          {
              Value v = ((ValueBox)(boxit.next())).getValue();
              if (!(v instanceof SpecialInvokeExpr))
                  continue;

              SpecialInvokeExpr siInvExp = (SpecialInvokeExpr)v;
              SootMethod invokedM = siInvExp.getMethod();
              SootClass dec = st.scm.getClass (invokedM.getDeclaringClass().getName());
              if (!(invokedM.getName().equals("<init>")))
              {
                  if ((WholeProgramUtil.isStrictSuperClass(inlinableclass, dec)) || 
                      (WholeProgramUtil.isStrictSuperClass (targetclass, dec)))
                      return false;
                  if ((Modifier.isPrivate(invokedM.getModifiers())) && 
                      (!targetclass.getName().equals(inlinableclass.getName())))
                      return false;
              }
          }
      }

      return true;
   }

    private boolean addLocalTypesToClassMap(String currPkgName, 
                                            Iterator localsIt, 
                                            HashMap classesHT, 
                                            HashMap resolverclassesHT,
                                            InlinerState st)
    {
      while (localsIt.hasNext())
      {
          boolean haveRef = false;
          boolean samePackage = false;
          Local loc = (Local) localsIt.next();
          String locName = null;

          if (loc.getType() instanceof ArrayType)
          {
              Type t = ((ArrayType) loc.getType()).baseType;
              if (t instanceof RefType)
                  { haveRef = true; locName = t.toString(); }
          }
          else if (loc.getType() instanceof RefType)
              { haveRef = true; locName = loc.getType().toString(); }

          if (haveRef)
          {
              SootClass sc = st.scm.getClass(locName);
              samePackage = WholeProgramUtil.isSamePackage(WholeProgramUtil.getPackageName
                                           (locName), currPkgName);

              if (((Integer) classesHT.get (sc.getName())) == null)
                  classesHT.put (sc.getName(), 
                                 InlinerModifierTwiddler.getCorrectModifier 
                                           (sc.getModifiers()));

              if (!samePackage)
              {
                  classesHT.put (sc.getName(), InlinerModifierTwiddler.pub);
                  if (((Integer) resolverclassesHT.get (sc.getName())).intValue() < 3)
                      return false;
              }
          }
      }

      return true;
    }

    boolean satisfiesResolverCriteria (SootMethod method, 
                                       InlinerState st, HashMap classesHT, 
                                       HashMap resolverclassesHT) 
    {
      boolean sameclass = false;
      boolean sameprotected = false;
      SootClass sc = null;

      HashMap methodsHT = new HashMap();
      HashMap fieldsHT = new HashMap();

      HashMap resolverclassesHT = st.res.getClassesHT();
      HashMap resolvermethodsHT = st.res.getMethodsHT();
      HashMap resolverfieldsHT = st.res.getFieldsHT();

      SootClass currclass = currmethod.getDeclaringClass();
      String currname = currclass.getName();
      String currpackagename = WholeProgramUtil.getPackageName (currname);
      JimpleBody body = Jimplifier.getJimpleBody (method);

      addLocalTypesToClassMap(currpackagename, body.getLocals().iterator(), classesHT, 
                              resolverclassesHT, st);

      Iterator it = body.getUnits().iterator();
      int boxnum = 0;
      while (it.hasNext())
      {
            Stmt s = (Stmt) it.next();
            List boxes = s.getUseAndDefBoxes();
            Iterator boxit = boxes.iterator();
            while (boxit.hasNext())
            {
               sameclass = false;
               boolean samePackage = false;
               sameprotected = false;
               ValueBox vb = (ValueBox) boxit.next();
               Value v = vb.getValue();
               SootClass dec = null;
               SootField field = null;
               Value im = null;
               if (v instanceof InstanceFieldRef)
               {
                  InstanceFieldRef ifr = (InstanceFieldRef) v;
                  im = ifr.getBase();
                  String basetype = im.getType().toString();
                  field = ifr.getField();
                  dec = field.getDeclaringClass();
                  if (currclass.getName().equals (dec.getName()))
                  sameclass = true;
                  samePackage = isSamePackage (WholeProgramUtil.getPackageName (dec.getName()), currpackagename);
                  if (((Integer) classesHT.get (dec.getName())) == null)
                  classesHT.put (dec.getName(), getCorrectModifier (dec.getModifiers()));
                  if (! (samePackage))
                  {
                     classesHT.put (dec.getName(), pub);
                     if (((Integer) resolverclassesHT.get (dec.getName())).intValue() < 3)
                     return false;
                  }

                  if (im.getType() instanceof ArrayType)
                  sameprotected = currclass.getName().equals ("java.lang.Object");
                  else
                  sameprotected = isSameProtected (dec, currclass, basetype, currpackagename);
                  if ((((Integer) fieldsHT.get (field.getSignature())) == null))
                  fieldsHT.put (field.getSignature(), getCorrectModifier (field.getModifiers()));
                  if (! (sameprotected))
                  {
                     fieldsHT.put (field.getSignature(), InlinerModifierTwiddler.pub);
                     if (((Integer) resolverfieldsHT.get (field.getSignature())).intValue() < 3)
                     return false;
                  }

                  else if ((! (samePackage)) && sameprotected)
                  {
                     if (((Integer) fieldsHT.get (field.getSignature())).intValue() < 2)
                     fieldsHT.put (field.getSignature(), InlinerModifierTwiddler.prot);
                     if (((Integer) resolverfieldsHT.get (field.getSignature())).intValue() < 2)
                     return false;
                  }

                  else if ((! (sameclass)) && samePackage)
                  {
                     if (((Integer) fieldsHT.get (field.getSignature())).intValue() < 1)
                     fieldsHT.put (field.getSignature(), InlinerModifierTwiddler.def);
                     if (((Integer) resolverfieldsHT.get (field.getSignature())).intValue() < 1)
                     return false;
                  }

               }

               else if (v instanceof StaticFieldRef)
               {
                  StaticFieldRef sfr = (StaticFieldRef) v;
                  field = sfr.getField();
                  dec = field.getDeclaringClass();
                  if (currclass.getName().equals (dec.getName()))
                  sameclass = true;
                  samePackage = isSamePackage (WholeProgramUtil.getPackageName(dec.getName()), currpackagename);
                  if (((Integer) classesHT.get (dec.getName())) == null)
                  classesHT.put (dec.getName(), getCorrectModifier (dec.getModifiers()));
                  if (! (samePackage))
                  {
                     classesHT.put(dec.getName(), pub);
                     if (((Integer) resolverclassesHT.get (dec.getName())).intValue() < 3)
                     return false;
                  }

                  sameprotected = isSameStaticProtected (dec, currclass, currpackagename);
                  if ((((Integer) fieldsHT.get (field.getSignature())) == null))
                  fieldsHT.put (field.getSignature(), getCorrectModifier (field.getModifiers()));
                  if (! (sameprotected))
                  {
                     fieldsHT.put (field.getSignature(), InlinerModifierTwiddler.pub);
                     if (((Integer) resolverfieldsHT.get (field.getSignature())).intValue() < 3)
                     return false;
                  }

                  else if ((! (samePackage)) && sameprotected)
                  {
                     if (((Integer) fieldsHT.get (field.getSignature())).intValue() < 2)
                     fieldsHT.put (field.getSignature(), InlinerModifierTwiddler.prot);
                     if (((Integer) resolverfieldsHT.get (field.getSignature())).intValue() < 2)
                     return false;
                  }

                  else if ((! (sameclass)) && samePackage)
                  {
                     if (((Integer) fieldsHT.get (field.getSignature())).intValue() < 1)
                     fieldsHT.put (field.getSignature(), InlinerModifierTwiddler.def);
                     if (((Integer) resolverfieldsHT.get (field.getSignature())).intValue() < 1)
                     return false;
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
                     samePackage = isSamePackage (WholeProgramUtil.getPackageName (locName), currpackagename);
                  }

                  else if (t instanceof ArrayType)
                  {
                     Type ty = ((ArrayType) t).baseType;
                     if (ty instanceof RefType)
                     {
                        haveRef = true;
                        String locName = ty.toString();
                        sc = scm.getClass (locName);
                        samePackage = isSamePackage (WholeProgramUtil.getPackageName (locName), currpackagename);
                     }

                  }

                  if (haveRef)
                  {
                     if (((Integer) classesHT.get (sc.getName())) == null)
                     classesHT.put (sc.getName(), getCorrectModifier (sc.getModifiers()));
                     if (! (samePackage))
                     {
                        classesHT.put (sc.getName(), pub);
                        if (((Integer) resolverclassesHT.get (sc.getName())).intValue() < 3)
                        return false;
                     }

                  }

               }

               else if (v instanceof InstanceOfExpr)
               {
                  haveRef = false;
                  InstanceOfExpr ie = (InstanceOfExpr) v;
                  Type t = ie.getCheckType();
                  if (t instanceof RefType)
                  {
                     haveRef = true;
                     String locName = t.toString();
                     sc = scm.getClass(locName);
                     samePackage = isSamePackage (WholeProgramUtil.getPackageName (locName), currpackagename);
                  }

                  else if (t instanceof ArrayType)
                  {
                     Type ty = ((ArrayType) t).baseType;
                     if (ty instanceof RefType)
                     {
                        haveRef = true;
                        String locName = ty.toString();
                        sc = scm.getClass(locName);
                        samePackage = isSamePackage (WholeProgramUtil.getPackageName (locName), currpackagename);
                     }

                  }

                  if (haveRef)
                  {
                     if (((Integer) classesHT.get (sc.getName())) == null)
                     classesHT.put (sc.getName(), getCorrectModifier (sc.getModifiers()));
                     if (! (samePackage))
                     {
                        classesHT.put (sc.getName(), pub);
                        if (((Integer) resolverclassesHT.get (sc.getName())).intValue() < 3)
                        return false;
                     }

                  }

               }

               else if (v instanceof NewExpr)
               {
                  NewExpr newexpr = (NewExpr) v;
                  RefType t = newexpr.getBaseType();
                  String locName = t.toString();
                  sc = scm.getClass (locName);
                  samePackage = isSamePackage (WholeProgramUtil.getPackageName (locName), currpackagename);
                  if (((Integer) classesHT.get (sc.getName())) == null)
                  classesHT.put (sc.getName(), getCorrectModifier (sc.getModifiers()));
                  if (! (samePackage))
                  {
                     classesHT.put (sc.getName(), pub);
                     if (((Integer) resolverclassesHT.get (sc.getName())).intValue() < 3)
                     return false;
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
                     samePackage = isSamePackage (WholeProgramUtil.getPackageName (locName), currpackagename);
                  }

                  else if (t instanceof ArrayType)
                  {
                     Type ty = ((ArrayType) t).baseType;
                     if (ty instanceof RefType)
                     {
                        haveRef = true;
                        String locName = ty.toString();
                        sc = scm.getClass(locName);
                        samePackage = isSamePackage (WholeProgramUtil.getPackageName (locName), currpackagename);
                     }

                  }

                  if (haveRef)
                  {
                     if (((Integer) classesHT.get (sc.getName())) == null)
                     classesHT.put (sc.getName(), getCorrectModifier (sc.getModifiers()));
                     if (! (samePackage))
                     {
                        classesHT.put (sc.getName(), pub);
                        if (((Integer) resolverclassesHT.get (sc.getName())).intValue() < 3)
                        return false;
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
                     samePackage = isSamePackage (WholeProgramUtil.getPackageName (locName), currpackagename);
                  }

                  else if (t instanceof ArrayType)
                  {
                     Type ty = ((ArrayType) t).baseType;
                     if (ty instanceof RefType)
                     {
                        haveRef = true;
                        String locName = ty.toString();
                        sc = scm.getClass (locName);
                        samePackage = isSamePackage (WholeProgramUtil.getPackageName (locName), currpackagename);
                     }

                  }

                  if (haveRef)
                  {
                     if (((Integer) classesHT.get (sc.getName())) == null)
                     classesHT.put (sc.getName(), getCorrectModifier (sc.getModifiers()));
                     if (! (samePackage))
                     {
                        classesHT.put (sc.getName(), pub);
                        if (((Integer) resolverclassesHT.get (sc.getName())).intValue() < 3)
                        return false;
                     }

                  }

               }

               else if (v instanceof StaticInvokeExpr)
               {
                  StaticInvokeExpr stinvexpr = (StaticInvokeExpr) v;
                  int argcount = stinvexpr.getArgCount();
                  int counter = 0;
                  while (counter < argcount)
                  {
                     samePackage = false;
                     haveRef = false;
                     if (stinvexpr.getMethod().getParameterType(counter) instanceof RefType)
                     {
                        haveRef = true;
                        String argtype = stinvexpr.getMethod().getParameterType(counter).toString();
                        sc = scm.getClass (argtype);
                        samePackage = isSamePackage (WholeProgramUtil.getPackageName (argtype), currpackagename);
                     }

                     else if (stinvexpr.getMethod().getParameterType(counter) instanceof ArrayType)
                     {
                        Type t = ((ArrayType) stinvexpr.getMethod().getParameterType(counter)).baseType;
                        if (t instanceof RefType)
                        {
                           haveRef = true;
                           String argtype = t.toString();
                           sc = scm.getClass (argtype);
                           samePackage = isSamePackage (WholeProgramUtil.getPackageName (argtype), currpackagename);
                        }

                     }

                     if (haveRef)
                     {
                        if (((Integer) classesHT.get (sc.getName())) == null)
                        classesHT.put (sc.getName(), getCorrectModifier (sc.getModifiers()));
                        if (! (samePackage))
                        {
                           classesHT.put (sc.getName(), pub);
                           if (((Integer) resolverclassesHT.get (sc.getName())).intValue() < 3)
                           return false;
                        }

                     }

                     counter++;
                  }

                  samePackage = false;
                  SootMethod meth = stinvexpr.getMethod();
                  dec = meth.getDeclaringClass();
                  if (currclass.getName().equals (dec.getName()))
                  sameclass = true;
                  samePackage = isSamePackage (WholeProgramUtil.getPackageName (dec.getName()), currpackagename);
                  if (((Integer) methodsHT.get (meth.getSignature())) == null)
                  methodsHT.put (meth.getSignature(), getCorrectModifier (meth.getModifiers()));
                  if (((Integer) classesHT.get (dec.getName())) == null)
                  classesHT.put (dec.getName(), getCorrectModifier (dec.getModifiers()));
                  if (! (samePackage))
                  {
                     classesHT.put (dec.getName(), pub);
                     if (((Integer) resolverclassesHT.get (dec.getName())).intValue() < 3)
                     return false;
                  }

                  sameprotected = isSameStaticProtected (dec, currclass, currpackagename);
                  if (! (sameprotected))
                  {
                     methodsHT.put (meth.getSignature(), pub);
                     if (((Integer) resolvermethodsHT.get (meth.getSignature())).intValue() < 3)
                     return false;
                  }

                  else if ((! (samePackage)) && sameprotected)
                  {
                     if (((Integer) methodsHT.get (meth.getSignature())).intValue() < 2)
                     methodsHT.put (meth.getSignature(), prot);
                     if (((Integer) resolvermethodsHT.get (meth.getSignature())).intValue() < 2)
                     return false;
                  }

                  else if ((! (sameclass)) && samePackage)
                  {
                     if (((Integer) methodsHT.get (meth.getSignature())).intValue() < 1)
                     methodsHT.put (meth.getSignature(), def);
                     if (((Integer) resolvermethodsHT.get (meth.getSignature())).intValue() < 1)
                     return false;
                  }

               }

               else if (v instanceof InvokeExpr)
               {
                  InvokeExpr invexpr = (InvokeExpr) v;
                  int argcount = invexpr.getArgCount();
                  int counter = 0;
                  while (counter < argcount)
                  {
                     haveRef = false;
                     samePackage = false;
                     if (invexpr.getMethod().getParameterType(counter) instanceof RefType)
                     {
                        haveRef = true;
                        String argtype = invexpr.getMethod().getParameterType(counter).toString();
                        sc = scm.getClass (argtype);
                        samePackage = isSamePackage (WholeProgramUtil.getPackageName (argtype), currpackagename);
                     }

                     else if (invexpr.getMethod().getParameterType(counter) instanceof ArrayType)
                     {
                        Type t = ((ArrayType) invexpr.getMethod().getParameterType(counter)).baseType;
                        if (t instanceof RefType)
                        {
                           haveRef = true;
                           String argtype = t.toString();
                           sc = scm.getClass (argtype);
                           samePackage = isSamePackage (WholeProgramUtil.getPackageName (argtype), currpackagename);
                        }

                     }

                     if (haveRef)
                     {
                        if (((Integer) classesHT.get (sc.getName())) == null)
                        classesHT.put (sc.getName(), getCorrectModifier(sc.getModifiers()));
                        if (! (samePackage))
                        {
                           classesHT.put (sc.getName(), pub);
                           if (((Integer) resolverclassesHT.get (sc.getName())).intValue() < 3)
                           return false;
                        }

                     }

                     counter++;
                  }

                  samePackage = false;
                  if (invexpr instanceof SpecialInvokeExpr)
                  im = ((SpecialInvokeExpr) invexpr).getBase();
                  else if (invexpr instanceof VirtualInvokeExpr)
                  im = ((VirtualInvokeExpr) invexpr).getBase();
                  else if (invexpr instanceof InterfaceInvokeExpr)
                  im = ((InterfaceInvokeExpr) invexpr).getBase();
                  String basetype = im.getType().toString();
                  SootMethod meth = invexpr.getMethod();
                  dec = meth.getDeclaringClass();
                  if (currclass.getName().equals (dec.getName()))
                  sameclass = true;
                  samePackage = isSamePackage (WholeProgramUtil.getPackageName (dec.getName()), currpackagename);
                  if (((Integer) methodsHT.get (meth.getSignature())) == null)
                  methodsHT.put (meth.getSignature(), getCorrectModifier (meth.getModifiers()));
                  if (((Integer) classesHT.get (dec.getName())) == null)
                  classesHT.put (dec.getName(), getCorrectModifier (dec.getModifiers()));
                  if (! (samePackage))
                  {
                     classesHT.put (dec.getName(), InlinerModifierTwiddler.pub);
                     if (((Integer) resolverclassesHT.get (dec.getName())).intValue() < 3)
                     return false;
                  }

                  if (im.getType() instanceof ArrayType)
                  sameprotected = currclass.getName().equals ("java.lang.Object");
                  else
                  sameprotected = isSameProtected (dec, currclass, basetype, currpackagename);
                  if (! (sameprotected))
                  {
                     methodsHT.put (meth.getSignature(), InlinerModifierTwiddler.pub);
                     if (((Integer) resolvermethodsHT.get (meth.getSignature())).intValue() < 3)
                         return false;
                  }

                  else if ((! (samePackage)) && sameprotected)
                  {
                     if (((Integer) methodsHT.get (meth.getSignature())).intValue() < 2)
                     methodsHT.put (meth.getSignature(), InlinerModifierTwiddler.prot);
                     if (((Integer) resolvermethodsHT.get (meth.getSignature())).intValue() < 2)
                         return false;
                  }

                  else if ((! (sameclass)) && samePackage)
                  {
                     if (((Integer) methodsHT.get (meth.getSignature())).intValue() < 1)
                     methodsHT.put (meth.getSignature(), InlinerModifierTwiddler.def);
                     if (((Integer) resolvermethodsHT.get (meth.getSignature())).intValue() < 1)
                         return false;
                  }

               }

            }

         }

      return InlinerModifierTwiddler.changeModifiersOfAccessesFrom (method, classesHT, methodsHT, fieldsHT);
   }

   boolean satisfiesExceptionsAccess (SootMethod currmethod, SootMethod m, InlinerState st,
                                      HashMap classesHT, HashMap resolverclassesHT) 
   {
      Iterator excit = m.getExceptions().iterator();
      String currpackname = WholeProgramUtil.getPackageName 
          (currmethod.getDeclaringClass().getName());

      while (excit.hasNext())
      {
         boolean samepack = false;

         SootClass nextException = (SootClass) excit.next();
         nextException = st.scm.getClass(nextException.getName());

         samepack = WholeProgramUtil.isSamePackage 
             (WholeProgramUtil.getPackageName(nextException.getName()), 
              currpackname);

         if (((Integer) classesHT.get (nextException.getName())) == null)
             classesHT.put (nextException.getName(), 
                            InlinerModifierTwiddler.getCorrectModifier(nextException.getModifiers()));

         if (!(samepack))
         {
            classesHT.put(nextException.getName(), InlinerModifierTwiddler.pub);
            if (((Integer) resolverclassesHT.get(nextException.getName())).intValue() < 3)
                return false;
         }

      }

      return true;
   }
}
