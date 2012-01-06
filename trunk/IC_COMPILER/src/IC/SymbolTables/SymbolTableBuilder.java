package IC.SymbolTables;

import IC.AST.*;

public class SymbolTableBuilder implements Visitor {

	public Object visit(Program program) {
		SymbolTable st = new SymbolTable("Root");
		String className;
		for (ICClass icClass : program.getClasses()) {
			className = icClass.getName();
			st.addEntry(className,
					new Symbol(className, icClass.getEnclosingType(),
							Kind.CLASS));
		}
		program.addenclosingScope(st);
		for (ICClass icClass : program.getClasses()) {
			SymbolTable stClass = new SymbolTable(icClass.getName(), st);
			icClass.addenclosingScope(stClass);
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
			SymbolTable stMethod = new SymbolTable(name, icClass.getenclosingScope());
			method.addenclosingScope(stMethod);
			icClass.getenclosingScope().addEntry(name,
					new Symbol(name, method.getEnclosingType(), Kind.METHOD));
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
	public Object visit(If ifStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(While whileStatement) {
		// TODO Auto-generated method stub
		return null;
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
		SymbolTable st = new SymbolTable(statementsBlock.toString(),statementsBlock.getenclosingScope());
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
