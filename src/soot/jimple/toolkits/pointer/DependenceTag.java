/* Soot - a J*va Optimization Framework
 * Copyright (C) 2000 Feng Qian
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


package soot.jimple.toolkits.pointer;
import soot.*;
import java.util.*;
import soot.tagkit.*;

public class DependenceTag implements Tag
{
    private final static String NAME = "DependenceTag";
    protected Set dependentStmtsRR = new HashSet();
    protected Set dependentStmtsRW = new HashSet();
    protected Set dependentStmtsWR = new HashSet();
    protected Set dependentStmtsWW = new HashSet();
    protected boolean callsNative = false;
    
    public void setCallsNative() {
	callsNative = true;
    }

    public void addStmtRR( Unit s ) {
	dependentStmtsRR.add( s );
    }

    public void addStmtRW( Unit s ) {
	dependentStmtsRW.add( s );
    }

    public void addStmtWR( Unit s ) {
	dependentStmtsWR.add( s );
    }

    public void addStmtWW( Unit s ) {
	dependentStmtsWW.add( s );
    }

    public String getName()
    {
	return NAME;
    }

    public byte[] getValue() {
	throw new RuntimeException( "A map mapping units to labels is needed." );
    }

    public byte[] getValue( Map instToLabel )
    {
	StringBuffer buf = new StringBuffer();
	if( callsNative ) {
	    buf.append( "N%" );
	}
	if( !dependentStmtsRR.isEmpty() ) {
	    buf.append( "RR" );
	    for( Iterator it = dependentStmtsRR.iterator(); it.hasNext(); ) {
		Unit u = (Unit) it.next();
		String label = (String) instToLabel.get( u );
		buf.append( label+"%" );
	    }
	}
	if( !dependentStmtsRW.isEmpty() ) {
	    buf.append( "RW" );
	    for( Iterator it = dependentStmtsRW.iterator(); it.hasNext(); ) {
		Unit u = (Unit) it.next();
		String label = (String) instToLabel.get( u );
		buf.append( label+"%" );
	    }
	}
	if( !dependentStmtsWR.isEmpty() ) {
	    buf.append( "WR" );
	    for( Iterator it = dependentStmtsWR.iterator(); it.hasNext(); ) {
		Unit u = (Unit) it.next();
		String label = (String) instToLabel.get( u );
		buf.append( label+"%" );
	    }
	}
	if( !dependentStmtsWW.isEmpty() ) {
	    buf.append( "WW" );
	    for( Iterator it = dependentStmtsWW.iterator(); it.hasNext(); ) {
		Unit u = (Unit) it.next();
		String label = (String) instToLabel.get( u );
		buf.append( label+"%" );
	    }
	}
	return buf.toString().getBytes();
    }

    public String toString()
    {
	StringBuffer buf = new StringBuffer();
	if( callsNative ) buf.append( "DependenceCallsNative\n" );
	for( Iterator it = dependentStmtsRR.iterator(); it.hasNext(); ) {
	    buf.append( "DependenceRR " );
	    buf.append( ""+it.next() );
	    buf.append( "\n" );
	}
	for( Iterator it = dependentStmtsRW.iterator(); it.hasNext(); ) {
	    buf.append( "DependenceRW " );
	    buf.append( ""+it.next() );
	    buf.append( "\n" );
	}
	for( Iterator it = dependentStmtsWR.iterator(); it.hasNext(); ) {
	    buf.append( "DependenceWR " );
	    buf.append( ""+it.next() );
	    buf.append( "\n" );
	}
	for( Iterator it = dependentStmtsWW.iterator(); it.hasNext(); ) {
	    buf.append( "DependenceWW " );
	    buf.append( ""+it.next() );
	    buf.append( "\n" );
	}
	return buf.toString();
    }
}
