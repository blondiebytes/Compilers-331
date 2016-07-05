package parser;
import errors.*;
//import errors.CompilerError;
import java.io.*;
import org.junit.*;
import static org.junit.Assert.*;


@Ignore
public class ParserTest
{

	@Test
	public void testArray() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/array.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testArray2() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/array2.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testArrayref() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/arrayref.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testBool() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/bool.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testExpression() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/expression.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testExptest() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/exptest.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testFib() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/fib.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testFunc() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/func.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testIf() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/if.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testMod() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/mod.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testOne() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/one.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testOr() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/or.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testProc() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/proc.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testRecursion() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/recursion.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testRel() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/rel.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testSimple() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/simple.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testTest1() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/test1.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testTest2() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/test2.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testTest3() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/test3.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testTest4() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/test4.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testTest5() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/test5.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testTest7() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/test7.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testTestingScope() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/testingScope.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testUlt() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/ult.pas");
		parser.parse();
		assertFalse(parser.error());
	}

	@Test
	public void testUnaryminus() throws IOException, SyntaxError, CompilerError {
		Parser parser = new Parser("src/test/resources/unaryminus.pas");
		parser.parse();
		assertFalse(parser.error());
	}

}
