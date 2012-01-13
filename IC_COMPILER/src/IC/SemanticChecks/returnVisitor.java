package IC.SemanticChecks;

import IC.AST.*;
import IC.Types.MethodType;
import IC.Types.TypeTable;


public class returnVisitor implements Visitor {

	public Object visit(Program program) {
		for (ICClass icClass : program.getClasses()) {
			if (!(Boolean)icClass.accept(this))
				return false;
		}
		return true;
	}

	public Object visit(ICClass icClass) {
		for (Method method : icClass.getMethods()) {
			if (!(Boolean)method.accept(this))
				return false;
		}
		return true;
	}
	
	public Object visit(Field field) {
		return false;
	}

	// Helper function - for all types of method
	private Object methodVisit(Method method){
		for (Statement s : method.getStatements()){
			if ((Boolean) s.accept(this)) 
				return true;
		}
		if (method.getEnclosingType() instanceof IC.Types.MethodType){
			IC.Types.MethodType mt = (MethodType) method.getEnclosingType();
			if (mt.getReturnVal()==TypeTable.voidType)
				return true;
		}
		System.err.println("Warning: Method " + method.getName() +" in line "+ method.getLine() +
				" does not have a return statement at each path");
		return false;
	}
	
	public Object visit(VirtualMethod method) {
		return methodVisit(method);
	}

	public Object visit(StaticMethod method) {
		return methodVisit(method);
	}

	public Object visit(LibraryMethod method) {
		return methodVisit(method);
	}

	public Object visit(Formal formal) {
		return false;
	}

	@Override
	public Object visit(PrimitiveType type) {
		return false;
	}

	@Override
	public Object visit(UserType type) {
		return false;
	}

	public Object visit(Assignment assignment) {
		return false;
	}

	public Object visit(CallStatement callStatement) {
		return callStatement.getCall().accept(this);
	}
	
	public Object visit(Return returnStatement) {

		return true;
	}

	public Object visit(If ifStatement) {
		// Check if condition of type boolean
		ifStatement.getCondition().accept(this);
		
		// Visit the operation in ifStatement

		// Visit the else operation if exists
		if ( (Boolean) ifStatement.getOperation().accept(this) && ifStatement.hasElse()){
			if ((Boolean) ifStatement.getElseOperation().accept(this))
				return true;
		}
		return false;
	}

	public Object visit(While whileStatement) {	
		// Check the operation in whileStatement
		return false;
	}

	public Object visit(Break breakStatement) {
		return false;
	}

	public Object visit(Continue continueStatement) {
		return false;
	}

	public Object visit(StatementsBlock statementsBlock) {
		for (Statement s : statementsBlock.getStatements()){
			if ((Boolean) s.accept(this)) return true;
		}
		return false;
	}

	
	public Object visit(LocalVariable localVariable) {
		// check initial value of local variable
		if (localVariable.hasInitValue()) {
			localVariable.getInitValue().accept(this);
		}
		return false;
	}

	@Override
	public Object visit(VariableLocation location) {	
		return false;
	}

	public Object visit(ArrayLocation location) {
		return false;
	}

	
	public Object visit(StaticCall call) {
		return false;
	}

	public Object visit(VirtualCall call) {
		return false;
	}

	public Object visit(This thisExpression) {
		return false;
	}

	public Object visit(NewClass newClass) {
		return false;
	}


	public Object visit(NewArray newArray) {
		return false;
	}

	public Object visit(Length length) {
		return false;
	}

	public Object visit(MathBinaryOp binaryOp) {
		return false;
	}

	public Object visit(LogicalBinaryOp binaryOp) {
		return false;
	}

	public Object visit(MathUnaryOp unaryOp) {
        return false;
	}

	public Object visit(LogicalUnaryOp unaryOp) {
        return false;
	}

	public Object visit(Literal literal) {
		return false;
	}

	public Object visit(ExpressionBlock expressionBlock) {
		return expressionBlock.getExpression().accept(this);
	}
}
