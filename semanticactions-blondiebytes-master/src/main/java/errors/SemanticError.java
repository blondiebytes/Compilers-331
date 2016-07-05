package errors;

import lex.Token;

public class SemanticError extends CompilerError
{

	public SemanticError(Type errorNumber, String message)
	{
		super(errorNumber, message);
	}

	public Type getType()
	{
		return errorType;
	}

	// Factory methods to generate the semantic exception types.

	public static SemanticError MultipleDeclaration(String identifier, int line)
	{
		return new SemanticError(Type.MULTI_DECL,
				">>> ERROR on line " + line + " : Multiple declaration of " + identifier);
	}

	public static SemanticError UndeclaredVariable(String identifier, int line)
	{
		return new SemanticError(Type.UN_DECL,
				">>> ERROR on line " + line + " : Variable " + identifier + " is undefined");
	}

	public static SemanticError IllegalFunctionName(String identifier, int line)
	{
		return new SemanticError(Type.ILLEGAL_FUNCTION,
				">>> ERROR on line " + line + " :  Call to function " + identifier + " is illegal in this context");
	}

	public static SemanticError TypeMismatch(int line)
	{
		return new SemanticError(Type.ILLEGAL_TYPES,
				">>> ERROR on line " + line + " : Cannot assign real value to integer variable ");
	}

	public static SemanticError ETypeMismatch(int line)
	{
		return new SemanticError(Type.ILLEGAL_ETYPES,
				">>> ERROR on line " + line + " : Illegal operation ");
	}

	public static SemanticError InvalidSubscript(int line)
	{
		return new SemanticError(Type.INVALID_SUBSCRIPT,
				">>> ERROR on line " + line + " : Array indexes must be of type integer ");
	}

	public static SemanticError NotArray(int line)
	{
		return new SemanticError(Type.MISPLACED_SUBSCRIPT,
				">>> ERROR on line " + line + " : Subscript cannot be used with scalar variable ");
	}

	public static SemanticError IllegalProcedureCall(String identifier, int line)
	{
		return new SemanticError(Type.INVALID_PROC,
				">>> ERROR on line " + line + " : " + identifier + " is not a procedure");
	}

	public static SemanticError WrongNumParameters(String identifier, int line)
	{
		return new SemanticError(Type.TOO_MANY_PARMS,
				">>> ERROR on line " + line + " : Too many parameters for function" + identifier);
	}

	public static SemanticError BadParmType(int line)
	{
		return new SemanticError(Type.BAD_PARM_TYPE,
				">>> ERROR on line " + line + " : Parameter types do not match");
	}

	public static SemanticError BadArrayBounds(int line)
	{
		return new SemanticError(Type.BAD_ARRAY_BOUNDS,
				">>> ERROR on line " + line + " : Array parameter bounds do not match formal parameter");
	}

	public static SemanticError WrongNumberParms(String procName, int line)
	{
		return new SemanticError(Type.WRONG_NUM_PARMS,
				">>> ERROR on line " + line + " : Wrong number of parameters for procedure " + procName);
	}

	public static SemanticError BadMODoperands(int line)
	{
		return new SemanticError(Type.BAD_MOD_OPERANDS,
				">>> ERROR on line " + line + " : Operands of the MOD operator must both be of type integer");
	}

	public static SemanticError IllegalRelop(Token token, int line)
	{
		return new SemanticError(Type.BAD_RELOP,
				">>> ERROR on line " + line + " : Operands of " + token.getOpType() + " must be relational");
	}

	public static SemanticError ReservedName(String varName, int line)
	{
		return new SemanticError(Type.RESERVED_NAME,
				">>> ERROR on line " + line + " : Identifier " + varName + " is reserved.");
	}

	public static SemanticError badRelETYPE(int line) {
		return new SemanticError(Type.ILLEGAL_ETYPES,
				">>> ERROR on line 9 : Invalid use of arithmetic operator"
		);
	}
}
