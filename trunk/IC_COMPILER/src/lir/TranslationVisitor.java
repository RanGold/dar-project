package lir;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
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
import IC.Types.TypeTable;

public class TranslationVisitor implements Visitor {
	private Map<String, String> stringLiterals;
	private Map<String, String[]> dispatchTables;
	private Map<String, String> fieldOffsets;
	private StringBuilder lirOutput;
	private StringBuilder instructions;
	private int strNum;
	private StringBuilder main;
	
	public TranslationVisitor() {
		this.stringLiterals = new LinkedHashMap<String, String>();
		this.dispatchTables = new LinkedHashMap<String, String[]>();
		this.fieldOffsets = new HashMap<String, String>();
		this.lirOutput = new StringBuilder();
		this.instructions = new StringBuilder();
		this.main = new StringBuilder();
		this.strNum = 1;
	}
	
	/**
	 * the method receives a string literal, and returns its label
	 * @param key - the string literal (i.e. "daniel")
	 * @return the label if exist, or new label if it doesn't already exist
	 */
	private String getStringLiteralName(String key) {//TODO verify "" before string literals
		String value = stringLiterals.get(key);
		//entry does not exist
		if (value == null){
			value = "str" + strNum;
			stringLiterals.put(key, value);
			strNum++;
		}
		return value;
	}

	private String getClassLabel(String className){
		return "_DV_" + className;
	}
	
	/**
	 * adds label for method to dispatch table
	 * @param className - the name of a class
	 * @param methodName - the name of a method
	 * @return the label of the method of the class, if it doesn't exist it creates a new label
	 */
	private String getMethodLabel(ICClass icClass, String methodName){
		String className = icClass.getenclosingScope().getVariableScope(methodName).getID();
		String methodLabel = "_" +  className + "_" + methodName;
		return methodLabel;
	}
	
	//checks it its a main function
	private boolean checkMain(Method method){
		
		if (!method.getName().equals("main")){
			return false;
		}
		
		//check that the method is static
		if(!(method instanceof StaticMethod)){
			return false;
		}
		//check correct number of parameters
		if (method.getFormals().size()!=1){
			return false;
		}
		//check parameter's type
		if (!method.getFormals().get(0).getType().getEnclosingType().subtypeof(TypeTable.arrayType(TypeTable.stringType)) || method.getFormals().get(0).getType().getDimension()!=1){
			return false;
		}
		//check return parameter
		if (!method.getType().getEnclosingType().subtypeof(TypeTable.voidType)){
			return false;
		}
		return true;
	}
	
	@Override
	public Object visit(Program program) {
		program.setClassesOffsets();
		
		for (ICClass icClass : program.getClasses())
		{
			instructions.append(((NodeLirTrans)icClass.accept(this)).codeTrans);
			instructions.append("\r\n");
		}
		
		instructions.append(main.toString());
		instructions.append("\r\n");
		
		lirOutput.append("# Lir code\r\n\r\n");
		
		//appending the string literals defined during the run of the visitor
		lirOutput.append("# String Literals\r\n");
		for (Entry<String, String> stringLiteral : stringLiterals.entrySet()){
			lirOutput.append(stringLiteral.getValue() + ": " + stringLiteral.getKey() + "\r\n");
		}
		lirOutput.append("\r\n");
		
		//appending the dispatch tables defined during the run of the visitor
		lirOutput.append("# Dispatch Tables\r\n");
		for (Entry<String, String[]> classLabel : dispatchTables.entrySet()){
			lirOutput.append(classLabel.getKey() + ": [");
			boolean first = true;
			for (String methodLabel : classLabel.getValue()){
				if (first){
					lirOutput.append(methodLabel);
					first = false;
				}
				lirOutput.append("," + methodLabel);
			}
			lirOutput.append("]\r\n");
			
			//add comment for field offsets
			lirOutput.append(fieldOffsets.get(classLabel.getKey()));
		}
		lirOutput.append("\r\n");
		
		//appending the blocks of methods
		lirOutput.append("# Method Blocks\r\n");
		lirOutput.append(instructions.toString());

		return lirOutput.toString();
	}

