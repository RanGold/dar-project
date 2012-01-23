package IC.AST;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Root AST node for an IC program.
 * 
 * @author Tovi Almozlino
 */
public class Program extends ASTNode {

	private List<ICClass> classes;
	private Map<String, ICClass> nameToClass = new HashMap<String, ICClass>();
	

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new program node.
	 * 
	 * @param classes
	 *            List of all classes declared in the program.
	 */
	public Program(List<ICClass> classes) {
		super(0);
		this.classes = classes;
	}
	
	public List<ICClass> getClasses() {
		return classes;
	}
	
	public void setClassesOffsets(){
		for (ICClass cl : classes){
			nameToClass.put(cl.getName(), cl);
		}
		for (ICClass cl : classes){
			cl.initOffsets(cl.hasSuperClass()? nameToClass.get(cl.getSuperClassName()) : null);
		}
		
	}
}
