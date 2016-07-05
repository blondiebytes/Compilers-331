package symbolTable;

import lex.TokenType;

/**
 * Created by kathrynhodge on 3/20/16.
 */
public class IODeviceEntry extends SymbolTableEntry {

    // IODeviceEntry (name [string])
    public IODeviceEntry(String name) {
        super(name, TokenType.FILE);
    }

    // Getters & Setters for NAME from SymbolTableEntry stay the same

    // Getters & Setters for TYPE from SymbolTableEntry stay the same

    // isVariable returns false --> already done in SymbolTableEntry

    // isKeyword returns false --> already done in SymbolTableEntry

    // isProcedure returns false --> already done in SymbolTableEntry

    // isFunction returns false --> already done in SymbolTableEntry

    // isFunctionResult returns the correct boolean --> already done in SymbolTableEntry

    // isParameter returns the correct boolean --> already done in SymbolTable Entry

    // isArray returns false --> already done in SymbolTableEntry

    // isConstant returns false --> already done in SymbolTableEntry

    // isReserved returns the correct boolean --> already done in SymbolTableEntry


}
