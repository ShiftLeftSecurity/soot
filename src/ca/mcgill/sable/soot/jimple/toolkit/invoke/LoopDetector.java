// package ca.mcgill.sable.soot.virtualCalls;

package ca.mcgill.sable.soot.jimple.toolkit.invoke;
import java.io.*;
import ca.mcgill.sable.soot.*;
import ca.mcgill.sable.soot.jimple.*;
import ca.mcgill.sable.util.*;
import java.util.*;
import ca.mcgill.sable.soot.baf.*;

class LoopDetector 
{
    // Modifies MethodNode's ImportantInvokeExprs and loopCount fields!
   void setLoopCountFor (MethodNode me) 
   {
      int loopCount = 0;

      SootMethod m = me.getMethod();

      JimpleBody jb = Jimplifier.getJimpleBody (m);

      Chain units = jb.getUnits();
      HashSet alreadyVisitedUnits = new HashSet(units.size() * 2 + 1, 0.7f);

      CompleteUnitGraph g = new CompleteUnitGraph(jb);
      Iterator stmtIt = units.iterator();
      while (stmtIt.hasNext())
      {
          Stmt s = (Stmt)stmtIt.next();
          alreadyVisitedUnits.add(s);

          if (s instanceof IfStmt)
          {
              List list = g.getSuccsOf( s );
              Iterator listIt = list.iterator();
              while (listIt.hasNext())
              {
                  Stmt succ = ( Stmt ) listIt.next();

                  if (alreadyVisitedUnits.contains(succ))
                  {
                      loopCount++;
                      Iterator innerIterator = units.iterator(succ, s);
                      int tempcount = 0;
                      HashSet innerAlreadySeenSet = new HashSet(units.size() * 2 + 1, 0.7f);

                      while (innerIterator.hasNext())
                      {
                          Stmt is = (Stmt) innerIterator.next();

                          if (is instanceof InvokeStmt)
                              me.ImportantInvokeExprs.add (((InvokeStmt)is).getInvokeExpr());
                          else if (is instanceof AssignStmt)
                          {
                              AssignStmt as = (AssignStmt)is;
                              if (as.getRightOp() instanceof InvokeExpr)
                                  me.ImportantInvokeExprs.add (as.getRightOp());                              
                          }

                          tempcount++;
                      }
                      
                  }

              }
              
          }
      }

      me.numloops = loopCount;
   }
}
