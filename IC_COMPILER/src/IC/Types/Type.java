package IC.Types;

public abstract class Type extends MetaType {
	
	@Override
	public boolean isSubType(Type t) {
		return (t.equals(this));
	}
}
