package lex;

import errors.LexicalError;
import symbolTable.KeywordTable;
import symbolTable.SymbolTable;
import symbolTable.SymbolTableEntry;

import java.io.File;
import java.io.IOException;
import java.net.URL;


public class Tokenizer
{
	///* MARKED March 4

	private CharStream stream = null;

	/** The KeywordTable is a SymbolTable that comes with all of the KeywordEntries
	 *  already inserted.
	 */
	private KeywordTable keywordTable;
	private SymbolTable table;
	private Token lastToken;
	private int maxIdentifierLength;
	private Token t = new Token();

	public Tokenizer(String filename) throws IOException, LexicalError
	{
		super();
		init(new CharStream(filename));
	}

	/** Used during testing to read files from the classpath. */
	public Tokenizer(URL url) throws IOException, LexicalError
	{
		super();
		init(new CharStream(url));
	}

	public Tokenizer(File file) throws IOException, LexicalError
	{
		super();
		init(new CharStream(file));
	}

	protected void init(CharStream stream)
	{
		this.stream = stream;
		keywordTable = new KeywordTable();
		maxIdentifierLength = 64;
		setLastToken(new Token());
	}

	public int getLineNumber() {
		return stream.lineNumber();
	}


	public Token getLastToken() {
		return lastToken;
	}

	public void setLastToken(Token lastToken) {
		this.lastToken = lastToken;
	}

	// ---------------
	// GET NEXT TOKEN
	// ---------------

	public Token getNextToken() throws LexicalError {
		lastToken = new Token();
		lastToken.setType(t.getType());
		lastToken.setValue(t.getValue());

		t = new Token();
		//blank (which represents anything including a blank, end-of-line, or a comment) y
		while (true) {
			char ch = stream.currentChar();
			if (ch == ' ' || ch == '\n') {
				ch = stream.currentChar();
			}
			// Is the next char a digit?
			if (Character.isDigit(ch)) {
				t = sawDigit(ch);
				// Is the next char a letter?
			} else if (Character.isLetter(ch)) {
				t = sawLetter(ch);
				//  Maybe could go in another instead of complicating the main method?
			} else if (didWeSeeRelopOrAssignment(ch)) {
				t = sawRelopOrAssignment(ch);
			} else if (didWeSeeMulop(ch)) {
				t = sawMulop(ch);
			} else if (didWeSeeAddop(ch)) {
				t = sawAddop(ch);
			} else {
				t = sawOtherCharacter(ch);
			}
			return t;
		}
	}

	public Token sawOtherCharacter(Character ch) throws LexicalError {
		switch (ch) {
			case '(':
				t.setType(TokenType.LEFTPAREN);
				break;
			case ')':
				t.setType(TokenType.RIGHTPAREN);
				break;
			case '[':
				t.setType(TokenType.LEFTBRACKET);
				break;
			case ']':
				t.setType(TokenType.RIGHTBRACKET);
				break;
			case ';':
				t.setType(TokenType.SEMICOLON);
				break;
			case ':':
				char l = stream.currentChar();
				if (l == '=') {
					t.setType(TokenType.ASSIGNOP);
				} else {
					t.setType(TokenType.COLON);
					stream.pushBack(l);
				}
				break;
			case ',':
				t.setType(TokenType.COMMA);
				break;
			case '.':
				char lookahead = stream.currentChar();
				// if the next thing is also a dot, then we got doubledot
				if (lookahead == '.') {
					t.setType(TokenType.DOUBLEDOT);
				} else {
					// if the next thing isn't a dot, then we have an end marker
					stream.pushBack(lookahead);
					t.setType(TokenType.ENDMARKER);
				}
				break;
			case CharStream.EOF:
				t.setType(TokenType.ENDOFFILE);
				break;
			default:
				// If it isn't valid... throw an error
				if (!stream.valid(ch)) {
					throw LexicalError.IllegalCharacter(ch, stream.lineNumber());
				}
		}
		return t;
	}


	// -------
	// DIGIT
	// -------

