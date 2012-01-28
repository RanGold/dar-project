package lir;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

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
	private Stack<Integer> whileLables;
	
	// TODO : Whenever calling an expression use loadGeneric
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
		this.whileLables = new Stack<Integer>();
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
		String className = icClass.getEnclosingScope().getVariableScope(methodName).getID();
		String methodLabel = "_" +  className + "_" + methodName;
		return methodLabel;
	}
	
	// checks it its a main function
	private boolean checkMain(Method method){
		
		if (!method.getName().equals("main")) {
			return false;
		}
		// check that the method is static
		if (!(method instanceof StaticMethod)) {
			return false;
		}
		// check correct number of parameters
		if (method.getFormals().size() != 1) {
			return false;
		}
		// check parameter's type
		if (!method.getFormals().get(0).getType().getEnclosingType()
				.subtypeof(TypeTable.arrayType(TypeTable.stringType))
				|| method.getFormals().get(0).getType().getDimension() != 1) {
			return false;
		}
		// check return parameter
		if (!method.getType().getEnclosingType().subtypeof(TypeTable.voidType)) {
			return false;
		}
		return true;
	}
	
	private NodeLirTrans loadGeneric(Expression expression, boolean returnMem) {
		NodeLirTrans expTrs = (NodeLirTrans) expression.accept(this);
		String resultRegister = null;
		StringBuilder sb = new StringBuilder();
		sb.append(expTrs.codeTrans);
		
		switch (expTrs.type) {
		case Register:
			return expTrs;
		case Mem:
			if (returnMem) {
				return expTrs;
			} else {
				sb.append("Move ");
				sb.append(expTrs.result);
				sb.append(",");
				String resReg = RegisterPool.getRegister();
				sb.append(resReg + "\r\n");
				return new NodeLirTrans(sb.toString(), resReg);
			}
		case ArrayLocation:
			sb.append("MoveArray ");
			resultRegister = RegisterPool.getRegister();
			break;
		case FieldLocation:
			sb.append("MoveField ");
			resultRegister = RegisterPool.getRegister();
			break;
		}
		
		sb.append(expTrs.result + ",");
		sb.append(resultRegister + "\r\n");
		return new NodeLirTrans(sb.toString(),resultRegister);
	}
	
	private NodeLirTrans loadGeneric(Expression expression) {
		return this.loadGeneric(expression, false);
	}
	
	private String runtimeChecks(){
		StringBuilder s = new StringBuilder();
		String register = RegisterPool.getRegister();
		String register2 = RegisterPool.getRegister();
		
		//null pointer dereference
		s.append("__checkNullRef:\r\n");
		s.append("Move a,"+register+"\r\n");
		s.append("Compare 0,"+register+"\r\n");
		s.append("JumpFalse _notNull\r\n");
		s.append("Library __println(strNullRef),Rdummy\r\n");
		s.append("Jump _errorExit\r\n");
		s.append("_notNull:\r\n");
		s.append("Return 0\r\n\r\n");
		
		//division by zero
		s.append("__checkZero:\r\n");
		s.append("Move b,"+register+"\r\n");
		s.append("Compare 0,"+register+"\r\n");
		s.append("JumpFalse _notZero\r\n");
		s.append("Library __println(strZero),Rdummy\r\n");
		s.append("Jump _errorExit\r\n");
		s.append("_notZero:\r\n");
		s.append("Return 0\r\n\r\n");
		
		//legal array size
		s.append("__checkSize:\r\n");
		s.append("Move n,"+register+"\r\n");
		s.append("Compare 0,"+register+"\r\n");
		s.append("JumpGE _okSize\r\n");//TODO LE or GE?
		s.append("Library __println(strSize),Rdummy\r\n");
		s.append("Jump _errorExit\r\n");
		s.append("_okSize:\r\n");
		s.append("Return 0\r\n\r\n");
		
		//legal array size
		s.append("__checkArrayAccess:\r\n");
		s.append("ArrayLength a,"+register+"\r\n");
		s.append("Move i,"+register2+"\r\n");
		s.append("Compare "+register+","+register2+"\r\n");
		s.append("JumpGE _indexOutOfBound\r\n");//TODO LE or GE?
		s.append("Compare 0,"+register2+"\r\n");
		s.append("JumpL _indexOutOfBound\r\n");//TODO LE or GE?
		s.append("Return 0\r\n");
		s.append("_indexOutOfBound:\r\n");
		s.append("Library __println(strArrayAccess),Rdummy\r\n");
		s.append("Jump _errorExit\r\n\r\n");
		
		return s.toString();
	}
	
	@Override
	public Object visit(Program program) {
		program.setClassesOffsets();
		nameToClass = program.getNameToClass();
		
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
		lirOutput.append("strNullRef: \"Runtime Error: Null pointer dereference!\"\r\n");
		lirOutput.append("strArrayAccess: \"Runtime Error: Array index out of bounds!\"\r\n");
		lirOutput.append("strSize: \"Runtime Error: Array allocation with negative array size!\"\r\n");
		lirOutput.append("strZero: \"Runtime Error: Runtime Error: Division by zero!\"\r\n");
		for (Entry<String, String> stringLiteral : stringLiterals.entrySet()){
			lirOutput.append(stringLiteral.getValue() + ": " + stringLiteral.getKey() + "\r\n");
		}
		lirOutput.append("\r\n");
		
		//appending the dispatch tables defined during the run of the visitor
		lirOutput.append("# Dispatch Tables\r\n");
		for (Entry<String, String[]> classLabel : dispatchTables.entrySet()) {
			lirOutput.append(classLabel.getKey() + ": [");
			boolean first = true;
			for (String methodLabel : classLabel.getValue()){
				if (first){
					lirOutput.append(methodLabel);
					first = false;
					continue;
				}
				lirOutput.append("," + methodLabel);
			}
			lirOutput.append("]\r\n");
			
			//add comment for field offsets
			lirOutput.append(fieldOffsets.get(classLabel.getKey()));
		}
		lirOutput.append("\r\n");
		
		//append the runtime checks methods
		lirOutput.append("# RunTime Checks\r\n");
		lirOutput.append(runtimeChecks());
		
		// appending the blocks of methods
		lirOutput.append("# Method Blocks\r\n");
		lirOutput.append(instructions.toString());

		lirOutput.append("_errorExit:");
		
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

			return new NodeLirTrans(classInstructions.toString());
		}
		
		return new NodeLirTrans("");
	}

	@Override
	public Object visit(Field field) {
		return null;
	}
	
	private NodeLirTrans methodVisit(Method method){
		StringBuilder sb = new StringBuilder();
		for (Statement stmt : method.getStatements()) {
			sb.append("# line: " + stmt.getLine() + "\r\n");
			sb.append(((NodeLirTrans)stmt.accept(this)).codeTrans);
		}
		
		if (!checkMain(method)) {
			sb.append("Return 0 \r\n");
		}
		return new NodeLirTrans(sb.toString());
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
		NodeLirTrans locTrs = (NodeLirTrans)assignment.getVariable().accept(this);
		NodeLirTrans asgnTrs = loadGeneric(assignment.getAssignment());
		
		StringBuilder sb = new StringBuilder();
		sb.append(locTrs.codeTrans);
		sb.append(asgnTrs.codeTrans);
		
		switch (locTrs.type) {
		case Register:
		case Mem:
			sb.append("Move ");
			break;
		case ArrayLocation:
			sb.append("MoveArray ");
			break;
		case FieldLocation:
			sb.append("MoveField ");
			break;
		}
		
		sb.append(asgnTrs.result);
		sb.append(",");
		sb.append(locTrs.result);
		sb.append("\r\n");
		
		return new NodeLirTrans(sb.toString());
	}

	@Override
	public Object visit(CallStatement callStatement) {
		NodeLirTrans trs = loadGeneric(callStatement.getCall());
		return new NodeLirTrans(trs.codeTrans);
	}

	@Override
	public Object visit(Return returnStatement) {
		StringBuilder s = new StringBuilder();
		if (returnStatement.hasValue()) {
			NodeLirTrans valueTrs = loadGeneric(returnStatement.getValue());
			s.append(valueTrs.codeTrans);
			s.append("Return " + valueTrs.result + "\r\n");
		} else {
			s.append("Return 0\r\n");
		}
		
		return new NodeLirTrans(s.toString());
	}

	@Override
	public Object visit(If ifStatement) {
		StringBuilder s = new StringBuilder();
		int id = getNextId();
		NodeLirTrans condTrs = loadGeneric(ifStatement.getCondition());
		s.append(condTrs.codeTrans + "\r\n");
		s.append("Compare 0," + condTrs.result + "\r\n");
		s.append("JumpTrue _false_if_" + id + "\r\n");
		NodeLirTrans operTrans = (NodeLirTrans) ifStatement.getOperation().accept(this);
		s.append(operTrans.codeTrans);
		s.append("Jump _end_if_" + id + "\r\n");
		s.append("_false_if_" + id + ":\r\n");
		if (ifStatement.hasElse()){
			NodeLirTrans elseTrans = (NodeLirTrans) ifStatement.getElseOperation().accept(this);
			s.append(elseTrans.codeTrans);
		}
		s.append("end_if_" + id + ":\r\n");
		return new NodeLirTrans(s.toString());
	}

	@Override
	public Object visit(While whileStatement) {
		StringBuilder s = new StringBuilder();
		int id = getNextId();
		whileLables.push(id);
		s.append("_while_" + id + ":\r\n");
		NodeLirTrans condTrs = loadGeneric(whileStatement.getCondition());
		s.append(condTrs.codeTrans);
		s.append("Compare 0," + condTrs.result + "\r\n");
		s.append("JumpTrue _end_while_" + id + "\r\n");
		NodeLirTrans operTrs = (NodeLirTrans)whileStatement.getOperation().accept(this);
		s.append(operTrs.codeTrans);
		s.append("Jump _while_" + id + "\r\n");
		s.append("_end_while_" + id + ":\r\n");
		whileLables.pop();
		
		return new NodeLirTrans(s.toString()); 
	}

	@Override
	public Object visit(Break breakStatement) {
		return new NodeLirTrans("Jump _end_while_" + whileLables.peek() + "\r\n"); 
	}

	@Override
	public Object visit(Continue continueStatement) {
		return new NodeLirTrans("Jump _while_" + whileLables.peek() + "\r\n"); 
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		StringBuilder sb = new StringBuilder();
		for(Statement stmt : statementsBlock.getStatements()){
			NodeLirTrans sTrans = (NodeLirTrans) stmt.accept(this);
			sb.append(sTrans.codeTrans + "\r\n");
		}
		return new NodeLirTrans(sb.toString());
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		StringBuilder sb = new StringBuilder();
		if (localVariable.hasInitValue()) {
			NodeLirTrans init = loadGeneric(localVariable.getInitValue());
			sb.append(init.codeTrans);
			sb.append("Move ");
			sb.append(init.result + ",");
			sb.append(localVariable.getEnclosingScope().getEntry(localVariable.getName()).getDistinctId() + "\r\n");
		}
		
		return new NodeLirTrans(sb.toString());
	}

	@Override
	public Object visit(VariableLocation location) {
		StringBuilder s = new StringBuilder();
		if (location.isExternal()){
			NodeLirTrans expTrs1 = loadGeneric(location.getLocation());
			s.append(expTrs1.codeTrans);
			//check null reference
			s.append("StaticCall __checkNullRef(a="+expTrs1.result+"),Rdummy\r\n");
			
			// TODO : check if all expression have enclosingType
			ICClass classInstance = ((ClassType)location.getLocation().getEnclosingType()).getICClass();
			int varLocationNum = classInstance.getFieldOffset(location.getName()); 
			String fieldId = expTrs1.result + "." + varLocationNum;
			return new NodeLirTrans(s.toString(), fieldId, ResultType.FieldLocation);
		} else {
			if (location.getEnclosingScope().getEntryRecursive(location.getName()).getKind().equals(Kind.FIELD)) {
				String classLocationReg = RegisterPool.getRegister();
				s.append("Move this," + classLocationReg);
				s.append("\r\n");
				
				ICClass classInstance = nameToClass.get(location.getEnclosingScope().getVariableScope(location.getName()).getID());
				int varLocationNum = classInstance.getFieldOffset(location.getName()); 
				String fieldId = classLocationReg + "." + varLocationNum;
				return new NodeLirTrans(s.toString(), fieldId, ResultType.FieldLocation);
			} else {
				String resReg = location.getEnclosingScope().getEntryRecursive(location.getName()).getDistinctId();
				return new NodeLirTrans(s.toString(), resReg, ResultType.Mem);
			}
		}
	}

	@Override
	public Object visit(ArrayLocation location) {
		NodeLirTrans expTrs1 = loadGeneric(location.getArray());
		NodeLirTrans expTrs2 = loadGeneric(location.getIndex());
		StringBuilder s = new StringBuilder();
		s.append(expTrs1.codeTrans);
		s.append(expTrs2.codeTrans);
		//check null reference
		s.append("StaticCall __checkNullRef(a="+expTrs1.result+"),Rdummy\r\n");
		//check array access
		s.append("StaticCall __checkArrayAccess(a="+expTrs1.result+",i="+expTrs2.result+"),Rdummy\r\n");
		String arraySymbol = expTrs1.result + "[" + expTrs2.result + "]";
		return new NodeLirTrans(s.toString(), arraySymbol, ResultType.ArrayLocation);
	}

	public NodeLirTrans visitCall(Call call, ICClass icClass) {
		ICClass containingClass = icClass.getEnclosingScope().getVariableScope(call.getName()).getContainer();
		
		Method methodSig = null;
		for (Method method : containingClass.getMethods()) {
			if (method.getName().equals(call.getName())) {
				methodSig = method;
				break;
			}
		}
		
		StringBuilder expressions = new StringBuilder();
		StringBuilder results = new StringBuilder();
		int argCounter = 0;
		
		for (Expression arg : call.getArguments()) {
			NodeLirTrans argTrs = loadGeneric(arg);
			expressions.append(argTrs.codeTrans);
			
			String curFormal = methodSig.getEnclosingScope().getEntry(methodSig.getFormals().get(argCounter).getName()).getDistinctId();
			if (!icClass.getName().equals("Library")) {
				results.append(curFormal);
				results.append("=");
			}
			results.append(argTrs.result);
			results.append(",");
			argCounter++;
		}
		
		String resultsStr = results.length() > 0 ? results.substring(0, results.length() - 1) : "";
		
		return new NodeLirTrans(expressions.toString(), resultsStr);
	}
	
	@Override
	public Object visit(StaticCall call) {
		ICClass icClass = nameToClass.get(call.getClassName());
		String methodLabel = getMethodLabel(icClass, call.getName());
		
		StringBuilder sb = new StringBuilder();
		NodeLirTrans params = visitCall(call, icClass);
		sb.append(params.codeTrans);
		if (call.getClassName().equals("Library")) {
			sb.append("Library __");
			sb.append(call.getName());
		} else {
			sb.append("StaticCall ");
			sb.append(methodLabel);
		}
		
		sb.append("(");
		sb.append(params.result);
		sb.append("),");
		String resultRegister = RegisterPool.getRegister();
		sb.append(resultRegister + "\r\n");

		return new NodeLirTrans(sb.toString(), resultRegister);
	}

	@Override
	public Object visit(VirtualCall call) {
		String location;
		StringBuilder sb = new StringBuilder();
		ICClass icClass = null;
		if (!call.isExternal()) {
			location = "this";
			icClass = ((ClassType)call.getEnclosingScope().getEntryRecursive("this").getType()).getICClass();
		} else {
			NodeLirTrans locTrns = loadGeneric(call.getLocation());
			sb.append(locTrns.codeTrans);
			//check null reference
			sb.append("StaticCall __checkNullRef(a="+locTrns.result+"),Rdummy\r\n");
			location = locTrns.result;
			icClass = ((ClassType)call.getLocation().getEnclosingType()).getICClass();
		}
		
		String methodLabel = location + "." + icClass.getMethodOffset(call.getName());
		
		NodeLirTrans params = visitCall(call, icClass);
		sb.append(params.codeTrans);
		sb.append("VirtualCall ");
		sb.append(methodLabel);
		sb.append("(");
		sb.append(params.result);
		sb.append("),");
		String resultRegister = RegisterPool.getRegister();
		sb.append(resultRegister + "\r\n");
		
		return new NodeLirTrans(sb.toString(), resultRegister);
	}

	@Override
	public Object visit(This thisExpression) {
		return new NodeLirTrans("", "this");
	}

	@Override
	public Object visit(NewClass newClass) {
		StringBuilder s = new StringBuilder();
		int objectSize = this.nameToClass.get(newClass.getName()).GetClassSize();
		String resultRegister = RegisterPool.getRegister();
		s.append("Library __allocateObject(" + objectSize + "),");
		s.append(resultRegister + "\r\n");
		s.append("MoveField " + getClassLabel(newClass.getName()) + ",");
		s.append(resultRegister + ".0\r\n");
		return new NodeLirTrans(s.toString(),resultRegister);
	}

	@Override
	public Object visit(NewArray newArray) {
		NodeLirTrans expTrs = loadGeneric(newArray.getSize());
		StringBuilder s = new StringBuilder();
		s.append(expTrs.codeTrans);
		//check null reference
		s.append("StaticCall __checkSize(n="+expTrs.result+"),Rdummy\r\n");
		s.append("Library __allocateArray(" + expTrs.result + "),");
		s.append(expTrs.result + "\r\n");
		return new NodeLirTrans(s.toString(), expTrs.result);
	}

	@Override
	public Object visit(Length length) {
		NodeLirTrans expTrs = loadGeneric(length.getArray());
		StringBuilder s = new StringBuilder();		
		s.append(expTrs.codeTrans);
		//check null reference
		s.append("StaticCall __checkNullRef(a="+expTrs.result+"),Rdummy\r\n");
		s.append("ArrayLength ");
		s.append(expTrs.result + ",");
		s.append(expTrs.result + "\r\n");
		return new NodeLirTrans(s.toString(), expTrs.result);
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		NodeLirTrans expTrs1 = loadGeneric(binaryOp.getSecondOperand());
		NodeLirTrans expTrs2 = loadGeneric(binaryOp.getFirstOperand());
		StringBuilder s = new StringBuilder();
		s.append(expTrs1.codeTrans);
		s.append(expTrs2.codeTrans);
		switch(binaryOp.getOperator()){
		case PLUS:
			// TODO : check all expressions have enclosing types
			if (binaryOp.getFirstOperand().getEnclosingType().equals(TypeTable.stringType)){
				s.append("Library __stringCat(");
				s.append(expTrs2.result+",");
				s.append(expTrs1.result+"),");
				s.append(expTrs2.result + "\r\n");
				return new NodeLirTrans(s.toString(), expTrs2.result);
			}
			else{
				s.append("Add ");
				s.append(expTrs1.result+",");
				s.append(expTrs2.result+"\r\n");
				return new NodeLirTrans(s.toString(), expTrs2.result);
			}
		case MINUS:
			s.append("Sub ");
			s.append(expTrs1.result+",");
			s.append(expTrs2.result+"\r\n");
			return new NodeLirTrans(s.toString(), expTrs2.result);
		case MULTIPLY:
			s.append("Mul ");
			s.append(expTrs1.result+",");
			s.append(expTrs2.result+"\r\n");
			return new NodeLirTrans(s.toString(), expTrs2.result);
		case DIVIDE:
			//division by zero
			s.append("StaticCall __checkZero(b="+expTrs2.result+"),Rdummy\r\n");
			
			s.append("Div ");
			s.append(expTrs1.result+",");
			s.append(expTrs2.result+"\r\n");
			return new NodeLirTrans(s.toString(), expTrs2.result);
		case MOD:
			s.append("Mod ");
			s.append(expTrs1.result+",");
			s.append(expTrs2.result+"\r\n");
			return new NodeLirTrans(s.toString(), expTrs2.result);
		default: 
			return null;
		}
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		NodeLirTrans expTrs1 = loadGeneric(binaryOp.getSecondOperand());
		NodeLirTrans expTrs2 = loadGeneric(binaryOp.getFirstOperand(), false);
		
		StringBuilder s = new StringBuilder();
		s.append(expTrs1.codeTrans);
		s.append(expTrs2.codeTrans);
		
		switch(binaryOp.getOperator()){
		case LAND:
			s.append("And ");
			s.append(expTrs1.result+",");
			s.append(expTrs2.result+"\r\n");
			return new NodeLirTrans(s.toString(), expTrs2.result);
		case LOR:
			s.append("Or ");
			s.append(expTrs1.result+",");
			s.append(expTrs2.result+"\r\n");
			return new NodeLirTrans(s.toString(), expTrs2.result);
		case LT:
			s.append("Compare ");
			s.append(expTrs1.result+",");
			s.append(expTrs2.result+"\r\n");
			s.append("JumpL ");
			break;
		case LTE:
			s.append("Compare ");
			s.append(expTrs1.result+",");
			s.append(expTrs2.result+"\r\n");
			s.append("JumpLE ");
			break;
		case GT:
			s.append("Compare ");
			s.append(expTrs1.result+",");
			s.append(expTrs2.result+"\r\n");
			s.append("JumpG ");
			break;
		case GTE:
			s.append("Compare ");
			s.append(expTrs1.result+",");
			s.append(expTrs2.result+"\r\n");
			s.append("JumpGE ");//TODO i think it's the other way around (a<=b -> b GE a), if you intended it this way then never mind
			break;
		case EQUAL:
			s.append("Compare ");
			s.append(expTrs1.result+",");
			s.append(expTrs2.result+"\r\n");
			s.append("JumpTrue ");
			break;
		case NEQUAL:
			s.append("Compare ");
			s.append(expTrs1.result+",");
			s.append(expTrs2.result+"\r\n");
			s.append("JumpFalse ");
		}
		int id = getNextId();
		s.append("_true_" + id + "\r\n");
		s.append("Move 0," + expTrs2.result + "\r\n");
		s.append("Jump " + "_end_boolean_" + id + "\r\n");
		s.append("_true_" + id + ":\r\n"); // if true
		s.append("Move 1," + expTrs2.result + "\r\n");	
		s.append("_end_boolean_" + id + ":\r\n");
		return new NodeLirTrans(s.toString(), expTrs2.result);
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		NodeLirTrans expTrs = loadGeneric(unaryOp.getOperand());
		StringBuilder s = new StringBuilder();
		s.append(expTrs.codeTrans);
		s.append("Mul -1,");
		s.append(expTrs.result + "\r\n");
		return new NodeLirTrans(s.toString(), expTrs.result);
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		NodeLirTrans expTrs = loadGeneric(unaryOp.getOperand());
		StringBuilder s = new StringBuilder();
		s.append(expTrs.codeTrans);
		s.append("Not ");//TODO neg?
		s.append(expTrs.result + "\r\n");
		return new NodeLirTrans(s.toString(), expTrs.result);
	}

	@Override
	public Object visit(Literal literal) {
		String literalIdentifier = null;
		switch (literal.getType()) {
		case FALSE:
			literalIdentifier = "0";
			break;
		case INTEGER:
			literalIdentifier = literal.getValue().toString();
			break;
		case NULL:
			// TODO : ?
			literalIdentifier = "0";
			break;
		case STRING:
			literalIdentifier = getStringLiteralName(literal.getValue().toString());
			break;
		case TRUE:
			literalIdentifier = "1";
			break;
		}

		StringBuilder s = new StringBuilder();
		s.append("Move ");
		s.append(literalIdentifier + ",");
		String resultRegister = RegisterPool.getRegister();
		s.append(resultRegister + "\r\n");
		return new NodeLirTrans(s.toString(),resultRegister);
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		return expressionBlock.getExpression().accept(this);
	}

}
