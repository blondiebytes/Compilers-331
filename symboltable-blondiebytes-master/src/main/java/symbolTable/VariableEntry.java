package symbolTable;

import lex.TokenType;

/**
 * Created by kathrynhodge on 3/20/16.
 */

public class VariableEntry extends SymbolTableEntry {

    int address;
    boolean isParam;
    boolean isFunctionResult;

    // VariableEntry (name [string], address [int], type [TokenType])
    public VariableEntry(String name, int address, TokenType type) {
        super(name, type);
        this.address = address;
        this.isParam = false;
        this.isFunctionResult = false;
    }

    // Getters & Setters for NAME from SymbolTableEntry stay the same

    // Getters & Setters for TYPE from SymbolTableEntry stay the same

    // Getters & Setters for the other properties
    public int getAddress() { return address; }
    public void setAddress(int address) {this.address = address;}


    public boolean isFunctionResult() { return isFunctionResult; }
    public void setIsFunctionResult(boolean b) { isFunctionResult = b; }

    public boolean isParameter() { return isParam; }
    public void setIsParameter(boolean b) { isParam = b;}

    // This is a variable
    public boolean isVariable() { return true; }

    // isKeyword returns false --> already done in SymbolTableEntry

    // isProcedure returns false --> already done in SymbolTableEntry

    // isFunction returns false --> already done in SymbolTableEntry

    // isFunctionResult returns the correct boolean --> already done in SymbolTableEntry

    // isParameter returns the correct boolean --> already done in SymbolTable Entry

    // isArray returns false --> already done in SymbolTableEntry

    // isConstant returns false --> already done in SymbolTableEntry

    // isReserved returns the correct boolean --> already done in SymbolTableEntry


}

