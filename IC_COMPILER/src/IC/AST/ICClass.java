package IC.AST;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class declaration AST node.
 * 
 * @author Tovi Almozlino
 */
public class ICClass extends ASTNode {

	private String name;

	private String superClassName = null;

	private List<Field> fields;

	private List<Method> methods;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new class node.
	 * 
	 * @param line
	 *            Line number of class declaration.
	 * @param name
	 *            Class identifier name.
	 * @param fields
	 *            List of all fields in the class.
	 * @param methods
	 *            List of all methods in the class.
	 */
	public ICClass(int line, String name, List<Field> fields, List<Method> methods) {
		super(line);
		this.name = name;
		this.fields = fields;
		this.methods = methods;
	}

	/**
	 * Constructs a new class node, with a superclass.
	 * 
	 * @param line
	 *            Line number of class declaration.
	 * @param name
	 *            Class identifier name.
	 * @param superClassName
	 *            Superclass identifier name.
	 * @param fields
	 *            List of all fields in the class.
	 * @param methods
	 *            List of all methods in the class.
	 */
	public ICClass(int line, String name, String superClassName,
			List<Field> fields, List<Method> methods) {
		this(line, name, fields, methods);
		this.superClassName = superClassName;

	}

	public String getName() {
		return name;
	}

	public boolean hasSuperClass() {
		return (superClassName != null);
	}

	public String getSuperClassName() {
		return superClassName;
	}

	public List<Field> getFields() {
		return fields;
	}

	public List<Method> getMethods() {
		return methods;
	}
	
	public int getFieldOffset(String fieldName){
		return fieldToOffset.get(fieldName);
	}
	
	public int getMethosOffset(String MethodName){
		return methodToOffset.get(MethodName);
	}
	
	public void copyMyOffset(Map<String, Integer> methods, Map<String, Integer> fields){//private
		for (Entry<String, Integer> entry: this.methodToOffset.entrySet()){
			methods.put(entry.getKey(), entry.getValue());
		}
		for (Entry<String, Integer> entry: this.fieldToOffset.entrySet()){
			fields.put(entry.getKey(), entry.getValue());
		}
	}
	
	
	public void initOffsets(ICClass father){
		this.methodToOffset = new HashMap<String, Integer>();
		this.fieldToOffset = new HashMap<String, Integer>();
		int methodCounter = 0;
		int fieldCounter = 1;
		
		//get fathers fields and methods
		if (father!=null){
			father.copyMyOffset(this.methodToOffset, this.fieldToOffset);
			Integer[] arr = methodToOffset.values().toArray(new Integer[0]); 
			Arrays.sort(arr);
			methodCounter = arr[arr.length - 1] + 1;
			arr = fieldToOffset.values().toArray(new Integer[0]);
			Arrays.sort(arr);
			fieldCounter = arr[arr.length -1] + 1;
		}
		
		//enter the class fields and methods
		for (Method m : methods){
			if (!methodToOffset.containsKey(m.getName())){
				methodToOffset.put(m.getName(),methodCounter);
				methodCounter++;
			}		
		}
		for (Field f : fields){
			if (!fieldToOffset.containsKey(f.getName())){
				fieldToOffset.put(f.getName(),fieldCounter);
				fieldCounter++;
			}
		}	
	}
	
	//class layout
	private Map<String, Integer> methodToOffset;
	private Map<String, Integer> fieldToOffset;

}
