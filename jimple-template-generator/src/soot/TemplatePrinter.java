/* Soot - a J*va Optimization Framework
 * Copyright (C) 2010 Hela Oueslati
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

package soot;

import java.io.PrintWriter;

public class TemplatePrinter {
    
	private PrintWriter out;
	private int indentationLevel = 0;

	public TemplatePrinter(Singletons.Global g) {
    }
	
    public static TemplatePrinter v() {
        return G.v().soot_TemplatePrinter();
    }

    //see also class soot.Printer!
	public void printTo(SootClass c, PrintWriter out) {
		this.out = out;
		
		printTo(c);
	}

	private void printTo(SootClass c) {
		String templateClassName = c.getName().replace('.', '_')+"_Maker";
		
		//open class
		print("public class ");
		print(templateClassName);
		println(" {");

		//open main method
		newMethod("main");
		
		//close main method
		closeMethod();
	
		//close class
		println("}");
	}

	private void closeMethod() {
		println("}");
		unindent();
	}

	private void newMethod(String name) {
		indent();
		println("public void "+name+"() {");
	}
	
	private void println(String s) {
		print(s); print("\n");
	}

	private void print(String s) {
		for(int i=0; i<indentationLevel; i++) {
			out.print("  ");
		}
		out.print(s);
	}
	
	private void indent() {
		indentationLevel++;
	}

	private void unindent() {
		indentationLevel--;
	}
}
