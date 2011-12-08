package IC.Parser;

import java_cup.runtime.Symbol;

public class Token extends Symbol {
	private int line;
	private boolean val;

	public Token(int id, int line) {
		super(id, null);
		this.line =line + 1;
		this.val =false;
	}

	public Token(int id, int line, Object value) {
		super(id, value);
		this.line =line + 1;
		this.val =true;
	}

	public String toString() {
		if (val)
			return this.line + ": " + getSymName(this.sym) + "("
					+ this.value.toString() + ")";
		else
			return this.line + ": " + getSymName(this.sym);
	}

//	public String toString() {
//		if (val)
//			return this.line + ": " + this.sym + "(" + this.value.toString()
//					+ ")";
//		else
//			return this.line + ": " + this.sym;
//	}
	
	public String getSymName(int s) {
		switch (s) {
		case IC.Parser.sym.DIVIDE:
			return "DIVIDE";
		case IC.Parser.sym.LCBR:
			return "LCBR";
		case IC.Parser.sym.LTE:
			return "LTE";
		case IC.Parser.sym.UMINUS:
			return "UMINUS";
		case IC.Parser.sym.INTEGER:
			return "INTEGER";
		case IC.Parser.sym.SEMI:
			return "SEMI";
		case IC.Parser.sym.CONTINUE:
			return "CONTINUE";
		case IC.Parser.sym.INT:
			return "INT";
		case IC.Parser.sym.MINUS:
			return "MINUS";
		case IC.Parser.sym.STATIC:
			return "STATIC";
		case IC.Parser.sym.LT:
			return "LT";
		case IC.Parser.sym.LP:
			return "LP";
		case IC.Parser.sym.COMMA:
			return "COMMA";
		case IC.Parser.sym.CLASS:
			return "CLASS";
		case IC.Parser.sym.RP:
			return "RP";
		case IC.Parser.sym.PLUS:
			return "PLUS";
		case IC.Parser.sym.MULTIPLY:
			return "MULTIPLY";
		case IC.Parser.sym.QUOTE:
			return "QUOTE";
		case IC.Parser.sym.ASSIGN:
			return "ASSIGN";
		case IC.Parser.sym.IF:
			return "IF";
		case IC.Parser.sym.THIS:
			return "THIS";
		case IC.Parser.sym.ID:
			return "ID";
		case IC.Parser.sym.DOT:
			return "DOT";
		case IC.Parser.sym.EOF:
			return "EOF";
		case IC.Parser.sym.BOOLEAN:
			return "BOOLEAN";
		case IC.Parser.sym.LAND:
			return "v";
		case IC.Parser.sym.RCBR:
			return "RCBR";
		case IC.Parser.sym.LB:
			return "LB";
		case IC.Parser.sym.RETURN:
			return "RETURN";
		case IC.Parser.sym.EQUAL:
			return "EQUAL";
		case IC.Parser.sym.TRUE:
			return "TRUE";
		case IC.Parser.sym.NEW:
			return "NEW";
		case IC.Parser.sym.error:
			return "error";
		case IC.Parser.sym.RB:
			return "RB";
		case IC.Parser.sym.LOR:
			return "LOR";
		case IC.Parser.sym.NULL:
			return "NULL";
		case IC.Parser.sym.MOD:
			return "MOD";
		case IC.Parser.sym.BREAK:
			return "BREAK";
		case IC.Parser.sym.VOID:
			return "VOID";
		case IC.Parser.sym.GTE:
			return "GTE";
		case IC.Parser.sym.ELSE:
			return "ELSE";
		case IC.Parser.sym.WHILE:
			return "WHILE";
		case IC.Parser.sym.NEQUAL:
			return "NEQUAL";
		case IC.Parser.sym.CLASS_ID:
			return "CLASS_ID";
		case IC.Parser.sym.EXTENDS:
			return "EXTENDS";
		case IC.Parser.sym.STRING:
			return "STRING";
		case IC.Parser.sym.LNEG:
			return "LNEG";
		case IC.Parser.sym.FALSE:
			return "FALSE";
		case IC.Parser.sym.GT:
			return "GT";
		case IC.Parser.sym.LENGTH:
			return "LENGTH";
		}
		return "";
	}

}