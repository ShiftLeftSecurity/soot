package ca.mcgill.sable.soot;


import java.util.*;
import ca.mcgill.sable.soot.*;
import java.io.*;


public class Block 
{
    private Unit mHead, mTail;
    private UnitBody mBody;
    private List mPreds, mSuccessors;
    private int mPredCount = 0, mBlockLength = 0, mIndexInMethod = 0;
    private BlockGraph mBlockGraph;
    
    public Block(Unit aHead, Unit aTail, UnitBody aBody, int aIndexInMethod, int aBlockLength, BlockGraph aBlockGraph)
    {
	mHead = aHead;
	mTail = aTail;
	mBody = aBody;
	mIndexInMethod = aIndexInMethod;
	mBlockLength = aBlockLength;
	mBlockGraph = aBlockGraph;
    }

    public int getIndexInMethod()
    {
	return mIndexInMethod;
    }

   
    
    public String toBriefString()
    {
        return toString(true, buildMapForBlock(), "        ");
    }
    
    public String toBriefString(Map stmtToName)
    {
        return toString(true, stmtToName, "");
    }
    
    public String toBriefString(String indentation)
    {
        return toString(true, buildMapForBlock(), indentation);
    }




    
    public String toBriefString(Map stmtToName, String indentation)
    {
        return toString(true, stmtToName, indentation);
    }
    
    public String toString()
    {
        return toString(false, allMapToUnnamed, "");
    }
    
    public String toString(Map stmtToName)
    {
        return toString(false, stmtToName, "");
    }
    
    public String toString(String indentation)
    {
        return toString(false, allMapToUnnamed, indentation);
    }
    
    public String toString(Map stmtToName, String indentation)
    {
        return toString(false, stmtToName, indentation);
    }
    
    protected String toString(boolean isBrief, Map stmtToName, String indentation)
    {
	StringBuffer strBuf = new StringBuffer();

	
   
	/*
	strBuf.append(toShortString() + " of method " + mBody.getMethod().getName() + ".\n");
	strBuf.append("Head: " + mHead.toBriefString(stmtToName, indentation ) + '\n');
	strBuf.append("Tail: " + mTail.toBriefString(stmtToName, indentation) + '\n');
	strBuf.append("Predecessors: \n");*/
	
	strBuf.append("     block" + mIndexInMethod + ":\t\t\t\t\t[preds: ");
	// print out predecessors.
	int count = 0;
	if(mPreds != null) {
	    Iterator it = mPreds.iterator();
	    while(it.hasNext()) {
		
		strBuf.append(((Block) it.next()).getIndexInMethod()+ " ");
	    }
	}
	strBuf.append("] [succs: ");
	if(mSuccessors != null) {
	    Iterator it = mSuccessors.iterator();
	    while(it.hasNext()) {
		
		strBuf.append(((Block) it.next()).getIndexInMethod() + " ");
	    }
	    
	}
	    
	strBuf.append("]\n");
	

	
	//strBuf.append("     block" + mIndexInMethod + ":\n");

	Chain methodUnits = mBody.getUnits();
	Iterator basicBlockIt = methodUnits.iterator(mHead);
	
	if(basicBlockIt.hasNext()) {
	    Unit someUnit = (Unit) basicBlockIt.next();
	    strBuf.append(someUnit.toBriefString(stmtToName, indentation) + ";\n");
	    if(!isBrief) {
		while(basicBlockIt.hasNext()){
		    someUnit = (Unit) basicBlockIt.next();
		    if(someUnit == mTail)
			break;
		    strBuf.append(someUnit.toBriefString(stmtToName, indentation) + ";\n");	
		}
	    } else {
		if(mBlockLength > 1)
		    strBuf.append("          ...\n");
	}
	    someUnit = mTail;
	    if(mTail == null) 
		strBuf.append("error: null tail found; block length: " + mBlockLength +"\n");
	    else if(mHead != mTail)
		strBuf.append(someUnit.toBriefString(stmtToName, indentation) + ";\n");	
	

	}  else 
	    System.out.println("No basic blocks found; must be interface class.");
    
	return strBuf.toString();
    }



    public Unit getHead() 
    {
	return mHead;
    }
    
    public Unit getTail()
    {
	return mTail;
    }


    void setPreds(List preds)
    {
	mPreds = preds;
	return;
    }

    List getPreds()
    {
	return mPreds;
    }


    void setSuccessors(List successors)
    {
	mSuccessors = successors;
    }


    List getSuccessors()
    {
	return mSuccessors;
    }

    /*
    public String toBriefString() 
    {
	return "block #" + mIndexInMethod ;
    }
    
    public String toString() 
    {
	StringBuffer strBuf = new StringBuffer();
	
	strBuf.append(toBriefString() + " of method " + mBody.getMethod().getName() + ".\n");
	strBuf.append("Head: " + mHead.toBriefString() + '\n');
	strBuf.append("Tail: " + mTail.toBriefString() + '\n');
	strBuf.append("Predecessors: \n");
	
	// print out predecessors.
	int count = 0;
	if(mPreds != null) {
	    Iterator it = mPreds.iterator();
	    while(it.hasNext()) {
		
		strBuf.append(((Block) it.next()).toBriefString() + '\n');
	    }
	}
	return strBuf.toString();
    }
    */








    public String toShortString() {return "Block #" + mIndexInMethod; }
    



    private Map buildMapForBlock() 
    {
	Map m = new HashMap();
	List basicBlocks = mBlockGraph.getBlocks();
	Iterator it = basicBlocks.iterator();
	while(it.hasNext()) {
	    Block currentBlock = (Block) it.next();
	    m.put(currentBlock.getHead(),  "block" + (new Integer(currentBlock.getIndexInMethod()).toString()));
	}	
	return m;
    }


    static Map allMapToUnnamed = new AllMapTo("???");

    static class AllMapTo extends AbstractMap
    {
        Object dest;
        
        public AllMapTo(Object dest)
        {
            this.dest = dest;
        }
        
        public Object get(Object key)
        {
            return dest;
        }
        
        public Set entrySet()
        {
            throw new UnsupportedOperationException();
        }
    }
}
