package IC.Types;

public abstract class PrimitiveType extends Type {

	public PrimitiveType() {
		super();
	}
	
	@Override
	public TypeClass getTypeClass() {
		return TypeClass.Primitive;
	}
}
