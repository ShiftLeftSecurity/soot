/*
 * Created on 7-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.ds;

/**
 * A {@link MRUCache} which deletes entries as the memory becomes full.
 *
 * @author Eric Bodden
 */
public class MemoryStableMRUCache extends MRUCache {
	
	protected final long memoryToKeepFree;
	
	protected final Runtime rt = Runtime.getRuntime();
	
	protected String label = null;

	public MemoryStableMRUCache(String label, long memoryToKeepFree, boolean compareOnIdentity) {
		super(Integer.MAX_VALUE,compareOnIdentity);
		this.memoryToKeepFree = memoryToKeepFree;
		this.label = label;
	}
	
	public MemoryStableMRUCache(long bytesToKeepFree, boolean compareOnIdentity) {
		this(null,bytesToKeepFree,compareOnIdentity);
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void touchItem(Object key) {
        mruList.remove(key);
        mruList.addFirst(key);

        if ((rt.maxMemory() - rt.freeMemory()) < memoryToKeepFree) {
            cache.remove(mruList.removeLast());
            if(!warned) {
            	warned=true;
            	System.out.println("MRUCache "+(label==null?"":("'"+label+"' "))+"is now full.");
            }
        }
	}
	
	public String toString() {
		return cache.toString();
	}
	
	private boolean warned = false; 

}
