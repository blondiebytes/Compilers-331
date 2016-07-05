package symbolTable;

import errors.SymbolTableError;
import org.junit.*;

import java.util.function.Function;

import static org.junit.Assert.*;

public class SymbolTableTest
{

	private SymbolTable table;

	// conforming to lexer
	protected String normalize(String input) {
		return input.toUpperCase();
	}

	@Before
	public void setup() throws SymbolTableError {
		table = new SymbolTable();
		SymbolTable.installBuiltins(table);
	}

	@After
	public void teardown() {
		table = null;
	}

	@Ignore
	@Test
	public void testInsert() throws SymbolTableError
	{
		System.out.println("SymbolTableTest.testInsert");
		SymbolTableEntry expected = new SymbolTableEntry("foo");
		table.insert(expected);
		SymbolTableEntry actual = (SymbolTableEntry) table.lookup("foo");
		assertEquals(expected, actual);
		assertEquals(6, table.size());
	}

	@Test(expected = SymbolTableError.class)
	public void testDupicates() throws SymbolTableError
	{
		System.out.println("SymbolTableTest.testDupicates");
		SymbolTableEntry entry = new SymbolTableEntry("foo");
		table.insert(entry);
		table.insert(entry);
	}

	@Test
	public void testUpperCaseNames() {
		System.out.println("SymbolTableTest.testUpperCaseNames");
		validate("WRITE");
		validate("READ");
		validate("MAIN");
	}

	@Test
	public void testLowerCaseNames() {
		System.out.println("SymbolTableTest.testLowerCaseNames");
		validate("write");
		validate("read");
		validate("main");
	}

	@Ignore
	@Test
	public void testIO() {
		System.out.println("SymbolTableTest.testIO");
		validateIO("input");
		validateIO("output");
		validateIO("InPut");
		validateIO("OUTPUT");
	}

	protected void validateIO(String name) {
		SymbolTableEntry entry = table.lookup(normalize(name));
		assertNotNull(entry);
		assertTrue(entry.isReserved());
		assertFalse(entry.isProcedure());
		assertFalse(entry.isArray());
		assertFalse(entry.isConstant());
		assertFalse(entry.isFunction());
		assertFalse(entry.isFunctionResult());
		assertFalse(entry.isKeyword());
		assertFalse(entry.isParameter());
		//assertFalse(entry.isVariable());
	}

	protected void validate(String name) {
		assertNotNull(name);
		SymbolTableEntry entry = table.lookup(normalize(name));
		assertNotNull(entry);
		assertTrue(entry.isReserved());
		assertTrue(entry.isProcedure());
		assertFalse(entry.isArray());
		assertFalse(entry.isConstant());
		assertFalse(entry.isFunction());
		assertFalse(entry.isFunctionResult());
		assertFalse(entry.isKeyword());
		assertFalse(entry.isParameter());
		assertFalse(entry.isVariable());
	}

}
