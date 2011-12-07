package IC.AST;

/**
 * Class field AST node.
 * 
 * @author Tovi Almozlino
 */
public class Field extends FieldOrMethod {

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new field node.
	 * 
	 * @param type
	 *            Data type of field.
	 * @param name
	 *            Name of field.
	 */
	public Field(Type type, String name) {
		super(type, name);
	}

}
