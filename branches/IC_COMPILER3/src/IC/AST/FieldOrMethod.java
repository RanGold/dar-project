package IC.AST;

import java.util.LinkedList;
import java.util.List;

public abstract class FieldOrMethod extends ASTNode {

	protected Type type;

	protected String name;
	
	protected FieldOrMethod(Type type, String name) {
		super(type.getLine());

		this.type = type;
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}
	
	public static List<Field> getFields(List<FieldOrMethod> list) {
		
		List<Field> fields = new LinkedList<Field>();
		
		for (FieldOrMethod curFOM : list) {
			if (curFOM instanceof Field) {
				fields.add((Field)curFOM);
			}
		}
		
		return (fields);
	}
	public static List<Method> getMethods(List<FieldOrMethod> list) {
		
		List<Method> methods = new LinkedList<Method>();
		
		for (FieldOrMethod curFOM : list) {
			if (curFOM instanceof Method) {
				methods.add((Method)curFOM);
			}
		}
		
		return (methods);
	}
}
