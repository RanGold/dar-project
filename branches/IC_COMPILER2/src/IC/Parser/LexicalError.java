package IC.Parser;

@SuppressWarnings("serial")
public class LexicalError extends Exception {
	private int line;
	private String value;

	public LexicalError(String message) {
		super(message);
	}

	public LexicalError(String message, int line, String value) {
		super(message);
		this.line = line + 1;
		this.value = value;
	}

	public String getMessage() {
		return toString();
	}

	public String toString() {
		if (value == null) {
			return "Lexical error at line " + line + ": " + super.getMessage();
		} else {
			return "Lexical error at line " + line + ": " + super.getMessage() + " '"
					+ this.value + "'";
		}
	}
}
