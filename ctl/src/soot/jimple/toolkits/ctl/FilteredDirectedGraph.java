package soot.jimple.toolkits.ctl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.jimple.toolkits.ctl.formula.Formula;
import soot.jimple.toolkits.ctl.formula.IFormula;
import soot.tagkit.Host;
import soot.toolkits.graph.DirectedGraph;

public class FilteredDirectedGraph<N extends Host> implements DirectedGraph<N> {
	
	protected DirectedGraph<N> delegate;
	protected IFormula f;
	protected transient List<N> heads, tails, all;
	
	public FilteredDirectedGraph(DirectedGraph<N> delegate, IFormula child) {
		super();
		this.delegate = delegate;
		this.f = child;
	}

	public List<N> getHeads() {
		if(heads==null) {
			heads = new ArrayList<N>(delegate.getHeads());
			filter(heads);
		}
		return heads;
	}

	public List<N> getPredsOf(N n) {
		List<N> preds = new ArrayList<N>(delegate.getPredsOf(n));
		filter(preds);
		return preds;
	}

	public List<N> getSuccsOf(N n) {
		List<N> succs = new ArrayList<N>(delegate.getSuccsOf(n));
		filter(succs);
		return succs;
	}

	public List<N> getTails() {
		if(tails==null) {
			tails = new ArrayList<N>(delegate.getTails());
			filter(tails);
		}
		return tails;
	}

	public Iterator<N> iterator() {
		if(all==null) {
			all = new ArrayList<N>();
			for (N n : delegate) {
				if(Formula.taggedWith(f,n))
					all.add(n);
			}
		}
		return all.iterator();
	}

	public int size() {
		return delegate.size();
	}

	protected void filter(List<N> list) {
		for (Iterator<N> iter = list.iterator(); iter.hasNext();) {
			N n = iter.next();
			if(!Formula.taggedWith(f, n)) {
				iter.remove();
			}
		}
	}
}
