/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package soot;

import soot.jimple.*;
import soot.util.*;
import java.util.*;

/** Represents the class hierarchy.  It is closely linked to a Scene,
 * and must be recreated if the Scene changes. 
 *
 * This version supercedes the old soot.Hierarchy class.
 */
public class FastHierarchy
{
    /** This map holds all key,value pairs such that 
     * value.getSuperclass() == key. This is one of the three maps that hold
     * the inverse of the relationships given by the getSuperclass and
     * getInterfaces methods of SootClass. */
    protected MultiMap classToSubclasses = new HashMultiMap();

    /** This map holds all key,value pairs such that value is an interface 
     * and key is in value.getInterfaces(). This is one of the three maps 
     * that hold the inverse of the relationships given by the getSuperclass 
     * and getInterfaces methods of SootClass. */
    protected MultiMap interfaceToSubinterfaces = new HashMultiMap();

    /** This map holds all key,value pairs such that value is a class 
     * (NOT an interface) and key is in value.getInterfaces(). This is one of 
     * the three maps that hold the inverse of the relationships given by the 
     * getSuperclass and getInterfaces methods of SootClass. */
    protected MultiMap interfaceToImplementers = new HashMultiMap();

    /** This map is a transitive closure of interfaceToSubinterfaces,
     * and each set contains its superinterface itself. */
    protected MultiMap interfaceToAllSubinterfaces = new HashMultiMap();

    /** This map gives, for an interface, all concrete classes that
     * implement that interface and all its subinterfaces, but
     * NOT their subclasses. */
    protected MultiMap interfaceToAllImplementers = new HashMultiMap();

    /** For each class (NOT interface), this map contains a Interval, which is
     * a pair of numbers giving a preorder and postorder ordering of classes
     * in the inheritance tree. */
    protected Map classToInterval = new HashMap();

    /** These maps cache subtype queries, so they can be re-done quickly. */
    protected MultiMap cacheSubtypes = new HashMultiMap();
    protected MultiMap cacheNonSubtypes = new HashMultiMap();

    protected int state;
    protected Scene sc;

    protected class Interval {
        int lower;
        int upper;
        boolean isSubrange( Interval potentialSubrange ) {
            if( lower > potentialSubrange.lower ) return false;
            if( upper < potentialSubrange.upper ) return false;
            return true;
        }
    }
    protected int dfsVisit( int start, SootClass c ) {
        Interval r = new Interval();
        r.lower = start++;
        Iterator it = classToSubclasses.get( c ).iterator();
        while( it.hasNext() ) {
            start = dfsVisit( start, (SootClass) it.next() );
        }
        r.upper = start++;
        classToInterval.put( c, r );
        return start;
    }

    /** Constructs a hierarchy from the current scene. */
    public FastHierarchy()
    {
        this.sc = Scene.v();
        state = sc.getState();

	/* First build the inverse maps. */
	for( Iterator it = sc.getClasses().iterator(); it.hasNext(); ) {
	    SootClass cl = (SootClass) it.next();
	    if( cl.hasSuperclass() ) {
		classToSubclasses.put( cl.getSuperclass(), cl );
	    }
	    for( Iterator ifs = cl.getInterfaces().iterator(); ifs.hasNext(); ) {
		SootClass supercl = (SootClass) ifs.next();
		if( cl.isInterface() ) {
		    interfaceToSubinterfaces.put( supercl, cl );
		} else {
		    interfaceToImplementers.put( supercl, cl );
		}
	    }
	}

	/* Now do a dfs traversal to get the Interval numbers. */
	dfsVisit( 0, Scene.v().getSootClass( "java.lang.Object" ) );
    }

    private void checkState()
    {
        if (state != sc.getState())
            throw new ConcurrentModificationException("Scene changed for Hierarchy!");
    }

    /** Return true if class child is a subclass of class parent, neither of
     * them being allowed to be interfaces. */
    protected boolean isSubclass( SootClass child, SootClass parent ) {
	Interval parentInterval = (Interval) classToInterval.get( parent );
	Interval childInterval = (Interval) classToInterval.get( child );
	return parentInterval.isSubrange( childInterval );
    }

    /** For an interface parent (MUST be an interface), returns set of all
     * implementers of it but NOT their subclasses. */
    protected Set getAllImplementersOfInterface( SootClass parent ) {
	Set subs;
	if( interfaceToAllImplementers.containsKey( parent ) ) {
	    subs = interfaceToAllImplementers.get( parent );
	} else {
	    subs = interfaceToAllImplementers.get( parent );
	    for( Iterator it = getAllSubinterfaces( parent ).iterator(); it.hasNext(); ) {
		SootClass subinterface = (SootClass) it.next();
		if( subinterface == parent ) continue;
		subs.addAll( getAllImplementersOfInterface( subinterface ) );
	    }
	    subs.addAll( interfaceToImplementers.get( parent ) );
	}
	return subs;
    }

    /** For an interface parent (MUST be an interface), returns set of all
     * subinterfaces. */
    protected Set getAllSubinterfaces( SootClass parent ) {
	Set subs;
	if( interfaceToAllSubinterfaces.containsKey( parent ) ) {
	    subs = interfaceToAllSubinterfaces.get( parent );
	} else {
	    subs = interfaceToAllSubinterfaces.get( parent );
	    subs.add( parent );
	    for( Iterator it = interfaceToSubinterfaces.get( parent ).iterator(); it.hasNext(); ) {
		subs.addAll( getAllSubinterfaces( (SootClass) it.next() ) );
	    }
	}
	return subs;
    }

