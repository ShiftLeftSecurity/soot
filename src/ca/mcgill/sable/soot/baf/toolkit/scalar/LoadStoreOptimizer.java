package ca.mcgill.sable.soot.baf.toolkit.scalar;

import java.util.*;
import java.io.*;
import ca.mcgill.sable.soot.*;
import ca.mcgill.sable.soot.jimple.*;
import ca.mcgill.sable.soot.baf.*;



public class LoadStoreOptimizer 
{

    private static Chain mUnits;

    public static void optimizeLoadStores(UnitBody body) 
    {
	Map loadMap = new HashMap();

	Chain units  = body.getUnits();
	mUnits = units;
	System.out.println("invoked");
	System.out.println(body.getMethod().toString());
	
	Map stackHeight = new HashMap(units.size());
	{
	    Iterator it = units.iterator();
	
	    while(it.hasNext()) {
		Unit u = (Unit) it.next();
		stackHeight.put(u, new Integer(((AbstractInst) u).getNetCount()));
		if(u instanceof LoadInst) {
		    Object local = ((LoadInst)u).getLocal();
		    
		    Integer i = (Integer) loadMap.get(local);
		    if(i != null) {
			loadMap.put(local, new Integer(i.intValue()+1));
		    } else {
			loadMap.put(local, new Integer(1));
		    }    
		}
	    }
	
	}
	
	{

	    CompleteBlockGraph blockGraph = new CompleteBlockGraph(body);
	    List blocks = blockGraph.getBlocks();
	    
	    Iterator blockIt = blocks.iterator();
	    while(blockIt.hasNext() ) {
		
		Block block = (Block) blockIt.next();
		

		
		Iterator it = units.iterator(block.getHead(), block.getTail());
		List storeInstList = new ArrayList();
	    
		while(it.hasNext()) {
		    Unit u = (Unit) it.next();

		    System.out.println("Iterating:   " +block.getTail().toString() );
		    if( u == units.getSuccOf( block.getTail())) 
			System.out.println("bounded iterator failled");
		 
		    
		    if(u instanceof StoreInst ) {
			storeInstList.add(u);
		    } else if(u instanceof LoadInst) {
		    
			Integer loadCount = (Integer) loadMap.get(((LoadInst)u).getLocal());
			if( (loadCount == null || loadCount.intValue() == 1)) {
			    

			    Iterator storeIt = storeInstList.iterator();
			    while(storeIt.hasNext()) {
				StoreInst storeInst = (StoreInst) storeIt.next();
			    
				if(((LoadInst)u).getLocal() == storeInst.getLocal()) {
				    if(LoadStoreOptimizer.stackIndependent(storeInst, u, block)) {
					System.out.println("head: " +  block.getHead().toString()+ "   tail: " + block.getTail().toString());
					System.out.println(((LoadInst)u).getLocal().toString() + "   " + storeInst.getLocal().toString());
					System.out.println(storeInst.toString());
					units.remove(u);
					units.remove(storeInst);
					System.out.println("Store/Load elimination occurred.");
					break;
				    } 


				}
			    }
			
			
			}
		    }
		    
		}
	    }
	}
    }	
			
