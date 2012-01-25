package lir;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
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
import IC.Types.ClassType;
import IC.Types.TypeTable;

public class TranslationVisitor implements Visitor {
	private Map<String, String> stringLiterals;
	private Map<String, String[]> dispatchTables;
	private Map<String, String> fieldOffsets;
	private StringBuilder lirOutput;
	private StringBuilder instructions;
	private int strNum;
	private StringBuilder main;
	private Map<String, ICClass> nameToClass; 
	private Stack<Integer> whileLables = new Stack<Integer>();
	
	private int getNextId(){
		strNum++;
		return strNum;
	}
	
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
			value = "str" + getNextId();
			stringLiterals.put(key, value);
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
	
	private NodeLirTrans loadGeneric(Expression expression){
		NodeLirTrans expTrs = (NodeLirTrans) expression.accept(this);
		String resultRegister = RegisterPool.getRegister();
		StringBuilder s = new StringBuilder();
		s.append(expTrs.codeTrans);
		if (expTrs.resultRegister.contains("[")) {
			s.append("MoveArray ");
		}
		else if (expTrs.resultRegister.contains(".")) {
			s.append("MoveField ");
		}
		else {
			s.append("Move ");
		}
		s.append(expTrs.resultRegister + ",");
		s.append(resultRegister + "\r\n");
		return new NodeLirTrans(s.toString(),resultRegister);
	}
	
	@Override
	public Object visit(Program program) {
		nameToClass = program.getNameToClass();
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
		throw new RuntimeException("Should not get here");
	}

	@Override
	public Object visit(UserType type) {
		throw new RuntimeException("Should not get here");
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
		StringBuilder s = new StringBuilder();
		if (returnStatement.hasValue()){
			NodeLirTrans valueTrs = loadGeneric(returnStatement.getValue());
			s.append(valueTrs.codeTrans);
			s.append("Return " + valueTrs.resultRegister);
		}else{
			s.append("Return");
		}
		return new NodeLirTrans(s.toString(), "");
	}

	@Override
	public Object visit(If ifStatement) {
		StringBuilder s = new StringBuilder();
		int id = getNextId();
		NodeLirTrans condTrs = loadGeneric(ifStatement.getCondition());
		s.append(condTrs.codeTrans + "\r\n");
		s.append("Compare 0," + condTrs.resultRegister + "\r\n");
		s.append("JumpTrue _false_" + id + "\r\n");
		NodeLirTrans operTrans = (NodeLirTrans) ifStatement.getOperation().accept(this);
		s.append(operTrans.codeTrans);
		s.append("Jump _end_" + id + "\r\n");
		s.append("_false_" + id + ":\r\n");
		if (ifStatement.hasElse()){
			NodeLirTrans elseTrans = (NodeLirTrans) ifStatement.getElseOperation().accept(this);
			s.append(elseTrans.codeTrans);
		}
		s.append("end_" + id + "\r\n");
		return new NodeLirTrans(s.toString(), "");
	}

	@Override
	public Object visit(While whileStatement) {
		StringBuilder s = new StringBuilder();
		int id = getNextId();
		whileLables.push(id);
		s.append("_while_" + id + ":\r\n");
		NodeLirTrans condTrs = loadGeneric(whileStatement.getCondition());
		s.append(condTrs.codeTrans + "\r\n");
		s.append("Compare 0," + condTrs.resultRegister + "\r\n");
		s.append("JumpTrue _end_while_" + id + "\r\n");
		NodeLirTrans operTrs = (NodeLirTrans)whileStatement.getOperation().accept(this);
		s.append("Jump _while_" + id + "\r\n");
		s.append("_end_while_" + id + "\r\n");
		whileLables.pop();
		
		return new NodeLirTrans(s.toString(), ""); 
	}

	@Override
	public Object visit(Break breakStatement) {
		return new NodeLirTrans("Jump _end_while_" + whileLables.peek(), ""); 
	}

	@Override
	public Object visit(Continue continueStatement) {
		return new NodeLirTrans("Jump _while_" + whileLables.peek(), ""); 
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		StringBuilder sb = new StringBuilder();
		for(Statement s : statementsBlock.getStatements()){
			NodeLirTrans sTrans = (NodeLirTrans) s.accept(this);
			sb.append(sTrans.codeTrans + "\r\n");
		}
		return new NodeLirTrans(sb.toString(),"");
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VariableLocation location) {
		StringBuilder s = new StringBuilder();
		if (location.isExternal()){
			NodeLirTrans expTrs1 = loadGeneric(location.getLocation());
			s.append(expTrs1.codeTrans);
			
			ICClass classInstance = ((ClassType)location.getLocation().getEnclosingType()).getICClass();
			int varLocationNum = classInstance.getFieldOffset(location.getName()); 
			String arraySymbol = expTrs1.resultRegister + "." + varLocationNum + "\r\n";
			return new NodeLirTrans(s.toString(),arraySymbol);
		}
		else{
			if(location.getenclosingScope().getEntryRecursive(location.getName()).getKind().equals(Kind.FIELD)){
				String classLocationReg = RegisterPool.getRegister();
				s.append("Move this," + classLocationReg);
				
				ICClass classInstance = ((ClassType)location.getLocation().getEnclosingType()).getICClass();
				int varLocationNum = classInstance.getFieldOffset(location.getName()); 
				String arraySymbol = classLocationReg + "." + varLocationNum + "\r\n";
				return new NodeLirTrans(s.toString(),arraySymbol);
			}
			else{
				String classLocationReg = RegisterPool.getRegister();
				s.append("Move " + location.getName() + "," + classLocationReg);
				return new NodeLirTrans(s.toString(),classLocationReg);
			}
		}//TODO distinct id
		
		
	}

