package IC.Parser;

%%

%class Lexer
%public
%function next_token
%type Token
%line
%scanerror LexicalError
%state QUOTE
%state COMMENT1
%state COMMENT2

%cup
%{
	private String quote;
%}

%eofval{
	if (yystate() == QUOTE) throw new LexicalError("Missing end of quote", yyline, null);
	if (yystate() == COMMENT2) throw new LexicalError("Missing end of comment", yyline, null);
	
  	return new Token(sym.EOF, yyline);
%eofval}

%%

/*White spaces*/
<YYINITIAL> [ \t\n\r]+ {}

/*Parentheses*/
<YYINITIAL> "(" 	{ return new Token(sym.LP,yyline ); }
<YYINITIAL> ")" 	{ return new Token(sym.RP,yyline ); }
<YYINITIAL> "{" 	{ return new Token(sym.LCBR,yyline ); }
<YYINITIAL> "}" 	{ return new Token(sym.RCBR,yyline ); }
<YYINITIAL> "[" 	{ return new Token(sym.LB,yyline ); }
<YYINITIAL> "]" 	{ return new Token(sym.RB,yyline ); }

/*Operators*/
<YYINITIAL> "/" 	{ return new Token(sym.DIVIDE,yyline ); }
<YYINITIAL> "-"		{ return new Token(sym.MINUS,yyline ); }
<YYINITIAL> "+" 	{ return new Token(sym.PLUS,yyline ); }
<YYINITIAL> "%" 	{ return new Token(sym.MOD,yyline ); }
<YYINITIAL> "*" 	{ return new Token(sym.MULTIPLY,yyline ); }

<YYINITIAL> "="		{ return new Token(sym.ASSIGN,yyline ); }
<YYINITIAL> "!="	{ return new Token(sym.NEQUAL,yyline ); }
<YYINITIAL> "==" 	{ return new Token(sym.EQUAL,yyline ); }
<YYINITIAL> ">" 	{ return new Token(sym.GT,yyline ); }
<YYINITIAL> ">=" 	{ return new Token(sym.GTE,yyline ); }
<YYINITIAL> "<" 	{ return new Token(sym.LT,yyline ); }
<YYINITIAL> "<=" 	{ return new Token(sym.LTE,yyline ); }
<YYINITIAL> "!" 	{ return new Token(sym.LNEG,yyline ); }
<YYINITIAL> "&&" 	{ return new Token(sym.LAND,yyline ); }
<YYINITIAL> "||" 	{ return new Token(sym.LOR,yyline ); }

/*punctuation characters*/
<YYINITIAL> ";" 	{ return new Token(sym.SEMI,yyline ); }
<YYINITIAL> "," 	{ return new Token(sym.COMMA,yyline ); }
<YYINITIAL> "." 	{ return new Token(sym.DOT,yyline ); }

/*Keywords*/
<YYINITIAL> "boolean"	{ return new Token(sym.BOOLEAN,yyline ); }	
<YYINITIAL> "string" 	{ return new Token(sym.STRING,yyline ); }
<YYINITIAL> "int" 		{ return new Token(sym.INT,yyline ); }
<YYINITIAL> "class" 	{ return new Token(sym.CLASS,yyline ); }				
<YYINITIAL> "break"		{ return new Token(sym.BREAK,yyline ); }
<YYINITIAL> "continue" 	{ return new Token(sym.CONTINUE,yyline ); }
<YYINITIAL> "static" 	{ return new Token(sym.STATIC,yyline ); }
<YYINITIAL> "this" 		{ return new Token(sym.THIS,yyline ); }
<YYINITIAL> "true" 		{ return new Token(sym.TRUE,yyline ); }
<YYINITIAL> "void" 		{ return new Token(sym.VOID,yyline ); }
<YYINITIAL> "extends" 	{ return new Token(sym.EXTENDS,yyline ); }
<YYINITIAL> "while" 	{ return new Token(sym.WHILE,yyline ); }
<YYINITIAL> "else" 		{ return new Token(sym.ELSE,yyline ); }
<YYINITIAL> "false" 	{ return new Token(sym.FALSE,yyline ); }
<YYINITIAL> "if" 		{ return new Token(sym.IF,yyline ); }
<YYINITIAL> "length" 	{ return new Token(sym.LENGTH,yyline ); }
<YYINITIAL> "new" 		{ return new Token(sym.NEW,yyline ); }
<YYINITIAL> "return"	{ return new Token(sym.RETURN,yyline ); }
<YYINITIAL> "null"	 	{ return new Token(sym.NULL,yyline ); }

/* Integer */
<YYINITIAL> [0]+	{ return new Token(sym.INTEGER,yyline,yytext()); }
<YYINITIAL> [1-9][0-9]* { 
       if (yytext().length()>10) throw new LexicalError("Integer out of bound", yyline,yytext()); 
        try{
        		Integer.parseInt("-" + yytext());
        		return new Token(sym.INTEGER,yyline,yytext()); 
        	}catch(NumberFormatException e){
        	        throw new LexicalError("Integer out of bound", yyline,yytext());  
        	}}

/* Comments */
<YYINITIAL> "//"	{ yybegin(COMMENT1); }
<COMMENT1> [\n]		{ yybegin(YYINITIAL); }
<COMMENT1> . 		{ }

<YYINITIAL> "/*"	{ yybegin(COMMENT2); }
<COMMENT2> "*/" 	{ yybegin(YYINITIAL); }
<COMMENT2> . 		{ }
<COMMENT2> [\n] 	{ }

/*Quotes*/
<YYINITIAL> [\"] 				{ quote = "\""; yybegin(QUOTE); }
<QUOTE> "\\n"|"\\t"|"\\\\"		{ quote += yytext(); }
<QUOTE> "\\\"" 					{ quote += yytext(); }
<QUOTE> "\"" 					{ quote += yytext(); yybegin(YYINITIAL); return new Token(sym.QUOTE,yyline,quote); }
<QUOTE> [ -\[\]-~]				{ quote += yytext(); }
<QUOTE> . 						{ throw new LexicalError("illegal character in string",yyline,yytext()); }

/* Identifiers*/
<YYINITIAL> [A-Z][_a-zA-Z0-9]*  { return new Token(sym.CLASS_ID,yyline,yytext()); }
<YYINITIAL> [a-z][_a-zA-Z0-9]* 	{ return new Token(sym.ID,yyline,yytext()); }
<YYINITIAL>[0-9][0-9a-zA-Z_]+ { throw new LexicalError("illegal token", yyline,yytext()); }

/* Illegal token */
<YYINITIAL> . 		{ throw new LexicalError("illegal character",yyline,yytext()); }

