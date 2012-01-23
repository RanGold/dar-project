package lir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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

public class TranslationVisitor implements Visitor {
	private Map<String, String> stringLiterals;
	private Map<String, List<String>> dispatchTable;
	private Map<String, String> fieldOffsets;
	private StringBuilder lirOutput;
	private StringBuilder instructions;
	private int strNum;
	
	public TranslationVisitor() {
		this.stringLiterals = new LinkedHashMap<String, String>();
		this.dispatchTable = new LinkedHashMap<String, List<String>>();
		this.fieldOffsets = new LinkedHashMap<String, String>();
		this.lirOutput = new StringBuilder();
		this.instructions = new StringBuilder();
		this.strNum = 1;
	}
	
	/**
	 * the method receives a string literal, and returns its label
	 * @param key - the string literal (i.e. "daniel")
	 * @return the label if exist, or new label if it doesn't already exist
	 */
	private String getStringLiteralName(String key){//TODO verify "" before string literals
		String value = stringLiterals.get(key);
		//entry does not exist
		if (value == null){
			value = "str" + strNum;
			stringLiterals.put(key, value);
			strNum++;
		}
		return value;
	}
	
	/**
	 * adds label for class to dispatch table
	 * @param className - the name of a class
	 * @return the label for the input class name, if it doesn't exist it creates a new label
	 */
	private String getClassLabel(String className){
		String label = "_DV_" + className;
		if (dispatchTable.get(label) == null){
			dispatchTable.put(label, new ArrayList<String>());
		}
		return label;
	}
	
	/**
	 * adds label for method to dispatch table
	 * @param className - the name of a class
	 * @param methodName - the name of a method
	 * @return the label of the method of the class, if it doesn't exist it creates a new label
	 */
	private String getMethodLabel(String className, String methodName){
		String classLabel = getClassLabel(className);
		String methodLabel = "_" + methodName;
		if (!dispatchTable.get(classLabel).contains(methodLabel)){
			dispatchTable.get(classLabel).add(methodLabel);
		}
		return methodLabel;
	}
	
	@Override
	public Object visit(Program program) {
		program.setClassesOffsets();
		
		for (ICClass icClass : program.getClasses())
		{
			instructions.append((String)icClass.accept(this));
			instructions.append("\r\n");
		}
		
		lirOutput.append("# Lir code\r\n\r\n");
		
		//appending the string literals defined during the run of the visitor
		lirOutput.append("# String Literals\r\n");
		for (Entry<String, String> stringLiteral : stringLiterals.entrySet()){
			lirOutput.append(stringLiteral.getValue() + ": " + stringLiteral.getKey() + "\r\n");
		}
		lirOutput.append("\r\n");
		
		//appending the dispatch tables defined during the run of the visitor
		lirOutput.append("# Dispatch Tables\r\n");
		for (Entry<String, List<String>> classLabel : dispatchTable.entrySet()){
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
			for (Method method : icClass.getMethods()){
				//building the dispatch table
				if (method instanceof VirtualMethod){
					getMethodLabel(icClass.getName(), method.getName());
				}
			}
			
			//adding comments for field offsets
			StringBuilder fieldOffsetsComment = new StringBuilder();
			fieldOffsetsComment.append("# field offsets:\r\n");
			for (Field field : icClass.getFields()){
				fieldOffsetsComment.append("# " + field.getName() + ": " + icClass.getFieldOffset(field.getName()) + "\r\n");
			}
			fieldOffsets.put(getClassLabel(icClass.getName()), fieldOffsetsComment.toString());
		}
		
		for (Method method : icClass.getMethods()){
			classInstructions.append((String)(method.accept(this)==null?"":method.accept(this)));
		}
		classInstructions.append("\r\n");
		
		return classInstructions.toString();
	}

	@Override
	public Object visit(Field field) {
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
