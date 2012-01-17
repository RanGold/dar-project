package IC.SymbolTables;

import java.util.HashMap;
import java.util.Map;

import IC.AST.*;
import IC.SemanticChecks.SemanticError;
import IC.Types.MethodType;
import IC.Types.TypeTable;

public class SymbolTableBuilder implements Visitor {

	private String path;
	private boolean seen_main;
	private Map<String,ICClass> classes;
	
	public SymbolTableBuilder(String path){
		this.classes = new HashMap<String,ICClass>();
		this.path = path;
		this.seen_main = false;
	}
	
	public Object visit(Program program) {
		Map<String,SymbolTable> classes_tobe_extended = new HashMap<String,SymbolTable>();
		
		SymbolTable st = new SymbolTable(path,SymbolTableTypes.Global);
		String className;
		program.setenclosingScope(st);
		
		for (ICClass icClass : program.getClasses()) {
			className = icClass.getName();
			
			//add class to symbol table of classes
			st.addEntry(className, new Symbol(className, icClass.getEnclosingType(), Kind.CLASS),icClass.getLine());
			
			SymbolTable stClass;
			//if class doesn't extend any other class
			//then make it a child of st and put it in classes_tobe_extended
			if (icClass.getSuperClassName()==null){
				stClass = new SymbolTable(className, st,SymbolTableTypes.Class);
				classes_tobe_extended.put(className, stClass);
				st.addChild(stClass);
			}
			else{//make it a child of the class it extends
				SymbolTable temp = classes_tobe_extended.get(icClass.getSuperClassName());
				if (temp==null){//class to be extended was yet to be defined
					throw new SemanticError("Trying to extend a class that has yet to be defined",icClass.getLine());
				}
				stClass = new SymbolTable(className, temp,SymbolTableTypes.Class);
				temp.addChild(stClass);
				classes_tobe_extended.put(className, stClass);
			}
			classes.put(className, icClass);
			icClass.setenclosingScope(stClass);
			icClass.accept(this);
		}
		if (!seen_main)
			throw new SemanticError("No main method has been defined");
		return st;
	}
	
	private Kind get_method_kind(String method_name){
		String virtual_method = "class IC.AST.VirtualMethod";
		String static_method = "class IC.AST.StaticMethod";
		String library_method = "class IC.AST.LibraryMethod";
		if (method_name.equals(virtual_method))
			return Kind.VIRTUAL_METHOD;
		else if (method_name.equals(static_method))
			return Kind.STATIC_METHOD;
		else if (method_name.equals(library_method))
			return Kind.STATIC_METHOD;
		else
			return Kind.METHOD;
	}

	private void check_main(Method method){
		//check that the method is static
		if(!get_method_kind(method.getClass().toString()).equals(Kind.STATIC_METHOD)){
			//throw new SemanticError("The main method must be a static method",method.getLine());
			return;
		}
		//check correct number of parameters
		if (method.getFormals().size()!=1){
			//throw new SemanticError("Wrong number of arguments for main method",method.getLine());
			return;
		}
		//check parameter's type
		if (!method.getFormals().get(0).getType().getEnclosingType().subtypeof(TypeTable.arrayType(TypeTable.stringType)) || method.getFormals().get(0).getType().getDimension()!=1){
			//throw new SemanticError("The type of the input parameter of the main method must be \"string[]\"",method.getLine());
			return;
		}
		//check return parameter
		if (!method.getType().getEnclosingType().subtypeof(TypeTable.voidType)){
			//throw new SemanticError("main method must return void",method.getLine());
			return;
		}
		//check that no main message has already been defined
		if (seen_main)
			throw new SemanticError("More than one main method was defined",method.getLine());
		seen_main = true;
	}
	
	private void super_field_uses(ICClass icClass, String field_name, int line){
		SymbolTable st=icClass.getenclosingScope().getParentSymbolTable();
		while (!st.getType().equals(SymbolTableTypes.Global)){
			if (st.existEntry(field_name))
				throw new SemanticError("Illegal reuse of the name \""+field_name+"\" (was previously defined in one of the super classes)",line);
			st = st.getParentSymbolTable();
		}
	}
	
