package IC.Types;

public class NullType extends MetaType {

	@Override
	public String toString() {
		return "null";
	}
	
	public boolean isSubType(Type t) {
		return ((t instanceof ClassType) || (t instanceof ArrayType));
	}

	@Override
	public TypeClass getTypeClass() {
		return TypeClass.Primitive;
	}
}
