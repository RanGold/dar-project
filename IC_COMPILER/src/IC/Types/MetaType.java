package IC.Types;

public abstract class MetaType {
	
	public abstract TypeClass getTypeClass();
	
	@Override
	public abstract String toString();
	
	public abstract boolean isSubType(Type t);
}