	// If we saw a digit first, it's a number
	// continue looking until we get to the end of the numbers
	// OPTIONS:
	// INT CONSTANT
	// REAL CONSTANT
	private Token sawDigit(char ch) throws LexicalError{
		StringBuffer num = new StringBuffer();
		while (Character.isDigit(ch)){
			num.append(Character.toString(ch));
			ch = stream.currentChar();
		}
		// 1a. IF WE SEE A DOT --> CHECK IF IT'S A REAL
		if (ch == '.') {
			char lookahead = stream.currentChar();
			//  2a. IF WE SEE A NUMBER NEXT ---> IT'S A REAL
			if (Character.isDigit(lookahead)) {
				// add on the dot
				num.append(Character.toString(ch));
				do {
					// add on lookahead
					num.append(Character.toString(lookahead));
					lookahead = stream.currentChar();
				} while(Character.isDigit(lookahead));
				// 3a. IF WE SEE AN E, THEN WE MIGHT HAVE EXPONENT
				// Lookahead again to make sure that a + - or num follows
				// If it's an exponent, go get the nums associated with the exponent
				char lookahead2 = stream.currentChar();
				if ((Character.toString(lookahead)).toUpperCase().equals("E")) {
					// 4a. CHECKING TO SEE IF LOOKAHEAD IS CORRECT SO IT IS AN EXPONENT
					if (lookahead2 == '+' || lookahead2 == '-' || Character.isDigit(lookahead2)) {
						char lookahead3 = stream.currentChar();
						if (Character.isDigit(lookahead3)) {
							// REAL NUMBER THAT HAS AN EXPONENT
							num.append(Character.toString(lookahead));
							num.append(Character.toString(lookahead2));
							num.append(Character.toString(lookahead3));
							lookahead3 = stream.currentChar();
							while (Character.isDigit(lookahead3)) {
								num.append(Character.toString(lookahead3));
								lookahead3 = stream.currentChar();
							}
							stream.pushBack(lookahead3);
							t.setType(TokenType.REALCONSTANT);
							t.setValue(num.toString());
						} else {
							stream.pushBack(lookahead3);
							stream.pushBack(lookahead2);
							stream.pushBack(lookahead);
							t.setType(TokenType.REALCONSTANT);
							t.setValue(num.toString());
						}
					} else {
						// 4b. NEXT CHAR ISN'T RIGHT SO E is part of something else so we are done.
						stream.pushBack(lookahead2);
						stream.pushBack(lookahead);
						t.setType(TokenType.REALCONSTANT);
						t.setValue(num.toString());
					}
				} else {
					// 3b: The next thing is not an E and it is not a digit so we push it back
					stream.pushBack(lookahead2);
					stream.pushBack(lookahead);
					// create a real token
					t.setType(TokenType.REALCONSTANT);
					t.setValue(num.toString());
				}

			} else {
				// 2b. IF WE DON'T SEE A NUMBER --> DOT IS PART OF SOMETHING ELSE
				// otherwise the . (dot) is part of something else, so let's
				// return what we have right now, our integer
				stream.pushBack(lookahead);
				stream.pushBack(ch);
				t.setType(TokenType.INTCONSTANT);
				t.setValue(num.toString());
			}

		}
		// 1b. NO DOT -->
		else {
			// otherwise we know the next char isn't a digit and it isn't a dot -->
			// we have to check if there is an exponent --> if there is an exponent, then it's a real
			// 2a. MAYBE EXPONENT
			if (((Character.toString(ch)).toUpperCase().equals("E"))) {
				// Make sure nums are associated with the exponent
				char lookahead = stream.currentChar();
				// 3a. CHECK LOOKAHEAD
				if (lookahead == '+' || lookahead == '-' || Character.isDigit(lookahead)) {
					char lookahead2 = stream.currentChar();
					if (Character.isDigit(lookahead2)) {
						// REAL NUMBER THAT HAS AN EXPONENT
						num.append(Character.toString(ch));
						num.append(Character.toString(lookahead));
						num.append(Character.toString(lookahead2));
						lookahead2 = stream.currentChar();
						while (Character.isDigit(lookahead2)) {
							num.append(Character.toString(lookahead2));
							lookahead2 = stream.currentChar();
						}
						stream.pushBack(lookahead2);
						t.setType(TokenType.REALCONSTANT);
						t.setValue(num.toString());
					} else {
						stream.pushBack(lookahead2);
						stream.pushBack(lookahead);
						stream.pushBack(ch);
						t.setType(TokenType.INTCONSTANT);
						t.setValue(num.toString());
					}
				} else {
					// 4b. NEXT CHAR ISN'T RIGHT SO E is part of something else so we are done.
					stream.pushBack(lookahead);
					stream.pushBack(ch);
					t.setType(TokenType.INTCONSTANT);
					t.setValue(num.toString());
				}
			} else {
				// 2b. NO EXPONENT: put ch on the stack because it isn't a digit or a dot or E
				stream.pushBack(ch);
				t.setType(TokenType.INTCONSTANT);
				t.setValue(num.toString());
			}
		}
		return t;
	}

	// -------
	// LETTER
	// -------

	// TWO OPTIONS:
	// 1. is it a keyword?
	// INTEGER
	// REAL
	// 2. is it an identifier?
	private Token sawLetter(char ch) throws LexicalError {
		StringBuffer id = new StringBuffer().append(ch);
		ch = stream.currentChar();
		while (Character.isDigit(ch) || Character.isLetter(ch)) { //we are still getting chars for identifier) {
			// If it's too long, then we throw an error
			if (id.length() >= maxIdentifierLength) {
				throw LexicalError.IdentifierTooLong(id.toString());
			} else { // otherwise keep adding
				id.append(ch);
				ch = stream.currentChar();
			}
		}
		//put back extra char on the stack
		stream.pushBack(ch);
		return makeIdentifier(id.toString());
	}

