package soot.jimple.toolkits.ide;

import static heros.debugsupport.SerializableEdgeData.EdgeKind.EDGE_FUNCTION;
import static heros.debugsupport.SerializableEdgeData.EdgeKind.JUMP_FUNCTION;
import heros.EdgeFunction;
import heros.debugsupport.NewEdgeSerializer;
import heros.debugsupport.SerializableEdgeData;

import java.io.ObjectOutputStream;
import java.util.NoSuchElementException;

import soot.Body;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnPosTag;

public class EdgeSerializer<D, V> extends NewEdgeSerializer<soot.SootMethod, D, soot.Unit, V> {

	public EdgeSerializer(ObjectOutputStream oos) {
		super(oos);
	}

	@Override
	public SerializableEdgeData serializeJumpFunction(SootMethod method, D sourceVal,Unit target, D targetVal, EdgeFunction<V> f) {
		return new SerializableEdgeData(JUMP_FUNCTION, method.getDeclaringClass().getName(), getLine(method), getCol(method), getLine(target), getCol(target), f.toString());
	}

	@Override
	public SerializableEdgeData serializeEdgeFunction(SootMethod sourceMethod, Unit source, D sourceVal, Unit target, D targetVal) {
		return new SerializableEdgeData(EDGE_FUNCTION, sourceMethod.getDeclaringClass().getName(), getLine(source), getCol(source), getLine(target), getCol(target), "");
	}

	private int getCol(SootMethod m) {
		if(m.hasActiveBody()) {
			Body b = m.getActiveBody();
			PatchingChain<Unit> units = b.getUnits();
			Unit u = units.getFirst();
			int col = getCol(u);
			while(col<0) {
				try{
					u = units.getSuccOf(u);
					col = getCol(u);
				} catch(NoSuchElementException e) {
					break;
				}
			}
			return col;
		}
		return -1;
	}
	
	private int getCol(Unit u) {
		SourceLnPosTag tag = (SourceLnPosTag) u.getTag("SourceLnPosTag");
		if(tag!=null) {
			return tag.startPos();
		}
		return -1;
	}

	private int getLine(SootMethod m) {
		if(m.hasActiveBody()) {
			Body b = m.getActiveBody();
			PatchingChain<Unit> units = b.getUnits();
			Unit u = units.getFirst();
			int line = getLine(u);
			while(line<0) {
				try{
					u = units.getSuccOf(u);
					line = getLine(u);
				} catch(NoSuchElementException e) {
					break;
				}
			}		
			return line;
		}
		return -1;
	}
	
	private int getLine(Unit u) {
		SourceLnPosTag tag = (SourceLnPosTag) u.getTag("SourceLnPosTag");
		if(tag!=null) {
			return tag.startLn();
		}
		LineNumberTag lnTag = (LineNumberTag) u.getTag("LineNumberTag");
		if(lnTag!=null) {
			return lnTag.getLineNumber(); 
		}
		return -1;
	}

}