    private static boolean stackIndependent(Unit from, Unit to, Block block) 
    {

	int stackHeight = 0;
	Iterator it = mUnits.iterator(from);
	Unit currentInst;
	
	if(from == to) {
	    System.out.println("same");
	    return true;
	}
	/* check if from and to follow each other */
	currentInst = (Unit) it.next();
	if(currentInst != from)
	    throw new RuntimeException();
	
	currentInst = (Unit) it.next();
	if(currentInst == to){
	    System.out.println("together");
	    return true;
	}
	
	while(currentInst != to) {
	    stackHeight -= ((AbstractInst)currentInst).getInCount();

	    // if we ever go below return false
	    if(stackHeight < 0)
		return false;
	    else
		stackHeight += ((AbstractInst)currentInst).getOutCount();
	    
	    currentInst = (Unit) it.next();
	}


	
	if(stackHeight == 0) {
	    System.out.println("stack ind");
	    return true;     
	} else if (stackHeight == 1) {
	    it = mUnits.iterator(from, to);
	    Unit u = (Unit) it.next();
	    Unit unitToMove = null; 

	    while( u != to) {
	      if(((AbstractInst)u).getNetCount() == 1) {
		  if(u instanceof LoadInst) {
		    
		    unitToMove =u;
		    int innerStackHeight = 0;
		    Iterator it2 = mUnits.iterator(unitToMove, mUnits.getPredOf(to));

		    // get passed the instruction to move
		    it2.next();
		    
		    while(it2.hasNext()) {
		      Unit currentU = (Unit) it2.next();
		      innerStackHeight -= ((AbstractInst) currentU).getInCount();
		      
		      // in this case we CANNOT move the instruction has the following instruction depend on it
		      if(innerStackHeight < 0) {
			unitToMove = null;
			break;
		      } else {
			innerStackHeight += ((AbstractInst) currentU).getOutCount();
		      }
		    }
		    // if we found a unitToMove, stop looking for one and break out of loop
		    if(unitToMove != null)
		      break;
		    
		  }
			  
		    

		  



		  
		  else{System.out.println(">>> condition 1 in LoadStoreOptimizer has been attained");
		  }
		   
	       
	      }
	      u = (Unit) it.next();
	    }

	    if(unitToMove != null) {
		Unit current;
		int h = 0;
		current = unitToMove;
		boolean reachedStore = false;
		
		while( current != block.getHead()) {
		    current = (Unit) mUnits.getPredOf(current);

		    List boxes = current.getDefBoxes();
		    Iterator boxIt = boxes.iterator();
		    while(boxIt.hasNext()) {
			ValueBox vBox = (ValueBox) boxIt.next();
			Local v = (Local) vBox.getValue();
			if(((LoadInst)unitToMove).getLocal() == v) {
			    System.out.println(">>> condition 6 in LoadStoreOptimizer has been attained");
			    return false;
			}
			    
		    }
			
		    if(current == from)
			reachedStore = true;
		    h += -((AbstractInst)current).getNetCount();
		    if(h < 0 ){
			System.out.println(">>> condition 1 in LoadStoreOptimizer has been attained");
			return false;
		    }
		    if(h == 0 && reachedStore == true) {
			System.out.println("reordering bytecode...............");
			mUnits.insertBefore(unitToMove.clone(), current);
			mUnits.remove(unitToMove);
			return true;
		    }
		    System.out.println("looping");
		}
		
		System.out.println(">>> condition 5 in LoadStoreOptimizer has been attained");




		

	    } else {System.out.println(">>> condition 2 in LoadStoreOptimizer has been attained");}
	}
	return false;
    }





 /*
    public void loadStoreElimination() 
    {
	changed = false;
	while(!changed && it.hasNext()) {
	    Inst u = (Inst) it.next();
	    topo.setElementAt(u, stackHeight);
	    stackHeight += (u.getOutCount() - u.getInCount());
			
	    if(u instanceof StoreInst ) {
		storeInstList.add(u);
	    } else if(u instanceof LoadInst) {
			   
		Iterator storeIt = storeInstList.iterator();
		while(storeIt.hasNext()) {
		    StoreInst inst = (StoreInst) storeIt.next();
		    
		    if(((LoadInst)u).getLocal() == inst.getLocal()) {
				    
			/	int isUnique = 0;
			Iterator loadIt = c.iterator();
			while(loadIt.hasNext()) {
			    Inst i = (Inst) loadIt.next();
			    if(i instanceof LoadInst) {
				if(((LoadInst) i).getLocal() == ((LoadInst)u).getLocal())
				    isUnique++;
			    }
			    }/
				    
					
			//if((isUnique<2) && stackIndependent(inst,u, c)) {
			  
			if( !isLive(((LoadInst)u).getLocal()) && stackIndependent(inst, u, c)) {
			    changed = true;
			    c.remove(u);
			    c.remove(inst);
			    
			    // temporary while both HashChain and Lists are used 
			    unitList.remove(u);
			    unitList.remove(inst);
			    
			    // debug
			    System.out.println("Store/Load elimination occurred.");
			}
			
		    }
							 
		}
			    
			    
	    }
			
	}
	}*/
    /*
    public static boolean stackIndependent(Inst from, Inst to) 
    {
	HashChain c = mUnitBody;
	int stackHeight = 0;
	Iterator it = c.iterator(from);
	Inst currentInst;
	Vector loads = new Vector(8);

	if(from == to) 
	    return true;
	
	currentInst = (Inst) it.next();
	currentInst = (Inst) it.next();
	if(currentInst == to)
	    return true;
	
	while(currentInst != to) {
	    stackHeight -= currentInst.getInCount();
	    if(stackHeight < 0)
		return false;
	    else
		stackHeight += currentInst.getOutCount();

	    currentInst = (Inst) it.next();
	}
	if(stackHeight == 0)
	    return true;     
	else {
	    boolean result = false;
	    if(stackHeight == 1)
		stackHeight -= reorderInstructions(loads);
	    if(stackHeight != 0)
		result = true;
	    return result;
	}
    }

    */
    }
