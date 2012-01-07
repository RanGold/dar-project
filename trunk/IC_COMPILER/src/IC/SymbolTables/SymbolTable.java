package IC.SymbolTables;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SymbolTable {

	private Map<String, Symbol> entries;
	private String id;
	private SymbolTableTypes type;
	private SymbolTable parentSymbolTable;
	private List<SymbolTable> children;

	public SymbolTable(String id, SymbolTableTypes type) {
		this.id = id;
		this.type = type;
		entries = new HashMap<String, Symbol>();
		children = new LinkedList<SymbolTable>();
		parentSymbolTable = null;
	}

	public SymbolTable(String id, SymbolTable parentSymbolTable,
			SymbolTableTypes type) {
		this(id, type);
		this.parentSymbolTable = parentSymbolTable;
	}

	public void addEntry(String key, Symbol data) {
		if (!entries.containsKey(key)) {
			entries.put(key, data);
		} else {
			// TODO WHAT IF EXISTS????
		}
	}

	public SymbolTable getParentSymbolTable() {
		return parentSymbolTable;
	}
	
	public void setParentSymbolTable(SymbolTable st) {
		parentSymbolTable = st;
	}
	
	public void addChild(SymbolTable child){
		if (!children.contains(child)) 
			children.add(child);
	}
	
	public void removeChild(SymbolTable child){
		children.remove(child);
	}
	
	public List<SymbolTable> getChildren(){
		return children;
	}

	private String stmtBlockLocation(SymbolTable st){
		String location="";
		SymbolTable parent=st.parentSymbolTable;
		while (!parent.type.equals(SymbolTableTypes.Method)){
			location += "statement block in ";
			parent=parent.parentSymbolTable;
		}
		location += parent.id;
		return location;
	}
	
	@Override
	public String toString() {
		String output="";
		if (type.equals(SymbolTableTypes.StatementBlock))
			output = type + " ( located in " + stmtBlockLocation(this) + " )\n";
		else
			output = type + ": " + id + "\n";
		for (Entry<String,Symbol> entry : entries.entrySet()){
			output += "\t" + entry.getValue() + "\n";
		}
		if (children.size()>0){
			output += "Children tables: ";
			int pos=0;
			for (SymbolTable child : children){
				if (pos++!=0)
					output += ", ";
				if (child.type.equals(SymbolTableTypes.StatementBlock))
					output += "statement block in " + stmtBlockLocation(child);
				else
					output += child.id;
			}
			output += "\n";
		}
		return output;
	}

}