	@Override
	public Object visit(ICClass icClass) {
		StringBuilder classInstructions = new StringBuilder();
	
		if (!icClass.getName().equals("Library")){//TODO should we avoid library?
			String[] methodLables = new String[icClass.getMethodsOffsets().size()];
			for (Entry<String, Integer> method : icClass.getMethodsOffsets().entrySet()){
				//building the dispatch table
				methodLables[method.getValue()] =  getMethodLabel(icClass, method.getKey());
			}
			
			dispatchTables.put(getClassLabel(icClass.getName()), methodLables);
			
			//adding comments for field offsets
			StringBuilder fieldOffsetsComment = new StringBuilder();
			fieldOffsetsComment.append("# field offsets:\r\n");
			for (Entry<String,Integer> field : icClass.getFieldsOffsets().entrySet()){
				fieldOffsetsComment.append("# " + field.getKey() + ": " + field.getValue() + "\r\n");
			}
			fieldOffsets.put(getClassLabel(icClass.getName()), fieldOffsetsComment.toString());
			
			for (Method method : icClass.getMethods()){
				NodeLirTrans methodTrs = (NodeLirTrans) method.accept(this);
				if (checkMain(method)){
					main.append("# main in " + icClass.getName() + "\r\n");
					main.append("_ic_main:\r\n");
					main.append(methodTrs.codeTrans);
				} else {
					classInstructions.append(getMethodLabel(icClass, method.getName()) + ":\r\n");
					classInstructions.append(methodTrs.codeTrans + "\r\n");
				}
			}

			return new NodeLirTrans(classInstructions.toString(), "");
		}
		
		return new NodeLirTrans("", "");
	}

	@Override
	public Object visit(Field field) {
		return null;
	}

	
	private NodeLirTrans methodVisit(Method method){
		StringBuilder sb = new StringBuilder();
		for (Statement s : method.getStatements()){
			sb.append(((NodeLirTrans)s.accept(this)).codeTrans);
		}
		sb.append("Return 0 \r\n");
		return new NodeLirTrans(sb.toString(), "");
	}
	
	@Override
	public Object visit(VirtualMethod method) {
		return methodVisit(method);
	}

	@Override
	public Object visit(StaticMethod method) {
		return methodVisit(method);
	}

	@Override
	public Object visit(LibraryMethod method) {
		throw new RuntimeException("Should not get here");
	}

	@Override
	public Object visit(Formal formal) {
		throw new RuntimeException("Should not get here");
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
		StringBuilder s = new StringBuilder();
		int objectSize = 0;
		String resultRegister = RegisterPool.getRegister();
		s.append("Library __allocateObject(" + objectSize + "),");
		s.append(resultRegister + "\r\n");
		s.append("MoveField " + getClassLabel(newClass.getName()) + ","); //TODO: add function that does that
		s.append(resultRegister + ".0");
		return new NodeLirTrans(s.toString(),resultRegister);
	}

	@Override
	public Object visit(NewArray newArray) {
		//NodeLirTrans expTrs = (NodeLirTrans) newArray.getType().accept(this); //TODO not needed?
		NodeLirTrans expTrs = (NodeLirTrans) newArray.getSize().accept(this);
		StringBuilder s = new StringBuilder();
		s.append("Library __allocateArray(" + expTrs.resultRegister + "),");
		s.append(expTrs.resultRegister + "\r\n");
		return new NodeLirTrans(s.toString(),expTrs.resultRegister);
	}

