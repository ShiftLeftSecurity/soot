package soot.jimple.toolkits.ctl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Transformer;
import soot.Unit;
import soot.Singletons.Global;
import soot.jimple.toolkits.ctl.formula.EG;
import soot.jimple.toolkits.ctl.formula.Proposition;
import soot.jimple.toolkits.ctl.predsparser.analysis.DepthFirstAdapter;
import soot.jimple.toolkits.ctl.predsparser.lexer.Lexer;
import soot.jimple.toolkits.ctl.predsparser.lexer.LexerException;
import soot.jimple.toolkits.ctl.predsparser.node.AFormal;
import soot.jimple.toolkits.ctl.predsparser.node.AFormalsPred;
import soot.jimple.toolkits.ctl.predsparser.node.AGroundPred;
import soot.jimple.toolkits.ctl.predsparser.node.AMultiFormalList;
import soot.jimple.toolkits.ctl.predsparser.node.ASingleFormalList;
import soot.jimple.toolkits.ctl.predsparser.node.Start;
import soot.jimple.toolkits.ctl.predsparser.parser.Parser;
import soot.jimple.toolkits.ctl.predsparser.parser.ParserException;
import soot.options.Options;
import soot.tagkit.Host;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.EscapedReader;

public class CTLTransformer extends BodyTransformer {

	protected boolean initialized = false;

	protected void init() {
		if(initialized) return;
		initialized = true;
		
		if(Options.v().tr()!=null && !Options.v().tr().isEmpty()) {
			String fileName = Options.v().tr();
			File trFile = new File(fileName);
			Parser p;
			try {
				BufferedReader in = new BufferedReader(
				          new InputStreamReader(new FileInputStream(trFile)));
				
				String line;
				while((line=in.readLine())!=null) {
					String trimmed = line.trim().replaceAll(" ", "");
					if(trimmed.equals("Transformations:")) break;
					else if(trimmed.equals("Predicates:")) continue;
					else if(trimmed.equals("")) continue;
					
					Scanner s = new Scanner(line).useDelimiter("[\\(\\) :]");
					try {
						String NAME = "[a-zA-Z]+";
						String name = s.next(NAME);
						s.skip("\\(");
						Pattern formalPat = Pattern.compile(NAME+" "+NAME);
						List<Formal> formals = new ArrayList<Formal>();
						while(s.hasNext(formalPat)) {
							String formal = s.next(formalPat);
							String[] typeAndName = formal.split(" ");
							formals.add(new Formal(typeAndName[0],typeAndName[1]));
							if(s.hasNext(","))
								s.skip(",");
						}
						s.skip("\\)[ ]*:");						
						System.err.println(s.next(".*"));
					} catch(NoSuchElementException e) {
						System.err.println(e);
					}
				}
				
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Could not find file "+trFile.getAbsolutePath(),e);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected List<Predicate> preds = new ArrayList<Predicate>();

	protected List<Transformation> trans = new ArrayList<Transformation>();
	
	private void process(Start tree) {		
		tree.apply(new DepthFirstAdapter() {

			List<Formal> formals;
			
			@Override
			public void caseAGroundPred(AGroundPred node) {
				preds.add(new Predicate(node.getIdentifier().getText().trim(),Collections.<Formal>emptyList(),node.getAny().getText().trim()));
				super.caseAGroundPred(node);
			}
			
			@Override
			public void caseAFormalsPred(AFormalsPred node) {
				preds.add(new Predicate(node.getIdentifier().getText().trim(),formals,node.getAny().getText().trim()));
				super.caseAFormalsPred(node);
			}
			
			@Override
			public void caseAMultiFormalList(AMultiFormalList l) {
				AFormal formal = (AFormal) l.getFormal();
				formals.add(new Formal(formal.getIdentifier().getText().trim(), formal.getMetaType().getText().trim()));
				super.caseAMultiFormalList(l);
			}
			
			@Override
			public void caseASingleFormalList(ASingleFormalList l) {
				AFormal formal = (AFormal) l.getFormal();
				formals.add(new Formal(formal.getIdentifier().getText().trim(), formal.getMetaType().getText().trim()));
				super.caseASingleFormalList(l);
			}
			
		});
	}

	@Override
	protected void internalTransform(Body b, String phaseName, Map options) {
		init();
		
		for (Predicate p : preds) {
			for (Unit u : b.getUnits()) {
				p.holdsIn(u);
			}
		}
		
		ExceptionalUnitGraph g = new ExceptionalUnitGraph(b);
		for(Transformation t: trans) {
			new CTLAnalysis<Unit>(g,new EG(
					new Proposition() {

				//TRUE
				
				@Override
				public Map holdsIn(Host n) {
					return null;
				}

				@Override
				public void closure(List result) {
					result.add(this);
				}

				public boolean label(Host node, List succs) {
					return addFormula(this, node);
				}
				
				@Override
				public String toString() {
					return "TRUE";
				}
				
			} 
			/*t.pred*/));
		}
	}

	
	
	public CTLTransformer(Global g) {
	}

	public static Transformer v() {
		return G.v().soot_jimple_toolkits_ctl_CTLTransformer();
	}
}
