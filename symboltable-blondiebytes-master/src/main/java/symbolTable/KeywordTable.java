package symbolTable;

import errors.SymbolTableError;
import lex.TokenType;

import java.util.Arrays;
import java.util.stream.Stream;

public class KeywordTable extends SymbolTable
{
	public KeywordTable()
	{
		super(0);
		Stream.of("PROGRAM", "BEGIN", "END", "VAR", "FUNCTION",
				"PROCEDURE", "RESULT", "INTEGER", "REAL", "ARRAY",
				"OF", "IF", "THEN", "ELSE", "WHILE", "DO", "NOT")
				.map((kw) -> new KeywordEntry(kw, TokenType.valueOf(kw)))
				.forEach(this::insertQuietly);
		insertQuietly(new KeywordEntry("AND", TokenType.MULOP));
		insertQuietly(new KeywordEntry("OR", TokenType.ADDOP));
		insertQuietly(new KeywordEntry("DIV", TokenType.MULOP));
		insertQuietly(new KeywordEntry("MOD", TokenType.MULOP));
	}

	private void insertQuietly(KeywordEntry entry){
		try
		{
			this.insert(entry);
		}
		catch (SymbolTableError error) {

		}
	}
}
