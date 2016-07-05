package symbolTable;

import lex.TokenType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;


public class KeyworkTableTest
{
	private KeywordTable table;

	public KeyworkTableTest()
	{

	}

	@Before
	public void setup() {
		table = new KeywordTable();
	}

	@After
	public void teardown() {
		table = null;
	}

	@Test
	public void getType() {
		KeywordEntry entry = (KeywordEntry) table.lookup("AND");
		assertNotNull(entry);
		assertNotNull(entry.getType());
		assertNotNull(entry.getKey());
		System.out.println(entry);
	}

	@Test
	public void testAllKeywords() {
		List<String> keywords = Stream.of("PROGRAM", "BEGIN", "END", "VAR", "FUNCTION",
				"PROCEDURE", "RESULT", "INTEGER", "REAL", "ARRAY",
				"OF", "IF", "THEN", "ELSE", "WHILE", "DO", "NOT")
				.collect(Collectors.toList());

		for (String keyword: keywords) {
			expect(keyword, TokenType.valueOf(keyword));
		}
		expect("AND", TokenType.MULOP);
		expect("OR", TokenType.ADDOP);
		expect("DIV", TokenType.MULOP);
		expect("MOD", TokenType.MULOP);
	}

	private void expect(String keyword, TokenType type) {
		SymbolTableEntry ste = table.lookup(keyword);
		assertNotNull(ste);
		assertTrue(ste instanceof KeywordEntry);
		KeywordEntry entry = (KeywordEntry) ste;
		assertNotNull(type);
		assertEquals("Name mismatch for " + keyword, keyword, entry.getName());
		assertEquals("Type mismatch for " + keyword, type, entry.getType());
		assertEquals("Keyword mismatch for " + keyword, type, entry.getKey());
	}
}
