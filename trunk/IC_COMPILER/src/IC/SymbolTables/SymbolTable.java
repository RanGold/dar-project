package IC.SymbolTables;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
	
	/** map from String to Symbol **/
	private Map<String, Symbol> entries;
	private String id;
	private SymbolTable parentSymbolTable;
	
	
	public SymbolTable(String id) {
		this.id = id;
		entries = new HashMap<String, Symbol>();
		parentSymbolTable = null;
	}
	public SymbolTable(String id, SymbolTable parentSymbolTable ) {
		this(id);
		this.parentSymbolTable = parentSymbolTable;
	}
	public void addEntry(String key, Symbol data){
		if (!entries.containsKey(key)){
			entries.put(key, data);
		}
		else{
			//TODO WHAT IF EXISTS????
		}
	}
	public SymbolTable getParentSymbolTable(){
		return parentSymbolTable;
	}

}
