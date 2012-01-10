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
	
	public SymbolTableBuilder(String path){
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
			icClass.setenclosingScope(stClass);
			icClass.accept(this);
		}
		if (!seen_main)
			throw new SemanticError("No main message has been defined");
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
			//return Kind.LIBRARY_METHOD;
			return Kind.STATIC_METHOD;
		else
			return Kind.METHOD;
	}

	private void check_main(Method method){
		//check that no main message has been already defined
		if (seen_main)
			throw new SemanticError("More than one main method was defined",method.getLine());
		//check that the method is static
		if(!get_method_kind(method.getClass().toString()).equals(Kind.STATIC_METHOD))
			throw new SemanticError("The main method must be a static method",method.getLine());
		//check correct number of parameters
		if (method.getFormals().size()!=1)
			throw new SemanticError("Wrong number of arguments for main method",method.getLine());
		//check parameter's type
		if (!method.getFormals().get(0).getType().getEnclosingType().subtypeof(TypeTable.arrayType(TypeTable.stringType)) || method.getFormals().get(0).getType().getDimension()!=1)
			throw new SemanticError("The type of the input parameter of the main method must be \"string[]\"",method.getLine());
		//check name of input parameter
		if (!method.getFormals().get(0).getName().equals("args"))
			throw new SemanticError("Input parameter in main method must be named \"args\"",method.getLine());
		//check return parameter
		if (!method.getType().getEnclosingType().subtypeof(TypeTable.voidType))
			throw new SemanticError("main method must return void",method.getLine());
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
						if (type1!=type2 && !type2.subtypeof(type1))
							throw new SemanticError("Overloading of methods is not supported",line);
					}
					//check that the return type is correctly overridden
					IC.Types.Type type1 = method_type.getReturnVal();
					IC.Types.Type type2 = methodTOoverride_type.getReturnVal();
					if (type1!=type2 && !type1.subtypeof(type2))
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
//		for (Field field : icClass.getFields()) {
//			field.accept(this);
//		}
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
			method.getenclosingScope().addEntry(name,new Symbol(name, formal.getEnclosingType(), Kind.FORMAL),formal.getLine());
		}
		for (Statement statement : method.getStatements()){
			statement.setenclosingScope(method.getenclosingScope());
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

	@Override//TODO add $ret here
	public Object visit(Return returnStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(If ifStatement) {

//		ifStatement.getCondition().setenclosingScope(ifStatement.getenclosingScope());//TODO needed?
//		ifStatement.getCondition().accept(this);
		
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
		
//		whileStatement.getCondition().setenclosingScope(whileStatement.getenclosingScope());//TODO needed?
//		whileStatement.getCondition().accept(this);
		
		whileStatement.getOperation().setenclosingScope(whileStatement.getenclosingScope());
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
		localVariable.getenclosingScope().addEntry(name, new Symbol(name,localVariable.getEnclosingType(),Kind.VAR),localVariable.getLine());
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