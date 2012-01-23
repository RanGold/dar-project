package lir;

public class RegisterPool {

	private static int counter = 0;
	
	
	public static String getRegister(){
		counter++;
		return "R" + counter;
	}
	
	

}
