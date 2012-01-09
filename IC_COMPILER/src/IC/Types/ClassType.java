package IC.Types;

import IC.AST.ICClass;

public class ClassType extends Type {

	private ICClass icClass;
	
	public ClassType(ICClass icClass) {
		super();
		this.icClass = icClass;
	}
	
	@Override
	public String toString() {
		return this.icClass.getName();
	}
	
	@Override
	public boolean subtypeof(Type t) {
		if (!(t instanceof ClassType) || t == null) {
			return false;
		} else {
			ClassType oc = (ClassType)t;
			if (!(super.equals(oc) || (this.icClass.getSuperClassName() != null && TypeTable.getClassType(this.icClass.getSuperClassName()).equals(oc)))){
				if (TypeTable.getClassType(this.icClass.getSuperClassName())==null)
					return false;
				return TypeTable.getClassType(this.icClass.getSuperClassName()).subtypeof(t);
			}
			else
				return true;
		}
	}

	
	@Override
	public TypeClass getTypeClass() {
		return TypeClass.Class;
	}
	
	public ICClass getICClass() {
		return this.icClass;
	}
}
