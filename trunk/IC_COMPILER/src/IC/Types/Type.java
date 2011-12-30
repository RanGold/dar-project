package IC.Types;

public abstract class Type {
	
	private static int idCounter = 0;
	private int typeId;
	
	public Type() {
		this.typeId = Type.getNextID();
	}
	
	private static synchronized int getNextID() {
		return idCounter++;
	}
	
	public boolean subtypeof(Type t) {
		return (t.equals(this));
	}

	public abstract TypeClass getTypeClass();

	@Override
	public abstract String toString();
	
	public int getTypeId() {
		return this.typeId;
	}
}
