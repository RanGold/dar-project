package IC;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import lir.TranslationVisitor;
import IC.AST.ICClass;
import IC.AST.PrettyPrinter;
import IC.AST.Program;
import IC.AST.Visitor;
import IC.Parser.Lexer;
import IC.Parser.LibraryParser;
import IC.Parser.Parser;
import IC.SemanticChecks.TypeCheckVisitor;
import IC.SemanticChecks.VarInitVisitor;
import IC.SemanticChecks.returnVisitor;
import IC.SymbolTables.SymbolTableBuilder;
import IC.SymbolTables.SymbolTablePrint;
import IC.Types.TypeTable;
import IC.Types.TypeTableBuilderVisitor;

public class Compiler {

	public static void main(String[] args) {
		boolean parse_libic = false, print_ast = false, seen_ICpath = false, dump_symtab = false, print_lir = false;
		String pathTOlibic = "libic.sig", pathToIC = null;

		if ((args.length == 0) || (args.length > 5)) {
			System.err.println("Usage: java IC.Compiler <file.ic> [-L</path/to/libic.sig>] [-print-ast] [-dump-symtab] [-print-lir]");
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
			} else if (args[i].equals("-dump-symtab")) {
				if (dump_symtab) {
					System.err.println("can't use the -dump-symtab flag more than once");
					return;
				}
				dump_symtab = true;
			} else if (args[i].equals("-print-lir")) {
				if (print_lir) {
					System.err.println("can't use the -print-lir flag more than once");
					return;
				}
				print_lir = true;
			} else {
				if (seen_ICpath) {
					System.err.println("Usage: java IC.Compiler <file.ic> [-L</path/to/libic.sig>] [-print-ast] [-dump-symtab] [-print-lir]");
					return;
				}
				seen_ICpath = true;
				pathToIC = args[i];
			}
		}

		if (!seen_ICpath) {
			System.err.println("Missing path to IC file");
			return;
		}
		
		FileReader txtFile = null;
		try {
			txtFile = new FileReader(pathTOlibic);
		} catch (FileNotFoundException e1) {
			System.err.println(e1.getMessage());
			return;
		}
		
		// Parse the IC file
		Program ICRoot = null;
		try {
			FileReader txtFile2 = new FileReader(pathToIC);
			Lexer scanner = new Lexer(txtFile2);
			Parser parser = new Parser(scanner);
			ICRoot = (Program) parser.parse().value;
			System.out.println("Parsed " + pathToIC + " successfully!");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return;
		}

		// If specified, parse the libic file
		try {
			//FileReader txtFile = new FileReader(pathTOlibic);
			Lexer scanner = new Lexer(txtFile);
			LibraryParser parser = new LibraryParser(scanner);
			ICClass LibicRoot = (ICClass) parser.parse().value;
			ICRoot.getClasses().add(0, LibicRoot);
			System.out.println("Parsed " + pathTOlibic + " successfully!");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return;
		}
		
		try {
			TypeTableBuilderVisitor t = new TypeTableBuilderVisitor();
			t.visit(ICRoot);
			
			Visitor s = new SymbolTableBuilder(pathToIC);
			s.visit(ICRoot);

			TypeCheckVisitor tc = new TypeCheckVisitor();
			ICRoot.accept(tc);
			
			returnVisitor rv = new returnVisitor();
			ICRoot.accept(rv);
			
			VarInitVisitor vi = new VarInitVisitor();
			ICRoot.accept(vi);
			
		} catch (RuntimeException exp) {
			System.err.println(exp.getMessage());
			return;
		}
		
		if (print_ast) {
			/* true - prints the tree with tabs
			 * false - prints the tree without tabs */
			PrettyPrinter printer = new PrettyPrinter(pathToIC, true);
			System.out.println(ICRoot.accept(printer)+"\n");//print the AST
		}
		
		if (dump_symtab) {	
			SymbolTablePrint pr = new SymbolTablePrint(ICRoot);
			pr.printSymbolTable();//print the Symbol Table
			
			System.out.println(TypeTable.getString(pathToIC));//print the Type Table
		}
		
		if (print_lir) {
			try{
				int beginIndex = pathToIC.lastIndexOf("\\");
				beginIndex = beginIndex == -1 ? pathToIC.lastIndexOf("/") : beginIndex;
				String pathToLir = pathToIC.substring(beginIndex + 1, pathToIC.lastIndexOf("."));
				pathToLir = pathToLir + ".lir";
				
				// Create file 
				FileWriter fstream = new FileWriter(pathToLir);
				BufferedWriter out = new BufferedWriter(fstream);
				
				Visitor v = new TranslationVisitor();
				String lirOutput = (String)ICRoot.accept(v);
				out.write(lirOutput);
				
				// TODO : delete this
				System.out.println(lirOutput);
				
				// Close the output stream
				out.close();
			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
			}
		}
	}
}
