package soot.toolkits.scalar;

import org.junit.Before;
import org.junit.Test;
import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.Main;
import soot.PackManager;
import soot.SootField;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

public class ExtendedSimpleLocalDefsTest {
  @Before
  public void buildClassFile() throws IOException, InterruptedException {
    String[] command = {"bash", "-c", "javac -g tests/soot/toolkits/scalar/ExtendedSimpleLocalDefsTestCode.java"};
    new ProcessBuilder().inheritIO().command(command).start();
    // We need to sleep here to wait for the output file.
    // Otherwise the test run on old class files.
    sleep(500);
  }
  @Test
  public void foo() {
    PackManager.v().getPack("gb").add(
          new Transform("gb.myTransform", new BodyTransformer() {
            @Override
            protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
              if (b.getMethod().getName().equals("testMethod")) {
                UnitGraph graph = new ExceptionalUnitGraph(b);
                ExtendedSimpleLocalDefs localDefs = new ExtendedSimpleLocalDefs(graph);

                for (Unit unit : b.getUnits()) {
                  for (ValueBox valueBox : unit.getUseBoxes()) {
                    Value value = valueBox.getValue();
                    if (value instanceof Local || value instanceof FieldRef) {
                      System.out.println(unit.toString() + ": " + value);
                      if (value instanceof Local) {
                        System.out.println(localDefs.getDefsOfAt((Local)value, unit));
                      }
                      else if (value instanceof FieldRef) {
                        System.out.println(localDefs.getDefsOfAt((FieldRef) value, unit));
                      }
                    }
                    if (value instanceof SootField) {
                      //System.out.println(value);
                    }
                  }
                }

              }
            }
          }));
    Options.v().set_soot_classpath("tests/soot/toolkits/scalar");
    List<String> procDirs = new LinkedList<String>();
    procDirs.add("tests/soot/toolkits/scalar");
    Options.v().set_process_dir(procDirs);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_whole_program(true);
    Options.v().set_output_format(Options.output_format_g);
    Options.v().set_src_prec(Options.src_prec_only_class);
    Options.v().setPhaseOption("jb", "use-original-names:true");
    Main.main(new String[]{" "});
  }
}
