// package ca.mcgill.sable.soot.virtualCalls;

package ca.mcgill.sable.soot.jimple.toolkit.invoke;

import java.io.*;
import ca.mcgill.sable.soot.jimple.*;
import ca.mcgill.sable.soot.grimp.*;
import ca.mcgill.sable.util.*;
import java.util.*;
import ca.mcgill.sable.soot.baf.*;
import ca.mcgill.sable.soot.jimple.toolkit.scalar.*;
import ca.mcgill.sable.soot.*;

class Inliner 
{
    static int averageSize = 0;
    static Set classesToAnalyze;
    static boolean NOLIB = false;
//     private InvokeExpr currInvokeExpr;
//     private JimpleBody body;
//     private JimpleBody mebody;
//     private Map localsHT = new HashMap();
//     private SootMethod currmethod;
//     private BinopExpr correctbinopexpr;
//     private UnopExpr correctunopexpr;
//     private InvokeExpr correctinvokeexpr;
//     private Stmt correctstmt;
//     private Value op1;
//     private Value op2;
//     private Value op;
//     private Value returnvariable;
//     private List args;
//     private int numstmts;
//     private Stmt returnstmt;
//     private Local dummyreturn;
//     private int inlinenumstmts;
//     private int targetnumstmts;
//     private Constant correctconstant;
//     private Type correcttype;
//     private Jimple jimple = Jimple.v();
//     private Scene scm;
//     private ClassGraphBuilder clgb;
//     private CallGraphBuilder cagb;
//     private Resolver resolver;
//     private Set changedclasses = new HashSet();
//     private Set incorrectlyjimplified;
//     private Type returnvartype;
//     private Set removedmethods = new HashSet();
//     private int j = 0;
//     private int returnj = 0;
//     private int nullcheckj = 0;
//     private int addressj = 0;
//     private int throwj = 0;
//     Inliner () {}


//     Inliner ( CallGraphBuilder callgb ) {
//        cagb = callgb;
//     }


//     private HashMap methodsToMinDepth = new HashMap();
//     private List classesToProcess = null;
//     private boolean includeLibraries = false;
//     void setClassesToProcess( List toProcess, boolean includeLibraries )
//     {
//        this.classesToProcess = toProcess;
//        this.includeLibraries = includeLibraries;
//     }


//     List setMethodDepths ( Collection methods ) {
//        List methodsQ = new ArrayList();
//        List reachedmethods = new ArrayList();
//        Iterator methodsit = methods.iterator();
//        Integer zero = new Integer(0);
//        while ( methodsit.hasNext() )
//        {
//           try {
//              MethodNode mn = ( MethodNode ) methodsit.next();
//              if ( ( ( mn.incomingedges == 0 ) || clgb.sources.contains ( mn.getMethod().getSignature() ) ) && ( ! mn.isRedundant ) )
//              {
//                 // reachedmethods.add ( mn );

//                 methodsToMinDepth.put ( mn.getMethod().getSignature(), zero );
//                 try {
//                    Set adjacentnodes = mn.getAllPossibleMethods();
//                    if ( adjacentnodes.size() > 0 )
//                    {
//                       Iterator adjacentit = adjacentnodes.iterator();
//                       while ( adjacentit.hasNext() )
//                       {
//                          MethodNode adjmn = ( MethodNode ) adjacentit.next();
//                          if ( ( ( ( Integer ) methodsToMinDepth.get ( adjmn.getMethod().getSignature() ) ) == null ) )
//                          {
//                             if ( ! methodsQ.contains ( adjmn ) )
//                             {
//                                if ( ( ! ( ( adjmn.incomingedges == 0 ) || clgb.sources.contains ( adjmn.getMethod().getSignature() ) ) ) && ( ! adjmn.isRedundant ) )
//                                methodsQ.add ( adjmn );
//                             }

//                          }

//                       }

//                    }

//                 }
//                 catch ( java.lang.RuntimeException e ) {}

//              }

//           }
//           catch ( java.lang.RuntimeException e ) {}

//        }

//        int nextlevelindex = methodsQ.size();
//        int currentlevel = 1;
//        Integer currentLevel = new Integer ( currentlevel );
//        while ( ! methodsQ.isEmpty() )
//        {
//           try {
//              MethodNode nextmethod = ( MethodNode ) methodsQ.get( 0 );
//              // reachedmethods.add ( nextmethod );

//              methodsToMinDepth.put ( nextmethod.getMethod().getSignature(), currentLevel );
//              try {
//                 Set adjacentnodes = nextmethod.getAllPossibleMethods();
//                 if ( adjacentnodes.size() > 0 )
//                 {
//                    Iterator adjacentit = adjacentnodes.iterator();
//                    while ( adjacentit.hasNext() )
//                    {
//                       MethodNode adjnode = ( MethodNode ) adjacentit.next();
//                       if ( ( ( ( Integer ) methodsToMinDepth.get ( adjnode.getMethod().getSignature() ) ) == null ) )
//                       {
//                          if ( ! methodsQ.contains ( adjnode ) )
//                          {
//                             methodsQ.add ( adjnode );
//                          }

//                       }

//                    }

//                 }

//              }
//              catch ( java.lang.RuntimeException e ) {}

//           }
//           catch ( java.lang.RuntimeException e ) {}

//           methodsQ.remove ( 0 );
//           nextlevelindex--;
//           if ( nextlevelindex == 0 )
//           {
//              nextlevelindex = methodsQ.size();
//              currentlevel++;
//              currentLevel = new Integer ( currentlevel );
//           }

//        }
//        // WHILE

//        Iterator methit = methods.iterator();
//        while ( methit.hasNext() )
//        {
//           MethodNode methn = ( MethodNode ) methit.next();
//           if ( ( ( Integer ) methodsToMinDepth.get ( methn.getMethod().getSignature() ) ) != null )
//           reachedmethods.add ( methn );
//        }

//        return reachedmethods;
//     }


//     List sortByMethodDepths ( List methodslist ) {
//        // List sortedmethods = new ArrayList();

//        List sortedmethods = new LinkedList();
//        if ( methodslist.size() > 0 )
//        {
//           boolean searchforfirst = true;
//           Iterator it = methodslist.iterator();
//           while ( ( it.hasNext() ) && searchforfirst )
//           {
//              MethodNode firstmn = ( MethodNode ) it.next();
//              if ( ! firstmn.isRedundant )
//              {
//                 sortedmethods.add ( firstmn );
//                 searchforfirst = false;
//              }

//           }

//           while ( it.hasNext() )
//           {
//              MethodNode mn = ( MethodNode ) it.next();
//              if ( ! mn.isRedundant )
//              {
//                 int depth = ( ( Integer ) methodsToMinDepth.get ( mn.getMethod().getSignature() ) ).intValue();
//                 ListIterator sortedmethodsit = sortedmethods.listIterator(0);
//                 int sortedmethnum = 0;
//                 boolean inserted = false;
//                 while ( ( sortedmethodsit.hasNext() ) && ( inserted == false ) )
//                 {
//                    MethodNode nextmethod = ( MethodNode ) sortedmethodsit.next();
//                    int nextdepth = ( ( Integer ) methodsToMinDepth.get ( nextmethod.getMethod().getSignature() ) ).intValue();
//                    if ( depth > nextdepth )
//                    // if ( depth < nextdepth )
//                    {
//                       inserted = true;
//                       sortedmethodsit.add ( /* sortedmethods.indexOf( nextmethod) sortedmethnum, */ mn );
//                    }

//                    sortedmethnum++;
//                 }

//                 if ( inserted == false )
//                 {
//                    sortedmethodsit.add ( /* sortedmethnum, */ mn );
//                 }

//              }

//           }

//        }

//        return sortedmethods;
//     }


//     List returnImportantMethods ( Collection methods ) {
//        List importantmethods = new ArrayList();
//        Iterator it1 = methods.iterator();
//        int i = 2;
//        int startindex = 0;
//        while ( it1.hasNext() )
//        {
//           MethodNode nextmn = ( MethodNode ) it1.next();
//           int nextdepth = ( ( Integer ) methodsToMinDepth.get ( nextmn.getMethod().getSignature() ) ).intValue();
//           if ( nextdepth < i )
//           {
//              importantmethods.add ( startindex, nextmn );
//           }

//           else
//           {
//              startindex = importantmethods.size();
//              i = i + 2;
//              importantmethods.add ( startindex, nextmn );
//           }

//        }

//        return importantmethods;
//     }


//     private int numBenchmarkNodes = 0;
//     List sortMethods ( Collection methods ) {
//        List sortedmethods = new /* Array */ LinkedList();
//        Object[] methodsarray = methods.toArray();
//        Arrays.sort ( methodsarray, new StringComparator() );
//        for (int i=0;i<methodsarray.length;i++)
//        sortedmethods.add ( (MethodNode) methodsarray[i] );
//        return sortedmethods;
//     }


//     List sortMethodNodes ( Collection methods ) {
//        List sortedmethods = new /* Array */ LinkedList();
//        if ( methods.size() > 0 )
//        {
//           Iterator it = methods.iterator();
//           MethodNode firstmn = ( MethodNode ) it.next();
//           if ( ! isLibraryNode ( firstmn.getMethod().getDeclaringClass().getName() ) )
//           numBenchmarkNodes++;
//           sortedmethods.add ( firstmn );
//           while ( it.hasNext() )
//           {
//              MethodNode mn = ( MethodNode ) it.next();
//              if ( ! isLibraryNode ( mn.getMethod().getDeclaringClass().getName() ) )
//              numBenchmarkNodes++;
//              ListIterator sortedmethodsit = sortedmethods.listIterator(0);
//              int sortedmethnum = 0;
//              boolean inserted = false;
//              while ( ( sortedmethodsit.hasNext() ) && ( inserted == false ) )
//              {
//                 MethodNode nextmethod = ( MethodNode ) sortedmethodsit.next();
//                 if ( nextmethod.getMethod().getSignature().compareTo ( mn.getMethod().getSignature() ) < 0 )
//                 {
//                    inserted = true;
//                    sortedmethodsit.add ( /* sortedmethnum , */ mn );
//                 }

//                 sortedmethnum++;
//              }

//              if ( inserted == false )
//              {
//                 sortedmethodsit.add ( /* sortedmethnum , */ mn );
//              }

//           }

//        }

//        return sortedmethods;
//     }


//     private HashMap invokeExprsToMethods = new HashMap();
//     private MethodNode inliningInsideMethod;
//     private ArrayList workQ;
//     private int numpotentiallyinlined = 0, numActuallySeen = 0, allowedtochange = 0,
//     initnum = 0;
//      private int inlinemono = 0, actuallyinlined = 0;
//     static boolean NOLIB = true;

//     void examineCallSite (InvokeExpr inlinableinvoke) 
//     {
//        final InlinerState st = new InlinerState();

//        numpotentiallyinlined++;
//        localsHT.clear();
//        int stmtsAtSite = 0;

//        workQ = new ArrayList();
//        workQ.add (inlinableinvoke);

//        while (!workQ.isEmpty())
//        {
//           numActuallySeen++;
//           localsHT.clear();
//           InvokeExpr ie = (InvokeExpr) workQ.remove(0);
//           currInvokeExpr = ie;

//           MethodNode mn = (MethodNode) invokeExprsToMethods.get (ie);
//           inliningInsideMethod = mn;

//           if (mn != null)
//           {
//              SootMethod meth = mn.getMethod();
//              if ((!meth.getName().equals("<clinit>")) && 
//                  (!Modifier.isNative(meth.getModifiers())))
//              {
//                 if (allowedToChange(meth.getDeclaringClass().getName()))
//                 {
//                    allowedtochange++;
//                    currmethod = meth;
//                    Body body = Jimplifier.getJimpleBody(meth);
//                    int origSize = ((Integer)origSizeHT.get
//                                    (meth.getSignature())).intValue();

//                    Iterator localsIt = body.getLocals().iterator();
//                    while (localsIt.hasNext())
//                    {
//                        Local newLocal = (Local)localsIt.next();
//                        localsHT.put (newLocal.getName(), newLocal);
//                    }

//                    Iterator stmtIter = body.getUnits().iterator();

//                    Stmt lastSeenStmt = null;

//                    if ((stmtsAtSite < 20) && 
//                        ((body.getUnits().size() < (8*origSize)) &&
//                         (body.getUnits().size() < 10000)))
//                       {
//                          numstmts = 0;

//                          boolean looking = true;

//                          while ((stmtIter.hasNext()) && looking)
//                          {
//                                Stmt s = (Stmt) stmtIter.next();
//                                lastSeenStmt = s;
                              
//                                st.assignFlag = false;
//                                st.invokeFlag = false;
//                                if (s instanceof InvokeStmt)
//                                {
//                                    st.invokeFlag = true;
//                                    if (currInvokeExpr.equals 
//                                        (((InvokeStmt)s).getInvokeExpr()))
//                                        looking = false;
//                                }

//                                if (s instanceof AssignStmt &&
//                                    ((AssignStmt)s).getRightOp() instanceof 
//                                                      InvokeExpr)
//                                {
//                                    st.assignFlag = true;
//                                    returnvariable = ((AssignStmt)s).getLeftOp();
//                                    if (currInvokeExpr.equals
//                                        (((AssignStmt)s).getRightOp()))
//                                        looking = false;
//                                }
//                          } // WHILE LOOKING

//                          if (!looking)
//                          {
//                             st.interfaceInvoked = false;
//                             st.specialInvoked = false;
//                             st.staticInvoked = false;
//                             st.virtualInvoked = false;

//                             currInvokeExpr.apply(new AbstractJimpleValueSwitch()
//                             {
//                                 public void caseInterfaceInvokeExpr
//                                     (InterfaceInvokeExpr v) 
//                                     { st.interfaceInvoked = true; }

//                                 public void caseSpecialInvokeExpr
//                                     (SpecialInvokeExpr v) 
//                                     { st.specialInvoked = true; }

//                                public void caseStaticInvokeExpr
//                                    (StaticInvokeExpr v) 
//                                    { st.staticInvoked = true; }

//                                public void caseVirtualInvokeExpr
//                                    (VirtualInvokeExpr v)
//                                    { st.virtualInvoked = true; }
//                             });

//                             CallSite cs = getCorrectCallSite(currInvokeExpr);
//                             if ((cs.getMethods().size() == 1) 
//                                 && (st.invokeFlag || st.assignFlag))
//                             {
//                                inlinemono++;
//                                st.syncFlag = false;
//                                MethodNode men = (MethodNode) 
//                                    cs.getMethods().iterator().next();
//                                SootMethod me = men.getMethod();

//                                if ((Modifier.isSynchronized(me.getModifiers()))
//                                              && !st.staticInvoked)
//                                    st.syncFlag = true;

//                                if (!(me.getName().equals("<init>")))
//                                {
//                                    if (InliningCriteria.satisfiesCriteria
//                                        (currmethod, me, currInvokeExpr, st))
//                                    {
//                                        changedclasses.add
//                                            (scm.getClass
//                                             (meth.getDeclaringClass().
//                                              getName()));
//                                        men.alreadyInlined = true;
//                                        stmtsAtSite = stmtsAtSite + me.getUnits().size();
//                                        prepareForInlining
//                                            (meth, me, lastSeenStmt, st);
//                                    }
//                                    else 
//                                        initnum++;
//                                }
//                             }
//                       }
//                       else st.crit.criteria0++;
//                    }
//                 }
//              }
//           }
//        }
//     }

//      private void prepareForInlining(SootMethod inlineIntoM, 
//                                      SootMethod inlineeM, 
//                                      Unit lastSeenStmt,
//                                      InlinerState st)
//      {
//          Body inlineIntoB = Jimplifier.getJimpleBody(inlineIntoM);
//          Body inlineeB = Jimplifier.getJimpleBody(inlineeM);

//          Iterator localsIt = inlineeB.getLocals().iterator();
            
//          while (localsIt.hasNext())
//          {
//              Local newlocal = (Local)localsIt.next();
//              Local clonedlocal = (Local)newlocal.clone();

//              clonedlocal.setName(new String("dummy"+j));
//              localsHT.put(newlocal.getName(), clonedlocal);
//              inlineIntoB.getLocals().add(clonedlocal);
//              j++;
//          }

//          if (st.assignFlag)
//          {
//              returnvartype = inlineeM.getReturnType();
//              if (returnvartype instanceof BooleanType ||
//                      returnvartype instanceof ByteType ||
//                      returnvartype instanceof CharType ||
//                      returnvartype instanceof ShortType)
//                  returnvartype = IntType.v();

//              dummyreturn = jimple.newLocal
//                  (new String("dummyreturn" + returnj), returnvartype);

//              localsHT.put(dummyreturn.getName(), dummyreturn);
//              inlineIntoB.getLocals().add(dummyreturn);
//              returnj++;
//          }

//          if (!st.staticInvoked)
//          {
//              Local dummyNullCheck = 
//                  jimple.newLocal(new String("dummynull"+nullcheckj), 
//                                  RefType.v("java.lang.NullPointerException"));

//              localsHT.put (dummyNullCheck.getName(), dummyNullCheck);
//              body.getLocals().add(dummyNullCheck);
//              nullcheckj++;

//              Value baseVal = ((NonStaticInvokeExpr)currInvokeExpr).getBase();
//              insertNullCheck
//                  (baseVal, dummyNullCheck, body, lastSeenStmt);
//          }

//          if (st.syncFlag)
//          {
//              syncthrow = jimple.newLocal(new String("dummythrow"+throwj), 
//                                          RefType.v("java.lang.Throwable"));
//              localsHT.put(syncthrow.getName(), syncthrow);
//              body.getLocals().add(syncthrow);
//              throwj++;
//          }

//          actuallyinlined++;

//          InlineMethod (inlineIntoB.getUnits(), inlineeB.getUnits(), stmtIter);
//          JumpOptimizer.optimizeJumps(body);
//      }

//     private HashMap invokeExprsHT = new HashMap();
//     CallSite getCorrectCallSite (InvokeExpr inve) 
//     {
//        return (CallSite) invokeExprsHT.get (inve);
//     }


//     private ArrayList ImportantQ = new ArrayList();
//     private ArrayList ImportantCS = new ArrayList();
//     private ArrayList UnimportantQ = new ArrayList();
//     private ArrayList ImprovedCallSites = new ArrayList();
//     private HashMap origSizeHT = new HashMap();

//     void setImprovedCallSites ( List improved ) 
//     {
//        ImprovedCallSites = ( ArrayList ) improved;
//     }


//     int averageSize = 0;
//     Set examineMethodsToFixCallSites ( Collection callgraph, Resolver res ) {
//        resolver = res;
//        scm = resolver.getManager();
//        clgb = resolver.getClassGraphBuilder();
//        incorrectlyjimplified = clgb.getIncorrectlyJimplifiedClasses();
//        Iterator incorrectlyjimplifiedit = incorrectlyjimplified.iterator();
//        while ( incorrectlyjimplifiedit.hasNext() )
//        {
//           System.out.println ( ( String ) incorrectlyjimplifiedit.next() );
//        }

//        System.out.println();
//        System.out.print("Identifying important call sites for inlining.....");
//        List sortedbydepths = null;
//        if ( NOLIB )
//        {
//           List newcallgraph = new ArrayList();
//           Iterator callit = callgraph.iterator();
//           while ( callit.hasNext() )
//           {
//              MethodNode tempMN = (MethodNode) callit.next();
//              String tempName = tempMN.getMethod().getDeclaringClass().getName();
//              if ( ! ( tempName.startsWith ( "java." ) || tempName.startsWith ("sun.") || tempName.startsWith("sunw.") || tempName.startsWith("javax.") || tempName.startsWith("org.") || tempName.startsWith("com.") ) )
//              {
//                 if ( ! ( classesToAnalyze == null ) )
//                 {
//                    if ( classesToAnalyze.contains(tempMN.getMethod().getDeclaringClass().getName()))
//                    newcallgraph.add(tempMN);
//                 }

//                 else
//                 newcallgraph.add(tempMN);
//              }

//           }

//           sortedbydepths = sortMethods ( newcallgraph );
//        }

//        else
//        {
//           List newcallgraph = new ArrayList();
//           Iterator callit = callgraph.iterator();
//           while ( callit.hasNext() )
//           {
//              MethodNode tempMN = (MethodNode) callit.next();
//              if ( ! ( classesToAnalyze == null ) )
//              {
//                 if ( classesToAnalyze.contains(tempMN.getMethod().getDeclaringClass().getName()))
//                 newcallgraph.add(tempMN);
//              }

//              else
//              newcallgraph.add(tempMN);
//           }

//           sortedbydepths = sortMethods ( newcallgraph );
//        }

//        // System.out.println ( "Sorting done");

//        // List reachedcallgraph = setMethodDepths ( sortedcallgraph );

//        // List sortedbydepths = sortByMethodDepths ( reachedcallgraph );

//        LoopDetector loopd = new LoopDetector();
//        Iterator numloopsit = sortedbydepths.iterator();
//        while ( numloopsit.hasNext() )
//        {
//           MethodNode loopsMN = ( MethodNode ) numloopsit.next();
//           //   System.out.println ( "LOOPING "+loopsMN.getMethod().getSignature() );

//           loopd.setLoopCountFor ( loopsMN );
//        }

//        // System.out.println ( "LOOPS SIZE = "+sortedbydepths.size() );

//        int totalSize = 0;
//        Iterator finalit = sortedbydepths.iterator();
//        while ( finalit.hasNext() )
//        {
//           MethodNode nextmn = ( MethodNode ) finalit.next();
//           int origSize = Jimplifier.getJimpleBody( nextmn.getMethod() ).getUnits().size();
//           totalSize = totalSize + origSize;
//           origSizeHT.put ( nextmn.getMethod().getSignature(), new Integer ( origSize ) );
//           boolean recursiveflag = false;
//           if ( cagb.recursiveMethods.contains ( nextmn.getMethod().getSignature() ) )
//           recursiveflag = true;
//           Iterator invokeExprsiter = cagb.getInvokeExprs( nextmn.getMethod() ).iterator();
//           while ( invokeExprsiter.hasNext() )
//           {
//              InvokeExpr nextie = (InvokeExpr) invokeExprsiter.next();
//              // CallSite nextcs = ( CallSite ) callsitesiter.next();

//              CallSite nextcs = nextmn.getCallSite(nextie);
//              invokeExprsToMethods.put ( nextcs.getInvokeExpr(), nextmn );
//              invokeExprsHT.put ( nextcs.getInvokeExpr(), nextcs );
//              if ( recursiveflag )
//              {
//                 // System.out.println ( "IMPORTANT CALLSITE "+nextcs.getCallerID() );

//                 ImportantQ.add ( nextcs.getInvokeExpr() );
//                 ImportantCS.add ( nextcs );
//              }

//              else
//              {
//                 if ( nextmn.ImportantInvokeExprs.contains ( nextcs.getInvokeExpr() ) )
//                 {
//                    // System.out.println ( "IMPORTANT CALLSITE "+nextcs.getCallerID() );

//                    ImportantQ.add ( nextcs.getInvokeExpr() );
//                    ImportantCS.add ( nextcs );
//                 }

//                 else
//                 UnimportantQ.add ( nextcs.getInvokeExpr() );
//              }

//           }

//        }

//        averageSize = totalSize / sortedbydepths.size();
//        ArrayList ImportantMethods = new ArrayList();
//        Iterator importit = ImportantCS.iterator();
//        while ( importit.hasNext() )
//        {
//           CallSite nextimportant = ( CallSite ) importit.next();
//           // System.out.println(nextimportant.getInvokeExpr()); 

//           List attachedmethods = nextimportant.getMethodsAsList();
//           Iterator attachedit = attachedmethods.iterator();
//           ArrayList attachedQ = new ArrayList();
//           // attachedQ.addAll ( attachedmethods );

//           while ( attachedit.hasNext() )
//           attachedQ.add( attachedit.next() );
//           while ( ! attachedQ.isEmpty() )
//           {
//              MethodNode nextimpmethod = ( MethodNode ) attachedQ.remove(0);
//              if ( ! ImportantMethods.contains ( nextimpmethod ) )
//              {
//                 ImportantMethods.add ( 0, nextimpmethod );
//                 Iterator allpossmethodsit = nextimpmethod.getAllPossibleMethodsAsList().iterator();
//                 while ( allpossmethodsit.hasNext() )
//                 attachedQ.add ( ( MethodNode ) allpossmethodsit.next() );
//              }

//           }

//        }

//        Iterator importantmthdsit = ImportantMethods.iterator();
//        while ( importantmthdsit.hasNext() )
//        {
//           MethodNode nextimpmn = ( MethodNode ) importantmthdsit.next();
//           // System.out.println ( "IMPORTANT METHOD "+nextimpmn.getMethod().getSignature() );

//           // Iterator callsitesit = nextimpmn.getCallSites().iterator();

//           Iterator invokeExprsit = cagb.getInvokeExprs(nextimpmn.getMethod()).iterator();
//           // while ( callsitesit.hasNext() )

//           while ( invokeExprsit.hasNext() )
//           {
//              // CallSite nextcs = ( CallSite ) callsitesit.next();

//              CallSite nextcs = ( CallSite ) nextimpmn.getCallSite ( (InvokeExpr) invokeExprsit.next() );
//              if ( ! ImportantCS.contains ( nextcs ) )
//              {
//                 // System.out.println ( "IMPORTANT CALLSITE "+nextcs.getCallerID() );

//                 ImportantCS.add ( nextcs );
//                 ImportantQ.add ( 0, nextcs.getInvokeExpr() );
//              }

//           }

//        }

//        System.out.println("Done");
//        System.out.println();
//        System.out.print("Attempting to inline at important call sites");
//        Iterator importantit = ImportantQ.iterator();
//        int impcs = 0;
//        while ( importantit.hasNext() )
//        {
//           impcs++;
//           if ( ( impcs % 10 ) == 0 )
//           System.out.print(".");
//           InvokeExpr importantinvoke = ( InvokeExpr ) importantit.next();

//           examineCallSite ( importantinvoke );
//        }

//        System.out.println("Done!");
//        return changedclasses;
//     }


//     static BufferedReader getBufReader(InputStream i)
//     throws IOException {
//        return( new BufferedReader
//        ( new InputStreamReader(i)));
//     }


//     static BufferedReader getBufReader(FileInputStream f)
//     throws IOException {
//        return getBufReader( (InputStream) f );
//     }


//     static BufferedReader getBufReader(File file)
//     throws IOException {
//        FileInputStream in = new FileInputStream( file);
//        return getBufReader( (InputStream) in );
//     }


//     static BufferedReader getBufReader(String file)
//     throws IOException {
//        FileInputStream in = new FileInputStream(file);
//        return getBufReader( (InputStream) in );
//     }

//     List getCallSitesFromProfile() {
//        ArrayList allCallSites = new ArrayList();
//        ArrayList halfCallSites = new ArrayList();
//        try {
//           BufferedReader b = getBufReader ( "frequency.out" );
//           for ( ;; )
//           {
//              String currentline = b.readLine();
//              if ( currentline == null )
//              break;
//              allCallSites.add ( 0, currentline );
//           }

//        }
//        catch ( java.io.IOException e ) {}

//        int allCallSitesSize = allCallSites.size();
//        int halfCallSitesSize = allCallSitesSize / 2;
//        for ( int i = 0; i < halfCallSitesSize; i++ )
//        {
//           String currentline = ( String ) allCallSites.get ( i );
//           int separatorindex = currentline.indexOf ( ' ' );
//           int callsitenumindex = currentline.lastIndexOf ( '$' );
//           String callsitenumAsString = currentline.substring ( callsitenumindex + 1, separatorindex );
//           Integer callsitenum = Integer.valueOf ( callsitenumAsString );
//           String methodsig = currentline.substring ( 0, callsitenumindex );
//           methodsig = ConvertToNewSig(methodsig);
//           MethodNode mnode = cagb.getNode ( methodsig );
//           CallSite cs = mnode.getCallSite ( callsitenum );
//           halfCallSites.add ( cs.getInvokeExpr() );
//        }

//        return halfCallSites;
//     }


//     String ConvertToNewSig( String s ) {
//        int i = s.indexOf('(');
//        String changedpart = s.substring(0,i);
//        String samepart = s.substring(i, s.length() );
//        int j = changedpart.lastIndexOf('.');
//        String classname = changedpart.substring(0, j);
//        String methodname = changedpart.substring(j+1, changedpart.length());
//        String newsig = "<'"+classname+"':'"+methodname+"':"+samepart+">";
//        return newsig;
//     }


//     private int examined = 0;
//     void examineMethod ( MethodNode mn ) {
//        localsHT.clear();
//        SootMethod meth = mn.getMethod();
//        inliningInsideMethod = mn;
//        if ( ( ! meth.getName().equals ( "<clinit>" ) ) && ( ! Modifier.isNative( meth.getModifiers() ) ) )
//        {
//           if ( examined < ( numBenchmarkNodes )/2 )
//           {
//              if ( allowedToChange ( meth.getDeclaringClass().getName() ) )
//              {
//                 int origSize = Jimplifier.getJimpleBody( meth ).getUnits().size();
//                 //      System.out.println ( "TRYING TO INLINE INSIDE TARGET "+meth.getSignature()+" "+( ( Integer ) methodsToMinDepth.get ( meth.getSignature() ) ) );

//                 examined++;
//                 try {
//                    currmethod = meth;
//                    body = Jimplifier.getJimpleBody( meth );
//                    List locals = body.getLocals();
//                    Iterator localsit = locals.iterator();
//                    while ( localsit.hasNext() )
//                    {
//                       Local newlocal = ( Local ) localsit.next();
//                       localsHT.put ( newlocal.getName(), newlocal );
//                    }

//                    Iterator stmtIter = body.getUnits().iterator();
//                    numstmts = 0;
//                    while ( stmtIter.hasNext() )
//                    {
//                       try {
//                          Stmt stmt = (Stmt)stmtIter.next();
//                          if ( body.getUnits().size() < ( 2*( origSize ) ) )
//                          {
//                             numstmts++;
//                             assignFlag = false;
//                             invokeFlag = false;
//                             stmt.apply( new AbstractStmtSwitch(){
//                                public void caseInvokeStmt(InvokeStmt s){
//                                   invokeFlag = true;
//                                   currInvokeExpr = ( InvokeExpr ) s.getInvokeExpr();
//                                }

//                                public void caseAssignStmt(AssignStmt s){
//                                   if( s.getRightOp() instanceof InvokeExpr )
//                                   {
//                                      assignFlag = true;
//                                      returnvariable = s.getLeftOp();
//                                      currInvokeExpr = ( InvokeExpr ) s.getRightOp();
//                                   }

//                                }

//                             });

//                             Iterator CSiter = mn.getCallSites().iterator();
//                             CallSite cs = null;
//                             boolean search = true;
//                             while ( ( CSiter.hasNext() )&&(search == true) )
//                             {
//                                try {
//                                   cs = (CallSite) CSiter.next();
//                                   InvokeExpr invExpr = cs.getInvokeExpr();
//                                   if ( invExpr.equals(currInvokeExpr) )
//                                   {
//                                      search = false;
//                                   }

//                                }
//                                catch ( java.lang.RuntimeException e ) {}

//                             }

//                             if ( search == false )
//                             {
//                                interfaceInvoked = false;
//                                specialInvoked = false;
//                                staticInvoked = false;
//                                virtualInvoked = false;
//                                currInvokeExpr.apply( new AbstractJimpleValueSwitch() {
//                                   public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
//                                      interfaceInvoked = true;
//                                   }

//                                   public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
//                                      specialInvoked = true;
//                                   }

//                                   public void caseStaticInvokeExpr(StaticInvokeExpr v) {
//                                      staticInvoked = true;
//                                   }

//                                   public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
//                                      virtualInvoked = true;
//                                   }

//                                });

//                                if ( ( cs.getMethods().size() == 1 ) && ( invokeFlag || assignFlag ) )
//                                {
//                                   syncFlag = false;
//                                   MethodNode men = (MethodNode) cs.getMethods().iterator().next();
//                                   me = men.getMethod();
//                                   if ( ( Modifier.isSynchronized ( me.getModifiers() ) ) && ( ! staticInvoked ) )
//                                   syncFlag = true;
//                                   if ( ! (me.getName().equals("<init>") ) )
//                                   {
//                                      if (InliningCriteria.satisfiesCriteria (currMethod, me, currInvokeExpr ) )
//                                      {
//                                         changedclasses.add ( scm.getClass ( meth.getDeclaringClass().getName() ) );
//                                         mebody = Jimplifier.getJimpleBody( me );
//                                         List melocals = mebody.getLocals();
//                                         Iterator melocalsit = melocals.iterator();
//                                         while ( melocalsit.hasNext() )
//                                         {
//                                            Local newlocal = ( Local ) melocalsit.next();
//                                            Local clonedlocal = ( Local ) newlocal.clone();
//                                            clonedlocal.setName( new String( "dummy"+j ) );
//                                            localsHT.put ( newlocal.getName(), clonedlocal );
//                                            body.addLocal( clonedlocal );
//                                            j++;
//                                         }

//                                         if ( assignFlag )
//                                         {
//                                            returnvartype = me.getReturnType();
//                                            me.getReturnType().apply ( new TypeSwitch() {
//                                               public void caseBooleanType ( BooleanType t ) {
//                                                  returnvartype = IntType.v ();
//                                               }

//                                               public void caseByteType ( ByteType t ) {
//                                                  returnvartype = IntType.v ();
//                                               }

//                                               public void caseCharType ( CharType t ) {
//                                                  returnvartype = IntType.v ();
//                                               }

//                                               public void caseShortType ( ShortType t ) {
//                                                  returnvartype = IntType.v ();
//                                               }

//                                            });

//                                            dummyreturn = jimple.newLocal ( new String ( "dummyreturn"+returnj ), returnvartype );
//                                            localsHT.put ( dummyreturn.getName(), dummyreturn );
//                                            body.addLocal ( dummyreturn );
//                                            returnj++;
//                                         }

//                                         if ( ! staticInvoked )
//                                         {
//                                            Local dummynullcheck = jimple.newLocal ( new String ( "dummynull"+nullcheckj ), RefType.v ( "java.lang.NullPointerException" ) );
//                                            localsHT.put ( dummynullcheck.getName(), dummynullcheck );
//                                            body.addLocal ( dummynullcheck );
//                                            nullcheckj++;
//                                            Value baseval = ( ( NonStaticInvokeExpr ) currInvokeExpr ).getBase();
//                                            insertNullCheck ( baseval, dummynullcheck, body, lastSeenStmt);
//                                         }

//                                         if ( syncFlag )
//                                         {
//                                            syncthrow = jimple.newLocal ( new String ( "dummythrow"+throwj ), RefType.v ( "java.lang.Throwable" ) );
//                                            localsHT.put ( syncthrow.getName(), syncthrow );
//                                            body.addLocal ( syncthrow );
//                                            throwj++;
//                                         }

//                                         men.alreadyInlined = true;

//                                         InlineMethod (mebody, body, stmtIter);
//                                         if ( ( men.incomingedges == 1 ) /* && ( ! mn.alreadyInlined ) */ )
//                                         {
//                                            removedmethods.add ( men.getMethod().getSignature() );
//                                         }

//                                      }

//                                   }
//                                   // ADDED NOW

//                                }

//                             }
//                             //IF SEARCH

//                          }
//                          // < 1.2*   

//                       }
//                       catch ( java.lang.RuntimeException e ){ e.printStackTrace(System.out); }

//                    }

//                 }
//                 catch ( java.lang.RuntimeException e ){}

//                 //     System.out.println ( " NEW JIMPLE CODE FOR "+meth.getSignature() );

//                 PrintWriter out = new PrintWriter(System.out, true);
//                 body.printTo ( out );
//              }
//              // IF

//           }
//           // IF

//        }
//        // IF

//     }


//     private Local syncaddress;
//     private Local syncthrow;

//     void insertNullCheck (Value base, Local nullLocal, 
//                           Body target, Unit lastSeenStmt)
//     {
//        List emptyList = new ArrayList();

//        AssignStmt nullAssignStmt = jimple.newAssignStmt 
//            (nullLocal, jimple.newNewExpr ((RefType)nullLocal.getType()));
//        SpecialInvokeExpr nullSpInvExpr = jimple.newSpecialInvokeExpr 
//            (nullLocal, 
//             scm.getClass("java.lang.NullPointerException").getMethod 
//                    ("<init>", emptyList), emptyList);

//        InvokeStmt nullInvStmt = jimple.newInvokeStmt(nullSpInvExpr);
//        ThrowStmt nullThrowStmt = jimple.newThrowStmt(nullLocal);
//        NeExpr neExpr = jimple.newNeExpr (base, NullConstant.v());

//        IfStmt nullIfStmt = jimple.newIfStmt (neExpr, (Stmt) targetbody.get (numstmts - 1 ));

//        Chain units = body.getUnits();

//        Stmt nextStmt = (Stmt)units.getSuccOf(lastSeenStmt);

//        units.insertAfter(lastSeenStmt, nullIfStmt);
//        nullIfStmt.setTarget ((Stmt)units.getSuccOf(lastSeenStmt));

//        units.insertBefore(nextStmt, nullAssignStmt);
//        units.insertBefore(nextStmt, nullInvStmt);
//        units.insertBefore(nextStmt, nullThrowStmt);
//     }


//     static List classesToAnalyze;

//      // clone stuff would start here.

//      // &#*($&@#(*)&$*)#(@$&*()&%@#()*@&#$)(*&#@)($&*)(324
//     Local getCorrectLocal ( Local local ) {
//        //String correctlocalname = ( String ) localsHT.get ( local.getName() );

//        // Local correctlocal = jimple.newLocal ( correctlocalname, getCorrectType (local.getType() ) );

//        Local correctlocal = ( Local ) localsHT.get ( local.getName() );
//        return correctlocal;
//     }

//      // also must fix invokeExpr cloning
//      // caughtExceptionRef cloning

//     boolean inliningImportant = true;

//     Local cloneForsync;
//     Local thisvariable;

//     IdentityStmt syncid;
//     ExitMonitorStmt syncexit;
//     List syncexits = new ArrayList();
//     List syncexittargets = new ArrayList();

//     void InlineMethod (Body inlineeBody, Body targetBody, 
//                        Iterator target, InlinerState st)                      
//     {
//        syncexits = new ArrayList();
//        syncexittargets = new ArrayList();

//        Iterator fixupiterator = targetBody.getUnits().iterator();
//        int fixupnumstmts = 0;

//        while ( fixupnumstmts < ( numstmts - 1 ) )
//        {
//           fixupiterator.next();
//           fixupnumstmts++;
//        }

//        targetnumstmts = fixupnumstmts + 1;
//        int numidentity = 0;

//        if (st.syncFlag)
//            numidentity = CountIdentityStmts (inlineeBody);

//        Iterator stmtiter = inlineeBody.getUnits().iterator();
//        int numSofar = 0;
//        boolean firstinsertion = true;
//        while ( stmtiter.hasNext() )
//        {
//           Stmt nextstmt = ( Stmt ) stmtiter.next();
//           numSofar++;
//           if ( (syncFlag ) && ( firstinsertion ) )
//           {
//              if ( numSofar > numidentity )
//              {
//                 AssignStmt syncassign = 
//                     jimple.newAssignStmt (cloneForsync, thisvariable );
//                 targetBody.add ( numstmts, syncassign );
//                 numstmts++;
//                 EnterMonitorStmt syncenter = jimple.newEnterMonitorStmt ( thisvariable );
//                 targetBody.add ( numstmts, syncenter );
//                 numstmts++;
//                 firstinsertion = false;
//                 syncid = jimple.newIdentityStmt ( syncthrow, jimple.newCaughtExceptionRef ( body ) );
//                 syncexit = jimple.newExitMonitorStmt ( thisvariable );
//              }

//           }

//           Stmt newstmt = getCorrectStmt ( nextstmt );
//           fixupiter.add (newstmt );
//           numstmts++;

//        }

//        targetmeth = targetBody;
//        inlinablemeth = inlineeBody;
//        FixupTargets (st);
//        FixupTraps(st);
//        if ( syncFlag )
//        {
//           targetBody.add ( numstmts, syncid );
//           numstmts++;
//           ExitMonitorStmt syncex = jimple.newExitMonitorStmt ( cloneForsync );
//           targetBody.add ( numstmts, syncex );
//           numstmts++;
//           ThrowStmt syncthrw = jimple.newThrowStmt ( syncthrow );
//           targetBody.add ( numstmts, syncthrw );
//           numstmts++;
//        }

//        if (st.assignFlag )
//            returnstmt = jimple.newAssignStmt (returnvariable, 
//                                               (Value) dummyreturn );
//        else
//            returnstmt = jimple.newNopStmt();
//        targetBody.add ( numstmts, returnstmt );
//        numstmts++;

//        if (st.syncFlag)
//        {
//            // we need to find where the trap goes to, in the new method.
//           int trapindex = indexOf ( firstnonidstmt, inlinablemeth ) + 2;
//           Stmt starttrap = (Stmt) targetmeth.get (targetnumstmts+trapindex);
//           Trap synctrap = jimple.newTrap 
//               (scm.getClass("java.lang.Throwable"), starttrap, syncid, syncid);
//           body.getTraps().add(synctrap);
//        }

//        FixupMethod ( targetBody, fixupiterator, target, fixupnumstmts );
//        Iterator syncexitit = syncexits.iterator();
//        Iterator syncexittargetit = syncexittargets.iterator();
//        while ( syncexitit.hasNext() )
//        {
//           ExitMonitorStmt monitorexit = ( ExitMonitorStmt ) syncexitit.next();
//           targetBody.add ( numstmts - 1, monitorexit );
//           numstmts++;
//           GotoStmt syncret = jimple.newGotoStmt ( ( Stmt ) syncexittargetit.next() );
//           targetBody.add ( numstmts - 1, syncret );
//           numstmts++;
//        }

//     }

//      // self-contained now.
//     int CountIdentityStmts (Chain units) 
//     {
//        Iterator unitIt = units.iterator();
//        int numId = 0;
//        Stmt idstmt = null;

//        while ((unitIt.hasNext()))
//        {
//           idstmt = (Stmt) unitIt.next();
//           if (idstmt instanceof IdentityStmt)
//           {
//              IdentityStmt ids = (IdentityStmt) idstmt;
//              if (ids.getRightOp() instanceof CaughtExceptionRef)
//                  break;
//              else
//                  numId++;
//           }
//           else
//               break;
//        }

//        return numId;
//     }

//     void FixupTraps (final InlinerState st) {
//        Iterator excit = me.getExceptions().iterator();
//        while ( excit.hasNext() )
//        {
//           SootClass nextException = scm.getClass ((( SootClass ) excit.next()).getName());
//           if ( ! ( currmethod.throwsException ( nextException ) ) )
//           currmethod.addException ( nextException );
//        }

//        List trapCopy = new ArrayList(); trapCopy.addAll(mebody.getTraps());
//        Iterator trapiterator = trapCopy.iterator();
//        while (trapiterator.hasNext())
//        {
//           Trap t = (Trap) trapiterator.next();
//           Unit beginstmt = t.getBeginUnit();
//           Unit endstmt = t.getEndUnit();
//           Unit handlerstmt = t.getHandlerUnit();
//           SootClass exclass = t.getException();

//           int indexOftarget = indexOf ( beginstmt , inlinablemeth );

//           if ( syncFlag )
//               indexOftarget = indexOftarget + 2;

//           Stmt inlinedbeginstmt = ( Stmt ) targetmeth.get ( targetnumstmts+indexOftarget );

//           indexOftarget = indexOf ( endstmt , inlinablemeth );
//           if ( syncFlag )
//               indexOftarget = indexOftarget + 2;
//           Stmt inlinedendstmt = ( Stmt ) targetmeth.get ( targetnumstmts+indexOftarget );
//           indexOftarget = indexOf ( handlerstmt , inlinablemeth );
//           if ( syncFlag )
//           indexOftarget = indexOftarget + 2;
//           Stmt inlinedhandlerstmt = ( Stmt ) targetmeth.get ( targetnumstmts+indexOftarget );
//           Trap newtrap = jimple.newTrap ( exclass, inlinedbeginstmt, inlinedendstmt, inlinedhandlerstmt );
//           body.getTraps().insertAfter(t,newtrap);
//        }

//     }


//     void FixupTargets (final InlinerState st) {
//        Iterator inlineiterator = inlinablemeth.iterator();
//        inlinenumstmts = 0;
//        while ( inlineiterator.hasNext() )
//        {
//           Stmt s = ( Stmt ) inlineiterator.next();
//           s.apply ( new AbstractStmtSwitch () {
//              public void caseGotoStmt ( GotoStmt s ) {
//                 Stmt target = ( Stmt ) s.getTarget();
//                 int indexOftarget = indexOf ( target, inlinablemeth );
//                 if (st.syncFlag )
//                 indexOftarget = indexOftarget + 2;
//                 GotoStmt inlinedStmt = null;
//                 if (st.syncFlag )
//                 inlinedStmt = ( GotoStmt ) targetmeth.get( targetnumstmts+inlinenumstmts + 2);
//                 else
//                 inlinedStmt = ( GotoStmt ) targetmeth.get( targetnumstmts+inlinenumstmts );
//                 Stmt inlinedtarget = ( Stmt ) targetmeth.get ( targetnumstmts+indexOftarget );
//                 inlinedStmt.setTarget ( inlinedtarget );
//              }

//              public void caseIfStmt ( IfStmt s ) {
//                 Stmt target = ( Stmt ) s.getTarget();
//                 int indexOftarget = indexOf ( target, inlinablemeth );
//                 if ( syncFlag )
//                 indexOftarget = indexOftarget + 2;
//                 IfStmt inlinedStmt = null;
//                 if ( syncFlag )
//                 inlinedStmt = ( IfStmt ) targetmeth.get( targetnumstmts+inlinenumstmts+2 );
//                 else
//                 inlinedStmt = ( IfStmt ) targetmeth.get( targetnumstmts+inlinenumstmts );
//                 Stmt inlinedtarget = ( Stmt ) targetmeth.get ( targetnumstmts+indexOftarget );
//                 inlinedStmt.setTarget ( inlinedtarget );
//              }

//              public void caseLookupSwitchStmt ( LookupSwitchStmt s ) {
//                 List targets = s.getTargets();
//                 LookupSwitchStmt inlinedStmt = null;
//                 if ( syncFlag )
//                 inlinedStmt = ( LookupSwitchStmt ) targetmeth.get ( targetnumstmts+inlinenumstmts+2 );
//                 else
//                 inlinedStmt = ( LookupSwitchStmt ) targetmeth.get ( targetnumstmts+inlinenumstmts );
//                 Iterator targetsit = targets.iterator();
//                 int targetsnum = 0;
//                 while ( targetsit.hasNext() )
//                 {
//                    Stmt target = ( Stmt ) targetsit.next();
//                    int indexOftarget = indexOf ( target, inlinablemeth );
//                    if ( syncFlag )
//                    indexOftarget = indexOftarget + 2;
//                    Stmt inlinedtarget = ( Stmt ) targetmeth.get ( targetnumstmts+indexOftarget );
//                    inlinedStmt.setTarget ( targetsnum, inlinedtarget );
//                    targetsnum++;
//                 }

//                 Stmt defaulttarget = ( Stmt ) s.getDefaultTarget();
//                 int indexOftarget = indexOf ( defaulttarget, inlinablemeth );
//                 if ( syncFlag )
//                 indexOftarget = indexOftarget + 2;
//                 Stmt inlineddtarget = ( Stmt ) targetmeth.get ( targetnumstmts+indexOftarget );
//                 inlinedStmt.setDefaultTarget ( inlineddtarget );
//              }

//              public void caseTableSwitchStmt ( TableSwitchStmt s ) {
//                 List targets = s.getTargets();
//                 TableSwitchStmt inlinedStmt = null;
//                 if ( syncFlag )
//                 inlinedStmt = ( TableSwitchStmt ) targetmeth.get ( targetnumstmts+inlinenumstmts+2 );
//                 else
//                 inlinedStmt = ( TableSwitchStmt ) targetmeth.get ( targetnumstmts+inlinenumstmts );
//                 Iterator targetsit = targets.iterator();
//                 int targetsnum = 0;
//                 while ( targetsit.hasNext() )
//                 {
//                    Stmt target = ( Stmt ) targetsit.next();
//                    int indexOftarget = indexOf ( target, inlinablemeth );
//                    if ( syncFlag )
//                    indexOftarget = indexOftarget + 2;
//                    Stmt inlinedtarget = ( Stmt ) targetmeth.get ( targetnumstmts+indexOftarget );
//                    inlinedStmt.setTarget ( targetsnum, inlinedtarget );
//                    targetsnum++;
//                 }

//                 Stmt defaulttarget = ( Stmt ) s.getDefaultTarget();
//                 int indexOftarget = indexOf ( defaulttarget, inlinablemeth );
//                 if ( syncFlag )
//                 indexOftarget = indexOftarget + 2;
//                 Stmt inlineddtarget = ( Stmt ) targetmeth.get ( targetnumstmts+indexOftarget );
//                 inlinedStmt.setDefaultTarget ( inlineddtarget );
//              }

//           });

//           inlinenumstmts++;
//        }

//     }


//     void FixupMethod ( Chain targetMethod, Iterator fixupIterator,
//     Iterator target, int fixupNumStmts ) {
//        targetMethod.remove ( fixupNumStmts );
//        targetMethod.add ( fixupNumStmts, jimple.newNopStmt() );
//        int numiter = 0;
//        fixupNumStmts++;
//        Stmt s = ( Stmt ) targetMethod.get ( fixupNumStmts );
//        while ( fixupNumStmts < numstmts )
//        {
//           if ( s instanceof ReturnVoidStmt )
//           {
//              if ( syncFlag )
//              {
//                 GotoStmt syncgoto = jimple.newGotoStmt ( syncexit );
//                 targetMethod.add ( fixupNumStmts + 1, syncgoto );
//                 numstmts++;
//                 fixupNumStmts++;
//                 syncexits.add ( syncexit );
//                 syncexit = jimple.newExitMonitorStmt( thisvariable );
//              }

//              GotoStmt gs = jimple.newGotoStmt ( returnstmt );
//              if ( syncFlag )
//              syncexittargets.add ( gs );
//              targetMethod.add ( fixupNumStmts+1 , gs );
//              if ( syncFlag )
//              targetMethod.remove ( fixupNumStmts - 1);
//              else
//              targetMethod.remove ( fixupNumStmts );
//           }

//           else if ( s instanceof ReturnStmt )
//           {
//              Value im = ( ( ReturnStmt ) s ).getReturnValue();
//              Value r = null;
//              if ( im instanceof Local )
//              r = ( Local ) im;
//              else if ( im instanceof Constant )
//              r = ( Constant ) im;
//              if ( syncFlag )
//              {
//                 GotoStmt syncgoto = jimple.newGotoStmt ( syncexit );
//                 targetMethod.add ( fixupNumStmts + 1, syncgoto );
//                 numstmts++;
//                 fixupNumStmts++;
//                 syncexits.add ( syncexit );
//                 syncexit = jimple.newExitMonitorStmt( thisvariable );
//              }

//              AssignStmt dummyreturnassign = jimple.newAssignStmt ( dummyreturn , r );
//              targetMethod.add ( fixupNumStmts+1 , dummyreturnassign );
//              fixupNumStmts++;
//              GotoStmt gs = jimple.newGotoStmt ( returnstmt );
//              if ( syncFlag )
//                  syncexittargets.add ( dummyreturnassign );
//              targetMethod.add ( fixupNumStmts+1 , gs );
//              if ( syncFlag )
//                  targetMethod.remove ( fixupNumStmts - 2 );
//              else
//                  targetMethod.remove ( fixupNumStmts - 1 );
//              numiter++;
//              numstmts++;
//           }

//           s = (Stmt) targetMethod.get ( fixupNumStmts+ 1);
//           fixupNumStmts++;
//        }

//     }

//     List getCorrectList ( List l ) {
//        List correctlist = new ArrayList();
//        correctlist.addAll ( l );
//        return correctlist;
//     }


//     void removeMethods() {}


//     void examineMethods ( Collection callgraph, Resolver res ) {
//        resolver = res;
//        scm = resolver.getManager();
//        clgb = resolver.getClassGraphBuilder();
//        incorrectlyjimplified = clgb.getIncorrectlyJimplifiedClasses();
//        //  System.out.println ( "INCORRECTLY JIMPLIFIED" );

//        Iterator incorrectlyjimplifiedit = incorrectlyjimplified.iterator();
//        while ( incorrectlyjimplifiedit.hasNext() )
//        {
//           System.out.println ( ( String ) incorrectlyjimplifiedit.next() );
//        }

//        List sortedcallgraph = sortMethods ( callgraph );
//        List reachedcallgraph = setMethodDepths ( sortedcallgraph );
//        List sortedbydepths = sortByMethodDepths ( reachedcallgraph );
//        // Iterator iter = callgraph.iterator();

//        // Iterator iter = sortedcallgraph.iterator();

//        LoopDetector loopd = new LoopDetector();
//        Iterator numloopsit = sortedbydepths.iterator();
//        while ( numloopsit.hasNext() )
//        {
//           MethodNode loopsMN = ( MethodNode ) numloopsit.next();
//           loopd.setLoopCountFor ( loopsMN );
//        }

//        // List importantmethods = returnImportantMethods ( sortedbydepths );

//        inliningImportant = false;
//        Iterator iter = sortedbydepths.iterator();
//        // Iterator iter = importantmethods.iterator();

//        while ( iter.hasNext() )
//        {
//           MethodNode tempMN = (MethodNode) iter.next();
//           examineMethod( tempMN );
//        }

//        removeMethods();
//        PrintWriter out = new PrintWriter(System.out, true);
//        Iterator changedit = changedclasses.iterator();
//        // System.out.println ( "+++++++++ NO. OF CHANGED CLASSES "+changedclasses.size() );

//        while ( changedit.hasNext () )
//        {
//              ArrayList usefulmethods = new ArrayList();
//              SootClass changedclass = ( SootClass ) changedit.next();
//              // System.out.println ( "CHANGGED CLASS "+changedclass );

//              Iterator methit = changedclass.getMethods().iterator();
//              // System.out.println ( "NO OF MTHDS = "+changedclass.getMethods().size() );

//              /*
//                  Jimple jimple = Jimple.v();
               
//                  BodyExpr storedclass = new StoredBody ( ClassFile.v() );
              
//              */
//              while ( methit.hasNext() )
//              {
//                 SootMethod changedmethod = ( SootMethod ) methit.next();
//                 MethodNode changednode = ( MethodNode ) cagb.getNode ( changedmethod );

//                 JimpleBody changedjb = null;
//                 if ( changedmethod.hasActiveBody() )
//                     changedjb = ( JimpleBody ) changedmethod.getActiveBody();
//                 else
//                     throw new java.lang.RuntimeException();
               
//                 gotoEliminate ( changedjb );
//                 Transformations.cleanupCode ( changedjb );

//                 changedmethod.setActiveBody ( new GrimpBody ( changedjb ) );
//                 usefulmethods.add ( changedmethod );
//              }

//              changedclass.printTo( out );
//              changedclass.write();
//        }

//     }


//     void gotoEliminate ( JimpleBody jb ) {
//         JumpOptimizer.optimizeJumps(jb);
//     }
}
