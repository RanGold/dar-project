package IC;
import java.io.*;

import IC.AST.*;
import IC.Parser.*;
import java_cup.runtime.Symbol;


//notice: our directory structure as of the moment is incorrect!!
public class Compiler {
  public static void main(String[] args) {
    Symbol currToken;
    try {
        FileReader txtFile = new FileReader(args[0]);
        Lexer scanner = new Lexer(txtFile);
        Parser p = new Parser(scanner);
        currToken = p.parse();
        Program program = (Program)currToken.value;
        PrettyPrinter printer = new PrettyPrinter(args[0]);
        System.out.println(program.accept(printer));
        
    } catch (Exception e) {
        System.err.println(e);
        return;
    }
  }
}
