/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Navindra Umanee
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package soot.shimple.internal;

import soot.*;
import soot.shimple.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.util.*;
import java.util.*;
import soot.toolkits.scalar.*;
import soot.toolkits.graph.*;

/**
 * Internal implementation of Phi nodes.
 *
 * @author Navindra Umanee
 * @see soot.shimple.PhiExpr
**/
public class SPhiExpr implements PhiExpr
{
    protected List argPairs;
    protected Type type;
    
    /**
     * Create a trivial Phi expression for leftLocal.  preds is an ordered
     * list of the control flow predecessor Blocks of the PhiExpr.
     **/
    public SPhiExpr(Local leftLocal, List preds)
    {
        type = leftLocal.getType();
        argPairs = new ArrayList();

        Iterator predsIt = preds.iterator();
        
        // *** TODO: Should we use something more restrictive than
        // *** RValueBox?
        while(predsIt.hasNext())
        {
            Block block = (Block) predsIt.next();
            addArg(leftLocal, block);
        }
    }

    /**
     * Create a Phi expression from the given list of Values and Blocks.
     **/
    public SPhiExpr(List args, List preds)
    {
        if(args.size() == 0)
            throw new RuntimeException("Arg list may not be empty");
        
        if(args.size() != preds.size())
            throw new RuntimeException("Arg list does not match Pred list");

        type = ((Value) args.get(0)).getType();
        argPairs = new ArrayList();
    
        Iterator argsIt = args.iterator();
        Iterator predsIt = preds.iterator();

        while(argsIt.hasNext()){
            Value arg = (Value) argsIt.next();
            Block block = (Block) predsIt.next();

            addArg(arg, block);
        }
    }

    /**
     * Create a Phi expression from the given list of argValues and
     * predUnits.
     * 
     * <p> Ugly, but sometimes we don't have a list of Blocks, only a
     * list of Units.  The value of the flag is ignored.  It is only
     * there to change the signature of the constructor.
     **/
    public SPhiExpr(List args, List preds, int onlyHaveUnitsFlag)
    {
        if(args.size() == 0)
            throw new RuntimeException("Arg list may not be empty");
        
        if(args.size() != preds.size())
            throw new RuntimeException("Arg list does not match Pred list");

        type = ((Value) args.get(0)).getType();
        argPairs = new ArrayList();
    
        Iterator argsIt = args.iterator();
        Iterator predsIt = preds.iterator();

        while(argsIt.hasNext()){
            Value arg = (Value) argsIt.next();
            Unit pred = (Unit) predsIt.next();

            addArg(arg, pred);
        }
    }
    
    public Value getValueArg(int index)
    {
        return ((ValueUnitPair)argPairs.get(index)).getValue();
    }

    public Value getValueArg(Block pred)
    {
        return getValueArg(pred.getTail());
    }

    public Value getValueArg(Unit predTailUnit)
    {
        // *** hmmm, expensive implementation

        Iterator argPairsIt = argPairs.iterator();

        while(argPairsIt.hasNext()){
            ValueUnitPair vu = (ValueUnitPair) argPairsIt.next();
            if((vu.getUnit()).equals(predTailUnit))
                return vu.getValue();
        }

        return null;
    }
    
    public Unit getPredArg(int index)
    {
        return ((ValueUnitPair)argPairs.get(index)).getUnit();
    }
    
    public List getValueArgs()
    {
        List args = new ArrayList();
        
        Iterator argPairsIt = argPairs.iterator();

        while(argPairsIt.hasNext()){
            Value arg = ((ValueUnitPair)argPairsIt.next()).getValue();

            args.add(arg);
        }
        
        return args;
    }

    public List getPredArgs()
    {
        List preds = new ArrayList();
        
        Iterator argPairsIt = argPairs.iterator();

        while(argPairsIt.hasNext()){
            Unit arg = ((ValueUnitPair)argPairsIt.next()).getUnit();

            preds.add(arg);
        }
        
        return preds;
    }

    public int getArgIndex(Block pred)
    {
        return getArgIndex(pred.getTail());
    }

    public int getArgIndex(Unit predTailUnit)
    {
        // *** hmmm, expensive implementation

        int count = 0;
        Iterator argPairsIt = argPairs.iterator();

        while(argPairsIt.hasNext()){
            ValueUnitPair vu = (ValueUnitPair) argPairsIt.next();
            if((vu.getUnit()).equals(predTailUnit))
                return count;
            count++;
        }

        return -1;
    }

    public int getArgCount()
    {
        return argPairs.size();
    }

    public boolean setArg(int index, Value arg, Block pred)
    {
        return setArg(index, arg, pred.getTail());
    }
    
