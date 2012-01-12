package IC.SemanticChecks;

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
import IC.Types.ArrayType;
import IC.Types.ClassType;
import IC.Types.MethodType;
import IC.Types.Type;
import IC.Types.TypeTable;

public class TypeCheckVisitor implements Visitor {
	
	private int inLoop = 0;
	private boolean inStaticMethod;

	public Object visit(Program program) {
		for (ICClass icClass : program.getClasses()) {
			icClass.accept(this);
		}
		return true;
	}

	public Object visit(ICClass icClass) {
		// there is no reason to check the fields
		for (Method method : icClass.getMethods()) {
			method.accept(this);
		}
		return true;
	}
	
	public Object visit(Field field) {
		//the fields are always legal, this function never calls
		return true;
	}

	//helper function - for all types of method
	private Object method_visit(Method method, boolean isStatic){
		inStaticMethod = isStatic;
		for (Statement s : method.getStatements()){
			s.accept(this);
		}
		inStaticMethod = false;
		return true;
	}
	
	public Object visit(VirtualMethod method) {
		// TODO check overriding
		return method_visit(method, false);
	}

	public Object visit(StaticMethod method) {
		// TODO check overriding
		return method_visit(method, true);
	}

	public Object visit(LibraryMethod method) {
		// TODO check overriding
		// TODO : is static method?
		return method_visit(method, true);
	}

	public Object visit(Formal formal) {
		//the formals are legal, this function never called
		return true;
	}

	@Override
	public Object visit(PrimitiveType type) {
		//this visitor is never called
		return true;
	}

	@Override
	public Object visit(UserType type) {
		//this visitor is never called
		return true;
	}

	public Object visit(Assignment assignment) {
		Type locationType  = (Type) assignment.getVariable().accept(this);
		Type assignmentType = (Type) assignment.getAssignment().accept(this);
		if (!assignmentType.subtypeof(locationType)){
			throw new SemanticError("Type mismatch - can't assign "+ assignmentType+ 
									 " into " + locationType, assignment.getLine());
		}				
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
		
		if (!returnValueType.subtypeof(returnStatement.getenclosingScope().getEntry("$ret").getType())) {
			throw new SemanticError("Return value type is not a subtype of the method's return value", returnStatement.getLine());
		}
		return returnValueType;
	}

	public Object visit(If ifStatement) {
		//check if condition of type boolean
		Type conditionType = (Type) ifStatement.getCondition().accept(this);
		
		if (!conditionType.subtypeof(TypeTable.boolType)) {
			throw new SemanticError("Condition in if statement is not of type boolean",ifStatement.getCondition().getLine());
		}
		//visit the operation in ifStatement
		ifStatement.getOperation().accept(this);
		
		//visit the else operation if exists
		if (ifStatement.hasElse()){
			ifStatement.getElseOperation().accept(this);
		}
		return true;
	}

	public Object visit(While whileStatement) {
		//check while condition of type boolean
		Type conditionType = (Type) whileStatement.getCondition().accept(this);
		
		if (!conditionType.subtypeof(TypeTable.boolType)) {
			throw new SemanticError("Condition in while statement is not of type boolean",whileStatement.getCondition().getLine());
		}
		inLoop++;
		
		//check the operation in whileStatement
		whileStatement.getOperation().accept(this);
		
		inLoop--;
		return true;
	}

	public Object visit(Break breakStatement) {
		if (inLoop==0){
			throw new SemanticError("Break outside of loop", breakStatement.getLine());
		}		
		return true;
	}

