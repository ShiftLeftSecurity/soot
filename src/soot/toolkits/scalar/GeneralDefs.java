package soot.toolkits.scalar;

import soot.Unit;

import java.util.List;

public interface GeneralDefs {
    public List<Unit> getDefsOfAt(Object object, Unit s);
    public List<Unit> getDefsOf(Object object);
}
