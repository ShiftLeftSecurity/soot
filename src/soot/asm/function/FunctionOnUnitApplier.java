package soot.asm.function;

import soot.Unit;
import soot.asm.UnitContainer;

import java.util.ArrayDeque;
import java.util.HashSet;

public class FunctionOnUnitApplier {
  /*
   * Applies a function on unit and if it is a UnitContainer recursively on all contained units.
   * The recursive processing is done in worklist fashion to not run into callstack size problems.
   * Loop detection is also in place to avoid cycling forever.
   * The passed function gets all units in the same order as in the program and needs to return
   * true if processing shall continue.
   */
  public static void process(Unit unit, Function<Unit, Boolean> function) {
    if (unit instanceof UnitContainer) {
      ArrayDeque<Unit> worklist = new ArrayDeque<>();
      // The set is here only for the case there are loops in the unit container which
      // would be a bug. But with SOOT you never know.
      HashSet<Unit> alreadyProcessedUnits = new HashSet<>();
      worklist.addFirst(unit);

      Boolean keepGoing = true;
      while (!worklist.isEmpty() && keepGoing) {
        Unit currentUnit = worklist.removeFirst();
        if (alreadyProcessedUnits.contains(currentUnit)) {
          continue;
        }

        alreadyProcessedUnits.add(currentUnit);

        if (currentUnit instanceof UnitContainer) {
          Unit units[] = ((UnitContainer) currentUnit).units;
          // To maintain ordering we need to add in reverse.
          for (int i = units.length - 1; i >=0; i--) {
            worklist.addFirst(units[i]);
          }
        } else {
          keepGoing = function.apply(currentUnit);
        }
      }
    } else {
      function.apply(unit);
    }
  }
}
