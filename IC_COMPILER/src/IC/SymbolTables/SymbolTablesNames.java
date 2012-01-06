package IC.SymbolTables;

public enum SymbolTablesNames {
	Global("Global Symbol Table"),
	Method("Method Symbol Table");
	
	private String name;
	
	private SymbolTablesNames(String name){
		this.name=name;
	}
	
	public String getName(){
		return name;
	}
}
