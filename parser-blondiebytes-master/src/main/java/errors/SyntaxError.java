/*
 * Copyright 2016 Vassar College
 * All rights reserverd.
 */

package errors;

import lex.TokenType;

public class SyntaxError extends CompilerError
{
	   public SyntaxError(Type errorNumber, String message)
	   {
	      super(errorNumber, message);
	   }

	   // Factory methods to generate the lexical exception types.

	   public static SyntaxError BadToken(TokenType t, int line)
	   {
	      return new SyntaxError(Type.BAD_TOKEN,
	                              ">>> ERROR on line " + line + " : unexpected " + t);
	   }

	public static SyntaxError BadToken(TokenType t, int line, String value)
	{
		return new SyntaxError(Type.BAD_TOKEN,
				">>> ERROR on line " + line + " : unexpected " + t + " [" + value + "]");
	}

	public static SyntaxError BadToken(TokenType currentToken, TokenType predictedToken, int line)
	{
		return new SyntaxError(Type.BAD_TOKEN,
				">>> ERROR on line " + line + " : expecting " + predictedToken + " , " + currentToken + " found");
	}

}
