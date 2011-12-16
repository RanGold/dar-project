package IC;

import java.io.FileReader;

import IC.AST.ICClass;
import IC.AST.PrettyPrinter;
import IC.AST.Program;
import IC.Parser.Lexer;
import IC.Parser.LibraryParser;
import IC.Parser.Parser;

// TODO : fix order of input parameters can vary
public class Compiler {

	public static void main(String[] args) {
		boolean parse_libic = false, print_ast = false, seen_ICpath = false;
		String pathTOlibic = "", pathTOic = "";

		if ((args.length == 0) || (args.length > 3)) {
			System.err.println("Usage: java IC.Compiler <file.ic> [-L</path/to/libic.sig>] [-print-ast]");
			return;
		}

		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-L")) {
				if (parse_libic) {
					System.err.println("can't use the -L flag more than once");
					return;
				} else if (!seen_ICpath) {
					System.err.println("Path to IC file must appear before the -L flag");
					return;
				}
				if (args[i].length() == 2) {
					System.err.println("-L flag should be followed by the libic file's path (no space)");
					return;
				}
				parse_libic = true;
				pathTOlibic = args[i].substring(2);
			} else if (args[i].equals("-print-ast")) {
				if (print_ast) {
					System.err.println("can't use the -print-ast flag more than once");
					return;
				}
				print_ast = true;
			} else {
				if (seen_ICpath) {
					System.err.println("Usage: java IC.Compiler <file.ic> [-L</path/to/libic.sig>] [-print-ast]");
					return;
				}
				seen_ICpath = true;
				pathTOic = args[i];
			}
		}

		if (!seen_ICpath) {
			System.err.println("Missing path to IC file");
			return;
		}

		// Parse the IC file
		Program ICRoot = null;
		try {
			FileReader txtFile = new FileReader(pathTOic);
			Lexer scanner = new Lexer(txtFile);
			Parser parser = new Parser(scanner);
			ICRoot = (Program) parser.parse().value;
			System.out.println("Parsed " + pathTOic + " successfully!");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return;
		}

		// If specified, parse the libic file
		if (parse_libic) {
			try {
				FileReader txtFile = new FileReader(pathTOlibic);
				Lexer scanner = new Lexer(txtFile);
				LibraryParser parser = new LibraryParser(scanner);
				ICClass LibicRoot = (ICClass) parser.parse().value;
				// TODO : Ask Guy Golan if thsi add is ok
				ICRoot.getClasses().add(LibicRoot);
				System.out.println("Parsed " + pathTOlibic + " successfully!");
			} catch (Exception e) {
				System.err.println(e.getMessage());
				return;
			}
		}
		
		if (print_ast) {
			PrettyPrinter printer = new PrettyPrinter(pathTOic);
			System.out.println(ICRoot.accept(printer));
		}
	}
}