	private void super_method_uses(ICClass icClass, Method method){
		String method_name = method.getName();
		int line = method.getLine();
		Kind method_kind = get_method_kind(method.getClass().toString());
		
		SymbolTable st=icClass.getenclosingScope().getParentSymbolTable();
		while (!st.getType().equals(SymbolTableTypes.Global)){
			if (st.existEntry(method_name)){
				Symbol methodTOoverride = st.getEntry(method_name);
				if (!method_kind.equals(methodTOoverride.getKind())){
					if (!methodTOoverride.getKind().equals(Kind.VIRTUAL_METHOD) && !methodTOoverride.getKind().equals(Kind.STATIC_METHOD))
						throw new SemanticError(method_kind+" cannot have the same name of a "+methodTOoverride.getKind().toString().toLowerCase(),line);
					else
						throw new SemanticError(method_kind+" cannot override/overload "+methodTOoverride.getKind().toString().toLowerCase(),line);
				}
				else{//validate that a correct override was used
					MethodType methodTOoverride_type = (MethodType) methodTOoverride.getType();
					MethodType method_type = (MethodType) method.getEnclosingType();
					//check that the number of arguments are equal
					if (method_type.getArguments().size()!=methodTOoverride_type.getArguments().size())
						throw new SemanticError("Overloading of methods is not supported",line);
					//check that the arguments types are correctly overridden
					for (int i=0;i<methodTOoverride_type.getArguments().size();i++){
						IC.Types.Type type1 = method_type.getArguments().get(i);
						IC.Types.Type type2 = methodTOoverride_type.getArguments().get(i);
						if (type1!=type2 /*&& !type2.subtypeof(type1)*/)
							throw new SemanticError("Overloading of methods is not supported",line);
					}
					//check that the return type is correctly overridden
					IC.Types.Type type1 = method_type.getReturnVal();
					IC.Types.Type type2 = methodTOoverride_type.getReturnVal();
					if (type1!=type2 /*&& !type1.subtypeof(type2)*/)
						throw new SemanticError("Overloading of methods is not supported",line);
				}
			}
			st = st.getParentSymbolTable();
		}
	}
	
