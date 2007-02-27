package abc.tm.weaving.weaver.tmanalysis.mustalias;
import soot.PackManager;
import soot.Transform;


public class MustAliasMain {
    public static void main(String[] args) 
    {
//	if(args.length == 0)
//	{
//	    System.out.println("Syntax: java "+
//		"olhotak.mustalias.MustAliasMain mainClass "+
//		"[soot options]");
//	    System.exit(0);
//	}            

	PackManager.v().getPack("jtp").add(
	    new Transform("jtp.mustalias",
                new MustAliasTagger() ) );

	soot.Main.main(args);
    }
}


