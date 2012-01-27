package IC.SymbolTables;

import IC.AST.ASTNode;

public class SymbolTablePrint {
	
	private ASTNode root;
	
	public SymbolTablePrint(ASTNode root){
		this.root=root;
	}
	
	private void printRecursion(SymbolTable cur){
		System.out.println(cur);
		for (SymbolTable child : cur.getChildren()){
			printRecursion(child);
		}
	}
	
	public void printSymbolTable(){
		printRecursion(root.getEnclosingScope());
	}
}
