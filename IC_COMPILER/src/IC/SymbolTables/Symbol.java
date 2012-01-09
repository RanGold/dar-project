package IC.SymbolTables;

public class Symbol {
	private String id;
	private IC.Types.Type type;
	private Kind kind;
	
	public Symbol(String id,IC.Types.Type type, Kind kind){
		this.id = id;
		this.type = type;
		this.kind=kind;
	}
	
	public IC.Types.Type getType(){
		return type;
	}
	
	@Override
	public String toString(){
		if (kind.equals(Kind.CLASS))
			return kind+": "+type;
		if (kind.equals(Kind.METHOD) || kind.equals(Kind.STATIC_METHOD) || kind.equals(Kind.VIRTUAL_METHOD) || kind.equals(Kind.LIBRARY_METHOD))
			return kind+": "+id+" "+type;
		return kind+": "+type+" "+id;
	}
}
