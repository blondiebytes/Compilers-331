package symbolTable;

import lex.TokenType;

public class KeywordEntry extends SymbolTableEntry {

	public KeywordEntry(String name) {
		super(name);
	}
	
	public KeywordEntry(String name, TokenType keyword) {
		super(name, keyword);
	}

	public boolean isKeyword() { return true; } 
	
	public TokenType getKey() { 
		return type;
	}

	public void setKey(TokenType keyword) {
		this.type = keyword;
	}
	
	public void print () {
		
		System.out.println("Keyword Entry:");
		System.out.println("   Name    : " + this.getName());
		System.out.println("   Type    : " + this.getKey());
		System.out.println();
	}
}
