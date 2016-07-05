package symbolTable;

import lex.TokenType;

public class SymbolTableEntry {

	String name;
	TokenType type;
	boolean isReserved;
	boolean isVariable;

	public SymbolTableEntry () {}
	
	public SymbolTableEntry (String name) {
		this.name = name;
	} 
	
	public SymbolTableEntry (String name, TokenType type) {
		this.name = name;
		this.type = type;
		this.isReserved = false;
		this.isVariable = false;
	} 
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public TokenType getType() {
		return type;
	}
	public void setType(TokenType type) {
		this.type = type;
	}
	public boolean isVariable() { return isVariable; }
	public void setIsVariable(boolean b) {isVariable = b; }
	public boolean isKeyword() { return false; } 
	public boolean isProcedure() { return false; } 
	public boolean isFunction() { return false; }
	public boolean isFunctionResult() { return false; }
	public boolean isParameter() { return false; }
	public boolean isArray() { return false; } 
	public boolean isConstant() { return false; }
	public boolean isReserved() { return isReserved; }
	public void setIsReserved(boolean b) {isReserved = b; }
	
}
