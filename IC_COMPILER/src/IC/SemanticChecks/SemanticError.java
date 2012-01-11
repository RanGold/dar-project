package IC.SemanticChecks;

@SuppressWarnings("serial")
public class SemanticError extends RuntimeException {
	private int line;
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
	}
}