	@Override
	public Object visit(ArrayLocation location) {
		NodeLirTrans expTrs1 = loadGeneric(location.getArray());
		NodeLirTrans expTrs2 = loadGeneric(location.getIndex());
		StringBuilder s = new StringBuilder();
		s.append(expTrs1.codeTrans);
		s.append(expTrs2.codeTrans);
		String arraySymbol = expTrs1.resultRegister + "[" + expTrs2.resultRegister + "]\r\n";
		return new NodeLirTrans(s.toString(),arraySymbol);
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
		int objectSize = 0;//TODO change this to actual class size
		String resultRegister = RegisterPool.getRegister();
		s.append("Library __allocateObject(" + objectSize + "),");
		s.append(resultRegister + "\r\n");
		s.append("MoveField " + getClassLabel(newClass.getName()) + ",");
		s.append(resultRegister + ".0\r\n");
		return new NodeLirTrans(s.toString(),resultRegister);
	}

	@Override
	public Object visit(NewArray newArray) {
		//NodeLirTrans expTrs = (NodeLirTrans) newArray.getType().accept(this); //TODO not needed?
		NodeLirTrans expTrs = (NodeLirTrans) newArray.getSize().accept(this);
		StringBuilder s = new StringBuilder();
		s.append(expTrs.codeTrans);
		s.append("Library __allocateArray(" + expTrs.resultRegister + "),");
		s.append(expTrs.resultRegister + "\r\n");
		return new NodeLirTrans(s.toString(),expTrs.resultRegister);
	}

	@Override
	public Object visit(Length length) {
		NodeLirTrans expTrs = loadGeneric(length.getArray());
		StringBuilder s = new StringBuilder();
		s.append(expTrs.codeTrans);
		s.append("ArrayLength ");
		s.append(expTrs.resultRegister + ",");
		s.append(expTrs.resultRegister + "\r\n");
		return new NodeLirTrans(s.toString(), expTrs.resultRegister);
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		NodeLirTrans expTrs1 = loadGeneric(binaryOp.getFirstOperand());
		NodeLirTrans expTrs2 = loadGeneric(binaryOp.getSecondOperand());
		StringBuilder s = new StringBuilder();
		s.append(expTrs1.codeTrans);
		s.append(expTrs2.codeTrans);
		s.append("# Mathematical binary operation\r\n");
		switch(binaryOp.getOperator()){
		case PLUS:
			if (binaryOp.getFirstOperand().getenclosingScope().equals(TypeTable.stringType)){
				s.append("Library __stringCat(");
				s.append(expTrs1.resultRegister+",");
				s.append(expTrs2.resultRegister+"),");
				s.append(expTrs2.resultRegister);
				return new NodeLirTrans(s.toString(), expTrs2.resultRegister);
			}
			else{
				s.append("Add ");
				s.append(expTrs1.resultRegister+",");
				s.append(expTrs2.resultRegister+"\r\n");
				return new NodeLirTrans(s.toString(), expTrs2.resultRegister);
			}
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
		NodeLirTrans expTrs1 = loadGeneric(binaryOp.getFirstOperand());
		NodeLirTrans expTrs2 = loadGeneric(binaryOp.getSecondOperand());
		StringBuilder s = new StringBuilder();
		s.append(expTrs1.codeTrans);
		s.append(expTrs2.codeTrans);
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
		int id = getNextId();
		s.append("_True_" + id + "\r\n");
		s.append("Move 0," + expTrs2.resultRegister + "\r\n");
		s.append("Jump " + "_End_Boolean_" + id);
		s.append("_True_" + id + ":\r\n"); //if true
		s.append("Move 1," + expTrs2.resultRegister + "\r\n");	
		s.append("_End_Boolean_" + id + ":\r\n");
		return new NodeLirTrans(s.toString(), expTrs2.resultRegister);
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		NodeLirTrans expTrs = loadGeneric(unaryOp.getOperand());
		StringBuilder s = new StringBuilder();
		s.append("# Mathematical unary operation\r\n");
		s.append(expTrs.codeTrans);
		s.append("Mul -1,");
		s.append(expTrs.resultRegister + "\r\n");
		return new NodeLirTrans(s.toString(), expTrs.resultRegister);
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		NodeLirTrans expTrs = loadGeneric(unaryOp.getOperand());
		StringBuilder s = new StringBuilder();
		s.append("# Logical unary operation\r\n");
		s.append(expTrs.codeTrans);
		s.append("Not ");//TODO neg?
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
