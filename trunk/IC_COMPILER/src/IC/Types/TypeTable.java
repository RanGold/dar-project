package IC.Types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.AST.ICClass;
import IC.SemanticChecks.SemanticError;

public class TypeTable {
	// Stores the table types' strings
	private static Map<String, PrimitiveType> uniquePrimitveTypes = new HashMap<String, PrimitiveType>();
	
	// Maps element types to array types
	private static Map<Type, ArrayType> uniqueArrayTypes = new HashMap<Type, ArrayType>();

	// Maps identifiers to class types
	private static Map<String, ClassType> uniqueClassTypes = new HashMap<String, ClassType>();
	
	// Maps identifiers to method types
	private static Map<String, MethodType> uniqueMethodTypes = new HashMap<String, MethodType>();

	// TODO : maybe add a map for primitives?
	// Primitive types
	public static PrimitiveType intType = new IntType();
	public static PrimitiveType boolType = new BoolType();
	public static PrimitiveType nullType = new NullType();
	public static PrimitiveType stringType = new StringType();
	public static PrimitiveType voidType = new VoidType();

	// Returns unique array type object
	public static ArrayType arrayType(Type elemType) {
		if (!uniqueArrayTypes.containsKey(elemType)) {
			// object doesn’t exist – create and return it
			ArrayType arrt = new ArrayType(elemType);
			uniqueArrayTypes.put(elemType, arrt);
		}
		
		return uniqueArrayTypes.get(elemType);
	}
	
	// Returns existing unique class type object or null for non existing
	public static ClassType getClassType(String identifier) {
		if (!TypeTable.uniqueClassTypes.containsKey(identifier)) {
			return null;
		} else {
			return TypeTable.uniqueClassTypes.get(identifier);
		}
	}
	
	// Returns unique class type object
	public static ClassType classType(ICClass icClass) {
		ClassType cls;
		String identifier = icClass.getName();
		
		if (uniqueClassTypes.containsKey(identifier)) {
			// class type object already created – return it
			cls = uniqueClassTypes.get(identifier);
		} else {
			// object doesn’t exist – create and return it
			cls = new ClassType(icClass);
			uniqueClassTypes.put(identifier, cls);
		}
		
		return cls;
	}
	
	public static void validateTypesTable() throws SemanticError {
		// Checking for class existance
		for (ClassType classType : TypeTable.uniqueClassTypes.values()) {
			String superName = classType.getICClass().getSuperClassName();
			if (superName != null && TypeTable.getClassType(superName) == null) {
				throw new SemanticError("The superclass " + superName + " doesn't exist for " + classType.getICClass().getName(), 
						classType.getICClass().getLine());
			}
		}
		
		// Checking if no circles in the class diagram
		// This check is separated from the upper one, cause failure 
		// at any stage on the upper one can lead to NullPointer Exception on this check
		for (ClassType classType : TypeTable.uniqueClassTypes.values()) {
			String superName = classType.getICClass().getSuperClassName();
			if (superName != null && TypeTable.getClassType(superName).subtypeof(classType)) {
				throw new SemanticError("The superclass " + superName + " is a subclass of " + classType.getICClass().getName(), 
						classType.getICClass().getLine());
			}
		}
	}
	
	// Returns unique method type object
	public static MethodType methodType(List<Type> arguments, Type returnVal) throws SemanticError {
		MethodType mtd = new MethodType(arguments, returnVal);
		String identifier = (mtd).toString();
		
		if (!uniqueMethodTypes.containsKey(identifier)) {
			// object doesn’t exist – create and return it
			uniqueMethodTypes.put(identifier, mtd);
		}
		
		return uniqueMethodTypes.get(identifier);
	}
	
	public static PrimitiveType primitiveType(String identifier) {
		addPrimitveTypes();
		return TypeTable.uniquePrimitveTypes.get(identifier);
	}
	
	private static void addPrimitveTypes() {
		if (!TypeTable.uniquePrimitveTypes.containsKey("int")) {
			TypeTable.uniquePrimitveTypes.put("int", intType);
		}
		if (!TypeTable.uniquePrimitveTypes.containsKey("boolean")) {
			TypeTable.uniquePrimitveTypes.put("boolean", boolType);
		}
		
		if (!TypeTable.uniquePrimitveTypes.containsKey("null")) {
			TypeTable.uniquePrimitveTypes.put("null", nullType);
		}
		
		if (!TypeTable.uniquePrimitveTypes.containsKey("string")) {
			TypeTable.uniquePrimitveTypes.put("string", stringType);
		}
		
		if (!TypeTable.uniquePrimitveTypes.containsKey("void")) {
			TypeTable.uniquePrimitveTypes.put("void", voidType);
		}
	}
	
	private static void appendTypeString(Type type, StringBuilder sb) {
		sb.append('\t');
		sb.append(type.getTypeId());
		sb.append(": ");
		sb.append(type.getTypeClass().toString());
		sb.append(" type: ");
		sb.append(type.toString());
		sb.append('\n');
		
		String superClass;
		if (type.getTypeClass() == TypeClass.Class && ((superClass = ((ClassType)type).getICClass().getSuperClassName()) != null)) {
			sb.append(", Superclass ID:" + TypeTable.getClassType(superClass).getTypeId());
		}
	}
	
	public static String getString() {
		addPrimitveTypes();
		
		StringBuilder sb = new StringBuilder();
		
		for (Type curType : TypeTable.uniquePrimitveTypes.values()) {
			TypeTable.appendTypeString(curType, sb);
		}
		
		for (Type curType : TypeTable.uniqueClassTypes.values()) {
			TypeTable.appendTypeString(curType, sb);
		}
		
		for (Type curType : TypeTable.uniqueArrayTypes.values()) {
			TypeTable.appendTypeString(curType, sb);
		}
		
		for (Type curType : TypeTable.uniqueMethodTypes.values()) {
			TypeTable.appendTypeString(curType, sb);
		}
		
		return sb.toString();
	}
}