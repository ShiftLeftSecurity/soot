package soot.jimple.toolkits.pointer;

import soot.*;
import java.util.*;

/** Provides naive side effect information. 
 * Relies on no context information; instead, does the least 
 * conservative thing possible even in the possible presence of badness. 
 *
 * Possible weakness of SideEffectTester: consider a Box.  We don't 
 * have a name for "what-is-inside-the-box" and so we can't 
 * ask questions about it.  But perhaps we need only ask questions
 * about the box itself; the sideeffect tester can deal with
 * that internally. */

//  ArrayRef, 
//  CaughtExceptionRef, 
//  FieldRef, 
//  IdentityRef, 
//  InstanceFieldRef, 
//  InstanceInvokeExpr, 
//  Local,  
//  StaticFieldRef

public class PASideEffectTester implements SideEffectTester
{
    /** Returns true if the unit can read from v.
     * Does not deal with expressions; deals with Refs. */
    public boolean unitCanReadFrom(Unit u, Value v)
    {
	/*
        Stmt s = (Stmt)u;

        // This doesn't really make any sense, but we need to return something.
        if (v instanceof Constant)
            return false;

        if (v instanceof Expr)
            throw new RuntimeException("can't deal with expr");

        // If it's an invoke, then only locals are safe.
        if (s.containsInvokeExpr())
        {
            if (v instanceof Local) {
                return false;
	    } else {
		RWSet readSet = sea.readSet( u );
		return readSet.containsValue( v );
	    }
        }

        // otherwise, use boxes tell all.
        Iterator useIt = u.getUseBoxes().iterator();
        while (useIt.hasNext())
        {
            Value use = (Value)useIt.next();

            if (use.equivTo(v))
                return true;

            Iterator vUseIt = v.getUseBoxes().iterator();
            while (vUseIt.hasNext())
            {
                if (use.equivTo(vUseIt.next()))
                    return true;
            }
        }
	*/
        return false;
    }

    public boolean unitCanWriteTo(Unit u, Value v)
    {
	/*
        Stmt s = (Stmt)u;

        if (v instanceof Constant)
            return false;

        if (v instanceof Expr)
            throw new RuntimeException("can't deal with expr");

        // If it's an invoke, then only locals are safe.
        if (s.containsInvokeExpr())
        {
            if (v instanceof Local) {
                return false;
	    } else {
		RWSet readSet = sea.readSet( u );
		return readSet.containsValue( v );
	    }
        }

        // otherwise, def boxes tell all.
        Iterator defIt = u.getDefBoxes().iterator();
        while (defIt.hasNext())
        {
            Value def = ((ValueBox)(defIt.next())).getValue();
            Iterator useIt = v.getUseBoxes().iterator();
            while (useIt.hasNext())
            {
                Value use = ((ValueBox)useIt.next()).getValue();
                if (def.equivTo(use))
                    return true;
            }
            // also handle the container of all these useboxes!
            if (def.equivTo(v))
                return true;

            // deal with aliasing - handle case where they
            // are a read to the same field, regardless of
            // base object.
            if (v instanceof InstanceFieldRef && 
                def instanceof InstanceFieldRef)
            {
                if (((InstanceFieldRef)v).getField() ==
                    ((InstanceFieldRef)def).getField())
                    return true;
            }
        }
	*/
        return false;
    }
}
