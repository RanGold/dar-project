package IC.AST;

import IC.SymbolTables.Symbol;
import IC.SymbolTables.SymbolTable;
import IC.SymbolTables.SymbolTableTypes;

/**
 * Pretty printing visitor - travels along the AST and prints info about each
 * node, in an easy-to-comprehend format.
 * 
 * @author Tovi Almozlino
 */
public class PrettyPrinter implements Visitor {

	private int depth = 0; // depth of indentation

	private String ICFilePath;
	
	private boolean doIndents;
	
	private static int arraySize;

	/**
	 * Constructs a new pretty printer visitor.
	 * 
	 * @param ICFilePath
	 *            The path + name of the IC file being compiled.
	 */
	public PrettyPrinter(String ICFilePath, boolean doIndents) {
		this.ICFilePath = ICFilePath;
		this.doIndents = doIndents;
	}

	private void indent(StringBuffer output, ASTNode node) {
		output.append("\n");
		if (this.doIndents) {
			for (int i = 0; i < depth; ++i) {
				output.append(" ");
			}
		}
		if (node != null)
			output.append(node.getLine() + ": ");
	}

	private void indent(StringBuffer output) {
		indent(output, null);
	}
	
	private String stmtBlockLocation(SymbolTable st){
		String output="";
		if (st.getType().equals(SymbolTableTypes.StatementBlock))
			output = "statement block in " + st.stmtBlockLocation();
		else if (st.getType().equals(SymbolTableTypes.Global))
			output = "Global";
		else
			output = st.getID();
		return output;
	}
	
	private String addSymbolTableEntry(SymbolTable st,String name){
		st = st.getVariableScope(name);
		String output = ", Type: ";
		Symbol symbol = st.getEntry(name);
		output += symbol.getType().toString() + ", Symbol table: " + stmtBlockLocation(st);
		return output;
	}
	
	private String addSymbolTableEntryNonRecursive(SymbolTable st,String name){
		String output = ", Type: ";
		Symbol symbol = st.getEntryRecursive(name);
		output += symbol.getType().toString() + ", Symbol table: " + stmtBlockLocation(st);
		return output;
	}
	
	private String addSymbolTableEntry(SymbolTable st){
		String output = ", Symbol table: " + stmtBlockLocation(st);
		return output;
	}

	public Object visit(Program program) {
		StringBuffer output = new StringBuffer();

		indent(output);
		output.append("Abstract Syntax Tree: " + ICFilePath + "\n");
		for (ICClass icClass : program.getClasses())
			output.append(icClass.accept(this));
		return output.toString();
	}

