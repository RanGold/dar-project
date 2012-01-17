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
	
	public static String getIdentifier(List<Type> arguments, Type returnVal) {
		StringBuilder sb = new StringBuilder("{");

		for (Type argument: arguments) {
			sb.append(argument.toString());
			sb.append(", ");
		}

		if (arguments.size() > 0) {
			sb.deleteCharAt(sb.length() - 2);
		} else {
			sb.append(" ");
		}
		sb.append("-> ");
		sb.append(returnVal.toString());
		sb.append("}");

		return sb.toString();
	}
	
	@Override
	public String toString() {
		return MethodType.getIdentifier(this.getArguments(), this.getReturnVal());
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
