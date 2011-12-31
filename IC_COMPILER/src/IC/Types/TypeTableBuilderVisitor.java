package IC.Types;

import IC.DataTypes;
import IC.AST.*;
import IC.SemanticChecks.SemanticError;

public class TypeTableBuilderVisitor implements Visitor{
	
	@Override
	public Object visit(Program program) {
		for (ICClass icClass : program.getClasses()){
			icClass.accept(this);
		}
		return null;
	}

	@Override
	public Object visit(ICClass icClass) throws SemanticError {
		 TypeTable.classType(icClass.getName(), icClass.getSuperClassName(), icClass.getLine());
		 for (Field field : icClass.getFields()){
			 field.accept(this);
		 }
		 for (Method method : icClass.getMethods()){
			 method.accept(this);
		 }
		 return null;
	}

	@Override
	public Object visit(Field field) throws SemanticError {
		if (field.getType().getDimension()==0){
			String typeName = field.getType().getName();
			if (!typeName.equals(DataTypes.INT) && !typeName.equals(DataTypes.BOOLEAN) && 
					(!typeName.equals(DataTypes.STRING))){
				TypeTable.classType(typeName, null, field.getLine());
			}
		}
		return null;
	}

	@Override
	public Object visit(VirtualMethod method) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StaticMethod method) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LibraryMethod method) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Formal formal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(IC.AST.PrimitiveType type) {
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		// TODO Auto-generated method stub
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
