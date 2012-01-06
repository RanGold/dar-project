package IC.SemanticChecks;

import java_cup.runtime.Symbol;

@SuppressWarnings("serial")
public class SemanticError extends Exception {
	private int line;
	private Symbol tok;
	private boolean isMessage;

	public SemanticError(String message, int line) {
		super(message);
		this.isMessage = true;
		this.line = line;
	}

	public String getMessage() {
		return toString();
	}

	public String toString() {
		if (this.isMessage) {
			return "Semantic error at line " + line + ": " + super.getMessage();
		} else {
			return "Semantic error at line " + tok.toString();
		}
	}
}
