package IC.AST;

import java.util.List;

/**
 * Abstract base class for method AST nodes.
 * 
 * @author Tovi Almozlino
 */
public abstract class Method extends FieldOrMethod {

	protected List<Formal> formals;

	protected List<Statement> statements;

	/**
	 * Constructs a new method node. Used by subclasses.
	 * 
	 * @param type
	 *            Data type returned by method.
	 * @param name
	 *            Name of method.
	 * @param formals
	 *            List of method parameters.
	 * @param statements
	 *            List of method's statements.
	 */
	protected Method(Type type, String name, List<Formal> formals,
			List<Statement> statements) {
		super(type, name);
		this.formals = formals;
		this.statements = statements;
	}

	public List<Formal> getFormals() {
		return formals;
	}

	public List<Statement> getStatements() {
		return statements;
	}
}