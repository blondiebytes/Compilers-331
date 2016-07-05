package symbolTable;

import lex.TokenType;

/**
 * Created by kathrynhodge on 3/20/16.
 */
public class ConstantEntry extends SymbolTableEntry {

    boolean isParam;

   // ConstantEntry (name [string], type [TokenType])
    public ConstantEntry(String name, TokenType type) {
        super(name, type);
        isParam = false;
    }

    // Getters & Setters for NAME from SymbolTableEntry stay the same

    // Getters & Setters for TYPE from SymbolTableEntry stay the same

    // Getters & Setters for isParam
    public boolean isParameter() { return isParam; }
    public void setIsParameter(boolean b) { isParam = b;}

    // isVariable returns false --> already done in SymbolTableEntry

    // isKeyword returns false --> already done in SymbolTableEntry

    // isProcedure returns false --> already done in SymbolTableEntry

    // isFunction returns false --> already done in SymbolTableEntry

    // isFunctionResult returns correct boolean --> already done in SymbolTableEntry

    // isParameter returns the correct boolean --> already done in SymbolTable Entry

    // isArray returns false --> already done in SymbolTableEntry

    // This is a constant
    public boolean isConstant() { return true; }

    // isReserved returns the correct boolean --> already done in SymbolTableEntry

}