	public Object visit(ICClass icClass) {
		StringBuffer output = new StringBuffer();
		
		indent(output, icClass);
		output.append("Declaration of class: " + icClass.getName());
		if (icClass.hasSuperClass())
			output.append(", subclass of " + icClass.getSuperClassName());
		output.append(addSymbolTableEntry(icClass.getenclosingScope(),icClass.getName()));
		depth += 2;
		for (Field field : icClass.getFields())
			output.append(field.accept(this));
		for (Method method : icClass.getMethods())
			output.append(method.accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(PrimitiveType type) {
		StringBuffer output = new StringBuffer();

		indent(output, type);
		output.append("Primitive data type: ");
		if (type.getDimension() > 0)
			output.append(type.getDimension() + "-dimensional array of ");
		output.append(type.getName());
		return output.toString();
	}

	public Object visit(UserType type) {
		StringBuffer output = new StringBuffer();

		indent(output, type);
		output.append("User-defined data type: ");
		if (type.getDimension() > 0)
			output.append(type.getDimension() + "-dimensional array of ");
		output.append(type.getName());
		return output.toString();
	}

	public Object visit(Field field) {
		StringBuffer output = new StringBuffer();

		indent(output, field);
		output.append("Declaration of field: " + field.getName());
		output.append(addSymbolTableEntry(field.getenclosingScope(),field.getName()));
		++depth;
		output.append(field.getType().accept(this));
		--depth;
		return output.toString();
	}

	public Object visit(LibraryMethod method) {
		StringBuffer output = new StringBuffer();

		indent(output, method);
		output.append("Declaration of library method: " + method.getName());
		output.append(addSymbolTableEntry(method.getenclosingScope(),method.getName()));
		depth += 2;
		output.append(method.getType().accept(this));
		for (Formal formal : method.getFormals())
			output.append(formal.accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(Formal formal) {
		StringBuffer output = new StringBuffer();

		indent(output, formal);
		output.append("Parameter: " + formal.getName());
		output.append(addSymbolTableEntry(formal.getenclosingScope(),formal.getName()));
		++depth;
		output.append(formal.getType().accept(this));
		--depth;
		return output.toString();
	}

	public Object visit(VirtualMethod method) {
		StringBuffer output = new StringBuffer();

		indent(output, method);
		output.append("Declaration of virtual method: " + method.getName());
		output.append(addSymbolTableEntry(method.getenclosingScope(),method.getName()));
		depth += 2;
		output.append(method.getType().accept(this));
		for (Formal formal : method.getFormals())
			output.append(formal.accept(this));
		for (Statement statement : method.getStatements())
			output.append(statement.accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(StaticMethod method) {
		StringBuffer output = new StringBuffer();

		indent(output, method);
		output.append("Declaration of static method: " + method.getName());
		output.append(addSymbolTableEntry(method.getenclosingScope(),method.getName()));
		depth += 2;
		output.append(method.getType().accept(this));
		for (Formal formal : method.getFormals())
			output.append(formal.accept(this));
		for (Statement statement : method.getStatements())
			output.append(statement.accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(Assignment assignment) {
		StringBuffer output = new StringBuffer();

		indent(output, assignment);
		output.append("Assignment statement");
		output.append(addSymbolTableEntry(assignment.getenclosingScope()));
		depth += 2;
		output.append(assignment.getVariable().accept(this));
		output.append(assignment.getAssignment().accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(CallStatement callStatement) {
		StringBuffer output = new StringBuffer();

		indent(output, callStatement);
		output.append("Method call statement");
		output.append(addSymbolTableEntry(callStatement.getenclosingScope()));
		++depth;
		output.append(callStatement.getCall().accept(this));
		--depth;
		return output.toString();
	}

	public Object visit(Return returnStatement) {
		StringBuffer output = new StringBuffer();

		indent(output, returnStatement);
		output.append("Return statement");
		if (returnStatement.hasValue())
			output.append(", with return value");
		output.append(addSymbolTableEntry(returnStatement.getenclosingScope()));
		if (returnStatement.hasValue()) {
			++depth;
			output.append(returnStatement.getValue().accept(this));
			--depth;
		}
		return output.toString();
	}

	public Object visit(If ifStatement) {
		StringBuffer output = new StringBuffer();

		indent(output, ifStatement);
		output.append("If statement");
		if (ifStatement.hasElse())
			output.append(", with Else operation");
		output.append(addSymbolTableEntry(ifStatement.getenclosingScope()));
		depth += 2;
		output.append(ifStatement.getCondition().accept(this));
		output.append(ifStatement.getOperation().accept(this));
		if (ifStatement.hasElse())
			output.append(ifStatement.getElseOperation().accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(While whileStatement) {
		StringBuffer output = new StringBuffer();

		indent(output, whileStatement);
		output.append("While statement");
		output.append(addSymbolTableEntry(whileStatement.getenclosingScope()));
		depth += 2;
		output.append(whileStatement.getCondition().accept(this));
		output.append(whileStatement.getOperation().accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(Break breakStatement) {
		StringBuffer output = new StringBuffer();

		indent(output, breakStatement);
		output.append("Break statement");
		output.append(addSymbolTableEntry(breakStatement.getenclosingScope()));
		return output.toString();
	}

	public Object visit(Continue continueStatement) {
		StringBuffer output = new StringBuffer();

		indent(output, continueStatement);
		output.append("Continue statement");
		output.append(addSymbolTableEntry(continueStatement.getenclosingScope()));
		return output.toString();
	}

	public Object visit(StatementsBlock statementsBlock) {
		StringBuffer output = new StringBuffer();

		indent(output, statementsBlock);
		output.append("Block of statements");
		output.append(", Symbol table: " + statementsBlock.getenclosingScope().stmtBlockLocation());
		depth += 2;
		for (Statement statement : statementsBlock.getStatements())
			output.append(statement.accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(LocalVariable localVariable) {
		StringBuffer output = new StringBuffer();

		indent(output, localVariable);
		output.append("Declaration of local variable: "
				+ localVariable.getName());
		if (localVariable.hasInitValue()) {
			output.append(", with initial value");
			++depth;
		}
		output.append(addSymbolTableEntry(localVariable.getenclosingScope(),localVariable.getName()));
		++depth;
		output.append(localVariable.getType().accept(this));
		if (localVariable.hasInitValue()) {
			output.append(localVariable.getInitValue().accept(this));
			--depth;
		}
		--depth;
		return output.toString();
	}

	public Object visit(VariableLocation location) {
		StringBuffer output = new StringBuffer();

		indent(output, location);
		output.append("Reference to variable: " + location.getName());
		if (location.isExternal())
			output.append(", in external scope");
		output.append(", Type: "+location.getenclosingScope().getEntryRecursive(location.getName()).getType());
		output.append(addSymbolTableEntry(location.getActualST()));
		if (location.isExternal()) {
			++depth;
			output.append(location.getLocation().accept(this));
			--depth;
		}
		return output.toString();
	}

	private String getArrayName(Expression location){
		if (!(location instanceof VariableLocation))
			return getArrayName(((ArrayLocation) location).getArray());
		return ((VariableLocation)location).getName();
	}
	
	public Object visit(ArrayLocation location) {
		arraySize++;
		StringBuffer output = new StringBuffer();

		indent(output, location);
		output.append("Reference to array");
		String type = location.getenclosingScope().getEntryRecursive(getArrayName(location.getArray())).getType().toString();
		output.append(", Type: "+type.substring(0, type.length()-2*arraySize));
		output.append(addSymbolTableEntry(location.getenclosingScope()));
		depth += 2;
		output.append(location.getArray().accept(this));
		output.append(location.getIndex().accept(this));
		depth -= 2;
		arraySize--;
		return output.toString();
	}

	public Object visit(StaticCall call) {
		StringBuffer output = new StringBuffer();

		indent(output, call);
		output.append("Call to static method: " + call.getName()
				+ ", in class " + call.getClassName());
		output.append(", Type: void");
		output.append(addSymbolTableEntry(call.getenclosingScope()));
		depth += 2;
		for (Expression argument : call.getArguments())
			output.append(argument.accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(VirtualCall call) {
		StringBuffer output = new StringBuffer();
		
		indent(output, call);
		output.append("Call to virtual method: " + call.getName());
		if (call.isExternal())
			output.append(", in external scope");
		output.append(", Type: void");

		output.append(addSymbolTableEntry(call.getenclosingScope()));
		depth += 2;
		if (call.isExternal())
			output.append(call.getLocation().accept(this));
		for (Expression argument : call.getArguments())
			output.append(argument.accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(This thisExpression) {
		StringBuffer output = new StringBuffer();

		indent(output, thisExpression);
		output.append("Reference to 'this' instance");
		output.append(addSymbolTableEntry(thisExpression.getenclosingScope()));
		return output.toString();
	}

	public Object visit(NewClass newClass) {
		StringBuffer output = new StringBuffer();

		indent(output, newClass);
		output.append("Instantiation of class: " + newClass.getName());
		output.append(addSymbolTableEntryNonRecursive(newClass.getenclosingScope(),newClass.getName()));
		return output.toString();
	}

	public Object visit(NewArray newArray) {
		StringBuffer output = new StringBuffer();

		indent(output, newArray);
		output.append("Array allocation");
		output.append(", Type: "+newArray.getEnclosingType());
		output.append(addSymbolTableEntry(newArray.getenclosingScope()));
		depth += 2;
		output.append(newArray.getType().accept(this));
		output.append(newArray.getSize().accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(Length length) {
		StringBuffer output = new StringBuffer();

		indent(output, length);
		output.append("Reference to array length");
		output.append(addSymbolTableEntry(length.getenclosingScope()));
		++depth;
		output.append(length.getArray().accept(this));
		--depth;
		return output.toString();
	}

	public Object visit(MathBinaryOp binaryOp) {
		StringBuffer output = new StringBuffer();

		indent(output, binaryOp);
		output.append("Mathematical binary operation: "
				+ binaryOp.getOperator().getDescription());
		output.append(", Type: "+binaryOp.getFirstOperand().getEnclosingType());
		output.append(addSymbolTableEntry(binaryOp.getenclosingScope()));
		depth += 2;
		output.append(binaryOp.getFirstOperand().accept(this));
		output.append(binaryOp.getSecondOperand().accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(LogicalBinaryOp binaryOp) {
		StringBuffer output = new StringBuffer();

		indent(output, binaryOp);
		output.append("Logical binary operation: "
				+ binaryOp.getOperator().getDescription());
		depth += 2;
		output.append(binaryOp.getFirstOperand().accept(this));
		output.append(binaryOp.getSecondOperand().accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(MathUnaryOp unaryOp) {
		StringBuffer output = new StringBuffer();

		indent(output, unaryOp);
		output.append("Mathematical unary operation: "
				+ unaryOp.getOperator().getDescription());
		++depth;
		output.append(unaryOp.getOperand().accept(this));
		--depth;
		return output.toString();
	}

	public Object visit(LogicalUnaryOp unaryOp) {
		StringBuffer output = new StringBuffer();

		indent(output, unaryOp);
		output.append("Logical unary operation: "
				+ unaryOp.getOperator().getDescription());
		++depth;
		output.append(unaryOp.getOperand().accept(this));
		--depth;
		return output.toString();
	}

	public Object visit(Literal literal) {
		StringBuffer output = new StringBuffer();

		indent(output, literal);
		output.append(literal.getType().getDescription() + ": "
				+ literal.getType().toFormattedString(literal.getValue()));
		return output.toString();
	}

	public Object visit(ExpressionBlock expressionBlock) {
		StringBuffer output = new StringBuffer();

		indent(output, expressionBlock);
		output.append("Parenthesized expression");
		++depth;
		output.append(expressionBlock.getExpression().accept(this));
		--depth;
		return output.toString();
	}
}