package soot.jimple.toolkits.pointer.kloj;
import soot.jimple.toolkits.pointer.*;
import java.util.*;

public class ReallyCheapRasUnion extends Union {

    public boolean isEmpty() {
	return false;
    }
    public boolean hasNonEmptyIntersection( ObjectSet other ) {
	return true;
    }
    public boolean addAll( ObjectSet s ) {
	return false;
    }
    public Object clone() {
	return this;
    }
    public Set possibleTypes() {
	throw new RuntimeException( "Not implemented" );
    }
    static int count = 0;
    public ReallyCheapRasUnion() {
	count++;
	if( ( count % 1000 ) == 0 ) System.out.println( "Made "+count+"th ReallyCheapRasUnion" );
    }
}
