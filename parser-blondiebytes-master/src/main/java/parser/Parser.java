/*
 * Copyright 2016 Vassar College
 * All rights reserverd.
 */

package parser;


import errors.*;
import grammar.NonTerminal;
import grammar.SemanticAction;
import lex.Token;
import lex.Tokenizer;
import lex.TokenType;
import grammar.GrammarSymbol;
import semanticActions.SemanticActions;
import symbolTable.SymbolTableEntry;

import java.io.File;
import java.io.IOException;
import java.util.Stack;
import java.util.List;
import java.util.LinkedList;


public class Parser
{
	private static final int ERROR = 999;

	private ParseTable parsetable = new ParseTable();
	private RHSTable rhsTable = new RHSTable();
	private Stack<GrammarSymbol> parseStack;
	private GrammarSymbol predicted;
	private Token currentToken;
	private boolean error;
	private Tokenizer tokenizer;
	private SemanticActions semanticActions;
	private List<CompilerError> errors = new LinkedList<CompilerError>();



	public Parser (String filename) throws IOException, LexicalError, SymbolTableError, SemanticError
	{
		this(new File(filename));
	}

	public Parser(File file) throws IOException, LexicalError, SymbolTableError, SemanticError
	{
		tokenizer = new Tokenizer(file);
		semanticActions = new SemanticActions(tokenizer);
	}

	public void parse () throws SyntaxError, LexicalError, SemanticError, SymbolTableError {
		error = false; // there are no error initially
		currentToken = tokenizer.getNextToken(); // get next token
		parseStack = new Stack();// SET Parse Stack TO Empty stack
		parseStack.push(TokenType.ENDOFFILE); // PUSH ENDOFFILE and Start symbol ON Parse stack;
		parseStack.push(NonTerminal.Goal);
		//WHILE Parse Stack not empty:
		while (!parseStack.isEmpty()) {
			predicted = parseStack.pop(); //SET Predicted TO POP(Parse Stack) -> get top item
			// If predicted is a tokentype
			if (predicted.isToken()) {
				// Try to match a move
				if (predicted == currentToken.getType()) {
					currentToken = tokenizer.getNextToken(); // matched
				} else { // not matched and we get an error and go into panic mode
					panicMode();
				}
			} // if predicted is a nonterminal
			else if (predicted.isNonTerminal()) {
				// get entry from parse table
				int entry = parsetable.getEntry(currentToken.getType(), (NonTerminal) predicted);
				// IF [Parse_table [Predicted, Current token]] = ERROR = A 999 entry
				///* FIXED use ERROR instead of 999
				if (entry == ERROR) {
					// then we have an error and go into panic mode
					panicMode();
				} else {
					// PUSH Symbols in RHS [Parse_table [Predicted, Current token]]
					// Push the right hand side symbols
					// if the entry is less than zero, we push nothing
					if (entry > 0) {
						// Get the rule
						GrammarSymbol[] rules = rhsTable.getRule(entry);
						// Push all the symbols
						for (int i = rules.length - 1; i >= 0; i--) {
							parseStack.push(rules[i]);
						}
					}
				}
			} // if predicted is a semantic action
			 else if (predicted.isAction()){
				SemanticAction action = (SemanticAction) predicted;
				// execute the action
				semanticActions.execute(action.getIndex(), tokenizer.getLastToken());
			}

		}
		semanticActions.quadruplesDump();

	}

	// A lookup method for looking things up in the symbol table
	// for debugging and testing purposes.
	public SymbolTableEntry lookup(String name) {
		return semanticActions.lookup(name);
	}

	public boolean error (){
		return errors.size() > 0;
	}

	public List<CompilerError> getErrorList() {
		return errors;
	}

	// prints the current contents of the parse stack.
	private void dump_stack() {
		System.out.println("Contents of parse stack: ");
		Stack<GrammarSymbol> temporaryStack = new Stack<GrammarSymbol>();

		while (!parseStack.isEmpty()) {
			// pop the elements of the parse stack to print them
			GrammarSymbol top = parseStack.pop();
			System.out.print(top + ":");
			// but save them via the temporary stack
			temporaryStack.push(top);
		}
		System.out.println();

		while (!temporaryStack.isEmpty()) {
			// put back the elements onto the parse stack
			parseStack.push(temporaryStack.pop());
		}
	}

	// PANIC MODE RECOVERY:
	// find synchronizing token
	private void panicMode() throws LexicalError, SyntaxError {

		// Add the error we just got to the total list of errors
		SyntaxError error = SyntaxError.BadToken(currentToken.getType(), tokenizer.getLineNumber());
		errors.add(error);

		// Skip tokens until we find the next semi-colon.
		while (currentToken.getType() != TokenType.ENDOFFILE &&
				currentToken.getType() != TokenType.SEMICOLON) {
			currentToken = tokenizer.getNextToken();
		}
		// If we hit EOF --> unrecoverable error
		if (currentToken.getType() == TokenType.ENDOFFILE) {
			// This is an unrecoverable error.
			throw error;
		}

		// Pop until we find the statement list
		GrammarSymbol symbol = parseStack.pop();
		while (symbol != NonTerminal.statement_list_tail) {
			// If the parseStack is empty, then we didn't push a production
			// onto the parse stack properly
			// TODO: Compiler Bug
			if (parseStack.empty()) {
				throw error;
			}
			symbol = parseStack.pop();
		}
	}


}

