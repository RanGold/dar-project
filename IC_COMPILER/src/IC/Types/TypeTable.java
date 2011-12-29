package IC.Types;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import IC.SemanticChecks.SemanticError;

public class TypeTable {
	// Stores the table types' strings
	private static List<MetaType> typesInTable = new LinkedList<MetaType>();
	private static boolean isPrimitveAdded = false;
	
	// Maps element types to array types
	private static Map<Type, ArrayType> uniqueArrayTypes = new HashMap<Type, ArrayType>();

	// Maps identifiers to class types
	private static Map<String, ClassType> uniqueClassTypes = new HashMap<String, ClassType>();
	
	// Maps identifiers to method types
	private static Map<String, MethodType> uniqueMethodTypes = new HashMap<String, MethodType>();

	// Primitive types
	public static Type intType = new IntType();
	public static Type boolType = new BoolType();
	public static MetaType nullType = new NullType();
	public static Type stringType = new StringType();
	public static Type voidType = new VoidType();

	// Returns unique array type object
	public static ArrayType arrayType(Type elemType) {
		ArrayType arrt;
		
		if (uniqueArrayTypes.containsKey(elemType)) {
			// array type object already created – return it
			arrt = uniqueArrayTypes.get(elemType);
		} else {
			// object doesn’t exist – create and return it
			arrt = new ArrayType(elemType);
			uniqueArrayTypes.put(elemType, arrt);
		}
		
		TypeTable.typesInTable.add(arrt);
		return arrt;
	}

	// Returns unique class type object
	public static ClassType classType(String identifier, String superIdentifier, int line) throws SemanticError {
		ClassType cls;
		
		if (uniqueClassTypes.containsKey(identifier)) {
			// class type object already created – return it
			cls = uniqueClassTypes.get(identifier);
		} else {
			// object doesn’t exist – create and return it
			if (superIdentifier == null) {
				cls = new ClassType(identifier, null);
			} else if (uniqueClassTypes.containsKey(superIdentifier)) {
				cls = new ClassType(identifier, uniqueClassTypes.get(superIdentifier));
			} else {
				throw new SemanticError("The superclass " + superIdentifier + " doesn't exist for " + identifier, line);
			}
			
			uniqueClassTypes.put(identifier, cls);
		}
		
		TypeTable.typesInTable.add(cls);
		return cls;
	}
	
	// Returns unique method type object
	public static MethodType methodType(List<Type> arguments, Type returnVal) throws SemanticError {
		MethodType mtd = new MethodType(arguments, returnVal);
		String identifier = (mtd).toString();
		
		if (uniqueMethodTypes.containsKey(identifier)) {
			// class type object already created – return it
			mtd = uniqueMethodTypes.get(identifier);
		} else {
			// object doesn’t exist – create and return it
			uniqueMethodTypes.put(identifier, mtd);
		}
		
		TypeTable.typesInTable.add(mtd);
		return mtd;
	}
	
	private static void addPrimitveTypes() {
		TypeTable.typesInTable.add(0, voidType);
		TypeTable.typesInTable.add(0, stringType);
		TypeTable.typesInTable.add(0, nullType);
		TypeTable.typesInTable.add(0, boolType);
		TypeTable.typesInTable.add(0, intType);
		TypeTable.isPrimitveAdded = true;
	}
	
	public static String GetString() {
		if (!TypeTable.isPrimitveAdded) {
			addPrimitveTypes();
		}
		
		StringBuilder sb = new StringBuilder();
		
		int id = 1;
		for (MetaType curType : TypeTable.typesInTable) {
			sb.append(id);
			sb.append(": ");
			sb.append(curType.getTypeClass().toString());
			sb.append(" type: ");
			sb.append(curType.toString());
			
			ClassType superClass;
			if (curType.getTypeClass() == TypeClass.Class && ((superClass = ((ClassType)curType).getSuperClass()) != null)) {
				sb.append(", Superclass ID:" + (TypeTable.typesInTable.indexOf(superClass) + 1));
			}
			id++;
		}
		
		return sb.toString();
	}
}