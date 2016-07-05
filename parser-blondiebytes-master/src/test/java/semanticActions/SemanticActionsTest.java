package semanticActions;

import errors.CompilerError;
import errors.LexicalError;
import errors.SemanticError;
import errors.SyntaxError;
import errors.SymbolTableError;
import lex.TokenType;
import org.junit.After;
import org.junit.Test;
import parser.Parser;
import symbolTable.ArrayEntry;
import symbolTable.ProcedureEntry;
import symbolTable.SymbolTableEntry;
import symbolTable.VariableEntry;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class SemanticActionsTest
{
	private static final File RESOURCE = new File("src/test/resources/phase1");
	private static final File RESOURCE2 = new File("src/test/resources");
	private static final File RESOURCE3 = new File("src/test/resources/phase2");

	protected Parser parser;

	@After
	public void cleanup()
	{
		parser = null;
	}

	protected void init(String filename) throws IOException, LexicalError, SyntaxError, SymbolTableError, SemanticError
	{
		init(new File(RESOURCE, filename));
	}

	protected void init2(String filename) throws IOException, LexicalError, SyntaxError, SymbolTableError, SemanticError
	{
		init(new File(RESOURCE2, filename));
	}

	protected void init3(String filename) throws IOException, LexicalError, SyntaxError, SymbolTableError, SemanticError
	{
		init(new File(RESOURCE3, filename));
	}

	protected void init(File file) throws IOException, LexicalError, SemanticError, SymbolTableError, SyntaxError
	{
		assertTrue("File not found:" + file.getPath(), file.exists());
		parser = new Parser(file);
		parser.parse();
	}

	@Test
	public void testBuiltIns() throws LexicalError, SemanticError, SymbolTableError, SyntaxError, IOException
	{
		System.out.println("SemanticActionsTest.testBuiltIns");
		init("builtins.pas");
		checkBuiltIn("main");
		checkBuiltIn("read");
		checkBuiltIn("write");
		checkIO("input");
		checkIO("output");
	}

	private void checkIO(String name) throws SemanticError
	{
		SymbolTableEntry entry = parser.lookup("input");
		assertTrue(name + " is not a varible", entry.isVariable());
		assertTrue(name + " is not reserved", entry.isReserved());
		checkIsA(entry, 2);
	}

	@Test
	public void testCaseSensitive() throws LexicalError, SemanticError, SymbolTableError, SyntaxError, IOException
	{
		System.out.println("SemanticActionsTest.testCaseSensitive");
		init("variable_test.pas");
		SymbolTableEntry a = parser.lookup("a");
		assertNotNull(a);
		SymbolTableEntry A = parser.lookup("A");
		assertNotNull(A);
		assertTrue("Lookup returned different objects.", a == A);
	}

	@Test
	public void variableTest() throws IOException, LexicalError, SymbolTableError, SemanticError, SyntaxError
	{
		System.out.println("SemanticActionsTest.variableTest");
		init("variable_test.pas");
		checkVariable("a", TokenType.INTEGER, 1);
		checkVariable("b", TokenType.INTEGER, 0);
		checkVariable("c", TokenType.REAL, 2);
	}

	@Test
	public void arrayDeclTest() throws IOException, LexicalError, SymbolTableError, SemanticError, SyntaxError
	{
		System.out.println("SemanticActionsTest.arrayDeclTest");
		init("array_decl_test.pas");
		checkArrayEntry("x", TokenType.REAL, 0, 1, 5);
		checkArrayEntry("y", TokenType.INTEGER, 5, 15, 100);
	}

	@Test
	public void bigTest() throws LexicalError, SemanticError, SymbolTableError, SyntaxError, IOException
	{
		System.out.println("SemanticActionsTest.bigTest");
		init("big_test.pas");
		checkVariable("a", TokenType.INTEGER, 1);
		checkVariable("b", TokenType.INTEGER, 0);
		checkArrayEntry("x", TokenType.REAL, 2, 1, 5);
		checkArrayEntry("y", TokenType.INTEGER, 7, 15, 100);
		checkVariable("c", TokenType.REAL, 93);
		checkVariable("d", TokenType.INTEGER, 94);
	}

	@Test
	public void duplicateVariable() throws LexicalError, SyntaxError, SymbolTableError, IOException
	{
		System.out.println("SemanticActionsTest.duplicateVariable");
		expectException("duplicate_variable.pas", CompilerError.Type.MULTI_DECL);
	}

	@Test
	public void reservedNameAsVariable() throws LexicalError, SymbolTableError, IOException, SyntaxError
	{
		System.out.println("SemanticActionsTest.reservedNameAsVariable");
		expectException("reserved_word.pas", CompilerError.Type.RESERVED_NAME);
	}

	@Test
	public void keywordAsVariable() throws LexicalError, SymbolTableError, IOException, SyntaxError
	{
		System.out.println("SemanticActionsTest.keywordAsVariable");
		expectException("keyword.pas", CompilerError.Type.RESERVED_NAME);
	}

	// Phase 2 tests

	@Test
	public void phase2a() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.phase2a");
		init3("phase2a.pas");
	}

	@Test
	public void phase2aa() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.phase2aa");
		init3("phase2aa.pas");
	}

	@Test
	public void phase2b() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.phase2b");
		expectException3("phase2bb.pas", CompilerError.Type.ILLEGAL_TYPES);
	}

	@Test
	public void phase2bb() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.phase2bb");
		expectException3("phase2bb.pas", CompilerError.Type.ILLEGAL_TYPES);
	}

	@Test
	public void phase2c() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.phase2c");
		init3("phase2c.pas");
	}

	@Test
	public void phase2cc() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.phase2cc");
		init3("phase2cc.pas");
	}

	@Test
	public void phase2d() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.phase2d");
		expectException3("phase2d.pas", CompilerError.Type.UN_DECL);
	}

	@Test
	public void phase2dd() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.phase2dd");
		expectException3("phase2dd.pas", CompilerError.Type.UN_DECL);
	}

	@Test
	public void phase2e() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.phase2e");
		expectException3("phase2e.pas", CompilerError.Type.UN_DECL);
	}

	@Test
	public void phase2ee() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.phase2ee");
		expectException3("phase2e.pas", CompilerError.Type.UN_DECL);
	}

	@Test
	public void phase2f() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.phase2f");
		init3("phase2f.pas");
	}

	@Test
	public void phase2g() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.phase2g");
		expectException3("phase2g.pas", CompilerError.Type.BAD_ARRAY_BOUNDS);
	}

	// ALL tests

	@Test
	public void array() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.array");
		init2("array.pas");
	}

	@Test
	public void array2() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.array2");
		init2("array2.pas");
	}

	@Test
	public void arrayRef() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.arrayref");
		init2("arrayref.pas");
	}

	@Test
	public void bool() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.bool");
		init2("bool.pas");
	}

	@Test
	public void expression() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.expression");
		init2("expression.pas");
	}

	@Test
	public void expTest() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.exptest");
		init2("exptest.pas");
	}

	@Test
	public void fib() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.fib");
		init2("fib.pas");
	}

	@Test
	public void func() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.func");
		init2("func.pas");
	}

	@Test
	public void ifTest() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.if");
		init2("if.pas");
	}

	@Test
	public void mod() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.mod");
		init2("mod.pas");
	}

	@Test
	public void one() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.one");
		init2("one.pas");
	}

	@Test
	public void or() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.or");
		init2("or.pas");
	}

	@Test
	public void parsetest() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.parsetest");
		init2("parsetest.dat");
	}

	@Test
	public void proc() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.proc");
		init2("proc.pas");
	}

	@Test
	public void recursion() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.recursion");
		init2("recursion.pas");
	}

	@Test
	public void rel() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.rel");
		init2("rel.pas");
	}

	@Test
	public void simple() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.simple");
		init2("simple.pas");
	}

	@Test
	public void test1() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.test1");
		init2("test1.pas");
	}

	@Test
	public void test2() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.test2");
		init2("test2.pas");
	}

	@Test
	public void test3() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.test3");
		init2("test3.pas");
	}

	@Test
	public void test4() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.test4");
		init2("test4.pas");
	}

	@Test
	public void test5() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.test5");
		init2("test5.pas");
	}

	@Test
	public void test7() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.test7");
		init2("test7.pas");
	}

	@Test
	public void testingScope() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.testingScope");
		init2("testingScope.pas");
	}

	@Test
	public void ult() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.ult");
		init2("ult.pas");
	}

	@Test
	public void unaryminus() throws LexicalError, SymbolTableError, SyntaxError, SemanticError, IOException {
		System.out.println("SemanticActionsTest.unaryminus");
		init2("unaryminus.pas");
	}

	private void expectException(String path, CompilerError.Type expected) throws LexicalError, IOException, SymbolTableError, SyntaxError
	{
		try
		{
			init(path);
			fail("Expected exception not thrown: " + expected);
		}
		catch (SemanticError e)
		{
			assertEquals("Wrong exception type thrown", expected, e.getType());
		}
	}

	private void expectException3(String path, CompilerError.Type expected) throws LexicalError, IOException, SymbolTableError, SyntaxError
	{
		try
		{
			init3(path);
			fail("Expected exception not thrown: " + expected);
		}
		catch (SemanticError e)
		{
			assertEquals("Wrong exception type thrown", expected, e.getType());
		}
	}


	private void checkBuiltIn(String name) throws SemanticError
	{
		SymbolTableEntry entry = parser.lookup(name);
		assertNotNull(entry);
		assertEquals("Wrong entry returned", name.toUpperCase(), entry.getName());
		assertTrue(name + " is not a prodedure", entry.isProcedure());
		assertTrue(name + " is not a reserved", entry.isReserved());
		checkIsA(entry, 2);

		assertTrue("Not a ProcedureEntry", entry instanceof ProcedureEntry);
	}

	private void checkArrayEntry(String name, TokenType type, int address, int lower, int upper) throws SemanticError
	{
		SymbolTableEntry entry = parser.lookup(name);
		assertNotNull(entry);
		assertEquals("Wrong entry returned", name.toUpperCase(), entry.getName());
		assertTrue("Entry is not an array.", entry.isArray());

		checkIsA(entry);
		ArrayEntry array = (ArrayEntry) entry;
		assertEquals("Wrong address assigned.", address, array.getAddress());
		checkType(type, entry.getType());
		assertEquals("Lower bound is wrong", lower, array.getLBound());
		assertEquals("Upper bound is wrong", upper, array.getUBound());
	}

	private void checkVariable(String name, TokenType type, int address) throws SemanticError
	{
		SymbolTableEntry entry = parser.lookup(name);
		assertNotNull(entry);
		assertEquals("Wrong entry returned", name.toUpperCase(), entry.getName());
		assertTrue(entry.isVariable());
		checkIsA(entry);
		VariableEntry ve = (VariableEntry) entry;
		assertEquals("Wrong address assigned.", address, ve.getAddress());
		checkType(type, entry.getType());
	}

	private void print(SymbolTableEntry e)
	{
		System.out.println(e.getName() + ": " + e.getType());
	}

	private void checkType(TokenType expected, TokenType actual)
	{
		assertNotNull(actual);
		String message = String.format("Invalid type. Expected: %s Actual: %s", expected, actual);
		assertEquals(message, expected, actual);
	}

	private void checkIsA(SymbolTableEntry e)
	{
		checkIsA(e, 1);
	}

	private void checkIsA(SymbolTableEntry e, int expected)
	{
		int count = 0;

		if (e.isArray()) ++count;
		if (e.isConstant()) ++count;
		if (e.isFunction()) ++count;
		if (e.isFunctionResult()) ++count;
		if (e.isKeyword()) ++count;
		if (e.isParameter()) ++count;
		if (e.isProcedure()) ++count;
		if (e.isReserved()) ++count;
		if (e.isVariable()) ++count;

		assertEquals("Invalid symbol table entry for " + e.getName(), expected, count);
	}


}
