package lex;

import symbolTable.ConstantEntry;

import java.util.StringJoiner;


//
//  Token.java
//  
//
//  Created by Nancy Ide on 2/18/07.
//  Copyright 2007 Vassar College. All rights reserved.
//

public class Token
{
	public enum OperatorType
	{
		EQUAL, NOTEQUAL, LESSTHAN, LESSTHANOREQUAL, GREATERTHAN, GREATERTHANOREQUAL, ADD,
		SUBTRACT, MULTIPLY, DIVIDE, INTEGERDIVIDE, MOD, AND, OR, NOT
	}

	private TokenType type;
	private String value;
	private OperatorType opType;
	private ConstantEntry entry;

	public Token()
	{
		super();
	}

	public Token(TokenType type, String value, OperatorType op)
	{
		this.type = type;
		this.value = value;
		this.opType = op;
	}

	public Token(Token token) {
		this.type = token.type;
		this.value = token.value;
		this.opType = token.opType;
	}

	public void setType(TokenType t)
	{
		type = t;
	}

	public void setValue(String s)
	{
		value = s;
	}

	public void setOpType(OperatorType op)
	{
		opType = op;
	}

	public TokenType getType()
	{
		return type;
	}

	public String getValue()
	{
		return value;
	}

	public OperatorType getOpType()
	{
		return opType;
	}

	public void clear()
	{
		type = null;
		value = null;
		opType = null;
	}

	public void copy(Token t)
	{
		this.type = t.getType();
		this.value = t.getValue();
	}

	public void print()
	{
		System.out.print("TOKEN : Type: " + this.type);
		if (this.value != null)
			System.out.println(" Value : " + this.value);
		else if (this.opType != null)
			System.out.println(" OpType : " + this.opType);
		System.out.println();
	}

	public String toString()
	{
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add(type.toString());
		if (value != null)
		{
			joiner.add(value);
		}
		if (opType != null)
		{
			joiner.add(opType.toString());
		}
		return joiner.toString();
	}

	public ConstantEntry getEntry()
	{
		return entry;
	}

	public void setEntry(ConstantEntry entry)
	{
		this.entry = entry;
	}

}