	public Object visit(Continue continueStatement) {
		
		if (inLoop==0){
			throw new SemanticError("Continue outside of loop", continueStatement.getLine());
		}		
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
			Type initValueType = (Type) localVariable.getInitValue().accept(this);

			// check initial value type is sub type of local variable
			Type localVariableType = localVariable.getenclosingScope()
					.getEntry(localVariable.getName()).getType();
			if (!initValueType.subtypeof(localVariableType)) {
				throw new SemanticError("Type mismatch - can't assign " + initValueType +
						" to " + localVariableType , localVariable.getLine());
			}
		}
		return true;
	}

	@Override
	public Object visit(VariableLocation location) {	
		
		if (!location.isExternal()){
			//if location is not external - we return the value in the symbol table
			Symbol symb = location.getenclosingScope().getEntryRecursive(location.getName());
			if (symb == null) {
				//does not exists in method or as class formal
				throw new SemanticError(location.getName() + " has not been declared" , location.getLine());
			}
			return symb.getType();
		}else{
			//location is external 
			if (location.getLocation().getenclosingScope().existEntryRecursive(location.getName())){
				return location.getLocation().getenclosingScope().getEntryRecursive(location.getName()).getType();
			}else{
				throw new SemanticError(location.getName() + " does not exist in scope" , location.getLine());
			}
		}
	}

	public Object visit(ArrayLocation location) {
		//check index is int type
		Type indexType = (Type) location.getIndex().accept(this);
		if (!indexType.subtypeof(TypeTable.intType)){
			throw new SemanticError(location.getIndex() + " is not a valid array index - not an int type" , location.getLine());
		}
		
		//check array is an array type
		Type arrType = (Type) location.getArray().accept(this);
		if (!(arrType instanceof ArrayType)) {
			throw new SemanticError(location.getArray() + " not an array type", location.getLine());
		}
		
		//return the type of an array element
		return ((ArrayType)arrType).getElementType();
	}

	private Object checkCallArguments(Call call, Symbol methodSym) {
		MethodType methodType = (MethodType)methodSym.getType();
		if (methodType.getArguments().size() != call.getArguments().size()) {
			throw new SemanticError("Invalid number of arguments for method " + call.getName(), call.getLine());
		}
		
		for (int i = 0; i < methodType.getArguments().size(); i++) {
			Type curArgType = (Type)call.getArguments().get(i).accept(this); 
			if (!curArgType.subtypeof(methodType.getArguments().get(i))) {
				throw new SemanticError("Argument number " + i + " is of invalid type, " +
						"for method " + call.getName(), call.getLine());
			}
		}
		
		return methodType.getReturnVal();
	}
	
	public Object visit(StaticCall call) {
		Symbol classSym = call.getenclosingScope().getEntryRecursive(call.getClassName());
		
		if (classSym == null) {
			throw new SemanticError("Class " + call.getClassName() + " doesn't exist", call.getLine());
		} else if (classSym.getKind() != IC.SymbolTables.Kind.CLASS) {
			throw new SemanticError("Identifier " + call.getClassName() + " is not a class type", call.getLine());
		} else {
			ICClass classRef = ((ClassType)classSym.getType()).getICClass();
			Symbol methodSym = classRef.getenclosingScope().getEntry(call.getName());
			if (methodSym == null) {
				throw new SemanticError("Identifier " + call.getName() + " doesn't exist in class " + call.getClassName(), call.getLine());
			} else if (methodSym.getKind() != Kind.STATIC_METHOD) {
				throw new SemanticError("There is not static method " + call.getName() + " in class " + call.getClassName(), call.getLine());
			} else {
				return checkCallArguments(call, methodSym);
			}
		}
	}

	public Object visit(VirtualCall call) {
		Symbol methodSym = null;
		if (!call.isExternal()) {
			// TODO : check this!
			methodSym = call.getenclosingScope().getEntryRecursive(call.getName());
		} else {
			Type locType = (Type) call.getLocation().accept(this);

			if (!(locType instanceof ClassType)) {
				throw new SemanticError("Invalid call", call.getLine());
			}

			methodSym = ((ClassType) locType).getICClass().getenclosingScope()
					.getEntry(call.getName());
		}
		
		if (methodSym == null) {
			throw new SemanticError("Method " + call.getName() + " doesn't exist", call.getLine());
		} else if (methodSym.getKind() != Kind.VIRTUAL_METHOD) {
			throw new SemanticError("identifier " + call.getName() + " isn't a virtual method", call.getLine());
		} else {
			return checkCallArguments(call, methodSym);
		}
		
	}

	public Object visit(This thisExpression) {
		if (inStaticMethod) {
			throw new SemanticError("this expression cannot appear within a static method", thisExpression.getLine());
		}
		return thisExpression.getenclosingScope().getEntry("this").getType();
	}

	public Object visit(NewClass newClass) {
		Type newClasstype = TypeTable.getClassType(newClass);
		if (newClasstype == TypeTable.nullType) 
			throw new SemanticError("Not a valid class type name - " + newClass.getName(), newClass.getLine());
		return newClasstype;
	}


	public Object visit(NewArray newArray) {
		//check the size is integer type
		Type sizeType = (Type) newArray.getSize().accept(this);
		if (!sizeType.subtypeof(TypeTable.intType)){
			throw new SemanticError("Array length is not an int value - " + newArray.getSize() ,newArray.getLine());
		}
		
		//return the array type
		return newArray.getEnclosingType();
	}

	public Object visit(Length length) {
		
		//check array is of array type
		Type arrType = (Type) length.getArray().accept(this);
		if (!(arrType instanceof ArrayType)){
			throw new SemanticError(length.getArray() + " not an array type" + length.getLine());
		}
		
		//return int
		return TypeTable.intType;
	}


	public Object visit(MathBinaryOp binaryOp) {
		Type binaryOpType1 = (Type) binaryOp.getFirstOperand().accept(this);
		Type binaryOpType2 = (Type) binaryOp.getSecondOperand().accept(this);
		if (binaryOpType1 == null || binaryOpType2 == null) return null;
		if (!binaryOpType1.subtypeof(TypeTable.intType) || !binaryOpType2.subtypeof(TypeTable.intType))
			throw new SemanticError("Mathematical binary operation on a non-int values", binaryOp.getLine());
		return TypeTable.intType;
	}

	public Object visit(LogicalBinaryOp binaryOp) {
		Type binaryOpType1 = (Type) binaryOp.getFirstOperand().accept(this);
		Type binaryOpType2 = (Type) binaryOp.getSecondOperand().accept(this);
		
		if (!binaryOpType1.subtypeof(binaryOpType2) && !binaryOpType2.subtypeof(binaryOpType1)){
		//if non of the operands is a sub type of the other	
		//check if logical operation
		if (binaryOp.getOperator() == IC.BinaryOps.LOR || binaryOp.getOperator() == IC.BinaryOps.LAND)
				throw new SemanticError("Logical unary operation on a non-boolean type", binaryOp.getLine());
		//check if the operand is one of the int operations <,<=,>,>=
		if (binaryOp.getOperator() == IC.BinaryOps.GT || binaryOp.getOperator() == IC.BinaryOps.GTE 
				|| binaryOp.getOperator() == IC.BinaryOps.LT || binaryOp.getOperator() == IC.BinaryOps.LTE)
			throw new SemanticError("Comparing non-int type", binaryOp.getLine());
		//operand is one of the comparison operands ==,!=
		else 
				throw new SemanticError("Comparing two different types - at least one has to be the sub type of the other", binaryOp.getLine());
		}
        
		
		if ((binaryOp.getOperator() == IC.BinaryOps.LOR) ||(binaryOp.getOperator() == IC.BinaryOps.LAND))
        {
			// operator is one of "||","&&"                            
			if (!binaryOpType1.subtypeof(TypeTable.boolType))                                          
				throw new SemanticError("Logical operation on non-boolean values", binaryOp.getLine());
		}
		else if (binaryOp.getOperator() == IC.BinaryOps.GT || binaryOp.getOperator() == IC.BinaryOps.GTE 
			|| binaryOp.getOperator() == IC.BinaryOps.LT || binaryOp.getOperator() == IC.BinaryOps.LTE)
		{
			// operator is one of "<=",">=", "<", ">"                                                         
			if (!binaryOpType1.subtypeof(TypeTable.intType)){                                          
					throw new SemanticError("Comparing non int values", binaryOp.getLine());                                		
			}
		}
		return TypeTable.boolType;
	}

	public Object visit(MathUnaryOp unaryOp) {
        Type unaryOpType = (Type) unaryOp.getOperand().accept(this);
        if (unaryOpType == null) return null;
        if (!unaryOpType.subtypeof(TypeTable.intType)){                                
        	throw new SemanticError("Mathematical unary operation on a non-integer type",unaryOp.getLine());
        }
        return unaryOpType;
	}

	public Object visit(LogicalUnaryOp unaryOp) {
        Type unaryOpType = (Type) unaryOp.getOperand().accept(this);
        if (!unaryOpType.subtypeof(TypeTable.boolType)){                                
        	throw new SemanticError("Logical unary operation on a non-boolean type",unaryOp.getLine());
        }
        return unaryOpType;
}



	public Object visit(Literal literal) {
		return literal.getEnclosingType();
	}


	public Object visit(ExpressionBlock expressionBlock) {
		return expressionBlock.getExpression().accept(this);
	}

}
