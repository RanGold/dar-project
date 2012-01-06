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
}
