package soot.jimple.toolkits.pointer;
import soot.tagkit.*;
import java.util.*;

public class DependenceAttribute extends CodeAttribute {

    public DependenceAttribute( List units, List dependenceTags ) {
	super( "DependenceAttribute", units, dependenceTags );
    }

    public String getJasminValue( Map instToLabel ) {
	StringBuffer buf = new StringBuffer();
	if (mTags.size() != mUnits.size())
	    throw new RuntimeException("Sizes must match!");

	Iterator tagIt = mTags.iterator();
	Iterator unitIt = mUnits.iterator();

	while (tagIt.hasNext())
	{
	    Object unit = unitIt.next();
	    Object tag = tagIt.next();

	    buf.append("%"+instToLabel.get(unit) + "%"+
		new String(Base64.encode(
			((DependenceTag)tag).getValue(instToLabel))));
	}

	return buf.toString();

    }
}
