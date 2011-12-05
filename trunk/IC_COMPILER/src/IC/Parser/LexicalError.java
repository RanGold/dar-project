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
		this.line = line+1;
		this.value=value;
	}
	
	public String toString() {
		if (value==null)
			return line + ": "  + "Lexical error: " + this.getMessage();
		else
			return line + ": " + "Lexical error: " + this.getMessage() + " '" + this.value + "'";
	}
}
