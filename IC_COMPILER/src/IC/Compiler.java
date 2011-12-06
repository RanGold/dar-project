package IC;
import java.io.*;
import IC.Parser.*;
import java_cup.runtime.Symbol;

public class Compiler {
  public static void main(String[] args) {
    Symbol currToken;
    try {
        FileReader txtFile = new FileReader(args[0]);
        Lexer scanner = new Lexer(txtFile);
        parser p = new parser(scanner);
        do {
            currToken = scanner.next_token();
            System.out.println(currToken);
        } while (currToken.sym != sym.EOF);
    
    } catch (Exception e) {
        System.out.println(e.toString());
    }
  }
}