	// Is it a keyword or not? We will find out!
	private Token makeIdentifier(String id) {
		String upperID = id.toUpperCase();
		SymbolTableEntry s = keywordTable.lookup(upperID);
		if (s != null) {
			switch(id.toUpperCase()) {
				case "AND":
					t.setType(s.getType());
					t.setOpType(Token.OperatorType.AND);
					break;
				case "OR":
					t.setType(s.getType());
					t.setOpType(Token.OperatorType.OR);
					break;
				case "NOT":
					t.setType(s.getType());
					t.setOpType(Token.OperatorType.NOT);
					break;
				case "DIV":
					t.setType(s.getType());
					t.setOpType(Token.OperatorType.INTEGERDIVIDE);
					break;
				case "MOD":
					t.setType(s.getType());
					t.setOpType(Token.OperatorType.MOD);
					break;
				default:
					t.setType(s.getType());
					break;
			}

		} else {
			// make the identifier (it's not a keyword)
			t.setType(TokenType.IDENTIFIER);
			t.setValue(id);
		}
		return t;
	}


	// -------
	// RELOP
	// -------

	private boolean didWeSeeRelopOrAssignment(char ch) {
		// is it one of the beginnings for the relops?
		return ch == '=' || ch == '>' || ch == '<';
	}

	// Deciding relop
	private Token sawRelopOrAssignment(char ch) throws LexicalError {
		char lookahead;
		switch(ch) {
			case '=':
				t.setType(TokenType.RELOP);
				t.setOpType(Token.OperatorType.EQUAL);
				break;
			// lookahead to see if it's >= or just >
			case '>':
				lookahead = stream.currentChar();
				if (lookahead == '=') {
					t.setType(TokenType.RELOP);
					t.setOpType(Token.OperatorType.GREATERTHANOREQUAL);
				} else {
					stream.pushBack(lookahead);
					t.setType(TokenType.RELOP);
					t.setOpType(Token.OperatorType.GREATERTHAN);
				}
				break;
			case '<':
				lookahead = stream.currentChar();
				if (lookahead == '=') {
					t.setType(TokenType.RELOP);
					t.setOpType(Token.OperatorType.LESSTHANOREQUAL);
				} else if (lookahead == '>') {
					t.setType(TokenType.RELOP);
					t.setOpType(Token.OperatorType.NOTEQUAL);
				} else {
					stream.pushBack(lookahead);
					t.setType(TokenType.RELOP);
					t.setOpType(Token.OperatorType.LESSTHAN);
				}
				break;
			default:
				// return null
		//	!	throw LexicalError.IllegalCharacter(ch, getLineNumber());
		}
		return t;
	}

	// -------
	// ADDOP
	// -------

	private boolean didWeSeeAddop(char ch) {
		return '+' == ch || '-' == ch; // OR is covered in identifiers/keywords
	}

	private Token sawAddop(char ch) throws LexicalError {
		TokenType lastTokenType = getLastToken().getType();
		// Checking if this character is a binary operator
		if (lastTokenType == TokenType.RIGHTPAREN ||
				lastTokenType == TokenType.RIGHTBRACKET ||
				lastTokenType == TokenType.IDENTIFIER ||
				lastTokenType == TokenType.INTCONSTANT ||
				lastTokenType == TokenType.REALCONSTANT) {
			if (ch == '+') {
				t.setType(TokenType.ADDOP);
				t.setOpType(Token.OperatorType.ADD);
			} else if (ch == '-') {
				t.setType(TokenType.ADDOP);
				t.setOpType(Token.OperatorType.SUBTRACT);
			} else {
				// should never reach this
				//return null;
				throw LexicalError.IllegalCharacter(ch, getLineNumber());
			}
		} else {
			if (ch == '+') {
				t.setType(TokenType.UNARYPLUS);

			} else if (ch == '-') {
				t.setType(TokenType.UNARYMINUS);
				//}
			} else {
				// should never reach this
				//return null;
				//!throw LexicalError.IllegalCharacter(ch, getLineNumber());
			}
		}
		return t;
	}


	// -------
	// MULOP
	// -------
	private boolean didWeSeeMulop(char ch) {
		return ch == '*' || ch == '/';
	}

	private Token sawMulop(char ch) throws LexicalError{
		if (ch == '*') {
			t.setType(TokenType.MULOP);
			t.setOpType(Token.OperatorType.MULTIPLY);
		} else if (ch == '/') {
			t.setType(TokenType.MULOP);
			t.setOpType(Token.OperatorType.DIVIDE);
		} else {
			// !throw LexicalError.IllegalCharacter(ch, getLineNumber());
			//return null;
		}
		return t;
	}



}
