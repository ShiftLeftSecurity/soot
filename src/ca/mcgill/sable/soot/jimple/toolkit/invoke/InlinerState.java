package ca.mcgill.sable.soot.jimple.toolkit.invoke;

import ca.mcgill.sable.soot.*;

class InlinerState
{
    // is the current inlinee synchronized?
    boolean syncFlag;
    
    // is the current statement being inlined away an assignStmt?
    boolean assignFlag;
    
    // an invokeStmt?
    boolean invokeFlag;
    
    boolean interfaceInvoked;
    boolean specialInvoked;
    boolean staticInvoked;
    boolean virtualInvoked;

    // Must initialize
    Resolver res;
    Scene scm;

    InliningCriteria crit = new InliningCriteria();
}
