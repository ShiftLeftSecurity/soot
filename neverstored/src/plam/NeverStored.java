package plam;

import soot.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;

public class NeverStored {
	public static void main(String[] args) {
            // Inject the analysis tagger into Soot
            soot.PackManager.v().getPack("wjtp").add
                (new soot.Transform("wjtp.nsa",
                NeverStoredAnnotator.v()));

            // Invoke soot.Main with arguments given
            soot.Main.main(args);
        }
}
