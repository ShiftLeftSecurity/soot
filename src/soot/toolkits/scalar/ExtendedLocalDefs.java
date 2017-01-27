package soot.toolkits.scalar;

import soot.Unit;
import soot.jimple.FieldRef;

import java.util.List;

public interface ExtendedLocalDefs extends LocalDefs {
    public List<Unit> getDefsOfAt(FieldRef fieldRef, Unit s);
    public List<Unit> getDefsOf(FieldRef fieldRef);
}
