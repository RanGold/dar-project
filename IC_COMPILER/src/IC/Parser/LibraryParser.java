
//----------------------------------------------------
// The following code was generated by CUP v0.11a beta 20060608
// Wed Dec 07 22:16:44 IST 2011
//----------------------------------------------------

package IC.Parser;

import IC.AST.*;
import IC.DataTypes;
import java_cup.runtime.*;
import java.util.List;
import java.util.LinkedList;

/** CUP v0.11a beta 20060608 generated parser.
  * @version Wed Dec 07 22:16:44 IST 2011
  */
public @SuppressWarnings(value={"all"}) class LibraryParser extends java_cup.runtime.lr_parser {

  /** Default constructor. */
  public LibraryParser() {super();}

  /** Constructor which sets the default scanner. */
  public LibraryParser(java_cup.runtime.Scanner s) {super(s);}

  /** Constructor which sets the default scanner. */
  public LibraryParser(java_cup.runtime.Scanner s, java_cup.runtime.SymbolFactory sf) {super(s,sf);}

  /** Production table. */
  protected static final short _production_table[][] = 
    unpackFromStrings(new String[] {
    "\000\020\000\002\002\007\000\002\002\004\000\002\007" +
    "\003\000\002\007\003\000\002\007\003\000\002\007\003" +
    "\000\002\007\005\000\002\005\004\000\002\006\003\000" +
    "\002\006\005\000\002\003\010\000\002\003\010\000\002" +
    "\003\011\000\002\003\011\000\002\004\003\000\002\004" +
    "\004" });

  /** Access to production table. */
  public short[][] production_table() {return _production_table;}

  /** Parse-action table. */
  protected static final short[][] _action_table = 
    unpackFromStrings(new String[] {
    "\000\046\000\004\005\004\001\002\000\004\022\007\001" +
    "\002\000\004\002\006\001\002\000\004\002\000\001\002" +
    "\000\004\012\010\001\002\000\004\017\012\001\002\000" +
    "\006\014\047\017\012\001\002\000\014\004\017\007\021" +
    "\020\014\021\020\022\015\001\002\000\006\014\ufff3\017" +
    "\ufff3\001\002\000\006\010\ufffe\023\ufffe\001\002\000\006" +
    "\010\ufffc\023\ufffc\001\002\000\006\010\032\023\040\001" +
    "\002\000\006\010\ufffd\023\ufffd\001\002\000\004\023\022" +
    "\001\002\000\006\010\uffff\023\uffff\001\002\000\004\011" +
    "\023\001\002\000\014\004\017\007\021\015\027\020\014" +
    "\022\015\001\002\000\006\006\034\015\035\001\002\000" +
    "\006\010\032\023\031\001\002\000\006\006\ufff9\015\ufff9" +
    "\001\002\000\004\016\030\001\002\000\006\014\ufff7\017" +
    "\ufff7\001\002\000\006\006\ufffa\015\ufffa\001\002\000\004" +
    "\013\033\001\002\000\006\010\ufffb\023\ufffb\001\002\000" +
    "\012\004\017\007\021\020\014\022\015\001\002\000\004" +
    "\016\036\001\002\000\006\014\ufff5\017\ufff5\001\002\000" +
    "\006\006\ufff8\015\ufff8\001\002\000\004\011\041\001\002" +
    "\000\014\004\017\007\021\015\043\020\014\022\015\001" +
    "\002\000\006\006\034\015\045\001\002\000\004\016\044" +
    "\001\002\000\006\014\ufff6\017\ufff6\001\002\000\004\016" +
    "\046\001\002\000\006\014\ufff4\017\ufff4\001\002\000\004" +
    "\002\001\001\002\000\006\014\ufff2\017\ufff2\001\002" });

  /** Access to parse-action table. */
  public short[][] action_table() {return _action_table;}

  /** <code>reduce_goto</code> table. */
  protected static final short[][] _reduce_table = 
    unpackFromStrings(new String[] {
    "\000\046\000\004\002\004\001\001\000\002\001\001\000" +
    "\002\001\001\000\002\001\001\000\002\001\001\000\006" +
    "\003\012\004\010\001\001\000\004\003\047\001\001\000" +
    "\004\007\015\001\001\000\002\001\001\000\002\001\001" +
    "\000\002\001\001\000\002\001\001\000\002\001\001\000" +
    "\002\001\001\000\002\001\001\000\002\001\001\000\010" +
    "\005\025\006\023\007\024\001\001\000\002\001\001\000" +
    "\002\001\001\000\002\001\001\000\002\001\001\000\002" +
    "\001\001\000\002\001\001\000\002\001\001\000\002\001" +
    "\001\000\006\005\036\007\024\001\001\000\002\001\001" +
    "\000\002\001\001\000\002\001\001\000\002\001\001\000" +
    "\010\005\025\006\041\007\024\001\001\000\002\001\001" +
    "\000\002\001\001\000\002\001\001\000\002\001\001\000" +
    "\002\001\001\000\002\001\001\000\002\001\001" });

  /** Access to <code>reduce_goto</code> table. */
  public short[][] reduce_table() {return _reduce_table;}

  /** Instance of action encapsulation class. */
  protected CUP$LibraryParser$actions action_obj;

  /** Action encapsulation object initializer. */
  protected void init_actions()
    {
      action_obj = new CUP$LibraryParser$actions(this);
    }

  /** Invoke a user supplied parse action. */
  public java_cup.runtime.Symbol do_action(
    int                        act_num,
    java_cup.runtime.lr_parser parser,
    java.util.Stack            stack,
    int                        top)
    throws java.lang.Exception
  {
    /* call code in generated class */
    return action_obj.CUP$LibraryParser$do_action(act_num, parser, stack, top);
  }

  /** Indicates start state. */
  public int start_state() {return 0;}
  /** Indicates start production. */
  public int start_production() {return 1;}

  /** <code>EOF</code> Symbol index. */
  public int EOF_sym() {return 0;}

  /** <code>error</code> Symbol index. */
  public int error_sym() {return 1;}

}

