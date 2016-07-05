package symbolTable;

import lex.TokenType;

import java.util.LinkedList;

/**
 * Created by kathrynhodge on 3/20/16.
 */
public class ProcedureEntry extends SymbolTableEntry {

    int numberOfParameters;
    LinkedList parameterInfo;

    //ProcedureEntry (name [string], numberOfParameters [int], parameterInfo [List]
    public ProcedureEntry(String name, int numberOfParameters, LinkedList parameterInfo) {
        super(name, TokenType.PROCEDURE);
        this.numberOfParameters = numberOfParameters;
        this.parameterInfo = parameterInfo;
    }

    // Getters & Setters for NAME from SymbolTableEntry stay the same

    // Getters & Setters for TYPE from SymbolTableEntry stay the same

    // Getters & Setters for other properties
    public int getNumberOfParamters() { return numberOfParameters; }
    public void setNumberOfParamters(int i) { numberOfParameters = i;}
    public LinkedList getParameterInfo() { return parameterInfo; }
    public void setParameterInfo(LinkedList info) { parameterInfo = info; }

    // isVariable returns false --> already done in SymbolTableEntry

    // isKeyword returns false --> already done in SymbolTableEntry

    // This is a procedure
    public boolean isProcedure() { return true; }

    // isFunction returns false --> already done in SymbolTableEntry

    // isFunctionResult returns correct boolean --> already done in SymbolTableEntry

    // isParameter returns the correct boolean --> already done in SymbolTable Entry

    // isArray returns false --> already done in SymbolTableEntry

    // isConstant returns false --> already done in SymbolTableEntry

    // isReserved returns the correct boolean --> already done in SymbolTableEntry


}

