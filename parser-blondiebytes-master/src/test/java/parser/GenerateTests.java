package parser;

import org.junit.Ignore;

import java.io.File;
import java.io.FileFilter;

/**
 * This is not a test, but can be used to generate the ParserTest.java file. Simply run
 * this class and copy/paste the output into the ParserTest.java file.
 */
@Ignore
public class GenerateTests
{
	public void write(File test) {
		String testName = test.getName().replace(".pas", "");
		String first = testName.substring(0, 1).toUpperCase();
		String tail = testName.substring(1);
		testName = "test" + first + tail;
		System.out.println("\t@Test");
		System.out.println("\tpublic void " + testName + "() throws IOException, SyntaxError, LexicalError {");
		System.out.println("\t\tParser parser = new Parser(\"" + test.getPath() + "\");");
		System.out.println("\t\tparser.parse();");
		System.out.println("\t\tassertFalse(parser.error());");
		System.out.println("\t}");
		System.out.println();;

	}

	public void generate() {
		File directory = new File("src/test/resources");
		FileFilter filter = f -> f.getName().endsWith(".pas");
		File[] files = directory.listFiles(filter);
		System.out.println("package parser;");
		System.out.println("import errors.SyntaxError;");
		System.out.println("import errors.LexicalError;");
		System.out.println("import java.io.*;");
		System.out.println("import org.junit.*;");
		System.out.println("import static org.junit.Assert.*;");
		System.out.println();
		System.out.println("public class ParserTest {");
		for (File file : files) {
			write(file);
		}
		System.out.println("}");
	}

	public static void main(String[] args) {
		new GenerateTests().generate();
	}
}