	@Override
	public Object visit(Length length) {
		NodeLirTrans expTrs = (NodeLirTrans) length.getArray().accept(this);
		StringBuilder s = new StringBuilder();
		s.append("ArrayLength ");
		s.append(expTrs.resultRegister + ",");
		s.append(expTrs.resultRegister + "\r\n");
		return new NodeLirTrans(s.toString(), expTrs.resultRegister);//TODO check that every basic literal/var returns register, otherwise change to new register
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		NodeLirTrans expTrs1 = (NodeLirTrans) binaryOp.getFirstOperand().accept(this);
		NodeLirTrans expTrs2 = (NodeLirTrans) binaryOp.getSecondOperand().accept(this);
		StringBuilder s = new StringBuilder();
		s.append(expTrs1.codeTrans); // TODO: added this code
		s.append(expTrs2.codeTrans); // TODO: added this code
		s.append("# Mathematical binary operation\r\n");
		switch(binaryOp.getOperator()){
		case PLUS:
			s.append("Add ");
			s.append(expTrs1.resultRegister+",");
			s.append(expTrs2.resultRegister+"\r\n");
			return new NodeLirTrans(s.toString(), expTrs2.resultRegister);
		case MINUS:
			s.append("Sub ");
			s.append(expTrs1.resultRegister+",");
			s.append(expTrs2.resultRegister+"\r\n");
			return new NodeLirTrans(s.toString(), expTrs2.resultRegister);
		case MULTIPLY:
			s.append("Mul ");
			s.append(expTrs1.resultRegister+",");
			s.append(expTrs2.resultRegister+"\r\n");
			return new NodeLirTrans(s.toString(), expTrs2.resultRegister);
		case DIVIDE:
			s.append("Div ");
			s.append(expTrs1.resultRegister+",");
			s.append(expTrs2.resultRegister+"\r\n");
			return new NodeLirTrans(s.toString(), expTrs2.resultRegister);
		case MOD:
			s.append("Mod ");
			s.append(expTrs1.resultRegister+",");
			s.append(expTrs2.resultRegister+"\r\n");
			return new NodeLirTrans(s.toString(), expTrs2.resultRegister);
		default: 
			return null;
		}
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		NodeLirTrans expTrs1 = (NodeLirTrans) binaryOp.getFirstOperand().accept(this);
		NodeLirTrans expTrs2 = (NodeLirTrans) binaryOp.getSecondOperand().accept(this);
		StringBuilder s = new StringBuilder();
		s.append(expTrs1.codeTrans); // TODO: added this code
		s.append(expTrs2.codeTrans); // TODO: added this code
		s.append("# Logical binary operation\r\n");
		switch(binaryOp.getOperator()){
		case LAND:
			s.append("And ");
			s.append(expTrs1.resultRegister+",");
			s.append(expTrs2.resultRegister+"\r\n");
			return new NodeLirTrans(s.toString(), expTrs2.resultRegister);
		case LOR:
			s.append("Or ");
			s.append(expTrs1.resultRegister+",");
			s.append(expTrs2.resultRegister+"\r\n");
			return new NodeLirTrans(s.toString(), expTrs2.resultRegister);
		case LT:
			s.append("Compare ");
			s.append(expTrs1.resultRegister+",");
			s.append(expTrs2.resultRegister+"\r\n");
			s.append("JumpL ");
			break;
		case LTE:
			s.append("Compare ");
			s.append(expTrs1.resultRegister+",");
			s.append(expTrs2.resultRegister+"\r\n");
			s.append("JumpLE ");
			break;
		case GT:
			s.append("Compare ");
			s.append(expTrs1.resultRegister+",");
			s.append(expTrs2.resultRegister+"\r\n");
			s.append("JumpG ");
			break;
		case GTE:
			s.append("Compare ");
			s.append(expTrs1.resultRegister+",");
			s.append(expTrs2.resultRegister+"\r\n");
			s.append("JumpGE ");
			break;
		case EQUAL:
			s.append("Compare ");
			s.append(expTrs1.resultRegister+",");
			s.append(expTrs2.resultRegister+"\r\n");
			s.append("JumpTrue ");
			break;
		case NEQUAL:
			s.append("Compare ");
			s.append(expTrs1.resultRegister+",");
			s.append(expTrs2.resultRegister+"\r\n");
			s.append("JumpFalse ");
		}
		s.append("_True_" + strNum + "\r\n");
		s.append("Move 0," + expTrs2.resultRegister + "\r\n");
		s.append("Jump " + "_End_Boolean_" + strNum);
		//s.append("_True_" + strNum + ":\r\n"); //if false //TODO: why twice?
		s.append("_True_" + strNum + ":\r\n"); //if true
		s.append("Move 1," + expTrs2.resultRegister + "\r\n");	
		s.append("_End_Boolean_" + strNum + ":\r\n");
		strNum++;
		return new NodeLirTrans(s.toString(), expTrs2.resultRegister);
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		NodeLirTrans expTrs = (NodeLirTrans) unaryOp.getOperand().accept(this);
		StringBuilder s = new StringBuilder();
		s.append("# Mathematical unary operation\r\n");
		s.append(expTrs.codeTrans);
		s.append("Mul -1,");
		s.append(expTrs.resultRegister + "\r\n");
		return new NodeLirTrans(s.toString(), expTrs.resultRegister);
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		NodeLirTrans expTrs = (NodeLirTrans) unaryOp.getOperand().accept(this);
		StringBuilder s = new StringBuilder();
		s.append("# Logical unary operation\r\n");
		s.append(expTrs.codeTrans);
		s.append("Not ");
		s.append(expTrs.resultRegister + "\r\n");
		return new NodeLirTrans(s.toString(), expTrs.resultRegister);
	}

	@Override
	public Object visit(Literal literal) {
		StringBuilder s = new StringBuilder();
		s.append("Move ");
		s.append(literal.getValue().toString() + ",");
		String resultRegister = RegisterPool.getRegister();
		s.append(resultRegister + "\r\n");
		return new NodeLirTrans(s.toString(),resultRegister);
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		return expressionBlock.getExpression().accept(this);
	}

}