    public boolean setArg(int index, Value arg, Unit predTailUnit)
    {
        ValueUnitPair argPair = (ValueUnitPair) argPairs.get(index);

        if(argPair == null)
            return false;

        argPair.setValue(arg);

        // remove old back pointer
        Unit oldPredTailUnit = argPair.getUnit();
        oldPredTailUnit.removeBoxPointingToThis(argPair);
            
        predTailUnit.addBoxPointingToThis(argPair);
        argPair.setUnit(predTailUnit);
        return true;
    }
    
    
    public boolean setValueArg(int index, Value arg)
    {
        ValueUnitPair argPair = (ValueUnitPair) argPairs.get(index);

        if(argPair == null)
            return false;

        argPair.setValue(arg);
        return true;
    }

    public boolean setPredArg(int index, Block pred)
    {
        return setPredArg(index, pred.getTail());
    }
    
    public boolean setPredArg(int index, Unit predTailUnit)
    {
        ValueUnitPair argPair = (ValueUnitPair) argPairs.get(index);

        // boolean canDo = argBox.canContainValue(arg);

        if(argPair == null)
            return false;

        // remove old back pointer
        Unit oldPredTailUnit = argPair.getUnit();
        oldPredTailUnit.removeBoxPointingToThis(argPair);

        predTailUnit.addBoxPointingToThis(argPair);
        argPair.setUnit(predTailUnit);
        return true;
    }
    
    public boolean removeArg(int index)
    {
        ValueUnitPair valueUnit = (ValueUnitPair) argPairs.remove(index);
        
        if(valueUnit == null)
            return false;
        
        // remove old back pointer
        Unit pred = valueUnit.getUnit();
        pred.removeBoxPointingToThis(valueUnit);

        return true;
    }

    public boolean removeArg(ValueUnitPair arg)
    {
        if(argPairs.remove(arg)){
            arg.getUnit().removeBoxPointingToThis(arg);
            return true;
        }
        
        return false;
    }

    public void addArg(Value arg, Block pred)
    {
        addArg(arg, pred.getTail());
    }
    
    public void addArg(Value arg, Unit predTailUnit)
    {
        ValueUnitPair vup = new ValueUnitPair(arg, predTailUnit);
        vup.setBranchTarget(false);
        predTailUnit.addBoxPointingToThis(vup);
        argPairs.add(vup);
    }

    
    
    public List getArgs()
    {
        return (List) ((ArrayList) argPairs).clone();
    }
    
    public ValueUnitPair getArgBox(int index)
    {
        return (ValueUnitPair) argPairs.get(index);
    }

    public boolean equivTo(Object o)
    {
        if(o instanceof SPhiExpr){
            SPhiExpr pe = (SPhiExpr) o;

            if(argPairs.size() != pe.getArgCount())
                return false;

            for(int i = 0; i < argPairs.size(); i++){
                if(!getArgBox(i).equals(pe.getArgBox(i)))
                    return false;
            }

            return true;
        }

        return false;
    }

    public int equivHashCode()
    {
        // *** TODO: Do we need this?
        
        throw new RuntimeException("Not Yet Implemented");
    }

    public String toString()
    {
        return toString(null);
    }
    
    public String toString(Map stmtToName)
    {
        StringBuffer expr = new StringBuffer("Phi(");

        Iterator argPairsIt = argPairs.iterator();
        while(argPairsIt.hasNext()){
            ValueUnitPair vuPair = (ValueUnitPair)argPairsIt.next();
            Value arg = vuPair.getValue();
            expr.append(arg.toString());

            if(stmtToName != null){
                Unit pred = vuPair.getUnit();
                expr.append(" #" + stmtToName.get(pred));
            }
            
            if(argPairsIt.hasNext())
                expr.append(", ");
        }

        expr.append(")");

        return expr.toString();
    }

    public String toBriefString()
    {
        return toBriefString(null);
    }
    
    public String toBriefString(Map stmtToName)
    {
        StringBuffer expr = new StringBuffer("Phi(");

        Iterator argPairsIt = argPairs.iterator();
        while(argPairsIt.hasNext()){
            ValueUnitPair vuPair = (ValueUnitPair)argPairsIt.next();
            Value arg = vuPair.getValue();
            expr.append(((ToBriefString)arg).toBriefString());

            if(stmtToName != null){
                Unit pred = vuPair.getUnit();
                expr.append(" #" + stmtToName.get(pred));
            }
            
            if(argPairsIt.hasNext())
                expr.append(", ");
        }

        expr.append(")");

        return expr.toString();
    }

    public List getUnitBoxes()
    {
        return argPairs;
    }

    public List getUseBoxes()
    {
        Set set = new HashSet();

        Iterator argPairsIt = argPairs.iterator();

        while(argPairsIt.hasNext()){
            ValueUnitPair argPair = (ValueUnitPair) argPairsIt.next();
            
            set.addAll(argPair.getValue().getUseBoxes());
            set.add(argPair);
        }

        return new ArrayList(set);
    }

    public Type getType()
    {
        return type;
    }
    
    public void apply(Switch sw)
    {
        ((ShimpleExprSwitch) sw).casePhiExpr(this);
    }

    public Object clone()
    {
        return new SPhiExpr(getValueArgs(), getPredArgs(), 0);
    }
}
