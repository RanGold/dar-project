package IC.SemanticChecks;

import IC.BinaryOps;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.Call;
import IC.AST.CallStatement;
import IC.AST.Continue;
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
import IC.AST.PrimitiveType;
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
import IC.SymbolTables.Kind;
import IC.SymbolTables.Symbol;
import IC.Types.ClassType;
import IC.Types.MethodType;
import IC.Types.Type;
import IC.Types.TypeTable;

public class BonusVisitor implements Visitor {
	
	private int inLoop = 0;

	public Object visit(Program program) {
		for (ICClass icClass : program.getClasses()) {
			icClass.accept(this);
		}
		return true;
	}

	public Object visit(ICClass icClass) {
		for (Method method : icClass.getMethods()) {
			method.accept(this);
		}
		return true;
	}
	
	public Object visit(Field field) {
		return true;
	}

	// Helper function - for all types of method
	private Object methodVisit(Method method, boolean isStatic){
		// TODO many checks here
		for (Statement s : method.getStatements()){
			s.accept(this);
		}
		return true;
	}
	
	public Object visit(VirtualMethod method) {
		return methodVisit(method, false);
	}

	public Object visit(StaticMethod method) {
		return methodVisit(method, true);
	}

	public Object visit(LibraryMethod method) {
		return methodVisit(method, true);
	}

	public Object visit(Formal formal) {
		return true;
	}

	@Override
	public Object visit(PrimitiveType type) {
		return true;
	}

	@Override
	public Object visit(UserType type) {
		return true;
	}

	public Object visit(Assignment assignment) {
		// TODO many checks here
		assignment.getVariable().accept(this);
		assignment.getAssignment().accept(this);
		return true;
	}

	public Object visit(CallStatement callStatement) {
		if (callStatement.getCall().accept(this) == null) return null;
		return true;
	}
	
	public Object visit(Return returnStatement) {
		Type returnValueType;
		if (returnStatement.hasValue()) {
			returnValueType = (Type) returnStatement.getValue().accept(this);
		} else {
			returnValueType = TypeTable.voidType;
		}
		
		return returnValueType;
	}

	public Object visit(If ifStatement) {
		// Check if condition of type boolean
		ifStatement.getCondition().accept(this);
		
		// Visit the operation in ifStatement
		ifStatement.getOperation().accept(this);
		
		// Visit the else operation if exists
		if (ifStatement.hasElse()){
			ifStatement.getElseOperation().accept(this);
		}
		return true;
	}

	public Object visit(While whileStatement) {
		// Check while condition of type boolean
		whileStatement.getCondition().accept(this);
		
		inLoop++;
		
		// Check the operation in whileStatement
		whileStatement.getOperation().accept(this);
		inLoop--;
		
		return true;
	}

	public Object visit(Break breakStatement) {
		return true;
	}

	public Object visit(Continue continueStatement) {
		return true;
	}

	public Object visit(StatementsBlock statementsBlock) {
		for (Statement s : statementsBlock.getStatements()){
			s.accept(this);
		}
		return true;
	}

	
	public Object visit(LocalVariable localVariable) {
		// check initial value of local variable
		if (localVariable.hasInitValue()) {
			localVariable.getInitValue().accept(this);
		}
		return true;
	}

	@Override
	public Object visit(VariableLocation location) {	
		
		if (!location.isExternal()) {
			Symbol locSym = location.getenclosingScope().getEntry(location.getName());
			return (locSym.getKind() == Kind.FIELD || locSym.getKind() == Kind.FORMAL || (locSym.getKind() == Kind.VAR && locSym.isInitialized()));
		} else {
			return true;
		}
	}

	public Object visit(ArrayLocation location) {
		// check index is int type
		Boolean resIndex = (Boolean)location.getIndex().accept(this);

		// check array is an array type
		Boolean resArray = (Boolean)location.getArray().accept(this);
		
		return resArray && resIndex;
	}

	private Object checkCallArguments(Call call, Symbol methodSym) {
		MethodType methodType = (MethodType)methodSym.getType();
		
		for (int i = 0; i < methodType.getArguments().size(); i++) {
			Type curArgType = (Type)call.getArguments().get(i).accept(this); 
		}
		
		return methodType.getReturnVal();
	}
	
	public Object visit(StaticCall call) {
		Symbol classSym = call.getenclosingScope().getEntryRecursive(call.getClassName());
		
		ICClass classRef = ((ClassType)classSym.getType()).getICClass();
		Symbol methodSym = classRef.getenclosingScope().getEntry(call.getName());
		return checkCallArguments(call, methodSym);
	}

	public Object visit(VirtualCall call) {
		Symbol methodSym = null;
		if (!call.isExternal()) {
			methodSym = call.getenclosingScope().getEntryRecursive(call.getName());
		} else {
			Type locType = (Type) call.getLocation().accept(this);
			methodSym = ((ClassType) locType).getICClass().getenclosingScope()
					.getEntryRecursive(call.getName());
		}
		
		return checkCallArguments(call, methodSym);
	}

	public Object visit(This thisExpression) {
		return thisExpression.getenclosingScope().getEntry("this").getType();
	}

	public Object visit(NewClass newClass) {
		Type newClasstype = TypeTable.getClassType(newClass);
		return newClasstype;
	}


	public Object visit(NewArray newArray) {
		// check the size is integer type
		Type sizeType = (Type) newArray.getSize().accept(this);
		
		// return the array type
		return newArray.getEnclosingType();
	}

	public Object visit(Length length) {
		// check array is of array type
		Type arrType = (Type) length.getArray().accept(this);

		// return int
		return TypeTable.intType;
	}


	public Object visit(MathBinaryOp binaryOp) {
		Type binaryOpType1 = (Type) binaryOp.getFirstOperand().accept(this);
		Type binaryOpType2 = (Type) binaryOp.getSecondOperand().accept(this);
		if (binaryOpType1 == null || binaryOpType2 == null) {
			return null;
		}

		// if binaryOp is '+' types are both int or both string
		if (binaryOp.getOperator().equals(BinaryOps.PLUS)) {
			if (binaryOpType1.subtypeof(TypeTable.stringType)
					&& binaryOpType2.subtypeof(TypeTable.stringType)) {
				return TypeTable.stringType;
			}
		}
		
		// else binaryOp is only on int types
		return TypeTable.intType;
	}

	public Object visit(LogicalBinaryOp binaryOp) {
		Type binaryOpType1 = (Type) binaryOp.getFirstOperand().accept(this);
		Type binaryOpType2 = (Type) binaryOp.getSecondOperand().accept(this);

		return TypeTable.boolType;
	}

	public Object visit(MathUnaryOp unaryOp) {
        Type unaryOpType = (Type) unaryOp.getOperand().accept(this);
        return TypeTable.intType;
	}

	public Object visit(LogicalUnaryOp unaryOp) {
        Type unaryOpType = (Type) unaryOp.getOperand().accept(this);
        return TypeTable.boolType;
}



	public Object visit(Literal literal) {
		//return literal.getEnclosingType();
		return true;
	}


	public Object visit(ExpressionBlock expressionBlock) {
		return expressionBlock.getExpression().accept(this);
	}

}
