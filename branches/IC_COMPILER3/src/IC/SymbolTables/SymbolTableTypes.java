package IC.SymbolTables;

public enum SymbolTableTypes {
	Global("Global"),
	Class("Class"),
	Method("Method"),
	StatementBlock("Statement Block");
	
	private String name;
	
	private SymbolTableTypes(String name){
		this.name=name;
	}
	
	@Override
	public String toString(){
		return name+" Symbol Table";
	}
}
