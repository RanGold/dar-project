package IC.SymbolTables;

public enum Kind {
	CLASS("Class"),
	FIELD("Field"),
	FORMAL("Parameter"),
	STATIC_METHOD("Static method"),
	VIRTUAL_METHOD("Virtual method"),
	METHOD("Method"),
	VAR("Local variable"),
	RET_VAR("Return variable"),
	THIS("this variable");
	
	private String name;
	
	private Kind(String name){
		this.name=name;
	}
	
	@Override
	public String toString(){
		return name;
	}
}