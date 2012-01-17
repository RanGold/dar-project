package IC.Types;

public class NullType extends PrimitiveType {

	@Override
	public String toString() {
		return "null";
	}
	
	public boolean subtypeof(Type t) {
		return ((t instanceof ClassType) || (t instanceof ArrayType) || (t instanceof StringType));
	}
}
