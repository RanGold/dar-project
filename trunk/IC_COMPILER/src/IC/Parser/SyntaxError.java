package IC.Parser;

@SuppressWarnings("serial")
public class SyntaxError extends Exception {
	private int line;

	public SyntaxError(String message) {
		super(message);
	}

	public SyntaxError(String message, int line) {
		super(message);
		this.line = line + 1;// TODO: do we need this???
	}

	public String toString() {
		return line + ": Syntax error: " + this.getMessage();
	}
}
