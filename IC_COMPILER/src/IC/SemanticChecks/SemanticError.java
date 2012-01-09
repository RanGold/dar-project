package IC.SemanticChecks;

import java_cup.runtime.Symbol;

@SuppressWarnings("serial")
public class SemanticError extends RuntimeException {
	private int line;
	private Symbol tok;
	private boolean hasLine;

	public SemanticError(String message, int line) {
		super(message);
		this.line = line;
		this.hasLine = true;
	}
	
	public SemanticError(String message) {
		super(message);
		this.hasLine = false;
		this.line = 0;
	}

	public String getMessage() {
		return toString();
	}

	public String toString() {
		if (this.hasLine)
			return "Semantic error at line " + line + ": " + super.getMessage();
		else
			return "Semantic error: " + super.getMessage();
//		else {
//			return "Semantic error at line " + tok.toString();//TODO why is this needed?
//		}
	}
}
