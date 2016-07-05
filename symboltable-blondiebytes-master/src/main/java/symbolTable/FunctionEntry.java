package symbolTable;

import lex.TokenType;

import java.util.LinkedList;

/**
 * Created by kathrynhodge on 3/20/16.
 */
public class FunctionEntry extends SymbolTableEntry{

    // FunctionEntry (name [string], numberOfParameters [int], parameterInfo [List], result [VariableEntry])

    int numberOfParameters;
    LinkedList parameterInfo;
    VariableEntry result;

    public FunctionEntry(String name, int numberOfParameters, LinkedList parameterInfo, VariableEntry result) {
        super(name, TokenType.FUNCTION);
        this.numberOfParameters = numberOfParameters;
        this.parameterInfo = parameterInfo;
        this.result = result;
    }

    // Getters & Setters for NAME from SymbolTableEntry stay the same

    // Getters & Setters for TYPE from SymbolTableEntry stay the same

    // Getters & Setters for other properties
    public int getNumberOfParamters() { return numberOfParameters; }
    public void setNumberOfParamters(int i) { numberOfParameters = i;}
    public LinkedList getParameterInfo() { return parameterInfo; }
    public void setParameterInfo(LinkedList info) { parameterInfo = info; }
    public VariableEntry getResult() { return result; }
    public void setResult(VariableEntry res) { result = res; }

    // isVariable returns false --> already done in SymbolTableEntry

    // isKeyword returns false --> already done in SymbolTableEntry

    // isProcedure returns false --> already done in SymbolTableEntry

    // This is a function
    public boolean isFunction() { return true;}

    // isFunctionResult returns correct boolean --> already done in SymbolTableEntry

    // isParameter returns the correct boolean --> already done in SymbolTable Entry

    // isArray returns false --> already done in SymbolTableEntry

    // isConstant returns false --> already done in SymbolTableEntry

    // isReserved returns the correct boolean --> already done in SymbolTableEntry


}
