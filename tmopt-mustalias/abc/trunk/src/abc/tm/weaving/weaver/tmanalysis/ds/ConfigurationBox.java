/*
 * Created on 7-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.ds;


/**
 * ConfigurationBox
 *
 * @author Eric Bodden
 */
public class ConfigurationBox {
	
	protected Configuration config;
	
	public void set(Configuration c) {
		config=c;
	}
	
	public Configuration get() {
		return config;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "Box<"+config.toString()+">";
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((config == null) ? 0 : config.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ConfigurationBox other = (ConfigurationBox) obj;
		if (config == null) {
			if (other.config != null)
				return false;
		} else if (!config.equals(other.config))
			return false;
		return true;
	}
	
	public boolean isEmpty() {
		return config==null;
	}
	

}
