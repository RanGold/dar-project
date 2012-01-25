package IC.SymbolTables;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import IC.SemanticChecks.SemanticError;
import IC.Types.Type;

public class SymbolTable {

	
	private Map<String, Symbol> entries;
	private String id;
	private SymbolTableTypes type;
	private SymbolTable parentSymbolTable;
	private List<SymbolTable> children;
	private static int idCounter = 1;
	private int symbolId;
	
	
	
	private static synchronized int getNextID() {
		return idCounter++;
	}
	
	
	public SymbolTable(String id, SymbolTableTypes type) {
		this.id = id;
		this.type = type;
		entries = new LinkedHashMap<String, Symbol>();
		children = new LinkedList<SymbolTable>();
		parentSymbolTable = null;
		this.symbolId = getNextID();
	}

	public SymbolTable(String id, SymbolTable parentSymbolTable,
			SymbolTableTypes type) {
		this(id, type);
		this.parentSymbolTable = parentSymbolTable;
	}

	public void addEntry(String key, Symbol data,int line) {
		if (!entries.containsKey(key)) {
			data.setDistinctId(symbolId);
			entries.put(key, data);
		} else {
			throw new SemanticError("Illegal reuse of name "+key,line);
		}
	}
	
	public boolean existEntry(String key){
		if (entries.containsKey(key))
			return true;
		return false;
	}
	
	public boolean existEntryRecursive(String key){
		if (entries.containsKey(key))
			return true;
		if (type.equals(SymbolTableTypes.Global))
			return false;
		return parentSymbolTable.existEntryRecursive(key);
	}
	
	public Symbol getEntry(String key){
		return entries.get(key);
	}
	
	public Symbol getEntryRecursive(String key){
		Symbol ret = entries.get(key);
		if (ret!=null)
			return ret;
		if (type.equals(SymbolTableTypes.Global))
			return null;
		return parentSymbolTable.getEntryRecursive(key);
	}
	
	public SymbolTable getVariableScope(String key){
		Symbol ret = entries.get(key);
		if (ret!=null)
			return this;
		if (type.equals(SymbolTableTypes.Global))
			return null;
		return parentSymbolTable.getVariableScope(key);
	}

	public SymbolTable getParentSymbolTable() {
		return parentSymbolTable;
	}
	
	public void addChild(SymbolTable child){
		if (!children.contains(child)) 
			children.add(child);
	}
	
	public List<SymbolTable> getChildren(){
		return children;
	}
	
	public SymbolTableTypes getType(){
		return type;
	}
	
	public String getID(){
		return id;
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
	
	public String stmtBlockLocation(){
		return stmtBlockLocation(this);
	}
	
	@Override
	public String toString() {
		String output="";
		if (type.equals(SymbolTableTypes.StatementBlock))
			output = type + " ( located in " + stmtBlockLocation(this) + " )\n";
		else
			output = type + ": " + id + "\n";
		for (Entry<String,Symbol> entry : entries.entrySet()){
			if (entry.getValue().getKind().equals(Kind.RET_VAR) || entry.getValue().getKind().equals(Kind.THIS))
				continue;
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