	public Object visit(ICClass icClass) {
		
		String name;
		/* add fields and methods to table */
		for (Field field : icClass.getFields()) {
			name = field.getName();
			icClass.getenclosingScope().addEntry(name,new Symbol(name, field.getEnclosingType(), Kind.FIELD),field.getLine());
			
			//check that field name wasn't used in super classes
			super_field_uses(icClass,name,field.getLine());
		}
		for (Method method : icClass.getMethods()) {
			name = method.getName();
			
			//check if this method is a main method
			if (name.equals("main"))
				check_main(method);
			
			//check that if this method's name exists in one of the super classes
			//then it overrides/hides it correctly
			super_method_uses(icClass,method);
			
			SymbolTable stMethod = new SymbolTable(name, icClass.getenclosingScope(),SymbolTableTypes.Method);
			icClass.getenclosingScope().addChild(stMethod);
			method.setenclosingScope(stMethod);
			icClass.getenclosingScope().addEntry(name,new Symbol(name, method.getEnclosingType(), get_method_kind(method.getClass().toString())),method.getLine());
		}

		/* call visitor on fields and methods */
		for (Field field : icClass.getFields()) {
			field.setenclosingScope(icClass.getenclosingScope());
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
		//add $ret to symbol table
		method.getenclosingScope().addEntry("$ret",new Symbol("$ret", method.getType().getEnclosingType(), Kind.RET_VAR),method.getLine());
		
		String name;
		for (Formal formal : method.getFormals()){
			name = formal.getName();
			formal.setenclosingScope(method.getenclosingScope());
			method.getenclosingScope().addEntry(name,new Symbol(name, formal.getEnclosingType(), Kind.FORMAL),formal.getLine());
		}
		for (Statement statement : method.getStatements()){
			statement.setenclosingScope(method.getenclosingScope());
			statement.accept(this);
		}
	}
	
	private IC.Types.Type getTheClassType(SymbolTable st){
		String name = st.getID();
		return classes.get(name).getEnclosingType();
	}
	
	@Override
	public Object visit(VirtualMethod method) {
		methodVisit(method);
		method.getenclosingScope().addEntry("this", new Symbol("this",getTheClassType(method.getenclosingScope().getParentSymbolTable()),Kind.THIS), method.getLine());
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
		return null;
	}

	@Override
	public Object visit(PrimitiveType type) {
		return null;
	}

	@Override
	public Object visit(UserType type) {
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		assignment.getVariable().setenclosingScope(assignment.getenclosingScope());
		assignment.getVariable().accept(this);
		assignment.getAssignment().setenclosingScope(assignment.getenclosingScope());
		assignment.getAssignment().accept(this);
		
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		callStatement.getCall().setenclosingScope(callStatement.getenclosingScope());
		callStatement.getCall().accept(this);
		return null;
	}

	@Override
	public Object visit(Return returnStatement) {
		if (returnStatement.hasValue()) {
			returnStatement.getValue().setenclosingScope(returnStatement.getenclosingScope());
			returnStatement.getValue().accept(this);
		}
		return null;
	}

	@Override
	public Object visit(If ifStatement) {

		ifStatement.getCondition().setenclosingScope(ifStatement.getenclosingScope());
		ifStatement.getCondition().accept(this);
		
		ifStatement.getOperation().setenclosingScope(ifStatement.getenclosingScope());
		ifStatement.getOperation().accept(this);
		
		if (ifStatement.hasElse()){
			ifStatement.getElseOperation().setenclosingScope(ifStatement.getenclosingScope());
			ifStatement.getElseOperation().accept(this);
		}
		
		return ifStatement.getenclosingScope();
	}

	@Override
	public Object visit(While whileStatement) {
		
		whileStatement.getCondition().setenclosingScope(whileStatement.getenclosingScope());
		whileStatement.getCondition().accept(this);
		
		whileStatement.getOperation().setenclosingScope(whileStatement.getenclosingScope());
		whileStatement.getOperation().accept(this);
		
		return whileStatement.getenclosingScope();
	}

	@Override
	public Object visit(Break breakStatement) {
		return null;
	}

	@Override
	public Object visit(Continue continueStatement) {
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		SymbolTable st = new SymbolTable(statementsBlock.toString(),statementsBlock.getenclosingScope(),SymbolTableTypes.StatementBlock);
		statementsBlock.getenclosingScope().addChild(st);
		statementsBlock.setenclosingScope(st);
		for(Statement statement : statementsBlock.getStatements()){
			statement.setenclosingScope(st);
			statement.accept(this);
		}
		
		return st;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		String name = localVariable.getName();
		
		Symbol varSym = new Symbol(name, localVariable.getEnclosingType(),
				Kind.VAR);
		// add variable to symbol table
		localVariable.getenclosingScope().addEntry(name, varSym,
				localVariable.getLine());
		
		localVariable.getType().setenclosingScope(localVariable.getenclosingScope());
		localVariable.getType().accept(this);
		
		if (localVariable.hasInitValue()){
			localVariable.getInitValue().setenclosingScope(localVariable.getenclosingScope());
			localVariable.getInitValue().accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(VariableLocation location) {
		location.setActualST(location.getenclosingScope());
		if (location.isExternal()) {
			location.getLocation().setenclosingScope(location.getenclosingScope());
			location.getLocation().accept(this);
		}
		else{
			SymbolTable enclosingScope = location.getenclosingScope().getVariableScope(location.getName());
			if (enclosingScope==null)
				throw new SemanticError("Variable "+location.getName()+" has been used before being declared.",location.getLine());
			location.setenclosingScope(enclosingScope);
		}
		return null;
	}

	@Override
	public Object visit(ArrayLocation location) {
		location.getArray().setenclosingScope(location.getenclosingScope());
		location.getArray().accept(this);
		location.getIndex().setenclosingScope(location.getenclosingScope());
		location.getIndex().accept(this);
		return null;
	}

	private void callVisit(Call call) {
		for (Expression exp : call.getArguments()) {
			exp.setenclosingScope(call.getenclosingScope());
			exp.accept(this);
		}
	}
	
	@Override
	public Object visit(StaticCall call) {
		callVisit(call);
		return null;
	}

	@Override
	public Object visit(VirtualCall call) {
		if (call.isExternal()) {
			call.getLocation().setenclosingScope(call.getenclosingScope());
			call.getLocation().accept(this);
		}
		callVisit(call);
		return null;
	}

	@Override
	public Object visit(This thisExpression) {
		return null;
	}

	@Override
	public Object visit(NewClass newClass) {
		return null;
	}

	@Override
	public Object visit(NewArray newArray) {
		newArray.getSize().setenclosingScope(newArray.getenclosingScope());
		newArray.getSize().accept(this);
		return null;
	}

	@Override
	public Object visit(Length length) {
		length.getArray().setenclosingScope(length.getenclosingScope());
		length.getArray().accept(this);
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		binaryOp.getFirstOperand().setenclosingScope(binaryOp.getenclosingScope());
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().setenclosingScope(binaryOp.getenclosingScope());
		binaryOp.getSecondOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		binaryOp.getFirstOperand().setenclosingScope(binaryOp.getenclosingScope());
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().setenclosingScope(binaryOp.getenclosingScope());
		binaryOp.getSecondOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		unaryOp.getOperand().setenclosingScope(unaryOp.getenclosingScope());
		unaryOp.getOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		unaryOp.getOperand().setenclosingScope(unaryOp.getenclosingScope());
		unaryOp.getOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(Literal literal) {
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		expressionBlock.getExpression().setenclosingScope(expressionBlock.getenclosingScope());
		expressionBlock.getExpression().accept(this);
		return null;
	}

}
