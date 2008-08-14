package soot.jimple.toolkits.ctl.formula;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;

import soot.jimple.toolkits.ctl.parser.lexer.Lexer;
import soot.jimple.toolkits.ctl.parser.lexer.LexerException;
import soot.jimple.toolkits.ctl.parser.parser.Parser;
import soot.jimple.toolkits.ctl.parser.parser.ParserException;
import soot.util.EscapedReader;

public class Test {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws LexerException 
	 * @throws ParserException 
	 */
	public static void main(String[] args) throws ParserException, LexerException, IOException {
		Parser p = 
            new Parser(new Lexer(
                  new PushbackReader(new EscapedReader(new BufferedReader(
                          new InputStreamReader(new FileInputStream(args[0])))), 1024)));
		
		p.parse();		
	}

}
