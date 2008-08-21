package soot.jimple.toolkits.ctl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import soot.Unit;
import soot.jimple.toolkits.ctl.formula.Proposition;
import soot.jimple.toolkits.ctl.patterns.IPattern;

public class Predicate extends Proposition<Unit> {

	protected final String IDENTIFIER = "([a-zA-Z\\$]+[0-9]*)";
	protected final String IDENTITY_REF = "(@this|@parameter[0-9]+)";
	
	protected final Pattern pat;
	protected final List<Formal> formals;

	private final String name;
	
	public Predicate(String name, List<Formal> formals, String pattern) {
		this.name = name;
		this.formals = formals;
		pattern = " " + pattern;
		for (Formal formal : formals) {
			if(formal.getType().equals("Immediate")) {
				pattern = pattern.replace(" "+formal.getName()+" ", " "+IDENTIFIER+" ");
			} else if(formal.getType().equals("Local")) {
				pattern = pattern.replace(" "+formal.getName()+" ", " "+IDENTIFIER+" ");
			} else if(formal.getType().equals("IdentityRef")) {
				pattern = pattern.replace(" "+formal.getName()+" ", " "+IDENTITY_REF+" ");
			}
		}
		pat = Pattern.compile(pattern);		
	}

	@Override
	public Map<IPattern, Set<Object>> holdsIn(Unit u) {
		String unitString = " "+u.toString()+" ; ";
		Matcher matcher = pat.matcher(unitString);
		if(matcher.matches()) {
			System.err.println("Match for "+name+": "+u);
			HashMap<String, Set<String>> res = new HashMap<String, Set<String>>();
			for(int i=1;i<=matcher.groupCount();i++) {
				String formal = formals.get(i-1).name;
				Set<String> set = res.get(formal);
				if(set==null) {
					set = new HashSet<String>();
					res.put(formal, set);
				}
				set.add(matcher.group(i));
				System.err.println(formals.get(i-1).name+"->"+matcher.group(i));
			}
			return null;
		} else return null;
//		System.err.println(unitString);
//		System.err.println(matcher.pattern().pattern());
//		System.err.println();
	}

}
