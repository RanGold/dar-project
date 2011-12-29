package IC.Types;

public abstract class PrimitiveType extends Type {

	@Override
	public TypeClass getTypeClass() {
		return TypeClass.Primitive;
	}
}
