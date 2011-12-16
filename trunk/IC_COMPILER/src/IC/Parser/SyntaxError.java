package IC.Parser;

import java_cup.runtime.Symbol;

@SuppressWarnings("serial")
public class SyntaxError extends Exception {
	private int line;
	private Symbol tok;
	private boolean isMessage;

	public SyntaxError(String message, int line) {
		super(message);
		this.isMessage = true;
		this.line = line;
	}

	public SyntaxError(Symbol tok) {
		this.isMessage = false;
		this.tok = tok;
	}

	public String getMessage() {
		return toString();
	}

	public String toString() {
		if (this.isMessage) {
			return "Syntax error at line "+ line + ":"+ super.getMessage();
		} else {
			return "Syntax error at line "+ tok.toString();
		}
	}
}
