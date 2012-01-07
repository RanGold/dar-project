package IC.SymbolTables;

import java.util.HashMap;
import java.util.Map;

import IC.AST.*;

public class SymbolTableBuilder implements Visitor {

	private String path;
	
	private Kind method_kind(String method_name){
		String virtual_method = "class IC.AST.VirtualMethod";
		String static_method = "class IC.AST.StaticMethod";
		String library_method = "class IC.AST.LibraryMethod";
		if (method_name.equals(virtual_method))
			return Kind.VIRTUAL_METHOD;
		else if (method_name.equals(static_method))
			return Kind.STATIC_METHOD;
		else if (method_name.equals(library_method))
			return Kind.LIBRARY_METHOD;
		else
			return Kind.METHOD;
	}
	
	public SymbolTableBuilder(String path){
		this.path = path;
	}
	
	public Object visit(Program program) {
		Map<String,SymbolTable> classes = new HashMap<String,SymbolTable>();
		
		SymbolTable st = new SymbolTable(path,SymbolTableTypes.Global);
		String className;
		program.addenclosingScope(st);
		
		for (ICClass icClass : program.getClasses()) {
			className = icClass.getName();
			st.addEntry(className, new Symbol(className, icClass.getEnclosingType(), Kind.CLASS));
			
			//add symbol table for every class and make program its father
			SymbolTable stClass = new SymbolTable(icClass.getName(), st,SymbolTableTypes.Class);
			icClass.addenclosingScope(stClass);
			st.addChild(stClass);
			classes.put(icClass.getName(), stClass);
		}
		
		
		for (ICClass icClass : program.getClasses()) {
			
			//if class extends other class then we reset the parent-child relationships
			if (icClass.getSuperClassName()!=null){
				SymbolTable temp = classes.get(icClass.getSuperClassName());
				st.removeChild(icClass.getenclosingScope());
				temp.addChild(icClass.getenclosingScope());
				icClass.getenclosingScope().setParentSymbolTable(temp);
			}
			
			icClass.accept(this);
		}
		
		return st;
	}

	// TODO - check what to do with static

	public Object visit(ICClass icClass) {
		
		String name;
		/* add fields and methods to table */
		for (Field field : icClass.getFields()) {
			name = field.getName();
			icClass.getenclosingScope().addEntry(name,
					new Symbol(name, field.getEnclosingType(), Kind.FIELD));
		}
		for (Method method : icClass.getMethods()) {
			name = method.getName();
			SymbolTable stMethod = new SymbolTable(name, icClass.getenclosingScope(),SymbolTableTypes.Method);
			icClass.getenclosingScope().addChild(stMethod);
			method.addenclosingScope(stMethod);
			icClass.getenclosingScope().addEntry(name,
					new Symbol(name, method.getEnclosingType(), method_kind(method.getClass().toString())));
		}

		/* call visitor on fields and methods */
		for (Field field : icClass.getFields()){
			field.accept(this);
		}
		for (Method method : icClass.getMethods()) {
			method.accept(this);
		}
		return icClass.getenclosingScope();
	}

	public Object visit(Field field) {
		return null;
	}

	private void methodVisit(Method method){
		String name;
		for (Formal formal : method.getFormals()){
			name = formal.getName();
			method.getenclosingScope().addEntry(name,
					new Symbol(name, formal.getEnclosingType(), Kind.FORMAL));
		}
		for (Statement statement : method.getStatements()){
			statement.addenclosingScope(method.getenclosingScope());
			statement.accept(this);
		}
	}
	
	//TODO add ret and this
	@Override
	public Object visit(VirtualMethod method) {
		methodVisit(method);
		return null;
	}

	@Override
	public Object visit(StaticMethod method) {
		methodVisit(method);
		return null;
	}

	@Override
	public Object visit(LibraryMethod method) {
		methodVisit(method);
		return null;
	}

	@Override
	public Object visit(Formal formal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(PrimitiveType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(UserType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Return returnStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(If ifStatement) {//TODO what about if (x==5) return false - meaning what about if with no {} should we open a block (new symbol table?)

		ifStatement.getCondition().addenclosingScope(ifStatement.getenclosingScope());//TODO needed?
		ifStatement.getCondition().accept(this);
		
		ifStatement.getOperation().addenclosingScope(ifStatement.getenclosingScope());
		ifStatement.getOperation().accept(this);
		
		if (ifStatement.hasElse()){
			ifStatement.getElseOperation().addenclosingScope(ifStatement.getenclosingScope());
			ifStatement.getElseOperation().accept(this);
		}
		
		return ifStatement.getenclosingScope();
	}

	@Override
	public Object visit(While whileStatement) {
		
		whileStatement.getCondition().addenclosingScope(whileStatement.getenclosingScope());//TODO needed?
		whileStatement.getCondition().accept(this);
		
		whileStatement.getOperation().addenclosingScope(whileStatement.getenclosingScope());
		whileStatement.getOperation().accept(this);
		
		return whileStatement.getenclosingScope();
	}

	@Override
	public Object visit(Break breakStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Continue continueStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		SymbolTable st = new SymbolTable(statementsBlock.toString(),statementsBlock.getenclosingScope(),SymbolTableTypes.StatementBlock);
		statementsBlock.getenclosingScope().addChild(st);
		statementsBlock.addenclosingScope(st);
		for(Statement statement : statementsBlock.getStatements()){
			statement.addenclosingScope(st);
			statement.accept(this);
		}
		
		return st;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		String name = localVariable.getName();
		localVariable.getenclosingScope().addEntry(name, new Symbol(name,localVariable.getEnclosingType(),Kind.VAR));
		return null;
	}

	@Override
	public Object visit(VariableLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ArrayLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StaticCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VirtualCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(This thisExpression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewClass newClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewArray newArray) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Length length) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Literal literal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		// TODO Auto-generated method stub
		return null;
	}

}
