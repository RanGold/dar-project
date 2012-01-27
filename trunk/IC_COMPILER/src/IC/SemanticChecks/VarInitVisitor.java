package IC.SemanticChecks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.BinaryOp;
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
import IC.SymbolTables.Symbol;

public class VarInitVisitor implements Visitor {
	

	private HashMap<If, LinkedList<VarRec>> ifAssignments = new HashMap<If, LinkedList<VarRec>>();
	private Map<If, Boolean> ifState = new HashMap<If, Boolean>();
	private Stack<If> ifStack = new Stack<If>();
	private If curIf;
	
	private static class VarRec {
		public String name;
		public boolean ifInit;
		public boolean elseInit;
		public boolean prevInit;
		
		public VarRec(String name, boolean ifInit, boolean elseInit, boolean prevInit) {
			this.name = name;
			this.ifInit = ifInit;
			this.elseInit = elseInit;
			this.prevInit = prevInit;
		}
		
		public VarRec(String name) {
			this.name = name;
			this.ifInit = false;
			this.elseInit = false;
		}

		@Override
		public boolean equals(Object obj) {
			return ((obj instanceof VarRec && ((VarRec)obj).name.equals(this.name)) ||
					(obj instanceof String && ((String)obj).equals(this.name)));
		}
	}

	public Object visit(Program program) {
		curIf = null;
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
		assignment.getAssignment().accept(this);
		
		VariableLocation varLoc;
		if (assignment.getVariable() instanceof VariableLocation) {
			varLoc = (VariableLocation)assignment.getVariable();
		} else {
			// Only other option is arrayLocation
			Expression varExp = ((ArrayLocation)assignment.getVariable()).getArray();
			if (varExp instanceof VariableLocation) {
				varLoc = (VariableLocation)varExp;
			} else {
				return true;
			}
		}
		
		Symbol varLocSym = varLoc.getEnclosingScope().getEntry(varLoc.getName());
		
		if (!varLoc.isExternal() && varLocSym.getKind() == Kind.VAR) {
			boolean isPrevInit = varLoc.getEnclosingScope().getEntryRecursive(varLoc.getName()).isInitialized();
			varLoc.getEnclosingScope().getEntryRecursive(varLoc.getName()).setInitialized(true);
			if (this.curIf != null) {
				if (ifState.get(curIf)) {
					if (this.ifAssignments.get(curIf).indexOf(new VarRec(varLoc.getName())) == -1) {
						this.ifAssignments.get(curIf).add(new VarRec(varLoc.getName(), true, false, isPrevInit));
					}
				} else {
					int index = this.ifAssignments.get(curIf).indexOf(new VarRec(varLoc.getName()));
					if (index == -1) {
						this.ifAssignments.get(curIf).add(new VarRec(varLoc.getName(), false, true, isPrevInit));
					} else {
						this.ifAssignments.get(curIf).get(index).elseInit = true;
					}
				}
			}
		}
		
		assignment.getVariable().accept(this);
		return true;
	}

	public Object visit(CallStatement callStatement) {
		return callStatement.getCall().accept(this);
	}
	
	public Object visit(Return returnStatement) {

		if (returnStatement.hasValue()) {
			return returnStatement.getValue().accept(this);
		}
		
		return true;
	}