    /** Given an object of declared type child, returns true if the object
     * can be stored in a variable of type parent. If child is an interface
     * that is not a subinterface of parent, this method will return false
     * even though some objects implementing the child interface may also
     * implement the parent interface. */
    public boolean canStoreType( Type child, Type parent ) {
	if( cacheSubtypes.get( parent ).contains( child ) ) return true;
	if( cacheNonSubtypes.get( parent ).contains( child ) ) return false;
	boolean ret = canStoreTypeInternal( child, parent );
	( ret ? cacheSubtypes : cacheNonSubtypes ).put( parent, child );
	return ret;
    }

    /** Given an object of declared type child, returns true if the object
     * can be stored in a variable of type parent. If child is an interface
     * that is not a subinterface of parent, this method will return false
     * even though some objects implementing the child interface may also
     * implement the parent interface. */
    protected boolean canStoreTypeInternal( Type child, Type parent ) {
	if( child instanceof RefType ) {
	    if( parent instanceof RefType) {
		return canStoreClass( ((RefType) child).getSootClass(),
		    ((RefType) parent).getSootClass() );
	    } else {
		return false;
	    }
	} else if( child instanceof NullType ) {
	    if( !(parent instanceof RefLikeType ) ) {
		throw new RuntimeException( "Unhandled type "+parent );
	    } else {
		return true;
	    }
	} else {
	    ArrayType achild = (ArrayType) child;
	    if( parent instanceof RefType ) {
		// From Java Language Spec, Chapter 10, Arrays
		return parent.equals( RefType.v( "java.lang.Object" ) )
		|| parent.equals( RefType.v( "java.io.Serializable" ) )
		|| parent.equals( RefType.v( "java.lang.Cloneable" ) );
	    }
	    ArrayType aparent = (ArrayType) parent;
	    // You can store a int[][] in a Object[]. Yuck!
	    return ( achild.numDimensions == aparent.numDimensions &&
		    achild.baseType instanceof RefType &&
		    aparent.baseType instanceof RefType &&
		    canStoreType( achild.baseType, aparent.baseType ) )
	    || ( achild.numDimensions > aparent.numDimensions &&
	       ( aparent.baseType.equals( RefType.v( "java.lang.Object" ) )
		 || aparent.baseType.equals( RefType.v( "java.io.Serializable" ) )
		 || aparent.baseType.equals( RefType.v( "java.lang.Cloneable" ) ) ) );
	}
    }

    /** Given an object of declared type child, returns true if the object
     * can be stored in a variable of type parent. If child is an interface
     * that is not a subinterface of parent, this method will return false
     * even though some objects implementing the child interface may also
     * implement the parent interface. */
    protected boolean canStoreClass( SootClass child, SootClass parent ) {
	checkState();

	Interval parentInterval = (Interval) classToInterval.get( parent );
	Interval childInterval = (Interval) classToInterval.get( child );
	if( parentInterval != null && childInterval != null ) {
	    return parentInterval.isSubrange( childInterval );
	}
	if( childInterval == null ) { // child is interface
	    if( parentInterval != null ) { // parent is not interface
		return false;
	    } else {
		return getAllSubinterfaces( parent ).contains( child );
	    }
	} else {
	    Set impl = getAllImplementersOfInterface( parent );
	    for( Iterator it = impl.iterator(); it.hasNext(); ) {
		parentInterval = (Interval) classToInterval.get( it.next() );
		if( parentInterval.isSubrange( childInterval ) ) {
		    return true;
		}
	    }
	    return false;
	}
    }

    // Questions about method invocation.

    /** Given an object of actual type C (o = new C()), returns the method which will be called
        on an o.f() invocation. */
    public SootMethod resolveConcreteDispatch(SootClass concreteType, SootMethod m)
    {
        checkState();

	if( concreteType.isInterface() ) {
	    throw new RuntimeException(
		"A concrete type cannot be an interface: "+concreteType );
	}

        String methodSig = m.getSubSignature();
	while( concreteType != null ) {
	    if( concreteType.declaresMethod( methodSig ) ) {
		return concreteType.getMethod( methodSig );
	    }
	    concreteType = concreteType.getSuperclass();
        }
        throw new RuntimeException("could not resolve concrete dispatch!\nType: "+concreteType+"\nMethod: "+m);
    }

    /** Returns the target for the given SpecialInvokeExpr. */
    public SootMethod resolveSpecialDispatch(SpecialInvokeExpr ie, SootMethod container)
    {
        SootMethod target = ie.getMethod();

        /* This is a bizarre condition!  Hopefully the implementation is correct.
           See VM Spec, 2nd Edition, Chapter 6, in the definition of invokespecial. */
        if (target.getName().equals("<init>") || target.isPrivate())
            return target;
        else if (isSubclass(target.getDeclaringClass(), container.getDeclaringClass()))
            return resolveConcreteDispatch(container.getDeclaringClass(), target);
        else
            return target;
    }
}
