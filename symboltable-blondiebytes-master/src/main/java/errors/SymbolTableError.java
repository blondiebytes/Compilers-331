package errors;

import lex.TokenType;

public class SymbolTableError extends CompilerError
{
	public SymbolTableError(CompilerError.Type errorNumber, String message)
	{
		super(errorNumber, message);
	}

	// Factory methods to generate the lexical exception types.

	public static SymbolTableError DuplicateEntry(String id)
	{
		return new SymbolTableError(CompilerError.Type.MULTI_DECL,
				">>> ERROR: multiple declaration of " + id);
	}




}
