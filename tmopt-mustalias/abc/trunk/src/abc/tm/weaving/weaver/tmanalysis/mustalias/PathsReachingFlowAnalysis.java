/** Approximates the number of paths that reach any given statement;
 * i.e. detects whether a statement belongs to a loop or not.  */

package abc.tm.weaving.weaver.tmanalysis.mustalias;

import java.util.HashSet;

import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class PathsReachingFlowAnalysis extends ForwardFlowAnalysis {
    public static final Object NONE = new Object();
    public static final Object ONE = new Object();
    public static final Object MANY = new Object();

    HashSet visited = new HashSet();
    
    public class Box { 
        Object o;

        public Box(Object o) {
            setValue(o);
        }

        public void setValue(Object o) {
            validate(o);
            this.o = o;
        }
        public Object getValue() {
            return o;
        }

        public void validate(Object o) {
            assert (o == NONE || o == ONE || o == MANY);
        }

        public boolean equals(Object other) {
            if (!(other instanceof Box)) return false;
            Box b = (Box)other;
            return (b.o == o);
        }
    }

	public PathsReachingFlowAnalysis(UnitGraph g) {
		super(g);
		doAnalysis();
    }

	protected void flowThrough(Object inVal, Object stmt, Object outVal) {
		Box out = (Box) outVal;
		if (visited.contains(stmt)) {
			out.setValue(MANY);
		} else {
			out.setValue(ONE);
			visited.add(stmt);
		}
	}

	protected Object newInitialFlow() {
		return new Box(NONE);
	}

	protected Object entryInitialFlow() {
		return new Box(NONE);
	}

	protected void copy(Object src, Object dest) {
		Box in = (Box) src, out = (Box) dest;
        out.setValue(in.getValue());
	}

	protected void merge(Object i1, Object i2, Object o) {
        //   N O M
        // N N O M
        // O O O M 
        // M M M M
		Box in1 = (Box) i1, in2 = (Box) i2, out = (Box) o;
        if (in1.getValue() == MANY || in2.getValue() == MANY) {
            out.setValue(MANY);
        }
        else if (in1.getValue() == NONE && in2.getValue() == NONE) {
            out.setValue(NONE);
        }
        else 
            out.setValue(ONE);
	}
}
