/* Soot - a J*va Optimization Framework
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

package soot.toolkits.scalar;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.IdentityUnit;
import soot.Local;
import soot.Timers;
import soot.Trap;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.options.Options;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalGraph;
import soot.toolkits.graph.ExceptionalGraph.ExceptionDest;
import soot.toolkits.graph.UnitGraph;

/**
 * Analysis that provides an implementation of the LocalDefs interface.
 */
public class ExtendedLocalDefs implements GeneralDefs {
  	static class ObjectWrapper {
  		private final Object object;

  		ObjectWrapper(Object object) {
  			this.object = object;
		}

		@Override
		public int hashCode() {
  		  	if (object instanceof FieldRef) {
  		  		FieldRef fieldRef = (FieldRef) object;
  		  		int hashCode = fieldRef.getField().hashCode();

				if(object instanceof InstanceFieldRef) {
					InstanceFieldRef instanceFieldRef = (InstanceFieldRef) object;
					hashCode += instanceFieldRef.getBase().hashCode();
				}
				return hashCode;
			}
			return object.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof ObjectWrapper) {
				ObjectWrapper otherWrapper = (ObjectWrapper) other;
				if (object.getClass() == otherWrapper.object.getClass() &&
						object instanceof FieldRef) {
					boolean equal;
					FieldRef fieldRef = (FieldRef) object;
					FieldRef otherFieldRef = (FieldRef) otherWrapper.object;
					equal = fieldRef.getField().equals(otherFieldRef.getField());

					if (equal && object instanceof InstanceFieldRef) {
						InstanceFieldRef instanceFieldRef= (InstanceFieldRef) object;
						InstanceFieldRef otherIntanceFieldRef = (InstanceFieldRef) otherWrapper.object;
						equal = instanceFieldRef.getBase().equals(otherIntanceFieldRef.getBase());
					}
					return equal;
				}
				return object.equals(otherWrapper.object);
			}
			else {
				return false;
			}
		}

	}
	static private class StaticSingleAssignment implements GeneralDefs {
		final Map<ObjectWrapper, List<Unit>> result;

		StaticSingleAssignment(Object[] trackables, List<Unit>[] unitList) {
			assert trackables.length == unitList.length;

			final int N = trackables.length;
			result = new HashMap<>((N * 3) / 2 + 7);

			for (int i = 0; i < N; i++) {
				if (unitList[i].isEmpty())
					continue;
				assert unitList[i].size() == 1;
				result.put(new ObjectWrapper(trackables[i]), unitList[i]);
			}
		}

		@Override
		public List<Unit> getDefsOfAt(Object object, Unit s) {
			List<Unit> lst = result.get(new ObjectWrapper(object));
			if (lst == null)
				return emptyList();

			// singleton-lists are immutable
			return lst;
		}

		@Override
		public List<Unit> getDefsOf(Object object) {
			return getDefsOfAt(object, null);
		}

	}

	static private class FlowAssignment extends
			ForwardFlowAnalysis<Unit, FlowAssignment.FlowBitSet> implements
			GeneralDefs {
		class FlowBitSet extends BitSet {
			private static final long serialVersionUID = -8348696077189400377L;

			FlowBitSet() {
				super(universe.length);
			}

			List<Unit> asList(int fromIndex, int toIndex) {
				BitSet bits = this;
				if (universe.length < toIndex || toIndex < fromIndex
						|| fromIndex < 0)
					throw new IndexOutOfBoundsException();

				if (fromIndex == toIndex) {
					return emptyList();
				}

				if (fromIndex == toIndex - 1) {
					if (bits.get(fromIndex)) {
						return singletonList(universe[fromIndex]);
					}
					return emptyList();
				}

				int i = bits.nextSetBit(fromIndex);
				if (i < 0 || i >= toIndex)
					return emptyList();

				if (i == toIndex - 1)
					return singletonList(universe[i]);

				List<Unit> elements = new ArrayList<Unit>(toIndex - i);

				for (;;) {
					int endOfRun = Math.min(toIndex, bits.nextClearBit(i + 1));
					do {
						elements.add(universe[i++]);
					} while (i < endOfRun);
					if (i >= toIndex)
						break;
					i = bits.nextSetBit(i + 1);
					if (i < 0 || i >= toIndex)
						break;
				}
				return elements;
			}
		}

		final Map<ObjectWrapper, Integer> trackables;
		final List<Unit>[] unitList;
		final int[] localRange;
		final Unit[] universe;

		private Map<Unit, Integer> indexOfUnit;

		FlowAssignment(DirectedGraph<Unit> graph, Object[] trackables,
				List<Unit>[] unitList, int units, boolean omitSSA) {
			super(graph);

			final int N = trackables.length;

			this.trackables = new HashMap<>((N * 3) / 2 + 7);
			this.unitList = unitList;

			universe = new Unit[units];
			indexOfUnit = new HashMap<Unit, Integer>(units);

			localRange = new int[N + 1];
			for (int j = 0, i = 0; i < N; localRange[++i] = j) {
				if (unitList[i].isEmpty())
					continue;

				this.trackables.put(new ObjectWrapper(trackables[i]), i);

				if (unitList[i].size() >= 2) {
					for (Unit u : unitList[i]) {
						indexOfUnit.put(u, j);
						universe[j++] = u;
					}
				} else if (omitSSA) {
					universe[j++] = unitList[i].get(0);
				}
			}
			assert localRange[N] == units;

			doAnalysis();

			indexOfUnit.clear();
			indexOfUnit = null;
		}

		@Override
		public List<Unit> getDefsOfAt(Object object, Unit s) {
			Integer lno = trackables.get(new ObjectWrapper(object));
			if (lno == null)
				return emptyList();

			int from = localRange[lno];
			int to = localRange[lno + 1];
			assert from <= to;

			if (from == to) {
				assert unitList[lno].size() == 1;
				// both singletonList is immutable
				return unitList[lno];
			}

			return getFlowBefore(s).asList(from, to);
		}

		@Override
		protected boolean omissible(Unit u) {
			// avoids temporary creation of iterators (more like micro-tuning)
			if (u.getDefBoxes().isEmpty())
				return true;
			for (ValueBox vb : u.getDefBoxes()) {
				Value v = vb.getValue();
				if (isOfTrackedType(v)) {
					int lno = trackables.get(new ObjectWrapper(v));
					return (localRange[lno] == localRange[lno + 1]);
				}
			}
			return true;
		}

		@Override
		protected Flow getFlow(Unit from, Unit to) {
			// QND
			if (to instanceof IdentityUnit) {
				if (graph instanceof ExceptionalGraph) {
					ExceptionalGraph<Unit> g = (ExceptionalGraph<Unit>) graph;
					if (!g.getExceptionalPredsOf(to).isEmpty()) {
						// look if there is a real exception edge
						for (ExceptionDest<Unit> exd : g
								.getExceptionDests(from)) {
							Trap trap = exd.getTrap();
							if (null == trap)
								continue;

							if (trap.getHandlerUnit() == to)
								return Flow.IN;
						}
					}
				}
			}
			return Flow.OUT;
		}

		@Override
		protected void flowThrough(FlowBitSet in, Unit unit, FlowBitSet out) {
			copy(in, out);

			// reassign all definitions
			for (ValueBox vb : unit.getDefBoxes()) {
				Value v = vb.getValue();
				if (isOfTrackedType(v)) {
					int lno = trackables.get(new ObjectWrapper(v));

					int from = localRange[lno];
					int to = localRange[1 + lno];

					if (from == to)
						continue;

					assert from <= to;

					if (to - from == 1) {
						// special case: this local has only one def point
						out.set(from);
					} else {
						out.clear(from, to);
						out.set(indexOfUnit.get(unit));
					}
				}
			}
		}

		@Override
		protected void copy(FlowBitSet source, FlowBitSet dest) {
			if (dest == source)
				return;
			dest.clear();
			dest.or(source);
		}

		@Override
		protected FlowBitSet newInitialFlow() {
			return new FlowBitSet();
		}

		@Override
		protected void mergeInto(Unit succNode, FlowBitSet inout, FlowBitSet in) {
			inout.or(in);
		}

		@Override
		protected void merge(FlowBitSet in1, FlowBitSet in2, FlowBitSet out) {
			throw new UnsupportedOperationException("should never be called");
		}

		@Override
		public List<Unit> getDefsOf(Object object) {
			List<Unit> defs = new ArrayList<Unit>();
			for (Unit u : graph) {
				List<Unit> defsOf = getDefsOfAt(object, u);
				if (defsOf != null)
					defs.addAll(defsOf);
			}
			return defs;
		}
		
	}

	private static boolean isOfTrackedType(Value v) {
		return v instanceof Local || v instanceof FieldRef;
	}

	private GeneralDefs def;
	private HashMap<ObjectWrapper, Integer> numberHash = new HashMap<>();

	/**
	 * 
	 * @param graph
	 */
	public ExtendedLocalDefs(UnitGraph graph) {
		this(graph, FlowAnalysisMode.Automatic);
	}

	public ExtendedLocalDefs(UnitGraph graph, FlowAnalysisMode mode) {
		this(graph, graph.getBody().getLocals(), mode);
	}

	ExtendedLocalDefs(UnitGraph graph, Collection<Local> locals,
			FlowAnalysisMode mode) {
		this(graph, locals.toArray(new Local[locals.size()]), mode);
	}

	ExtendedLocalDefs(UnitGraph graph, Local[] locals, boolean omitSSA) {
		this(graph, locals, omitSSA ? FlowAnalysisMode.OmitSSA : FlowAnalysisMode.Automatic);
	}

	/**
	 * The different modes in which the flow analysis can run
	 */
	enum FlowAnalysisMode {
		/**
		 * Automatically detect the mode to use
		 */
		Automatic,
		/**
		 * Never use the SSA form, even if the unit graph would allow for a
		 * flow-insensitive analysis without losing precision
		 */
		OmitSSA,
		/**
		 * Always conduct a flow-insensitive analysis
		 */
		FlowInsensitive
	}
	
	ExtendedLocalDefs(UnitGraph graph, Local[] locals, FlowAnalysisMode mode) {
		if (Options.v().time())
			Timers.v().defsTimer.start();

		List<Object> fieldRefs = getFieldRefs(graph);
		Object[] trackables = new Object[locals.length + fieldRefs.size()];
		int pos;
		for (pos = 0; pos < locals.length; pos++) {
			trackables[pos] = locals[pos];
		}
		for (Iterator<Object> instanceFieldRefIter = fieldRefs.iterator(); pos < trackables.length; pos++) {
			trackables[pos] = instanceFieldRefIter.next();
		}

		int number = 0;
		for (Object object : trackables) {
			numberHash.put(new ObjectWrapper(object), number);
			number++;
		}

		init(graph, trackables, mode);

		if (Options.v().time())
			Timers.v().defsTimer.end();
	}

	private void init(DirectedGraph<Unit> graph, Object[] trackables, FlowAnalysisMode mode) {
	  	// Stores for each local in which units it is defined.
		@SuppressWarnings("unchecked")
		List<Unit>[] unitList = (List<Unit>[]) new List[trackables.length];

		Arrays.fill(unitList, emptyList());

		//boolean omitSSA = mode == FlowAnalysisMode.OmitSSA;
		boolean doFlowAnalsis = false;

		int units = 0;

		// collect all def points
		for (Unit unit : graph) {
			for (ValueBox box : unit.getDefBoxes()) {
				Value v = box.getValue();
				if (isOfTrackedType(v)) {
					int lno = numberHash.get(new ObjectWrapper(v));

					switch (unitList[lno].size()) {
					case 0:
						unitList[lno] = singletonList(unit);
						//if (omitSSA)
						//	units++;
						break;
					case 1:
						//if (!omitSSA)
							units++;
						unitList[lno] = new ArrayList<Unit>(unitList[lno]);
						doFlowAnalsis = true;
						// fallthrough
					default:
						unitList[lno].add(unit);
						units++;
						break;
					}
				}
			}
		}

		if (doFlowAnalsis && mode != FlowAnalysisMode.FlowInsensitive) {
			def = new FlowAssignment(graph, trackables, unitList, units, false);
		} else {
			def = new StaticSingleAssignment(trackables, unitList);
		}
	}

	@Override
	public List<Unit> getDefsOfAt(Object object, Unit s) {
		return def.getDefsOfAt(object, s);
	}

	@Override
	public List<Unit> getDefsOf(Object object) {
		return def.getDefsOf(object);
	}

	private List<Object> getFieldRefs(UnitGraph graph) {
		List<Object> instanceFieldRefs = new LinkedList<>();
		for (ValueBox valueBox : graph.getBody().getDefBoxes())
		{
			if (valueBox.getValue() instanceof FieldRef) {
				FieldRef fieldRef = (FieldRef) valueBox.getValue();
				instanceFieldRefs.add(fieldRef);
			}
		}
		return instanceFieldRefs;
	}
}
