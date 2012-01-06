package IC.SymbolTables;

public enum SymbolTableNames {
	Global("Global"),
	Class("Class"),
	Method("Method"),
	StatementBlock("Statement Block");
	
	private String name;
	
	private SymbolTableNames(String name){
		this.name=name;
	}
	
	@Override
	public String toString(){
		return name+" Symbol Table";
	}
}
