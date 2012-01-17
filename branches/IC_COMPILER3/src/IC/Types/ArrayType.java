package IC.Types;

public class ArrayType extends Type {

	private Type elemType;
	
	public ArrayType(Type elemType) {
		super();
		this.elemType = elemType;
	}
	
	@Override
	public String toString() {
		return (this.elemType.toString() + "[]");
	}
	
	@Override
	public TypeClass getTypeClass() {
		return TypeClass.Array;
	}
	
	
	public Type getElementType(){
		return this.elemType;
	}
}
