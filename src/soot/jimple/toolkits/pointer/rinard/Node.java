package soot.jimple.toolkits.pointer.rinard;

class Node {
    static int lastId = 0;
    private int id;
    Node() {
	id = lastId++;
    }
    int getId() {
	return id;
    }
    String toDotGraph( String options ) {
	return ""+getId()+" [ label=\"" + toString() + "\" "+dotGraphOptions()+" "+options+" ] ;\n";
    }
    String dotGraphOptions() { return ""; }
    public int hashCode() {
	return id;
    }
    public boolean equals( Object o ) {
	try {
	    return getId() == ((Node)o).getId();
	} catch( ClassCastException e ) {
	    return false;
	}
    }
}

