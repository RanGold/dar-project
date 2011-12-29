package IC.Types;

public class ClassType extends Type {

	private ClassType superClass;
	private String identifier;
	
	public ClassType(String identifier, ClassType superClass) {
		this.identifier = identifier;
		this.superClass = superClass;
	}
	
	@Override
	public String toString() {
		return this.identifier;
	}
	
	@Override
	public boolean isSubType(Type t) {
		if (!(t instanceof ClassType) || t == null) {
			return false;
		} else {
			ClassType oc = (ClassType)t;
			return (super.equals(oc) || (this.getSuperClass() != null && this.getSuperClass().equals(oc)));
		}
	}

	public ClassType getSuperClass() {
		return this.superClass;
	}
	
	@Override
	public TypeClass getTypeClass() {
		return TypeClass.Class;
	}
}
