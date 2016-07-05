/*
 * Copyright 2016 Vassar College
 * All rights reserverd.
 */

package parser;

import grammar.NonTerminal;
import lex.TokenType;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class ParseTable {

	public static final String PARSE_TABLE_LOCATION = "parsetable.dat";

	private int nRows = 35;
	private int nColumns = 38;
	
	private int[][] parseTable ;

	public ParseTable()
	{
		
		parseTable = new int [nRows] [nColumns];
		init();
	}

	private void init()
	{
		try
		{
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			if (loader == null)
			{
				loader = ParseTable.class.getClassLoader();
			}
			URL url = loader.getResource(PARSE_TABLE_LOCATION);
			Scanner sc = new Scanner(url.openStream());
			for ( int i = 0; i < nRows; i++ )
			{
				for (int j = 0; j < nColumns; j++ )
				{  
					parseTable[i][j] = sc.nextInt();
				}
			}
		}
		catch (IOException ex)
		{
			System.out.println(ex);
		}
	}

	public int getEntry (TokenType t, NonTerminal n)
	{
		return parseTable[t.getIndex()][n.getIndex()];
	}

	public void printTable()
	{
		for ( int i = 0; i < nRows; i++ ) 
		{
			for (int j = 0; j < nColumns; j++ )
				System.out.print(" " + parseTable[i][j]);
			System.out.println();
		}
	}

}

