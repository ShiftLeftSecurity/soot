package soot.jimple.toolkits.ctl;

public class Matching {
	
	static String stmts[] = { 
    		"r0 := @this: soot.Main;",
    		"r1 := @parameter0: soot.Singletons$Global;",
            "specialinvoke r0.<java.lang.Object: void <init>(int,int)>(r2,$r3);",
            "r0.<soot.Main: java.lang.String versionString> = \"2.3.0\";",
            "$r2 = newarray (java.lang.String)[0];",
            "r0.<soot.Main: java.lang.String[] cmdLineArgs> = $r2;",
            "return;"
    };

    static String patterns[] = { 
    		"r0 := @this: soot.Main;",
    		"r1 := @parameter0: soot.Singletons$Global;",
            ".*invoke .*\\(.*IDENTIFIER.*\\);",
            "r0.<soot.Main: java.lang.String versionString> = \"2.3.0\";",
            "\\$r2 = newarray \\(java.lang.String\\)\\[0\\];",
            "r0.<soot.Main: java.lang.String\\[\\] cmdLineArgs> = \\$r2;",
            "return;"
    };
    
    protected final static String IDENTIFIER = "([a-zA-Z\\$]+[0-9]*|@this|@parameter[0-9]+)";

    protected final static String WILDCARD = "[a-zA-Z\\$]+[0-9]*|@this|@parameter[0-9]+";

    public static void main(String[] args) {
		for (int i = 0; i < patterns.length; i++) {
			String pat = patterns[i];
			//pat = "\\Q"+pat+"\\E";
			pat = pat.replace("IDENTIFIER", IDENTIFIER);
			pat = pat.replace("ANY", WILDCARD);
			System.out.println(pat);
			System.out.println(i+": "+stmts[i].matches(pat));
		}
	}
    
    
    
    
	
	
	

}
