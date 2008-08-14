package soot.jimple.toolkits.ctl;

public class Formal {
	
	protected String name;
	protected String type;
	
	public Formal(String name, String type) {
		this.name = name;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return type + " " + name;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

}
