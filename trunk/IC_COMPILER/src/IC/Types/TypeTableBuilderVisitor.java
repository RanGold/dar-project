package IC.Types;

import java.util.LinkedList;
import java.util.List;

import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.Call;
import IC.AST.CallStatement;
import IC.AST.Continue;
import IC.AST.Expression;
import IC.AST.ExpressionBlock;
import IC.AST.Field;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.If;
import IC.AST.Length;
import IC.AST.LibraryMethod;
import IC.AST.Literal;
import IC.AST.LocalVariable;
import IC.AST.LogicalBinaryOp;
import IC.AST.LogicalUnaryOp;
import IC.AST.MathBinaryOp;
import IC.AST.MathUnaryOp;
import IC.AST.Method;
import IC.AST.NewArray;
import IC.AST.NewClass;
import IC.AST.Program;
import IC.AST.Return;
import IC.AST.Statement;
import IC.AST.StatementsBlock;
import IC.AST.StaticCall;
import IC.AST.StaticMethod;
import IC.AST.This;
import IC.AST.UserType;
import IC.AST.VariableLocation;
import IC.AST.VirtualCall;
import IC.AST.VirtualMethod;
import IC.AST.Visitor;
import IC.AST.While;
import IC.SemanticChecks.SemanticError;

public class TypeTableBuilderVisitor implements Visitor {
	
	@Override
	public Object visit(Program program) {
		for (ICClass icClass : program.getClasses()) {
			TypeTable.classType(icClass);
		}
		
		for (ICClass icClass : program.getClasses()) {
			icClass.accept(this);
		}
		
		TypeTable.validateTypesTable();
		return null;
	}

	@Override
	public Object visit(ICClass icClass) {
		 for (Field field : icClass.getFields()){
			 field.accept(this);
		 }
		 
		 for (Method method : icClass.getMethods()){
			 method.accept(this);
		 }
		 
		 ClassType type = TypeTable.classType(icClass);
		 icClass.setEnclosingType(type);
		 return type;
	}

	@Override
	public Object visit(Field field) {
		Type type = (Type)field.getType().accept(this);
		field.setEnclosingType(type);
		return type;
	}

	private MethodType methodVisit(Method method) {
		for (Statement stmt : method.getStatements()) {
			stmt.accept(this);
		}
		
		List<Type> formalTypes = new LinkedList<Type>();
		for (Formal formal : method.getFormals()) {
			formalTypes.add((Type)formal.accept(this));
		}
		
		Type retVal = (Type)method.getType().accept(this);
		
		MethodType type = TypeTable.methodType(formalTypes, retVal);
		method.setEnclosingType(type);
		return type;
	}
	
	@Override
	public Object visit(VirtualMethod method) {
		return this.methodVisit(method);
	}

	@Override
	public Object visit(StaticMethod method) {
		return this.methodVisit(method);
	}

	@Override
	public Object visit(LibraryMethod method) {
		return this.methodVisit(method);
	}

	@Override
	public Object visit(Formal formal) {
		Type retVal = (Type)formal.getType().accept(this);
		formal.setEnclosingType(retVal);
		return retVal;
	}

	@Override
	public Object visit(IC.AST.PrimitiveType type) {
		Type curType = TypeTable.primitiveType(type.getName());
		for (int i = 0; i < type.getDimension(); i++) {
			curType = TypeTable.arrayType(curType);
		}
		
		type.setEnclosingType(curType);
		return curType;
	}

	@Override
	public Object visit(UserType type) {
		Type curType = TypeTable.getClassType(type);
		for (int i = 0; i < type.getDimension(); i++) {
			curType = TypeTable.arrayType(curType);
		}
		
		type.setEnclosingType(curType);
		return curType;
	}

	@Override
	public Object visit(Assignment assignment) {
		assignment.getVariable().accept(this);
		return assignment.getAssignment().accept(this);
	}

	@Override
	public Object visit(CallStatement callStatement) {
		callStatement.getCall().accept(this);
		callStatement.setEnclosingType(callStatement.getCall().getEnclosingType());
		return null;
	}

	@Override
	public Object visit(Return returnStatement) {
		returnStatement.getValue().accept(this);
		returnStatement.setEnclosingType(returnStatement.getValue().getEnclosingType());
		return returnStatement.getEnclosingType(); 
	}

	@Override
	public Object visit(If ifStatement) {
		ifStatement.getCondition().accept(this);
		ifStatement.getOperation().accept(this);
		if (ifStatement.hasElse()) {
			ifStatement.getElseOperation().accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(While whileStatement) {
		whileStatement.getCondition().accept(this);
		whileStatement.getOperation().accept(this);
		return null;
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
		for (Statement statement : statementsBlock.getStatements()) {
			statement.accept(this);
		}

		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		if (localVariable.hasInitValue()) {
			localVariable.getInitValue().accept(this);
		}
		
		Type type = (Type)localVariable.getType().accept(this);
		localVariable.setEnclosingType(type);
		return type;
	}

	@Override
	public Object visit(VariableLocation location) {
		if (location.isExternal()) {
			return location.getLocation().accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(ArrayLocation location) {
		location.getArray().accept(this);
		location.getIndex().accept(this);
		
		return null;
	}

	private void visitCall(Call call) {
		for (Expression expression : call.getArguments()) {
			expression.accept(this);
		}
	}
	
	@Override
	public Object visit(StaticCall call) {
		this.visitCall(call);
		return null;
	}

	@Override
	public Object visit(VirtualCall call) {
		this.visitCall(call);
		return null;
	}

	@Override
	public Object visit(This thisExpression) {
		return null;
	}

	@Override
	public Object visit(NewClass newClass) {
		return TypeTable.getClassType(newClass);
	}

	@Override
	public Object visit(NewArray newArray) {
		Type t = (Type)newArray.getType().accept(this);
		newArray.getSize().accept(this);
		newArray.setEnclosingType(t);
		return t;
	}

	@Override
	public Object visit(Length length) {
		return length.getArray().accept(this);
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		return unaryOp.getOperand().accept(this);
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		return unaryOp.getOperand().accept(this);
	}

	@Override
	public Object visit(Literal literal) {
		IC.LiteralTypes type = literal.getType();
		switch (type) {
		case STRING:
			 literal.setEnclosingType(TypeTable.stringType);
			 break;
		case INTEGER:
			literal.setEnclosingType(TypeTable.intType);
			break;
		case TRUE:
			literal.setEnclosingType(TypeTable.boolType);
			break;
		case FALSE:
			literal.setEnclosingType(TypeTable.boolType);
			break;
		case NULL:
			literal.setEnclosingType(TypeTable.nullType);
			break;
		default:
			throw new SemanticError("Undefined literal type", literal.getLine());
		}

		return literal.getEnclosingType();
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		return expressionBlock.getExpression().accept(this);
	}
}