	public Object visit(If ifStatement) {
		// Check if condition of type boolean
		ifStatement.getCondition().accept(this);
		
		ifStack.push(curIf);
		ifAssignments.put(ifStatement, new LinkedList<VarInitVisitor.VarRec>());
		
		curIf = ifStatement;
		ifState.put(curIf, true);

		// Visit the operation in ifStatement
		ifStatement.getOperation().accept(this);
		
		for (VarRec var : ifAssignments.get(ifStatement)) {
			if (var.ifInit && !var.prevInit) {
				ifStatement.getEnclosingScope().getEntryRecursive(var.name).setInitialized(false);
			}
		}
		
		ifState.remove(curIf);
		ifState.put(curIf, false);
		// Visit the else operation if exists
		if (ifStatement.hasElse()) {
			ifStatement.getElseOperation().accept(this);
		}
		
		for (VarRec var : ifAssignments.get(ifStatement)) {
			if (var.ifInit && !var.prevInit) {
				ifStatement.getEnclosingScope().getEntryRecursive(var.name).setInitialized(true);
			}
		}
		
		If prevIf = ifStack.peek();
		for (VarRec var : ifAssignments.get(ifStatement)) {
			if ((!var.ifInit || !var.elseInit) && !var.prevInit) {
				System.err.println("Semantic warning at line " + ifStatement.getLine() + 
						": varibale " + var.name + " might be uninitialized after this if");
			} else {
				if (prevIf != null) {
					 List<VarRec> prevVars = ifAssignments.get(prevIf);
					 if (prevVars.indexOf(var) == -1) {
						 prevVars.add(new VarRec(var.name, false, false, var.prevInit));
					 }
					 
					 int index = prevVars.indexOf(var);
					 
					 if (ifState.get(prevIf)) {
						 prevVars.get(index).ifInit = true;
					 } else {
						 prevVars.get(index).elseInit = true;
					 }
				}
			}
		}
		
		ifState.remove(curIf);
		ifAssignments.remove(curIf);
		curIf = ifStack.pop();
		return true;
	}

	public Object visit(While whileStatement) {
		// Check while condition of type boolean
		whileStatement.getCondition().accept(this);
		
		// Check the operation in whileStatement
		whileStatement.getOperation().accept(this);
		
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
		
		Symbol locSym = localVariable.getEnclosingScope().getEntryRecursive(localVariable.getName());
		locSym.setInitialized(localVariable.hasInitValue());
		return true;
	}

	@Override
	public Object visit(VariableLocation location) {	
		
		if (!location.isExternal()) {
			Symbol locSym = location.getEnclosingScope().getEntry(location.getName());
			if (!(locSym.getKind() == Kind.FIELD || locSym.getKind() == Kind.FORMAL || (locSym.getKind() == Kind.VAR && locSym.isInitialized()))) {
				System.err.println("Semantic warning at line " + location.getLine() + ": Use of uninitialized local varibale " + location.getName());
			}
		} else {
			location.getLocation().accept(this);
		}
		
		return true;
	}

	public Object visit(ArrayLocation location) {
		// check index is int type
		Boolean resIndex = (Boolean)location.getIndex().accept(this);

		// check array is an array type
		Boolean resArray = (Boolean)location.getArray().accept(this);
		
		return resArray && resIndex;
	}

	private Object checkCallArguments(Call call) {
		for (int i = 0; i < call.getArguments().size(); i++) {
			if (!(Boolean)call.getArguments().get(i).accept(this)) {
				return false;
			}
		}
		
		return true;
	}
	
	public Object visit(StaticCall call) {
		return checkCallArguments(call);
	}

	public Object visit(VirtualCall call) {
		if (call.isExternal()) {
			if (!(Boolean)call.getLocation().accept(this)) {
				return false;
			}
		}
		
		return checkCallArguments(call);
	}

	public Object visit(This thisExpression) {
		return true;
	}

	public Object visit(NewClass newClass) {
		return true;
	}


	public Object visit(NewArray newArray) {
		return newArray.getSize().accept(this);
	}

	public Object visit(Length length) {
		return length.getArray().accept(this);
	}

	private Boolean visitBinOp(BinaryOp binaryOp) {
		Boolean binaryOp1 = (Boolean)binaryOp.getFirstOperand().accept(this);
		Boolean binaryOp2 = (Boolean)binaryOp.getSecondOperand().accept(this);
				
		return binaryOp1 && binaryOp2;
	}

	public Object visit(MathBinaryOp binaryOp) {
		return this.visitBinOp(binaryOp);
	}

	public Object visit(LogicalBinaryOp binaryOp) {
		return this.visitBinOp(binaryOp);
	}

	public Object visit(MathUnaryOp unaryOp) {
        return unaryOp.getOperand().accept(this);
	}

	public Object visit(LogicalUnaryOp unaryOp) {
        return unaryOp.getOperand().accept(this);
	}

	public Object visit(Literal literal) {
		return true;
	}

	public Object visit(ExpressionBlock expressionBlock) {
		return expressionBlock.getExpression().accept(this);
	}
}
