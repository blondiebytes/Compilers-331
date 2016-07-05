package symbolTable;

import lex.TokenType;

/**
 * Created by kathrynhodge on 3/20/16.
 */
public class ArrayEntry extends SymbolTableEntry{

    int address;
    int upperBound;
    int lowerBound;
    boolean isParam;

    // ArrayEntry (name [string], address [int], type [TokenType], upperBound [int], lowerBound [int])
    public ArrayEntry(String name, int address, TokenType type, int upperBound, int lowerBound) {
        super(name, type);
        this.address = address;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
        this.isParam = false;
    }

    // Getters & Setters for NAME from SymbolTableEntry stay the same

    // Getters & Setters for TYPE from SymbolTableEntry stay the same

    // Getters & Setters for the other properties
    public int getAddress() { return address; }
    public void setAddress(int address) {this.address = address;}
    public int getUBound() { return upperBound;}
    public void setUBound(int upperBound) {this.upperBound = upperBound;}
    public int getLBound() { return lowerBound;}
    public void setLBound(int lowerBound) {this.lowerBound = lowerBound;}
    public boolean isParameter() { return isParam; }
    public void setIsParameter(boolean b) { isParam = b;}

    // isVariable returns false --> already done in SymbolTableEntry

    // isKeyword returns false --> already done in SymbolTableEntry

    // isProcedure returns false --> already done in SymbolTableEntry

    // isFunction returns false --> already done in SymbolTableEntry

    // isFunctionResult returns the correct boolean --> already done in SymbolTableEntry

    // isParameter returns the correct boolean --> already done in SymbolTable Entry

    // This is an array
    public boolean isArray() { return true; }

    // isConstant returns false --> already done in SymbolTableEntry

    // isReserved returns the correct boolean --> already done in SymbolTableEntry

}