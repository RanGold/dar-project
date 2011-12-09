package IC;
import java.io.*;

import IC.AST.*;
import IC.Parser.*;
import java_cup.runtime.Symbol;


//notice: our directory structure as of the moment is incorrect!!
public class Compiler {
	public static void main(String[] args) {
		boolean parse_libic = false, print_ast = false;
		String pathTOlibic="";
		if (args.length == 0) {
			System.err.println("Missing ic file name, expected: java IC.Compiler <file.ic> [ -L</path/to/libic.sig> ] [ -print-ast ]");
			return;
		}
		if (args.length > 3) {
			System.err.println("Too many arguments to command line, expected: java IC.Compiler <file.ic> [ -L</path/to/libic.sig> ] [ -print-ast ]");
			return;
		}
		
		//args[0] should always contain the path to the file.IC
		
		for (int i=1;i<args.length;i++){
			if (args[i].startsWith("-L")){
				if (parse_libic){
					System.err.println("can't use the -L flag more than once");
					return;
				}
				if (args.length==2){
					System.err.println("-L flag should be followed with the libic file's path (no space)");
					return;
				}
				parse_libic=true;
				pathTOlibic=args[i].substring(2);
			}
			else if (args[i].equals("-print-ast")){
				if (print_ast){
					System.err.println("can't use the -print-ast flag more than once");
					return;
				}
				print_ast=true;
			}
			else{
				System.err.println("Illegal input, expected: java IC.Compiler <file.ic> [ -L</path/to/libic.sig> ] [ -print-ast ]");
				return;
			}
		}
		
		Program ICRoot;
		//parse the ic file
		try {
			FileReader txtFile = new FileReader(args[0]);
			Lexer scanner = new Lexer(txtFile);
			Parser parser = new Parser(scanner);
			Symbol symbol = parser.parse();
			ICRoot = (Program) symbol.value;
		} catch (Exception e) {
			System.err.println(e);
			return;
		}
		System.out.println("IC file parsed successfully");
		
		ICClass LibicRoot;
		//if specified, parse the libic file
		if (parse_libic) {
			try {
				FileReader txtFile = new FileReader(pathTOlibic);
				Lexer scanner = new Lexer(txtFile);
				LibraryParser parser = new LibraryParser(scanner);
				Symbol symbol = parser.parse();
				LibicRoot = (ICClass) symbol.value;
				ICRoot.AddNewClass(LibicRoot);
			} catch (Exception e) {
				System.err.println(e);
				return;
			}
			System.out.println("libic file parsed successfully");
		}
		
		if (print_ast){
			PrettyPrinter printer = new PrettyPrinter(args[0]);
            System.out.println(ICRoot.accept(printer)); 
		}
	}
}
