package IC.Parser;

import java_cup.runtime.Symbol;

@SuppressWarnings("serial")
public class SyntaxError extends Exception {
	private int line;
	private Symbol tok;
	private boolean m;

	public SyntaxError(String message, int line) {
		super(message);
		m=true;
		this.line=line;
	}

	public SyntaxError(Symbol tok) {
		m=false;
		this.tok=tok;
	}

	public String getMessage(){
		return toString();
	}
	
	public String toString() {
		if (m)
			return "Syntax error: "+super.getMessage()+" (line: )"+line;
		return "Syntax error: "+tok.toString();
	}
}
