package IC.SymbolTables;

public enum Kind {
	CLASS("Class"),
	FIELD("Field"),
	FORMAL("Parameter"),
	STATIC_METHOD("Static method"),
	VIRTUAL_METHOD("Virtual method"),
	//LIBRARY_METHOD("Library method"),
	//LIBRARY_METHOD("Static method"),
	METHOD("Method"),
	VAR("Local variable");
	
	private String name;
	
	private Kind(String name){
		this.name=name;
	}
	
	@Override
	public String toString(){
		return name;
	}
}