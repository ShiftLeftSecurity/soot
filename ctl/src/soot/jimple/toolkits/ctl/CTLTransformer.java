package soot.jimple.toolkits.ctl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Transformer;
import soot.Unit;
import soot.Singletons.Global;
import soot.jimple.toolkits.ctl.parser.analysis.DepthFirstAdapter;
import soot.jimple.toolkits.ctl.parser.lexer.Lexer;
import soot.jimple.toolkits.ctl.parser.lexer.LexerException;
import soot.jimple.toolkits.ctl.parser.node.AFile;
import soot.jimple.toolkits.ctl.parser.node.AFileBody;
import soot.jimple.toolkits.ctl.parser.node.AFormal;
import soot.jimple.toolkits.ctl.parser.node.AFormalsReduction;
import soot.jimple.toolkits.ctl.parser.node.AGroundReduction;
import soot.jimple.toolkits.ctl.parser.node.AMultiFormalList;
import soot.jimple.toolkits.ctl.parser.node.APredicate;
import soot.jimple.toolkits.ctl.parser.node.ASingleFormalList;
import soot.jimple.toolkits.ctl.parser.node.PFormal;
import soot.jimple.toolkits.ctl.parser.node.PFormalList;
import soot.jimple.toolkits.ctl.parser.node.PMember;
import soot.jimple.toolkits.ctl.parser.node.PReduction;
import soot.jimple.toolkits.ctl.parser.node.PStatement;
import soot.jimple.toolkits.ctl.parser.node.Start;
import soot.jimple.toolkits.ctl.parser.parser.Parser;
import soot.jimple.toolkits.ctl.parser.parser.ParserException;
import soot.options.Options;
import soot.tagkit.Host;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.EscapedReader;
import soot.jimple.toolkits.ctl.formula.EG;
import soot.jimple.toolkits.ctl.formula.EU;
import soot.jimple.toolkits.ctl.formula.EX;
import soot.jimple.toolkits.ctl.formula.IFormula;
import soot.jimple.toolkits.ctl.formula.Proposition;

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
				p = new Parser(new Lexer(
	                  new PushbackReader(new EscapedReader(new BufferedReader(
	                          new InputStreamReader(new FileInputStream(trFile)))), 1024)));
				
				Start tree = p.parse();
				process(tree);
				
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Could not find file "+trFile.getAbsolutePath(),e);
			} catch (ParserException e) {
				e.printStackTrace();
			} catch (LexerException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected List<Transformation> trans = new ArrayList<Transformation>();
	
	private void process(Start tree) {		
		tree.apply(new DepthFirstAdapter() {

			List<Formal> formals;
			
			@Override
			public void caseAGroundReduction(AGroundReduction node) {
				formals = new ArrayList<Formal>();
				super.caseAGroundReduction(node);
			}
			
			public void caseAFormalsReduction(AFormalsReduction r) {
				formals = new ArrayList<Formal>();
				super.caseAFormalsReduction(r);
			};
			
			@Override
			public void caseAMultiFormalList(AMultiFormalList l) {
				AFormal formal = (AFormal) l.getFormal();
				formals.add(new Formal(formal.getIdentifier().getText(), formal.getJimpleType().toString()));
				super.caseAMultiFormalList(l);
			}
			
			@Override
			public void caseASingleFormalList(ASingleFormalList l) {
				AFormal formal = (AFormal) l.getFormal();
				formals.add(new Formal(formal.getIdentifier().getText(), formal.getJimpleType().toString()));
				super.caseASingleFormalList(l);
			}
			
			@Override
			public void caseAPredicate(APredicate p) {
				PStatement stmt = p.getStatement();
				Transformation tr = new Transformation(formals, new Predicate(formals, stmt.toString()));
				trans.add(tr);
				super.caseAPredicate(p);
			}
		});
	}

	@Override
	protected void internalTransform(Body b, String phaseName, Map options) {
		init();
		
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