/** Cup generated class to encapsulate user supplied action code.*/
class CUP$LibraryParser$actions {
  private final LibraryParser parser;

  /** Constructor */
  CUP$LibraryParser$actions(LibraryParser parser) {
    this.parser = parser;
  }

  /** Method with the actual generated action code. */
  public final java_cup.runtime.Symbol CUP$LibraryParser$do_action(
    int                        CUP$LibraryParser$act_num,
    java_cup.runtime.lr_parser CUP$LibraryParser$parser,
    java.util.Stack            CUP$LibraryParser$stack,
    int                        CUP$LibraryParser$top)
    throws java.lang.Exception
    {
      /* Symbol object for return from actions */
      java_cup.runtime.Symbol CUP$LibraryParser$result;

      /* select the action based on the action number */
      switch (CUP$LibraryParser$act_num)
        {
          /*. . . . . . . . . . . . . . . . . . . .*/
          case 15: // libmethod_list ::= libmethod_list libmethod 
            {
              List<Method> RESULT =null;
		int lmlleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-1)).left;
		int lmlright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-1)).right;
		List<Method> lml = (List<Method>)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-1)).value;
		int lmleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).left;
		int lmright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).right;
		LibraryMethod lm = (LibraryMethod)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.peek()).value;
			
				lml.add(lm);
				RESULT = lml;
			
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("libmethod_list",2, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-1)), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          return CUP$LibraryParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 14: // libmethod_list ::= libmethod 
            {
              List<Method> RESULT =null;
		int lmleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).left;
		int lmright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).right;
		LibraryMethod lm = (LibraryMethod)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.peek()).value;
		 	
				RESULT = new LinkedList<Method>(); 
				RESULT.add(lm);
			
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("libmethod_list",2, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          return CUP$LibraryParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 13: // libmethod ::= STATIC type ID LP formal_list RP SEMI 
            {
              LibraryMethod RESULT =null;
		int tleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-5)).left;
		int tright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-5)).right;
		Type t = (Type)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-5)).value;
		int idleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-4)).left;
		int idright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-4)).right;
		String id = (String)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-4)).value;
		int flleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-2)).left;
		int flright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-2)).right;
		List<Formal> fl = (List<Formal>)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-2)).value;
		
				RESULT=new LibraryMethod(t,id,fl);
			
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("libmethod",1, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-6)), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          return CUP$LibraryParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 12: // libmethod ::= STATIC VOID ID LP formal_list RP SEMI 
            {
              LibraryMethod RESULT =null;
		int idleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-4)).left;
		int idright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-4)).right;
		String id = (String)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-4)).value;
		int flleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-2)).left;
		int flright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-2)).right;
		List<Formal> fl = (List<Formal>)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-2)).value;
		
				RESULT=new LibraryMethod(new PrimitiveType(idleft,DataTypes.VOID),id,fl);
			
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("libmethod",1, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-6)), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          return CUP$LibraryParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 11: // libmethod ::= STATIC type ID LP RP SEMI 
            {
              LibraryMethod RESULT =null;
		int tleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-4)).left;
		int tright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-4)).right;
		Type t = (Type)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-4)).value;
		int idleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-3)).left;
		int idright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-3)).right;
		String id = (String)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-3)).value;
		
				RESULT=new LibraryMethod(t,id,new LinkedList<Formal>());
			
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("libmethod",1, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-5)), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          return CUP$LibraryParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 10: // libmethod ::= STATIC VOID ID LP RP SEMI 
            {
              LibraryMethod RESULT =null;
		int idleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-3)).left;
		int idright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-3)).right;
		String id = (String)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-3)).value;
		
				RESULT=new LibraryMethod(new PrimitiveType(idleft,DataTypes.VOID),id,new LinkedList<Formal>());
			
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("libmethod",1, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-5)), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          return CUP$LibraryParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 9: // formal_list ::= formal_list COMMA formal 
            {
              List<Formal> RESULT =null;
		int flleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-2)).left;
		int flright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-2)).right;
		List<Formal> fl = (List<Formal>)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-2)).value;
		int fleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).left;
		int fright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).right;
		Formal f = (Formal)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.peek()).value;
			
				fl.add(f);
				RESULT = fl;
			
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("formal_list",4, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-2)), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          return CUP$LibraryParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 8: // formal_list ::= formal 
            {
              List<Formal> RESULT =null;
		int fleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).left;
		int fright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).right;
		Formal f = (Formal)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.peek()).value;
		 	
				RESULT = new LinkedList<Formal>(); 
				RESULT.add(f);
			
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("formal_list",4, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          return CUP$LibraryParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 7: // formal ::= type ID 
            {
              Formal RESULT =null;
		int tleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-1)).left;
		int tright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-1)).right;
		Type t = (Type)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-1)).value;
		int idleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).left;
		int idright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).right;
		String id = (String)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.peek()).value;
		
				RESULT = new Formal(t,id);
			
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("formal",3, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-1)), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          return CUP$LibraryParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 6: // type ::= type LB RB 
            {
              Type RESULT =null;
		int tleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-2)).left;
		int tright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-2)).right;
		Type t = (Type)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-2)).value;
			
				t.incrementDimension();
				RESULT = t; 
			
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("type",5, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-2)), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          return CUP$LibraryParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 5: // type ::= CLASS_ID 
            {
              Type RESULT =null;
		int class_idleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).left;
		int class_idright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).right;
		String class_id = (String)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.peek()).value;
		 RESULT = new UserType(class_idleft, class_id); 
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("type",5, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          return CUP$LibraryParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 4: // type ::= BOOLEAN 
            {
              Type RESULT =null;
		int boolleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).left;
		int boolright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).right;
		Object bool = (Object)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.peek()).value;
		 RESULT = new PrimitiveType(boolleft, DataTypes.BOOLEAN); 
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("type",5, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          return CUP$LibraryParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 3: // type ::= STRING 
            {
              Type RESULT =null;
		int strleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).left;
		int strright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).right;
		Object str = (Object)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.peek()).value;
		 RESULT = new PrimitiveType(strleft, DataTypes.STRING); 
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("type",5, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          return CUP$LibraryParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 2: // type ::= INT 
            {
              Type RESULT =null;
		int integleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).left;
		int integright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()).right;
		Object integ = (Object)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.peek()).value;
		 RESULT = new PrimitiveType(integleft, DataTypes.INT); 
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("type",5, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          return CUP$LibraryParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 1: // $START ::= libic EOF 
            {
              Object RESULT =null;
		int start_valleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-1)).left;
		int start_valright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-1)).right;
		ICClass start_val = (ICClass)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-1)).value;
		RESULT = start_val;
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("$START",0, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-1)), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          /* ACCEPT */
          CUP$LibraryParser$parser.done_parsing();
          return CUP$LibraryParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 0: // libic ::= CLASS CLASS_ID LCBR libmethod_list RCBR 
            {
              ICClass RESULT =null;
		int class_idleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-3)).left;
		int class_idright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-3)).right;
		String class_id = (String)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-3)).value;
		int lm_listleft = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-1)).left;
		int lm_listright = ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-1)).right;
		List<Method> lm_list = (List<Method>)((java_cup.runtime.Symbol) CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-1)).value;
		 	
				if (class_id.compareTo("Library") != 0) throw new SyntaxError("Illegal library class name",class_idleft);
				RESULT = new ICClass(class_idleft,class_id,new LinkedList<Field>(),lm_list); 
			
              CUP$LibraryParser$result = parser.getSymbolFactory().newSymbol("libic",0, ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.elementAt(CUP$LibraryParser$top-4)), ((java_cup.runtime.Symbol)CUP$LibraryParser$stack.peek()), RESULT);
            }
          return CUP$LibraryParser$result;

          /* . . . . . .*/
          default:
            throw new Exception(
               "Invalid action number found in internal parse table");

        }
    }
}

