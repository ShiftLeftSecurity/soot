/****************************************
 * 
 * Copyright (c) 2006, University of California, Berkeley.
 * All rights reserved.
 *
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 * - Redistributions of source code must retain the above copyright 
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright 
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the 
 *   distribution.
 * - Neither the name of the University of California, Berkeley nor the 
 *   names of its contributors may be used to endorse or promote 
 *   products derived from this software without specific prior written 
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ***************************************/ 

package edu.berkeley.pa.csdemand;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import manu.util.Predicate;

import edu.berkeley.pa.util.PagToDotDumper;

import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.FieldRefNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.VarNode;

/**
 * you can just add edges and then dump them as a dot graph
 * 
 * @author Manu Sridharan
 * 
 */
public class DotPointerGraph {

	private final Set<String> edges = new HashSet<String>();

	private final Set<Node> nodes = new HashSet<Node>();

	public void addAssign(VarNode from, VarNode to) {
		addEdge(to, from, "", "black");
	}

	private void addEdge(Node from, Node to, String edgeLabel, String color) {
		nodes.add(from);
		nodes.add(to);
		addEdge(PagToDotDumper.makeNodeName(from), PagToDotDumper
				.makeNodeName(to), edgeLabel, color);
	}

	private void addEdge(String from, String to, String edgeLabel, String color) {
		StringBuffer tmp = new StringBuffer();
		tmp.append("    ");
		tmp.append(from);
		tmp.append(" -> ");
		tmp.append(to);
		tmp.append(" [label=\"");
		tmp.append(edgeLabel);
		tmp.append("\", color=");
		tmp.append(color);
		tmp.append("];");
		edges.add(tmp.toString());
	}

	public void addNew(AllocNode from, VarNode to) {
		addEdge(to, from, "", "yellow");
	}

	public void addCall(VarNode from, VarNode to, Integer callSite) {
		addEdge(to, from, callSite.toString(), "blue");
	}

	public void addMatch(VarNode from, VarNode to) {
		addEdge(to, from, "", "brown");
	}

	public void addLoad(FieldRefNode from, VarNode to) {
		addEdge(to, from.getBase(), from.getField().toString(), "green");
	}

	public void addStore(VarNode from, FieldRefNode to) {
		addEdge(to.getBase(), from, to.getField().toString(), "red");
	}

	public int numEdges() {
		return edges.size();
	}
	
	public void dump(String filename) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileOutputStream(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// pw.println("digraph G {\n\trankdir=LR;");
		pw.println("digraph G {");
		Predicate<Node> falsePred = new Predicate<Node>() {

			@Override
			public boolean test(Node obj_) {
				return false;
			}

		};
		for (Node node : nodes) {
			pw.println(PagToDotDumper.translateLabel(node, falsePred));
		}
		for (String edge : edges) {
			pw.println(edge);
		}
		pw.println("}");
		pw.close();

	}
}
