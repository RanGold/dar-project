package IC.AST;

import IC.SymbolTables.SymbolTable;

/**
 * Abstract AST node base class.
 * 
 * @author Tovi Almozlino
 */
public abstract class ASTNode {

	private int line;
	
	/** reference to symbol table of enclosing scope **/
	private SymbolTable enclosingScope;
	
	private IC.Types.Type enclosingType;

	/**
	 * Double dispatch method, to allow a visitor to visit a specific subclass.
	 * 
	 * @param visitor
	 *            The visitor.
	 * @return A value propagated by the visitor.
	 */
	public abstract Object accept(Visitor visitor);

	/**
	 * Constructs an AST node corresponding to a line number in the original
	 * code. Used by subclasses.
	 * 
	 * @param line
	 *            The line number.
	 */
	protected ASTNode(int line) {
		this.line = line;
	}

	public int getLine() {
		return line;
	}
	
	/** returns symbol table of enclosing scope **/
	public SymbolTable getEnclosingScope() {
		return enclosingScope;
	}

	public void setenclosingScope(SymbolTable enclosingScope) {
		this.enclosingScope = enclosingScope;
		enclosingScope.setContainer(this);
	}
	
	public IC.Types.Type getEnclosingType() {
		return this.enclosingType;
	}
	
	public void setEnclosingType(IC.Types.Type type) {
		this.enclosingType = type;
	}
}
