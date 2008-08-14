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

public class Predicate extends Proposition<Unit> {

	protected final String IDENTIFIER = "([a-zA-Z\\$]+[0-9]*)";
	
	protected final Pattern pat;
	protected final List<Formal> formals;
	
	public Predicate(List<Formal> formals, String pattern) {
		this.formals = formals;
//FIXME escaping		
//		System.err.println();
//		System.err.print(pattern);
//		System.err.println("  "+formals);
		pattern = " " + pattern;
		pattern = pattern.replace("??", "[^=]*");
		pattern = pattern.replace("?", "[^ ]*");
		pattern = pattern.replace(" = ", " (?=|:=) ");
		for (Formal formal : formals) {
			pattern = pattern.replace(" "+formal.getName()+" ", " "+IDENTIFIER+" ");
		}
//		System.err.println(pattern);
		pat = Pattern.compile(pattern);
	}
	
	@Override
	public Map<String, Set<String>> holdsIn(Unit u) {
		String unitString = " "+u.toString()+" ; ";
		Matcher matcher = pat.matcher(unitString);
		if(matcher.matches()) {
			HashMap<String, Set<String>> res = new HashMap<String, Set<String>>();
			for(int i=1;i<=matcher.groupCount();i++) {
				String formal = formals.get(i-1).name;
				Set<String> set = res.get(formal);
				if(set==null) {
					set = new HashSet<String>();
					res.put(formal, set);
				}
				set.add(matcher.group(i));
					//System.err.println(formals.get(i-1).name+"->"+matcher.group(i));
			}
			return res;
		} else return null;
//		System.err.println(unitString);
//		System.err.println(matcher.pattern().pattern());
//		System.err.println();
	}

}
