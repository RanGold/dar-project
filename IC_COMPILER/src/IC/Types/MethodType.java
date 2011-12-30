package IC.Types;

import java.util.LinkedList;
import java.util.List;

public class MethodType extends Type {

	private List<Type> arguments;
	private Type returnVal;
	
	public MethodType(List<Type> arguments, Type returnVal) {
		super();
		this.arguments = new LinkedList<Type>(arguments);
		this.returnVal = returnVal;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		
		for (Type argument: this.arguments) {
			sb.append(argument.toString());
			sb.append(", ");
		}
		
		sb.deleteCharAt(sb.length() - 2);
		sb.append("-> ");
		sb.append(returnVal.toString());
		sb.append("}");
		
		return sb.toString();
	}

	@Override
	public TypeClass getTypeClass() {
		return TypeClass.Method;
	}
	
	public Type getReturnVal() {
		return this.returnVal;
	}
	
	public List<Type> getArguments() {
		return new LinkedList<Type>(this.arguments);
	}

	@Override
	public boolean subtypeof(Type t) {
		return false;
	}
}
