package soot.jimple.toolkits.pointer;


import soot.*;
import java.util.*;

import soot.tagkit.*;

public class DependenceTagAggregator implements TagAggregator
{    
    private boolean status = false;
    private List tags = new LinkedList();
    private List units = new LinkedList();

    public DependenceTagAggregator(boolean active)
    {
	this.status = active;
    }

    public boolean isActive()
    {
	return this.status;
    }

  /** Clears accumulated tags. */
    public void refresh()
    {
        tags.clear();
	units.clear();
    }

  /** Adds a new (unit, tag) pair. */
    public void aggregateTag(Tag t, Unit u)
    {
	DependenceTag dt = (DependenceTag) t;
	units.add(u);
	tags.add(dt);
    }
    
  /** Returns a CodeAttribute with all tags aggregated. */ 
    public Tag produceAggregateTag()
    {
	if(units.size() == 0)
	    return null;
	else
	    return new DependenceAttribute( new LinkedList(units), 
				     new LinkedList(tags));
    }
}







