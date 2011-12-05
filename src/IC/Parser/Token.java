package IC.Parser;

import java_cup.runtime.Symbol;

public class Token extends Symbol {
	private int line;
	private boolean val;

	public Token(int id, int line) {
		super(id, null);
		this.line = line + 1;
		this.val = false;
	}

	public Token(int id, int line, Object value) {
		super(id, value);
		this.line = line+1;
		this.val = true;
	}

	public String toString() {
		if (val)
			return this.line + ": " + IC.Parser.sym.names[this.sym] + "("
					+ this.value.toString() + ")";
		else
			return this.line + ": " + IC.Parser.sym.names[this.sym];
	}
	
}