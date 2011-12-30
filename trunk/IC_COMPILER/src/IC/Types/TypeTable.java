package IC.Types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.SemanticChecks.SemanticError;

public class TypeTable {
	// Stores the table types' strings
	private static Map<String, Type> uniquePrimitveTypes = new HashMap<String, Type>();
	
	// Maps element types to array types
	private static Map<Type, ArrayType> uniqueArrayTypes = new HashMap<Type, ArrayType>();

	// Maps identifiers to class types
	private static Map<String, ClassType> uniqueClassTypes = new HashMap<String, ClassType>();
	
	// Maps identifiers to method types
	private static Map<String, MethodType> uniqueMethodTypes = new HashMap<String, MethodType>();

	// TODO : maybe add a map for primitives?
	// Primitive types
	public static Type intType = new IntType();
	public static Type boolType = new BoolType();
	public static Type nullType = new NullType();
	public static Type stringType = new StringType();
	public static Type voidType = new VoidType();

	// Returns unique array type object
	public static ArrayType arrayType(Type elemType) {
		if (!uniqueArrayTypes.containsKey(elemType)) {
			// object doesn’t exist – create and return it
			ArrayType arrt = new ArrayType(elemType);
			uniqueArrayTypes.put(elemType, arrt);
		}
		
		return uniqueArrayTypes.get(elemType);
	}

	// TODO: We can set the superclass later for existing classes, and only check for circles each time
	// Returns unique class type object
	public static ClassType classType(String identifier, String superIdentifier, int line) throws SemanticError {
		ClassType cls;
		
		if (uniqueClassTypes.containsKey(identifier)) {
			// class type object already created – return it
			cls = uniqueClassTypes.get(identifier);
			if (superIdentifier != null && cls.getSuperClass() == null) {
				
			}
		} else {
			// object doesn’t exist – create and return it
			if (superIdentifier == null) {
				cls = new ClassType(identifier, null);
			} else if (uniqueClassTypes.containsKey(superIdentifier)) {
				ClassType superClass = uniqueClassTypes.get(superIdentifier);
				cls = new ClassType(identifier, superClass);

				// Checking if no circles in the class diagram
				if (superClass.subtypeof(cls)) {
					throw new SemanticError("The superclass " + superIdentifier + " is a subclass of " + identifier, line);
				}
				
			} else {
				throw new SemanticError("The superclass " + superIdentifier + " doesn't exist for " + identifier, line);
			}
			
			uniqueClassTypes.put(identifier, cls);
		}
		
		return cls;
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
		
		ClassType superClass;
		if (type.getTypeClass() == TypeClass.Class && ((superClass = ((ClassType)type).getSuperClass()) != null)) {
			sb.append(", Superclass ID:" + superClass.getTypeId());
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