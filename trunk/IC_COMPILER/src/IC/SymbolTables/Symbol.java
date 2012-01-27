package IC.SymbolTables;

import IC.Types.Type;

public class Symbol {
	private String id;
	private Type type;
	private Kind kind;
	private boolean isInitialized;
	private String distinctId;
	
	public Symbol(String id, Type type, Kind kind) {
		this(id, type,kind, false);
	}
	
	public Symbol(String id, Type type, Kind kind, boolean isInitialized) {
		this.id = id;
		this.type = type;
		this.kind = kind;
		this.isInitialized = isInitialized;
	}

	public Type getType() {
		return type;
	}

	public Kind getKind() {
		return kind;
	}

	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	public boolean isInitialized() {
		return isInitialized;
	}
	
	@Override
	public String toString() {
		if (kind.equals(Kind.CLASS))
			return kind + ": " + type;
		if (kind.equals(Kind.METHOD) || kind.equals(Kind.STATIC_METHOD)
				|| kind.equals(Kind.VIRTUAL_METHOD))
			return kind + ": " + id + " " + type;
		return kind + ": " + type + " " + id;
	}

	public String getDistinctId() {
		return distinctId;
	}

	public void setDistinctId(int distinctId) {
		String s = Integer.toString(distinctId);
		switch(kind){
		case FIELD:
			s = "f" + s;
			break;
		case FORMAL:
			s = "p" + s;
			break;
		case VAR:
			s = "v" + s; 
			break;
		default: 
			this.distinctId = id;
			return;
		}
		
		this.distinctId = s + id;
	}
}
